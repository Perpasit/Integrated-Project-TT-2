import { defineStore } from "pinia";
import { getItems, postItem, putItem, patchItem, deleteItem } from "../utils/fetch";
export const useCollabStore = defineStore("collab", {
  state: () => ({
    collabs: [],
  }),
  
  actions: {
    async fetchCollaborators(boardId) {
      try {
        const response = await getItems(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs`);
        this.collabs = response.data; 
      } catch (error) {
        console.error("Error fetching collaborators:", error);
      }
    },

    // Fetch a specific collaborator by ID
    async fetchCollaboratorById(boardId, collabOid) {
      try {
        const response = await getItems(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs/${collabOid}`);
        return response.data;
      } catch (error) {
        console.error("Error fetching collaborator:", error);
        throw error;
      }
    },
       
    // Add a new collaborator to a board
    async addCollaborator(boardId, collaboratorData) {
      try {
        const response = await postItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs`, collaboratorData);
        this.collabs.push(response.data);
      } catch (error) {
        console.error("Error adding collaborator:", error);
      }
    },

    async updateCollaborator(boardId, collabOid, newAccessRight) {
      try {
        const response = await patchItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs/${collabOid}`, {
          accessRight: newAccessRight
        });
        const index = this.collabs.findIndex(c => c.oid === collabOid);
        if (index !== -1) {
          this.collabs[index] = response.data;
        }
      } catch (error) {
        console.error("Error updating collaborator:", error);
      }
    },

    // Remove a collaborator from a board
    async removeCollaborator(boardId, collabOid) {
      try {
        const response = await deleteItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/collabs/${collabOid}`);
        // Remove the collaborator from the state
        this.collabs = this.collabs.filter(c => c.oid !== collabOid);
      } catch (error) {
        console.error("Error removing collaborator:", error);
      }
    },
  },
});
