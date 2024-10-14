<script setup>
import { computed, ref, onMounted, watch } from "vue"
import { useRouter, useRoute } from "vue-router"
import { useUserStore } from "../stores/userStore"
import { useBoardStore } from "../stores/boardStore"
import { getItems, getItemsNoUser } from "../utils/fetch"
import { useCollabStore } from "../stores/collaboratorStore"

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const boardStore = useBoardStore()
const collabStore = useCollabStore()
const boardId = route.params.boardId
const userOid = ref("")
const boardOid = ref(null)

const hasWriteAccess = computed(() => {
  const collaboratorData = collabStore.getCollaboratorByBoardAndUser(
    userOid.value
  )
  return collaboratorData?.accessRight === "WRITE"
})
const isSameOid = computed(() => {
  if (!boardOid.value || !userOid.value) {
    return false // Return false ถ้าไม่มีค่า boardOid หรือ userOid เพื่อให้หน้าโหลดขึ้นมาได้
  }
  return boardOid.value === userOid.value
})
const isSameOidOrHasWriteAccess = computed(() => {
  if (!boardOid.value || !userOid.value) {
    return false
  }

  return boardOid.value === userOid.value || hasWriteAccess.value
})

const fetchBoardAndUserOids = async (boardId) => {
  try {
    const boardData = await boardStore.getBoardById(boardId)
    // console.log(boardData)
 
    if (!boardData) {
      throw new Error(`Board with id ${boardId} not found.`)
    }

    boardOid.value = boardData.owner.oid
    userOid.value = userStore.getUserOid || null

    // console.log("Board OID:", boardOid.value, "User OID:", userOid.value)

    const visibility = boardData.visibility

    await collabStore.fetchCollaborators(boardId)

    // ตรวจสอบว่า collaborator ถูกดึงมาหรือไม่
    // console.log("Collaborators:", collabStore.collabs);

    // ตรวจสอบว่าเป็นบอร์ด Private และผู้ใช้ไม่ใช่เจ้าของบอร์ด
    if (!isSameOidOrHasWriteAccess.value && visibility === "PRIVATE") {
      // ตรวจสอบว่าผู้ใช้มีสิทธิ์การเข้าถึงแบบ READ หรือ WRITE หรือไม่
      const collaboratorData = await collabStore.getCollaboratorByBoardAndUser(
        userOid.value
      )

      // console.log(collaboratorData)
      if (
        !collaboratorData ||
        (collaboratorData.accessRight !== "READ" &&
          collaboratorData.accessRight !== "WRITE")
      ) {
        // ถ้าไม่มีสิทธิ์ทั้ง READ และ WRITE ให้ redirect ไปที่หน้า AccessDenied
        // console.log(collaboratorData)
        router.push({ name: "AccessDenied" })
      }
    }
  } catch (error) {
    console.error("Error fetching board or user OID:", error);
    router.push({ name: "AccessDenied" });
  }
}
watch(
  () => route.params.boardId,
  async (newBoardId) => {
    boardId = newBoardId
    await fetchBoardAndUserOids(newBoardId) // เรียกฟังก์ชันเพื่อ fetch boardOid และ userOid
  }
)

const isManageStatusPage = computed(() => {
  return router.currentRoute.value.path.includes("/status")
})
const isCollaboratorPage = computed(() => {
  return router.currentRoute.value.path.includes("/collab")
})

const isTaskPage = computed(() => {
  return router.currentRoute.value.path.includes("/task")
})

const addTask = () => {
  router.push({ name: "AddTask" })
}

const goHome = () => {
  router.push({ name: "home", params: { boardId } })
}

const goToManageStatus = () => {
  router.push({ name: "ManageStatus", params: { boardId } })
}

const addStatus = () => {
  router.push({ name: "AddStatus" })
}
const addCollab = () => {
  router.push({ name: "AddCollabManage", params: { boardId } })
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
  router.push({ name: "Board" })
}

const goToCollaborator = () => {
  router.push({ name: "CollabManage", params: { boardId } })
}
onMounted(async () => {
  userStore.loadFromLocalStorage()
  const boardId = route.params.boardId
  // console.log("Board ID from route:", boardId)
  // console.log("User is authenticated:", userStore.getIsAuthenticated)

  try {
    await fetchBoardAndUserOids(boardId)
  } catch (error) {
    console.error("Error during component mount:", error)
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
        @click="goToBoard"
      >
        Kanban
      </h1>
      <span
        :class="[
          'itbkk-button-home text-2xl  cursor-pointer px-3 rounded hover:scale-110 transition-all duration-200 ease-in-out  flex justify-end items-end ',
          isTaskPage ? 'text-white' : 'text-blue-600 hover:text-white'
        ]"
        @click="goHome"
      >
        Home
      </span>
      <span
        :class="[
          'itbkk-manage-status text-2xl cursor-pointer  px-3 rounded hover:scale-110 transition-all duration-200 ease-in-out flex justify-end items-end',
          isManageStatusPage ? 'text-white' : 'text-blue-600 hover:text-white'
        ]"
        @click="goToManageStatus"
      >
        Manage Status
      </span>
      <span
        :class="[
          'itbkk-manage-status text-2xl cursor-pointer  px-3 rounded hover:scale-110 transition-all duration-200 ease-in-out flex justify-end items-end',
          isCollaboratorPage ? 'text-white' : 'text-blue-600 hover:text-white'
        ]"
        @click="goToCollaborator"
      >
        Collaborator
      </span>
    </div>

    <div class="flex items-end space-x-4">
      <div class="account px-4 pt-4 pr-3 flex items-center">
        <div class="flex items-end space-x-4 relative">
          <span
            class="itbkk-fullname text-blue-600 text-xl items-end font-medium"
          >
            {{ accountName }}
          </span>

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
              class="text-gray-700 hover:text-white hover:bg-gray-700 px-2 py-2 rounded transition duration-150 ease-in-out w-full text-left"
            >
              Board List
            </button>
            <!-- <button
              @click="goToCollaborator"
              class="text-gray-700 hover:text-white hover:bg-gray-700 px-2 py-2 rounded transition duration-150 ease-in-out w-full text-left"
            >
              Collaborator
            </button> -->
            <button
              @click="logout"
              class="text-gray-700 hover:text-white hover:bg-gray-700 px-2 py-2 rounded transition duration-150 ease-in-out w-full text-left"
            >
              Logout
            </button>
          </div>
          <!-- <div
        @click="goToCollaborator"
        class="border p-2 rounded-full cursor-pointer flex items-center justify-center shadow-md hover:scale-110 transition-all duration-200 ease-in-out">
        <svg xmlns="http://www.w3.org/2000/svg"  fill="currentColor" class="bi bi-person-add h-6 w-6 text-slate-500" viewBox="0 0 16 16">
          <path d="M12.5 16a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7m.5-5v1h1a.5.5 0 0 1 0 1h-1v1a.5.5 0 0 1-1 0v-1h-1a.5.5 0 0 1 0-1h1v-1a.5.5 0 0 1 1 0m-2-6a3 3 0 1 1-6 0 3 3 0 0 1 6 0M8 7a2 2 0 1 0 0-4 2 2 0 0 0 0 4"/>
          <path d="M8.256 14a4.5 4.5 0 0 1-.229-1.004H3c.001-.246.154-.986.832-1.664C4.484 10.68 5.711 10 8 10q.39 0 .74.025c.226-.341.496-.65.804-.918Q8.844 9.002 8 9c-5 0-6 3-6 4s1 1 1 1z"/>
        </svg>
      </div> -->
        </div>
      </div>

      <button
        v-show="isTaskPage"
        @click="isSameOidOrHasWriteAccess ? addTask() : null"
        :class="{
          'itbkk-button-add hover:scale-110 transition-all duration-200 ease-in-out ml-4 bg-transparent hover:bg-gradient-to-r from-cyan-500 to-blue-500 text-blue-700 font-semibold hover:text-white py-3 px-5 border border-blue-500 shadow-lg shadow-blue-500/50 hover:border-transparent rounded': true,
          ' text-blue-700 cursor-not-allowed tooltip tooltip-secondary tooltip-bottom':
            !isSameOidOrHasWriteAccess
        }"
        :disabled="!isSameOidOrHasWriteAccess"
        data-tip="You need to be board owner to perform this action."
      >
        + NEW TASK
      </button>
      <button
        v-show="isManageStatusPage"
        @click="isSameOidOrHasWriteAccess ? addStatus() : null"
        :class="{
          'itbkk-button-add hover:scale-110 transition-all duration-200 ease-in-out ml-4 bg-transparent hover:bg-gradient-to-r from-orange-300 to-amber-300 text-orange-700 font-semibold hover:text-white py-3 px-5 border border-orange-500 shadow-lg shadow-amber-500 hover:border-transparent rounded': true,
          'text-orange-700 cursor-not-allowed tooltip tooltip-secondary tooltip-bottom':
            !isSameOidOrHasWriteAccess
        }"
        :disabled="!isSameOidOrHasWriteAccess"
        data-tip="You need to be board owner to perform this action."
      >
        ADD STATUS
      </button>
      <button
        v-show="isCollaboratorPage"
        @click="isSameOid ? addCollab() : null"
        :class="{
          'itbkk-button-add hover:scale-110 transition-all duration-200 ease-in-out ml-4 bg-transparent hover:bg-gradient-to-r from-orange-300 to-amber-300 text-orange-700 font-semibold hover:text-white py-3 px-5 border border-orange-500 shadow-lg shadow-amber-500 hover:border-transparent rounded': true,
          'text-orange-700 cursor-not-allowed tooltip tooltip-secondary tooltip-bottom':
            !isSameOid
        }"
        :disabled="!isSameOid"
        data-tip="You need to be board owner to perform this action."
      >
        ADD COLLAB
      </button>
    </div>
  </div>
</template>
