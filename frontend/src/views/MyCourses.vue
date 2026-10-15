<template>
  <div>
    <div class="section-title">我选择的课程（{{ list.length }}）</div>
    <el-row :gutter="16">
      <el-col v-for="c in list" :key="c.id" :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="course-card">
          <div class="course-head">
            <span class="course-code">{{ c.code || '课程' }}</span>
          </div>
          <h3 class="course-name">{{ c.name }}</h3>
          <p class="course-meta"><el-icon><User /></el-icon> {{ c.teacher || '未指定教师' }}</p>
          <p class="course-meta"><el-icon><Clock /></el-icon> {{ c.schedule || '时间待定' }}</p>
          <p class="course-desc">{{ c.description }}</p>
          <el-space>
            <el-button type="primary" plain size="small" @click="$router.push('/student/reservations')">去预约</el-button>
            <el-button type="success" plain size="small" @click="$router.push('/student/assistant')">问 AI 助教</el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && list.length === 0" description="还没有选择课程">
      <el-button type="primary" @click="$router.push('/student/home')">去选课</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import http from '../api/http';

const list = ref([]);
const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  try {
    const res = await http.get('/courses/my');
    list.value = res.list;
  } catch (e) { /* 拦截器已提示 */ } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.section-title { font-size: 18px; font-weight: 700; margin: 0 0 16px; }
.course-card { margin-bottom: 16px; }
.course-head { display: flex; justify-content: space-between; align-items: center; }
.course-code { color: #6366f1; font-weight: 600; }
.course-name { margin: 10px 0 6px; font-size: 17px; }
.course-meta { color: #64748b; font-size: 13px; margin: 4px 0; display: flex; align-items: center; gap: 4px; }
.course-desc { color: #94a3b8; font-size: 13px; min-height: 36px; }
</style>
