<template>
  <div class="manage-page">
    <PageSkeleton v-if="pageLoading" variant="table" />
    <template v-else>
    <PageIntro text="维护培训机构教师档案，条纹列表展示教师信息，便于纵向浏览。" />
    <StatCards :items="teacherStats" />
    <div class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增教师</button>
    </div>
    <div v-if="!list.length" class="empty-tip">暂无教师数据</div>
    <div v-else class="media-list">
      <div v-for="item in list" :key="item.id" class="media-list-item">
        <div class="avatar-badge avatar-badge--teacher">{{ (item.name || '师').charAt(0) }}</div>
        <div class="media-list-body">
          <div class="media-list-title">{{ item.name }}</div>
          <div class="media-list-sub">
            {{ item.subject || '未设置科目' }} · {{ item.title || '暂无职称' }} · {{ formatCell(item.teacherLevel, 'teacherLevel') }}
          </div>
          <div class="tag-list" style="margin-top: 6px">
            <span class="tag-chip">{{ formatCell(item.gender, 'gender') }}</span>
            <span class="tag-chip">{{ item.phone || '无电话' }}</span>
            <span class="tag-chip">入职 {{ item.hireDate || '-' }}</span>
          </div>
        </div>
        <div class="media-list-actions">
          <span :class="['badge', item.status === 1 ? 'badge-success' : 'badge-danger']">
            {{ formatCell(item.status, 'teacherStatus') }}
          </span>
          <div style="display: flex; gap: 8px; margin-top: 8px">
            <button class="btn btn-sm btn-info" @click="handleEdit(item)">编辑</button>
            <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}教师</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>姓名 *</label>
            <input v-model="form.name" type="text" placeholder="请输入姓名" />
          </div>
          <div class="form-item">
            <label>性别</label>
            <select v-model="form.gender">
            <option :value="1">男</option>
            <option :value="2">女</option>
          </select>
          </div>
          <div class="form-item">
            <label>电话</label>
            <input v-model="form.phone" type="text" placeholder="请输入电话" />
          </div>
          <div class="form-item">
            <label>邮箱</label>
            <input v-model="form.email" type="text" placeholder="请输入邮箱" />
          </div>
          <div class="form-item">
            <label>教师级别</label>
            <select v-model="form.teacherLevel">
            <option :value="1">任课教师</option>
            <option :value="2">班主任</option>
          </select>
          </div>
          <div class="form-item">
            <label>科目</label>
            <input v-model="form.subject" type="text" placeholder="请输入科目" />
          </div>
          <div class="form-item">
            <label>职称</label>
            <input v-model="form.title" type="text" placeholder="请输入职称" />
          </div>
          <div class="form-item">
            <label>入职日期</label>
            <input v-model="form.hireDate" type="date" placeholder="请输入入职日期" />
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="form.status">
            <option :value="1">在职</option>
            <option :value="0">离职</option>
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
import { getTeacherListApi, addTeacherApi, updateTeacherApi, deleteTeacherApi } from '@/api/teacher'
import PageSkeleton from '@/components/PageSkeleton.vue'
import PageIntro from '@/components/PageIntro.vue'
import StatCards from '@/components/StatCards.vue'
import { usePageLoading } from '@/composables/usePageLoading'
import { useFormatCell } from '@/composables/useFormatCell'

const { formatCell } = useFormatCell()

const { pageLoading, withLoading } = usePageLoading()


const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)

const teacherStats = computed(() => [
  { label: '教师总数', value: list.value.length, icon: '👨‍🏫', tone: 'purple' },
  { label: '任课教师', value: list.value.filter(t => t.teacherLevel === 1).length, icon: '📚', tone: 'blue' },
  { label: '班主任', value: list.value.filter(t => t.teacherLevel === 2).length, icon: '🏫', tone: 'green' },
  { label: '在职', value: list.value.filter(t => t.status === 1).length, icon: '✅', tone: 'orange' }
])

const form = reactive({
  id: null,
  name: '',
  gender: 1,
  phone: '',
  email: '',
  teacherLevel: 1,
  subject: '',
  title: '',
  hireDate: '',
  status: 1
})


const resetForm = () => {
  Object.assign(form, { id: null, name: '',
  gender: 1,
  phone: '',
  email: '',
  teacherLevel: 1,
  subject: '',
  title: '',
  hireDate: '',
  status: 1 })
}

const loadList = () => withLoading(async () => {
  try {
    const res = await getTeacherListApi()
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
  if (!form.name) { alert('请填写姓名'); return }
  try {
    loading.value = true
    if (isEdit.value) await updateTeacherApi(form)
    else await addTeacherApi(form)
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteTeacherApi(id)
    alert('删除成功')
    loadList()
  } catch (e) { alert(e.message) }
}

onMounted(async () => {
  loadList()
  
})
</script>

<style scoped>
@import '@/assets/manage.css';
</style>
