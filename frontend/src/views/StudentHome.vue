<template>
  <div>
    <div class="hero">
      <div>
        <h1>欢迎回来，{{ auth.user && auth.user.name }}</h1>
        <p>挑选感兴趣的课程，随时向 AI 助教提问，开启你的学习之旅。</p>
        <el-button type="primary" size="large" round @click="$router.push('/student/assistant')">
          <el-icon><ChatDotRound /></el-icon> 去问 AI 助教
        </el-button>
      </div>
    </div>

    <div class="section-title">全部课程</div>
    <el-input v-model="keyword" placeholder="搜索课程 / 教师" clearable style="max-width: 320px; margin-bottom: 16px" @keyup.enter="load" />
    <el-row :gutter="16">
      <el-col v-for="c in courses" :key="c.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card shadow="hover" class="course-card">
          <div class="course-head">
            <span class="course-code">{{ c.code || '课程' }}</span>
            <el-tag v-if="selectedIds.includes(c.id)" type="success" effect="dark" size="small">已选</el-tag>
          </div>
          <h3 class="course-name">{{ c.name }}</h3>
          <p class="course-meta"><el-icon><User /></el-icon> {{ c.teacher || '未指定教师' }}</p>
          <p class="course-meta"><el-icon><Clock /></el-icon> {{ c.schedule || '时间待定' }}</p>
          <p class="course-meta">
            <el-icon><UserFilled /></el-icon>
            剩余名额：{{ c.capacity == null ? '不限' : (c.capacity > 0 ? c.capacity : '已满') }}
          </p>
          <p class="course-desc">{{ c.description }}</p>
          <el-button
            :type="selectedIds.includes(c.id) ? 'success' : 'primary'"
            plain
            style="width:100%"
            :disabled="!selectedIds.includes(c.id) && c.capacity != null && c.capacity <= 0"
            @click="toggle(c)"
          >
            {{ selectedIds.includes(c.id) ? '取消选择' : (c.capacity != null && c.capacity <= 0 ? '名额已满' : '选择课程') }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && courses.length === 0" description="暂无课程" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import http from '../api/http';
import { useAuthStore } from '../store/auth';

const auth = useAuthStore();
const courses = ref([]);
const selectedIds = ref([]);
const keyword = ref('');
const loading = ref(false);

async function load() {
  loading.value = true;
  try {
    const [all, mine] = await Promise.all([
      http.get('/courses', { params: { keyword: keyword.value || undefined } }),
      http.get('/courses/my')
    ]);
    courses.value = all.list;
    selectedIds.value = mine.selectedIds || [];
  } catch (e) { /* 拦截器已提示 */ } finally {
    loading.value = false;
  }
}

async function toggle(c) {
  // 选课/退选：维护前端已选状态，并调用后端接口扣减/加回名额
  const selected = selectedIds.value.includes(c.id);
  try {
    if (selected) {
      await http.post(`/courses/${c.id}/deselect`);
      selectedIds.value = selectedIds.value.filter((id) => id !== c.id);
      ElMessage.success('已取消选择');
    } else {
      await http.post(`/courses/${c.id}/select`);
      selectedIds.value = [...selectedIds.value, c.id];
      ElMessage.success('选课成功');
    }
  } catch (e) { /* 拦截器已提示 */ }
}

onMounted(load);
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 60%, #a855f7 100%);
  color: #fff; border-radius: 16px; padding: 32px; margin-bottom: 24px;
}
.hero h1 { margin: 0 0 8px; font-size: 26px; }
.hero p { margin: 0 0 16px; opacity: .92; }
.section-title { font-size: 18px; font-weight: 700; margin: 0 0 8px; }
.course-card { margin-bottom: 16px; }
.course-head { display: flex; justify-content: space-between; align-items: center; }
.course-code { color: #6366f1; font-weight: 600; }
.course-name { margin: 10px 0 6px; font-size: 17px; }
.course-meta { color: #64748b; font-size: 13px; margin: 4px 0; display: flex; align-items: center; gap: 4px; }
.course-desc { color: #94a3b8; font-size: 13px; min-height: 36px; }
</style>
