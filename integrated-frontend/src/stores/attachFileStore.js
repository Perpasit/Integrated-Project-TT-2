import { defineStore } from "pinia";
import { useUserStore } from "../stores/userStore"; // assuming you're using Pinia for state management
import { fetchWithAuthCheck } from "../utils/fetch"; // assuming this function is already in place

export const useAttachFileStore = defineStore("attachFileStore", {
  state: () => ({
    selectedFiles: [], // to store the files selected by the user
    uploadStatus: null, // to track the upload status (success, error)
    attachments: [], // to store the fetched attachments
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
      this.selectedFiles.forEach((file) => {
        formData.append("files", file);
      });

      try {
        // Call the backend API to upload the files
        const response = await fetchWithAuthCheck(
          `${
            import.meta.env.VITE_API_ENDPOINTV3
          }/boards/${boardId}/tasks/${taskId}/attachments`,
          {
            method: "PATCH",
            body: formData,
          }
        );

        // Check if the upload was successful
        if (response.status === 201) {
          this.uploadStatus = "Files uploaded successfully!";
          return response;
        } else {
          this.uploadStatus = `Failed to upload files. Status: ${response.status}`;
          return response;
        }
      } catch (error) {
        this.uploadStatus = "An error occurred during upload.";
        console.error("Upload error:", error);
      }
    },

    // Action to fetch attachments
    async getAttachments(boardId, taskId) {
      try {
        // Fetch attachments from the backend API using fetchWithAuthCheck
        const response = await fetchWithAuthCheck(
          `${
            import.meta.env.VITE_API_ENDPOINTV3
          }/boards/${boardId}/tasks/${taskId}/attachments`,
          {
            method: "GET", // Use GET method to fetch data
          }
        );

        // Check if the response is successful
        if (response.status === 200) {
          const contentType = response.headers.get("Content-Type");
          if (contentType && contentType.includes("application/json")) {
            const attachmentsData = await response.json();
            this.attachments = attachmentsData.attachments || []; // Store attachments in the state
            console.log("Fetched Attachments:", this.attachments);
          } else {
            console.error("Invalid response format. Expected JSON.");
          }
        } else {
          console.error(`Failed to fetch attachments. Status: ${response.status}`);
        }
      } catch (error) {
        console.error("Error fetching attachments:", error);
      }
    },

    // Action to remove a file from the selected files list
    removeFile(index) {
      // Validate the index before attempting to remove the file
      if (index >= 0 && index < this.selectedFiles.length) {
        this.selectedFiles.splice(index, 1); // Remove the file at the given index
        console.log(`File at index ${index} removed.`);
      } else {
        console.error("Invalid file index.");
      }
    },
  },
});
