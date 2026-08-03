<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/home')">
          <el-icon :size="24"><EditPen /></el-icon>
          <span>AI 小说创作</span>
        </div>
        <nav class="nav" :class="{ open: menuOpen }">
          <router-link v-for="item in navItems" :key="item.path" :to="item.path" @click="menuOpen = false">
            <el-icon><component :is="item.icon" /></el-icon>
            {{ item.label }}
          </router-link>
        </nav>
        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32">{{ nickname.charAt(0) }}</el-avatar>
              <span class="username">{{ nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-icon class="menu-toggle" @click="menuOpen = !menuOpen"><Menu /></el-icon>
        </div>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const menuOpen = ref(false)

const nickname = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '用户')

const navItems = [
  { path: '/home', label: 'AI 创作', icon: 'MagicStick' },
  { path: '/editor', label: '编辑预览', icon: 'Edit' },
  { path: '/library', label: '我的作品', icon: 'Collection' },
  { path: '/templates', label: '模板广场', icon: 'Grid' }
]

async function handleLogout() {
  await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
  cursor: pointer;
}

.nav {
  display: flex;
  gap: 8px;

  a {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 8px 16px;
    border-radius: 8px;
    color: #606266;
    transition: all 0.2s;

    &:hover, &.router-link-active {
      color: #409eff;
      background: #ecf5ff;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #606266;
}

.menu-toggle {
  display: none;
  font-size: 24px;
  cursor: pointer;
}

.main {
  flex: 1;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .nav {
    display: none;
    position: absolute;
    top: 60px;
    left: 0;
    right: 0;
    background: #fff;
    flex-direction: column;
    padding: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

    &.open { display: flex; }
  }

  .menu-toggle { display: block; }
  .username { display: none; }
}
</style>
