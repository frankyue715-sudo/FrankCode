<template>
  <div class="layout-container">
    <aside class="sidebar">
      <div class="sidebar-header">
        <el-icon :size="32" color="#0ea5e9"><Monitor /></el-icon>
        <span>DevOps</span>
      </div>
      
      <nav class="sidebar-nav">
        <router-link 
          v-for="item in navItems" 
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: $route.path === item.path }"
        >
          <el-icon :size="20"><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
      
      <div class="sidebar-footer">
        <div class="user-info">
          <el-avatar :size="36" style="background: linear-gradient(135deg, #0ea5e9, #8b5cf6)">
            {{ userStore.userInfo?.username?.[0]?.toUpperCase() || 'A' }}
          </el-avatar>
          <div class="user-detail">
            <span class="username">{{ userStore.userInfo?.nickname || 'Admin' }}</span>
            <span class="role">管理员</span>
          </div>
        </div>
        <el-button type="primary" link @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
        </el-button>
      </div>
    </aside>
    
    <main class="main-content">
      <header class="header">
        <div class="header-left">
          <h2>{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <el-badge :value="alertCount" :hidden="alertCount === 0">
            <el-button :icon="Bell" circle @click="$router.push('/alert')" />
          </el-badge>
          <el-dropdown @command="handleCommand">
            <el-button :icon="Setting" circle />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <div class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Monitor, DataAnalysis, Connection, Box, 
  Promotion, Bell, Setting, SwitchButton 
} from '@element-plus/icons-vue'
import { useUserStore, api } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const alertCount = ref(0)

const navItems = [
  { path: '/dashboard', label: '仪表盘', icon: DataAnalysis },
  { path: '/pipeline', label: 'CI/CD流水线', icon: Connection },
  { path: '/application', label: '应用管理', icon: Box },
  { path: '/deployment', label: '部署管理', icon: Promotion },
  { path: '/monitor', label: '系统监控', icon: 'Monitor' },
  { path: '/alert', label: '告警中心', icon: Bell }
]

const pageTitle = computed(() => {
  const item = navItems.find(i => i.path === route.path)
  return item?.label || 'DevOps Platform'
})

const getAlertCount = async () => {
  try {
    const res = await api.get('/alert/count')
    alertCount.value = res.data.activeCount || 0
  } catch (e) {
    console.error(e)
  }
}

const handleLogout = async () => {
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  
  userStore.logout()
  router.push('/login')
  ElMessage.success('已退出登录')
}

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  }
}

onMounted(() => {
  getAlertCount()
})
</script>

<style scoped>
.layout-container {
  display: flex;
  min-height: 100vh;
  background: var(--bg-primary);
}

.sidebar {
  width: 260px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  z-index: 100;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px;
  border-bottom: 1px solid var(--border-color);
}

.sidebar-header span {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--radius-md);
  transition: all 0.2s ease;
  font-size: 14px;
}

.nav-item:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.nav-item.active {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.15), rgba(14, 165, 233, 0.05));
  color: var(--primary);
  border: 1px solid rgba(14, 165, 233, 0.2);
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.role {
  font-size: 12px;
  color: var(--text-muted);
}

.main-content {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
}

.header {
  height: 64px;
  padding: 0 32px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 50;
}

.header-left h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content {
  flex: 1;
  padding: 24px 32px;
}

:deep(.el-button) {
  border: 1px solid var(--border-color);
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

:deep(.el-button:hover) {
  border-color: var(--primary);
  color: var(--primary);
}

:deep(.el-dropdown-menu) {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
}

:deep(.el-dropdown-menu__item) {
  color: var(--text-secondary);
}

:deep(.el-dropdown-menu__item:hover) {
  background: var(--bg-tertiary);
  color: var(--primary);
}
</style>
