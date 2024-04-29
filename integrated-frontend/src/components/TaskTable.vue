<script setup>
import { ref, onMounted, computed } from "vue"
import { useTaskStore } from "../stores/task"
import Modal from "./Modal.vue"

const taskStore = useTaskStore()
const tasks = ref([])
const selectedTask = ref(null)
const sortOrder = ref("asc")

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

const formattedStatus = computed(() => {
  return (task) => {
    const status = task.status.toLowerCase()
    let formatted = ""
    let capitalizeNext = true
    for (let i = 0; i < status.length; i++) {
      const char = status[i]
      if (char === "_") {
        formatted += " "
        capitalizeNext = true
      } else {
        formatted += capitalizeNext ? char.toUpperCase() : char
        capitalizeNext = false
      }
    }
    return formatted
  }
})

const sortedTasks = computed(() => {
  return [...tasks.value].sort((a, b) => {
    if (sortOrder.value === "asc") {
      return new Date(a.createdOn) - new Date(b.createdOn)
    } else {
      return new Date(b.createdOn) - new Date(a.createdOn)
    }
  })
})

const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === "asc" ? "desc" : "asc"
}

onMounted(async () => {
  await taskStore.fetchAllTasks()
  tasks.value = taskStore.getTasks
})
</script>
<template>
  <div class="container mx-auto px-4 py-8">
    <div class="flex justify-end mb-4">
      <button class="bg-white text-purple-600 font-semibold py-2 px-4 rounded">
        + NEW TASK
      </button>
    </div>
    <div class="overflow-x-auto">
      <table class="table-fixed w-full max-w-4xl mx-auto border-separate border-spacing-y-2">
        <thead>
          <tr class="bg-purple-100">
            <th class="w-12 px-4 py-2 rounded-l-lg"></th>
            <th
              class="w-1/3 px-4 py-2 cursor-pointer"
              @click="toggleSortOrder('title')"
            >
              Title
            </th>
            <th class="w-1/3 px-4 py-2">Assignees</th>
            <th class="w-1/6 px-4 py-2 rounded-r-lg">Status</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(task, index) in sortedTasks"
            :key="index"
            class="bg-white shadow hover:bg-gray-100 itbkk-item"
          >
            <td class="px-4 py-2 text-center rounded-l-lg">{{ index + 1 }}</td>
            <td class="px-4 py-2">
              <p
                class="truncate cursor-pointer itbkk-title"
                @click="showModal(task)"
              >
                {{ task.title }}
              </p>
            </td>
            <td class="px-4 py-2 text-center itbkk-assignees">
              <span v-if="task.assignees">
                {{ task.assignees.split(",").map((a) => a.trim()).join(", ") }}
              </span>
              <span v-else class="text-gray-400 italic itbkk-assignees">Unassigned</span>
            </td>
            <td class="px-4 py-2 text-center rounded-r-lg">
              <span
                class="inline-block px-2 py-1 rounded-full text-center itbkk-status"
                :class="taskStore.getStatusClass(formattedStatus(task))"
              >
                {{ formattedStatus(task) }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-if="tasks.length <= 0" class="text-center mt-4 text-gray-500">
      No task
    </div>
    <modal
      v-if="selectedTask"
      :task="selectedTask"
      @close="closeModal"
      @save="saveTask"
    />
  </div>
</template>