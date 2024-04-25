// task.js
import { defineStore } from 'pinia'
import { getItems, postItem, putItem, deleteItem } from '../utils/fetch'

export const useTaskStore = defineStore('task', {
  state: () => ({
    tasks: [],
  }),

  actions: {
    async fetchAllTasks() {
      try {
        const data = await getItems('/data/data.json') 
        this.tasks = data.tasks || []
      } catch (error) {
        console.error('Error fetching tasks:', error)
      }
    },

    async addTask(newTask) {
      try {
        await postItem('/data/data.json', newTask) 
        await this.fetchAllTasks() 
      } catch (error) {
        console.error('Error adding task:', error)
      }
    },

    async updateTask(taskId, updatedTask) {
      try {
        await putItem('/data/data.json', taskId, updatedTask) 
        await this.fetchAllTasks() 
      } catch (error) {
        console.error('Error updating task:', error)
      }
    },

    async deleteTask(taskId) {
      try {
        await deleteItem('/data/data.json', taskId)
        await this.fetchAllTasks()
      } catch (error) {
        console.error('Error deleting task:', error)
      }
    },

    getStatusClass(status) {
      switch (status) {
        case "No Status":
          return "bg-gray-400 text-white"
        case "To Do":
          return "bg-yellow-400 text-black"
        case "Doing":
          return "bg-blue-400 text-white"
        case "Done":
          return "bg-green-400 text-white"
        default:
          return "bg-gray-400 text-white"
      }
    },
  },

  getters: {
    getTasks(state) {
      return state.tasks
    },
  },
})