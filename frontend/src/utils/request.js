import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
    response => {
      const res = response.data
      if (res.code !== 200) {
        if (!response.config.skipErrorTip) {
          ElMessage.error(res.message || '请求失败')
        }
        const err = new Error(res.message)
        err.code = res.code
        err.responseData = res
        return Promise.reject(err)
      }
      return res
    },
    error => {
      if (error.response?.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        ElMessage.warning('登录已过期，请重新登录')
        router.push({ name: 'Login' })
      } else if (!error.config?.skipErrorTip) {
        ElMessage.error(error.response?.data?.message || error.message || '网络错误')
      }
      return Promise.reject(error)
    }
)

export default request
