<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.bgColor }">
          <el-icon :size="24" :color="stat.color">
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
        <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
          <el-icon><component :is="stat.trend > 0 ? 'Top' : 'Bottom'" /></el-icon>
          {{ Math.abs(stat.trend) }}%
        </div>
      </div>
    </div>
    
    <div class="charts-row">
      <div class="chart-card">
        <h3>资源使用趋势</h3>
        <v-chart :option="resourceChart" autoresize style="height: 300px" />
      </div>
      <div class="chart-card">
        <h3>部署统计</h3>
        <v-chart :option="deployChart" autoresize style="height: 300px" />
      </div>
    </div>
    
    <div class="bottom-row">
      <div class="activity-card">
        <h3>最近构建</h3>
        <div class="activity-list">
          <div class="activity-item" v-for="item in recentBuilds" :key="item.id">
            <div class="activity-status" :class="getStatusClass(item.status)"></div>
            <div class="activity-content">
              <span class="activity-title">{{ item.pipelineName }}</span>
              <span class="activity-desc">{{ item.commitMessage }}</span>
            </div>
            <div class="activity-meta">
              <span class="mono">{{ item.duration }}s</span>
              <span>{{ formatTime(item.startTime) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="alert-card">
        <h3>活跃告警</h3>
        <div class="alert-list">
          <div class="alert-item" v-for="alert in alerts" :key="alert.id">
            <el-tag :type="getSeverityType(alert.severity)" size="small">
              {{ getSeverityLabel(alert.severity) }}
            </el-tag>
            <div class="alert-content">
              <span class="alert-title">{{ alert.name }}</span>
              <span class="alert-target">{{ alert.target }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { api } from '../stores/user'

use([CanvasRenderer, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent])

const stats = ref([
  { label: '流水线', value: '12', icon: 'Connection', color: '#0ea5e9', bgColor: 'rgba(14, 165, 233, 0.15)', trend: 8 },
  { label: '应用服务', value: '28', icon: 'Box', color: '#10b981', bgColor: 'rgba(16, 185, 129, 0.15)', trend: 12 },
  { label: '部署次数', value: '156', icon: 'Promotion', color: '#8b5cf6', bgColor: 'rgba(139, 92, 246, 0.15)', trend: -3 },
  { label: '告警数量', value: '3', icon: 'Bell', color: '#ef4444', bgColor: 'rgba(239, 68, 68, 0.15)', trend: -15 }
])

const recentBuilds = ref([])
const alerts = ref([])

const resourceChart = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['CPU', '内存', '磁盘'], textStyle: { color: '#94a3b8' }, bottom: 0 },
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
    axisLabel: { color: '#64748b', formatter: '{value}%' },
    splitLine: { lineStyle: { color: '#1e293b' } }
  },
  series: [
    {
      name: 'CPU',
      type: 'line',
      smooth: true,
      data: [30, 45, 62, 58, 72, 55, 42],
      lineStyle: { color: '#0ea5e9', width: 2 },
      areaStyle: { color: 'rgba(14, 165, 233, 0.1)' }
    },
    {
      name: '内存',
      type: 'line',
      smooth: true,
      data: [55, 58, 62, 68, 65, 70, 65],
      lineStyle: { color: '#8b5cf6', width: 2 },
      areaStyle: { color: 'rgba(139, 92, 246, 0.1)' }
    },
    {
      name: '磁盘',
      type: 'line',
      smooth: true,
      data: [40, 42, 45, 48, 50, 52, 55],
      lineStyle: { color: '#10b981', width: 2 },
      areaStyle: { color: 'rgba(16, 185, 129, 0.1)' }
    }
  ]
})

const deployChart = ref({
  tooltip: { trigger: 'item' },
  legend: { orient: 'vertical', right: 10, top: 'center', textStyle: { color: '#94a3b8' } },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#1e293b', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold', color: '#f1f5f9' } },
      labelLine: { show: false },
      data: [
        { value: 98, name: '成功', itemStyle: { color: '#10b981' } },
        { value: 35, name: '失败', itemStyle: { color: '#ef4444' } },
        { value: 23, name: '进行中', itemStyle: { color: '#0ea5e9' } }
      ]
    }
  ]
})

const getStatusClass = (status) => {
  const map = { 1: 'running', 2: 'success', 3: 'failed', 4: 'canceled' }
  return map[status] || 'pending'
}

const getSeverityType = (severity) => {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[severity] || 'info'
}

const getSeverityLabel = (severity) => {
  const map = { 1: '提示', 2: '警告', 3: '严重' }
  return map[severity] || '未知'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', { 
    month: 'numeric', 
    day: 'numeric', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const fetchData = async () => {
  try {
    const [buildRes, alertRes] = await Promise.all([
      api.get('/pipeline/0/runs?current=1&size=5'),
      api.get('/alert/list?current=1&size=5&status=1')
    ])
    recentBuilds.value = buildRes.data.records || []
    alerts.value = alertRes.data.records || []
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  border-color: var(--primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: var(--font-mono);
}

.stat-label {
  font-size: 13px;
  color: var(--text-muted);
}

.stat-trend {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.stat-trend.up { color: var(--success); }
.stat-trend.down { color: var(--danger); }

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.chart-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
}

.chart-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.bottom-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.activity-card,
.alert-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
}

.activity-card h3,
.alert-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
}

.activity-status {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.activity-status.running { background: var(--primary); animation: pulse 1.5s infinite; }
.activity-status.success { background: var(--success); }
.activity-status.failed { background: var(--danger); }
.activity-status.canceled { background: var(--text-muted); }
.activity-status.pending { background: var(--warning); }

.activity-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.activity-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.activity-desc {
  font-size: 12px;
  color: var(--text-muted);
}

.activity-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  font-size: 12px;
  color: var(--text-muted);
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
}

.alert-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.alert-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.alert-target {
  font-size: 12px;
  color: var(--text-muted);
}

@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
  .bottom-row { grid-template-columns: 1fr; }
}
</style>
