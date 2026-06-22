<template>
  <div class="manage-page">
    <PageSkeleton v-if="pageLoading" variant="cards" />
    <template v-else>
    <div v-if="!readOnly" class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增课程</button>
    </div>
    <div v-if="!list.length" class="empty-tip">暂无课程数据</div>
    <div v-else class="catalog-list">
      <div v-for="item in list" :key="item.id" class="catalog-item">
        <div class="catalog-accent" />
        <div class="catalog-body">
          <h3 class="catalog-title">{{ item.name ?? '-' }}</h3>
          <div class="catalog-meta">
            <span>{{ item.targetGrade || '全年级' }}</span>
            <span>{{ item.subject || '综合' }}</span>
            <span>{{ formatCell(item.teachMode, 'teachMode') }}</span>
            <span>教师：{{ item.teacherName ?? '-' }}</span>
            <span>{{ item.hours ?? '-' }} 学时</span>
          </div>
          <p v-if="item.description" class="catalog-desc">{{ item.description }}</p>
        </div>
        <div class="catalog-side">
          <span :class="['badge', item.status === 1 ? 'badge-success' : 'badge-warning']">
            {{ formatCell(item.status, 'shelf') }}
          </span>
          <div class="catalog-price">¥{{ item.fee ?? 0 }}</div>
          <div v-if="!readOnly" style="display: flex; gap: 6px">
            <button class="btn btn-sm btn-info" @click="handleEdit(item)">编辑</button>
            <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!readOnly && dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}课程</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>课程名称 *</label>
            <input v-model="form.name" type="text" placeholder="请输入课程名称" />
          </div>
          <div class="form-item">
            <label>课程介绍</label>
            <textarea v-model="form.description" placeholder="请输入课程介绍" rows="3"></textarea>
          </div>
          <div class="form-item">
            <label>任课教师 *</label>
            <select v-model="form.teacherId"><option :value="null">请选择</option><option v-for="t in linkedTeachers" :key="t.id" :value="t.id">{{ t.name }}</option></select>
            <p v-if="!readOnly" class="form-hint">仅显示已绑定登录账号的教师，新增后对应教师可在「我的课程」中查看</p>
          </div>
          <div class="form-item">
            <label>适用年级</label>
            <input v-model="form.targetGrade" type="text" placeholder="如：一年级、二年级、全年级" />
          </div>
          <div class="form-item">
            <label>学科</label>
            <input v-model="form.subject" type="text" placeholder="如：语文、数学、英语" />
          </div>
          <div class="form-item">
            <label>授课方式</label>
            <select v-model="form.teachMode">
              <option :value="1">线下授课</option>
              <option :value="2">线上直播</option>
              <option :value="3">线上线下混合</option>
            </select>
          </div>
          <div class="form-item">
            <label>上课地点</label>
            <input v-model="form.location" type="text" placeholder="线下填教室，线上填平台名称" />
          </div>
          <div class="form-item">
            <label>有效开始日期</label>
            <input v-model="form.validStart" type="date" />
          </div>
          <div class="form-item">
            <label>有效结束日期</label>
            <input v-model="form.validEnd" type="date" />
          </div>
          <div class="form-item">
            <label>上课时间说明</label>
            <input v-model="form.classTimeDesc" type="text" placeholder="如：每周一、三 09:00-10:30" />
          </div>
          <div class="form-item">
            <label>适合年龄</label>
            <input v-model="form.suitableAge" type="text" placeholder="如：6-7岁" />
          </div>
          <div class="form-item">
            <label>招生名额（0 表示不限）</label>
            <input v-model="form.maxStudents" type="number" placeholder="请输入招生名额" />
          </div>
          <div class="form-item">
            <label>已报名人数</label>
            <input v-model="form.enrolledCount" type="number" placeholder="请输入已报名人数" />
          </div>
          <div class="form-item">
            <label>课程亮点（| 分隔）</label>
            <input v-model="form.highlights" type="text" placeholder="如：拼音启蒙|阅读训练|写作提升" />
          </div>
          <div class="form-item">
            <label>学时</label>
            <input v-model="form.hours" type="number" placeholder="请输入学时" />
          </div>
          <div class="form-item">
            <label>费用</label>
            <input v-model="form.fee" type="number" placeholder="请输入费用" />
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="form.status">
            <option :value="1">上架</option>
            <option :value="0">下架</option>
          </select>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn" @click="dialogVisible = false">取消</button>
          <button class="btn btn-primary" @click="handleSubmit" :disabled="loading">{{ loading ? '提交中...' : '确定' }}</button>
        </div>
      </div>
    </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getCourseListApi, addCourseApi, updateCourseApi, deleteCourseApi } from '@/api/course'
import { getTeacherListApi } from '@/api/teacher'
import { useReadOnly } from '@/composables/useReadOnly'
import { getScopeModeFromRoute } from '@/composables/useTeacherScope'
import PageSkeleton from '@/components/PageSkeleton.vue'
import { usePageLoading } from '@/composables/usePageLoading'
import { useFormatCell } from '@/composables/useFormatCell'

const { formatCell } = useFormatCell()

const { pageLoading, withLoading } = usePageLoading()

const route = useRoute()
const readOnly = useReadOnly()

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const teachers = ref([])

const linkedTeachers = computed(() => teachers.value.filter(t => t.userId != null))

const form = reactive({
  id: null,
  name: '',
  description: '',
  teacherId: null,
  targetGrade: '',
  subject: '',
  teachMode: 1,
  location: '',
  validStart: '',
  validEnd: '',
  classTimeDesc: '',
  maxStudents: 0,
  enrolledCount: 0,
  suitableAge: '',
  highlights: '',
  hours: 0,
  fee: 0,
  status: 1
})

const resetForm = () => {
  Object.assign(form, { id: null, name: '',
  description: '',
  teacherId: null,
  targetGrade: '',
  subject: '',
  teachMode: 1,
  location: '',
  validStart: '',
  validEnd: '',
  classTimeDesc: '',
  maxStudents: 0,
  enrolledCount: 0,
  suitableAge: '',
  highlights: '',
  hours: 0,
  fee: 0,
  status: 1 })
}

const loadList = () => withLoading(async () => {
  try {
    const res = await getCourseListApi(getScopeModeFromRoute(route))
    list.value = res.data || []
  } catch (e) { alert(e.message) }
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
  if (!form.name) { alert('请填写课程名称'); return }
  if (!form.teacherId) { alert('请选择任课教师，否则教师端无法查看该课程'); return }
  try {
    loading.value = true
    if (isEdit.value) await updateCourseApi(form)
    else await addCourseApi(form)
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteCourseApi(id)
    alert('删除成功')
    loadList()
  } catch (e) { alert(e.message) }
}

onMounted(async () => {
  loadList()
  teachers.value = (await getTeacherListApi()).data || []
})
</script>

<style scoped>
@import '@/assets/manage.css';
</style>
