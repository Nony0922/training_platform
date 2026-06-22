<template>
  <div class="manage-page">
    <PageSkeleton v-if="pageLoading" variant="grouped" />
    <template v-else>
    <PageIntro text="按课程与考试分组展示成绩，前三名以奖牌样式高亮，支持录入与维护。" />
    <div class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增成绩</button>
    </div>
    <div v-if="!groups.length" class="empty-tip">暂无成绩数据</div>

    <div v-for="course in groups" :key="course.courseId" class="data-group">
      <div class="group-header">
        <span class="group-title">{{ course.courseName }}</span>
        <span class="group-meta">{{ countScoreRows(course) }} 条成绩</span>
      </div>
      <div v-for="group in course.exams" :key="group.examId" class="subgroup">
        <div class="subgroup-header exam-subgroup-header">
          <div class="exam-group-left">
            <span class="subgroup-title">{{ group.examName }}</span>
            <span class="exam-group-sub">{{ group.className }} · {{ group.examDate }}</span>
          </div>
          <span class="subgroup-meta">{{ group.rows.length }} 名学生</span>
        </div>
        <div class="score-list">
          <div v-for="item in group.rows" :key="item.id" class="score-row">
            <span :class="['score-rank', item.rankNum && Number(item.rankNum) <= 3 ? `score-rank--${item.rankNum}` : '']">
              {{ item.rankNum ?? '-' }}
            </span>
            <span class="score-row-name">{{ item.studentName ?? '-' }}</span>
            <span class="score-row-value">{{ item.score ?? '-' }}</span>
            <span style="font-size: 12px; color: #9ca3af; max-width: 120px; overflow: hidden; text-overflow: ellipsis">{{ item.remark || '-' }}</span>
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
          <h3>{{ isEdit ? '编辑' : '新增' }}成绩</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>考试</label>
            <select v-model="form.examId"><option :value="null">请选择</option><option v-for="e in exams" :key="e.id" :value="e.id">{{ e.name }}</option></select>
          </div>
          <div class="form-item">
            <label>学生</label>
            <select v-model="form.studentId"><option :value="null">请选择</option><option v-for="s in students" :key="s.id" :value="s.id">{{ s.name }}</option></select>
          </div>
          <div class="form-item">
            <label>分数</label>
            <input v-model="form.score" type="number" placeholder="请输入分数" />
          </div>
          <div class="form-item">
            <label>排名</label>
            <input v-model="form.rankNum" type="number" placeholder="请输入排名" />
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
import { getScoreListApi, addScoreApi, updateScoreApi, deleteScoreApi } from '@/api/score'
import { getExamListApi } from '@/api/exam'
import { getStudentListApi } from '@/api/student'
import { getScopeModeFromRoute } from '@/composables/useTeacherScope'
import { groupScoresByCourseExam } from '@/utils/groupTeachingData'
import PageSkeleton from '@/components/PageSkeleton.vue'
import PageIntro from '@/components/PageIntro.vue'
import { usePageLoading } from '@/composables/usePageLoading'

const route = useRoute()
const scopeMode = () => getScopeModeFromRoute(route)
const { pageLoading, withLoading } = usePageLoading()

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const exams = ref([])
const students = ref([])

const groups = computed(() => groupScoresByCourseExam(list.value, exams.value))

const countScoreRows = (course) =>
  course.exams.reduce((sum, exam) => sum + exam.rows.length, 0)

const form = reactive({
  id: null,
  examId: null,
  studentId: null,
  score: '',
  rankNum: '',
  remark: ''
})


const resetForm = () => {
  Object.assign(form, { id: null, examId: null,
  studentId: null,
  score: '',
  rankNum: '',
  remark: '' })
}

const loadList = () => withLoading(async () => {
  const res = await getScoreListApi(scopeMode())
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
    if (isEdit.value) await updateScoreApi(form)
    else await addScoreApi(form)
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteScoreApi(id)
    alert('删除成功')
    loadList()
  } catch (e) { alert(e.message) }
}

onMounted(async () => {
  loadList()
  exams.value = (await getExamListApi(scopeMode())).data || []
students.value = (await getStudentListApi(scopeMode())).data || []
})
</script>

<style scoped>
@import '@/assets/manage.css';
</style>
