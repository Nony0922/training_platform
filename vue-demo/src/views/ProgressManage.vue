<template>
  <div class="manage-page">
    <PageIntro text="跟踪各班级课程教学进度，进度条列表直观展示章节完成情况。" />
    <StatCards :items="progressStats" />
    <div class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增教学进度</button>
    </div>
    <div v-if="!list.length" class="empty-tip">暂无教学进度数据</div>
    <div v-else class="progress-list">
      <div v-for="item in list" :key="item.id" class="progress-item">
        <div class="progress-item-head">
          <div>
            <div class="progress-item-title">{{ item.courseName ?? '-' }} · {{ item.chapter ?? '-' }}</div>
            <div class="progress-item-sub">
              {{ item.className ?? '-' }}
              <template v-if="!isTeacher"> · {{ item.teacherName ?? '-' }}</template>
            </div>
          </div>
          <span :class="['badge', item.status === 2 ? 'badge-success' : item.status === 1 ? 'badge-warning' : 'badge-info']">
            {{ formatCell(item.status, 'progressStatus') }}
          </span>
        </div>
        <div class="progress-track">
          <div class="progress-track-fill" :class="`progress-track-fill--${item.status ?? 0}`" />
        </div>
        <div class="progress-dates">
          <span>计划：{{ item.plannedDate ?? '-' }}</span>
          <span>实际：{{ item.actualDate ?? '-' }}</span>
        </div>
        <div class="entity-card-foot" style="border-top: none; padding-top: 8px; margin-top: 4px">
          <button class="btn btn-sm btn-info" @click="handleEdit(item)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
        </div>
      </div>
    </div>
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}教学进度</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>班级</label>
            <select v-model="form.classId"><option :value="null">请选择</option><option v-for="c in formClasses" :key="c.id" :value="c.id">{{ c.name }}</option></select>
          </div>
          <div class="form-item">
            <label>课程</label>
            <select v-model="form.courseId" @change="onCourseChange"><option :value="null">请选择</option><option v-for="c in courses" :key="c.id" :value="c.id">{{ c.name }}</option></select>
          </div>
          <div v-if="!isTeacher" class="form-item">
            <label>教师</label>
            <select v-model="form.teacherId"><option :value="null">请选择</option><option v-for="t in teachers" :key="t.id" :value="t.id">{{ t.name }}</option></select>
          </div>
          <div class="form-item">
            <label>章节</label>
            <input v-model="form.chapter" type="text" placeholder="请输入章节" />
          </div>
          <div class="form-item">
            <label>内容</label>
            <textarea v-model="form.content" placeholder="请输入内容" rows="3"></textarea>
          </div>
          <div class="form-item">
            <label>计划日期</label>
            <input v-model="form.plannedDate" type="date" placeholder="请输入计划日期" />
          </div>
          <div class="form-item">
            <label>实际日期</label>
            <input v-model="form.actualDate" type="date" placeholder="请输入实际日期" />
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="form.status">
            <option :value="0">未开始</option>
            <option :value="1">进行中</option>
            <option :value="2">已完成</option>
          </select>
          </div>
          <div class="form-item">
            <label>备注</label>
            <input v-model="form.remark" type="text" placeholder="请输入备注" />
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
import { useRoute } from 'vue-router'
import { getProgressListApi, addProgressApi, updateProgressApi, deleteProgressApi } from '@/api/progress'
import { getClazzListApi } from '@/api/clazz'
import { getCourseListApi } from '@/api/course'
import { getTeacherListApi } from '@/api/teacher'
import { getScheduleListApi } from '@/api/schedule'
import { getScopeModeFromRoute } from '@/composables/useTeacherScope'
import PageIntro from '@/components/PageIntro.vue'
import StatCards from '@/components/StatCards.vue'
import { useFormatCell } from '@/composables/useFormatCell'

const { formatCell } = useFormatCell()

const route = useRoute()
const scopeMode = () => getScopeModeFromRoute(route)

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const classes = ref([])
const courses = ref([])
const teachers = ref([])
const schedules = ref([])

const progressStats = computed(() => [
  { label: '进度条目', value: list.value.length, icon: '📈', tone: 'purple' },
  { label: '未开始', value: list.value.filter(p => p.status === 0).length, icon: '⏳', tone: 'orange' },
  { label: '进行中', value: list.value.filter(p => p.status === 1).length, icon: '🔄', tone: 'blue' },
  { label: '已完成', value: list.value.filter(p => p.status === 2).length, icon: '✅', tone: 'green' }
])

const isTeacher = computed(() => {
  try {
    const user = JSON.parse(localStorage.getItem('loginUser'))
    return user?.role === 'teacher'
  } catch {
    return false
  }
})

const formClasses = computed(() => {
  if (!isTeacher.value) return classes.value
  const classIds = new Set(
    schedules.value
      .filter(s => !form.courseId || s.courseId === form.courseId)
      .map(s => s.classId)
  )
  return classes.value.filter(c => classIds.has(c.id))
})

const form = reactive({
  id: null,
  classId: null,
  courseId: null,
  teacherId: null,
  chapter: '',
  content: '',
  plannedDate: '',
  actualDate: '',
  status: 0,
  remark: ''
})

const onCourseChange = () => {
  if (isTeacher.value) {
    form.classId = null
  }
}

const resetForm = () => {
  Object.assign(form, { id: null, classId: null,
  courseId: null,
  teacherId: null,
  chapter: '',
  content: '',
  plannedDate: '',
  actualDate: '',
  status: 0,
  remark: '' })
}

const loadList = async () => {
  try {
    const res = await getProgressListApi(scopeMode())
    list.value = res.data || []
  } catch (e) { alert(e.message) }
}

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
  if (!form.courseId) {
    alert('请选择课程')
    return
  }
  if (!form.classId) {
    alert('请选择班级')
    return
  }
  if (!isTeacher.value && !form.teacherId) {
    alert('请选择教师')
    return
  }

  try {
    loading.value = true
    if (isEdit.value) await updateProgressApi(form, scopeMode())
    else await addProgressApi(form, scopeMode())
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteProgressApi(id, scopeMode())
    alert('删除成功')
    loadList()
  } catch (e) { alert(e.message) }
}

onMounted(async () => {
  loadList()
  classes.value = (await getClazzListApi()).data || []
  courses.value = (await getCourseListApi()).data || []
  if (isTeacher.value) {
    schedules.value = (await getScheduleListApi()).data || []
  } else {
    teachers.value = (await getTeacherListApi()).data || []
  }
})
</script>

<style scoped>
@import '@/assets/manage.css';
</style>
