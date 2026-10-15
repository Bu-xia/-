import { createRouter, createWebHistory } from 'vue-router';

// 按角色划分的路由：/student 学生端、/admin 管理端
const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  {
    path: '/student',
    component: () => import('../views/StudentLayout.vue'),
    redirect: '/student/home',
    meta: { role: 'student' },
    children: [
      { path: 'home', name: 'student-home', component: () => import('../views/StudentHome.vue'), meta: { title: '选课中心' } },
      { path: 'my-courses', name: 'my-courses', component: () => import('../views/MyCourses.vue'), meta: { title: '我的课程' } },
      { path: 'assistant', name: 'student-assistant', component: () => import('../views/AiAssistant.vue'), meta: { title: 'AI 助教' } },
      { path: 'resources', name: 'student-resources', component: () => import('../views/Resources.vue'), meta: { title: '课程资料库' } },
      { path: 'reservations', name: 'student-reservations', component: () => import('../views/Reservations.vue'), meta: { title: '课程预约' } }
    ]
  },
  {
    path: '/admin',
    component: () => import('../views/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { role: 'admin' },
    children: [
      { path: 'dashboard', name: 'admin-dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '管理后台' } },
      { path: 'courses', name: 'admin-courses', component: () => import('../views/Courses.vue'), meta: { title: '课程管理' } },
      { path: 'resources', name: 'admin-resources', component: () => import('../views/Resources.vue'), meta: { title: '资料库管理' } },
      { path: 'reservations', name: 'admin-reservations', component: () => import('../views/Reservations.vue'), meta: { title: '预约管理' } },
      { path: 'assistant', name: 'admin-assistant', component: () => import('../views/AiAssistant.vue'), meta: { title: 'AI 助教' } }
    ]
  },
  { path: '/', redirect: '/student/home' }
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, from, next) => {
  // 登录校验 + 角色越权拦截（学生不能进管理端，反之亦然）
  const token = localStorage.getItem('sp_token');
  const user = JSON.parse(localStorage.getItem('sp_user') || 'null');
  if (to.path !== '/login' && !token) return next('/login');
  if (to.path === '/login' && token) {
    return next(user && user.role === 'admin' ? '/admin/dashboard' : '/student/home');
  }
  if (to.meta.role && user && to.meta.role !== user.role) {
    return next(user.role === 'admin' ? '/admin/dashboard' : '/student/home');
  }
  next();
});

export default router;
