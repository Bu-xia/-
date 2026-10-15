<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo"><el-icon><School /></el-icon> AI 学习平台</div>
      <el-menu :default-active="$route.path" router background-color="#1e293b" text-color="#94a3b8" active-text-color="#ffffff">
        <el-menu-item index="/student/home"><el-icon><HomeFilled /></el-icon><span>选课中心</span></el-menu-item>
        <el-menu-item index="/student/my-courses"><el-icon><Notebook /></el-icon><span>我的课程</span></el-menu-item>
        <el-menu-item index="/student/assistant"><el-icon><ChatDotRound /></el-icon><span>AI 助教</span></el-menu-item>
        <el-menu-item index="/student/resources"><el-icon><FolderOpened /></el-icon><span>课程资料库</span></el-menu-item>
        <el-menu-item index="/student/reservations"><el-icon><Calendar /></el-icon><span>课程预约</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="page-title">{{ $route.meta.title }}</div>
        <el-dropdown @command="onCommand">
          <span class="user-info">
            <el-avatar :size="30" style="background:#6366f1">{{ (auth.user && auth.user.name || 'S').slice(0,1) }}</el-avatar>
            <span>{{ auth.user && auth.user.name }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useAuthStore } from '../store/auth';

const router = useRouter();
const auth = useAuthStore();

function onCommand(cmd) {
  if (cmd === 'logout') {
    auth.logout();
    router.push('/login');
  }
}
</script>

<style scoped>
.layout { height: 100%; }
.aside { background: #1e293b; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; gap: 8px; color: #fff; font-size: 17px; font-weight: 700; }
.header { background: #fff; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.page-title { font-size: 17px; font-weight: 600; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; color: #333; }
.main { padding: 18px; background: #f1f5f9; }
</style>
