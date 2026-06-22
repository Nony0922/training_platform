<template>
  <div class="teacher-schedule-page">
    <PageSkeleton v-if="pageLoading" variant="grouped" />
    <template v-else>
      <div class="page-desc">
        <p>维护全校课程表安排，支持按学期筛选；左侧为可视化周课表，右侧为课程列表，可进行新增、编辑与删除。</p>
      </div>

      <div class="toolbar">
        <label>学期</label>
        <select v-model="semester" @change="loadSchedules">
          <option value="">全部学期</option>
          <option v-for="s in semesters" :key="s" :value="s">{{ s }}</option>
        </select>
        <button class="btn btn-primary" :disabled="pageLoading" @click="loadSchedules">
          {{ pageLoading ? '加载中...' : '刷新' }}
        </button>
        <button class="btn btn-success" @click="handleAdd">新增课程表</button>
        <span class="stat-tag">共 {{ schedules.length }} 节课</span>
      </div>

      <div v-if="!pageLoading && !schedules.length" class="empty-tip">
        暂无课程表数据，请点击「新增课程表」添加排课记录。
      </div>

      <div v-else class="main-layout">
        <div class="timetable-panel">
          <div class="panel-title">周课表</div>
          <div class="timetable-wrap">
            <table class="timetable">
              <thead>
                <tr>
                  <th class="time-col">时间</th>
                  <th v-for="day in weekdays" :key="day.value">{{ day.label }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="slot in timeSlots" :key="slot.key">
                  <td class="time-col">{{ slot.label }}</td>
                  <td v-for="day in weekdays" :key="day.value" class="slot-cell">
                    <div
                      v-for="item in getCellSchedules(day.value, slot.key)"
                      :key="item.id"
                      class="schedule-block mine"
                    >
                      <div class="block-title">{{ item.courseName }}</div>
                      <div class="block-meta">{{ item.className }} · {{ item.teacherName }}</div>
                      <div class="block-meta">{{ item.room }}</div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="list-panel">
          <div class="panel-title">课程列表</div>
          <div class="schedule-list">
            <div v-for="item in sortedSchedules" :key="item.id" class="list-item mine">
              <div class="list-day">{{ weekdayText(item.weekday) }}</div>
              <div class="list-body">
                <div class="list-title">{{ item.courseName }}</div>
                <div class="list-meta">{{ shortTime(item.startTime) }} - {{ shortTime(item.endTime) }} · {{ item.className }}</div>
                <div class="list-meta">{{ item.teacherName }} · {{ item.room }} · {{ item.semester || '-' }}</div>
                <div class="list-actions">
                  <button class="btn btn-sm btn-info" @click="handleEdit(item)">编辑</button>
                  <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}课程表</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>班级</label>
            <select v-model="form.classId">
              <option :value="null">请选择</option>
              <option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="form-item">
            <label>课程</label>
            <select v-model="form.courseId" @change="onCourseChange">
              <option :value="null">请选择</option>
              <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="form-item">
            <label>教师</label>
            <select v-model="form.teacherId" :disabled="!!form.courseId">
              <option :value="null">请选择</option>
              <option v-for="t in linkedTeachers" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
            <p class="form-hint">选择课程后自动带出任课教师，须与课程一致教师端才可见</p>
          </div>
          <div class="form-item">
            <label>星期</label>
            <select v-model="form.weekday">
              <option :value="1">周一</option>
              <option :value="2">周二</option>
              <option :value="3">周三</option>
              <option :value="4">周四</option>
              <option :value="5">周五</option>
              <option :value="6">周六</option>
              <option :value="7">周日</option>
            </select>
          </div>
          <div class="form-item">
            <label>开始时间</label>
            <input v-model="form.startTime" type="time" />
          </div>
          <div class="form-item">
            <label>结束时间</label>
            <input v-model="form.endTime" type="time" />
          </div>
          <div class="form-item">
            <label>教室</label>
            <input v-model="form.room" type="text" placeholder="请输入教室" />
          </div>
          <div class="form-item">
            <label>学期</label>
            <input v-model="form.semester" type="text" placeholder="请输入学期，如 2025春季" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn" @click="dialogVisible = false">取消</button>
          <button class="btn btn-primary" @click="handleSubmit" :disabled="loading">
            {{ loading ? '提交中...' : '确定' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  getScheduleListApi,
  getScheduleSemestersApi,
  addScheduleApi,
  updateScheduleApi,
  deleteScheduleApi
} from '@/api/schedule'
import { getClazzListApi } from '@/api/clazz'
import { getCourseListApi } from '@/api/course'
import { getTeacherListApi } from '@/api/teacher'
import PageSkeleton from '@/components/PageSkeleton.vue'
import { usePageLoading } from '@/composables/usePageLoading'

const { pageLoading, withLoading } = usePageLoading()

const weekdays = [
  { value: 1, label: '周一' },
  { value: 2, label: '周二' },
  { value: 3, label: '周三' },
  { value: 4, label: '周四' },
  { value: 5, label: '周五' },
  { value: 6, label: '周六' },
  { value: 7, label: '周日' }
]

const semesters = ref([])
const semester = ref('')
const schedules = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const classes = ref([])
const courses = ref([])
const teachers = ref([])

const linkedTeachers = computed(() => teachers.value.filter(t => t.userId != null))

const form = reactive({
  id: null,
  classId: null,
  courseId: null,
  teacherId: null,
  weekday: 1,
  startTime: '',
  endTime: '',
  room: '',
  semester: ''
})

const sortedSchedules = computed(() => {
  return [...schedules.value].sort((a, b) => {
    const dayDiff = (a.weekday || 0) - (b.weekday || 0)
    if (dayDiff !== 0) return dayDiff
    return shortTime(a.startTime).localeCompare(shortTime(b.startTime))
  })
})

const timeSlots = computed(() => {
  const map = new Map()
  schedules.value.forEach(s => {
    const key = `${shortTime(s.startTime)}-${shortTime(s.endTime)}`
    if (!map.has(key)) {
      map.set(key, { key, label: `${shortTime(s.startTime)} - ${shortTime(s.endTime)}`, start: shortTime(s.startTime) })
    }
  })
  return Array.from(map.values()).sort((a, b) => a.start.localeCompare(b.start))
})

const shortTime = (time) => (time || '').substring(0, 5)

const weekdayText = (weekday) => weekdays.find(d => d.value === weekday)?.label || '-'

const getCellSchedules = (weekday, slotKey) => {
  return schedules.value.filter(s => {
    const key = `${shortTime(s.startTime)}-${shortTime(s.endTime)}`
    return s.weekday === weekday && key === slotKey
  })
}

const onCourseChange = () => {
  const course = courses.value.find(c => c.id == form.courseId)
  if (course?.teacherId) {
    form.teacherId = course.teacherId
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    classId: null,
    courseId: null,
    teacherId: null,
    weekday: 1,
    startTime: '',
    endTime: '',
    room: '',
    semester: semester.value || ''
  })
}

const loadSemesters = async () => {
  try {
    const res = await getScheduleSemestersApi()
    semesters.value = res.data || []
    if (semesters.value.length && !semester.value) {
      semester.value = semesters.value[0]
    }
  } catch (e) {
    console.error(e)
  }
}

const loadSchedules = () => withLoading(async () => {
  const params = semester.value ? { semester: semester.value } : {}
  const res = await getScheduleListApi(params)
  schedules.value = res.data || []
})

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (item) => {
  isEdit.value = true
  Object.assign(form, {
    ...item,
    startTime: shortTime(item.startTime),
    endTime: shortTime(item.endTime)
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.classId || !form.courseId || !form.teacherId) {
    alert('请填写班级、课程和授课教师')
    return
  }
  try {
    loading.value = true
    if (isEdit.value) await updateScheduleApi(form)
    else await addScheduleApi(form)
    alert('操作成功')
    dialogVisible.value = false
    await loadSemesters()
    loadSchedules()
  } catch (e) {
    alert(e.message)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除该课程表记录吗？')) return
  try {
    await deleteScheduleApi(id)
    alert('删除成功')
    loadSchedules()
  } catch (e) {
    alert(e.message)
  }
}

onMounted(async () => {
  classes.value = (await getClazzListApi()).data || []
  courses.value = (await getCourseListApi()).data || []
  teachers.value = (await getTeacherListApi()).data || []
  await loadSemesters()
  loadSchedules()
})
</script>

<style scoped>
@import '@/assets/manage.css';

.teacher-schedule-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-desc {
  padding: 12px 16px;
  background: linear-gradient(90deg, #f5f3ff, #eef2ff);
  border-radius: 8px;
  border-left: 4px solid #7c3aed;
}

.page-desc p {
  margin: 0;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.6;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar label {
  font-size: 14px;
  color: #374151;
}

.toolbar select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  min-width: 140px;
}

.stat-tag {
  padding: 6px 12px;
  border-radius: 999px;
  background: #ede9fe;
  color: #5b21b6;
  font-size: 12px;
  font-weight: 600;
}

.empty-tip {
  padding: 48px 24px;
  text-align: center;
  color: #6b7280;
  background: #fff;
  border: 1px dashed #d1d5db;
  border-radius: 10px;
}

.main-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
  align-items: start;
}

.timetable-panel,
.list-panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.timetable-wrap {
  overflow-x: auto;
}

.timetable {
  width: 100%;
  border-collapse: collapse;
  min-width: 760px;
}

.timetable th,
.timetable td {
  border: 1px solid #e5e7eb;
  vertical-align: top;
  padding: 8px;
}

.timetable th {
  background: #f9fafb;
  text-align: center;
  font-size: 13px;
}

.time-col {
  width: 110px;
  background: #fafafa;
  font-size: 12px;
  color: #6b7280;
  white-space: nowrap;
}

.slot-cell {
  min-width: 110px;
  min-height: 72px;
  background: #fcfcff;
}

.schedule-block {
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
  border-left: 3px solid #9ca3af;
  border-radius: 6px;
  padding: 6px 8px;
  margin-bottom: 6px;
  font-size: 12px;
}

.schedule-block.mine,
.list-item.mine {
  background: linear-gradient(135deg, #ede9fe, #e0e7ff);
  border-left-color: #7c3aed;
}

.schedule-block.mine {
  border-left: 3px solid #7c3aed;
}

.block-title,
.list-title {
  font-weight: 600;
  color: #1f2937;
}

.block-meta,
.list-meta {
  color: #6b7280;
  margin-top: 2px;
  font-size: 12px;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 640px;
  overflow-y: auto;
}

.list-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  border-left: 3px solid #9ca3af;
}

.list-day {
  width: 42px;
  flex-shrink: 0;
  font-weight: 700;
  color: #7c3aed;
  font-size: 13px;
}

.list-body {
  min-width: 0;
  flex: 1;
}

.list-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

@media (max-width: 1100px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
}
</style>
