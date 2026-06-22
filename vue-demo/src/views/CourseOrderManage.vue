<template>
  <div class="manage-page">
    <PageSkeleton v-if="pageLoading" variant="cards" />
    <template v-else>
    <PageIntro text="管理课程购买订单，跟踪支付状态与家长报名记录。" />
    <StatCards :items="orderStats" />
    <div class="toolbar">
      <button class="btn btn-primary" @click="handleAdd">新增课程购买</button>
    </div>
    <div v-if="!list.length" class="empty-tip">暂无订单数据</div>
    <template v-else>
      <div
        v-for="section in orderSections"
        :key="section.key"
        class="status-section"
        :class="`status-section--${section.key}`"
      >
        <div class="status-section-head">
          <span>{{ section.label }}</span>
          <span>{{ section.rows.length }} 笔</span>
        </div>
        <div class="status-section-body">
          <div
            v-for="item in section.rows"
            :key="item.id"
            class="entity-card order-card"
            :class="orderCardClass(item.status)"
          >
            <div class="entity-card-head">
              <div>
                <h3 class="entity-card-title">{{ item.courseName ?? '-' }}</h3>
                <p class="order-card-no">{{ item.orderNo ?? '-' }}</p>
              </div>
              <span :class="['badge', item.status === 1 ? 'badge-success' : item.status === 0 ? 'badge-warning' : 'badge-info']">
                {{ formatCell(item.status, 'orderStatus') }}
              </span>
            </div>
            <div class="entity-card-body">
              <div class="info-row"><span class="info-row-label">家长</span><span class="info-row-value">{{ item.parentName ?? '-' }}</span></div>
              <div class="info-row"><span class="info-row-label">教师</span><span class="info-row-value">{{ item.teacherName ?? '-' }}</span></div>
              <div class="info-row"><span class="info-row-label">费用</span><span class="info-row-value">¥{{ item.fee ?? '-' }}</span></div>
              <div class="info-row"><span class="info-row-label">支付</span><span class="info-row-value">{{ item.payTime || '-' }}</span></div>
            </div>
            <div class="entity-card-foot">
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
          <h3>{{ isEdit ? '编辑' : '新增' }}课程购买</h3>
          <button class="close-btn" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>订单号 *</label>
            <input v-model="form.orderNo" type="text" placeholder="请输入订单号" />
          </div>
          <div class="form-item">
            <label>家长</label>
            <select v-model="form.parentId"><option :value="null">请选择</option><option v-for="p in parents" :key="p.id" :value="p.id">{{ p.name }}</option></select>
          </div>
          <div class="form-item">
            <label>课程</label>
            <select v-model="form.courseId" @change="onCourseChange"><option :value="null">请选择</option><option v-for="c in courses" :key="c.id" :value="c.id">{{ c.name }}</option></select>
          </div>
          <div class="form-item">
            <label>课程名称</label>
            <input v-model="form.courseName" type="text" placeholder="请输入课程名称" />
          </div>
          <div class="form-item">
            <label>任课教师</label>
            <input v-model="form.teacherName" type="text" placeholder="请输入任课教师" />
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
            <option :value="0">待支付</option>
            <option :value="1">已支付</option>
            <option :value="2">已取消</option>
          </select>
          </div>
          <div class="form-item">
            <label>支付时间</label>
            <input v-model="form.payTime" type="datetime-local" placeholder="请输入支付时间" />
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
import { getCourseOrderListApi, addCourseOrderApi, updateCourseOrderApi, deleteCourseOrderApi } from '@/api/courseOrder'
import { getParentListApi } from '@/api/parent'
import { getCourseListApi } from '@/api/course'
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
const parents = ref([])
const courses = ref([])

const form = reactive({
  id: null,
  orderNo: '',
  parentId: null,
  courseId: null,
  courseName: '',
  teacherName: '',
  hours: '',
  fee: '',
  status: 0,
  payTime: ''
})

const orderStats = computed(() => [
  { label: '订单总数', value: list.value.length, icon: '📋', tone: 'purple' },
  { label: '待支付', value: list.value.filter(o => o.status === 0).length, icon: '⏳', tone: 'orange' },
  { label: '已支付', value: list.value.filter(o => o.status === 1).length, icon: '✅', tone: 'green' },
  { label: '已取消', value: list.value.filter(o => o.status === 2).length, icon: '✖', tone: 'red' }
])

const orderSections = computed(() => [
  { key: 'pending', label: '待支付', rows: list.value.filter(o => o.status === 0) },
  { key: 'paid', label: '已支付', rows: list.value.filter(o => o.status === 1) },
  { key: 'cancelled', label: '已取消', rows: list.value.filter(o => o.status === 2) }
].filter(s => s.rows.length > 0))

const orderCardClass = (status) => {
  if (status === 0) return 'order-card--pending'
  if (status === 1) return 'order-card--paid'
  if (status === 2) return 'order-card--cancelled'
  return ''
}

const onCourseChange = () => {
  const c = courses.value.find(x => x.id == form.courseId)
  if (c) {
    form.courseName = c.name
    form.teacherName = c.teacherName
    form.hours = c.hours
    form.fee = c.fee
  }
}

const resetForm = () => {
  Object.assign(form, { id: null, orderNo: '',
  parentId: null,
  courseId: null,
  courseName: '',
  teacherName: '',
  hours: '',
  fee: '',
  status: 0,
  payTime: '' })
}

const loadList = () => withLoading(async () => {
  try {
    const res = await getCourseOrderListApi()
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
  if (!form.orderNo) { alert('请填写订单号'); return }
  try {
    loading.value = true
    if (isEdit.value) await updateCourseOrderApi(form)
    else await addCourseOrderApi(form)
    alert('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await deleteCourseOrderApi(id)
    alert('删除成功')
    loadList()
  } catch (e) { alert(e.message) }
}

onMounted(async () => {
  loadList()
  parents.value = (await getParentListApi()).data || []
courses.value = (await getCourseListApi()).data || []
})
</script>

<style scoped>
@import '@/assets/manage.css';
</style>
