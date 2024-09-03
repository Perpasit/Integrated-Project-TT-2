import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    accountName: '',
    isAuthenticated: false,
  }),

  getters: {
    getUserInfo: (state) => state.userInfo,
    getAccountName: (state) => state.accountName,
    getIsAuthenticated: (state) => state.isAuthenticated,
  },

  actions: {
    setUserInfo(info) {
      this.userInfo = info;
      this.isAuthenticated = true;
    },

    setAccountName(name) {
      this.accountName = name;
    },

    async fetchAndSetAccountName(username) {
      try {
        // Simulating an API call to fetch the account name
        // Replace this with an actual API call if needed
        const response = await fetch(`/api/users/${username}`);
        const data = await response.json();
        this.setAccountName(data.accountName);
      } catch (error) {
        console.error('Error fetching account name:', error);
      }
    },

    clearUserInfo() {
      this.userInfo = null;
      this.accountName = '';
      this.isAuthenticated = false;
    },

    logout() {
      this.clearUserInfo();
      // Additional logout logic (e.g., clearing tokens, redirecting) can be added here
    },
  },

  persist: {
    enabled: true,
    strategies: [
      {
        key: 'user',
        storage: localStorage,
      },
    ],
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
