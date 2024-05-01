<script setup>
import { ref, computed, watch } from "vue"
import { useTaskStore } from "../stores/task"

const taskStore = useTaskStore()
const showModal = ref(false)
const showSuccessModal = ref(false)
const savedTask = ref(null)

const editedTask = ref({
  title: "",
  description: "",
  assignees: "",
  status: "NO_STATUS"
})

const titleError = ref("")

const isTitleValid = computed(() => {
  return editedTask.value.title.trim() !== ""
})

watch(
  () => editedTask.value.title,
  (newTitle) => {
    if (newTitle.trim() === "") {
      titleError.value = "Title is required"
    } else {
      titleError.value = ""
    }
  }
)

function openModal() {
  resetForm()
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  resetForm()
}

async function saveTask() {
  if (!isTitleValid.value) {
    titleError.value = "Title is required"

    return
  }

  try {
    const response = await fetch(`${import.meta.env.VITE_API_ENDPOINT}/tasks`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(editedTask.value)
    })

    if (response.ok) {
      const data = await response.json()
      console.log("Task saved successfully:", data)

      await taskStore.addTask(data)

      savedTask.value = data
      showSuccessModal.value = true

      setTimeout(() => {
        closeSuccessModal()
        resetForm()
      }, 3000)
    } else {
      console.error("Error saving task:", response.status)
    }
  } catch (error) {
    console.error("Error saving task:", error)
  }
  showModal.value = false
}

function resetForm() {
  editedTask.value = {
    title: "",
    description: "",
    assignees: "",
    status: "NO_STATUS"
  };
  titleError.value = "";
}

function closeSuccessModal() {
  showSuccessModal.value = false
  savedTask.value = null
}
</script>

<template>
  <div class="container mx-auto px-0 pt-5 flex flex-row justify-between">
    <h1 class="text-4xl font-bold font-italiana pt-2 text-lime-100">Kanban</h1>

    <button
      @click="openModal"
      class="itbkk-button-add bg-transparent hover:bg-gradient-to-r from-cyan-500 to-blue-500 text-blue-700 font-semibold hover:text-white py-2 px-4 border border-blue-500 shadow-lg shadow-blue-500/50 hover:border-transparent rounded"
    >
      + NEW TASK
    </button>
  </div>

  <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>

      <div
        class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-4xl sm:w-full mx-auto"
      >
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-6">
          <div class="itbkk-button-add text-lg leading-6 font-medium text-gray-900 mb-4">
            New Task
          </div>

          <div class="sm:flex sm:items-start">
            <div class="mt-3 text-center sm:mt-5 sm:text-left w-full mr-6 ml-6">
              <div>
                <label class="block text-sm font-bold mb-1">Title :</label>
                <textarea
                  rows="2"
                  v-model="editedTask.title"
                  class="itbkk-title shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                ></textarea>
                <p v-if="titleError" class="text-red-500 text-sm mt-1">
                  {{ titleError }}
                </p>
              </div>

              <div class="mt-4">
                <label class="block text-sm font-bold mb-1"
                  >Description :</label
                >
                <textarea
                  rows="10"
                  v-model="editedTask.description"
                  class="itbkk-description shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                  placeholder="No Description Provided"
                ></textarea>
              </div>
            </div>

            <div class="mt-3 text-center sm:mt-2 sm:text-left w-1/2 mr-6 ml-6">
              <div class="mt-2.5 mb-4">
                <label class="block text-sm font-bold mb-1">Assignees :</label>
                <textarea
                  rows="6"
                  v-model="editedTask.assignees"
                  class="itbkk-assignees shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                  placeholder="Unassigned"
                ></textarea>
              </div>

              <div>
                <label class="block text-sm font-bold mb-1">Status :</label>
                <select
                  v-model="editedTask.status"
                  class="itbkk-status shadow-sm p-3 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                >
                  <option value="NO_STATUS">No Status</option>
                  <option value="TO_DO">To Do</option>
                  <option value="DOING">Doing</option>
                  <option value="DONE">Done</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button
            type="button"
            class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
            :class="{
              'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500':
                isTitleValid,
              'bg-gray-300 cursor-not-allowed': !isTitleValid
            }"
            @click="saveTask"
            :disabled="!isTitleValid"
          >
            SAVE
          </button>

          <button
            type="button"
            class="itbkk-button-cancel mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            @click="closeModal"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>

  <div v-if="showSuccessModal" class="fixed z-50 inset-0 overflow-y-auto">
    <div class="flex items-start justify-center min-h-screen">
      <div
        class="bg-white mt-2 rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full"
      >
        <div class="bg-white px-4 sm:p-4 sm:pb-4 py-0">
          <div class="flex justify-between items-center">
            <div class="text-lg leading-6 font-medium text-gray-900">
              Success
            </div>
            <button
              type="button"
              class="text-gray-400 hover:text-gray-500 focus:outline-none focus:text-gray-500 transition ease-in-out duration-150"
              @click="closeSuccessModal"
            >
              <svg
                class="h-6 w-6"
                stroke="currentColor"
                fill="none"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                ></path>
              </svg>
            </button>
          </div>
          <div>
            <p class="text-sm leading-5 text-gray-500 itbkk-message">
              The Task "{{ savedTask.title }}" is added successfully.
            </p>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-1 sm:px-6 sm:flex sm:flex-row-reverse">
          <span class="flex w-full rounded-md shadow-sm sm:ml-3 sm:w-auto">
            <button
              type="button"
              class="inline-flex justify-center w-full rounded-md border border-transparent px-4 py-2 bg-indigo-600 text-base leading-6 font-medium text-white shadow-sm hover:bg-indigo-500 focus:outline-none focus:border-indigo-700 focus:shadow-outline-indigo transition ease-in-out duration-150 sm:text-sm sm:leading-3"
              @click="closeSuccessModal"
            >
              OK
            </button>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Italiana&display=swap");

.font-italiana {
  font-family: "Italiana", serif;
}
</style>
