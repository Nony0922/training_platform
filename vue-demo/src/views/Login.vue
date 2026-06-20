<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-orb bg-orb--1"></div>
      <div class="bg-orb bg-orb--2"></div>
      <div class="bg-orb bg-orb--3"></div>
    </div>

    <div class="login-shell">
      <!-- 左侧品牌区 -->
      <aside class="login-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <span class="brand-icon">🌸</span>
            <div>
              <h1>心田花开</h1>
              <p>培训机构综合管理平台</p>
            </div>
          </div>

          <div class="brand-hero">
            <h2>让教育管理<br />更简单、更温暖</h2>
            <p>一站式覆盖教务、家校沟通与 AI 智能分析，助力机构高效运营。</p>
          </div>

          <ul class="brand-features">
            <li>
              <span class="feature-dot"></span>
              管理员 / 教师 PC 端协同
            </li>
            <li>
              <span class="feature-dot"></span>
              34 项功能模块全覆盖
            </li>
            <li>
              <span class="feature-dot"></span>
              AI 智能排课与学情分析
            </li>
          </ul>
        </div>

        <div class="brand-decoration">
          <div class="deco-ring deco-ring--1"></div>
          <div class="deco-ring deco-ring--2"></div>
          <div class="deco-float deco-float--1">📚</div>
          <div class="deco-float deco-float--2">✨</div>
          <div class="deco-float deco-float--3">🎓</div>
        </div>
      </aside>

      <!-- 右侧登录区 -->
      <main class="login-panel">
        <div class="login-card">
          <div class="card-header">
            <div class="mobile-logo">
              <span>🌸</span>
              <span>心田花开</span>
            </div>
            <h2>欢迎回来</h2>
            <p class="card-subtitle">请登录您的管理账号</p>
          </div>

          <form class="login-form" @submit.prevent="handleLogin">
            <div class="form-group">
              <label for="username">用户名</label>
              <div class="input-wrap">
                <span class="input-icon">👤</span>
                <input
                  id="username"
                  v-model.trim="form.username"
                  type="text"
                  placeholder="请输入用户名"
                  class="form-input"
                  autocomplete="username"
                />
              </div>
            </div>

            <div class="form-group">
              <label for="password">密码</label>
              <div class="input-wrap">
                <span class="input-icon">🔒</span>
                <input
                  id="password"
                  v-model.trim="form.password"
                  type="password"
                  placeholder="请输入密码"
                  class="form-input"
                  autocomplete="current-password"
                />
              </div>
            </div>

            <div class="form-options">
              <label class="checkbox-label">
                <input type="checkbox" v-model="rememberMe" />
                <span class="checkbox-box"></span>
                <span>记住我</span>
              </label>
            </div>

            <button type="submit" class="login-btn" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              <span>{{ loading ? '登录中...' : '登 录' }}</span>
              <span v-if="!loading" class="btn-arrow">→</span>
            </button>
          </form>

          <Transition name="fade">
            <p v-if="errorMsg" class="error-message">
              <span class="error-icon">!</span>
              {{ errorMsg }}
            </p>
          </Transition>

          <footer class="login-footer">
            <p>© 2026 心田花开培训机构 · PC 管理端</p>
          </footer>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { loginApi } from '@/api/user'
import { getDefaultRoute } from '@/router'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')
const rememberMe = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  errorMsg.value = ''
  if (!form.username || !form.password) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  try {
    loading.value = true
    const res = await loginApi({
      username: form.username,
      password: form.password
    })
    const user = res.data || {}
    if (user.role === 'parent') {
      errorMsg.value = '家长账号请使用小程序端登录'
      return
    }
    localStorage.setItem('loginUser', JSON.stringify(user))
    if (rememberMe.value) {
      localStorage.setItem('rememberUser', form.username)
    } else {
      localStorage.removeItem('rememberUser')
    }
    router.push(getDefaultRoute(user))
  } catch (error) {
    errorMsg.value = error?.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const rememberUser = localStorage.getItem('rememberUser')
  if (rememberUser) {
    form.username = rememberUser
    rememberMe.value = true
  }
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  background-color: #f5f3ff;
  background-image: url('/flower-pattern.svg'), linear-gradient(145deg, #ede9fe 0%, #faf5ff 40%, #fce7f3 100%);
  background-size: var(--flower-pattern-size) var(--flower-pattern-size), 100% 100%;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}

.bg-orb--1 {
  width: 420px;
  height: 420px;
  background: #c4b5fd;
  top: -120px;
  right: 10%;
  animation-delay: 0s;
}

.bg-orb--2 {
  width: 320px;
  height: 320px;
  background: #f0abfc;
  bottom: -80px;
  left: 5%;
  animation-delay: -3s;
}

.bg-orb--3 {
  width: 200px;
  height: 200px;
  background: #fbcfe8;
  top: 40%;
  left: 45%;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, -20px) scale(1.05); }
}

.login-shell {
  position: relative;
  z-index: 10;
  display: flex;
  width: 100%;
  max-width: 980px;
  min-height: 580px;
  margin: 24px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow:
    0 24px 80px rgba(91, 33, 182, 0.18),
    0 0 0 1px rgba(255, 255, 255, 0.6);
  animation: card-in 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}

@keyframes card-in {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 左侧品牌区 */
.login-brand {
  flex: 1;
  position: relative;
  background: linear-gradient(160deg, #6d28d9 0%, #5b21b6 45%, #4c1d95 100%);
  padding: 48px 44px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
  color: #fff;
}

.login-brand::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.12) 0%, transparent 50%);
  pointer-events: none;
}

.brand-content {
  position: relative;
  z-index: 2;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 48px;
}

.brand-icon {
  width: 52px;
  height: 52px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.brand-logo h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.brand-logo p {
  margin: 4px 0 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
}

.brand-hero h2 {
  margin: 0 0 16px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.35;
  letter-spacing: -0.02em;
}

.brand-hero p {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.75);
  max-width: 320px;
}

.brand-features {
  list-style: none;
  margin: 36px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.brand-features li {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fbbf24;
  box-shadow: 0 0 8px rgba(251, 191, 36, 0.6);
  flex-shrink: 0;
}

.brand-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.deco-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.deco-ring--1 {
  width: 280px;
  height: 280px;
  bottom: -80px;
  right: -60px;
}

.deco-ring--2 {
  width: 180px;
  height: 180px;
  bottom: 20px;
  right: 40px;
}

.deco-float {
  position: absolute;
  font-size: 28px;
  opacity: 0.35;
  animation: float 6s ease-in-out infinite;
}

.deco-float--1 { top: 18%; right: 12%; animation-delay: -1s; }
.deco-float--2 { bottom: 28%; right: 22%; animation-delay: -3s; font-size: 22px; }
.deco-float--3 { top: 55%; right: 8%; animation-delay: -2s; font-size: 24px; }

/* 右侧登录区 */
.login-panel {
  flex: 0 0 420px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 36px;
}

.login-card {
  width: 100%;
  max-width: 340px;
}

.card-header {
  margin-bottom: 32px;
}

.mobile-logo {
  display: none;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
  font-size: 18px;
  font-weight: 700;
  color: var(--primary, #7c3aed);
}

.mobile-logo span:first-child {
  font-size: 28px;
}

.card-header h2 {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: -0.02em;
}

.card-subtitle {
  margin: 0;
  font-size: 14px;
  color: #9ca3af;
}

.login-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #374151;
  font-size: 13px;
  font-weight: 600;
}

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  font-size: 16px;
  opacity: 0.5;
  pointer-events: none;
  z-index: 1;
}

.form-input {
  width: 100%;
  height: 50px;
  border: 1.5px solid #e5e7eb;
  border-radius: 12px;
  padding: 0 16px 0 44px;
  font-size: 15px;
  box-sizing: border-box;
  background: #fafafa;
  color: #1f2937;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.form-input::placeholder {
  color: #d1d5db;
}

.form-input:focus {
  outline: none;
  border-color: var(--primary, #7c3aed);
  background: #fff;
  box-shadow: 0 0 0 4px rgba(124, 58, 237, 0.1);
}

.form-options {
  margin-bottom: 24px;
}

.checkbox-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #6b7280;
  font-size: 14px;
  user-select: none;
}

.checkbox-label input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.checkbox-box {
  width: 18px;
  height: 18px;
  border: 2px solid #d1d5db;
  border-radius: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.2s, background 0.2s;
  flex-shrink: 0;
}

.checkbox-label input:checked + .checkbox-box {
  background: var(--primary, #7c3aed);
  border-color: var(--primary, #7c3aed);
}

.checkbox-label input:checked + .checkbox-box::after {
  content: '✓';
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}

.login-btn {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 50%, #5b21b6 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: transform 0.2s, box-shadow 0.2s, opacity 0.2s;
  box-shadow: 0 4px 16px rgba(124, 58, 237, 0.35);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(124, 58, 237, 0.4);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.75;
  cursor: not-allowed;
}

.btn-arrow {
  font-size: 18px;
  transition: transform 0.2s;
}

.login-btn:hover:not(:disabled) .btn-arrow {
  transform: translateX(4px);
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-message {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 20px 0 0;
  padding: 12px 16px;
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border: 1px solid #fecaca;
  border-radius: 10px;
  color: #dc2626;
  font-size: 13px;
  line-height: 1.5;
}

.error-icon {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #ef4444;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.login-footer {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f3f4f6;
  text-align: center;
}

.login-footer p {
  margin: 0;
  color: #d1d5db;
  font-size: 12px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s, transform 0.25s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

@media (max-width: 820px) {
  .login-shell {
    flex-direction: column;
    max-width: 440px;
    min-height: auto;
  }

  .login-brand {
    display: none;
  }

  .login-panel {
    flex: 1;
    padding: 36px 28px;
  }

  .mobile-logo {
    display: flex;
  }
}

@media (max-width: 480px) {
  .login-shell {
    margin: 16px;
    border-radius: 20px;
  }

  .login-panel {
    padding: 28px 20px;
  }

  .card-header h2 {
    font-size: 22px;
  }
}
</style>
