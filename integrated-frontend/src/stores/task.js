import { defineStore } from "pinia";
import { getItems, postItem, putItem, deleteItem } from "../utils/fetch";
import { useRouter } from "vue-router";

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
    async fetchAllTasks(boardId) {
      const router = useRouter();
      try {
        const data = await getItems(
          `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/tasks?sortBy=${this.sortBy}&filterStatuses=${this.filterStatuses.join(",")}`
        );
        this.tasks = data || [];
      } catch (error) {
        if (error.response?.status === 401) {
          router.push({ name: 'login' }); // Redirect to login page
        } else {
          console.error("Error fetching tasks:", error);
          throw error;
        }
      }
    },
    
    async addTask(boardId, newTask) {
      const router = useRouter();
      try {
        const formattedTask = {
          title: newTask.title.trim(),
          description: newTask.description.trim(),
          assignees: newTask.assignees.trim(),
          status: newTask.status
        };
        const createdTask = await postItem(
          `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/tasks`,
          formattedTask
        );
        this.tasks.push(createdTask);
      } catch (error) {
        if (error.response?.status === 401) {
          router.push({ name: 'login' }); // Redirect to login page
        } else {
          console.error("Error adding task:", error);
          throw error;
        }
      }
    },
    
    async updateTask(boardId, id, updatedTask) {
      const router = useRouter();
      try {
        const formattedTask = {
          title: updatedTask.title.trim(),
          description: updatedTask.description.trim(),
          assignees: updatedTask.assignees.trim(),
          status: updatedTask.status.id
        };
        const updatedData = await putItem(
          `${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/tasks/${id}`,
          formattedTask
        );
        const index = this.tasks.findIndex((task) => task.id === id);
        if (index !== -1) {
          this.tasks.splice(index, 1, updatedData);
        }
      } catch (error) {
        if (error.response?.status === 401) {
          router.push({ name: 'login' }); // Redirect to login page
        } else {
          console.error("Error updating task:", error);
          throw error;
        }
      }
    },
    
    async deleteTask(boardId, id) {
      const router = useRouter();
      try {
        const response = await deleteItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}/tasks/${id}`);
        this.tasks = this.tasks.filter((task) => task.id !== id);
        return response;
      } catch (error) {
        if (error.response?.status === 401) {
          router.push({ name: 'login' }); // Redirect to login page
        } else {
          console.error("Error deleting task:", error);
          throw error;
        }
      }
    },
    
    setSortBy(sortBy, boardId) {
      this.sortBy = sortBy;
      this.fetchAllTasks(boardId);
    },
    
    setFilterStatuses(filterStatuses, boardId) {
      this.filterStatuses = filterStatuses;
      this.fetchAllTasks(boardId);
    }
  }
});
