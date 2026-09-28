<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside :width="collapsed ? '64px' : '210px'" style="transition: width .25s; background: #fff; box-shadow: 2px 0 8px rgba(0,21,41,.06)">
      <div class="brand">
        <div class="logo">🐾</div>
        <span v-show="!collapsed" class="brand-text">宠物店管理系统</span>
      </div>
      <el-menu :default-active="$route.path" :collapse="collapsed" router class="menu">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部 -->
      <el-header class="header">
        <el-button text :icon="collapsed ? Expand : Fold" @click="collapsed = !collapsed" />
        <span class="page-title">{{ $route.meta.title }}</span>
        <div class="spacer" />
        <el-dropdown @command="onCommand">
          <span class="user-box">
            <el-icon><UserFilled /></el-icon>
            <span>{{ store.userInfo?.realName || '--' }}</span>
            <el-tag size="small" :type="store.isAdmin ? 'danger' : 'info'" style="margin-left: 6px">
              {{ store.isAdmin ? '管理员' : '店员' }}
            </el-tag>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 内容区 -->
      <el-main style="background: #f5f7fa; padding: 16px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <!-- 修改密码 -->
  <el-dialog v-model="pwdVisible" title="修改密码" width="380px">
    <el-form ref="pwdForm" :model="pwd" :rules="pwdRules" label-width="80px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="pwd.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwd.newPassword" type="password" show-password placeholder="6-20 位" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirm">
        <el-input v-model="pwd.confirm" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pwdVisible = false">取消</el-button>
      <el-button type="primary" :loading="pwdLoading" @click="submitPwd">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Expand, Fold, UserFilled } from '@element-plus/icons-vue'
import { changePassword } from '../api'
import { useUserStore } from '../stores/user'

const store = useUserStore()
const collapsed = ref(false)
const pwdVisible = ref(false)
const pwdLoading = ref(false)
const pwdForm = ref()
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度需在 6-20 位之间', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: (r, v, cb) => (v === pwd.newPassword ? cb() : cb(new Error('两次输入不一致'))), trigger: 'blur' }
  ]
}

// 菜单即路由：按 meta.role 过滤，店员隐藏「员工账号」
const router = useRouter()
const menus = computed(() => {
  const root = router.options.routes.find(r => r.path === '/')
  return (root?.children || [])
    .map(c => ({ path: '/' + c.path, title: c.meta?.title, icon: c.meta?.icon, role: c.meta?.role }))
    .filter(i => !i.role || store.isAdmin)
})

function onCommand(cmd) {
  if (cmd === 'password') {
    Object.assign(pwd, { oldPassword: '', newPassword: '', confirm: '' })
    pwdVisible.value = true
  } else {
    store.logout()
  }
}

async function submitPwd() {
  await pwdForm.value.validate()
  pwdLoading.value = true
  try {
    await changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
    ElMessage.success('密码已修改，请重新登录')
    pwdVisible.value = false
    store.logout()
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.brand {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid #f0f0f0;
}
.logo {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}
.brand-text {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  letter-spacing: .5px;
  white-space: nowrap;
}
.menu { border-right: none; }
.header {
  display: flex;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, .06);
}
.page-title { margin-left: 12px; font-size: 15px; font-weight: 600; color: #303133; }
.spacer { flex: 1; }
.user-box { display: flex; align-items: center; gap: 6px; cursor: pointer; color: #606266; font-size: 14px; }
</style>
