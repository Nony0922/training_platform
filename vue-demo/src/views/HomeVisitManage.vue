<template>
  <div class="manage-page">
    <PageSkeleton v-if="pageLoading" variant="table" />
    <template v-else>
    <PageIntro text="记录并维护学生家访情况，按时间线卡片展示家访方式、内容与反馈。" />
    <div class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增家访记录</button>
    </div>
    <div v-if="!list.length" class="empty-tip">暂无家访记录</div>
    <div v-else class="timeline-list">
      <div v-for="item in list" :key="item.id" class="timeline-item">
        <div class="timeline-dot"></div>
        <div class="timeline-body">
          <div class="entity-card-head" style="margin-bottom: 8px">
            <div>
              <h3 class="entity-card-title">{{ item.studentName ?? '-' }}</h3>
              <p class="entity-card-sub">{{ item.visitDate ?? '-' }} · {{ formatCell(item.visitType, 'visitType') }}</p>
            </div>
            <span class="tag-chip tag-chip--purple">{{ item.teacherName ?? '-' }}</span>
          </div>
          <p style="margin: 0 0 8px; font-size: 14px; color: #374151; line-height: 1.6">{{ item.content || '暂无内容' }}</p>
          <p v-if="item.feedback" style="margin: 0; font-size: 13px; color: #6b7280">家长反馈：{{ item.feedback }}</p>
          <div class="entity-card-foot" style="border-top: none; padding-top: 10px; margin-top: 8px">
            <button class="btn btn-sm btn-info" @click="handleEdit(item)">编辑</button>
            <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
          </div>
        </div>
      </div>
    </div>
    </template>
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}家访记录</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>学生</label>
            <select v-model="form.studentId"><option :value="null">请选择</option><option v-for="s in students" :key="s.id" :value="s.id">{{ s.name }}{{ s.className ? `（${s.className}）` : '' }}</option></select>
          </div>
          <div v-if="!isTeacher" class="form-item">
            <label>教师</label>
            <select v-model="form.teacherId"><option :value="null">请选择</option><option v-for="t in teachers" :key="t.id" :value="t.id">{{ t.name }}</option></select>
          </div>
          <div class="form-item">
            <label>家访日期</label>
            <input v-model="form.visitDate" type="date" placeholder="请输入家访日期" />
          </div>
          <div class="form-item">
            <label>方式</label>
            <select v-model="form.visitType">
            <option :value="1">上门</option>
            <option :value="2">电话</option>
            <option :value="3">线上</option>
          </select>
          </div>
          <div class="form-item">
            <label>内容</label>
            <textarea v-model="form.content" placeholder="请输入内容" rows="3"></textarea>
          </div>
          <div class="form-item">
            <label>反馈</label>
            <textarea v-model="form.feedback" placeholder="请输入反馈" rows="3"></textarea>
          </div>
          <div class="form-item">
            <label>下一步计划</label>
            <input v-model="form.nextPlan" type="text" placeholder="请输入下一步计划" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn" @click="dialogVisible = false">取消</button>
          <button class="btn btn-primary" @click="handleSubmit" :disabled="loading">{{ loading ? '提交中...' : '确定' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getHomeVisitListApi, addHomeVisitApi, updateHomeVisitApi, deleteHomeVisitApi } from '@/api/homeVisit'
import { getStudentListApi } from '@/api/student'
import { getTeacherListApi } from '@/api/teacher'
import PageSkeleton from '@/components/PageSkeleton.vue'
import PageIntro from '@/components/PageIntro.vue'
import { usePageLoading } from '@/composables/usePageLoading'
import { useFormatCell } from '@/composables/useFormatCell'

const { pageLoading, withLoading } = usePageLoading()
const { formatCell } = useFormatCell()

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const students = ref([])
const teachers = ref([])

const isTeacher = computed(() => {
  try {
    const user = JSON.parse(localStorage.getItem('loginUser'))
    return user?.role === 'teacher'
  } catch {
    return false
  }
})

const form = reactive({
  id: null,
  studentId: null,
  teacherId: null,
  visitDate: '',
  visitType: 1,
  content: '',
  feedback: '',
  nextPlan: ''
})


const resetForm = () => {
  Object.assign(form, { id: null, studentId: null,
  teacherId: null,
  visitDate: '',
  visitType: 1,
  content: '',
  feedback: '',
  nextPlan: '' })
}

const loadList = () => withLoading(async () => {
  const res = await getHomeVisitListApi()
  list.value = res.data || []
})

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (item) => {
  isEdit.value = true
  Object.assign(form, { ...item })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.studentId) {
    alert('请选择学生')
    return
  }
  if (!isTeacher.value && !form.teacherId) {
    alert('请选择教师')
    return
  }
  if (!form.visitDate) {
    alert('请选择家访日期')
    return
  }

  try {
    loading.value = true
    if (isEdit.value) await updateHomeVisitApi(form)
    else await addHomeVisitApi(form)
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteHomeVisitApi(id)
    alert('删除成功')
    loadList()
  } catch (e) { alert(e.message) }
}

onMounted(async () => {
  loadList()
  students.value = (await getStudentListApi('homeroom')).data || []
  if (!isTeacher.value) {
    teachers.value = (await getTeacherListApi()).data || []
  }
})
</script>

<style scoped>
@import '@/assets/manage.css';
</style>
