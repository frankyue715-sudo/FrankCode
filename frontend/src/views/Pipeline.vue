<template>
  <div class="pipeline-page">
    <div class="page-header">
      <div class="header-left">
        <el-input 
          v-model="search" 
          placeholder="搜索流水线..." 
          :prefix-icon="Search" 
          clearable
          @input="fetchData"
        />
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="openDialog()">新建流水线</el-button>
      </div>
    </div>
    
    <div class="pipeline-grid">
      <div class="pipeline-card" v-for="item in list" :key="item.id">
        <div class="pipeline-header">
          <div class="pipeline-icon">
            <el-icon :size="28"><Operation /></el-icon>
          </div>
          <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small">
            {{ item.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </div>
        
        <h3 class="pipeline-name">{{ item.name }}</h3>
        <p class="pipeline-desc">{{ item.description || '暂无描述' }}</p>
        
        <div class="pipeline-info">
          <div class="info-item">
            <el-icon><Branch /></el-icon>
            <span>{{ item.branch }}</span>
          </div>
          <div class="info-item">
            <el-icon><Link /></el-icon>
            <span class="mono">{{ getRepoName(item.repositoryUrl) }}</span>
          </div>
        </div>
        
        <div class="pipeline-actions">
          <el-button size="small" type="primary" @click="runPipeline(item)">
            <el-icon><CaretRight /></el-icon>
            执行
          </el-button>
          <el-button size="small" @click="openDialog(item)">编辑</el-button>
          <el-button size="small" @click="viewRuns(item)">历史</el-button>
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
      :title="isEdit ? '编辑流水线' : '新建流水线'"
      width="600px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="流水线名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入流水线名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="仓库地址" prop="repositoryUrl">
          <el-input v-model="form.repositoryUrl" placeholder="https://github.com/owner/repo" />
        </el-form-item>
        <el-form-item label="分支" prop="branch">
          <el-input v-model="form.branch" placeholder="main" />
        </el-form-item>
        <el-form-item label="构建脚本" prop="buildScript">
          <el-input v-model="form.buildScript" type="textarea" rows="3" placeholder="npm install && npm run build" />
        </el-form-item>
        <el-form-item label="镜像名称" prop="imageName">
          <el-input v-model="form.imageName" placeholder="myapp:latest" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="runDialogVisible" title="执行历史" width="800px">
      <el-table :data="runList" stripe>
        <el-table-column prop="pipelineName" label="流水线" />
        <el-table-column prop="branch" label="分支" width="100" />
        <el-table-column prop="commitId" label="提交ID" width="100">
          <template #default="{ row }">
            <span class="mono">{{ row.commitId?.substring(0, 7) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getRunStatusType(row.status)" size="small">
              {{ getRunStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="100">
          <template #default="{ row }">
            {{ row.duration }}s
          </template>
        </el-table-column>
        <el-table-column prop="triggerType" label="触发方式" width="100" />
        <el-table-column prop="startTime" label="开始时间">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Search, Operation, Branch, Link, CaretRight } from '@element-plus/icons-vue'
import { api } from '../stores/user'

const search = ref('')
const current = ref(1)
const size = ref(12)
const total = ref(0)
const list = ref([])
const dialogVisible = ref(false)
const runDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const currentPipeline = ref(null)
const runList = ref([])

const form = reactive({
  name: '',
  description: '',
  repositoryUrl: '',
  branch: 'main',
  buildScript: '',
  imageName: ''
})

const rules = {
  name: [{ required: true, message: '请输入流水线名称', trigger: 'blur' }],
  repositoryUrl: [{ required: true, message: '请输入仓库地址', trigger: 'blur' }]
}

const fetchData = async () => {
  try {
    const res = await api.get('/pipeline/list', { params: { current: current.value, size: size.value, name: search.value } })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取流水线列表失败')
  }
}

const openDialog = (item = null) => {
  if (item) {
    isEdit.value = true
    Object.assign(form, item)
  } else {
    isEdit.value = false
    Object.assign(form, { name: '', description: '', repositoryUrl: '', branch: 'main', buildScript: '', imageName: '' })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    if (isEdit.value) {
      await api.put('/pipeline', form)
      ElMessage.success('更新成功')
    } else {
      await api.post('/pipeline', form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const handleDelete = async (item) => {
  await ElMessageBox.confirm(`确定删除流水线 "${item.name}" 吗？`, '警告', { type: 'warning' })
  try {
    await api.delete(`/pipeline/${item.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const runPipeline = async (item) => {
  try {
    await api.post(`/pipeline/${item.id}/run`)
    ElMessage.success('流水线已触发')
    viewRuns(item)
  } catch (e) {
    ElMessage.error('触发失败')
  }
}

const viewRuns = async (item) => {
  currentPipeline.value = item
  try {
    const res = await api.get(`/pipeline/${item.id}/runs?current=1&size=20`)
    runList.value = res.data.records || []
    runDialogVisible.value = true
  } catch (e) {
    ElMessage.error('获取执行记录失败')
  }
}

const getRepoName = (url) => {
  if (!url) return '-'
  const match = url.match(/\/([^\/]+\/[^\/]+)$/)
  return match ? match[1] : url
}

const getRunStatusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'success', 3: 'danger', 4: 'warning' }
  return map[status] || 'info'
}

const getRunStatusLabel = (status) => {
  const map = { 0: '等待', 1: '运行中', 2: '成功', 3: '失败', 4: '取消' }
  return map[status] || '未知'
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
.pipeline-page {
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

.pipeline-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

.pipeline-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  transition: all 0.3s ease;
}

.pipeline-card:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-glow);
}

.pipeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pipeline-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.2), rgba(139, 92, 246, 0.2));
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.pipeline-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.pipeline-desc {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 16px;
  line-height: 1.5;
}

.pipeline-info {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.pipeline-actions {
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
