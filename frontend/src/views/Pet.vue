<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="宠物名 / 品种" clearable style="width: 180px" @clear="load" />
      <el-select v-model="query.customerId" placeholder="按主人筛选" filterable clearable style="width: 200px">
        <el-option v-for="c in customers" :key="c.id" :label="c.name + ' ' + c.phone" :value="c.id" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" :icon="Plus" @click="openCreate">新增宠物</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="name" label="名字" width="100" />
      <el-table-column prop="species" label="类型" width="80" />
      <el-table-column prop="breed" label="品种" width="120" />
      <el-table-column label="性别" width="70">
        <template #default="{ row }">{{ PET_GENDER[row.gender] || '-' }}</template>
      </el-table-column>
      <el-table-column prop="birthDate" label="出生日期" width="120" />
      <el-table-column prop="weight" label="体重(kg)" width="90" />
      <el-table-column label="绝育" width="80">
        <template #default="{ row }">
          <el-tag :type="row.sterilized === 1 ? 'success' : 'info'" size="small">
            {{ row.sterilized === 1 ? '已绝育' : '未绝育' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="主人" width="120">
        <template #default="{ row }">{{ row.customerName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="notes" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑宠物档案' : '新增宠物档案'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="主人" prop="customerId">
          <el-select v-model="form.customerId" filterable placeholder="选择客户" style="width: 100%">
            <el-option v-for="c in customers" :key="c.id" :label="c.name + ' ' + c.phone" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="宠物名" prop="name">
          <el-input v-model="form.name" placeholder="如：豆豆" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.species" style="width: 100%">
            <el-option v-for="s in SPECIES" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="form.breed" placeholder="如：比熊、布偶" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">公</el-radio>
            <el-radio :value="2">母</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input-number v-model="form.weight" :min="0" :precision="2" :step="0.1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="已绝育">
          <el-switch v-model="form.sterilized" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" placeholder="如：对鸡肉过敏" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pagePets, createPet, updatePet, deletePet, listCustomers } from '../api'
import { PET_GENDER, SPECIES } from '../utils/dict'

const list = ref([])
const total = ref(0)
const customers = ref([])
const loading = ref(false)
const submitting = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '', customerId: null })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null, customerId: null, name: '', species: '狗', breed: '',
  gender: 1, birthDate: '', weight: null, sterilized: 0, notes: ''
})
const rules = {
  customerId: [{ required: true, message: '请选择宠物主人', trigger: 'change' }],
  name: [{ required: true, message: '请输入宠物名', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const data = await pagePets(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, {
    id: null, customerId: null, name: '', species: '狗', breed: '',
    gender: 1, birthDate: '', weight: null, sterilized: 0, notes: ''
  })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function onSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    form.id ? await updatePet(form) : await createPet(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确定删除宠物「${row.name}」的档案？`, '提示', { type: 'warning' })
  await deletePet(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  customers.value = await listCustomers()
  load()
})
</script>
