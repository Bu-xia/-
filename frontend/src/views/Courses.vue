<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="按课程名 / 教师搜索" clearable style="width: 240px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <div style="flex:1"></div>
      <el-button v-if="auth.isAdmin" type="primary" @click="openDialog()">新增课程</el-button>
    </div>
    <el-table :data="list" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="code" label="课程代码" width="120" />
      <el-table-column prop="name" label="课程名称" min-width="140" />
      <el-table-column prop="teacher" label="教师" width="120" />
      <el-table-column prop="schedule" label="上课时间" width="180" />
      <el-table-column prop="capacity" label="剩余名额" width="100" />
      <el-table-column prop="description" label="简介" min-width="180" />
      <el-table-column v-if="auth.isAdmin" label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="remove(row)">
            <template #reference><el-button link type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="560px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="课程代码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="课程名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="教师"><el-input v-model="form.teacher" /></el-form-item>
        <el-form-item label="上课时间"><el-input v-model="form.schedule" placeholder="如：周一 10:00-12:00" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import http from '../api/http';
import { useAuthStore } from '../store/auth';

const auth = useAuthStore();
const list = ref([]);
const keyword = ref('');
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const form = reactive({ id: null, code: '', name: '', teacher: '', schedule: '', capacity: 30, description: '' });

async function load() {
  loading.value = true;
  try {
    const res = await http.get('/courses', { params: { keyword: keyword.value || undefined } });
    list.value = res.list;
  } catch (e) { /* 拦截器已提示 */ } finally {
    loading.value = false;
  }
}

function openDialog(row) {
  Object.assign(form, { id: null, code: '', name: '', teacher: '', schedule: '', capacity: 30, description: '' });
  if (row) Object.assign(form, row);
  dialogVisible.value = true;
}

async function save() {
  if (!form.name) return ElMessage.warning('请填写课程名称');
  saving.value = true;
  try {
    if (form.id) await http.put(`/courses/${form.id}`, form);
    else await http.post('/courses', form);
    ElMessage.success('已保存');
    dialogVisible.value = false;
    load();
  } catch (e) { /* 拦截器已提示 */ } finally {
    saving.value = false;
  }
}

async function remove(row) {
  try {
    await http.delete(`/courses/${row.id}`);
    ElMessage.success('已删除');
    load();
  } catch (e) { /* 拦截器已提示 */ }
}

onMounted(load);
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; align-items: center; }
</style>
