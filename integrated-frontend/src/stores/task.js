import { defineStore } from "pinia"
import { getItems, postItem, putItem, deleteItem } from "../utils/fetch"

export const useTaskStore = defineStore("task", {
  state: () => ({
    tasks: []
  }),
  getters: {
    getTasks: (state) => state.tasks,
    getTaskById: (state) => (taskId) =>
      state.tasks.find((task) => task.id === taskId)
  },
  actions: {
    async fetchAllTasks() {
      try {
        const data = await getItems(
          `${import.meta.env.VITE_API_ENDPOINT}/tasks`
        )
        this.tasks = data || []
      } catch (error) {
        console.error("Error fetching tasks:", error)
        throw error
      }
    },
    async addTask(newTask) {
      try {
        const formattedTask = {
          taskTitle: newTask.title.trim(),
          description: newTask.description.trim(),
          assignees: newTask.assignees.trim(),
          status: { id: newTask.status }
        }

        const createdTask = await postItem(
          `${import.meta.env.VITE_API_ENDPOINT}/tasks`,
          formattedTask
        )
        this.tasks.push(createdTask)
      } catch (error) {
        console.error("Error adding task:", error)
        throw error
      }
    },
    async updateTask(id, updatedTask) {
      try {
        const formattedTask = {
          taskTitle: updatedTask.title.trim(),
          taskDescription: updatedTask.description.trim(),
          taskAssignees: updatedTask.assignees.trim(),
          status: { id: updatedTask.status }
        };
    
        const updatedData = await putItem(
          `${import.meta.env.VITE_API_ENDPOINT}/tasks/${id}`,
          formattedTask
        );
    
        const index = this.tasks.findIndex(task => task.id === id);
        if (index !== -1) {
          this.tasks.splice(index, 1, updatedData);
        }
      } catch (error) {
        console.error("Error updating task:", error);
        throw error;
      }
    },    
    async deleteTask(id) {
      try {
        const deleteTask = await deleteItem(
          `${import.meta.env.VITE_API_ENDPOINT}/tasks/${id}`
        )
        this.tasks = this.tasks.filter((task) => task.id !== id)
        return deleteTask
      } catch (error) {
        console.error("Error deleting task:", error)
      }
    }
  }
})
