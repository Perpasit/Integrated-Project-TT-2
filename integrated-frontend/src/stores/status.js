import { defineStore } from "pinia";
import { getItems, postItem, putItem, deleteItem } from "../utils/fetch";

export const useStatusStore = defineStore("status", {
  state: () => ({
    statuses: [],
  }),
  actions: {
    async fetchAllStatuses() {
        try {
          const data = await getItems(`${import.meta.env.VITE_API_ENDPOINT}/statuses`);
          this.statuses = data || [];
          console.log(this.statuses);
        } catch (error) {
          console.error("Error fetching statuses:", error);
        }
      },
    async createStatus(newStatus) {
      try {
        const createdStatus = await postItem(`${import.meta.env.VITE_API_ENDPOINT}/statuses`, newStatus);
        this.statuses.push(createdStatus);
        return createdStatus;
      } catch (error) {
        console.error("Error creating status:", error);
        throw error;
      }
    },
    async updateStatus(id, updatedStatus) {
      try {
        const updatedData = await putItem(`${import.meta.env.VITE_API_ENDPOINT}/statuses/${id}`, updatedStatus);
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
    async deleteStatus(id) {
      try {
        await deleteItem(`${import.meta.env.VITE_API_ENDPOINT}/statuses/${id}`);
        this.statuses = this.statuses.filter((status) => status.id !== id);
      } catch (error) {
        console.error("Error deleting status:", error);
        throw error;
      }
    },
  },
  getters: {
    getStatusById: (state) => (name) => {
        return state.statuses.find((status) => status.name === name);
      },
      getStatusClass: (state) => (statusName) => {
        const status = state.statuses.find((s) => s.name === statusName);
        if (status) {
          return `background-color: ${status.colorCode}; color: white; border-radius: 0.375rem; padding: 0.5rem 0.75rem;`;
        } else {
          return "background-color: #f87171; color: white; border-radius: 0.375rem; padding: 0.5rem 0.75rem;";
        }
      },
  },
});