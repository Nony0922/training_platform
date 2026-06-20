<template>
  <div class="home" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <aside class="sidebar">
      <div class="logo">
        <div class="logo-icon">🌸</div>
        <div class="logo-text">
          <h2>心田花开</h2>
          <p class="logo-sub">PC端 · {{ roleText }}</p>
        </div>
      </div>

      <nav class="nav-menu">
        <!-- 管理员菜单 -->
        <template v-if="user?.role === 'admin'">
          <div class="nav-section">
            <div class="section-label">权限管理</div>
            <router-link
              to="/home/permission"
              class="nav-item"
              active-class="active"
              title="权限管理"
            >
              <span class="nav-icon">🔐</span>
              <span class="nav-text">权限管理</span>
            </router-link>
          </div>

          <div class="nav-section">
            <div class="section-label">学校管理</div>
            <router-link
              v-for="item in adminSchoolMenus"
              :key="item.path"
              :to="item.path"
              class="nav-item nav-sub"
              active-class="active"
              :title="item.title"
            >
              <span class="nav-icon">{{ item.icon }}</span>
              <span class="nav-text">{{ item.title }}</span>
            </router-link>
          </div>
        </template>

        <!-- 教师菜单 -->
        <template v-if="user?.role === 'teacher'">
          <div class="nav-section">
            <div class="section-label">任课教师功能</div>
            <router-link
              v-for="item in teacherCommonMenus"
              :key="item.path"
              :to="item.path"
              class="nav-item"
              active-class="active"
              :title="item.title"
            >
              <span class="nav-icon">{{ item.icon }}</span>
              <span class="nav-text">{{ item.title }}</span>
            </router-link>
          </div>

          <div v-if="isHeadTeacher" class="nav-section">
            <div class="section-label">班主任专有</div>
            <router-link
              v-for="item in headTeacherOnlyMenus"
              :key="item.path"
              :to="item.path"
              class="nav-item nav-sub"
              active-class="active"
              :title="item.title"
            >
              <span class="nav-icon">{{ item.icon }}</span>
              <span class="nav-text">{{ item.title }}</span>
            </router-link>
          </div>
        </template>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info">
          <div class="user-avatar">{{ roleLabel }}</div>
          <div class="user-detail">
            <span class="user-name">{{ user?.name || '用户' }}</span>
            <span class="user-role">{{ roleText }}</span>
          </div>
          <button class="logout-btn" title="退出登录" @click="handleLogout">
            <span class="logout-icon">⏻</span>
            <span class="logout-text">退出</span>
          </button>
        </div>
        <button
          class="sidebar-collapse-btn"
          type="button"
          :title="sidebarCollapsed ? '展开侧栏' : '收起侧栏'"
          @click="toggleSidebar"
        >
          <span class="collapse-icon">{{ sidebarCollapsed ? '»' : '«' }}</span>
        </button>
      </div>
    </aside>

    <main class="main-content">
      <header class="header">
        <div class="header-left">
          <button
            class="header-toggle"
            type="button"
            :title="sidebarCollapsed ? '展开侧栏' : '收起侧栏'"
            @click="toggleSidebar"
          >
            <span class="toggle-bar"></span>
            <span class="toggle-bar"></span>
            <span class="toggle-bar"></span>
          </button>
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
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const SIDEBAR_KEY = 'xthk-sidebar-collapsed'

const router = useRouter()
const route = useRoute()

const sidebarCollapsed = ref(localStorage.getItem(SIDEBAR_KEY) === '1')

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
  localStorage.setItem(SIDEBAR_KEY, sidebarCollapsed.value ? '1' : '0')
}

const adminSchoolMenus = [
  { path: '/home/messages', title: '留言管理', icon: '💬' },
  { path: '/home/announcements', title: '公告管理', icon: '📢' },
  { path: '/home/students', title: '学生管理', icon: '👨‍🎓' },
  { path: '/home/teachers', title: '教师管理', icon: '👨‍🏫' },
  { path: '/home/parents', title: '家长管理', icon: '👪' },
  { path: '/home/classes', title: '班级管理', icon: '🏫' },
  { path: '/home/courses', title: '课程管理', icon: '📚' },
  { path: '/home/progress', title: '教学进度管理', icon: '📈' },
  { path: '/home/exams', title: '考试管理', icon: '📝' },
  { path: '/home/attendance', title: '考勤管理', icon: '✅' },
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

const user = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('loginUser'))
  } catch {
    return null
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
  --sidebar-width: 260px;
  display: flex;
  min-height: 100vh;
}

.home.sidebar-collapsed {
  --sidebar-width: 76px;
}

.sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  background: linear-gradient(195deg, #6d28d9 0%, #4c1d95 55%, #3b0764 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(76, 29, 149, 0.18);
  transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 20;
  overflow: hidden;
}

.sidebar::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 10%, rgba(255, 255, 255, 0.08) 0%, transparent 45%);
  pointer-events: none;
}

.logo {
  padding: 22px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 88px;
}

.logo-icon {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  backdrop-filter: blur(4px);
}

.logo-text {
  overflow: hidden;
  white-space: nowrap;
  transition: opacity 0.2s, width 0.28s;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.logo-sub {
  margin: 4px 0 0;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.65);
}

.home.sidebar-collapsed .logo {
  justify-content: center;
  padding: 22px 12px;
}

.home.sidebar-collapsed .logo-text {
  opacity: 0;
  width: 0;
}

.nav-menu {
  flex: 1;
  padding: 12px 10px 16px;
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-menu::-webkit-scrollbar {
  width: 4px;
}

.nav-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}

.nav-section {
  margin-bottom: 6px;
}

.section-label {
  padding: 8px 14px 6px;
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.4);
  letter-spacing: 0.08em;
  white-space: nowrap;
  overflow: hidden;
  transition: opacity 0.2s;
}

.home.sidebar-collapsed .section-label {
  opacity: 0;
  height: 0;
  padding: 0;
  margin: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  margin-bottom: 2px;
  color: rgba(255, 255, 255, 0.78);
  text-decoration: none;
  transition: background 0.2s, color 0.2s, transform 0.15s;
  border-radius: 10px;
  font-size: 14px;
  white-space: nowrap;
}

.nav-sub {
  font-size: 13px;
}

.nav-icon {
  font-size: 16px;
  width: 24px;
  text-align: center;
  flex-shrink: 0;
}

.nav-text {
  overflow: hidden;
  transition: opacity 0.2s, width 0.28s;
}

.home.sidebar-collapsed .nav-item {
  justify-content: center;
  padding: 11px 0;
}

.home.sidebar-collapsed .nav-text {
  opacity: 0;
  width: 0;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.nav-item.active {
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.12);
}

.sidebar-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 12px 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 6px 10px;
  overflow: hidden;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-detail {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: opacity 0.2s, width 0.28s;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
}

.home.sidebar-collapsed .user-detail,
.home.sidebar-collapsed .logout-text {
  opacity: 0;
  width: 0;
  overflow: hidden;
}

.home.sidebar-collapsed .user-info {
  justify-content: center;
  padding-bottom: 8px;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border: none;
  border-radius: 8px;
  background: rgba(239, 68, 68, 0.85);
  color: #fff;
  cursor: pointer;
  font-size: 12px;
  flex-shrink: 0;
  transition: background 0.2s;
}

.logout-btn:hover {
  background: #ef4444;
}

.home.sidebar-collapsed .logout-btn {
  padding: 6px 8px;
}

.logout-icon {
  font-size: 14px;
  line-height: 1;
}

.sidebar-collapse-btn {
  width: 100%;
  padding: 8px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.75);
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s, color 0.2s;
}

.sidebar-collapse-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f5f3ff;
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
  box-shadow: 0 1px 0 rgba(124, 58, 237, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.header-toggle {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  width: 38px;
  height: 38px;
  padding: 0;
  border: 1px solid #e9d5ff;
  border-radius: 10px;
  background: #faf5ff;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.2s, border-color 0.2s;
}

.header-toggle:hover {
  background: #f3e8ff;
  border-color: #d8b4fe;
}

.toggle-bar {
  display: block;
  width: 16px;
  height: 2px;
  margin: 0 auto;
  background: #7c3aed;
  border-radius: 2px;
  transition: transform 0.2s;
}

.header-breadcrumb {
  min-width: 0;
}

.breadcrumb {
  display: block;
  font-size: 12px;
  color: #7c3aed;
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

.header-right {
  flex-shrink: 0;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  background: #faf5ff;
  border: 1px solid #ede9fe;
  border-radius: 999px;
}

.header-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c3aed, #5b21b6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.header-user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.header-user-name {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.header-user-role {
  font-size: 11px;
  color: #7c3aed;
}

.content {
  flex: 1;
  padding: 24px 28px 32px;
  overflow: auto;
}

@media (max-width: 768px) {
  .header-user-info {
    display: none;
  }

  .header {
    padding: 12px 16px;
  }

  .content {
    padding: 16px;
  }
}
</style>
