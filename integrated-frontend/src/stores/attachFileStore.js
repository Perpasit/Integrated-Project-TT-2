import { defineStore } from "pinia"
import { fetchWithAuthCheck } from "../utils/fetch" // assuming this function is already in place

export const useAttachFileStore = defineStore("attachFileStore", {
  state: () => ({
    selectedFiles: [], 
    uploadStatus: null, 
    attachments: [] 
  }),

  actions: {
    setSelectedFiles(files) {
      this.selectedFiles = Array.from(files)
      
    },
    clearSelectedFiles() {
      this.selectedFiles = [];
    },
    deleteAttachmentF(attachmentId) {
      this.attachments = this.attachments.filter(
        (attachment) => attachment.id !== attachmentId
      );
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

    removeFile(index) {
      if (index >= 0 && index < this.selectedFiles.length) {
        this.selectedFiles.splice(index, 1)
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
        if (response) {
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
    },

     // Download an attachment
    async downloadAttachment(boardId, taskId, attachmentId) {
      const url = `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/tasks/${taskId}/attachments/${attachmentId}/download`;
      console.log("Fetching file with URL:", url);

      try {
        const response = await fetchWithAuthCheck(url, { method: "GET" });
        if (!response.ok) {
          console.error("Failed to fetch the file:", response.status, response.statusText);
          return false;
        }

        const blob = await response.blob();
        const contentDisposition = response.headers.get("content-disposition");

        // Extract the filename from the Content-Disposition header if available
        let fileName = "downloaded_file";
        if (contentDisposition && contentDisposition.includes("filename=")) {
          fileName = contentDisposition.split("filename=")[1].replace(/"/g, "").trim();
        }

        // Trigger file download
        const urlObject = window.URL.createObjectURL(blob);
        const anchor = document.createElement("a");
        anchor.href = urlObject;
        anchor.download = fileName;
        document.body.appendChild(anchor);
        anchor.click();

        // Clean up
        document.body.removeChild(anchor);
        window.URL.revokeObjectURL(urlObject);

        console.log("File downloaded successfully:", fileName);
        return true;
      } catch (error) {
        console.error("An error occurred during file download:", error);
        return false;
      }
    },
    

    async loadFileDisplay(boardId, taskId, attachmentId) {
      const url = `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/tasks/${taskId}/attachments/${attachmentId}/download`;

      try {
        const response = await fetchWithAuthCheck(url, { method: "GET" });
        console.log(response)
          
          const contentType = response.headers.get('Content-Type')
          const arrayBuffer = await response.arrayBuffer()

          let binary = ''
          const bytes = new Uint8Array(arrayBuffer)
          bytes.forEach((byte) => {
              binary += String.fromCharCode(byte)
          })
          const base64String = window.btoa(binary)

          const base64URL = `data:${contentType};base64,${base64String}`
          // const fileWindow = window.open(base64URL, "_blank")
          // if (fileWindow) {
          //     fileWindow.document.title = filename
          //     if (contentType.includes("image")) {
          //         fileWindow.document.body.innerHTML = `<img src="${base64URL}" alt="${filename}" />`
          //     } else if (contentType.includes("pdf")) {
          //         fileWindow.document.body.innerHTML = `<embed src="${base64URL}" width="100%" height="100%" type="application/pdf" />`
          //     } else {
          //         fileWindow.document.body.innerHTML = `<a href="${base64URL}" download="${filename}">Download ${filename}</a>`
          //     }
          // }

          return base64URL
      } catch (error) {
          console.error(`Error fetching attachment by filename: ${error}`)
      }
    }
  }
})
