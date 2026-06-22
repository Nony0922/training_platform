<template>
  <div class="manage-page">
    <PageSkeleton v-if="pageLoading" variant="cards" />
    <template v-else>
    <PageIntro text="维护家长联系信息，便于家校沟通与课程报名关联。" />
    <StatCards :items="parentStats" />
    <div v-if="!readOnly" class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增家长</button>
    </div>
    <div v-if="!list.length" class="empty-tip">暂无家长数据</div>
    <div v-else class="card-grid">
      <div v-for="item in list" :key="item.id" class="entity-card">
        <div class="profile-card">
          <div class="avatar-badge avatar-badge--parent">{{ (item.name || '家').charAt(0) }}</div>
          <div class="profile-card-main">
            <div class="entity-card-head" style="margin-bottom: 0">
              <div>
                <h3 class="entity-card-title">{{ item.name ?? '-' }}</h3>
                <p class="entity-card-sub">{{ item.phone ?? '-' }}</p>
              </div>
            </div>
            <div class="entity-card-body" style="margin-top: 10px">
              <div class="info-row"><span class="info-row-label">邮箱</span><span class="info-row-value">{{ item.email || '-' }}</span></div>
              <div class="info-row"><span class="info-row-label">地址</span><span class="info-row-value">{{ item.address || '-' }}</span></div>
              <div class="info-row"><span class="info-row-label">创建</span><span class="info-row-value">{{ item.createTime || '-' }}</span></div>
            </div>
          </div>
        </div>
        <div v-if="!readOnly" class="entity-card-foot">
          <button class="btn btn-sm btn-info" @click="handleEdit(item)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
        </div>
      </div>
    </div>
    <div v-if="!readOnly && dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}家长</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>姓名 *</label>
            <input v-model="form.name" type="text" placeholder="请输入姓名" />
          </div>
          <div class="form-item">
            <label>电话 *</label>
            <input v-model="form.phone" type="text" placeholder="请输入电话" />
          </div>
          <div class="form-item">
            <label>邮箱</label>
            <input v-model="form.email" type="text" placeholder="请输入邮箱" />
          </div>
          <div class="form-item">
            <label>地址</label>
            <input v-model="form.address" type="text" placeholder="请输入地址" />
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
import { getParentListApi, addParentApi, updateParentApi, deleteParentApi } from '@/api/parent'
import { useReadOnly } from '@/composables/useReadOnly'
import PageSkeleton from '@/components/PageSkeleton.vue'
import PageIntro from '@/components/PageIntro.vue'
import StatCards from '@/components/StatCards.vue'
import { usePageLoading } from '@/composables/usePageLoading'

const { pageLoading, withLoading } = usePageLoading()

const readOnly = useReadOnly()

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)

const parentStats = computed(() => [
  { label: '家长总数', value: list.value.length, icon: '👨‍👩‍👧', tone: 'purple' }
])

const form = reactive({
  id: null,
  name: '',
  phone: '',
  email: '',
  address: ''
})

const resetForm = () => {
  Object.assign(form, { id: null, name: '',
  phone: '',
  email: '',
  address: '' })
}

const loadList = () => withLoading(async () => {
  try {
    const res = await getParentListApi()
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
  if (!form.phone) { alert('请填写电话'); return }
  try {
    loading.value = true
    if (isEdit.value) await updateParentApi(form)
    else await addParentApi(form)
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteParentApi(id)
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
