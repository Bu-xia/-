import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';

const http = axios.create({ baseURL: '/api', timeout: 30000 });

http.interceptors.request.use((config) => {
  // 自动给每个请求带上登录 token
  const token = localStorage.getItem('sp_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (res) => res.data,
  (err) => {
    // 401 表示登录失效，清空登录态并跳回登录页；其余统一弹出错误提示
    const status = err.response && err.response.status;
    const msg = (err.response && err.response.data && err.response.data.message) || '请求失败';
    if (status === 401) {
      localStorage.removeItem('sp_token');
      localStorage.removeItem('sp_user');
      router.push('/login');
    }
    ElMessage.error(msg);
    return Promise.reject(err);
  }
);

export default http;
