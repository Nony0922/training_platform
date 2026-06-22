<template>
  <div class="manage-page">
    <PageSkeleton v-if="pageLoading" variant="cards" />
    <template v-else>
    <PageIntro text="管理培训班级信息，查看各班容量使用情况与班主任安排。" />
    <div class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增班级</button>
    </div>
    <div v-if="!list.length" class="empty-tip">暂无班级数据</div>
    <div v-else class="card-grid">
      <div v-for="item in list" :key="item.id" class="entity-card">
        <div class="profile-card">
          <div class="avatar-badge avatar-badge--class">{{ (item.name || '班').charAt(0) }}</div>
          <div class="profile-card-main">
            <div class="entity-card-head" style="margin-bottom: 0">
              <div>
                <h3 class="entity-card-title">{{ item.name ?? '-' }}</h3>
                <p class="entity-card-sub">{{ item.grade || '未设置年级' }} · {{ item.room || '未分配教室' }}</p>
              </div>
              <span :class="['badge', item.status === 1 ? 'badge-success' : 'badge-danger']">
                {{ formatCell(item.status, 'status') }}
              </span>
            </div>
            <div class="entity-card-body" style="margin-top: 10px">
              <div class="info-row"><span class="info-row-label">班主任</span><span class="info-row-value">{{ item.headTeacherName || '-' }}</span></div>
              <div class="info-row">
                <span class="info-row-label">容量</span>
                <span class="info-row-value">
                  {{ item.studentCount != null ? `${item.studentCount} / ${item.capacity ?? '-'}` : item.capacity ?? '-' }}
                </span>
              </div>
              <div class="capacity-bar">
                <div class="capacity-bar-fill" :style="{ width: capacityPercent(item) + '%' }" />
              </div>
            </div>
          </div>
        </div>
        <div class="entity-card-foot">
          <button class="btn btn-sm btn-info" @click="handleEdit(item)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
        </div>
      </div>
    </div>
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}班级</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>班级名称 *</label>
            <input v-model="form.name" type="text" placeholder="请输入班级名称" />
          </div>
          <div class="form-item">
            <label>年级</label>
            <input v-model="form.grade" type="text" placeholder="请输入年级" />
          </div>
          <div class="form-item">
            <label>班主任</label>
            <select v-model="form.headTeacherId"><option :value="null">请选择</option><option v-for="t in teachers" :key="t.id" :value="t.id">{{ t.name }}</option></select>
          </div>
          <div class="form-item">
            <label>教室</label>
            <input v-model="form.room" type="text" placeholder="请输入教室" />
          </div>
          <div class="form-item">
            <label>容量</label>
            <input v-model="form.capacity" type="number" placeholder="请输入容量" />
          </div>
          <div class="form-item">
            <label>描述</label>
            <textarea v-model="form.description" placeholder="请输入描述" rows="3"></textarea>
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="form.status">
            <option :value="1">正常</option>
            <option :value="0">停用</option>
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
import { ref, reactive, onMounted } from 'vue'
import { getClazzListApi, addClazzApi, updateClazzApi, deleteClazzApi } from '@/api/clazz'
import { getTeacherListApi } from '@/api/teacher'
import PageSkeleton from '@/components/PageSkeleton.vue'
import PageIntro from '@/components/PageIntro.vue'
import { usePageLoading } from '@/composables/usePageLoading'
import { useFormatCell } from '@/composables/useFormatCell'

const { formatCell } = useFormatCell()

const { pageLoading, withLoading } = usePageLoading()

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const teachers = ref([])

const form = reactive({
  id: null,
  name: '',
  grade: '',
  headTeacherId: null,
  room: '',
  capacity: 30,
  description: '',
  status: 1
})

const capacityPercent = (item) => {
  if (item.studentCount != null && item.capacity) {
    return Math.min(100, Math.round((item.studentCount / item.capacity) * 100))
  }
  return 100
}

const resetForm = () => {
  Object.assign(form, { id: null, name: '',
  grade: '',
  headTeacherId: null,
  room: '',
  capacity: 30,
  description: '',
  status: 1 })
}

const loadList = () => withLoading(async () => {
  try {
    const res = await getClazzListApi()
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
  if (!form.name) { alert('请填写班级名称'); return }
  try {
    loading.value = true
    if (isEdit.value) await updateClazzApi(form)
    else await addClazzApi(form)
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteClazzApi(id)
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
