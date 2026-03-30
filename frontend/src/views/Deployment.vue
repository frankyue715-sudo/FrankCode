<template>
  <div class="deployment-page">
    <div class="page-header">
      <div class="header-left">
        <el-select v-model="queryEnv" placeholder="环境筛选" clearable @change="fetchData">
          <el-option label="开发环境" value="dev" />
          <el-option label="测试环境" value="test" />
          <el-option label="生产环境" value="prod" />
        </el-select>
        <el-button type="primary" :icon="Plus" @click="dialogVisible = true">新建部署</el-button>
      </div>
    </div>
    
    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="applicationName" label="应用名称" min-width="150" />
      <el-table-column prop="environment" label="环境" width="100">
        <template #default="{ row }">
          <el-tag :type="getEnvType(row.environment)" size="small">
            {{ getEnvLabel(row.environment) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="120">
        <template #default="{ row }">
          <span class="mono">{{ row.version }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="image" label="镜像" min-width="200">
        <template #default="{ row }">
          <span class="mono text-muted">{{ row.image }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deployedBy" label="部署人" width="100" />
      <el-table-column prop="startTime" label="开始时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button 
            size="small" 
            type="warning" 
            v-if="row.status === 2"
            @click="handleRollback(row)"
          >
            回滚
          </el-button>
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
    
    <el-dialog v-model="dialogVisible" title="新建部署" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="选择应用" prop="applicationId">
          <el-select v-model="form.applicationId" placeholder="请选择应用" filterable>
            <el-option 
              v-for="app in appList" 
              :key="app.id" 
              :label="app.name" 
              :value="app.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="部署环境" prop="environment">
          <el-radio-group v-model="form.environment">
            <el-radio label="dev">开发环境</el-radio>
            <el-radio label="test">测试环境</el-radio>
            <el-radio label="prod">生产环境</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="form.version" placeholder="v1.0.0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDeploy">确定部署</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { api } from '../stores/user'

const route = useRoute()
const loading = ref(false)
const current = ref(1)
const size = ref(10)
const total = ref(0)
const list = ref([])
const appList = ref([])
const queryEnv = ref('')
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive({
  applicationId: null,
  environment: 'dev',
  version: 'v1.0.0',
  deployedBy: 'admin'
})

const rules = {
  applicationId: [{ required: true, message: '请选择应用', trigger: 'change' }],
  environment: [{ required: true, message: '请选择环境', trigger: 'change' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.get('/deployment/list', { 
      params: { 
        current: current.value, 
        size: size.value,
        environment: queryEnv.value
      } 
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取部署记录失败')
  } finally {
    loading.value = false
  }
}

const fetchApps = async () => {
  try {
    const res = await api.get('/application/list?current=1&size=100')
    appList.value = res.data.records || []
  } catch (e) {
    console.error(e)
  }
}

const handleDeploy = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    await api.post('/deployment', form)
    ElMessage.success('部署已启动')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '部署失败')
  }
}

const handleRollback = async (row) => {
  await ElMessageBox.confirm(`确定回滚到上一个版本吗？`, '提示', { type: 'warning' })
  try {
    await api.post(`/deployment/${row.id}/rollback`)
    ElMessage.success('回滚已启动')
    fetchData()
  } catch (e) {
    ElMessage.error('回滚失败')
  }
}

const getEnvType = (env) => {
  const map = { dev: 'info', test: 'warning', prod: 'danger' }
  return map[env] || 'info'
}

const getEnvLabel = (env) => {
  const map = { dev: '开发', test: '测试', prod: '生产' }
  return map[env] || env
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'success', 3: 'danger', 4: 'warning' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { 0: '等待', 1: '部署中', 2: '成功', 3: '失败', 4: '回滚中' }
  return map[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchData()
  fetchApps()
  
  if (route.query.appId) {
    form.applicationId = parseInt(route.query.appId)
    dialogVisible.value = true
  }
})
</script>

<style scoped>
.deployment-page {
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

:deep(.el-select) {
  width: 200px;
}

:deep(.el-radio-group) {
  display: flex;
  gap: 16px;
}
</style>
