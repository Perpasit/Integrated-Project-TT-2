<!-- Header.vue -->
<script setup>
import { ref, computed } from "vue"
import AddEditTask from "./AddEditTask.vue"
import { useRouter } from "vue-router"

const router = useRouter()
const emits = defineEmits(["openModal"])
const showAddModal = ref(false)

const openModal = async () => {
  try {
    const newTaskId = await generateNewTaskId()
    router.push({ name: "AddTask", params: { id: newTaskId } })
  } catch (error) {
    console.error("Error occurred while navigating to add task:", error)
  }
}

const generateNewTaskId = async () => {
  try {
    const response = await fetch(`${import.meta.env.VITE_API_ENDPOINT}/tasks`)
    const tasks = await response.json()
    const taskCount = tasks.length
    const tasksData = tasks[taskCount - 1]
    const newTaskNumber = parseInt(tasksData.id) + 1
    return `${newTaskNumber}`
  } catch (error) {
    console.error("Error fetching tasks:", error)
    return "1"
  }
}

const closeModal = () => {
  showAddModal.value = false
}

const goHome = () => {
  router.push({ name: 'home' })
}

const goToManageStatus = () => {
  router.push({ name: 'ManageStatus' })
}

const isManageStatusPage = computed(() => {
  return router.currentRoute.value.name === 'ManageStatus'
})

const addStatus = () => {
  emits('addStatus')
}
</script>

<template>
  <div class="container mx-auto px-0 pt-5 flex flex-row justify-around items-center">
    <h1 class="text-5xl font-bold font-italiana pt-2 text-lime-100 mr-96">Kanban</h1>
    <div class="flex items-center space-x-4">
      <span class="text-lg block cursor-pointer py-2 px-3 text-blue-700 hover:text-white rounded"
        @click="goHome">Home</span>
        
      <span
        :class="['text-lg block cursor-pointer py-2 px-3 rounded', isManageStatusPage ? 'text-white ' : 'text-blue-700 hover:text-white']"
        @click="goToManageStatus">Manage Status</span>

      <button v-if="!isManageStatusPage" @click="openModal"
        class="itbkk-button-add ml-4 bg-transparent hover:bg-gradient-to-r from-cyan-500 to-blue-500 text-blue-700 font-semibold hover:text-white py-3 px-5 border border-blue-500 shadow-lg shadow-blue-500/50 hover:border-transparent rounded">
        + NEW TASK
      </button>

      <button v-else @click="addStatus"
        class="itbkk-button-add ml-4 bg-transparent hover:bg-gradient-to-r from-orange-300 to-amber-300 text-orange-700 font-semibold hover:text-white py-3 px-5 border border-orange-500 shadow-lg shadow-amber-500 hover:border-transparent rounded">
        ADD STATUS
      </button>
    </div>
  </div>
  <AddEditTask v-if="showAddModal" :show="showAddModal" mode="add" @close="closeModal" />
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Italiana&display=swap");

.font-italiana {
  font-family: "Italiana", serif;
}
</style>