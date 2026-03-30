<template>
  <div class="application-page">
    <div class="page-header">
      <div class="header-left">
        <el-input 
          v-model="search" 
          placeholder="搜索应用..." 
          :prefix-icon="Search" 
          clearable
          @input="fetchData"
        />
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="openDialog()">新建应用</el-button>
      </div>
    </div>
    
    <div class="app-grid">
      <div class="app-card" v-for="item in list" :key="item.id">
        <div class="app-header">
          <div class="app-icon">
            <el-icon :size="32"><Box /></el-icon>
          </div>
          <div class="app-status">
            <span class="status-dot" :class="{ running: item.status === 1 }"></span>
            {{ item.status === 1 ? '运行中' : '已停止' }}
          </div>
        </div>
        
        <h3 class="app-name">{{ item.name }}</h3>
        <p class="app-desc">{{ item.description || '暂无描述' }}</p>
        
        <div class="app-meta">
          <div class="meta-item">
            <span class="label">镜像</span>
            <span class="value mono">{{ item.image }}</span>
          </div>
          <div class="meta-item">
            <span class="label">副本数</span>
            <span class="value">{{ item.replicas }}</span>
          </div>
          <div class="meta-item">
            <span class="label">端口</span>
            <span class="value mono">{{ item.port }}</span>
          </div>
          <div class="meta-item">
            <span class="label">命名空间</span>
            <span class="value">{{ item.namespace }}</span>
          </div>
        </div>
        
        <div class="app-resources">
          <div class="resource">
            <span class="resource-label">CPU</span>
            <div class="resource-bar">
              <div class="resource-fill" :style="{ width: '45%' }"></div>
            </div>
            <span class="resource-value">{{ item.cpuLimit }}</span>
          </div>
          <div class="resource">
            <span class="resource-label">内存</span>
            <div class="resource-bar">
              <div class="resource-fill memory" :style="{ width: '62%' }"></div>
            </div>
            <span class="resource-value">{{ item.memoryLimit }}</span>
          </div>
        </div>
        
        <div class="app-actions">
          <el-button size="small" type="primary" @click="goToDeploy(item)">部署</el-button>
          <el-button size="small" @click="openDialog(item)">编辑</el-button>
          <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(item)" />
        </div>
      </div>
    </div>
    
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
      :title="isEdit ? '编辑应用' : '新建应用'"
      width="600px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="应用名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入应用名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="镜像" prop="image">
          <el-input v-model="form.image" placeholder="nginx:latest" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="副本数" prop="replicas">
              <el-input-number v-model="form.replicas" :min="1" :max="10" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="端口" prop="port">
              <el-input v-model="form.port" placeholder="8080" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="CPU限制" prop="cpuLimit">
              <el-input v-model="form.cpuLimit" placeholder="500m" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="内存限制" prop="memoryLimit">
              <el-input v-model="form.memoryLimit" placeholder="1Gi" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="命名空间">
          <el-input v-model="form.namespace" placeholder="default" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Search, Box } from '@element-plus/icons-vue'
import { api } from '../stores/user'

const router = useRouter()
const search = ref('')
const current = ref(1)
const size = ref(12)
const total = ref(0)
const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({
  name: '',
  description: '',
  image: '',
  replicas: 1,
  port: '8080',
  cpuLimit: '500m',
  memoryLimit: '1Gi',
  namespace: 'default'
})

const rules = {
  name: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  image: [{ required: true, message: '请输入镜像地址', trigger: 'blur' }]
}

const fetchData = async () => {
  try {
    const res = await api.get('/application/list', { params: { current: current.value, size: size.value, name: search.value } })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取应用列表失败')
  }
}

const openDialog = (item = null) => {
  if (item) {
    isEdit.value = true
    Object.assign(form, item)
  } else {
    isEdit.value = false
    Object.assign(form, {
      name: '', description: '', image: '', replicas: 1,
      port: '8080', cpuLimit: '500m', memoryLimit: '1Gi', namespace: 'default'
    })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    if (isEdit.value) {
      await api.put('/application', form)
      ElMessage.success('更新成功')
    } else {
      await api.post('/application', form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const handleDelete = async (item) => {
  await ElMessageBox.confirm(`确定删除应用 "${item.name}" 吗？`, '警告', { type: 'warning' })
  try {
    await api.delete(`/application/${item.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const goToDeploy = (item) => {
  router.push({ path: '/deployment', query: { appId: item.id } })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.application-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header .el-input {
  width: 300px;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

.app-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  transition: all 0.3s ease;
}

.app-card:hover {
  border-color: var(--success);
  box-shadow: 0 0 20px rgba(16, 185, 129, 0.15);
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.app-icon {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(14, 165, 233, 0.2));
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--success);
}

.app-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--text-muted);
}

.status-dot.running {
  background: var(--success);
  animation: pulse 2s infinite;
}

.app-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.app-desc {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.app-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-item .label {
  font-size: 12px;
  color: var(--text-muted);
}

.meta-item .value {
  font-size: 13px;
  color: var(--text-primary);
}

.app-resources {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
}

.resource {
  display: flex;
  align-items: center;
  gap: 10px;
}

.resource-label {
  width: 40px;
  font-size: 12px;
  color: var(--text-muted);
}

.resource-bar {
  flex: 1;
  height: 6px;
  background: var(--bg-tertiary);
  border-radius: 3px;
  overflow: hidden;
}

.resource-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), var(--primary-light));
  border-radius: 3px;
  transition: width 0.3s ease;
}

.resource-fill.memory {
  background: linear-gradient(90deg, var(--info), #818cf8);
}

.resource-value {
  width: 50px;
  font-size: 12px;
  color: var(--text-secondary);
  text-align: right;
}

.app-actions {
  display: flex;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}
</style>
