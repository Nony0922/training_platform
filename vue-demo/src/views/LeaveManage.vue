<template>
  <div class="manage-page">
    <PageSkeleton v-if="pageLoading" variant="cards" />
    <template v-else>
    <div class="kanban-board">
      <div class="kanban-column kanban-column--pending">
        <div class="kanban-column-head">
          <span class="kanban-column-title">待审批</span>
          <span class="kanban-column-count">{{ pendingList.length }}</span>
        </div>
        <div
          v-for="item in pendingList"
          :key="item.id"
          class="kanban-card"
          @click="handleApprove(item)"
        >
          <div class="kanban-card-title">{{ item.studentName ?? '-' }}</div>
          <div class="kanban-card-meta">
            {{ formatCell(item.leaveType, 'leaveType') }} · {{ item.startDate }} ~ {{ item.endDate }}<br>
            申请人：{{ item.applicantName ?? '-' }}<br>
            {{ item.reason || '无原因说明' }}
          </div>
        </div>
      </div>
      <div class="kanban-column kanban-column--approved">
        <div class="kanban-column-head">
          <span class="kanban-column-title">已通过</span>
          <span class="kanban-column-count">{{ approvedList.length }}</span>
        </div>
        <div
          v-for="item in approvedList"
          :key="item.id"
          class="kanban-card"
          @click="handleView(item)"
        >
          <div class="kanban-card-title">{{ item.studentName ?? '-' }}</div>
          <div class="kanban-card-meta">
            {{ formatCell(item.leaveType, 'leaveType') }} · {{ item.startDate }} ~ {{ item.endDate }}<br>
            申请人：{{ item.applicantName ?? '-' }}<br>
            {{ item.reason || '无原因说明' }}
          </div>
        </div>
      </div>
      <div class="kanban-column kanban-column--rejected">
        <div class="kanban-column-head">
          <span class="kanban-column-title">已驳回</span>
          <span class="kanban-column-count">{{ rejectedList.length }}</span>
        </div>
        <div
          v-for="item in rejectedList"
          :key="item.id"
          class="kanban-card"
          @click="handleView(item)"
        >
          <div class="kanban-card-title">{{ item.studentName ?? '-' }}</div>
          <div class="kanban-card-meta">
            {{ formatCell(item.leaveType, 'leaveType') }} · {{ item.startDate }} ~ {{ item.endDate }}<br>
            申请人：{{ item.applicantName ?? '-' }}<br>
            {{ item.reason || '无原因说明' }}
          </div>
        </div>
      </div>
      <div class="kanban-column kanban-column--withdrawn">
        <div class="kanban-column-head">
          <span class="kanban-column-title">已撤回</span>
          <span class="kanban-column-count">{{ withdrawnList.length }}</span>
        </div>
        <div
          v-for="item in withdrawnList"
          :key="item.id"
          class="kanban-card"
          @click="handleView(item)"
        >
          <div class="kanban-card-title">{{ item.studentName ?? '-' }}</div>
          <div class="kanban-card-meta">
            {{ formatCell(item.leaveType, 'leaveType') }} · {{ item.startDate }} ~ {{ item.endDate }}<br>
            申请人：{{ item.applicantName ?? '-' }}
          </div>
        </div>
      </div>
    </div>
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isApprove ? '审批请假' : '请假详情' }}</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>学生</label>
            <input :value="form.studentName" type="text" readonly />
          </div>
          <div class="form-item">
            <label>申请人</label>
            <input :value="form.applicantName" type="text" readonly />
          </div>
          <div class="form-item">
            <label>类型</label>
            <input :value="formatCell(form.leaveType, 'leaveType')" type="text" readonly />
          </div>
          <div class="form-item">
            <label>开始日期</label>
            <input :value="form.startDate" type="text" readonly />
          </div>
          <div class="form-item">
            <label>结束日期</label>
            <input :value="form.endDate" type="text" readonly />
          </div>
          <div class="form-item">
            <label>原因</label>
            <textarea :value="form.reason" readonly rows="3"></textarea>
          </div>
          <div v-if="isApprove" class="form-item">
            <label>审批结果</label>
            <select v-model="form.status">
              <option :value="1">通过</option>
              <option :value="2">驳回</option>
            </select>
          </div>
          <div v-else class="form-item">
            <label>状态</label>
            <input :value="formatCell(form.status, 'leaveStatus')" type="text" readonly />
          </div>
          <div class="form-item">
            <label>审批备注</label>
            <textarea v-if="isApprove" v-model="form.remark" placeholder="请输入审批备注" rows="2"></textarea>
            <input v-else :value="form.remark || '-'" type="text" readonly />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn" @click="dialogVisible = false">{{ isApprove ? '取消' : '关闭' }}</button>
          <button v-if="isApprove" class="btn btn-primary" @click="handleSubmit" :disabled="loading">{{ loading ? '提交中...' : '确定' }}</button>
        </div>
      </div>
    </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getLeaveListApi, updateLeaveApi } from '@/api/leave'
import PageSkeleton from '@/components/PageSkeleton.vue'
import { usePageLoading } from '@/composables/usePageLoading'
import { useFormatCell } from '@/composables/useFormatCell'

const { formatCell } = useFormatCell()

const { pageLoading, withLoading } = usePageLoading()

const list = ref([])
const dialogVisible = ref(false)
const isApprove = ref(false)
const loading = ref(false)

const pendingList = computed(() => list.value.filter(item => item.status === 0))
const approvedList = computed(() => list.value.filter(item => item.status === 1))
const rejectedList = computed(() => list.value.filter(item => item.status === 2))
const withdrawnList = computed(() => list.value.filter(item => item.status === 3))

const form = reactive({
  id: null,
  studentName: '',
  applicantName: '',
  leaveType: 1,
  startDate: '',
  endDate: '',
  reason: '',
  status: 1,
  remark: ''
})

const getUser = () => {
  try {
    return JSON.parse(localStorage.getItem('loginUser'))
  } catch {
    return null
  }
}

const loadList = () => withLoading(async () => {
  try {
    const res = await getLeaveListApi()
    list.value = res.data || []
  } catch (e) { alert(e.message) }
})

const handleApprove = (item) => {
  isApprove.value = true
  Object.assign(form, { ...item, status: 1, remark: '' })
  dialogVisible.value = true
}

const handleView = (item) => {
  isApprove.value = false
  Object.assign(form, { ...item })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (form.status !== 1 && form.status !== 2) {
    alert('请选择通过或驳回')
    return
  }
  try {
    loading.value = true
    const user = getUser()
    const now = new Date()
    const approveTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`
    await updateLeaveApi({
      ...form,
      approverId: user?.id,
      approveTime
    })
    alert('审批成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

onMounted(loadList)
</script>

<style scoped>
@import '@/assets/manage.css';
input[readonly], textarea[readonly] {
  background: #f9fafb;
  color: #6b7280;
}
</style>
