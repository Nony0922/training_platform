<template>
  <div class="manage-page">
    <PageSkeleton v-if="pageLoading" variant="grouped" />
    <template v-else>
    <PageIntro text="按课程与班级查看考勤记录，状态以标签芯片形式展示，便于快速浏览。" />
    <div class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增考勤</button>
    </div>
    <div v-if="!groups.length" class="empty-tip">暂无考勤数据</div>

    <div v-for="course in groups" :key="course.courseId" class="data-group">
      <div class="group-header">
        <span class="group-title">{{ course.courseName }}</span>
        <span class="group-meta">{{ countAttendanceRows(course) }} 条记录</span>
      </div>
      <div v-for="klass in course.classes" :key="klass.classId" class="subgroup">
        <div class="subgroup-header">
          <span class="subgroup-title">{{ klass.className }}</span>
          <span class="subgroup-meta">{{ klass.rows.length }} 条</span>
        </div>
        <div class="chip-grid">
          <div v-for="item in klass.rows" :key="item.id" class="attendance-chip">
            <span class="attendance-chip-name">{{ item.studentName ?? '-' }}</span>
            <span class="tag-chip" :class="attendanceChipClass(item.status)">{{ formatCell(item.status, 'attendanceStatus') }}</span>
            <span style="font-size: 12px; color: #9ca3af">{{ item.attendDate ?? '-' }}</span>
            <button class="btn btn-sm btn-info" style="margin-left: auto" @click="handleEdit(item)">编辑</button>
            <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
          </div>
        </div>
      </div>
    </div>
    </template>
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}考勤</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>学生</label>
            <select v-model="form.studentId"><option :value="null">请选择</option><option v-for="s in students" :key="s.id" :value="s.id">{{ s.name }}</option></select>
          </div>
          <div class="form-item">
            <label>班级</label>
            <select v-model="form.classId"><option :value="null">请选择</option><option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</option></select>
          </div>
          <div class="form-item">
            <label>课程</label>
            <select v-model="form.courseId"><option :value="null">请选择</option><option v-for="c in courses" :key="c.id" :value="c.id">{{ c.name }}</option></select>
          </div>
          <div class="form-item">
            <label>日期</label>
            <input v-model="form.attendDate" type="date" placeholder="请输入日期" />
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="form.status">
            <option :value="1">正常</option>
            <option :value="2">迟到</option>
            <option :value="3">早退</option>
            <option :value="4">缺勤</option>
            <option :value="5">请假</option>
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
import { getAttendanceListApi, addAttendanceApi, updateAttendanceApi, deleteAttendanceApi } from '@/api/attendance'
import { getStudentListApi } from '@/api/student'
import { getClazzListApi } from '@/api/clazz'
import { getCourseListApi } from '@/api/course'
import { getScopeModeFromRoute } from '@/composables/useTeacherScope'
import { groupAttendanceByCourseClass } from '@/utils/groupTeachingData'
import PageSkeleton from '@/components/PageSkeleton.vue'
import PageIntro from '@/components/PageIntro.vue'
import { usePageLoading } from '@/composables/usePageLoading'
import { useFormatCell } from '@/composables/useFormatCell'

const route = useRoute()
const scopeMode = () => getScopeModeFromRoute(route)
const { pageLoading, withLoading } = usePageLoading()
const { formatCell } = useFormatCell()

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const students = ref([])
const classes = ref([])
const courses = ref([])

const groups = computed(() => groupAttendanceByCourseClass(list.value))

const countAttendanceRows = (course) =>
  course.classes.reduce((sum, klass) => sum + klass.rows.length, 0)

const form = reactive({
  id: null,
  studentId: null,
  classId: null,
  courseId: null,
  attendDate: '',
  status: 1,
  remark: ''
})


const attendanceChipClass = (status) => {
  if (status === 1) return 'tag-chip--green'
  if (status === 5) return 'tag-chip--purple'
  return 'tag-chip--orange'
}

const resetForm = () => {
  Object.assign(form, { id: null, studentId: null,
  classId: null,
  courseId: null,
  attendDate: '',
  status: 1,
  remark: '' })
}

const loadList = () => withLoading(async () => {
  const res = await getAttendanceListApi(scopeMode())
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

  try {
    loading.value = true
    if (isEdit.value) await updateAttendanceApi(form)
    else await addAttendanceApi(form)
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteAttendanceApi(id)
    alert('删除成功')
    loadList()
  } catch (e) { alert(e.message) }
}

onMounted(async () => {
  loadList()
  students.value = (await getStudentListApi(scopeMode())).data || []
classes.value = (await getClazzListApi()).data || []
courses.value = (await getCourseListApi()).data || []
})
</script>

<style scoped>
@import '@/assets/manage.css';
</style>
