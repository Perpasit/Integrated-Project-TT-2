<script setup>
import { ref, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useUserStore } from "../stores/userStore"
import { useBoardStore } from "../stores/boardStore"
import AddEditBoard from "./AddEditBoard.vue"
import VisibilityModal from "./VisibilityModal.vue"
import TaskTable from "./TaskTable.vue"

const router = useRouter()
const boardStore = useBoardStore()
const userStore = useUserStore()
const boards = ref([])

const showDeleteConfirmation = ref(false)
let selectedBoardToDelete = null
const showSuccessModal = ref(false)
const showNotFoundModal = ref(false)
const showAddEditBoardModal = ref(false)
const selectedBoard = ref(null)
const showVisibilityModal = ref(false)
const newVisibility = ref("")

const editBoard = (board) => {
    selectedBoard.value = board
    showAddEditBoardModal.value = true
}

const confirmDelete = (board) => {
    selectedBoardToDelete = board
    showDeleteConfirmation.value = true
}

const deleteBoard = async () => {
    showDeleteConfirmation.value = false
    try {
      await boardStore.deleteBoard(selectedBoardToDelete.id)
      showSuccessModal.value = true
      selectedBoardToDelete.value = null
      await fetchPersonalBoards()

      setTimeout(() => {
        closeDeleteModal()
      }, 3000)
    } catch (error) {
      if (error.message.includes("does not exist")) {
        showNotFoundModal.value = true
        await fetchPersonalBoards()
        setTimeout(() => {
          closeDeleteModal()
        }, 3000)
      }
    }
    
}

function closeDeleteModal() {
  showSuccessModal.value = false
  showNotFoundModal.value = false
}

const cancelDelete = () => {
    selectedBoardToDelete = null
    showDeleteConfirmation.value = false
}

const openAddEditBoardModal = (board = null) => {
    selectedBoard.value = board
    showAddEditBoardModal.value = true
}


const navigateToTasks = (board) => {
  
    if (board && board.id) {
      
      router.push({ name: "home", params: { boardId: board.id } })
      
    }
}

const fetchPersonalBoards = async () => {
    try {
      await boardStore.fetchAllBoards()
      boards.value = boardStore.getBoardsPersonal
      console.log(boards.value)
    } catch (error) {
      console.error("Error fetching boards:", error)
    }
}

// Eye toggle functionality
const openVisibilityModal = (board) => { 
  selectedBoard.value = board
  newVisibility.value = board.visibility === "PRIVATE" ? "PUBLIC" : "PRIVATE"
  showVisibilityModal.value = true
}
// Close modal
const closeVisibilityModal = () => {
  showVisibilityModal.value = false
}

// Confirm the visibility change
const confirmVisibilityChange = async () => {
  try {
    await boardStore.updateBoard(selectedBoard.value.id, {
      visibility: newVisibility.value
    })
    // console.log(`Board visibility updated to ${newVisibility.value}`)
    selectedBoard.value.visibility = newVisibility.value
  } catch (error) {
    console.error("Error updating board visibility:", error)
  }
  closeVisibilityModal()
}

onMounted(async () => {
  await fetchPersonalBoards()
})
</script>

<template>
  <div class="container mx-auto px-4 pt-2">
    <div class="overflow-visible">
      <h2 class="text-center text-5xl font-bold text-white mb-4 py-1">Personal Board</h2>

      <div class="flex justify-end mb-6 pb-4 border-b border-white">
        <button @click="openAddEditBoardModal()"
          class="itbkk-button-create relative inline-flex items-center justify-center p-0.5 overflow-hidden text-sm font-medium text-white rounded-lg group bg-gradient-to-br from-[#eddbe8] to-[#e9b9de] group-hover:from-teal-300 transition duration-300 ease-in-out transform hover:scale-105">
          <span
            class="relative px-5 py-2.5 transition-all ease-in duration-75  dark:bg-gray-900 rounded-md group-hover:bg-opacity-0">
            Create Personal Board
          </span>
        </button>
      </div>

      <table class="table-fixed w-full max-w-6xl mx-auto border-separate border-spacing-y-2">
        <thead>
          <tr class="text-white w-full">
            <th class="w-1/12 py-2 rounded-l-lg text-xl font-medium">No</th>
            <th class="w-2/3 px-4 py-2 text-xl font-medium">Name</th>
            <th class="w-1/6 px-4 py-2 rounded-r-lg text-xl font-medium">
              Action
            </th>
          </tr>
        </thead>
        <tbody>
  <tr v-for="(board, index) in boards" :key="index"
    class="itbkk-item bg-white shadow-lg hover:bg-gray-100 hover:scale-105 transition-all duration-200 ease-in-out cursor-pointer"
    @click="navigateToTasks(board)">
    <td class="px-4 py-5 text-center rounded-l-lg">{{ index + 1 }}</td>
    <td class="px-4 py-2">
      <p class="itbkk-title truncate">{{ board.name }}</p>
    </td>
    <td class="itbkk-button-action px-4 py-5 rounded-r-lg flex justify-evenly">
      <!-- Eye toggle -->
      <button class="itbkk-button-toggle hover:scale-110 transition-all duration-200 ease-in-out"
        @click.stop="openVisibilityModal(board)">
        <svg v-if="board.visibility === 'PRIVATE'" class="h-7 w-7" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
<path fill-rule="evenodd" clip-rule="evenodd" d="M19.7071 5.70711C20.0976 5.31658 20.0976 4.68342 19.7071 4.29289C19.3166 3.90237 18.6834 3.90237 18.2929 4.29289L14.032 8.55382C13.4365 8.20193 12.7418 8 12 8C9.79086 8 8 9.79086 8 12C8 12.7418 8.20193 13.4365 8.55382 14.032L4.29289 18.2929C3.90237 18.6834 3.90237 19.3166 4.29289 19.7071C4.68342 20.0976 5.31658 20.0976 5.70711 19.7071L9.96803 15.4462C10.5635 15.7981 11.2582 16 12 16C14.2091 16 16 14.2091 16 12C16 11.2582 15.7981 10.5635 15.4462 9.96803L19.7071 5.70711ZM12.518 10.0677C12.3528 10.0236 12.1792 10 12 10C10.8954 10 10 10.8954 10 12C10 12.1792 10.0236 12.3528 10.0677 12.518L12.518 10.0677ZM11.482 13.9323L13.9323 11.482C13.9764 11.6472 14 11.8208 14 12C14 13.1046 13.1046 14 12 14C11.8208 14 11.6472 13.9764 11.482 13.9323ZM15.7651 4.8207C14.6287 4.32049 13.3675 4 12 4C9.14754 4 6.75717 5.39462 4.99812 6.90595C3.23268 8.42276 2.00757 10.1376 1.46387 10.9698C1.05306 11.5985 1.05306 12.4015 1.46387 13.0302C1.92276 13.7326 2.86706 15.0637 4.21194 16.3739L5.62626 14.9596C4.4555 13.8229 3.61144 12.6531 3.18002 12C3.6904 11.2274 4.77832 9.73158 6.30147 8.42294C7.87402 7.07185 9.81574 6 12 6C12.7719 6 13.5135 6.13385 14.2193 6.36658L15.7651 4.8207ZM12 18C11.2282 18 10.4866 17.8661 9.78083 17.6334L8.23496 19.1793C9.37136 19.6795 10.6326 20 12 20C14.8525 20 17.2429 18.6054 19.002 17.0941C20.7674 15.5772 21.9925 13.8624 22.5362 13.0302C22.947 12.4015 22.947 11.5985 22.5362 10.9698C22.0773 10.2674 21.133 8.93627 19.7881 7.62611L18.3738 9.04043C19.5446 10.1771 20.3887 11.3469 20.8201 12C20.3097 12.7726 19.2218 14.2684 17.6986 15.5771C16.1261 16.9282 14.1843 18 12 18Z" fill="#000000"/>
</svg>
        <svg v-else class="h-7 w-7" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
<path fill-rule="evenodd" clip-rule="evenodd" d="M6.30147 15.5771C4.77832 14.2684 3.6904 12.7726 3.18002 12C3.6904 11.2274 4.77832 9.73158 6.30147 8.42294C7.87402 7.07185 9.81574 6 12 6C14.1843 6 16.1261 7.07185 17.6986 8.42294C19.2218 9.73158 20.3097 11.2274 20.8201 12C20.3097 12.7726 19.2218 14.2684 17.6986 15.5771C16.1261 16.9282 14.1843 18 12 18C9.81574 18 7.87402 16.9282 6.30147 15.5771ZM12 4C9.14754 4 6.75717 5.39462 4.99812 6.90595C3.23268 8.42276 2.00757 10.1376 1.46387 10.9698C1.05306 11.5985 1.05306 12.4015 1.46387 13.0302C2.00757 13.8624 3.23268 15.5772 4.99812 17.0941C6.75717 18.6054 9.14754 20 12 20C14.8525 20 17.2429 18.6054 19.002 17.0941C20.7674 15.5772 21.9925 13.8624 22.5362 13.0302C22.947 12.4015 22.947 11.5985 22.5362 10.9698C21.9925 10.1376 20.7674 8.42276 19.002 6.90595C17.2429 5.39462 14.8525 4 12 4ZM10 12C10 10.8954 10.8955 10 12 10C13.1046 10 14 10.8954 14 12C14 13.1046 13.1046 14 12 14C10.8955 14 10 13.1046 10 12ZM12 8C9.7909 8 8.00004 9.79086 8.00004 12C8.00004 14.2091 9.7909 16 12 16C14.2092 16 16 14.2091 16 12C16 9.79086 14.2092 8 12 8Z" fill="#000000"/>
</svg>
      </button>

      <!-- Edit and delete buttons -->
      <button class="itbkk-button-edit hover:scale-110 transition-all duration-200 ease-in-out ml-2"
        @click.stop="editBoard(board)">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
        </svg>
      </button>
      <button class="itbkk-button-delete hover:scale-110 transition-all duration-200 ease-in-out ml-2"
        @click.stop="confirmDelete(board)">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="red">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
        </svg>
      </button>
    </td>
  </tr>
</tbody>
      </table>
    </div>
    <div v-if="boards.length <= 0" class="text-center mt-4 text-gray-500">
      No Personal Board
    </div>
  </div>

  <VisibilityModal
  v-if="showVisibilityModal"
  :board="selectedBoard"
  :new-visibility="newVisibility"
  @close="closeVisibilityModal"
  @confirm="confirmVisibilityChange"
/>
  <!-- AddEditBoard component -->
  <AddEditBoard
    v-model:showModal="showAddEditBoardModal"
    :editingBoard="selectedBoard"
    @boardSaved="fetchPersonalBoards"
  />

  <!-- Delete Confirmation Modal -->
  <div v-if="showDeleteConfirmation" class="fixed inset-0 flex items-center justify-center bg-gray-500 bg-opacity-75">
    <div class="bg-white p-8 rounded-lg">
      <p class="itbkk-message text-lg mb-4">
        Are you sure you want to delete
        <span class="text-red-500">{{ selectedBoardToDelete.name }}</span>
        board?
      </p>
      <div class="flex justify-end">
        <button class="itbkk-button-cancel bg-gray-200 text-gray-800 px-4 py-2 rounded mr-2" @click="cancelDelete">
          Cancel
        </button>
        <button class="itbkk-button-confirm bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded"
          @click="deleteBoard">
          Delete
        </button>
      </div>
    </div>
  </div>

  <!-- Success Modal -->
  <div v-if="showSuccessModal" class="fixed z-50 inset-0 overflow-y-auto">
    <div class="flex items-start justify-center min-h-screen">
      <div class="bg-white mt-2 rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full">
        <div class="bg-white px-4 sm:p-4 sm:pb-4 py-0">
          <div class="flex justify-between items-center">
            <div class="text-lg leading-6 font-medium text-gray-900">
              Success
            </div>
            <button type="button"
              class="text-gray-400 hover:text-gray-500 focus:outline-none focus:text-gray-500 transition ease-in-out duration-150"
              @click="closeDeleteModal">
              <svg class="h-6 w-6" stroke="currentColor" fill="none" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
          <div>
            <p class="itbkk-message text-sm leading-5 text-gray-500">
              The Board is deleted successfully.
            </p>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-1 sm:px-6 sm:flex sm:flex-row-reverse">
          <span class="flex w-full rounded-md shadow-sm sm:ml-3 sm:w-auto">
            <button type="button"
              class="inline-flex justify-center w-full rounded-md border border-transparent px-4 py-2 bg-indigo-600 text-base leading-6 font-medium text-white shadow-sm hover:bg-indigo-500 focus:outline-none focus:border-indigo-700 focus:shadow-outline-indigo transition ease-in-out duration-150 sm:text-sm sm:leading-3"
              @click="closeDeleteModal">
              OK
            </button>
          </span>
        </div>
      </div>
    </div>
  </div>

  <!-- Not Found Modal -->
  <div v-if="showNotFoundModal" class="fixed z-50 inset-0 overflow-y-auto">
    <div class="flex items-start justify-center min-h-screen">
      <div class="bg-white mt-2 rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full">
        <div class="bg-white px-4 sm:p-4 sm:pb-4 py-0">
          <div class="flex justify-between items-center">
            <div class="text-lg leading-6 font-medium text-red-600">
              Board Not Found
            </div>
            <button type="button"
              class="text-gray-400 hover:text-gray-500 focus:outline-none focus:text-gray-500 transition ease-in-out duration-150"
              @click="closeDeleteModal">
              <svg class="h-6 w-6" stroke="currentColor" fill="none" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
          <div>
            <p class="itbkk-message text-sm leading-5 text-red-600">
              The Board is not found.
            </p>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-1 sm:px-6 sm:flex sm:flex-row-reverse">
          <span class="flex w-full rounded-md shadow-sm sm:ml-3 sm:w-auto">
            <button type="button"
              class="inline-flex justify-center w-full rounded-md border border-transparent px-4 py-2 bg-indigo-600 text-base leading-6 font-medium text-white shadow-sm hover:bg-indigo-500 focus:outline-none focus:border-indigo-700 focus:shadow-outline-indigo transition ease-in-out duration-150 sm:text-sm sm:leading-3"
              @click="closeDeleteModal">
              OK
            </button>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>