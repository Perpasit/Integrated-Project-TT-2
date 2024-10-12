<script setup>
import { ref, onMounted, computed, watch } from "vue"
import { useCollabStore } from "../stores/collaboratorStore"
import { useBoardStore } from "../stores/boardStore"
import { useUserStore } from "../stores/userStore"

import { useRoute, useRouter } from "vue-router"

const route = useRoute()
const router = useRouter()
const collabStore = useCollabStore()
const boardStore = useBoardStore()
const userStore = useUserStore()
const collaborators = ref([]) // เปลี่ยนจาก computed เป็น ref ที่สามารถเขียนค่าได้
const isDeleteModalVisible = ref(false)
const showSuccessModal = ref(false)
const showAccessModal = ref(false) // Modal สำหรับการเปลี่ยนสิทธิ์การเข้าถึง
const collaboratorToDelete = ref(null)
const collaboratorToUpdate = ref(null) // Track collaborator สำหรับการเปลี่ยน access
const newAccessRight = ref("") // เก็บสิทธิ์การเข้าถึงใหม่
const boardName = ref("")

const boardId = ref(route.params.boardId) // ใช้ ref แทน const ธรรมดา

const userOid = ref(userStore.getUserOid) // เรียกใช้ property getter ของ userOid
const boardOid = ref(null)

// คำนวณว่า user และ board เป็นเจ้าของเดียวกันหรือไม่
const isSameOid = computed(() => {
  if (!boardOid.value || !userOid.value) {
    return false // คืนค่า false ถ้าไม่มีค่า boardOid หรือ userOid เพื่อให้หน้าโหลดขึ้นมาได้
  }
  return boardOid.value === userOid.value
})

// ตรวจสอบว่าสิทธิ์การเข้าถึงคือ "WRITE"
const hasWriteAccess = computed(() => {
  const collaboratorData = collabStore.getCollaboratorByBoardAndUser(
    userOid.value
  )
  return collaboratorData?.accessRight === "WRITE"
})

// ตรวจสอบว่าผู้ใช้เป็นเจ้าของบอร์ดหรือมีสิทธิ์การเข้าถึงแบบ WRITE
const isSameOidOrHasWriteAccess = computed(() => {
  if (!boardOid.value || !userOid.value) {
    return false
  }
  return boardOid.value === userOid.value || hasWriteAccess.value
})
// ดึงข้อมูล collaborator
const fetchCollaborator = async () => {
  try {
    const collabAll = await collabStore.fetchCollaborators(boardId.value);

    if (Array.isArray(collabAll)) {
      // ล้างค่า collaborators และบังคับให้ Vue ตรวจจับการเปลี่ยนแปลง
      collaborators.value = [];  // ล้างค่าเดิม
      collaborators.value = collabAll.map((collab) => ({
        ...collab,
        accessRight: collab.accessRight,
      }));

      // ตรวจสอบผลลัพธ์ใน console
      console.log('Fetched collaborators:', collaborators.value);
    } else {
      console.error("Unexpected data format:", collabAll);
      collaborators.value = [];
    }
  } catch (error) {
    if (error.response && error.response.status === 404) {
      console.warn("No collaborators found for this board.");
      collaborators.value = [];
    } else {
      console.error("Failed to fetch collaborators:", error);
    }
  }
};



// ดึงข้อมูลบอร์ดและ OID ของผู้ใช้
const fetchBoardAndUserOids = async (boardId) => {
  try {
    const boardData = await boardStore.getBoardById(boardId)
    boardName.value = boardData.name
    if (!boardData) {
      throw new Error(`Board with id ${boardId} not found.`)
    }

    boardOid.value = boardData.owner.oid
    userOid.value = userStore.getUserOid || null

    const visibility = boardData.visibility

    await collabStore.fetchCollaborators(boardId)

    // ตรวจสอบว่า collaborator ถูกดึงมาหรือไม่
    if (!isSameOidOrHasWriteAccess.value && visibility === "PRIVATE") {
      const collaboratorData = await collabStore.getCollaboratorByBoardAndUser(
        userOid.value
      )
      if (
        !collaboratorData ||
        (collaboratorData.accessRight !== "READ" &&
          collaboratorData.accessRight !== "WRITE")
      ) {
        router.push({ name: "AccessDenied" })
      }
    }
  } catch (error) {
    console.error("Error fetching board or user OID:", error)
    router.push({ name: "AccessDenied" })
  }
}

// อัปเดตเมื่อเปลี่ยน boardId
watch(
  () => route.params.boardId,
  async (newBoardId) => {
    boardId.value = newBoardId
    await fetchBoardAndUserOids(newBoardId) // เรียกฟังก์ชันเพื่อ fetch boardOid และ userOid
  }
)

// ยืนยันการเปลี่ยนสิทธิ์การเข้าถึง
const confirmAccessRightChange = (collaborator) => {
  collaboratorToUpdate.value = collaborator
  newAccessRight.value = collaborator.accessRight
  showAccessModal.value = true
}

// อัปเดตสิทธิ์การเข้าถึงของ collaborator
const updateAccessRight = async () => {
  if (collaboratorToUpdate.value) {
    try {
      await collabStore.updateCollaborator(
        boardId.value,
        collaboratorToUpdate.value.oid,
        newAccessRight.value
      )
      showAccessModal.value = false // ปิด modal หลังจากอัปเดตสำเร็จ
      collaboratorToUpdate.value = null // ล้าง collaborator
      await fetchCollaborator() // โหลด collaborator ใหม่
    } catch (error) {
      console.error("Failed to update access right:", error)
    }
  }
}

// ปิด Access Modal
const closeAccessModal = () => {
  showAccessModal.value = false
  fetchCollaborator()
}

// ยืนยันการลบ collaborator
const confirmDelete = (collaborator) => {
  collaboratorToDelete.value = collaborator
  isDeleteModalVisible.value = true
}

// ลบ collaborator
const deleteCollaborator = async () => {
  if (collaboratorToDelete.value) {
    try {
      await collabStore.removeCollaborator(
        boardId.value,
        collaboratorToDelete.value.oid
      )
      isDeleteModalVisible.value = false
      showSuccessModal.value = true
      collaboratorToDelete.value = null
      await fetchCollaborator()
    } catch (error) {
      console.error("Failed to delete collaborator:", error)
    }
  }
}

// ปิด Delete Modal
const closeDeleteModal = () => {
  showSuccessModal.value = false
}

// เรียกฟังก์ชันเมื่อ component ถูก mount
onMounted(() => {
  userStore.loadFromLocalStorage()
  fetchCollaborator()
  fetchBoardAndUserOids(boardId.value)
  console.log("User OID:", userOid.value)
})
</script>

<template>
  <div class="container mx-auto px-4 pt-2">
    <div class="overflow-visible flex flex-col gap-8">
      <div class="flex text-4xl mt-10 text-white justify-center font-extrabold">
        {{ boardName }}
      </div>

      <!-- <div class="flex justify-end mb-6 pb-4 border-b border-white">
      <button @click="openAddCollaboratorModal"
        class="itbkk-button-create relative inline-flex items-center justify-center p-0.5 overflow-hidden text-sm font-medium text-white rounded-lg group bg-gradient-to-br from-[#eddbe8] to-[#e9b9de] group-hover:from-teal-300 transition duration-300 ease-in-out transform hover:scale-105">
        <span
          class="relative px-5 py-2.5 transition-all ease-in duration-75 dark:bg-gray-900 rounded-md group-hover:bg-opacity-0">
          Add Collaborator
        </span>
      </button>
    </div> -->

      <table
        class="table-fixed w-full max-w-6xl mx-auto border-separate border-spacing-y-2"
      >
        <thead>
          <tr class="text-white w-full">
            <th class="w-1/12 py-2 rounded-l-lg text-xl font-medium">No</th>
            <th class="w-1/3 px-4 py-2 text-xl font-medium">Name</th>
            <th class="w-1/3 px-4 py-2 text-xl font-medium">E-mail</th>
            <th class="w-1/6 px-4 py-2 text-xl font-medium">Access Right</th>
            <th class="w-1/6 px-4 py-2 rounded-r-lg text-xl font-medium">
              Action
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(collaborator, index) in collaborators"
            :key="collaborator.oid"
            class="itbkk-item bg-white shadow-lg hover:bg-gray-100 hover:scale-105 transition-all duration-200 ease-in-out cursor-pointer"
          >
            <td class="px-4 py-5 text-center rounded-l-lg">{{ index + 1 }}</td>
            <td class="px-4 py-2">
              <p class="itbkk-title truncate">{{ collaborator.name }}</p>
            </td>
            <td class="px-4 py-2">
              <p class="itbkk-email truncate">{{ collaborator.email }}</p>
            </td>
            <td class="px-4 py-2">
              <div
                :class="{
                  'cursor-not-allowed tooltip tooltip-secondary w-full':
                    !isSameOidOrHasWriteAccess
                }"
                :disabled="!isSameOidOrHasWriteAccess"
                data-tip="You need to be board owner to perform this action."
              >
                <select
                  v-model="collaborator.accessRight"
                  @change="confirmAccessRightChange(collaborator)"
                  class="itbkk-select bg-white border w-full border-gray-300 rounded-md p-2 text-gray-900 items-start focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 ease-in-out"
                  :disabled="!isSameOidOrHasWriteAccess"
                >
                  <option value="READ">Read</option>
                  <option value="WRITE">Write</option>
                </select>
              </div>
            </td>
            <td
              class="itbkk-button-action px-4 py-5 rounded-r-lg flex justify-evenly"
            >
              <button
                :class="{
                  'itbkk-button-delete hover:scale-110 transition-all duration-200 ease-in-out ml-2': true,
                  'text-gray-700 cursor-not-allowed tooltip tooltip-secondary':
                    !isSameOidOrHasWriteAccess
                }"
                @click="isSameOid ? confirmDelete(collaborator) : null"
                :disabled="!isSameOidOrHasWriteAccess"
                data-tip="You need to be board owner to perform this action."
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

    <div
      v-if="collaborators.length <= 0"
      class="text-center mt-4 text-gray-500"
    >
      No Collaborators
    </div>

    <!-- Delete Confirmation Modal -->
    <div
      v-if="isDeleteModalVisible"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
    >
      <div class="bg-white p-8 rounded-lg shadow-lg max-w-md mx-auto">
        <h3 class="text-2xl font-semibold mb-4">Confirm Deletion</h3>
        <p>Are you sure you want to delete {{ collaboratorToDelete?.name }}?</p>
        <div class="flex justify-end mt-6 space-x-3">
          <button
            @click="isDeleteModalVisible = false"
            class="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg"
          >
            Cancel
          </button>
          <button
            @click="deleteCollaborator"
            class="px-4 py-2 bg-red-600 text-white rounded-lg"
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
    <!-- Access Right Change Modal -->
    <div
      v-if="showAccessModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
    >
      <div class="bg-white p-8 rounded-lg shadow-lg max-w-md mx-auto">
        <h3 class="text-2xl font-semibold mb-4">Confirm Access Right Change</h3>
        <p>
          Are you sure you want to change {{ collaboratorToUpdate?.name }}'s
          access right to {{ newAccessRight }}?
        </p>
        <div class="flex justify-end mt-6 space-x-3">
          <button
            @click="closeAccessModal"
            class="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg"
          >
            Cancel
          </button>
          <button
            @click="updateAccessRight"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg"
          >
            Confirm
          </button>
        </div>
      </div>
    </div>

    <router-view />
  </div>
</template>

<style scoped>
/* Custom styles for modal background and transition */
</style>
