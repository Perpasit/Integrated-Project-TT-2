import { defineStore } from "pinia"
import { fetchWithAuthCheck } from "../utils/fetch" // assuming this function is already in place

export const useAttachFileStore = defineStore("attachFileStore", {
  state: () => ({
    selectedFiles: [], // to store the files selected by the user
    uploadStatus: null, // to track the upload status (success, error)
    attachments: [] // to store the fetched attachments
  }),

  actions: {
    // Action to select files
    setSelectedFiles(files) {
      this.selectedFiles = Array.from(files)
    },

    // Action to upload files
    async uploadFiles(boardId, taskId) {
      if (this.selectedFiles.length === 0) {
        this.uploadStatus = "No files selected."
        return
      }

      const formData = new FormData()
      this.selectedFiles.forEach((file) => {
        formData.append("files", file)
      })

      try {
        // Call the backend API to upload the files
        const response = await fetchWithAuthCheck(
          `${
            import.meta.env.VITE_API_ENDPOINTV3
          }/boards/${boardId}/tasks/${taskId}/attachments`,
          {
            method: "PATCH",
            body: formData
          }
        )

        // Check if the upload was successful
        if (response.status === 201) {
          this.uploadStatus = "Files uploaded successfully!"
          return response
        } else {
          this.uploadStatus = `Failed to upload files. Status: ${response.status}`
          return response
        }
      } catch (error) {
        this.uploadStatus = "An error occurred during upload."
        console.error("Upload error:", error)
      }
    },

    async getAttachments(boardId, taskId) {
      const url = `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/tasks/${taskId}/attachments`
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
    }
  }
})
