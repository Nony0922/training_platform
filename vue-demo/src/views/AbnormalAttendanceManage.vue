<template>
  <div class="manage-page abnormal-page">
    <PageSkeleton v-if="pageLoading" variant="table" />
    <template v-else>
      <PageIntro text="考勤管理中迟到、早退、缺勤记录会自动同步至本页；对待处理记录点击「处理」填写结果，不需要的记录可「删除」。" />
      <StatCards :items="abnormalStats" />

      <div class="toolbar abnormal-toolbar">
        <div class="filter-tabs">
          <button
            v-for="tab in filterTabs"
            :key="tab.value"
            type="button"
            class="filter-tab"
            :class="{ 'filter-tab--active': filter === tab.value }"
            @click="filter = tab.value"
          >
            {{ tab.label }}
            <span class="filter-tab-count">{{ tab.count }}</span>
          </button>
        </div>
      </div>

      <div v-if="!filteredList.length" class="empty-tip">暂无异常考勤数据</div>

      <div v-else class="table-container">
        <table class="data-table data-table--comfortable">
          <thead>
            <tr>
              <th>学生</th>
              <th>考勤日期</th>
              <th>课程</th>
              <th>异常类型</th>
              <th>描述</th>
              <th>处理状态</th>
              <th>处理结果</th>
              <th>处理时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredList" :key="item.id">
              <td class="cell-user-name">{{ item.studentName ?? '-' }}</td>
              <td>{{ item.attendDate || '-' }}</td>
              <td>{{ item.courseName || '-' }}</td>
              <td>
                <span :class="abnormalTypeChipClass(item.abnormalType)">
                  {{ formatCell(item.abnormalType, 'abnormalType') }}
                </span>
              </td>
              <td class="desc-cell">{{ item.description || '-' }}</td>
              <td>
                <span :class="['badge', Number(item.handleStatus) === 1 ? 'badge-success' : 'badge-warning']">
                  {{ formatCell(item.handleStatus, 'handleStatus') }}
                </span>
              </td>
              <td>{{ item.handleResult || '-' }}</td>
              <td>{{ item.handleTime || '-' }}</td>
              <td class="actions">
                <button
                  v-if="Number(item.handleStatus) !== 1"
                  class="btn btn-sm btn-primary"
                  @click="openHandle(item)"
                >
                  处理
                </button>
                <button class="btn btn-sm btn-danger" @click="handleDelete(item.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="handleVisible" class="dialog-overlay" @click.self="handleVisible = false">
        <div class="dialog">
          <div class="dialog-header">
            <h3>处理异常考勤</h3>
            <button class="close-btn" @click="handleVisible = false">&times;</button>
          </div>
          <div class="dialog-body">
            <div class="handle-summary">
              <p><strong>学生：</strong>{{ handleTarget?.studentName ?? '-' }}</p>
              <p><strong>日期：</strong>{{ handleTarget?.attendDate || '-' }} · {{ handleTarget?.courseName || '-' }}</p>
              <p><strong>异常类型：</strong>{{ formatCell(handleTarget?.abnormalType, 'abnormalType') }}</p>
              <p><strong>描述：</strong>{{ handleTarget?.description || '-' }}</p>
            </div>
            <div class="form-item">
              <label>处理结果 *</label>
              <textarea
                v-model="handleResult"
                placeholder="请填写处理情况，如：已联系家长、已补签等"
                rows="4"
              ></textarea>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="btn" @click="handleVisible = false">取消</button>
            <button class="btn btn-primary" :disabled="loading" @click="submitHandle">
              {{ loading ? '提交中...' : '确认' }}
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  getAbnormalAttendanceListApi,
  handleAbnormalAttendanceApi,
  deleteAbnormalAttendanceApi
} from '@/api/abnormalAttendance'
import PageSkeleton from '@/components/PageSkeleton.vue'
import PageIntro from '@/components/PageIntro.vue'
import StatCards from '@/components/StatCards.vue'
import { usePageLoading } from '@/composables/usePageLoading'
import { useFormatCell } from '@/composables/useFormatCell'

const { formatCell } = useFormatCell()
const { pageLoading, withLoading } = usePageLoading()

const list = ref([])
const handleVisible = ref(false)
const loading = ref(false)
const filter = ref('all')
const handleTarget = ref(null)
const handleResult = ref('')

const pendingList = computed(() => list.value.filter(a => Number(a.handleStatus) !== 1))
const handledList = computed(() => list.value.filter(a => Number(a.handleStatus) === 1))

const filteredList = computed(() => {
  if (filter.value === 'pending') return pendingList.value
  if (filter.value === 'handled') return handledList.value
  return list.value
})

const filterTabs = computed(() => [
  { label: '全部', value: 'all', count: list.value.length },
  { label: '待处理', value: 'pending', count: pendingList.value.length },
  { label: '已处理', value: 'handled', count: handledList.value.length }
])

const abnormalStats = computed(() => [
  { label: '异常总数', value: list.value.length, icon: '⚠️', tone: 'red' },
  { label: '待处理', value: pendingList.value.length, icon: '⏳', tone: 'orange' },
  { label: '已处理', value: handledList.value.length, icon: '✅', tone: 'green' }
])

const abnormalTypeChipClass = (type) => {
  const map = { 2: 'tag-chip tag-chip--orange', 3: 'tag-chip tag-chip--purple', 4: 'tag-chip tag-chip--red' }
  return map[type] || 'tag-chip'
}

const getLoginUserId = () => {
  try {
    const user = JSON.parse(localStorage.getItem('loginUser'))
    return user?.id ?? null
  } catch {
    return null
  }
}

const loadList = () => withLoading(async () => {
  try {
    const res = await getAbnormalAttendanceListApi()
    list.value = res.data || []
  } catch (e) { alert(e.message) }
})

const openHandle = (item) => {
  handleTarget.value = item
  handleResult.value = ''
  handleVisible.value = true
}

const submitHandle = async () => {
  if (!handleResult.value.trim()) {
    alert('请填写处理结果')
    return
  }
  try {
    loading.value = true
    await handleAbnormalAttendanceApi(handleTarget.value.id, {
      handleResult: handleResult.value.trim(),
      handlerId: getLoginUserId()
    })
    alert('处理成功')
    handleVisible.value = false
    loadList()
  } catch (e) {
    alert(e.message)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteAbnormalAttendanceApi(id)
    alert('删除成功')
    loadList()
  } catch (e) { alert(e.message) }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
@import '@/assets/manage.css';

.abnormal-page :deep(.data-table) {
  font-size: 13px;
}

.abnormal-page :deep(.data-table th) {
  font-size: 12px;
  padding: 10px 12px;
}

.abnormal-page :deep(.data-table td) {
  padding: 10px 12px;
  font-size: 13px;
  color: #4b5563;
}

.abnormal-page :deep(.cell-user-name) {
  font-size: 13px;
}

.abnormal-page :deep(.tag-chip) {
  font-size: 12px;
  padding: 2px 8px;
}

.abnormal-page :deep(.badge) {
  font-size: 12px;
  padding: 3px 8px;
}

.abnormal-page :deep(.btn-sm) {
  font-size: 12px;
  padding: 4px 10px;
}

.desc-cell {
  max-width: 200px;
  font-size: 12px;
  white-space: normal;
  word-break: break-word;
  line-height: 1.45;
  color: #6b7280;
}

.handle-summary {
  padding: 12px 14px;
  margin-bottom: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border-left: 3px solid #7c3aed;
  font-size: 14px;
  line-height: 1.7;
  color: #374151;
}

.handle-summary p {
  margin: 0 0 6px;
}

.handle-summary p:last-child {
  margin-bottom: 0;
}
</style>
