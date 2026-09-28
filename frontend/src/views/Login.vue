<template>
  <div class="login-page">
    <div class="login-box">
      <div class="brand">
        <div class="logo">🐾</div>
        <div>
          <h1>宠物店管理系统</h1>
          <p>Pet Store Management System</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="onSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" :prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="onSubmit">
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>
      </el-form>

      <div class="tip">
        默认账号：<b>admin</b> / <b>123456</b>（管理员）　<small>staff / 123456（店员）</small>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await login(form)
    store.setLogin(data)
    ElMessage.success(`欢迎回来，${data.realName}`)
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f4ff 100%);
}
.login-box {
  width: 380px;
  background: #fff;
  border-radius: 12px;
  padding: 36px 32px 26px;
  box-shadow: 0 8px 30px rgba(0, 21, 41, .12);
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 26px;
}
.logo {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.brand h1 { font-size: 22px; color: #303133; letter-spacing: 2px; }
.brand p { font-size: 12px; color: #909399; margin-top: 3px; }
.tip {
  margin-top: 18px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
