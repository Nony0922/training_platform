<template>
  <div class="manage-page">
    <PageSkeleton v-if="pageLoading" variant="grouped" />
    <template v-else>
    <PageIntro text="按课程与班级分组管理考试安排，卡片与时间轴结合展示每场考试。" />
    <div class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增考试</button>
    </div>
    <div v-if="!groups.length" class="empty-tip">暂无考试数据</div>

    <div v-for="course in groups" :key="course.courseId" class="data-group">
      <div class="group-header">
        <span class="group-title">{{ course.courseName }}</span>
        <span class="group-meta">{{ countExamRows(course) }} 场考试</span>
      </div>
      <div v-for="klass in course.classes" :key="klass.classId" class="subgroup">
        <div class="subgroup-header">
          <span class="subgroup-title">{{ klass.className }}</span>
          <span class="subgroup-meta">{{ klass.rows.length }} 场</span>
        </div>
        <div class="exam-card-grid">
          <div v-for="item in klass.rows" :key="item.id" class="exam-card">
            <div class="exam-card-title">{{ item.name ?? '-' }}</div>
            <div class="info-row"><span class="info-row-label">日期</span><span class="info-row-value">{{ item.examDate ?? '-' }}</span></div>
            <div class="info-row"><span class="info-row-label">时间</span><span class="info-row-value">{{ formatTimeRange(item) }}</span></div>
            <div class="info-row"><span class="info-row-label">地点</span><span class="info-row-value">{{ item.location ?? '-' }}</span></div>
            <div class="info-row"><span class="info-row-label">总分</span><span class="info-row-value">{{ item.totalScore ?? '-' }}</span></div>
            <div class="tag-list" style="margin-top: 8px">
              <span class="tag-chip tag-chip--purple">{{ formatCell(item.status, 'examStatus') }}</span>
            </div>
            <div class="entity-card-foot" style="border-top: none; padding-top: 10px; margin-top: 4px">
              <button class="btn btn-sm btn-info" @click="handleEdit(item)">编辑</button>
              <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    </template>
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}考试</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>考试名称 *</label>
            <input v-model="form.name" type="text" placeholder="请输入考试名称" />
          </div>
          <div class="form-item">
            <label>课程</label>
            <select v-model="form.courseId"><option :value="null">请选择</option><option v-for="c in courses" :key="c.id" :value="c.id">{{ c.name }}</option></select>
          </div>
          <div class="form-item">
            <label>班级</label>
            <select v-model="form.classId"><option :value="null">请选择</option><option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</option></select>
          </div>
          <div class="form-item">
            <label>考试日期</label>
            <input v-model="form.examDate" type="date" placeholder="请输入考试日期" />
          </div>
          <div class="form-item">
            <label>开始时间</label>
            <input v-model="form.startTime" type="time" placeholder="请输入开始时间" />
          </div>
          <div class="form-item">
            <label>结束时间</label>
            <input v-model="form.endTime" type="time" placeholder="请输入结束时间" />
          </div>
          <div class="form-item">
            <label>地点</label>
            <input v-model="form.location" type="text" placeholder="请输入地点" />
          </div>
          <div class="form-item">
            <label>总分</label>
            <input v-model="form.totalScore" type="number" placeholder="请输入总分" />
          </div>
          <div class="form-item">
            <label>状态</label>
            <input type="text" readonly value="根据考试日期与时间自动判定（未开始/进行中/已结束）" />
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
import { getExamListApi, addExamApi, updateExamApi, deleteExamApi } from '@/api/exam'
import { getClazzListApi } from '@/api/clazz'
import { getCourseListApi } from '@/api/course'
import { getScopeModeFromRoute } from '@/composables/useTeacherScope'
import { groupExamsByCourseClass } from '@/utils/groupTeachingData'
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
const classes = ref([])
const courses = ref([])

const groups = computed(() => groupExamsByCourseClass(list.value))

const countExamRows = (course) =>
  course.classes.reduce((sum, klass) => sum + klass.rows.length, 0)

const formatTimeRange = (item) => {
  const start = (item.startTime || '').substring(0, 5)
  const end = (item.endTime || '').substring(0, 5)
  if (start && end) return `${start} - ${end}`
  return start || end || '-'
}

const form = reactive({
  id: null,
  name: '',
  courseId: null,
  classId: null,
  examDate: '',
  startTime: '',
  endTime: '',
  location: '',
  totalScore: 100,
  status: 0,
  remark: ''
})


const resetForm = () => {
  Object.assign(form, { id: null, name: '',
  courseId: null,
  classId: null,
  examDate: '',
  startTime: '',
  endTime: '',
  location: '',
  totalScore: 100,
  status: 0,
  remark: '' })
}

const loadList = () => withLoading(async () => {
  const res = await getExamListApi(scopeMode())
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
  if (!form.name) { alert('请填写考试名称'); return }
  try {
    loading.value = true
    if (isEdit.value) await updateExamApi(form)
    else await addExamApi(form)
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteExamApi(id)
    alert('删除成功')
    loadList()
  } catch (e) { alert(e.message) }
}

onMounted(async () => {
  loadList()
  classes.value = (await getClazzListApi()).data || []
courses.value = (await getCourseListApi()).data || []
})
</script>

<style scoped>
@import '@/assets/manage.css';
</style>
