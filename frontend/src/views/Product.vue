<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="商品名称" clearable style="width: 180px" @clear="load" />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 150px">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button :icon="Operation" @click="categoryVisible = true">分类管理</el-button>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增商品</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="spec" label="规格" width="90" />
      <el-table-column prop="unit" label="单位" width="70" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column label="售价" width="100">
        <template #default="{ row }"><span class="money">{{ fmtMoney(row.price) }}</span></template>
      </el-table-column>
      <el-table-column label="成本" width="100">
        <template #default="{ row }">{{ row.cost == null ? '-' : fmtMoney(row.cost) }}</template>
      </el-table-column>
      <el-table-column label="库存" width="90">
        <template #default="{ row }">
          <el-tag :type="row.stock <= 10 ? 'danger' : row.stock <= 30 ? 'warning' : 'success'" size="small">
            {{ row.stock }}
          </el-tag>
        </template>
      </el-table-column>
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

    <!-- 商品表单 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.spec" placeholder="如 1.5kg" style="width: 48%" />
          <el-input v-model="form.unit" placeholder="单位 袋/个/瓶" style="width: 48%; margin-left: 4%" />
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="成本">
          <el-input-number v-model="form.cost" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="上架销售">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分类管理 -->
    <el-drawer v-model="categoryVisible" title="商品分类管理" size="460px">
      <el-table :data="categories" border size="small">
        <el-table-column label="分类名称">
          <template #default="{ row }">
            <el-input v-if="row.editing" v-model="row.name" size="small" />
            <span v-else>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button v-if="!row.editing" link type="primary" @click="row.editing = true">改名</el-button>
            <el-button v-else link type="primary" @click="saveCategory(row)">保存</el-button>
            <el-button link type="danger" @click="removeCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 14px; display: flex; gap: 8px">
        <el-input v-model="newCategory" placeholder="新增分类名称" />
        <el-button type="primary" @click="addCategory">添加</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Operation } from '@element-plus/icons-vue'
import {
  pageProducts, createProduct, updateProduct, deleteProduct,
  listCategories, createCategory, updateCategory, deleteCategory
} from '../api'
import { fmtMoney } from '../utils/dict'

const list = ref([])
const total = ref(0)
const categories = ref([])
const loading = ref(false)
const submitting = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '', categoryId: null, status: null })

const dialogVisible = ref(false)
const categoryVisible = ref(false)
const newCategory = ref('')
const formRef = ref()
const form = reactive({
  id: null, categoryId: null, name: '', spec: '', unit: '',
  price: null, cost: null, stock: 0, status: 1, notes: ''
})
const rules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const data = await pageProducts(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  categories.value = await listCategories()
}

function openCreate() {
  Object.assign(form, {
    id: null, categoryId: null, name: '', spec: '', unit: '',
    price: null, cost: null, stock: 0, status: 1, notes: ''
  })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function onToggleStatus(row, value) {
  try {
    await updateProduct(row)
    ElMessage.success(value === 1 ? '已上架' : '已下架')
  } catch {
    row.status = value === 1 ? 0 : 1 // 失败回滚开关
  }
}

async function onSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    form.id ? await updateProduct(form) : await createProduct(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确定删除商品「${row.name}」？`, '提示', { type: 'warning' })
  await deleteProduct(row.id)
  ElMessage.success('删除成功')
  load()
}

async function addCategory() {
  if (!newCategory.value.trim()) return
  await createCategory({ name: newCategory.value.trim() })
  newCategory.value = ''
  ElMessage.success('分类已添加')
  loadCategories()
}

async function saveCategory(row) {
  await updateCategory({ id: row.id, name: row.name })
  row.editing = false
  ElMessage.success('已保存')
  loadCategories()
  load()
}

async function removeCategory(row) {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」？`, '提示', { type: 'warning' })
  await deleteCategory(row.id)
  ElMessage.success('删除成功')
  loadCategories()
}

onMounted(async () => {
  await loadCategories()
  load()
})
</script>
