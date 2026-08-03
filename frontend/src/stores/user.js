import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo } from '@/api/auth'

function safeParseUserInfo() {
  try {
    const raw = localStorage.getItem('userInfo')
    if (!raw) return null
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : null
  } catch (e) {
    console.warn('userInfo 解析失败，已重置:', e)
    localStorage.removeItem('userInfo')
    return null
  }
}

function safeGetToken() {
  try {
    const raw = localStorage.getItem('token')
    if (!raw || raw === 'null' || raw === 'undefined') return ''
    return raw
  } catch (e) {
    return ''
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(safeGetToken())
  const userInfo = ref(safeParseUserInfo())

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(data) {
    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      nickname: data.nickname,
      avatar: data.avatar
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  async function login(credentials) {
    const res = await loginApi(credentials)
    setAuth(res.data)
    return res
  }

  async function register(credentials) {
    const res = await registerApi(credentials)
    setAuth(res.data)
    return res
  }

  async function fetchUserInfo() {
    const res = await getUserInfo()
    userInfo.value = { ...userInfo.value, ...res.data }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    return res
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, isLoggedIn, login, register, fetchUserInfo, logout }
})
