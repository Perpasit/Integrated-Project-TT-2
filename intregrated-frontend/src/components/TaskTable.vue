<script setup>
import { ref, onMounted } from 'vue'
import { useTaskStore } from '../stores/task'
import Modal from './Modal.vue'

const taskStore = useTaskStore()
const tasks = ref([])
const selectedTask = ref(null)

const showModal = (task) => {
  selectedTask.value = task
}

const closeModal = () => {
  selectedTask.value = null
}

const saveTask = async (updatedTask) => {
  if (updatedTask.id) {
    await taskStore.updateTask(updatedTask.id, updatedTask)
  } else {
    await taskStore.addTask(updatedTask)
  }
  closeModal()
}

onMounted(() => {
  taskStore.fetchAllTasks().then(() => {
    tasks.value = taskStore.getTasks
  })
})
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
          <td class="px-4 py-2 itbkk-title" @click="showModal(task)" style="word-break: break-word; overflow-wrap: break-word">
            <button>{{ task.title }}</button>
          </td>
          <td class="px-4 py-2 itbkk-assignees text-center">
            <span v-if="task.assignees && task.assignees.trim() !== ''">{{ task.assignees }}</span>
            <span v-else class="italic text-gray-400">Unassigned</span>
          </td>
          <td class="px-4 py-2 itbkk-status text-center">
            <span class="inline-block px-2 py-1 rounded-full text-center" :class="taskStore.getStatusClass(task.status)">{{ task.status }}</span>
          </td>
        </tr>
      </tbody>
    </table>
    <modal v-if="selectedTask" :task="selectedTask" @close="closeModal" @save="saveTask" />
  </div>
</template>