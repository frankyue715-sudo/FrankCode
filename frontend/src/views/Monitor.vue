<template>
  <div class="monitor-page">
    <div class="overview-cards">
      <div class="overview-card" v-for="item in overview" :key="item.label">
        <div class="overview-icon" :style="{ background: item.bgColor }">
          <el-icon :size="24" :color="item.color"><component :is="item.icon" /></el-icon>
        </div>
        <div class="overview-content">
          <span class="overview-value">{{ item.value }}</span>
          <span class="overview-label">{{ item.label }}</span>
        </div>
      </div>
    </div>
    
    <div class="monitor-grid">
      <div class="monitor-card">
        <h3>
          <el-icon><Cpu /></el-icon>
          CPU 使用率
        </h3>
        <v-chart :option="cpuChart" autoresize style="height: 280px" />
      </div>
      
      <div class="monitor-card">
        <h3>
          <el-icon><MemoryStick /></el-icon>
          内存使用率
        </h3>
        <v-chart :option="memoryChart" autoresize style="height: 280px" />
      </div>
      
      <div class="monitor-card">
        <h3>
          <el-icon><Monitor /></el-icon>
          磁盘使用率
        </h3>
        <v-chart :option="diskChart" autoresize style="height: 280px" />
      </div>
      
      <div class="monitor-card">
        <h3>
          <el-icon><Connection /></el-icon>
          网络流量
        </h3>
        <v-chart :option="networkChart" autoresize style="height: 280px" />
      </div>
    </div>
    
    <div class="server-list">
      <div class="list-header">
        <h3>服务器列表</h3>
        <el-button type="primary" size="small" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      
      <el-table :data="serverList" stripe>
        <el-table-column prop="target" label="服务器" />
        <el-table-column prop="targetType" label="类型" width="100" />
        <el-table-column prop="cpuUsage" label="CPU" width="150">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress 
                :percentage="Math.round(row.cpuUsage || 0)" 
                :color="getProgressColor(row.cpuUsage)"
                :stroke-width="8"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="memoryUsage" label="内存" width="150">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress 
                :percentage="Math.round(row.memoryUsage || 0)" 
                :color="getProgressColor(row.memoryUsage)"
                :stroke-width="8"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="diskUsage" label="磁盘" width="150">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress 
                :percentage="Math.round(row.diskUsage || 0)" 
                :color="getProgressColor(row.diskUsage)"
                :stroke-width="8"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="networkIn" label="入站" width="100">
          <template #default="{ row }">
            <span class="mono">{{ formatBytes(row.networkIn) }}/s</span>
          </template>
        </el-table-column>
        <el-table-column prop="networkOut" label="出站" width="100">
          <template #default="{ row }">
            <span class="mono">{{ formatBytes(row.networkOut) }}/s</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, GaugeChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Cpu, MemoryStick, Monitor, Connection, Refresh } from '@element-plus/icons-vue'
import { api } from '../stores/user'

use([CanvasRenderer, LineChart, GaugeChart, GridComponent, TooltipComponent, LegendComponent])

const overview = ref([])
const serverList = ref([])
let timer = null

const cpuChart = ref({
  series: [{
    type: 'gauge',
    startAngle: 200,
    endAngle: -20,
    min: 0,
    max: 100,
    splitNumber: 10,
    itemStyle: { color: '#0ea5e9' },
    progress: { show: true, width: 20 },
    pointer: { show: false },
    axisLine: { lineStyle: { width: 20, color: [[1, '#334155']] } },
    axisTick: { show: false },
    splitLine: { show: false },
    axisLabel: { show: false },
    detail: { 
      valueAnimation: true, 
      fontSize: 36, 
      color: '#f1f5f9',
      formatter: '{value}%',
      offsetCenter: [0, '30%']
    },
    data: [{ value: 45 }]
  }]
})

const memoryChart = ref({
  series: [{
    type: 'gauge',
    startAngle: 200,
    endAngle: -20,
    min: 0,
    max: 100,
    itemStyle: { color: '#8b5cf6' },
    progress: { show: true, width: 20 },
    pointer: { show: false },
    axisLine: { lineStyle: { width: 20, color: [[1, '#334155']] } },
    axisTick: { show: false },
    splitLine: { show: false },
    axisLabel: { show: false },
    detail: { 
      valueAnimation: true, 
      fontSize: 36, 
      color: '#f1f5f9',
      formatter: '{value}%',
      offsetCenter: [0, '30%']
    },
    data: [{ value: 62 }]
  }]
})

const diskChart = ref({
  series: [{
    type: 'gauge',
    startAngle: 200,
    endAngle: -20,
    min: 0,
    max: 100,
    itemStyle: { color: '#10b981' },
    progress: { show: true, width: 20 },
    pointer: { show: false },
    axisLine: { lineStyle: { width: 20, color: [[1, '#334155']] } },
    axisTick: { show: false },
    splitLine: { show: false },
    axisLabel: { show: false },
    detail: { 
      valueAnimation: true, 
      fontSize: 36, 
      color: '#f1f5f9',
      formatter: '{value}%',
      offsetCenter: [0, '30%']
    },
    data: [{ value: 38 }]
  }]
})

const networkChart = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['入站', '出站'], textStyle: { color: '#94a3b8' }, bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
    axisLine: { lineStyle: { color: '#334155' } },
    axisLabel: { color: '#64748b' }
  },
  yAxis: {
    type: 'value',
    axisLine: { lineStyle: { color: '#334155' } },
    axisLabel: { color: '#64748b', formatter: '{value} MB/s' },
    splitLine: { lineStyle: { color: '#1e293b' } }
  },
  series: [
    {
      name: '入站',
      type: 'line',
      smooth: true,
      data: [120, 150, 280, 320, 250, 180, 140],
      lineStyle: { color: '#0ea5e9', width: 2 },
      areaStyle: { color: 'rgba(14, 165, 233, 0.1)' }
    },
    {
      name: '出站',
      type: 'line',
      smooth: true,
      data: [80, 120, 180, 220, 190, 150, 100],
      lineStyle: { color: '#10b981', width: 2 },
      areaStyle: { color: 'rgba(16, 185, 129, 0.1)' }
    }
  ]
})

const fetchOverview = async () => {
  try {
    const res = await api.get('/monitor/overview')
    const data = res.data
    overview.value = [
      { label: '服务器', value: data.totalServers, icon: 'Monitor', color: '#0ea5e9', bgColor: 'rgba(14, 165, 233, 0.15)' },
      { label: '应用', value: data.totalApps, icon: 'Box', color: '#10b981', bgColor: 'rgba(16, 185, 129, 0.15)' },
      { label: '平均CPU', value: data.cpuUsage + '%', icon: 'Cpu', color: '#8b5cf6', bgColor: 'rgba(139, 92, 246, 0.15)' },
      { label: '平均内存', value: data.memoryUsage + '%', icon: 'MemoryStick', color: '#f59e0b', bgColor: 'rgba(245, 158, 11, 0.15)' }
    ]
    
    cpuChart.value.series[0].data[0].value = Math.round(data.cpuUsage)
    memoryChart.value.series[0].data[0].value = Math.round(data.memoryUsage)
    diskChart.value.series[0].data[0].value = Math.round(data.diskUsage)
  } catch (e) {
    console.error(e)
  }
}

const fetchServerList = async () => {
  try {
    const res = await api.get('/monitor/list', { params: { current: 1, size: 20 } })
    serverList.value = res.data.records || []
  } catch (e) {
    console.error(e)
  }
}

const refreshData = () => {
  fetchOverview()
  fetchServerList()
}

const getProgressColor = (value) => {
  if (value > 80) return '#ef4444'
  if (value > 60) return '#f59e0b'
  return '#10b981'
}

const formatBytes = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchOverview()
  fetchServerList()
  timer = setInterval(refreshData, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.monitor-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.overview-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.overview-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.overview-content {
  display: flex;
  flex-direction: column;
}

.overview-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: var(--font-mono);
}

.overview-label {
  font-size: 13px;
  color: var(--text-muted);
}

.monitor-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.monitor-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
}

.monitor-card h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.server-list {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.list-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.progress-cell {
  padding: 4px 0;
}

:deep(.el-progress-bar__outer) {
  background: var(--bg-tertiary);
}

@media (max-width: 1200px) {
  .overview-cards { grid-template-columns: repeat(2, 1fr); }
  .monitor-grid { grid-template-columns: 1fr; }
}
</style>
