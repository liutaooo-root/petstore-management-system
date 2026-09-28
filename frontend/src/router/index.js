import { createRouter, createWebHashHistory } from 'vue-router'
import Layout from '../layout/Layout.vue'

/**
 * hash 路由：部署时静态资源由 jar 直出，无需服务端路由兜底
 * meta.role 控制菜单与入口（用户管理仅管理员可见）
 */
const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { title: '登录' } },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '数据看板', icon: 'Odometer' } },
      { path: 'customer', component: () => import('../views/Customer.vue'), meta: { title: '客户管理', icon: 'User' } },
      { path: 'pet', component: () => import('../views/Pet.vue'), meta: { title: '宠物档案', icon: 'Notebook' } },
      { path: 'product', component: () => import('../views/Product.vue'), meta: { title: '商品管理', icon: 'Goods' } },
      { path: 'service', component: () => import('../views/ServiceView.vue'), meta: { title: '服务管理', icon: 'Brush' } },
      { path: 'appointment', component: () => import('../views/Appointment.vue'), meta: { title: '预约管理', icon: 'Calendar' } },
      { path: 'bill', component: () => import('../views/Bill.vue'), meta: { title: '账单收银', icon: 'Tickets' } },
      { path: 'user', component: () => import('../views/User.vue'), meta: { title: '员工账号', icon: 'UserFilled', role: 'ADMIN' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach(to => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/dashboard'
  }
  // 角色拦截：无权限页面跳回看板
  if (to.meta.role && JSON.parse(localStorage.getItem('userInfo') || 'null')?.role !== to.meta.role) {
    return '/dashboard'
  }
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + '宠物店管理系统'
})

export default router
