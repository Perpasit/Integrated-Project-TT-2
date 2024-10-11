<script setup>
import { ref, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useUserStore } from "../stores/userStore"
import { useBoardStore } from "../stores/boardStore"
import { useCollabStore } from "../stores/collaboratorStore"

const router = useRouter()
const boardStore = useBoardStore()
const userStore = useUserStore()
const collabStore = useCollabStore()
const boards = ref([])
const currentUser = userStore.user

const showDeleteConfirmation = ref(false)
let selectedBoardToDelete = null
const showSuccessModal = ref(false)
const showNotFoundModal = ref(false)
const showAddEditBoardModal = ref(false)
const selectedBoard = ref(null)

const confirmDelete = (board) => {
  console.log("Selected board to delete:", board) // Log เพื่อดูว่า board ที่ส่งมามีค่าหรือไม่
  selectedBoardToDelete = board
  showDeleteConfirmation.value = true
}

const leaveBoard = async (board) => {
  showDeleteConfirmation.value = false
  if (!board || !board.id) {
    console.error("Board is undefined or has no id:", board) // เพิ่ม log ตรวจสอบปัญหา
    return
  }
  try {
    await collabStore.removeCollaborator(board.id, currentUser.id)
    console.log(board.id)
    console.log(currentUser.id)
    showSuccessModal.value = true
    await fetchCollabsBoards()

    setTimeout(() => {
      closeDeleteModal()
    }, 3000)
  } catch (error) {
    console.error("Error leaving board:", error)
    if (error.message.includes("does not exist")) {
      showNotFoundModal.value = true
      await fetchCollabsBoards()
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

const navigateToTasks = (board) => {
  if (board && board.id) {
    router.push({ name: "home", params: { boardId: board.id } })
  }
}

const fetchCollabsBoards = async () => {
  try {
    await boardStore.fetchAllBoards()
    boards.value = boardStore.getBoardsCollab
  } catch (error) {
    console.error("Error fetching boards:", error)
  }
}


// // Function to leave a board
// const leaveBoard = async (board) => {
//   try {
//     await boardStore.leaveBoard(board.id, currentUser.id);
//     await fetchCollabsBoards();
//   } catch (error) {
//     console.error("Error leaving board:", error);
//   }
// };

// Eye toggle functionality
const openVisibilityModal = (board) => {
  selectedBoard.value = board;
  newVisibility.value = board.visibility === "PRIVATE" ? "PUBLIC" : "PRIVATE";
  showVisibilityModal.value = true;
};

const closeVisibilityModal = () => {
  showVisibilityModal.value = false;
};

const confirmVisibilityChange = async () => {
  try {
    await boardStore.updateBoard(selectedBoard.value.id, {
      visibility: newVisibility.value,
    });
    selectedBoard.value.visibility = newVisibility.value;
  } catch (error) {
    console.error("Error updating board visibility:", error);
  }
  closeVisibilityModal();
};


onMounted(async () => {
  await fetchCollabsBoards()
  console.log(boards.value.length)
})
</script>

<template>
  <div class="container mx-auto px-4 pt-2">
    <div class="overflow-visible">
      <h2 class="text-center text-5xl font-bold text-white mb-4 py-1">
        Collab Boards
      </h2>

      <table
        class="table-fixed w-full max-w-6xl mx-auto border-separate border-spacing-y-2"
      >
        <thead>
          <tr class="text-white w-full">
            <th class="w-1/12 py-2 rounded-l-lg text-xl font-medium">No</th>
            <th class="w-2/5 px-4 py-2 text-xl font-medium">Name</th>
            <th class="w-1/6 px-4 py-2 text-xl font-medium">Owner</th>
            <th class="w-1/6 px-4 py-2 text-xl font-medium">Access Right</th>
            <th class="w-1/6 px-4 py-2 rounded-r-lg text-xl font-medium">
              Action
            </th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="(board, index) in boards"
            :key="index"
            class="itbkk-item bg-white shadow-lg hover:bg-gray-100 hover:scale-105 transition-all duration-200 ease-in-out cursor-pointer"
            @click="navigateToTasks(board)"
          >
            <td class="px-4 py-5 text-center rounded-l-lg">{{ index + 1 }}</td>
            <td class="px-4 py-2">
              <p class="itbkk-title truncate">{{ board.name }}</p>
            </td>
            <td class="px-4 py-2">
              <p class="itbkk-owner truncate">{{ board.owner.name }}</p>
              <!-- Displaying the board owner's name -->
            </td>
            <td class="px-4 py-2">
              <p class="itbkk-access-right">
                {{ board.accessRight === "edit" ? "Edit" : "Read" }}
              </p>
              <!-- Conditionally display "Edit" or "Read" -->
            </td>
            <td
              class="itbkk-button-action px-4 py-5 rounded-r-lg flex justify-evenly"
            >
              <button
                class="itbkk-leave-board bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 transition-all duration-200 ease-in-out"
                @click.stop="confirmDelete(board)"
              >
                Leave
              </button>
              <!-- Leave button to trigger leaveBoard function -->
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div
      v-if="!boards || boards.length <= 0"
      class="text-center mt-4 text-gray-500"
    >
      No Collaborative Board
    </div>
  </div>

  <!-- Leave Confirmation Modal -->
  <div
    v-if="showDeleteConfirmation"
    class="fixed inset-0 flex items-center justify-center bg-gray-500 bg-opacity-75"
  >
    <div class="bg-white p-8 rounded-lg">
      <p class="text-lg mb-4">
        Are you sure you want to leave the
        <span class="text-red-500">{{ selectedBoardToDelete.name }}</span>
        board?
      </p>
      <div class="flex justify-end">
        <button
          class="bg-gray-200 text-gray-800 px-4 py-2 rounded mr-2"
          @click="cancelDelete"
        >
          Cancel
        </button>
        <button
          class="itbkk-leave-board bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 transition-all duration-200 ease-in-out"
          @click.stop="leaveBoard(selectedBoardToDelete)"
          > Leave
        </button>
      </div>
    </div>
  </div>

  <!-- Success Modal -->
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
            <p class="text-sm leading-5 text-gray-500">
              You have successfully left the board.
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

  <!-- Not Found Modal -->
  <div v-if="showNotFoundModal" class="fixed z-50 inset-0 overflow-y-auto">
    <div class="flex items-start justify-center min-h-screen">
      <div
        class="bg-white mt-2 rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full"
      >
        <div class="bg-white px-4 sm:p-4 sm:pb-4 py-0">
          <div class="flex justify-between items-center">
            <div class="text-lg leading-6 font-medium text-red-600">
              Board Not Found
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
            <p class="text-sm leading-5 text-red-600">
              The Board is not found.
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
