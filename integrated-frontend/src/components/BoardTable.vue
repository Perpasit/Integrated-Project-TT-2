<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useBoardStore } from "../stores/boardStore";

const router = useRouter();
const boardStore = useBoardStore();
const boards = ref([]);

const showDeleteConfirmation = ref(false);
let selectedBoardToDelete = null;
const showSuccessModal = ref(false);
const showNotFoundModal = ref(false);

const fetchBoards = async () => {
  await boardStore.fetchAllBoards();
  boards.value = boardStore.boards;
};

const editBoard = (board) => {
  if (board && board.id) {
    router.push({ name: "EditBoard", params: { id: board.id } });
  }
};

const confirmDelete = (board) => {
  selectedBoardToDelete = board;
  showDeleteConfirmation.value = true;
};

const deleteBoard = async () => {
  showDeleteConfirmation.value = false;
  try {
    await boardStore.deleteBoard(selectedBoardToDelete.id);
    showSuccessModal.value = true;
    selectedBoardToDelete = null;
    fetchBoards();

    setTimeout(() => {
      closeDeleteModal();
    }, 3000);
  } catch (error) {
    if (error.message.includes("does not exist")) {
      showNotFoundModal.value = true;
      fetchBoards();
      setTimeout(() => {
        closeDeleteModal();
      }, 3000);
    }
  }
};

function closeDeleteModal() {
  showSuccessModal.value = false;
  showNotFoundModal.value = false;
}

const cancelDelete = () => {
  selectedBoardToDelete = null;
  showDeleteConfirmation.value = false;
};

onMounted(async () => {
  fetchBoards();
});
</script>

<template>
  <div class="container mx-auto px-4 pt-2">
    <div class="overflow-visible">
      <table
        class="table-fixed w-full max-w-6xl mx-auto border-separate border-spacing-y-2"
      >
        <thead>
          <tr class="text-white w-full">
            <th class="w-1/12 py-2 rounded-l-lg text-xl font-medium">No</th>
            <th class="w-2/3 px-4 py-2 text-xl font-medium ">Name</th>
            <th class="w-1/6 px-4 py-2 rounded-r-lg text-xl font-medium">
              Action
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(board, index) in boards"
            :key="index"
            class="itbkk-item bg-white shadow-lg hover:bg-gray-100 hover:scale-105 transition-all duration-200 ease-in-out"
          >
            <td class="px-4 py-5 text-center rounded-l-lg">{{ index + 1 }}</td>
            <td class="px-4 py-2">
              <p class="itbkk-title truncate">{{ board.name }}</p>
            </td>
            <td class="itbkk-button-action px-1 py-2 rounded-r-lg">
              <button
                class="itbkk-button-edit hover:scale-110 transition-all duration-200 ease-in-out"
                @click="editBoard(board)"
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
                @click="confirmDelete(board)"
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
    <div v-if="boards.length <= 0" class="text-center mt-4 text-gray-500">
      No Boards
    </div>
  </div>

  <!-- Delete Confirmation Modal -->
  <div
    v-if="showDeleteConfirmation"
    class="fixed inset-0 flex items-center justify-center bg-gray-500 bg-opacity-75"
  >
    <div class="bg-white p-8 rounded-lg">
      <p class="itbkk-message text-lg mb-4">
        Are you sure you want to delete
        <span class="text-red-500">{{ selectedBoardToDelete.name }}</span>
        board?
      </p>
      <div class="flex justify-end">
        <button
          class="itbkk-button-cancel bg-gray-200 text-gray-800 px-4 py-2 rounded mr-2"
          @click="cancelDelete"
        >
          Cancel
        </button>
        <button
          class="itbkk-button-confirm bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded"
          @click="deleteBoard"
        >
          Delete
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
            <p class="itbkk-message text-sm leading-5 text-gray-500">
              The Board is deleted successfully.
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
            <p class="itbkk-message text-sm leading-5 text-red-600">
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
