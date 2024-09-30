<script setup>
import { ref, computed, onMounted, watch } from "vue"
import { useGlobalSettingStore } from "../stores/global_setting"
import { useBoardStore } from "../stores/boardStore"
import { useUserStore } from "../stores/userStore"
import { useRoute } from "vue-router"

const showConfirmationModal = ref(false)
const globalSettingStore = useGlobalSettingStore()
const confirmationMessage = ref("")
const boardStore = useBoardStore()
const userStore = useUserStore()
const route = useRoute()

const boardId = route.params.boardId
const userOid = ref("")
const boardOid = ref(null); 
const isSameOid = computed(() => {
  if (!boardOid.value || !userOid.value) {
    return false; // Return false ถ้าไม่มีค่า boardOid หรือ userOid เพื่อให้หน้าโหลดขึ้นมาได้
  }
  return boardOid.value === userOid.value;
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
watch(
  () => route.params.boardId,
  async (newBoardId) => {
    boardId = newBoardId;
    await fetchBoardAndUserOids(newBoardId); // เรียกฟังก์ชันเพื่อ fetch boardOid และ userOid
  }
);
const generalSetting = computed(
  () => globalSettingStore.getCurrentGlobalSetting
)

const maxTasks = ref(generalSetting.value?.maximumTaskLimit)
const isLimitEnabled = ref(generalSetting.value?.limitMaximumTask)

const props = defineProps({
  show: {
    type: Boolean,
    required: true,
  },
  currentTaskCounts: {
    type: Object,
    required: true,
  },
  currentStatusNames: {
    type: Array,
    required: true,
  },
})

const emit = defineEmits(["close", "save"])

const isMaxTasksValid = computed(() => {
  return (
    (isLimitEnabled.value !==
      globalSettingStore.getCurrentGlobalSetting.limitMaximumTask ||
      maxTasks.value !==
        globalSettingStore.getCurrentGlobalSetting.maximumTaskLimit) &&
    maxTasks.value >= 1 &&
    maxTasks.value <= 100
  )
})

watch(
  () => [maxTasks.value, isLimitEnabled.value],
  () => {
    emit("save", {
      maxTasks: maxTasks.value,
      isLimitEnabled: isLimitEnabled.value,
    })
  }
)

function saveIsLimitEnabledToLocalStorage(value) {
  localStorage.setItem("isLimitEnabled", value)
}

function closeModal() {
  isLimitEnabled.value =
    globalSettingStore.getCurrentGlobalSetting?.limitMaximumTask
  maxTasks.value =
    globalSettingStore.getCurrentGlobalSetting?.maximumTaskLimit || 10
  emit("close")
}

function showConfirmation(isLimitEnabled) {
  showConfirmationModal.value = true
  if (isLimitEnabled) {
    confirmationMessage.value = `The Kanban board now limits ${maxTasks.value} tasks in each status.`
  } else {
    confirmationMessage.value =
      "The Kanban board has disabled the task limit in each status."
  }
  setTimeout(() => {
    showConfirmationModal.value = false
  }, 2000)
}

async function saveMaxTasks() {
  if (isMaxTasksValid.value) {
    const updatedSetting = {
      maximumTaskLimit: maxTasks.value,
      limitMaximumTask: isLimitEnabled.value,
    }
    await globalSettingStore.updateGlobalSetting("General", updatedSetting)
    saveIsLimitEnabledToLocalStorage(isLimitEnabled.value)
    emit("save", {
      maxTasks: maxTasks.value,
      isLimitEnabled: isLimitEnabled.value,
    })
    closeModal()
    showConfirmation(isLimitEnabled.value) // Pass isLimitEnabled value
  }
}

function toggleLimitEnabled() {
  isLimitEnabled.value = !isLimitEnabled.value
  globalSettingStore.setIsLimitEnabled(isLimitEnabled.value)
  saveIsLimitEnabledToLocalStorage(isLimitEnabled.value)
}

const limitReachedStatusesWithCounts = computed(() => {
  return props.currentStatusNames
    .filter((statusName) => {
      const taskCount = props.currentTaskCounts[statusName]
      return (
        taskCount >= maxTasks.value &&
        statusName !== "NO STATUS" &&
        statusName !== "DONE"
      )
    })
    .map(
      (statusName) =>
        `"${statusName}"(${props.currentTaskCounts[statusName]}Task)`
    )
})
const isTaskLimitReached = computed(() => {
  if (!isLimitEnabled.value) return false
  return limitReachedStatusesWithCounts.value.length > 0
})

const closeConfirmationModal = () => {
  showConfirmationModal.value = false
}
onMounted(async () => {
  const boardId = route.params.boardId
  
  try {
    await globalSettingStore.fetchGlobalSetting("General")
    const currentGlobalSetting = globalSettingStore.getCurrentGlobalSetting
    const limitMaximumTask = currentGlobalSetting?.limitMaximumTask
    const maximumTaskLimit = currentGlobalSetting?.maximumTaskLimit

    isLimitEnabled.value = limitMaximumTask
    maxTasks.value = maximumTaskLimit || 10
    globalSettingStore.setIsLimitEnabled(isLimitEnabled.value)
    saveIsLimitEnabledToLocalStorage(isLimitEnabled.value)

    watch(
      () => currentGlobalSetting?.limitMaximumTask,
      (newValue) => {
        isLimitEnabled.value = newValue
        globalSettingStore.setIsLimitEnabled(newValue)
        saveIsLimitEnabledToLocalStorage(newValue)
      }
    )
  } catch (error) {
    console.error("Error fetching global setting:", error)
  }
  await fetchBoardAndUserOids(boardId);
  try {
    const boardData = await boardStore.getBoardById(boardId);
    boardOid.value = boardData.owner.oid;
    userOid.value = userStore.getUserOid;
  } catch (error) {
    console.error("Error fetching board or user OID:", error);
  }
})
</script>

<template>
  <div
    v-if="show"
    class="itbkk-modal-setting fixed z-10 inset-0 overflow-y-auto"
  >
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>
      <div
        class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full mx-auto"
      >
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="sm:flex sm:items-start">
            <div class="mt-3 text-center sm:mt-0 sm:text-left w-full">
              <h3 class="text-lg leading-6 font-medium text-gray-900">
                Status Settings
              </h3>
              <div class="mt-4">
                <label class="flex items-center mb-2">
                  <label
                    class="tbkk-limit-task inline-flex items-center cursor-pointer"
                  >
                    <input
                      type="checkbox"
                      class="itbkk-limit-task sr-only peer"
                      :checked="isLimitEnabled"
                      @change="toggleLimitEnabled"
                    />
                    <div
                      class="relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-200 peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"
                    ></div>
                  </label>

                  <span class="ml-2 text-gray-700"
                    >Limit tasks in this status</span
                  >
                </label>
                <div v-if="isLimitEnabled">
                  <input
                    type="number"
                    v-model.number="maxTasks"
                    class="itbkk-max-task shadow-sm p-2 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                    min="1"
                    max="100"
                  />
                  <p class="text-gray-500 text-sm mt-1">
                    Users can limit the task in a status by setting maximum
                    tasks in each status (except "No Status" and "Done"
                    statuses).
                  </p>
                  <br />
                  <p class="text-sm">
                    The Kanban board will limit the number of tasks in each
                    status to {{ maxTasks }}
                  </p>
                </div>
                <div
                  v-if="isTaskLimitReached"
                  class="mt-2 bg-yellow-100 border-l-4 border-yellow-400 p-4"
                >
                  <p class="text-sm text-yellow-700">
                    The status <br />
                    {{ limitReachedStatusesWithCounts.join(", ") }} <br />
                    These Status has reached the task limit ({{ maxTasks }}). No
                    additional tasks can be added to this status.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <div class="flex space-x-4">
            <!-- Button for isMaxTasksValid, only show when OIDs are not the same -->
            <button
              v-if="isSameOid"
              type="button"
              class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
              :class="{
                'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500':
                  isMaxTasksValid,
                'bg-gray-300 cursor-not-allowed': !isMaxTasksValid,
              }"
              @click="isMaxTasksValid && saveMaxTasks()"
              :disabled="!isMaxTasksValid"
            >
              Save
            </button>

            <!-- Button for Board Owner Only -->
            <button
              v-if="!isSameOid"
              type="button"
              class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm tooltip tooltip-secondary tooltip-left"
              :class="{
                'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500':
                  isSameOid,
                'bg-gray-300 cursor-not-allowed': !isSameOid,
              }"
              @click="isSameOid && saveMaxTasks()"
              :disabled="!isSameOid"
              data-tip="You need to be the board owner to perform this action."
            >
              Save 
            </button>
          </div>
          <button
            type="button"
            class="itbkk-button-cancel mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            @click="closeModal"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>

  <div v-if="showConfirmationModal" class="fixed z-50 inset-0 overflow-y-auto">
    <div class="flex items-start justify-center min-h-screen">
      <div
        class="bg-white mt-2 rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full"
      >
        <div class="bg-white px-4 sm:p-4 sm:pb-4 py-0">
          <div class="flex justify-between items-center">
            <div class="text-lg leading-6 font-medium text-gray-900">
              Limit Updated
            </div>
            <button
              type="button"
              class="text-gray-400 hover:text-gray-500 focus:outline-none focus:text-gray-500 transition ease-in-out duration-150"
              @click="closeConfirmationModal()"
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
              {{ confirmationMessage }}
            </p>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-1 sm:px-6 sm:flex sm:flex-row-reverse">
          <span class="flex w-full rounded-md shadow-sm sm:ml-3 sm:w-auto">
            <button
              type="button"
              class="inline-flex justify-center w-full rounded-md border border-transparent px-4 py-2 bg-indigo-600 text-base leading-6 font-medium text-white shadow-sm hover:bg-indigo-500 focus:outline-none focus:border-indigo-700 focus:shadow-outline-indigo transition ease-in-out duration-150 sm:text-sm sm:leading-3"
              @click="closeConfirmationModal()"
            >
              OK
            </button>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
