import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: {
      sub: "",
      role: "",
      iss: "",
      name: "",
      oid: "",
      exp: "",
      iat: "",
      email: "",
    },
    isAuthenticated: false,
    
  }),

  getters: {
    getUserInfo: (state) => state.userInfo,
    getAccountName: (state) => state.userInfo.name,
    getIsAuthenticated: (state) => state.isAuthenticated,
    getToken: (state) => state.token,
  },

  actions: {
    setUserInfo(info) {
      this.userInfo = info;
      this.isAuthenticated = true;
      this.saveToLocalStorage();
    },

   

    logout() {
      this.userInfo = {};
      this.isAuthenticated = false;
      localStorage.removeItem('userStore');
      localStorage.removeItem('isLimitEnabled');

    },

    saveToLocalStorage() {
      localStorage.setItem('userStore', JSON.stringify({
        userInfo: this.userInfo,
        isAuthenticated: this.isAuthenticated,
      
      }));
    },

    loadFromLocalStorage() {
      const storedData = localStorage.getItem('userStore');
      if (storedData) {
        const parsedData = JSON.parse(storedData);
        this.userInfo = parsedData.userInfo;
        this.isAuthenticated = parsedData.isAuthenticated;
    
        if (this.isTokenExpired()) {
          this.logout();
        }
      }
    },
    isTokenExpired() {
      if (!this.userInfo.exp) return true;
      const currentTime = Math.floor(Date.now() / 1000); // Current time in seconds
      return currentTime > this.userInfo.exp;
    },
  },
});


// import { defineStore } from 'pinia';
// import { getItems } from '../utils/fetch'; 

// export const useUserStore = defineStore('user', {
//   state: () => ({
//     accountName: localStorage.getItem('accountName') || '', 
//   }),
//   actions: {
//     setAccountName(name) {
//       this.accountName = name;
//       localStorage.setItem('accountName', name); 
//     },

//     async fetchAndSetAccountName(username) {
//       try {
//         const userDetails = await getItems(`${import.meta.env.VITE_API_ENDPOINT2}/login/${username}`);
//         if (userDetails) {
//           this.setAccountName(userDetails); 
//         }
//       } catch (error) {
//         console.error("Failed to fetch user details:", error);
//         throw error; 
//       }
//     },

//     reset() {
//       localStorage.removeItem('accountName')
//     },
//   },
// });
