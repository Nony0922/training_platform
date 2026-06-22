<template>
  <div class="manage-page">
    <PageSkeleton v-if="pageLoading" variant="grouped" :show-toolbar="!readOnly" />
    <template v-else>
    <div v-if="!readOnly" class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增学生</button>
    </div>
    <div v-if="!groups.length" class="empty-tip">暂无学生数据</div>

    <div v-for="group in groups" :key="group.classId" class="data-group">
      <div class="group-header">
        <span class="group-title">{{ group.className }}</span>
        <span class="group-meta">{{ group.rows.length }} 名学生</span>
      </div>
      <div class="person-grid">
        <div v-for="item in group.rows" :key="item.id" class="person-card">
          <div class="person-card-name">{{ item.name ?? '-' }}</div>
          <div class="person-card-meta">
            {{ formatCell(item.gender, 'gender') }} · {{ formatCell(item.status, 'status') }}<br>
            家长：{{ item.parentName ?? '-' }}<br>
            电话：{{ item.phone ?? '-' }}<br>
            入学：{{ item.enrollDate ?? '-' }}
          </div>
          <div v-if="!readOnly" class="actions">
            <button class="btn btn-sm btn-info" @click="handleEdit(item)">编辑</button>
            <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
          </div>
        </div>
      </div>
    </div>
    </template>
    <div v-if="!readOnly && dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}学生</h3>
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
            <label>生日</label>
            <input v-model="form.birthday" type="date" placeholder="请输入生日" />
          </div>
          <div class="form-item">
            <label>电话</label>
            <input v-model="form.phone" type="text" placeholder="请输入电话" />
          </div>
          <div class="form-item">
            <label>班级</label>
            <select v-model="form.classId"><option :value="null">请选择</option><option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</option></select>
          </div>
          <div class="form-item">
            <label>家长</label>
            <select v-model="form.parentId"><option :value="null">请选择</option><option v-for="p in parents" :key="p.id" :value="p.id">{{ p.name }}</option></select>
          </div>
          <div class="form-item">
            <label>入学日期</label>
            <input v-model="form.enrollDate" type="date" placeholder="请输入入学日期" />
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="form.status">
            <option :value="1">在读</option>
            <option :value="0">离校</option>
          </select>
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
import { getStudentListApi, addStudentApi, updateStudentApi, deleteStudentApi } from '@/api/student'
import { getClazzListApi } from '@/api/clazz'
import { getParentListApi } from '@/api/parent'
import { useReadOnly } from '@/composables/useReadOnly'
import { getScopeModeFromRoute } from '@/composables/useTeacherScope'
import { groupByClass } from '@/utils/groupTeachingData'
import PageSkeleton from '@/components/PageSkeleton.vue'
import { usePageLoading } from '@/composables/usePageLoading'
import { useFormatCell } from '@/composables/useFormatCell'

const { formatCell } = useFormatCell()

const route = useRoute()
const readOnly = useReadOnly()
const scopeMode = () => getScopeModeFromRoute(route)
const { pageLoading, withLoading } = usePageLoading()

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const classes = ref([])
const parents = ref([])

const groups = computed(() => groupByClass(list.value))

const form = reactive({
  id: null,
  name: '',
  gender: 1,
  birthday: '',
  phone: '',
  classId: null,
  parentId: null,
  enrollDate: '',
  status: 1
})

const resetForm = () => {
  Object.assign(form, { id: null, name: '',
  gender: 1,
  birthday: '',
  phone: '',
  classId: null,
  parentId: null,
  enrollDate: '',
  status: 1 })
}

const loadList = () => withLoading(async () => {
  const res = await getStudentListApi(scopeMode())
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
  if (!form.name) { alert('请填写姓名'); return }
  try {
    loading.value = true
    if (isEdit.value) await updateStudentApi(form)
    else await addStudentApi(form)
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteStudentApi(id)
    alert('删除成功')
    loadList()
  } catch (e) { alert(e.message) }
}

onMounted(async () => {
  loadList()
  classes.value = (await getClazzListApi()).data || []
parents.value = (await getParentListApi()).data || []
})
</script>

<style scoped>
@import '@/assets/manage.css';
</style>
