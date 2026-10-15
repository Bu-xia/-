<template>
  <el-container class="layout">
    <el-aside width="210px" class="aside">
      <div class="logo">AI 学习辅助平台</div>
      <el-menu :default-active="$route.path" router background-color="#1f2d3d" text-color="#a6adb4" active-text-color="#ffffff">
        <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon><span>工作台</span></el-menu-item>
        <el-menu-item index="/courses"><el-icon><Reading /></el-icon><span>课程</span></el-menu-item>
        <el-menu-item index="/resources"><el-icon><FolderOpened /></el-icon><span>课程资料库</span></el-menu-item>
        <el-menu-item index="/reservations"><el-icon><Calendar /></el-icon><span>课程预约</span></el-menu-item>
        <el-menu-item index="/assistant"><el-icon><ChatDotRound /></el-icon><span>AI 助教</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="page-title">{{ $route.meta.title }}</div>
        <el-dropdown @command="onCommand">
          <span class="user-info">
            <el-icon><UserFilled /></el-icon> {{ auth.user && auth.user.name }}
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
.aside { background: #1f2d3d; }
.logo { height: 56px; line-height: 56px; text-align: center; color: #fff; font-size: 16px; font-weight: 600; }
.header { background: #fff; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
.page-title { font-size: 16px; font-weight: 600; }
.user-info { display: flex; align-items: center; gap: 4px; cursor: pointer; color: #333; }
.main { padding: 16px; }
</style>
