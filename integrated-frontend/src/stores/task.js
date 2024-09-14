import { defineStore } from "pinia";
import { getItems, postItem, putItem, deleteItem } from "../utils/fetch";

export const useTaskStore = defineStore("task", {
  state: () => ({
    tasks: [],
    sortBy: "createOn",
    filterStatuses: []
  }),
  getters: {
    getTasks: (state) => state.tasks,
    getTaskById: (state) => (taskId) => state.tasks.find((task) => task.id === taskId)
  },
  actions: {
    getTokenFromLocalStorage() {
      const storedData = JSON.parse(localStorage.getItem('userStore'));
      return storedData?.token || ''; // Safely access the token
    },

    async fetchAllTasks() {
      const token = this.getTokenFromLocalStorage(); // Now properly defined as an action
      console.log("Token for fetchAllTasks:", token); // Log the token

      if (!token) {
        console.error("Token is missing in fetchAllTasks");
        return;
      }

      try {
        const data = await getItems(
          `${import.meta.env.VITE_API_ENDPOINT}/boards/1/tasks?sortBy=${this.sortBy}&filterStatuses=${this.filterStatuses.join(",")}`,
          {
            headers: { Authorization: `Bearer ${token}` } // Include token in the headers
          }
        );
        this.tasks = data || [];
      } catch (error) {
        console.error("Error in fetchAllTasks:", error); // Log the error if any
        throw error;
      }
    },

    async addTask(newTask) {
      const token = this.getTokenFromLocalStorage(); // Get the token from localStorage
      console.log("Token for addTask:", token); // Log the token

      if (!token) {
        console.error("Token is missing in addTask");
        return;
      }

      try {
        const formattedTask = {
          title: newTask.title.trim(),
          description: newTask.description.trim(),
          assignees: newTask.assignees.trim(),
          status: newTask.status
        };
        const createdTask = await postItem(
          `${import.meta.env.VITE_API_ENDPOINT}/tasks`,
          formattedTask,
          {
            headers: { Authorization: `Bearer ${token}` } // Include token in the headers
          }
        );
        this.tasks.push(createdTask);
      } catch (error) {
        console.error("Error in addTask:", error); // Log the error if any
        throw error;
      }
    },

    async updateTask(id, updatedTask) {
      const token = this.getTokenFromLocalStorage(); // Get the token from localStorage
      console.log("Token for updateTask:", token); // Log the token

      if (!token) {
        console.error("Token is missing in updateTask");
        return;
      }

      try {
        const formattedTask = {
          title: updatedTask.title.trim(),
          description: updatedTask.description.trim(),
          assignees: updatedTask.assignees.trim(),
          status: updatedTask.status.id
        };
        const updatedData = await putItem(
          `${import.meta.env.VITE_API_ENDPOINT}/tasks/${id}`,
          formattedTask,
          {
            headers: { Authorization: `Bearer ${token}` } // Pass token in the headers
          }
        );
        const index = this.tasks.findIndex((task) => task.id === id);
        if (index !== -1) {
          this.tasks.splice(index, 1, updatedData);
        }
      } catch (error) {
        console.error("Error in updateTask:", error); // Log the error if any
        throw error;
      }
    },

    async deleteTask(id) {
      const token = this.getTokenFromLocalStorage(); // Get the token from localStorage
      console.log("Token for deleteTask:", token); // Log the token

      if (!token) {
        console.error("Token is missing in deleteTask");
        return;
      }

      try {
        const response = await deleteItem(
          `${import.meta.env.VITE_API_ENDPOINT}/tasks/${id}`,
          {
            headers: { Authorization: `Bearer ${token}` } // Include token in the headers
          }
        );
        if (response.status === 404) {
          throw new Error();
        }
        this.tasks = this.tasks.filter((task) => task.id !== id);
        return response;
      } catch (error) {
        console.error("Error in deleteTask:", error); // Log the error if any
        throw error;
      }
    },

    setSortBy(sortBy) {
      this.sortBy = sortBy;
      this.fetchAllTasks();
    },

    setFilterStatuses(filterStatuses) {
      this.filterStatuses = filterStatuses;
      this.fetchAllTasks();
    }
  }
});
