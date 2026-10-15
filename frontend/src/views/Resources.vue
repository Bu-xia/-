<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="标题 / 标签 / 描述" clearable style="width: 220px" @keyup.enter="load" />
      <el-input v-model="query.category" placeholder="分类" clearable style="width: 140px" />
      <el-select v-model="query.courseId" placeholder="按课程筛选" clearable style="width: 180px">
        <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="load">检索</el-button>
      <div style="flex:1"></div>
      <el-button type="primary" @click="openUpload">上传资料</el-button>
    </div>

    <el-table :data="list" v-loading="loading" style="margin-top:12px">
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="tags" label="标签" min-width="140" />
      <el-table-column prop="fileType" label="类型" width="80" />
      <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="上传时间" width="170">
        <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.fileUrl" link type="primary" @click="openFile(row.fileUrl)">查看</el-button>
          <el-button v-if="auth.isAdmin" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="上传资料" width="560px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" placeholder="如：课件 / 讲义 / 题库" /></el-form-item>
        <el-form-item label="所属课程">
          <el-select v-model="form.courseId" clearable style="width:100%">
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签"><el-input v-model="form.tags" placeholder="逗号分隔" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="文件">
          <el-upload :auto-upload="false" :limit="1" :on-change="onFileChange" :on-remove="onFileRemove">
            <el-button>选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">上传</el-button>
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
const courses = ref([]);
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const query = reactive({ keyword: '', category: '', courseId: null });
const form = reactive({ title: '', category: '', courseId: null, tags: '', description: '', file: null });

function fmt(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : '';
}

function onFileChange(file) { form.file = file.raw; }
function onFileRemove() { form.file = null; }

async function load() {
  loading.value = true;
  try {
    const params = { ...query };
    Object.keys(params).forEach((k) => { if (params[k] === '' || params[k] == null) delete params[k]; });
    const res = await http.get('/resources', { params });
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

function openUpload() {
  Object.assign(form, { title: '', category: '', courseId: null, tags: '', description: '', file: null });
  dialogVisible.value = true;
}

async function save() {
  if (!form.title) return ElMessage.warning('请填写标题');
  const fd = new FormData();
  fd.append('title', form.title);
  if (form.category) fd.append('category', form.category);
  if (form.courseId) fd.append('courseId', form.courseId);
  if (form.tags) fd.append('tags', form.tags);
  if (form.description) fd.append('description', form.description);
  if (form.file) fd.append('file', form.file);
  saving.value = true;
  try {
    await http.post('/resources', fd, { headers: { 'Content-Type': 'multipart/form-data' } });
    ElMessage.success('上传成功');
    dialogVisible.value = false;
    load();
  } catch (e) { /* 拦截器已提示 */ } finally {
    saving.value = false;
  }
}

function openFile(url) {
  window.open(url, '_blank');
}

async function remove(row) {
  try {
    await http.delete(`/resources/${row.id}`);
    ElMessage.success('已删除');
    load();
  } catch (e) { /* 拦截器已提示 */ }
}

onMounted(() => { loadCourses(); load(); });
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
</style>
