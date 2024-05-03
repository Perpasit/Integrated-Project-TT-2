import { defineStore } from 'pinia';
import { getItems, postItem, putItem, deleteItem } from '../utils/fetch';

export const useTaskStore = defineStore('task', {
 state: () => ({
   tasks: [],
 }),
 actions: {
   async fetchAllTasks() {
     try {
       const data = await getItems(`${import.meta.env.VITE_API_ENDPOINT}/tasks`);
       this.tasks = data || [];
     } catch (error) {
       console.error('Error fetching tasks:', error);
     }
   },
   
   async fetchTaskById(id) {
     try {
       const task = await getItems(`${import.meta.env.VITE_API_ENDPOINT}/tasks/${id}`);
       return task;
     } catch (error) {
       console.error('Error fetching task:', error);
       throw new Error('Task not found');
     }
   },
   async addTask(newTask) {
    try {
      const createdTask = await postItem(`${import.meta.env.VITE_API_ENDPOINT}/tasks`, newTask);
      this.tasks.push(createdTask);
    } catch (error) {
      console.error('Error adding task:', error);
    }
  },
   async updateTask(id, updatedTask) {
     try {
       const updatedData = await putItem(`${import.meta.env.VITE_API_ENDPOINT}/tasks/${id}`, updatedTask);
       const index = this.tasks.findIndex(task => task.id === id);
       if (index !== -1) {
         this.tasks.splice(index, 1, updatedData);
       }
     } catch (error) {
       console.error('Error updating task:', error);
     }
   },
   async deleteTask(id) {
     try {
       await deleteItem(`${import.meta.env.VITE_API_ENDPOINT}/tasks/${id}`);
       this.tasks = this.tasks.filter(task => task.id !== id);
     } catch (error) {
       console.error('Error deleting task:', error);
     }
   },
   getStatusClass(status) {
     switch (status) {
       case "No Status":
         return "bg-gray-300 text-white rounded-md px-3 py-1";
       case "To Do":
         return "bg-indigo-500 text-white rounded-md px-3 py-1";
       case "Doing":
         return " bg-amber-400 text-white rounded-md px-3 py-1";
       case "Done":
         return "bg-green-400 text-white rounded-md px-3 py-1";
       default:
         return "bg-violet-400 text-white rounded-md px-3 py-1";
     }
   },
 },
 getters: {
   getTasks(state) {
     return state.tasks;
   },
 },
});