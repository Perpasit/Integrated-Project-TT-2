import { defineStore } from "pinia";

export const useModalStore = defineStore('modalStore', {
  state: () => ({
    showSuccessModal: false,
    showStatusFullModal: false,
    statusFullModalMessage: "",
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
    },openStatusFullModal(message) {
      this.showStatusFullModal = true
      this.statusFullModalMessage = message
    },
    closeStatusFullModal() {
      this.showStatusFullModal = false
      this.statusFullModalMessage = ""
    }
  },
});