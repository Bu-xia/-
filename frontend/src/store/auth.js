import { defineStore } from 'pinia';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('sp_token') || '',
    user: JSON.parse(localStorage.getItem('sp_user') || 'null')
  }),
  getters: {
    isLogin: (s) => !!s.token,
    isAdmin: (s) => s.user && s.user.role === 'admin'
  },
  actions: {
    setAuth(token, user) {
      this.token = token;
      this.user = user;
      localStorage.setItem('sp_token', token);
      localStorage.setItem('sp_user', JSON.stringify(user));
    },
    logout() {
      this.token = '';
      this.user = null;
      localStorage.removeItem('sp_token');
      localStorage.removeItem('sp_user');
    }
  }
});
