<template>
  <div class="alert-page">
    <div class="page-header">
      <div class="header-left">
        <el-select v-model="queryStatus" placeholder="状态筛选" clearable @change="fetchData">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-select v-model="querySeverity" placeholder="级别筛选" clearable @change="fetchData">
          <el-option label="提示" :value="1" />
          <el-option label="警告" :value="2" />
          <el-option label="严重" :value="3" />
        </el-select>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="openDialog()">新建告警</el-button>
      </div>
    </div>
    
    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="name" label="告警名称" min-width="150" />
      <el-table-column prop="target" label="监控对象" width="120" />
      <el-table-column prop="metric" label="指标" width="100">
        <template #default="{ row }">
          <el-tag :type="getMetricType(row.metric)" size="small">
            {{ getMetricLabel(row.metric) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="条件" width="150">
        <template #default="{ row }">
          <span class="mono">{{ row.condition === 'gt' ? '>' : row.condition === 'lt' ? '<' : '=' }} {{ row.threshold }}%</span>
        </template>
      </el-table-column>
      <el-table-column prop="severity" label="级别" width="100">
        <template #default="{ row }">
          <el-tag :type="getSeverityType(row.severity)" size="small">
            {{ getSeverityLabel(row.severity) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-switch 
            v-model="row.status" 
            :active-value="1" 
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="notifyType" label="通知方式" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="current"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
      />
    </div>
    
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑告警' : '新建告警'"
      width="600px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="告警名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入告警名称" />
        </el-form-item>
        <el-form-item label="监控对象" prop="target">
          <el-input v-model="form.target" placeholder="server-1" />
        </el-form-item>
        <el-form-item label="监控指标" prop="metric">
          <el-select v-model="form.metric" placeholder="请选择指标">
            <el-option label="CPU使用率" value="cpu" />
            <el-option label="内存使用率" value="memory" />
            <el-option label="磁盘使用率" value="disk" />
          </el-select>
        </el-form-item>
        <el-form-item label="告警条件">
          <el-select v-model="form.condition" style="width: 80px">
            <el-option label=">" value="gt" />
            <el-option label="<" value="lt" />
            <el-option label="=" value="eq" />
          </el-select>
          <el-input-number v-model="form.threshold" :min="0" :max="100" style="width: 120px; margin-left: 10px" />
          <span style="margin-left: 10px">%</span>
        </el-form-item>
        <el-form-item label="告警级别" prop="severity">
          <el-radio-group v-model="form.severity">
            <el-radio :label="1">提示</el-radio>
            <el-radio :label="2">警告</el-radio>
            <el-radio :label="3">严重</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="通知方式">
          <el-select v-model="form.notifyType" placeholder="请选择">
            <el-option label="邮件" value="email" />
            <el-option label="短信" value="sms" />
            <el-option label="Webhook" value="webhook" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知人">
          <el-input v-model="form.notifyUsers" placeholder="user1,user2" />
        </el-form-item>
        <el-form-item label="告警消息">
          <el-input v-model="form.message" type="textarea" rows="2" placeholder="告警触发时发送的消息内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { api } from '../stores/user'

const loading = ref(false)
const current = ref(1)
const size = ref(10)
const total = ref(0)
const list = ref([])
const queryStatus = ref('')
const querySeverity = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({
  name: '',
  target: '',
  metric: 'cpu',
  condition: 'gt',
  threshold: 80,
  severity: 2,
  notifyType: 'email',
  notifyUsers: '',
  message: ''
})

const rules = {
  name: [{ required: true, message: '请输入告警名称', trigger: 'blur' }],
  target: [{ required: true, message: '请输入监控对象', trigger: 'blur' }],
  metric: [{ required: true, message: '请选择监控指标', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.get('/alert/list', { 
      params: { 
        current: current.value, 
        size: size.value,
        status: queryStatus.value,
        severity: querySeverity.value
      } 
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取告警列表失败')
  } finally {
    loading.value = false
  }
}

const openDialog = (item = null) => {
  if (item) {
    isEdit.value = true
    Object.assign(form, item)
  } else {
    isEdit.value = false
    Object.assign(form, {
      name: '', target: '', metric: 'cpu', condition: 'gt',
      threshold: 80, severity: 2, notifyType: 'email', notifyUsers: '', message: ''
    })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    if (isEdit.value) {
      await api.put('/alert', form)
      ElMessage.success('更新成功')
    } else {
      await api.post('/alert', form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const handleStatusChange = async (item) => {
  try {
    ElMessage.success(`告警已${item.status === 1 ? '启用' : '禁用'}`)
  } catch (e) {
    ElMessage.error('操作失败')
    fetchData()
  }
}

const getMetricType = (metric) => {
  const map = { cpu: 'warning', memory: 'danger', disk: 'info' }
  return map[metric] || 'info'
}

const getMetricLabel = (metric) => {
  const map = { cpu: 'CPU', memory: '内存', disk: '磁盘' }
  return map[metric] || metric
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
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.alert-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  gap: 12px;
}

.header-left .el-select {
  width: 150px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

:deep(.el-select) {
  width: 100%;
}
</style>
