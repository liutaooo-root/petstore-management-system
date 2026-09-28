import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * axios 统一封装：
 * - 请求注入 Bearer Token
 * - 响应解包 Result：code=200 直接返回 data，401 踢回登录页，其余弹错误
 */
const request = axios.create({ baseURL: '/api', timeout: 15000 })

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

request.interceptors.response.use(
  res => {
    const body = res.data
    if (body.code === 200) {
      return body.data
    }
    if (body.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.warning('登录已过期，请重新登录')
      window.location.hash = '#/login'
      window.location.reload()
      return Promise.reject(body)
    }
    ElMessage.error(body.msg || '操作失败')
    return Promise.reject(body)
  },
  err => {
    ElMessage.error(err.message?.includes('timeout') ? '请求超时，请稍后重试' : '网络异常，请稍后重试')
    return Promise.reject(err)
  }
)

export default request
