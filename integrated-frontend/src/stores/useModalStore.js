import { defineStore } from "pinia";

export const useModalStore = defineStore('modalStore', {
  state: () => ({
    showSuccessModal: false,
    taskTitle: '',
    mode: '',
    entityType: '',
  }),
  actions: {
    openSuccessModal(title, mode, entityType = 'task') {
      this.showSuccessModal = true;
      this.taskTitle = title;
      this.mode = mode;
      this.entityType = entityType;
      setTimeout(() => {
        this.closeSuccessModal()
      }, 3000)
    },
    closeSuccessModal() {
      this.showSuccessModal = false;
      this.taskTitle = '';
      this.mode = '';
      this.entityType = '';
    },
  },
});