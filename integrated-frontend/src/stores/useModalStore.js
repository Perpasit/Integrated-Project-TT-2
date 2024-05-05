import { defineStore } from 'pinia';

export const useModalStore = defineStore('modal', {
  state: () => ({
    showSuccessModal: false,
  }),
  actions: {
    openSuccessModal() {
      this.showSuccessModal = true;
      setTimeout(() => {
        this.showSuccessModal = false;
      }, 3000);
    },
    closeSuccessModal() {
      this.showSuccessModal = false;
    },
  },
});