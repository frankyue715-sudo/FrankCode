import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const login = async (username, password) => {
    const res = await api.post('/auth/login', { username, password })
    if (res.data.token) {
      token.value = res.data.token
      localStorage.setItem('token', res.data.token)
      userInfo.value = res.data
      return true
    }
    return false
  }

  const getUserInfo = async () => {
    try {
      const res = await api.get('/auth/info')
      userInfo.value = res.data
    } catch (e) {
      logout()
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    login,
    getUserInfo,
    logout
  }
})

export { api }
