import { defineStore } from 'pinia';
import { getItems } from '../utils/fetch'; 

export const useUserStore = defineStore('user', {
  state: () => ({
    accountName: localStorage.getItem('accountName') || '', 
  }),
  actions: {
    setAccountName(name) {
      this.accountName = name;
      localStorage.setItem('accountName', name); 
    },

    async fetchAndSetAccountName(username) {
      try {
        const userDetails = await getItems(`http://localhost:8080/api/users/${username}`);
        if (userDetails) {
          this.setAccountName(userDetails); 
        }
      } catch (error) {
        console.error("Failed to fetch user details:", error);
        throw error; 
      }
    },

    reset() {
      localStorage.removeItem('accountName')
    },
  },
});
