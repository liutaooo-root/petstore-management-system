<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="服务名称" clearable style="width: 200px" @clear="load" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" :icon="Plus" @click="openCreate">新增服务</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="name" label="服务名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="价格" width="110">
        <template #default="{ row }"><span class="money">{{ fmtMoney(row.price) }}</span></template>
      </el-table-column>
      <el-table-column label="时长" width="100">
        <template #default="{ row }">{{ row.durationMin ? row.durationMin + ' 分钟' : '-' }}</template>
      </el-table-column>
      <el-table-column prop="description" label="服务说明" min-width="240" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="v => onToggleStatus(row, v)" />
        </template>
      </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑服务' : '新增服务'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="form.name" placeholder="如：基础洗护（小型犬）" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="form.durationMin" :min="0" :step="15" style="width: 100%" />
        </el-form-item>
        <el-form-item label="服务说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="服务包含内容、注意事项" />
        </el-form-item>
        <el-form-item label="上架销售">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { pageServices, createService, updateService, deleteService } from '../api'
import { fmtMoney } from '../utils/dict'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '', status: null })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', price: null, durationMin: 60, description: '', status: 1 })
const rules = {
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入服务价格', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const data = await pageServices(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { id: null, name: '', price: null, durationMin: 60, description: '', status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function onToggleStatus(row, value) {
  try {
    await updateService(row)
    ElMessage.success(value === 1 ? '已上架' : '已下架')
  } catch {
    row.status = value === 1 ? 0 : 1
  }
}

async function onSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    form.id ? await updateService(form) : await createService(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确定删除服务「${row.name}」？`, '提示', { type: 'warning' })
  await deleteService(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
