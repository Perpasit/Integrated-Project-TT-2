import { defineStore } from 'pinia';
import router from '../router';

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: {
      sub: '',
      role: '',
      iss: '',
      name: '',
      oid: '',
      exp: '',
      iat: '',
      email: '',
    },
    isAuthenticated: false,
    token: '',
  }),

  getters: {
    getUserInfo: (state) => state.userInfo,
    getAccountName: (state) => state.userInfo.name,
    getIsAuthenticated: (state) => state.isAuthenticated,
    getToken: (state) => state.token,
  },

  actions: {
    setUserInfo(info, token) {
      this.userInfo = info;
      this.token = token;
      this.isAuthenticated = true;
      this.saveToLocalStorage();
    },

    logout() {
      this.userInfo = {
        sub: '',
        role: '',
        iss: '',
        name: '',
        oid: '',
        exp: '',
        iat: '',
        email: '',
      };
      this.isAuthenticated = false;
      this.token = '';
      localStorage.removeItem('userStore');
      localStorage.removeItem('isLimitEnabled');
      router.push('/login');
    },

    saveToLocalStorage() {
      this.loadFromLocalStorage()
      localStorage.setItem(
        'userStore',
        JSON.stringify({
          userInfo: this.userInfo,
          isAuthenticated: this.isAuthenticated,
          token: this.token,
        })
      );
    },

    loadFromLocalStorage() {
      const storedData = localStorage.getItem('userStore');
      if (storedData) {
        const parsedData = JSON.parse(storedData);
        this.userInfo = parsedData.userInfo;
        this.isAuthenticated = parsedData.isAuthenticated;
        this.token = parsedData.token;
      }
    },

    isTokenExpired() {
      this.loadFromLocalStorage()
      if (!this.userInfo.exp) {
        this.loadFromLocalStorage();
        if (!this.userInfo.exp) return true;
      }
      const currentTime = Math.floor(Date.now() / 1000);
      return currentTime > this.userInfo.exp;
    },

    validateToken() {
      this.loadFromLocalStorage()
      if (!this.token || this.isTokenExpired()) {
        console.log('Token is invalid or expired. Logging out...');
        this.logout();
        return false;
      }
      return true;
    },

    checkAndValidateToken() {
    this.loadFromLocalStorage()
      if (!this.validateToken()) {
        console.log('Token validation failed. Redirecting to login...');
        router.push('/login');
      }
    },
  },
});