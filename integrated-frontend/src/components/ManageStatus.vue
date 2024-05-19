<!-- ManageStatus.vue -->
<script setup>
import { ref, onMounted, computed } from "vue"
import { useRouter } from "vue-router"
import Header from "./Header.vue"
import AddEditStatus from "./AddEditStatus.vue"
import { useStatusStore } from "../stores/status"
import { useModalStore } from "../stores/useModalStore"

const showDeleteConfirmationModal = ref(false)
const modalStore = useModalStore()
const statusStore = useStatusStore()
const router = useRouter()
const statuses = computed(() => statusStore.getStatuses)
const deleteResult = ref(null)
const selectedStatus = ref(null)
const showDeleteConfirmation = ref(false)

const showSuccessModal = ref(false)
const showNotFoundModal = ref(false)

let selectedStatusToDelete = null

const showDeleteTransferModal = ref(false)
const showCannotEditDeleteModal = ref(false)
const cannotDeleteEditTimeout = ref(null)

const showStatusDetail = (status) => {
  router.push({
    name: "statusDetail",
    params: { statusId: status.id }
  })
}

const closeCannotDeleteModal = () => {
  showCannotEditDeleteModal.value = false
  selectedStatusToDelete = null
  clearTimeout(cannotDeleteEditTimeout.value)
}

const editStatus = (status) => {
  if (status.id !== 1 && status.id !== 4) {
    router.push({ name: "EditStatus", params: { id: status.id } })
  } else {
    showCannotEditDeleteModal.value = true
    cannotDeleteEditTimeout.value = setTimeout(() => {
      closeCannotDeleteModal()
    }, 3000)
  }
}

const saveStatus = (status) => {
  if (status.id) {
    const index = statuses.value.findIndex((s) => s.id === status.id)
    statuses.value.splice(index, 1, { ...status })
  } else {
    status.id = Date.now()
    statuses.value.push(status)
  }
  selectedStatus.value = null
}

async function confirmDeleteStatus() {
  if (selectedStatusToDelete) {
    try {
      const result = await statusStore.deleteStatus(selectedStatusToDelete.id)

      if (result.notFound) {
        showDeleteConfirmationModal.value = false
        selectedStatusToDelete = null
        showNotFoundModal.value = true

        statusStore.fetchAllStatuses()
        setTimeout(() => {
          showNotFoundModal.value = false
        }, 3000)
      } else if (result.needsTransfer) {
        showDeleteConfirmationModal.value = false
        confirmDeleteTransfer(selectedStatusToDelete)
      } else {
        showDeleteConfirmationModal.value = false
        selectedStatusToDelete = null
        showSuccessModal.value = true
        setTimeout(() => {
          router.push({ name: "ManageStatus" })
        }, 3000)
      }
    } catch (error) {
      console.error("Error deleting status:", error)
    }
    setTimeout(() => {
      closeDeleteModal()
    }, 3000)
  }
}

const canDeleteStatus = computed(() => {
  return selectedStatusToDelete && selectedStatusToDelete.id !== 1
})

const confirmDeleteTransfer = (status) => {
  if (status && status.id !== 1) {
    selectedStatusToDelete = status
    showDeleteTransferModal.value = true
  } else {
    showCannotEditDeleteModal.value = true
    cannotDeleteEditTimeout.value = setTimeout(() => {
      closeCannotDeleteModal()
    }, 3000)
  }
}
const closeCannotEditDeleteModal = () => {
  showCannotEditDeleteModal.value = false
  selectedStatusToDelete = null
}
const newStatusId = ref(1)

const deleteAndTransfer = async (newStatusId) => {
  if (canDeleteStatus.value) {
    try {
      const result = await statusStore.deleteStatusTransfer(
        selectedStatusToDelete.id,
        newStatusId
      )
      deleteResult.value = result
      selectedStatusToDelete = null
      showDeleteTransferModal.value = false
      statusStore.fetchAllStatuses()

      if (result.notFound) {
        showNotFoundModal.value = true
      } else if (result.status === 404) {
        showNotFoundModal.value = true
      } else if (result.status === 500) {
        showNotFoundModal.value = true
      } else {
        showSuccessModal.value = true
      }
    } catch (error) {
      console.error("Error deleting and transferring status:", error)
      deleteResult.value = { status: 500 }
      showSuccessModal.value = false
      showNotFoundModal.value = true
    }
    setTimeout(() => {
      closeDeleteModal()
    }, 3000)
  }
}
function closeDeleteModal() {
  showSuccessModal.value = false
  showNotFoundModal.value = false
}

const fetchStatuses = async () => {
  await statusStore.fetchAllStatuses()
}

const cancelDeleteTransfer = () => {
  selectedStatusToDelete = null
  showDeleteTransferModal.value = false
}

const formatStatus = (status) => {
  return (
    status
      .toUpperCase()
      // .replace(/_/g, " ")
      .replace(/(?:^|\s)\S/g, (char) => char.toUpperCase())
  )
}

const closeSuccessModal = () => {
  modalStore.closeSuccessModal()
}

const getStatusColor = (status) => {
  return statusStore.getStatusColorByName(status.name)
}

onMounted(async () => {
  await statusStore.fetchAllStatuses()
})
</script>

<template>
  <div
    class="manage-status-page bg-gradient-to-t from-[#ffc3ee] to-[#A9C9FF] min-h-screen"
  >
    <Header
      @addStatus="selectedStatus = { id: null, name: '', description: '' }"
    />

    <div class="container mx-auto px-4 pt-12">
      <div class="overflow-x-auto">
        <table
          class="table-fixed w-full max-w-6xl mx-auto border-separate border-spacing-y-2"
        >
          <thead>
            <tr class="text-white w-full">
              <th class="w-1/6 px-4 py-2 rounded-l-lg text-xl font-medium">
                No.
              </th>
              <th class="w-1/6 px-4 py-2 text-xl font-medium">Name</th>
              <th class="w-1/2 px-4 py-2 text-xl font-medium">Description</th>
              <th class="w-1/6 px-4 py-2 text-xl font-medium">Color</th>
              <th class="w-1/6 px-4 py-2 rounded-r-lg text-xl font-medium">
                Action
              </th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="(status, index) in statuses"
              :key="status.id"
              class="bg-white shadow-lg hover:bg-gray-100 hover:scale-105 transition-all duration-200 ease-in-out"
            >
              <td class="px-4 py-5 rounded-l-lg text-center">
                {{ index + 1 }}
              </td>

              <td
                class="itbkk-status-name px-4 py-5 truncate text-center cursor-pointer"
                @click="showStatusDetail(status)"
              >
                {{ formatStatus(status.name) }}
              </td>
              <td
                class="itbkk-status-description px-4 py-2 truncate text-center"
              >
                <span v-if="status.description">
                  {{ status.description }}
                </span>
                <span
                  v-else
                  class="text-gray-400 italic itbkk-status-description"
                >
                  No Description Provided
                </span>
              </td>

              <td class="px-4 py-4 text-center">
                <div
                  class="w-6 h-6 rounded-full mx-auto"
                  :style="{ 'background-color': getStatusColor(status) }"
                ></div>
              </td>

              <td
                class="px-4 py-2 rounded-r-lg text-center itbkk-button-action"
              >
                <button
                  class="itbkk-button-edit hover:scale-110 transition-all duration-200 ease-in-out px-2 py-1"
                  @click="editStatus(status)"
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
                  @click="
                    if (status.id === 1 || status.id === 4) {
                      showCannotEditDeleteModal = true;
                      selectedStatusToDelete = status;
                    } else {
                      showDeleteConfirmationModal = true;
                      selectedStatusToDelete = status;
                    }
                  "
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
      <div v-if="statuses.length <= 0" class="text-center mt-4 text-gray-500">
        No Status
      </div>

      <AddEditStatus
        v-if="selectedStatus"
        :show="!!selectedStatus"
        :status="selectedStatus"
        @close="selectedStatus = null"
        @save="saveStatus"
      />
    </div>

    <div
      v-if="showCannotEditDeleteModal"
      class="fixed z-50 inset-0 overflow-y-auto"
    >
      <div class="flex items-start justify-center min-h-screen">
        <div
          class="bg-white mt-2 rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full"
        >
          <div class="bg-white px-4 sm:p-4 sm:pb-4 py-0">
            <div class="flex justify-between items-center">
              <div class="text-lg leading-6 font-medium text-red-600">
                {{
                  selectedStatusToDelete
                    ? 'Cannot Delete "NO_STATUS"'
                    : 'Cannot Edit "NO_STATUS"'
                }}
              </div>
              <button
                type="button"
                class="text-gray-400 hover:text-gray-500 focus:outline-none focus:text-gray-500 transition ease-in-out duration-150"
                @click="closeCannotEditDeleteModal"
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
                {{
                  selectedStatusToDelete
                    ? "You cannot delete the Default status."
                    : "You cannot edit the Default status."
                }}
              </p>
            </div>
          </div>
          <div class="bg-gray-50 px-4 py-1 sm:px-6 sm:flex sm:flex-row-reverse">
            <span class="flex w-full rounded-md shadow-sm sm:ml-3 sm:w-auto">
              <button
                type="button"
                class="inline-flex justify-center w-full rounded-md border border-transparent px-4 py-2 bg-indigo-600 text-base leading-6 font-medium text-white shadow-sm hover:bg-indigo-500 focus:outline-none focus:border-indigo-700 focus:shadow-outline-indigo transition ease-in-out duration-150 sm:text-sm sm:leading-3"
                @click="closeCannotEditDeleteModal"
              >
                OK
              </button>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showDeleteTransferModal"
      class="fixed inset-0 flex items-center justify-center bg-gray-500 bg-opacity-75"
    >
      <div class="bg-white p-8 rounded-lg">
        <p class="text-lg mb-4 itbkk-message">
          Transfer tasks from
          <span class="text-red-500">{{ selectedStatusToDelete.name }}</span>
          status to:
        </p>

        <div class="mb-4">
          <select
            v-model="newStatusId"
            class="border border-gray-300 rounded py-2 px-3 w-full"
          >
            <option
              v-for="status in statuses.filter(
                (s) => s.id !== selectedStatusToDelete.id
              )"
              :key="status.id"
              :value="status.id"
            >
              {{ formatStatus(status.name) }}
            </option>
          </select>
        </div>

        <div class="flex justify-end">
          <button
            class="itbkk-button-cancel bg-gray-200 text-gray-800 px-4 py-2 rounded mr-2"
            @click="cancelDeleteTransfer"
          >
            Cancel
          </button>
          <button
            class="itbkk-button-confirm bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded"
            @click="deleteAndTransfer(newStatusId)"
          >
            Transfer and Delete
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
              The task(s) have been transferred and the status has been deleted.
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
              {{
                deleteResult?.status === 500
                  ? "Status Limit Reached"
                  : "Status Not Found"
              }}
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
            <p class="text-sm leading-5 itbkk-message text-red-600">
              {{
                deleteResult?.status === 500
                  ? "The status that you are about to transfer has reached its maximum task limit."
                  : "An error has occurred, the status does not exist."
              }}
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

  <div
    v-if="modalStore.showSuccessModal"
    class="fixed z-50 inset-0 overflow-y-auto"
  >
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
              The {{ modalStore.entityType }} "{{ modalStore.taskTitle }}" is
              {{
                modalStore.mode === "add"
                  ? "added"
                  : modalStore.mode === "edit"
                  ? "edited"
                  : "deleted"
              }}
              successfully.
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
  <div
    v-if="showDeleteConfirmationModal"
    class="fixed inset-0 flex items-center justify-center bg-gray-500 bg-opacity-75"
  >
    <div class="bg-white p-8 rounded-lg">
      <p class="text-lg mb-4 itbkk-message">
        Are you sure you want to delete the status "{{
          selectedStatusToDelete.name
        }}"?
      </p>
      <div class="flex justify-end">
        <button
          class="itbkk-button-cancel bg-gray-200 text-gray-800 px-4 py-2 rounded mr-2"
          @click="
            ;(showDeleteConfirmationModal = false),
              (selectedStatusToDelete = null)
          "
        >
          Cancel
        </button>
        <button
          class="itbkk-button-confirm bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded"
          @click="confirmDeleteStatus"
        >
          Delete
        </button>
      </div>
    </div>
  </div>

  <router-view />
</template>
