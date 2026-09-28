<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="姓名 / 手机号" clearable style="width: 200px" @clear="load" />
      <el-select v-model="query.level" placeholder="会员等级" clearable style="width: 130px">
        <el-option v-for="(t, l) in LEVEL_TAG" :key="l" :label="l" :value="l" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" :icon="Plus" @click="openCreate">新增客户</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="name" label="姓名" width="110" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="性别" width="70">
        <template #default="{ row }">{{ GENDER[row.gender] || '-' }}</template>
      </el-table-column>
      <el-table-column label="会员等级" width="100">
        <template #default="{ row }">
          <el-tag :type="LEVEL_TAG[row.level]" size="small">{{ row.level }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="points" label="积分" width="90" />
      <el-table-column prop="birthday" label="生日" width="120" />
      <el-table-column prop="notes" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 14px; justify-content: flex-end"
      layout="total, sizes, prev, pager, next"
      :total="total"
      v-model:current-page="query.current"
      v-model:page-size="query.size"
      @current-change="load"
      @size-change="load"
    />

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑客户' : '新增客户'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="11 位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" placeholder="如：宠物过敏史、偏好品牌" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉：客户信息 + 名下宠物 + 近期账单 -->
    <el-drawer v-model="detailVisible" title="客户详情" size="620px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ detail.customer?.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.customer?.phone }}</el-descriptions-item>
        <el-descriptions-item label="会员等级">
          <el-tag :type="LEVEL_TAG[detail.customer?.level]" size="small">{{ detail.customer?.level }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="积分">{{ detail.customer?.points }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detail.customer?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.customer?.notes || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">名下宠物（{{ detail.pets?.length || 0 }}）</el-divider>
      <el-table :data="detail.pets" size="small" border>
        <el-table-column prop="name" label="名字" width="90" />
        <el-table-column prop="species" label="类型" width="80" />
        <el-table-column prop="breed" label="品种" />
        <el-table-column label="性别" width="70">
          <template #default="{ row }">{{ PET_GENDER[row.gender] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="weight" label="体重(kg)" width="90" />
      </el-table>

      <el-divider content-position="left">近期账单</el-divider>
      <el-table :data="detail.recentBills" size="small" border>
        <el-table-column prop="billNo" label="单号" width="170" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">{{ BILL_TYPE[row.type] }}</template>
        </el-table-column>
        <el-table-column label="实收" width="100">
          <template #default="{ row }">
            <span class="money">{{ fmtMoney(row.payAmount ?? 0) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="BILL_STATUS_TAG[row.status]" size="small">{{ BILL_STATUS[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="开单时间" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pageCustomers, createCustomer, updateCustomer, deleteCustomer, getCustomer } from '../api'
import { GENDER, PET_GENDER, LEVEL_TAG, BILL_TYPE, BILL_STATUS, BILL_STATUS_TAG, fmtMoney } from '../utils/dict'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '', level: '' })

const dialogVisible = ref(false)
const detailVisible = ref(false)
const detail = ref({})
const formRef = ref()
const form = reactive({ id: null, name: '', phone: '', gender: 1, birthday: '', notes: '' })
const rules = {
  name: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

async function load() {
  loading.value = true
  try {
    const data = await pageCustomers(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { id: null, name: '', phone: '', gender: 1, birthday: '', notes: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function openDetail(row) {
  detail.value = await getCustomer(row.id)
  detailVisible.value = true
}

async function onSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    form.id ? await updateCustomer(form) : await createCustomer(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确定删除客户「${row.name}」？`, '提示', { type: 'warning' })
  await deleteCustomer(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
