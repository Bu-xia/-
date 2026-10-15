<template>
  <div class="login-wrap">
    <div class="brand">
      <h1>AI 智能学习辅助平台</h1>
      <p>对接通义千问大模型，整合课程资料库、选课与预约，让学习更高效。</p>
    </div>
    <el-card class="login-card">
      <h2 class="title">{{ mode === 'login' ? '欢迎回来' : '创建账号' }}</h2>
      <el-form @submit.prevent>
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="submit">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item v-if="mode === 'register'">
          <el-input v-model="form.name" placeholder="姓名" size="large">
            <template #prefix><el-icon><Avatar /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">
          {{ mode === 'login' ? '登录' : '注册' }}
        </el-button>
      </el-form>
      <p class="hint">
        {{ mode === 'login' ? '还没有账号？' : '已有账号？' }}
        <el-link type="primary" @click="toggle">{{ mode === 'login' ? '去注册' : '去登录' }}</el-link>
      </p>
      <p class="hint dim">默认管理员：admin / admin123</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import http from '../api/http';
import { useAuthStore } from '../store/auth';

const router = useRouter();
const auth = useAuthStore();
const mode = ref('login');
const loading = ref(false);
const form = reactive({ username: '', password: '', name: '' });

function toggle() {
  mode.value = mode.value === 'login' ? 'register' : 'login';
}

async function submit() {
  if (!form.username || !form.password) return ElMessage.warning('请输入用户名和密码');
  loading.value = true;
  try {
    const url = mode.value === 'login' ? '/auth/login' : '/auth/register';
    const payload = mode.value === 'login'
      ? { username: form.username, password: form.password }
      : { username: form.username, password: form.password, name: form.name };
    const res = await http.post(url, payload);
    auth.setAuth(res.token, res.user);
    ElMessage.success(mode.value === 'login' ? '登录成功' : '注册成功');
    router.push(res.user.role === 'admin' ? '/admin/dashboard' : '/student/home');
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-wrap { height: 100%; display: flex; align-items: center; justify-content: center; gap: 60px; padding: 20px; background: linear-gradient(135deg, #1e293b 0%, #312e81 55%, #6d28d9 100%); }
.brand { color: #fff; max-width: 420px; }
.brand h1 { font-size: 38px; margin: 0 0 16px; }
.brand p { font-size: 16px; opacity: .85; line-height: 1.8; }
.login-card { width: 380px; padding: 24px 20px 8px; border-radius: 16px; }
.title { text-align: center; margin: 0 0 22px; color: #1e293b; }
.hint { text-align: center; color: #64748b; font-size: 13px; margin-top: 10px; }
.hint.dim { color: #94a3b8; }
@media (max-width: 860px) { .brand { display: none; } }
</style>
