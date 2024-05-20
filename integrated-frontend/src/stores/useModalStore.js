import { defineStore } from "pinia";

export const useModalStore = defineStore('modalStore', {
  state: () => ({
    showTaskSuccessModal: false,
    showStatusSuccessModal: false,
    showStatusFullModal: false,
    statusFullModalMessage: "",
    taskTitle: '',
    statusName: '',
    mode: '',
    taskSuccessTimeout: null,
    statusSuccessTimeout: null,
  }),
  actions: {
    openTaskSuccessModal(title, mode) {
      this.showTaskSuccessModal = true;
      this.taskTitle = title;
      this.mode = mode;
      this.taskSuccessTimeout = setTimeout(() => {
        this.closeTaskSuccessModal()
      }, 3000)
    },
    closeTaskSuccessModal() {
      this.showTaskSuccessModal = false;
      this.taskTitle = '';
      this.mode = '';
      clearTimeout(this.taskSuccessTimeout);
    },
    openStatusSuccessModal(name, mode) {
      this.showStatusSuccessModal = true;
      this.statusName = name;
      this.mode = mode;
      this.statusSuccessTimeout = setTimeout(() => {
        this.closeStatusSuccessModal()
      }, 3000)
    },
    closeStatusSuccessModal() {
      this.showStatusSuccessModal = false;
      this.statusName = '';
      this.mode = '';
      clearTimeout(this.statusSuccessTimeout);
    },
    openStatusFullModal(message) {
      this.showStatusFullModal = true;
      this.statusFullModalMessage = message;
    },
    closeStatusFullModal() {
      this.showStatusFullModal = false;
      this.statusFullModalMessage = "";
    }
  },
});