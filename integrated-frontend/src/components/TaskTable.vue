<script setup>
import { ref, onMounted, computed } from "vue"
import { useTaskStore } from "../stores/task"
import { useStatusStore } from "../stores/status"
import AddEditTask from "./AddEditTask.vue"
import { useRouter } from "vue-router"

const router = useRouter()
const taskStore = useTaskStore()
const statusStore = useStatusStore()
const tasks = computed(() => taskStore.getTasks)
const selectedTask = ref(null)
const sortOrder = ref("asc")

const showDeleteConfirmation = ref(false)
let selectedTaskToDelete = null
const showSuccessModal = ref(false)
const showNotFoundModal = ref(false)

const showTaskDetail = (task) => {
  router.push({
    name: "taskDetail",
    params: { taskId: task.id },
    state: { task }
  })
}

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
    const status = statusStore.getStatusById(task.status)
    if (status) {
      return status.name
        .replace(/_/g, " ")
        .replace(/(?:^|\s)\S/g, (char) => char.toUpperCase())
    } else {
      return "Unknown"
    }
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

const confirmDelete = (task) => {
  selectedTaskToDelete = task
  showDeleteConfirmation.value = true
}

const deleteTask = async () => {
  showDeleteConfirmation.value = false
  const selectedTask = await taskStore.deleteTask(selectedTaskToDelete.id)
  if (selectedTask.status === 404) {
    showNotFoundModal.value = true
  } else {
    showSuccessModal.value = true
  }
  selectedTaskToDelete = null
  await fetchTasks()

  setTimeout(() => {
    closeDeleteModal()
  }, 3000)
}
const fetchTasks = async () => {
  await taskStore.fetchAllTasks()
}

function closeDeleteModal() {
  showSuccessModal.value = false
  showNotFoundModal.value = false
}

const cancelDelete = () => {
  selectedTaskToDelete = null
  showDeleteConfirmation.value = false
}

const editTask = (task) => {
  if (task && task.id) {
    router.push({ name: "EditTask", params: { id: task.id } })
  }
}

onMounted(async () => {
  await taskStore.fetchAllTasks()
  await statusStore.fetchAllStatuses()
})
</script>

<template>
  <div class="container mx-auto px-4 pt-2">
    <div class="flex justify-end mb-10"></div>

    <div class="overflow-x-auto">
      <table
        class="table-fixed w-full max-w-6xl mx-auto border-separate border-spacing-y-2"
      >
        <thead>
          <tr class="text-white w-full">
            <th class="w-19 px-4 py-2 rounded-l-lg text-xl font-medium">No.</th>
            <th
              class="w-1/3 px-4 py-2 cursor-pointer text-xl font-medium"
              @click="toggleSortOrder('title')"
            >
              Title
            </th>
            <th class="w-1/3 px-4 py-2 text-xl font-medium">Assignees</th>
            <th class="w-1/6 px-4 py-2 text-xl font-medium">Status</th>
            <th class="w-1/8 px-4 py-2 rounded-r-lg"></th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(task, index) in sortedTasks"
            :key="index"
            class="bg-white shadow-lg hover:bg-gray-100 itbkk-item"
          >
            <td class="px-4 py-5 text-center rounded-l-lg">{{ index + 1 }}</td>
            <td class="px-4 py-2">
              <p
                class="truncate cursor-pointer itbkk-title"
                @click="showTaskDetail(task)"
              >
                {{ task.title }}
              </p>
            </td>

            <td class="truncate px-4 py-5 text-center itbkk-assignees">
              <span v-if="task.assignees">
                {{
                  task.assignees
                    .split(",")
                    .map((a) => a.trim())
                    .join(", ")
                }}
              </span>
              <span v-else class="text-gray-400 italic itbkk-assignees"
                >Unassigned</span
              >
            </td>

            <td class="px-5 py-2 text-center">
              <span
                class="inline-block px-2 py-1 rounded-full text-center itbkk-status"
                :style="statusStore.getStatusClass(task.status)"
              >
                {{ formattedStatus(task) }}
              </span>
            </td>

            <td class="px-1 py-2 rounded-r-lg itbkk-button-action">
              <button
                class="itbkk-button-edit hover:scale-110 transition-all duration-200 ease-in-out"
                @click="editTask(task)"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-6 w-6"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                  />
                </svg>
              </button>

              <button
                class="itbkk-button-delete hover:scale-110 transition-all duration-200 ease-in-out ml-2"
                @click="confirmDelete(task)"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-6 w-6"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="red"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                  />
                </svg>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="tasks.length <= 0" class="text-center mt-4 text-gray-500">
      No task
    </div>

    <AddEditTask
  v-if="selectedTask"
  :show="!!selectedTask"
  :task="{
    ...selectedTask,
    status: selectedTask.status // ส่ง status id แทน name
  }"
  mode="edit"
  @close="closeModal"
  @save="saveTask"
/>
  </div>

  <div
    v-if="showDeleteConfirmation"
    class="fixed inset-0 flex items-center justify-center bg-gray-500 bg-opacity-75"
  >
    <div class="bg-white p-8 rounded-lg">
      <p class="text-lg mb-4">
        Are you sure you want to delete
        <span class="text-red-500">{{ selectedTaskToDelete.title }}</span> task?
      </p>
      <div class="flex justify-end">
        <button
          class="bg-gray-200 text-gray-800 px-4 py-2 rounded mr-2"
          @click="cancelDelete"
        >
          Cancel
        </button>
        <button
          class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded"
          @click="deleteTask(selectedTask)"
        >
          Delete
        </button>
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
              @click="closeDeleteModal"
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
              The Task is deleted successfully.
            </p>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-1 sm:px-6 sm:flex sm:flex-row-reverse">
          <span class="flex w-full rounded-md shadow-sm sm:ml-3 sm:w-auto">
            <button
              type="button"
              class="inline-flex justify-center w-full rounded-md border border-transparent px-4 py-2 bg-indigo-600 text-base leading-6 font-medium text-white shadow-sm hover:bg-indigo-500 focus:outline-none focus:border-indigo-700 focus:shadow-outline-indigo transition ease-in-out duration-150 sm:text-sm sm:leading-3"
              @click="closeDeleteModal"
            >
              OK
            </button>
          </span>
        </div>
      </div>
    </div>
  </div>

  <div v-if="showNotFoundModal" class="fixed z-50 inset-0 overflow-y-auto">
    <div class="flex items-start justify-center min-h-screen">
      <div
        class="bg-white mt-2 rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full"
      >
        <div class="bg-white px-4 sm:p-4 sm:pb-4 py-0">
          <div class="flex justify-between items-center">
            <div class="text-lg leading-6 font-medium text-gray-900">
              Task Not Found
            </div>
            <button
              type="button"
              class="text-gray-400 hover:text-gray-500 focus:outline-none focus:text-gray-500 transition ease-in-out duration-150"
              @click="closeDeleteModal"
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
              The Task is not found.
            </p>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-1 sm:px-6 sm:flex sm:flex-row-reverse">
          <span class="flex w-full rounded-md shadow-sm sm:ml-3 sm:w-auto">
            <button
              type="button"
              class="inline-flex justify-center w-full rounded-md border border-transparent px-4 py-2 bg-indigo-600 text-base leading-6 font-medium text-white shadow-sm hover:bg-indigo-500 focus:outline-none focus:border-indigo-700 focus:shadow-outline-indigo transition ease-in-out duration-150 sm:text-sm sm:leading-3"
              @click="closeDeleteModal"
            >
              OK
            </button>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>