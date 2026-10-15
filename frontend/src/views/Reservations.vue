<template>
  <div>
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header>预约课程</template>
      <el-form inline>
        <el-form-item label="课程">
          <el-select v-model="book.courseId" placeholder="选择课程" style="width: 180px">
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="book.reserveDate" type="date" value-format="YYYY-MM-DD" style="width: 160px" />
        </el-form-item>
        <el-form-item label="时间段">
          <el-select v-model="book.timeSlot" style="width: 150px">
            <el-option v-for="t in slots" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="bookReserve">提交预约</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>{{ auth.isAdmin ? '全部预约' : '我的预约' }}</template>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="courseName" label="课程" min-width="130" />
        <el-table-column prop="studentName" label="学生" width="110" />
        <el-table-column prop="reserveDate" label="日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="130" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="auth.isAdmin && row.status === 'pending'">
              <el-button link type="success" @click="setStatus(row, 'confirmed')">确认</el-button>
              <el-button link type="danger" @click="setStatus(row, 'cancelled')">取消</el-button>
            </template>
            <el-button v-if="!auth.isAdmin && row.status === 'pending'" link type="danger" @click="cancel(row)">取消预约</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import http from '../api/http';
import { useAuthStore } from '../store/auth';

const auth = useAuthStore();
const courses = ref([]);
const list = ref([]);
const loading = ref(false);
const slots = ['08:00-10:00', '10:00-12:00', '14:00-16:00', '16:00-18:00', '19:00-21:00'];
const book = reactive({ courseId: null, reserveDate: '', timeSlot: '10:00-12:00' });

const statusMap = { pending: '待确认', confirmed: '已确认', cancelled: '已取消' };
const statusTypeMap = { pending: 'warning', confirmed: 'success', cancelled: 'info' };
const statusText = (s) => statusMap[s] || s;
const statusType = (s) => statusTypeMap[s] || 'info';

async function load() {
  loading.value = true;
  try {
    const res = await http.get('/reservations');
    list.value = res.list;
  } catch (e) { /* 拦截器已提示 */ } finally {
    loading.value = false;
  }
}

async function loadCourses() {
  try {
    const res = await http.get('/courses');
    courses.value = res.list;
  } catch (e) { /* 拦截器已提示 */ }
}

async function bookReserve() {
  if (!book.courseId || !book.reserveDate) return ElMessage.warning('请选择课程和日期');
  try {
    await http.post('/reservations', { ...book });
    ElMessage.success('预约成功，等待确认');
    load();
  } catch (e) { /* 拦截器已提示 */ }
}

async function setStatus(row, status) {
  try {
    await http.put(`/reservations/${row.id}/status`, { status });
    ElMessage.success('已更新');
    load();
  } catch (e) { /* 拦截器已提示 */ }
}

async function cancel(row) {
  try {
    await http.post(`/reservations/${row.id}/cancel`);
    ElMessage.success('已取消');
    load();
  } catch (e) { /* 拦截器已提示 */ }
}

onMounted(() => { loadCourses(); load(); });
</script>
