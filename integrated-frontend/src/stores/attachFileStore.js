import { defineStore } from "pinia"
import { fetchWithAuthCheck } from "../utils/fetch" // assuming this function is already in place

export const useAttachFileStore = defineStore("attachFileStore", {
  state: () => ({
    selectedFiles: [], // to store the files selected by the user
    uploadStatus: null, // to track the upload status (success, error)
    attachments: [] // to store the fetched attachments
  }),

  actions: {
    setSelectedFiles(files) {
      this.selectedFiles = Array.from(files)
    },

    async uploadFiles(boardId, taskId) {
      if (!this.selectedFiles || this.selectedFiles.length === 0) {
        this.uploadStatus = "No files selected."
        console.warn(this.uploadStatus)
        return null
      }

      const formData = new FormData()
      this.selectedFiles.forEach((file) => {
        formData.append("files", file)
      })

      try {
        const url = `${
          import.meta.env.VITE_API_ENDPOINTV3
        }/boards/${boardId}/tasks/${taskId}/attachments`
        const response = await fetchWithAuthCheck(url, {
          method: "PATCH",
          body: formData
        })
      } catch (error) {
        this.uploadStatus = "An error occurred during file upload."
        return false
      }
    },
    async getAttachments(boardId, taskId) {
      const url = `${
        import.meta.env.VITE_API_ENDPOINTV3
      }/boards/${boardId}/tasks/${taskId}/attachments`
      try {
        const response = await fetchWithAuthCheck(url, { method: "GET" })

        if (!response || typeof response.attachments === "undefined") {
          return []
        }

        if (
          Array.isArray(response.attachments) &&
          response.attachments.length > 0
        ) {
          console.log("Fetched Attachments:", response.attachments)
          this.attachments = response.attachments
          return this.attachments
        } else {
          return []
        }
      } catch (error) {
        return []
      }
    },

    // Action to remove a file from the selected files list
    removeFile(index) {
      // Validate the index before attempting to remove the file
      if (index >= 0 && index < this.selectedFiles.length) {
        this.selectedFiles.splice(index, 1) // Remove the file at the given index
        console.log(`File at index ${index} removed.`)
      } else {
        console.error("Invalid file index.")
      }
    },
    async deleteAttachment(boardId, taskId, attachmentId) {
      const url = `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/tasks/${taskId}/attachments/${attachmentId}`;
      
      try {
        const response = await fetchWithAuthCheck(url, {
          method: "DELETE",
        });
    
        // ตรวจสอบ response
        if (response.ok) {
          // อัปเดต attachments ใน state
          this.attachments = this.attachments.filter(
            (attachment) => attachment.id !== attachmentId
          );
          console.log(`Attachment with ID ${attachmentId} deleted.`);
          return true;
        } else {
      
          return false;
        }
      } catch (error) {
        console.error("An error occurred while deleting the attachment:", error);
        return false;
      }
    }
    
  }
})
