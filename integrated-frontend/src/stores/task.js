// task.js
import { defineStore } from 'pinia'
import { getItems, postItem, putItem, deleteItem } from '../utils/fetch'

export const useTaskStore = defineStore('task', {
  state: () => ({
    tasks: [],
  }),

  actions: {
//////////////////
    async getTask(id) {
      const response = await fetch(`/api/tasks/${id}`)
      if (response.status === 404) {
        throw new Error("The requested task does not exist")
      }
      const data = await response.json()
      return data
    },


    async fetchAllTasks() {
      try {
        const data = await getItems(`${import.meta.env.VITE_API_ENDPOINT}/tasks`)
        this.tasks = data || []
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

    async updateTask(id, updatedTask) {
      const response = await fetch(`/api/tasks/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(updatedTask)
      })
      if (response.status === 404) {
        throw new Error("The requested task does not exist")
      }
      const data = await response.json()
      return data
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
        case "NO STATUS":
          return "bg-gray-300 text-white rounded-md px-3 py-1";
        case "TO DO":
          return "bg-indigo-500	 text-white rounded-md px-3 py-1";
        case "DOING":
          return " bg-amber-400	 text-white rounded-md px-3 py-1";
        case "DONE":
          return "bg-green-400 text-white rounded-md px-3 py-1";
        default:
          return "bg-violet-400	 text-white rounded-md px-3 py-1";
      }
    },
  },

  getters: {
    getTasks(state) {
      return state.tasks
    },
  },
})