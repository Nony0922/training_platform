<template>
  <div class="home" :class="{ 'rail-expanded': railExpanded }">
    <main class="main-content">
      <header class="header">
        <div class="header-left">
          <div class="header-breadcrumb">
            <span v-if="breadcrumb" class="breadcrumb">{{ breadcrumb }}</span>
            <h1>{{ currentTitle }}</h1>
          </div>
        </div>
        <div class="header-right">
          <div class="header-user">
            <span class="header-avatar">{{ roleLabel }}</span>
            <div class="header-user-info">
              <span class="header-user-name">{{ user?.name || '用户' }}</span>
              <span class="header-user-role">{{ roleText }}</span>
            </div>
          </div>
        </div>
      </header>
      <div class="content">
        <router-view />
      </div>
    </main>

    <!-- 右侧固定菜单：收起仅图标，展开显示名称 -->
    <aside class="icon-rail">
      <div class="rail-header">
        <span class="brand-icon brand-icon--rail">🌼</span>
        <div class="rail-brand">
          <span class="brand-name">心田花开</span>
          <span class="brand-role">{{ roleText }}</span>
        </div>
      </div>

      <nav class="rail-nav">
        <template v-if="user?.role === 'admin'">
          <div class="section-label">权限管理</div>
          <router-link to="/home/permission" class="rail-item" active-class="active">
            <span class="item-icon">🔐</span>
            <span class="item-label">权限管理</span>
          </router-link>

          <div class="section-label">学校管理</div>
          <router-link
            v-for="item in adminSchoolMenus"
            :key="item.path"
            :to="item.path"
            class="rail-item"
            active-class="active"
          >
            <span class="item-icon">{{ item.icon }}</span>
            <span class="item-label">{{ item.title }}</span>
          </router-link>
        </template>

        <template v-if="user?.role === 'teacher'">
          <div class="section-label">任课教师</div>
          <router-link
            v-for="item in teacherCommonMenus"
            :key="item.path"
            :to="item.path"
            class="rail-item"
            active-class="active"
          >
            <span class="item-icon">{{ item.icon }}</span>
            <span class="item-label">{{ item.title }}</span>
          </router-link>

          <template v-if="isHeadTeacher">
            <div class="section-label">班主任专有</div>
            <router-link
              v-for="item in headTeacherOnlyMenus"
              :key="item.path"
              :to="item.path"
              class="rail-item"
              active-class="active"
            >
              <span class="item-icon">{{ item.icon }}</span>
              <span class="item-label">{{ item.title }}</span>
            </router-link>
          </template>
        </template>
      </nav>

      <div class="rail-footer">
        <button class="rail-item rail-logout" type="button" @click="handleLogout">
          <span class="item-icon">⏻</span>
          <span class="item-label">退出登录</span>
        </button>
        <button
          class="rail-toggle"
          type="button"
          :title="railExpanded ? '收起菜单' : '展开菜单'"
          @click="toggleRail"
        >
          <span class="toggle-icon">{{ railExpanded ? '»' : '«' }}</span>
          <span class="toggle-label">{{ railExpanded ? '收起' : '展开' }}</span>
        </button>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getUserByIdApi } from '@/api/user'

const RAIL_KEY = 'xthk-rail-expanded'

const router = useRouter()
const route = useRoute()

const railExpanded = ref(localStorage.getItem(RAIL_KEY) === '1')

const toggleRail = () => {
  railExpanded.value = !railExpanded.value
  localStorage.setItem(RAIL_KEY, railExpanded.value ? '1' : '0')
}

const adminSchoolMenus = [
  { path: '/home/messages', title: '留言管理', icon: '💬' },
  { path: '/home/announcements', title: '公告管理', icon: '📢' },
  { path: '/home/students', title: '学生管理', icon: '👨‍🎓' },
  { path: '/home/teachers', title: '教师管理', icon: '👨‍🏫' },
  { path: '/home/parents', title: '家长管理', icon: '👪' },
  { path: '/home/classes', title: '班级管理', icon: '🏫' },
  { path: '/home/courses', title: '课程管理', icon: '📚' },
  { path: '/home/course-orders', title: '课程订单管理', icon: '🛒' },
  { path: '/home/schedules', title: '课程表管理', icon: '📅' },
  { path: '/home/progress', title: '教学进度管理', icon: '📈' },
  { path: '/home/exams', title: '考试管理', icon: '📝' },
  { path: '/home/scores', title: '成绩管理', icon: '🏆' },
  { path: '/home/attendance', title: '考勤管理', icon: '✅' },
  { path: '/home/abnormal-attendance', title: '异常考勤管理', icon: '⚠️' },
  { path: '/home/schedule-ai', title: 'AI 智能排课', icon: '🤖' },
  { path: '/home/learning-report', title: 'AI 学情分析', icon: '📊' }
]

const teacherCommonMenus = [
  { path: '/home/browse/announcements', title: '公告浏览', icon: '📢', breadcrumb: '教师' },
  { path: '/home/browse/courses', title: '我的课程', icon: '📚', breadcrumb: '教师' },
  { path: '/home/teacher/schedule', title: '我的课表', icon: '📅', breadcrumb: '教师' },
  { path: '/home/teacher/exams', title: '考试管理', icon: '📝', breadcrumb: '教师' },
  { path: '/home/teacher/subject/attendance', title: '授课考勤', icon: '✅', breadcrumb: '教师' },
  { path: '/home/teacher/subject/scores', title: '授课成绩', icon: '🏆', breadcrumb: '教师' },
  { path: '/home/teacher/progress', title: '教学进度管理', icon: '📈', breadcrumb: '教师' },
  { path: '/home/teacher/learning-report', title: 'AI 学情分析', icon: '📊', breadcrumb: '教师' }
]

const headTeacherOnlyMenus = [
  { path: '/home/browse/students', title: '本班学生', icon: '👨‍🎓', breadcrumb: '班主任' },
  { path: '/home/browse/parents', title: '本班家长', icon: '👪', breadcrumb: '班主任' },
  { path: '/home/teacher/leave', title: '本班请假', icon: '📋', breadcrumb: '班主任' },
  { path: '/home/teacher/home-visit', title: '家访管理', icon: '🏠', breadcrumb: '班主任' },
  { path: '/home/teacher/attendance', title: '本班考勤', icon: '✅', breadcrumb: '班主任' },
  { path: '/home/teacher/scores', title: '本班成绩', icon: '🏆', breadcrumb: '班主任' }
]

const isHeadTeacher = computed(() => user.value?.teacherLevel === 2)

const allRouteTitles = [
  { path: '/home/permission', title: '权限管理', breadcrumb: '权限管理' },
  ...adminSchoolMenus.map(m => ({ ...m, breadcrumb: '学校管理' })),
  ...teacherCommonMenus,
  ...headTeacherOnlyMenus
]

const user = ref(null)

const loadUserFromStorage = () => {
  try {
    user.value = JSON.parse(localStorage.getItem('loginUser'))
  } catch {
    user.value = null
  }
}

onMounted(async () => {
  loadUserFromStorage()
  const current = user.value
  if (current?.role === 'teacher' && current.id) {
    try {
      const res = await getUserByIdApi(current.id)
      const fresh = res.data
      if (fresh) {
        const merged = { ...current, ...fresh }
        localStorage.setItem('loginUser', JSON.stringify(merged))
        user.value = merged
      }
    } catch {
      // 刷新失败时沿用本地缓存
    }
  }
})

const roleText = computed(() => {
  const u = user.value
  if (!u) return ''
  if (u.role === 'admin') return '管理员'
  if (u.role === 'teacher') return u.teacherLevel === 2 ? '班主任' : '任课教师'
  return u.role
})

const roleLabel = computed(() => {
  const u = user.value
  if (!u) return '👤'
  if (u.role === 'admin') return '管'
  if (u.role === 'teacher') return '师'
  return '👤'
})

const currentTitle = computed(() => {
  const item = allRouteTitles.find(m => m.path === route.path)
  return item?.title || route.meta?.title || '心田花开培训机构综合管理平台'
})

const breadcrumb = computed(() => {
  const item = allRouteTitles.find(m => m.path === route.path)
  return item?.breadcrumb || ''
})

const handleLogout = () => {
  localStorage.removeItem('loginUser')
  router.push('/')
}
</script>

<style scoped>
.home {
  --rail-collapsed: 52px;
  --rail-expanded: 210px;
  --rail-width: var(--rail-collapsed);
  min-height: 100vh;
}

.home.rail-expanded {
  --rail-width: var(--rail-expanded);
}

.main-content {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-right: var(--rail-width);
  transition: padding-right 0.22s cubic-bezier(0.4, 0, 0.2, 1);
  background-color: var(--bg, #fafdf8);
  background-image: var(--flower-pattern);
  background-size: var(--flower-pattern-size) var(--flower-pattern-size);
  min-width: 0;
}

.header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  padding: 14px 28px;
  box-shadow: 0 1px 0 rgba(46, 173, 106, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-left {
  min-width: 0;
}

.breadcrumb {
  display: block;
  font-size: 12px;
  color: var(--primary, #2ead6a);
  margin-bottom: 2px;
  font-weight: 500;
}

.header h1 {
  margin: 0;
  font-size: 20px;
  color: #1f2937;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  background: var(--primary-light, #e8f8f0);
  border: 1px solid var(--border, #b8e6ce);
  border-radius: 999px;
}

.header-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent, #f5c842), var(--accent-dark, #e8b020));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.header-user-name {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.header-user-role {
  font-size: 11px;
  color: var(--primary, #2ead6a);
}

.content {
  flex: 1;
  padding: 24px 28px 32px;
  overflow: auto;
}

/* ---- 固定侧栏 ---- */
.icon-rail {
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
  width: var(--rail-width);
  display: flex;
  flex-direction: column;
  background: #fff;
  border-left: 1px solid var(--border, #b8e6ce);
  z-index: 100;
  transition: width 0.22s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.rail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 56px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--border, #b8e6ce);
  padding: 0;
  justify-content: center;
  transition: padding 0.22s, justify-content 0.22s;
}

.rail-expanded .rail-header {
  padding: 0 14px;
  justify-content: flex-start;
}

.brand-icon--rail {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  margin: 0 auto;
  font-size: 22px;
  background: linear-gradient(135deg, #fff8e1, #e8f8f0);
  border: 1px solid var(--border, #b8e6ce);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: margin 0.22s;
}

.rail-expanded .brand-icon--rail {
  margin: 0;
}

.rail-brand {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  white-space: nowrap;
  opacity: 0;
  max-width: 0;
  transition: opacity 0.18s, max-width 0.22s;
}

.rail-expanded .rail-brand {
  opacity: 1;
  max-width: 140px;
}

.brand-name {
  font-size: 14px;
  font-weight: 700;
  color: #1f8f55;
  line-height: 1.3;
}

.brand-role {
  font-size: 11px;
  color: #9ca3af;
}

.rail-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 6px 0;
}

.rail-nav::-webkit-scrollbar {
  width: 3px;
}

.rail-nav::-webkit-scrollbar-thumb {
  background: rgba(46, 173, 106, 0.2);
  border-radius: 3px;
}

.section-label {
  padding: 8px 0 2px;
  font-size: 10px;
  font-weight: 600;
  color: var(--accent-dark, #c9a020);
  letter-spacing: 0.05em;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  opacity: 0;
  max-height: 0;
  transition: opacity 0.18s, max-height 0.22s, padding 0.22s;
}

.rail-expanded .section-label {
  opacity: 1;
  max-height: 28px;
  padding: 8px 14px 2px;
  text-align: left;
}

.rail-item {
  display: flex;
  align-items: center;
  width: 100%;
  height: 40px;
  padding: 0;
  border: none;
  background: none;
  color: #4b5563;
  text-decoration: none;
  cursor: pointer;
  font-family: inherit;
  gap: 0;
  justify-content: center;
  transition: background 0.15s, justify-content 0.22s, padding 0.22s;
}

.rail-expanded .rail-item {
  justify-content: flex-start;
  padding: 0 12px 0 0;
  gap: 4px;
}

.item-icon {
  font-size: 20px;
  line-height: 1;
  flex-shrink: 0;
  width: var(--rail-collapsed);
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-label {
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  opacity: 0;
  max-width: 0;
  transition: opacity 0.18s, max-width 0.22s;
}

.rail-expanded .item-label {
  opacity: 1;
  max-width: 150px;
}

.rail-item:hover {
  background: var(--accent-light, #fff8e1);
}

.rail-item.active {
  background: var(--primary-light, #e8f8f0);
  color: var(--primary-dark, #1f8f55);
}

.rail-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  width: 3px;
  height: 24px;
  border-radius: 0 3px 3px 0;
  background: var(--primary, #2ead6a);
}

.rail-item {
  position: relative;
}

.rail-footer {
  flex-shrink: 0;
  border-top: 1px solid var(--border, #b8e6ce);
}

.rail-logout:hover {
  background: #fef2f2;
}

.rail-logout .item-icon {
  color: #ef4444;
}

.rail-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 40px;
  border: none;
  background: var(--primary-light, #e8f8f0);
  color: var(--primary-dark, #1f8f55);
  cursor: pointer;
  font-family: inherit;
  gap: 0;
  transition: background 0.15s, gap 0.22s, padding 0.22s;
}

.rail-expanded .rail-toggle {
  justify-content: flex-start;
  padding: 0 16px;
  gap: 8px;
}

.rail-toggle:hover {
  background: #d4f0e0;
}

.toggle-icon {
  font-size: 16px;
  font-weight: 700;
  width: var(--rail-collapsed);
  text-align: center;
  flex-shrink: 0;
}

.toggle-label {
  font-size: 12px;
  font-weight: 500;
  opacity: 0;
  max-width: 0;
  overflow: hidden;
  white-space: nowrap;
  transition: opacity 0.18s, max-width 0.22s;
}

.rail-expanded .toggle-label {
  opacity: 1;
  max-width: 60px;
}

@media (max-width: 768px) {
  .header {
    padding: 12px 16px;
  }

  .header-user-info {
    display: none;
  }

  .content {
    padding: 16px;
  }
}
</style>
