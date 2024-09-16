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
const showCreateBoardModal = ref(false);
const newBoardName = ref("");



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


const createPersonalBoard = () => {
  console.log("Create Personal Board clicked");
};

const openCreateBoardModal = () => {
  showCreateBoardModal.value = true;
  newBoardName.value = "";
};

const closeCreateBoardModal = () => {
  showCreateBoardModal.value = false;
  newBoardName.value = "";
};

const saveNewBoard = async () => {
  if (newBoardName.value.trim()) {
    try {
      await boardStore.createBoard(newBoardName.value.trim());
      closeCreateBoardModal();
      fetchBoards();
    } catch (error) {
      console.error("Error creating new board:", error);
    }
  }
};


const fetchBoards = async () => {
  try {
    await boardStore.fetchAllBoards();
    boards.value = boardStore.getBoards;

    if (boards.value.length >= 1) {
      router.push({ name: "home" , params :{boardId: boards.id = 1} });
  }

  } catch (error) {
    console.error('Error fetching boards:', error);
  }
};

onMounted(async () => {
  await fetchBoards();
});

const navigateToTasks = (board) => {
  if (board && board.id) {
    router.push({ name: "home", params: { boardId: board.id } });
  }
};

</script>

<template>
  <div class="container mx-auto px-4 pt-2">
    <div class="overflow-visible">
      <h2 class="text-center text-5xl font-bold text-white mb-4 py-1">Board List</h2>

      <div class="flex justify-end mb-6 pb-4 border-b border-white">
        <button @click="openCreateBoardModal"
          class="itbkk-button-create relative inline-flex items-center justify-center p-0.5 overflow-hidden text-sm font-medium text-white rounded-lg group bg-gradient-to-br from-[#eddbe8] to-[#e9b9de] group-hover:from-teal-300 transition duration-300 ease-in-out transform hover:scale-105">
          <span
            class="relative px-5 py-2.5 transition-all ease-in duration-75  dark:bg-gray-900 rounded-md group-hover:bg-opacity-0">
            Create Personal Board
          </span>
        </button>
      </div>


      <div v-if="showCreateBoardModal" class="fixed z-10 inset-0 overflow-y-auto">
        <div class="flex items-center justify-center min-h-screen px-4">
          <div class="fixed inset-0 transition-opacity" aria-hidden="true">
            <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
          </div>
          <div class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-2xl sm:w-full">
            <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
              <div class="sm:flex sm:items-start">
                <div class="mt-3 text-center sm:mt-0 sm:text-left w-full">
                  <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">New Board</h3>
                  <div class="mb-4">
                    <label for="boardName" class="block text-sm font-bold mb-2">Name</label>
                    <input type="text" id="boardName" v-model="newBoardName"
                      class="itbkk-board-name shadow-sm p-2 mt-1 block w-full text-base border border-gray-300 rounded-md"
                      placeholder="Enter board name">
                  </div>
                </div>
              </div>
            </div>
            <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
              <button @click="saveNewBoard" :class="{
                'bg-gray-300 text-gray-500 cursor-not-allowed': !newBoardName,
                'bg-indigo-600 text-white hover:bg-indigo-700': newBoardName
              }" :disabled="!newBoardName"
                class="itbkk-button-ok w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm">
                Save
              </button>
              <button @click="closeCreateBoardModal"
                class="itbkk-button-cancel mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">
                Cancel
              </button>
            </div>
          </div>
        </div>
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
              <button class="itbkk-button-edit hover:scale-110 transition-all duration-200 ease-in-out"
                @click.stop="editBoard(board)">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
                  stroke="currentColor">
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
