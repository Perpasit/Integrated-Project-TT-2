<!-- TaskTable.vue -->
<script setup>
import { ref, onMounted, computed } from "vue"
import { useTaskStore } from "../stores/task"
import { useStatusStore } from "../stores/status"
import AddEditTask from "./AddEditTask.vue"
import { useRouter , useRoute } from "vue-router"
import LimitTaskModal from "./LimitTaskModal.vue"

const router = useRouter()
const route = useRoute()
const taskStore = useTaskStore()
const statusStore = useStatusStore()
const tasks = computed(() => taskStore.getTasks)
const selectedTask = ref(null)

const showDeleteConfirmation = ref(false)
let selectedTaskToDelete = null
const showSuccessModal = ref(false)
const showNotFoundModal = ref(false)
const showLimitTaskModal = ref(false)
const maxTasksPerStatus = ref(10)

const showTaskDetail = (task) => {
  router.push({
    name: "taskDetail",
    params: { taskId: task.id },
  })
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
    const status = statusStore.getStatusByName(task.status)
    if (status) {
      return (
        status.name
          .toUpperCase()
          .replace(/(?:^|\s)\S/g, (char) => char.toUpperCase())
      )
    } else {
      return "Unknown"
    }
  }
})

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
  await fetchTasks()

  const sortByFromUrl = route.query.sortBy
  if (sortByFromUrl === 'asc' || sortByFromUrl === 'desc' || sortByFromUrl === 'normal') {
    sortBy.value = sortByFromUrl
    await fetchTasks()
  }

  // Check the filterStatus query parameter from the URL
  const filterStatusFromUrl = route.query.filterStatus
  if (filterStatusFromUrl) {
    const statusArray = filterStatusFromUrl.split(',')
    selectedStatuses.value = statusArray
    await fetchTasks()
  } else {
    // If filterStatus is not provided, clear the selectedStatuses
    selectedStatuses.value = []
    await fetchTasks()
  }
})

const isDropdownOpen = ref(false)

const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value
}

const selectedStatuses = computed({
  get: () => taskStore.filterStatuses,
  set: (value) => taskStore.setFilterStatuses(value),
})

const clearStatusFilter = () => {
  taskStore.setFilterStatuses([])
  searchKeyword.value = ""
}

const searchKeyword = ref("")
const displayStatusLimit = ref(5)
const showAllStatuses = ref(false)

const filteredStatuses = computed(() => {
  const results = statusStore.statuses.filter((status) =>
    status.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
  if (showAllStatuses.value) {
    return results
  }
  return results.slice(0, displayStatusLimit.value)
})

const sortBy = computed({
  get: () => taskStore.sortBy,
  set: (value) => taskStore.setSortBy(value),
})

const togglesortBy = () => {
  if (sortBy.value === "normal") {
    sortBy.value = "asc"
  } else if (sortBy.value === "asc") {
    sortBy.value = "desc"
  } else {
    sortBy.value = "normal"
  }
  taskStore.setSortBy(sortBy.value)
}

const updateMaxTasksPerStatus = (limit) => {
  maxTasksPerStatus.value = limit
}




</script>

<template>
  <div class="container mx-auto px-4 pt-2">
    <div class="flex justify-end mb-10"></div>

    
    <div class="overflow-visible">
      <table
        class="table-fixed w-full max-w-6xl mx-auto border-separate border-spacing-y-2"
      >
        <thead>
          <tr class="text-white w-full">
            <th class="w-19 py-2 rounded-l-lg text-xl font-medium">
              <div class="flex">
                <div class="relative">
                  <button
                    @click="toggleDropdown"
                    class="hover:scale-110 transition-all duration-200 ease-in-out text-white pl-1 py-2 rounded-md flex items-center justify-between w-36 hover:border-transparent hover:border-blue-500"
                  >
                    Filter Status
                    <svg
                      :class="{ 'transform rotate-180': isDropdownOpen }"
                      xmlns="http://www.w3.org/2000/svg"
                      viewBox="0 0 20 20"
                      fill="currentColor"
                      class="w-4 h-4"
                    >
                      <path
                        fill-rule="evenodd"
                        d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                        clip-rule="evenodd"
                      />
                    </svg>
                  </button>
                  <div
                    v-if="isDropdownOpen"
                    class="absolute mt-1 w-48 bg-white rounded shadow-lg z-10 text-gray-700"
                  >
                    <div class="py-1">
                      <div class="px-4 py-2">
                        <input
                          type="text"
                          v-model="searchKeyword"
                          placeholder="Search keywords..."
                          class="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                        />
                      </div>
                      <div class="px-4 py-2">
                        <span class="block font-medium text-sm text-gray-700"
                          >Status</span
                        >
                        <label
                          v-for="(status, index) in filteredStatuses"
                          :key="status.name"
                          class="flex items-center text-sm text-gray-700 mt-1"
                        >
                          <input
                            type="checkbox"
                            v-model="selectedStatuses"
                            :value="status.name"
                            class="mr-2"
                          />
                          {{ status.name }}
                        </label>
                      </div>
                      <!-- Show More Button -->
                      
                      <div class="px-4 py-2 flex justify-between">
                        <button
                        v-if="statusStore.statuses.length > displayStatusLimit"
                        @click="showAllStatuses = !showAllStatuses"
                        class=" text-sm  text-gray-700"
                      >
                        {{ showAllStatuses ? "Show Less" : "Show More" }}
                      </button>
                        <button
                          @click="clearStatusFilter"
                          class="text-sm text-gray-600 hover:text-gray-800"
                        >
                          Clear all
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </th>
            <th class="w-1/3 px-4 py-2 text-xl font-medium">Title</th>
            <th class="w-1/3 px-4 py-2 text-xl font-medium">Assignees</th>

           <th
        class="w-1/6 px-4 py-2 text-xl font-medium cursor-pointer"
        @click="togglesortBy"
      >
        Status
        <button class="ml-2 itbkk-status-sort">
          <svg
            v-if="sortBy === 'asc'"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 448 512"
            class="w-5 h-5"
          >
            <path
              fill="#7D2DFF"
              d="M176 352h-48V48a16 16 0 0 0 -16-16H80a16 16 0 0 0 -16 16v304H16c-14.2 0-21.4 17.2-11.3 27.3l80 96a16 16 0 0 0 22.6 0l80-96C197.4 369.3 190.2 352 176 352zm240-64H288a16 16 0 0 0 -16 16v32a16 16 0 0 0 16 16h56l-61.3 70.5A32 32 0 0 0 272 446.4V464a16 16 0 0 0 16 16h128a16 16 0 0 0 16-16v-32a16 16 0 0 0 -16-16h-56l61.3-70.5A32 32 0 0 0 432 321.6V304a16 16 0 0 0 -16-16zm31.1-85.4l-59.3-160A16 16 0 0 0 372.7 32h-41.4a16 16 0 0 0 -15.1 10.6l-59.3 160A16 16 0 0 0 272 224h24.8a16 16 0 0 0 15.2-11.1l4.4-12.9h71l4.4 12.9A16 16 0 0 0 407.2 224H432a16 16 0 0 0 15.1-21.4zM335.6 144L352 96l16.4 48z"
            ></path>
          </svg>
          <svg
            v-else-if="sortBy === 'desc'"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 448 512"
            class="w-5 h-5"
          >
            <path
              fill="#7D2DFF"
              d="M16 160h48v304a16 16 0 0 0 16 16h32a16 16 0 0 0 16-16V160h48c14.2 0 21.4-17.2 11.3-27.3l-80-96a16 16 0 0 0 -22.6 0l-80 96C-5.4 142.7 1.8 160 16 160zm400 128H288a16 16 0 0 0 -16 16v32a16 16 0 0 0 16 16h56l-61.3 70.5A32 32 0 0 0 272 446.4V464a16 16 0 0 0 16 16h128a16 16 0 0 0 16-16v-32a16 16 0 0 0 -16-16h-56l61.3-70.5A32 32 0 0 0 432 321.6V304a16 16 0 0 0 -16-16zm31.1-85.4l-59.3-160A16 16 0 0 0 372.7 32h-41.4a16 16 0 0 0 -15.1 10.6l-59.3 160A16 16 0 0 0 272 224h24.8a16 16 0 0 0 15.2-11.1l4.4-12.9h71l4.4 12.9A16 16 0 0 0 407.2 224H432a16 16 0 0 0 15.1-21.4zM335.6 144L352 96l16.4 48z"
            ></path>
          </svg>
          <svg
            v-else
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 448 512"
            class="w-5 h-5"
          >
            <path
              fill="#ffffff"
              d="M176 352h-48V48a16 16 0 0 0 -16-16H80a16 16 0 0 0 -16 16v304H16c-14.2 0-21.4 17.2-11.3 27.3l80 96a16 16 0 0 0 22.6 0l80-96C197.4 369.3 190.2 352 176 352zm240-64H288a16 16 0 0 0 -16 16v32a16 16 0 0 0 16 16h56l-61.3 70.5A32 32 0 0 0 272 446.4V464a16 16 0 0 0 16 16h128a16 16 0 0 0 16-16v-32a16 16 0 0 0 -16-16h-56l61.3-70.5A32 32 0 0 0 432 321.6V304a16 16 0 0 0 -16-16zm31.1-85.4l-59.3-160A16 16 0 0 0 372.7 32h-41.4a16 16 0 0 0 -15.1 10.6l-59.3 160A16 16 0 0 0 272 224h24.8a16 16 0 0 0 15.2-11.1l4.4-12.9h71l4.4 12.9A16 16 0 0 0 407.2 224H432a16 16 0 0 0 15.1-21.4zM335.6 144L352 96l16.4 48z"
            ></path>
          </svg>
        </button>
      </th>

            <th class="w-1/8 px-4 py-2 rounded-r-lg">
              <button
                class="itbkk-status-setting"
                @click="showLimitTaskModal = true"
              >
                <svg
                  class="w-5 h-5 mr-7 text-white dark:text-white"
                  aria-hidden="true"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 20 20"
                >
                  <path
                    stroke="currentColor"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M7.75 4H19M7.75 4a2.25 2.25 0 0 1-4.5 0m4.5 0a2.25 2.25 0 0 0-4.5 0M1 4h2.25m13.5 6H19m-2.25 0a2.25 2.25 0 0 1-4.5 0m4.5 0a2.25 2.25 0 0 0-4.5 0M1 10h11.25m-4.5 6H19M7.75 16a2.25 2.25 0 0 1-4.5 0m4.5 0a2.25 2.25 0 0 0-4.5 0M1 16h2.25"
                  />
                </svg>
              </button>
            </th>
          </tr>
        </thead>

        <LimitTaskModal
          :show="showLimitTaskModal"
          :maxTaskLimit="maxTasksPerStatus"
          @close="showLimitTaskModal = false"
          @save="updateMaxTasksPerStatus"
        />

        <tbody>
          <tr
            v-for="(task, index) in tasks"
            :key="index"
            class="bg-white shadow-lg hover:bg-gray-100 itbkk-item hover:scale-105 transition-all duration-200 ease-in-out"
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
                class="truncate inline-block px-2 py-1 rounded-full text-center itbkk-status w-32 h-10"
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
        status: selectedTask.status,
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
            <div class="text-lg leading-6 font-medium text-red-600">
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
            <p class="text-sm leading-5 text-red-600 itbkk-message">
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
