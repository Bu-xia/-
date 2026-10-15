<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="8"><el-card shadow="hover"><div class="stat"><div class="num">{{ stats.courses }}</div><div class="label">课程数</div></div></el-card></el-col>
      <el-col :span="8"><el-card shadow="hover"><div class="num">{{ stats.resources }}</div><div class="label">资料数</div></el-card></el-col>
      <el-col :span="8"><el-card shadow="hover"><div class="num">{{ stats.reservations }}</div><div class="label">我的预约</div></el-card></el-col>
    </el-row>
    <el-card shadow="never" style="margin-top:16px">
      <template #header>欢迎使用 AI 智能学习辅助平台</template>
      <p style="color:#666">在这里可以浏览课程、检索课程资料、预约课程，并向 AI 助教提问。</p>
      <el-space wrap>
        <el-button type="primary" @click="$router.push('/assistant')">去问 AI 助教</el-button>
        <el-button @click="$router.push('/resources')">检索课程资料</el-button>
        <el-button @click="$router.push('/reservations')">课程预约</el-button>
      </el-space>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue';
import http from '../api/http';

const stats = reactive({ courses: 0, resources: 0, reservations: 0 });

onMounted(async () => {
  try {
    const [c, r, v] = await Promise.all([
      http.get('/courses'),
      http.get('/resources'),
      http.get('/reservations')
    ]);
    stats.courses = c.list.length;
    stats.resources = r.list.length;
    stats.reservations = v.list.length;
  } catch (e) { /* 拦截器已提示 */ }
});
</script>

<style scoped>
.stat { text-align: center; padding: 8px 0; }
.num { font-size: 30px; font-weight: 700; color: #409eff; }
.label { color: #666; margin-top: 4px; }
</style>
