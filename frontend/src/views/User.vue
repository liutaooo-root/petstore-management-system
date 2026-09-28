<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="登录名 / 姓名" clearable style="width: 200px" @clear="load" />
      <el-button type="primary" :icon="Search" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" :icon="Plus" @click="openCreate">新增员工账号</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="username" label="登录名" width="130" />
      <el-table-column prop="realName" label="姓名" width="110" />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">{{ ROLE[row.role] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="v => onToggle(row, v)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="warning" @click="onResetPwd(row)">重置密码</el-button>
          <el-button v-if="row.id !== myId" link type="danger" @click="onDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑员工账号' : '新增员工账号'" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="登录名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="登录账号，创建后不可修改" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="管理员（可管理员工与全部数据）" value="ADMIN" />
            <el-option label="店员（仅业务操作）" value="STAFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码">
          <el-input value="123456" disabled />
        </el-form-item>
        <el-form-item v-else label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pageUsers, createUser, updateUser, resetUserPassword, toggleUserStatus, deleteUser } from '../api'
import { useUserStore } from '../stores/user'
import { ROLE } from '../utils/dict'

const store = useUserStore()
const myId = computed(() => store.userInfo?.id)

const list = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '' })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, username: '', realName: '', role: 'STAFF', phone: '', status: 1 })
const rules = {
  username: [{ required: true, message: '请输入登录名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const data = await pageUsers(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { id: null, username: '', realName: '', role: 'STAFF', phone: '', status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function onToggle(row, value) {
  try {
    await toggleUserStatus(row.id)
    ElMessage.success(value === 1 ? '账号已启用' : '账号已停用')
  } catch {
    row.status = value === 1 ? 0 : 1
  }
}

async function onResetPwd(row) {
  const { value } = await ElMessageBox.prompt(
    `将「${row.realName}」的密码重置为：`, '重置密码', {
      inputValue: '123456',
      inputPattern: /^.{6,20}$/,
      inputErrorMessage: '密码长度需在 6-20 位之间'
    })
  await resetUserPassword(row.id, value)
  ElMessage.success('密码已重置')
}

async function onSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    form.id ? await updateUser(form) : await createUser(form)
    ElMessage.success(form.id ? '已保存' : '账号已创建，初始密码 123456')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确定删除账号「${row.username}」？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
