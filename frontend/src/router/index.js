import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/components/AppLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: 'AI 创作' }
      },
      {
        path: 'editor/:id?',
        name: 'Editor',
        component: () => import('@/views/Editor.vue'),
        meta: { title: '编辑预览' }
      },
      {
        path: 'library',
        name: 'Library',
        component: () => import('@/views/Library.vue'),
        meta: { title: '我的作品' }
      },
      {
        path: 'templates',
        name: 'Templates',
        component: () => import('@/views/Templates.vue'),
        meta: { title: '模板广场' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  // 防御：token 存在但为空字符串或 'null'/'undefined' 字符串的情况，统一视为未登录
  const hasValidToken = !!(userStore.token && typeof userStore.token === 'string' && userStore.token.trim().length > 0 && userStore.token !== 'null' && userStore.token !== 'undefined')

  if (!to.meta.public && !hasValidToken) {
    // 防止死循环：已经在 Login 就不再 push
    if (to.name !== 'Login') {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  } else if (to.name === 'Login' && hasValidToken) {
    // 已经登录的访问 Login，跳 Home，但 from=Home 时避免来回跳
    if (from.name !== 'Home') {
      next({ name: 'Home' })
      return
    }
  }
  next()
})

export default router
