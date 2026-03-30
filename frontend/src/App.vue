<template>
  <div class="app-container">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from './stores/user'

const userStore = useUserStore()

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    userStore.getUserInfo()
  }
})
</script>

<style>
.app-container {
  min-height: 100vh;
  background: var(--bg-primary);
}
</style>
