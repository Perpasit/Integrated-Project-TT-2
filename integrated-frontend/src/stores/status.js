import { defineStore } from "pinia";
import { useUserStore } from './userStore';
import { getItems, postItem, putItem, deleteItem,getItemsNoUser } from "../utils/fetch";

export const useStatusStore = defineStore("status", {
  state: () => ({
    statuses: []
  }),
  getters: {
    getStatuses: (state) => state.statuses,
    getStatusColorByName: (state) => (statusName) => {
      const status = state.statuses.find((s) => s.name === statusName);
      return status ? status.colorCode : null;
    },
    getStatusNameById: (state) => (id) => {
      const status = state.statuses.find((status) => status.id === parseInt(id));
      return status ? status.name : null;
    },
    getStatusByName: (state) => (name) => {
      return state.statuses.find((status) => status.name === name);
    },
    getStatusById: (state) => (id) => {
      return state.statuses.find((status) => status.id === parseInt(id));
    },
    getStatusClass: (state) => (statusName) => {
      const status = state.statuses.find((s) => s.name === statusName);
      if (status) {
        return `background-color: ${status.colorCode}; color: white; border-radius: 0.375rem; padding: 0.5rem 0.75rem;`;
      } else {
        return "background-color: #f87171; color: white; border-radius: 0.375rem; padding: 0.5rem 0.75rem;";
      }
    }
  },

  actions: {
    setStatuses(newStatuses) {
      this.statuses = newStatuses;
    },
    async fetchAllStatuses(boardId) {
      const userStore = useUserStore()
      try {
        let data;
        if (userStore.getIsAuthenticated){
          data = await getItems(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/statuses`);

        } else{
       data = await getItemsNoUser(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/statuses`);

        }
        this.statuses = data || [];
      } catch (error) {
        console.error("Error fetching statuses:", error);
      }
    },

    async deleteStatusTransfer(boardId, id, newId) {
      try {
        const deletedStatus = await deleteItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/statuses/${id}/${newId}`);
        this.statuses = this.statuses.filter((status) => status.id !== id);
        return deletedStatus;
      } catch (error) {
        console.error("Error deleting and transferring status:", error);
        throw error;
      }
    },

    async createStatus(boardId, newStatus) {
      try {
        const formattedStatus = {
          name: newStatus.name,
          description: newStatus.description,
          colorCode: newStatus.colorCode
        };
        const createdStatus = await postItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/statuses`, formattedStatus);
        this.statuses.push(createdStatus);
        return createdStatus;
      } catch (error) {
        console.error("Error creating status:", error);
        throw error;
      }
    },

    async updateStatus(boardId, id, updatedStatus) {
      try {
        const formattedStatus = {
          name: updatedStatus.name,
          description: updatedStatus.description,
          colorCode: updatedStatus.colorCode
        };
        const updatedData = await putItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/statuses/${id}`, formattedStatus);
        const index = this.statuses.findIndex((status) => status.id === id);
        if (index !== -1) {
          this.statuses.splice(index, 1, updatedData);
        }
        return updatedData;
      } catch (error) {
        console.error("Error updating status:", error);
        throw error;
      }
    },

    async deleteStatus(boardId, id) {
      try {
        const response = await getItems(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/statuses/${id}/tasks`);
        
        // if (!response.ok) {
        //   if (response.status === 404) {
        //     return { notFound: true };
        //   } else {
        //     throw new Error('Failed to fetch tasks');
        //   }
        // }
        const tasks = await response
        
        if (tasks.length > 0) {
          return { needsTransfer: true, statusId: id };
        } else {
          const deleteStatus = await deleteItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/statuses/${id}`);
          
          this.statuses = this.statuses.filter((status) => status.id !== id);
          return deleteStatus;
        }
      } catch (error) {
        console.error("Error deleting status:", error);
        throw error;
      }
    }
  }
});
