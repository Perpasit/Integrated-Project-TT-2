<script setup>
import { ref, computed, watch, onMounted } from "vue"
import { useTaskStore } from "../stores/task"
import { useStatusStore } from "../stores/status"
import { useModalStore } from "../stores/useModalStore"
import { useBoardStore } from "../stores/boardStore"
import { useUserStore } from "../stores/userStore"
import { useGlobalSettingStore } from "../stores/global_setting"
import { useRouter, useRoute } from "vue-router"
import { getItems, getItemsNoUser } from "../utils/fetch"
import { useCollabStore } from "../stores/collaboratorStore"
import { useAttachFileStore } from "../stores/attachFileStore"

const router = useRouter()
const route = useRoute()
const taskStore = useTaskStore()
const statusStore = useStatusStore()
const modalStore = useModalStore()
const boardStore = useBoardStore()
const userStore = useUserStore()
const collabStore = useCollabStore()
const globalSettingStore = useGlobalSettingStore()

const attachFileStore = useAttachFileStore()

const isLoading = ref(false)
const uploadStatus = ref(null)

const uploadStatusClass = computed(() => {
  if (uploadStatus.value === "Files uploaded successfully!") {
    return "success";
  } else if (uploadStatus.value) {
    return "error";
  }
});


// Function to handle file selection
const handleFileChange = (event) => {
  const files = event.target.files;
  attachFileStore.setSelectedFiles(files); // Set files to the store
};
const generateUniqueFileName = (fileName) => {
  const timestamp = Date.now();
  const extension = fileName.split('.').pop();
  const baseName = fileName.replace(`.${extension}`, '');
  return `${baseName}-${timestamp}.${extension}`;
};

const uploadFiles = async () => {
  if (isLoading.value) return; // Prevent multiple uploads

  isLoading.value = true;

  const boardId = route.params.boardId; // Get boardId from URL params
  const taskId = route.params.id; // Get taskId from URL params

  if (!taskId) {
    uploadStatus.value = "Task ID is missing.";
    isLoading.value = false;
    return;
  }

  // Creating new files with unique names
  const filesToUpload = Array.from(attachFileStore.selectedFiles).map(file => {
    const uniqueFileName = generateUniqueFileName(file.name);
    // Create a new file with the same content but a unique name
    return new File([file], uniqueFileName, { type: file.type });
  });

  // Update the store with the newly named files
  attachFileStore.setSelectedFiles(filesToUpload);

  console.log('boardId:', boardId, 'taskId:', taskId); // Debugging output

  // Call the store action to upload files
  try {
    const response = await attachFileStore.uploadFiles(boardId, taskId);
    if (response.status === 201) {
      uploadStatus.value = "Files uploaded successfully!";
    }

  } catch (error) {
    uploadStatus.value = "An error occurred during upload.";
    console.error("Upload error:", error);
  } finally {
    isLoading.value = false; // Reset loading status
  }
};



let boardId = route.params.boardId
const userOid = ref("")
const boardOid = ref(null)
const hasWriteAccess = computed(() => {
  const collaboratorData = collabStore.getCollaboratorByBoardAndUser(
    userOid.value
  )
  return collaboratorData?.accessRight === "WRITE"
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
    if (!boardData) {
      throw new Error(`Board with id ${boardId} not found.`)
    }

    boardOid.value = boardData.owner.oid
    userOid.value = userStore.getUserOid || null
    // const visibility = boardData.visibility;
    // console.log("Board Visibility:", visibility);
    // console.log(isSameOidOrHasWriteAccess.value)

    if (!isSameOidOrHasWriteAccess.value) {
      router.push({ name: "AccessDenied" })
    }
  } catch (error) {
    console.error("Error fetching board or user OID:", error)
    router.push({ name: "AccessDenied" })
  }
}

const generalSetting = computed(
  () => globalSettingStore.getCurrentGlobalSetting
)
const maxTasks = ref(generalSetting.value?.maximumTaskLimit)

const isLimitEnabled = ref(generalSetting.value?.limitMaximumTask)
watch(
  () => route.params.boardId,
  async (newBoardId) => {
    boardId = newBoardId
    await fetchBoardAndUserOids(newBoardId) // เรียกฟังก์ชันเพื่อ fetch boardOid และ userOid
  }
)

watch(
  () => route.params.boardId,
  async (newBoardId) => {
    boardId = newBoardId
    await fetchTaskAndStatus(boardId)
  }
)

watch(generalSetting, (newValue) => {
  isLimitEnabled.value = newValue?.limitMaximumTask
  maxTasks.value = newValue?.maximumTaskLimit
})

const props = defineProps({
  mode: {
    type: String,
    default: "add"
  }
})

const editedTask = ref({
  id: "",
  title: "",
  description: "",
  assignees: "",
  status: null
})

const isTitleTooLong = computed(() => editedTask.value?.title?.length > 100)
const isDescriptionTooLong = computed(
  () => editedTask.value?.description?.length > 500
)
const areAssigneesTooLong = computed(
  () => editedTask.value?.assignees?.length > 30
)

const isTitleValid = computed(() => {
  return (
    editedTask.value.title.trim() !== "" &&
    !isTitleTooLong.value &&
    !isDescriptionTooLong.value &&
    !areAssigneesTooLong.value &&
    (props.mode === "add" ||
      (props.mode === "edit" &&
        editedTask.value &&
        (editedTask.value.title !== originalTask.value.title ||
          editedTask.value.description !== originalTask.value.description ||
          editedTask.value.assignees !== originalTask.value.assignees ||
          editedTask.value.status !== originalTask.value.status)))
  )
})

const originalTask = ref(null)

watch(
  () => router.currentRoute.value.params.id,
  async (id) => {
    if (props.mode === "edit") {
      try {
        let response

        // ตรวจสอบว่า authenticated หรือไม่
        if (userStore.getIsAuthenticated) {
          // ถ้า authenticated ให้เรียก getItems
          response = await getItems(
            `${import.meta.env.VITE_API_ENDPOINTV3
            }/boards/${boardId}/tasks/${id}`
          )
        } else {
          // ถ้าไม่ authenticated ให้เรียก getItemsNoUser
          response = await getItemsNoUser(
            `${import.meta.env.VITE_API_ENDPOINTV3
            }/boards/${boardId}/tasks/${id}`
          )
        }
        if (response.status === 404) {
          editedTask.value = null
          setTimeout(() => {
            closeModal()
          }, 3000)
        } else {
          const data = await response

          const statusId = statusStore.getStatusByName(data.status)?.id
          editedTask.value = {
            ...data,
            description: data.description || "",
            assignees: data.assignees || "",
            status: statusId || 1
          }
          originalTask.value = {
            ...data,
            description: data.description || "",
            assignees: data.assignees || "",
            status: statusId || 1
          }
        }
      } catch (error) {
        console.error("Error fetching task:", error)
        editedTask.value = null
      }
    }
  },
  { immediate: true }
)

function resetForm() {
  editedTask.value = {
    title: "",
    description: "",
    assignees: "",
    status: null
  }
}

async function saveTask() {
  try {
    errorMessage.value = ""

    // ตรวจสอบสิทธิ์เฉพาะเมื่อทำการบันทึก task
    if (!isSameOidOrHasWriteAccess.value) {
      console.error("You don't have permission to edit this task.")
      errorMessage.value = "You don't have permission to edit this task."
      return
    }

    const updatedTask = {
      title: editedTask.value.title.trim(),
      description: editedTask.value.description
        ? editedTask.value.description.trim()
        : "",
      assignees: editedTask.value.assignees
        ? editedTask.value.assignees.trim()
        : "",
      status: { id: editedTask.value.status }
    }

    if (props.mode === "edit") {
      const taskId = router.currentRoute.value.params.id
      if (taskId) {
        await taskStore.updateTask(boardId, taskId, updatedTask)
        fetchTaskAndStatus(boardId)
        modalStore.openTaskSuccessModal(updatedTask.title, props.mode)
      } else {
        console.error("The task does not exist.")
      }
    } else {
      const newTask = {
        title: editedTask.value.title.trim(),
        description: editedTask.value.description
          ? editedTask.value.description.trim()
          : "",
        assignees: editedTask.value.assignees
          ? editedTask.value.assignees.trim()
          : "",
        status: editedTask.value.status
      }
      // console.log(boardId, newTask);
      await taskStore.addTask(boardId, newTask)
      fetchTaskAndStatus(boardId)
      router.push({ name: "home" })
      modalStore.openTaskSuccessModal(newTask.title, props.mode)

      const status = statusStore.statuses.find((s) => s.id === newTask.status)
      if (status) {
        status.noOfTasks++
      }

      closeModal()
    }
    router.push({ name: "home" })
    fetchTaskAndStatus(boardId)
  } catch (error) {
    errorMessage.value = error.message
  }
}

const errorMessage = ref("")

const localTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone
const parseAndFormatDate = (dateString) => {
  const date = new Date(dateString)
  if (isNaN(date)) {
    return "Invalid Date"
  }
  return date.toLocaleString("en-GB", { timeZone: localTimeZone })
}

const formatStatus = (status) => {
  return status
}

function closeModal() {
  router.push({ name: "home" })
  resetForm()
  taskStore.fetchAllTasks(boardId)
}

const getStatusUsage = (statusId) => {
  const status = statusStore.statuses.find((s) => s.id === statusId)
  if (status) {
    if (
      status.name.toLowerCase() === "no status" ||
      status.name.toLowerCase() === "done"
    ) {
      return ""
    }
    return `${status.noOfTasks || 0}/${maxTasks.value || 0} tasks`
  }
  return "0/0 tasks"
}

const fetchTaskAndStatus = async (boardId) => {
  await statusStore.fetchAllStatuses(boardId)
  await taskStore.fetchAllTasks(boardId)
}

onMounted(async () => {
  let boardId = route.params.boardId

  try {
    await fetchTaskAndStatus(boardId)
    const boardData = await boardStore.getBoardById(boardId)
    boardOid.value = boardData.owner.oid
    userOid.value = userStore.getUserOid
    if (props.mode === "add" || !editedTask.value.status) {
      editedTask.value.status = statusStore.statuses[0]?.id || null
    }
  } catch (error) {
    console.error("Error fetching board or user OID:", error)
  }
  await fetchBoardAndUserOids(boardId)
  if (isSameOidOrHasWriteAccess.value) {
    await fetchTaskAndStatus(boardId)
  } else {
    console.warn("You do not have permission to access this board.")
  }
})
</script>

<template>
  <div v-if="editedTask !== null" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>

      <div
        class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-4xl sm:w-full mx-auto">
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-6">
          <div class="itbkk-title text-lg leading-6 font-medium text-gray-900 mb-4">
            {{ mode === "add" ? "New Task" : "Edit Task" }}
          </div>

          <div class="sm:flex sm:items-start">
            <div class="mt-3 text-center sm:mt-5 sm:text-left w-full mr-6 ml-6">
              <div>
                <label class="block text-sm font-bold mb-1">Title:</label>
                <textarea rows="2" v-model="editedTask.title"
                  class="itbkk-title shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"></textarea>
                <p v-if="!editedTask.title" class="text-red-500 text-sm mt-1">
                  Title is required.
                </p>
                <p class="text-gray-500 text-sm mt-1" :class="{ 'text-red-500': isTitleTooLong }">
                  {{ editedTask.title ? editedTask.title.length : 0 }}/100
                  characters
                </p>
              </div>

              <div class="mt-4">
                <label class="block text-sm font-bold mb-1">Description:</label>
                <textarea rows="5" v-model="editedTask.description"
                  class="itbkk-description shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                  placeholder="No Description Provided"></textarea>
                <p class="text-gray-500 text-sm mt-1" :class="{ 'text-red-500': isDescriptionTooLong }">
                  {{
                    editedTask.description ? editedTask.description.length : 0
                  }}/500 characters
                </p>
              </div>

              <div v-if="mode === 'edit'">
                <h2>Attach Files</h2>

                <!-- File input to select multiple files -->
                <input type="file" @change="handleFileChange" multiple />

                <!-- Display selected files with a dark background and close button -->
                <div v-if="attachFileStore.selectedFiles.length">
                  <h3>Selected Files:</h3>
                  <div class="space-y-2">
                    <div v-for="(file, index) in attachFileStore.selectedFiles" :key="index"
                      class="flex justify-between items-center bg-gray-200 text-black p-2 ">
                      <span class="text-sm">{{ file.name }}</span>
                      <button type="button" class="text-red-500" @click="attachFileStore.removeFile(index)">
                        X
                      </button>
                    </div>
                  </div>
                </div>

                <!-- Upload button -->
                <button @click="uploadFiles" :disabled="isLoading">
                  Upload Files
                </button>

                <!-- Loading message -->
                <div v-if="isLoading">Uploading...</div>

                <!-- Success or error message -->
                <div v-if="uploadStatus" :class="uploadStatusClass">
                  {{ uploadStatus }}
                </div>
              </div>



            </div>

            <div class="mt-3 text-center sm:mt-2 sm:text-left w-1/2 mr-6 ml-6">
              <div class="mt-2.5 mb-4">
                <label class="block text-sm font-bold mb-1">Assignees:</label>
                <textarea rows="6" v-model="editedTask.assignees"
                  class="itbkk-assignees shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                  placeholder="Unassigned"></textarea>
                <p class="text-gray-500 text-sm mt-1" :class="{ 'text-red-500': areAssigneesTooLong }">
                  {{
                    editedTask.assignees ? editedTask.assignees.length : 0
                  }}/30 characters
                </p>
              </div>

              <div class="mt-2.5 mb-4">
                <label class="block text-sm font-bold mb-1">Status:</label>
                <select v-model="editedTask.status"
                  class="itbkk-status shadow-sm p-3 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md">
                  <option v-for="status in statusStore.statuses" :key="status.id" :value="status.id">
                    {{ formatStatus(status.name) }}
                  </option>
                </select>
              </div>

              <div v-if="mode === 'add' || mode === 'edit'" class="text-sm text-gray-500 mb-4">
                <template v-if="isLimitEnabled">
                  <p class="text-green-500">(Limit is enabled)</p>
                  <template v-if="getStatusUsage(editedTask.status) !== ''">
                    <p>Status usage: {{ getStatusUsage(editedTask.status) }}</p>
                  </template>
                </template>
                <template v-else>
                  <p class="text-red-500">(Limit is disabled)</p>
                </template>
              </div>

              <div v-if="mode === 'edit'">
                <div class="mt-4">
                  <label class="itbkk-timezone block text-sm font-bold mb-1">TimeZone : {{ localTimeZone }}</label>
                </div>
                <div class="mt-4">
                  <label class="itbkk-created-on block text-sm font-bold mb-1">Created On :
                    {{ parseAndFormatDate(editedTask.createdOn) }}</label>
                </div>
                <div class="mt-4">
                  <label class="itbkk-updated-on block text-sm font-bold mb-1">Updated On :
                    {{ parseAndFormatDate(editedTask.updatedOn) }}</label>
                  <p class="mt-1 text-sm text-gray-900"></p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <div class="flex space-x-4">
            <!-- Button for non-board owners (when OIDs are not the same) -->
            <button v-if="isSameOidOrHasWriteAccess" type="button"
              class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
              :class="{
                'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500':
                  isTitleValid,
                'bg-gray-300 cursor-not-allowed': !isTitleValid
              }" @click="isTitleValid && saveTask()" :disabled="!isTitleValid ||
                isTitleTooLong ||
                isDescriptionTooLong ||
                areAssigneesTooLong
                ">
              {{ mode === "add" ? "Add" : "Save" }}
            </button>

            <!-- Button for board owners (when OIDs are the same) -->
            <button v-if="!isSameOidOrHasWriteAccess" type="button"
              class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm tooltip tooltip-secondary tooltip-left"
              :class="{
                'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500':
                  isSameOidOrHasWriteAccess,
                'bg-gray-300 cursor-not-allowed': !isSameOidOrHasWriteAccess
              }" @click="isSameOidOrHasWriteAccess && saveTask()" :disabled="!isSameOidOrHasWriteAccess"
              data-tip="You need to be the board owner to perform this action.">
              {{ mode === "add" ? "Add" : "Save" }}
            </button>
          </div>
          <button type="button"
            class="itbkk-button-cancel mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            @click="closeModal">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>
      <div class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full mx-auto">
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="sm:flex sm:items-start">
            <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
              <h3 class="itbkk-message text-lg leading-6 font-medium text-red-500">
                The task does not exist.
              </h3>
            </div>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button type="button"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm"
            @click="closeModal">
            OK
          </button>
        </div>
      </div>
    </div>
  </div>

  <div v-if="errorMessage" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>
      <div class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full mx-auto">
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="sm:flex sm:items-start">
            <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
              <h3 class="text-lg leading-6 font-medium text-red-500">Error</h3>
              <div class="mt-2">
                <p class="itbkk-message text-sm leading-5 text-red-500">
                  {{ errorMessage }}
                </p>
              </div>
            </div>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button type="button"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm"
            @click="errorMessage = ''">
            OK
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>