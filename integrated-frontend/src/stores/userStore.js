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
    accessToken: '',
    refreshToken: '',  // Add refresh token here
  }),

  getters: {
    getUserInfo: (state) => state.userInfo,
    getAccountName: (state) => state.userInfo.name,
    getIsAuthenticated: (state) => state.isAuthenticated,
    getAccessToken: (state) => state.accessToken,
    getRefreshToken: (state) => state.refreshToken,  // Getter for refresh token
    getUserOid: (state) => state.userInfo.oid,
  },

  actions: {
    setUserInfo(info, accessToken, refreshToken) {
      this.userInfo = info;
      this.accessToken = accessToken;
      this.refreshToken = refreshToken;  // Save the refresh token
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
      this.accessToken = '';
      this.refreshToken = '';  // Clear refresh token
      localStorage.removeItem('userStore');
      localStorage.removeItem('isLimitEnabled');
      router.push({name: "Login"});
    },

    saveToLocalStorage() {
      localStorage.setItem(
        'userStore',
        JSON.stringify({
          userInfo: this.userInfo,
          isAuthenticated: this.isAuthenticated,
          accessToken: this.accessToken,
          refreshToken: this.refreshToken,  // Save refresh token to localStorage
        })
      );
    },

    loadFromLocalStorage() {
      const storedData = localStorage.getItem('userStore');
      if (storedData) {
        const parsedData = JSON.parse(storedData);
        this.userInfo = parsedData.userInfo;
        this.isAuthenticated = parsedData.isAuthenticated;
        this.accessToken = parsedData.accessToken;
        this.refreshToken = parsedData.refreshToken;  // Load refresh token from localStorage
      }
    },
  },
});