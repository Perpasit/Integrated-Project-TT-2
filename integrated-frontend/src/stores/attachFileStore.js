import { defineStore } from "pinia";
import { useUserStore } from "../stores/userStore";  // assuming you're using Pinia for state management
import { fetchWithAuthCheck } from "../utils/fetch";  // assuming this function is already in place

export const useAttachFileStore = defineStore("attachFileStore", {
  state: () => ({
    selectedFiles: [],  // to store the files selected by the user
    uploadStatus: null,  // to track the upload status (success, error)
  }),

  actions: {
    // Action to select files
    setSelectedFiles(files) {
      this.selectedFiles = Array.from(files);
    },

    // Action to upload files
    async uploadFiles(boardId, taskId) {
      if (this.selectedFiles.length === 0) {
        this.uploadStatus = "No files selected.";
        return;
      }

      const formData = new FormData();
      this.selectedFiles.forEach(file => {
        formData.append("files", file);
      });

      try {
        // Call the backend API to upload the files
        const response = await fetchWithAuthCheck(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/tasks/${taskId}/attachments`, {
          method: "PATCH",
          body: formData,
        });

        if (response.status === 201) {
          this.uploadStatus = "Files uploaded successfully!";
          return response;
        } else {
          this.uploadStatus = "Failed to upload files.";
          return response;
        }
      } catch (error) {
        this.uploadStatus = "An error occurred during upload.";
        console.error("Error uploading files:", error);
      }
    },

    // Action to remove a file from the selected files list
    removeFile(index) {
      this.selectedFiles.splice(index, 1);  // Remove the file at the given index
    },
  },
});
