<template>
  <div class="manage-page">
    <PageSkeleton v-if="pageLoading" />
    <template v-else>
    <PageIntro text="查看家长留言并及时回复，左侧列表 + 右侧详情分栏展示。" />
    <StatCards :items="messageStats" />
    <div class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增留言</button>
    </div>
    <div v-if="!list.length" class="empty-tip">暂无留言</div>
    <div v-else class="split-panel">
      <div class="split-panel-side">
        <div
          v-for="item in list"
          :key="item.id"
          class="split-list-item"
          :class="{ 'split-list-item--active': selectedId === item.id, 'strip-item--highlight': item.status !== 1 }"
          @click="selectMessage(item)"
        >
          <div class="split-list-head">
            <strong>{{ item.parentName ?? '家长' }}</strong>
            <span :class="['badge', item.status === 1 ? 'badge-success' : 'badge-warning']">
              {{ formatCell(item.status, 'msgStatus') }}
            </span>
          </div>
          <div class="split-list-preview">{{ item.content ?? '-' }}</div>
          <div style="font-size: 11px; color: #9ca3af; margin-top: 4px">{{ item.createTime ?? '-' }}</div>
        </div>
      </div>
      <div class="split-panel-main">
        <template v-if="selectedMessage">
          <div class="message-detail-card">
            <div class="message-detail-header">
              <div class="message-detail-avatar">{{ (selectedMessage.parentName || '家').charAt(0) }}</div>
              <div>
                <div style="font-size: 18px; font-weight: 600">{{ selectedMessage.parentName ?? '家长' }}</div>
                <div style="font-size: 13px; color: #9ca3af; margin-top: 2px">{{ selectedMessage.createTime ?? '-' }}</div>
              </div>
              <span :class="['badge', selectedMessage.status === 1 ? 'badge-success' : 'badge-warning']" style="margin-left: auto">
                {{ formatCell(selectedMessage.status, 'msgStatus') }}
              </span>
            </div>
            <div class="message-detail-bubble">{{ selectedMessage.content ?? '-' }}</div>
            <div v-if="selectedMessage.reply" class="message-detail-reply">
              <strong>回复：</strong>{{ selectedMessage.reply }}
            </div>
            <div class="message-detail-actions">
              <button class="btn btn-sm btn-info" @click="handleEdit(selectedMessage)">编辑</button>
              <button class="btn btn-sm btn-danger" @click="handleDelete(selectedMessage.id)">删除</button>
            </div>
          </div>
        </template>
        <div v-else class="empty-tip">点击左侧留言查看详情</div>
      </div>
    </div>
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}留言</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>家长</label>
            <select v-model="form.parentId"><option :value="null">请选择</option><option v-for="p in parents" :key="p.id" :value="p.id">{{ p.name }}</option></select>
          </div>
          <div class="form-item">
            <label>留言内容 *</label>
            <textarea v-model="form.content" placeholder="请输入留言内容" rows="3"></textarea>
          </div>
          <div class="form-item">
            <label>回复</label>
            <textarea v-model="form.reply" placeholder="请输入回复" rows="3"></textarea>
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="form.status">
            <option :value="0">待回复</option>
            <option :value="1">已回复</option>
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
import { getMessageListApi, addMessageApi, updateMessageApi, deleteMessageApi } from '@/api/message'
import { getParentListApi } from '@/api/parent'
import PageSkeleton from '@/components/PageSkeleton.vue'
import PageIntro from '@/components/PageIntro.vue'
import StatCards from '@/components/StatCards.vue'
import { usePageLoading } from '@/composables/usePageLoading'
import { useFormatCell } from '@/composables/useFormatCell'

const { formatCell } = useFormatCell()

const { pageLoading, withLoading } = usePageLoading()

const list = ref([])
const selectedId = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const parents = ref([])

const form = reactive({
  id: null,
  parentId: null,
  content: '',
  reply: '',
  status: 0
})


const messageStats = computed(() => [
  { label: '留言总数', value: list.value.length, icon: '💬', tone: 'purple' },
  { label: '待回复', value: list.value.filter(m => m.status !== 1).length, icon: '⏳', tone: 'orange' },
  { label: '已回复', value: list.value.filter(m => m.status === 1).length, icon: '✅', tone: 'green' }
])

const selectedMessage = computed(() =>
  list.value.find(m => m.id === selectedId.value) || null
)

const selectMessage = (item) => {
  selectedId.value = item.id
}

const resetForm = () => {
  Object.assign(form, { id: null, parentId: null,
  content: '',
  reply: '',
  status: 0 })
}

const loadList = () => withLoading(async () => {
  try {
    const res = await getMessageListApi()
    list.value = res.data || []
    if (!selectedId.value && list.value.length) {
      selectedId.value = list.value[0].id
    }
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
  if (!form.content) { alert('请填写留言内容'); return }
  try {
    loading.value = true
    if (isEdit.value) await updateMessageApi(form)
    else await addMessageApi(form)
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteMessageApi(id)
    alert('删除成功')
    if (selectedId.value === id) selectedId.value = null
    loadList()
  } catch (e) { alert(e.message) }
}

onMounted(async () => {
  loadList()
  parents.value = (await getParentListApi()).data || []
})
</script>

<style scoped>
@import '@/assets/manage.css';
</style>
