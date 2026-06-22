<template>
  <div class="manage-page">
    <PageSkeleton
      v-if="pageLoading"
      :variant="readOnly ? 'cards' : 'table'"
      :show-toolbar="!readOnly"
    />
    <template v-else>
    <PageIntro v-if="!readOnly" text="发布面向管理员、教师或家长的通知公告，支持草稿与已发布状态。" />
    <div v-if="!readOnly" class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增通知公告</button>
    </div>

    <template v-if="readOnly">
      <div v-if="!visibleList.length" class="empty-tip">暂无公告</div>
      <div v-for="item in visibleList" :key="item.id" class="announcement-card">
        <div class="announcement-card-header">
          <h3 class="announcement-title">{{ item.title ?? '-' }}</h3>
          <span class="announcement-status">{{ formatCell(item.status, 'annStatus') }}</span>
        </div>
        <div class="announcement-content">{{ item.content || '暂无内容' }}</div>
        <div class="announcement-meta">
          <span>发布人：{{ item.publisherName ?? '-' }}</span>
          <span>面向：{{ formatCell(item.targetRole, 'role') }}</span>
          <span>发布时间：{{ item.publishTime ?? item.createTime ?? '-' }}</span>
        </div>
      </div>
    </template>

    <template v-else>
      <div v-if="!list.length" class="empty-tip">暂无公告</div>
      <template v-else>
        <div v-if="featuredAnnouncement" class="featured-announcement">
          <div class="featured-announcement-label">最新公告</div>
          <h2 class="featured-announcement-title">{{ featuredAnnouncement.title ?? '-' }}</h2>
          <p class="featured-announcement-excerpt">{{ featuredAnnouncement.content || '暂无内容' }}</p>
          <div class="featured-announcement-meta">
            <span>发布人：{{ featuredAnnouncement.publisherName ?? '-' }}</span>
            <span>面向：{{ formatCell(featuredAnnouncement.targetRole, 'role') }}</span>
            <span>{{ featuredAnnouncement.publishTime ?? featuredAnnouncement.createTime ?? '-' }}</span>
          </div>
          <div class="featured-announcement-actions">
            <button class="btn btn-sm" @click="handleView(featuredAnnouncement)">查看全文</button>
            <button class="btn btn-sm" @click="handleEdit(featuredAnnouncement)">编辑</button>
          </div>
        </div>

        <div v-if="publishedList.length > 1" class="data-group" style="margin-bottom: 20px">
          <div class="group-header">
            <span class="group-title">已发布公告</span>
            <span class="group-meta">杂志列表</span>
          </div>
          <div class="magazine-list">
            <div v-for="item in publishedList.slice(1)" :key="item.id" class="magazine-item">
              <div class="magazine-date">{{ formatAnnounceDate(item) }}</div>
              <div>
                <h3 class="magazine-title">{{ item.title ?? '-' }}</h3>
                <p class="magazine-excerpt">{{ item.content || '暂无内容' }}</p>
                <div class="magazine-foot">
                  <span>{{ item.publisherName ?? '-' }}</span>
                  <span>{{ formatCell(item.targetRole, 'role') }}</span>
                </div>
              </div>
              <div class="magazine-actions">
                <button class="btn btn-sm btn-info" @click="handleView(item)">查看</button>
                <button class="btn btn-sm btn-info" @click="handleEdit(item)">编辑</button>
                <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="draftList.length" class="data-group">
          <div class="group-header">
            <span class="group-title">草稿箱</span>
            <span class="group-meta">{{ draftList.length }} 篇草稿</span>
          </div>
          <div class="card-grid--announcements">
            <div v-for="item in draftList" :key="item.id" class="announcement-card">
              <div class="announcement-card-header">
                <h3 class="announcement-title">{{ item.title ?? '-' }}</h3>
                <span class="announcement-status">{{ formatCell(item.status, 'annStatus') }}</span>
              </div>
              <div class="announcement-content">{{ item.content || '暂无内容' }}</div>
              <div class="announcement-meta">
                <span>面向：{{ formatCell(item.targetRole, 'role') }}</span>
              </div>
              <div class="announcement-card-foot">
                <button class="btn btn-sm btn-info" @click="handleView(item)">查看</button>
                <button class="btn btn-sm btn-info" @click="handleEdit(item)">编辑</button>
                <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
              </div>
            </div>
          </div>
        </div>
      </template>
    </template>
    </template>

    <div v-if="detailVisible" class="dialog-overlay" @click.self="detailVisible = false">
      <div class="dialog announcement-detail-dialog">
        <div class="dialog-header">
          <h3>{{ detailItem?.title ?? '公告详情' }}</h3>
          <button class="close-btn" @click="detailVisible = false">&times;</button>
        </div>
        <div class="dialog-body announcement-detail-body">
          <div class="announcement-detail-meta">
            <span>发布人：{{ detailItem?.publisherName ?? '-' }}</span>
            <span>面向：{{ formatCell(detailItem?.targetRole, 'role') }}</span>
            <span>状态：{{ formatCell(detailItem?.status, 'annStatus') }}</span>
            <span>发布时间：{{ detailItem?.publishTime ?? detailItem?.createTime ?? '-' }}</span>
          </div>
          <div class="announcement-detail-content">{{ detailItem?.content || '暂无内容' }}</div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-primary" @click="detailVisible = false">关闭</button>
        </div>
      </div>
    </div>

    <div v-if="!readOnly && dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑' : '新增' }}通知公告</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>标题 *</label>
            <input v-model="form.title" type="text" placeholder="请输入标题" />
          </div>
          <div class="form-item">
            <label>内容</label>
            <textarea v-model="form.content" placeholder="请输入内容" rows="3"></textarea>
          </div>
          <div class="form-item">
            <label>发布人</label>
            <input v-model="form.publisherName" type="text" placeholder="请输入发布人" />
          </div>
          <div class="form-item">
            <label>目标角色</label>
            <select v-model="form.targetRole">
            <option :value="'all'">全部</option>
            <option :value="'admin'">管理员</option>
            <option :value="'teacher'">教师</option>
            <option :value="'parent'">家长</option>
          </select>
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="form.status">
            <option :value="1">已发布</option>
            <option :value="0">草稿</option>
          </select>
          </div>
          <div class="form-item">
            <label>发布时间</label>
            <input v-model="form.publishTime" type="datetime-local" placeholder="请输入发布时间" />
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
import { getAnnouncementListApi, addAnnouncementApi, updateAnnouncementApi, deleteAnnouncementApi } from '@/api/announcement'
import { useReadOnly } from '@/composables/useReadOnly'
import PageSkeleton from '@/components/PageSkeleton.vue'
import PageIntro from '@/components/PageIntro.vue'
import { usePageLoading } from '@/composables/usePageLoading'
import { useFormatCell } from '@/composables/useFormatCell'

const readOnly = useReadOnly()
const { pageLoading, withLoading } = usePageLoading()
const { formatCell } = useFormatCell()

const list = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detailItem = ref(null)
const isEdit = ref(false)
const loading = ref(false)

const visibleList = computed(() => {
  if (!readOnly.value) return list.value
  return list.value.filter((item) =>
    item.status === 1 && (item.targetRole === 'all' || item.targetRole === 'teacher')
  )
})

const formatAnnounceDate = (item) => {
  const raw = item.publishTime || item.createTime || ''
  if (!raw) return '-'
  const d = String(raw).slice(0, 10)
  const parts = d.split('-')
  if (parts.length >= 3) return `${parts[1]}/${parts[2]}\n${parts[0]}`
  return d
}

const publishedList = computed(() =>
  list.value.filter(item => item.status === 1)
)

const draftList = computed(() =>
  list.value.filter(item => item.status !== 1)
)

const featuredAnnouncement = computed(() => publishedList.value[0] || null)

const form = reactive({
  id: null,
  title: '',
  content: '',
  publisherName: '',
  targetRole: 'all',
  status: 1,
  publishTime: ''
})


const resetForm = () => {
  Object.assign(form, { id: null, title: '',
  content: '',
  publisherName: '',
  targetRole: 'all',
  status: 1,
  publishTime: '' })
}

const loadList = () => withLoading(async () => {
  const res = await getAnnouncementListApi()
  list.value = res.data || []
})

const handleView = (item) => {
  detailItem.value = { ...item }
  detailVisible.value = true
}

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
  if (!form.title) { alert('请填写标题'); return }
  try {
    loading.value = true
    if (isEdit.value) await updateAnnouncementApi(form)
    else await addAnnouncementApi(form)
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteAnnouncementApi(id)
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

.announcement-detail-dialog {
  max-width: 720px;
}

.announcement-detail-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  font-size: 13px;
  color: #6b7280;
  padding-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.announcement-detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: #1f2937;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
