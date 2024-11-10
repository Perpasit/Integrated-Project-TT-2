import { defineStore } from "pinia"
import {
  getItems,
  postItem,
  putItem,
  patchItem,
  deleteItem,
  getItemsNoUser
} from "../utils/fetch"
import { useUserStore } from './userStore';

export const useCollabStore = defineStore("collab", {
  state: () => ({
    collabs: []
  }),

  getters: {
    getCollaborators: (state) => state.collabs,
    // getCollaboratorByOid: (state) => (collabOid) => {
    //   return state.collabs.find(
    //     (collaborator) => collaborator.oid === collabOid
    //   )
    // },

    // getCollaboratorsByAccessRight: (state) => (accessRight) => {
    //   return state.collabs.filter(
    //     (collaborator) => collaborator.accessRight === accessRight
    //   )
    // },

    // totalCollaborators: (state) => {
    //   return state.collabs.length
    // }
  },
  actions: {
    async fetchCollaborators(boardId) {
      const userStore = useUserStore();
      try {
        let data;
        if (userStore.getIsAuthenticated) {
          data = await getItems(
            `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs`
          );
        } else {
          data = await getItemsNoUser(
            `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs`
          );
        }
        this.collabs = data.collaborators || [];
        

        
        return this.collabs;
      } catch (error) {
        console.error("Error fetching collaborators:", error);
      }
    },
    
    async fetchInvitations(boardId) {
      const userStore = useUserStore();
      try {
        let data;
        if (userStore.getIsAuthenticated) {
          data = await getItems(
            `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs/invitations`
          );
        } else {
          data = await getItemsNoUser(
            `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs/invitations`
          );
        }
        return data.invitations || [];
      } catch (error) {
        console.error("Error fetching invitations:", error);
        return [];
      }
    },
    async fetchInvitationsCheck(boardId) {
      const userStore = useUserStore();
      try {
        let data;
        if (userStore.getIsAuthenticated) {
          data = await getItems(
            `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs/invitations`
          );
        } else {
          data = await getItemsNoUser(
            `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs/invitations`
          );
        }
        return data.invitations || [];
      } catch (error) {
        console.error("Error fetching invitations:", error);
        return [];
      }
    },
    

    // async fetchCollaboratorById(boardId, collabOid) {
    //   try {
    //     const response = await getItems(
    //       `${
    //         import.meta.env.VITE_API_ENDPOINTV3
    //       }/boards/${boardId}/collabs/${collabOid}`
    //     )
    //     return response.data
    //   } catch (error) {
    //     console.error("Error fetching collaborator:", error)
    //     throw error
    //   }
    // },

    // Updated method to add a new collaborator using email
    async addCollaborator(boardId, email, accessRight = "READ") {
      try {
        const collaboratorData = {
          email: email,
          accessRight: accessRight
        }

        const response = await postItem(
          `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs`,
          collaboratorData
        )
        if (response.name != null || undefined) {
          this.collabs.push(response); 
        } 
        return response
      } catch (error) {
        console.error("Error adding collaborator:", error)
      }
    },

    async updateCollaborator(boardId, userId, newAccessRight) {
        try {
          const response = await patchItem(
            `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs/${userId}`, // Use userId here, not email
            {
              accessRight: newAccessRight
            }
          );
          const index = this.collabs.findIndex((c) => c.user_id === userId); // Ensure you're checking against user_id
          if (index !== -1) {
            this.collabs[index] = response.data;
          }
        } catch (error) {
          console.error("Error updating collaborator:", error);
        }
      },
      
      async removeCollaborator(boardId, collabOid) {
        try {
          // console.log("Removing collaborator:", collabOid, "from board:", boardId); // เพิ่ม log ตรวจสอบ
          await deleteItem(
            `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs/${collabOid}`
          );
          // console.log("Collaborator removed successfully");
        } catch (error) {
          console.error("Error removing collaborator:", error); // Log error หากมีปัญหา
        }
      },


    getCollaboratorByBoardAndUser(userOid) {
      // ตรวจสอบว่า collabs เป็น array ก่อนใช้งาน find
      if (!Array.isArray(this.collabs)) {
        console.error("Collaborators data is not available or not an array.");
        return null;
      }
    
      // // แสดงโครงสร้างของแต่ละ collaborator ใน collabs
      // this.collabs.forEach((collaborator, index) => {
      //   console.log(`Collaborator ${index}:`, JSON.stringify(collaborator, null, 2));
      // });
    
      // ใช้ oid แทน userOid ในการค้นหา
      return this.collabs.find(
        (collaborator) => collaborator.oid === userOid // ใช้ฟิลด์ oid สำหรับการตรวจสอบ userOid
      );
    },

    async fetchCollaboratorByCollabOid(boardId, collabOid) {
      try {
        const response = await getItems(
          `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs/${collabOid}`
        );
        return response;
      } catch (error) {
        console.error("Error fetching collaborator by collabOid:", error);
        throw error;
      }
    }
    
    
    
    
  }
})
