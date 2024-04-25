<script>
import { getItems, postItem, putItem, deleteItem } from "../utils/fetch"
import Modal from './Modal.vue';

export default {
  components: {
    Modal
  },
  data() {
    return {
      tasks: [],
      url: "../data/data.json",
      selectedTask: null
    }
  },
  async created() {
    this.tasks = await getItems(this.url)
  },
  methods: {
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
    changeStatus(task) {
      const statusOptions = ['No status', 'Todo', 'Doing', 'Done'];
      const currentIndex = statusOptions.indexOf(task.status);
      const nextIndex = (currentIndex + 1) % statusOptions.length;
      task.status = statusOptions[nextIndex];
    },
    showModal(task) {
      this.selectedTask = task;
    },
    closeModal() {
      this.selectedTask = null;
    },
    saveTask(editedTask) {
      Object.assign(this.selectedTask, editedTask);
      this.closeModal();
    },
    async addTask(task) {
      await postItem(this.url, task)
      this.tasks = await getItems(this.url)
    },
    async updateTask(taskId, updatedTask) {
      await putItem(this.url, taskId, updatedTask)
      this.tasks = await getItems(this.url)
    },
    async deleteTask(taskId) {
      await deleteItem(this.url, taskId)
      this.tasks = await getItems(this.url)
    }
  }
}
</script>

<template>
  <div class="container mx-auto p-4">
    <table class="table-fixed w-full max-w-4xl mx-auto border-collapse">
      <thead>
        <tr class="bg-gray-200">
          <th class="w-12 px-4 py-2 border">#</th>
          <th class="w-1/3 px-4 py-2 border">Title</th>
          <th class="w-1/3 px-4 py-2 border">Assignees</th>
          <th class="w-1/6 px-4 py-2 border">Status</th>
        </tr>
      </thead>


      <tbody>
        <tr v-for="(task, index) in tasks" :key="index" class="border-b hover:bg-gray-100">
          <td class="px-4 py-2 text-center">{{ index + 1 }}</td>
          <td class="px-4 py-2 itbkk-title" @click="showModal(task)"
            style="word-break: break-word; overflow-wrap: break-word">
            {{ task.title }}
          </td>
          <td class="px-4 py-2 itbkk-assignees text-center">
            <span v-if="task.assignees && task.assignees.trim() !== ''">{{
          task.assignees
        }}</span>
            <span v-else class="italic text-gray-400">Unassigned</span>
          </td>
          <td class="px-4 py-2 itbkk-status text-center">
            <span class="inline-block px-2 py-1 rounded-full text-center" :class="getStatusClass(task.status)">{{
          task.status }}</span>
          </td>
        </tr>
      </tbody>
    </table>
    <modal :task="selectedTask" @close="closeModal" @save="saveTask" />
  </div>
</template>
