import { defineStore } from 'pinia'

/** 登录用户状态（localStorage 持久化，刷新不掉线） */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),
  getters: {
    isAdmin: state => state.userInfo?.role === 'ADMIN'
  },
  actions: {
    setLogin(data) {
      this.token = data.token
      this.userInfo = data
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(data))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.hash = '#/login'
      window.location.reload()
    }
  }
})
