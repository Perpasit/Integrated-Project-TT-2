<script setup>
import { computed, ref, onMounted } from "vue"
import { useRouter, useRoute } from "vue-router"
import { useUserStore } from "../stores/userStore"
import { useBoardStore } from "../stores/boardStore";

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const boardStore = useBoardStore();

const userOid = computed(() => userStore.getUserOid);
const boardOid = ref(null);
const isSameOid = computed(() => {
  if (!boardOid.value || !userOid.value) {
    return true; // Return false ถ้าไม่มีค่า boardOid หรือ userOid เพื่อให้หน้าโหลดขึ้นมาได้
  }
  return boardOid.value !== userOid.value;
});
const fetchBoardAndUserOids = async (boardId) => {
  try {
    // ดึงข้อมูลบอร์ดจาก API หรือ state ถ้ายังไม่มี
    let boardData = await boardStore.getBoardById(boardId);
    if (!boardData) {
      throw new Error(`Board with id ${boardId} not found.`);
    }

    // เก็บค่า boardOid
    boardOid.value = boardData.owner.oid;

    // ตรวจสอบว่ามีค่า userOid หรือไม่ ถ้าไม่มีให้กำหนดเป็น null
    userOid.value = userStore.getUserOid || null;
  } catch (error) {
    console.error("Error fetching board or user OID:", error);
  }
};

const isManageStatusPage = computed(() => {
  return router.currentRoute.value.path.includes("/status")
})

const isTaskPage = computed(() => {
  return router.currentRoute.value.path.includes("/task")
})

const addTask = () => {
  router.push({ name: "AddTask" })
}
const boardId = route.params.boardId
const goHome = () => {
  router.push({ name: "home", params: { boardId } })
}

const goToManageStatus = () => {
  router.push({ name: "ManageStatus", params: { boardId } })
  
}

const addStatus = () => {
  router.push({ name: "AddStatus" })
}

const accountName = computed(() => userStore.getAccountName)
const showDropdown = ref(false)

const logout = () => {
  userStore.logout()
  router.push({ name: "Login" })
}

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const goToBoard = () => {
  router.push("/board")
}

onMounted(async() => {
try {
    // Fetch board details and statuses after token validation
    await fetchBoardAndUserOids(boardId);
  } catch (error) {
    console.error("Error during component mount:", error);
  }
})
</script>

<template>
  <div
    class="container mx-auto px-0 pt-5 flex flex-row justify-around items-center"
  >
    <div class="flex">
      <h1
        class="cursor-default text-6xl font-bold font-italiana pt-2 text-lime-100 hover:scale-110 transition-all duration-200 ease-in-out mr-4"
        @click="goHome"
      >
        Kanban
      </h1>
      <span
        :class="[
          'itbkk-button-home text-2xl  cursor-pointer px-3 rounded hover:scale-110 transition-all duration-200 ease-in-out  flex justify-end items-end ',
          isTaskPage ? 'text-white' : 'text-blue-600 hover:text-white',
        ]"
        @click="goHome"
      >
        Home
      </span>
      <span
        :class="[
          'itbkk-manage-status text-2xl cursor-pointer  px-3 rounded hover:scale-110 transition-all duration-200 ease-in-out flex justify-end items-end',
          isManageStatusPage ? 'text-white' : 'text-blue-600 hover:text-white',
        ]"
        @click="goToManageStatus"
      >
        Manage Status
      </span>
    </div>

    <div class="flex items-end space-x-4">
      <div class="account px-4 pt-4 pr-3 flex items-center">
        <div class="flex items-end space-x-4 relative">
          <span
            class="itbkk-fullname text-blue-600 text-xl items-end font-medium"
          >
            {{ accountName }}</span
          >
          <div
            class="border p-2 rounded-full cursor-pointer flex items-center justify-center shadow-md hover:scale-110 transition-all duration-200 ease-in-out"
            @click="toggleDropdown"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-6 w-6 text-slate-500"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
              />
            </svg>
          </div>

          <div
            v-if="showDropdown"
            class="absolute top-full right-0 mt-2 w-32 bg-white shadow-lg rounded-lg p-2"
          >
            <button
              @click="goToBoard"
              class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left"
            >
              Board List
            </button>
            <button
              @click="goToProfile"
              class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left"
            >
              Profile
            </button>
            <button
              @click="goToSettings"
              class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left"
            >
              Settings
            </button>
            <button
              @click="logout"
              class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left"
            >
              Logout
            </button>
          </div>
        </div>
      </div>

      <button
        v-show="isTaskPage"
        @click="addTask"
        class="itbkk-button-add hover:scale-110 transition-all duration-200 ease-in-out ml-4 bg-transparent text-blue-700 font-semibold py-3 px-5 border border-blue-500  shadow-lg shadow-blue-500/50 rounded"
        :class="{
            'hover:bg-gradient-to-r from-cyan-500 to-blue-500 hover:text-white hover:border-transparent   ': !isSameOid,
            'cursor-not-allowed tooltip tooltip-secondary tooltip-bottom':
              isSameOid, // Grey and disabled when OID is the same
              }"
        :disabled="isSameOid"
        data-tip="You need to be the board owner to perform this action." 
      >
        + NEW TASK
      </button>
      <button
        v-show="isManageStatusPage"
        @click="addStatus"
        class="itbkk-button-add hover:scale-110 transition-all duration-200 ease-in-out ml-4 bg-transparent  text-orange-700 font-semibold  py-3 px-5 border border-orange-500 shadow-lg shadow-amber-500 rounded"
        :class="{
            'hover:bg-gradient-to-r from-orange-300 to-amber-300 hover:text-white hover:border-transparent   ': !isSameOid,
            'cursor-not-allowed tooltip tooltip-secondary tooltip-bottom':
              isSameOid, // Grey and disabled when OID is the same
              }"
        :disabled="isSameOid"
        data-tip="You need to be the board owner to perform this action."  
      >
        ADD STATUS
      </button>
    </div>
  </div>
</template>
