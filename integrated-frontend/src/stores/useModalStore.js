import { defineStore } from "pinia"

export const useModalStore = defineStore({
  id: "modal",
  state: () => ({
    showSuccessModal: false,
    taskTitle: "",
    mode: ""
  }),
  actions: {
    openSuccessModal(title, mode) {
      this.showSuccessModal = true
      this.taskTitle = title
      this.mode = mode

      setTimeout(() => {
        this.closeSuccessModal()
      }, 3000)
    },
    closeSuccessModal() {
      this.showSuccessModal = false
      this.taskTitle = ""
      this.mode = ""
    }
  }
})
