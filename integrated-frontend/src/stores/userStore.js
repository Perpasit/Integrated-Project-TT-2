import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    accountName: '',
  }),
  actions: {
    setAccountName(name) {
      this.accountName = name;
    },
  },
});
