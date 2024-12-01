<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useTaskStore } from "../stores/task";
import { useStatusStore } from "../stores/status";
import { useModalStore } from "../stores/useModalStore";
import { useBoardStore } from "../stores/boardStore";
import { useUserStore } from "../stores/userStore";
import { useGlobalSettingStore } from "../stores/global_setting";
import { useRouter, useRoute } from "vue-router";
import { getItems, getItemsNoUser } from "../utils/fetch";
import { useCollabStore } from "../stores/collaboratorStore";
import { useAttachFileStore } from "../stores/attachFileStore";
import { storeToRefs } from "pinia";

const router = useRouter();
const route = useRoute();
const taskStore = useTaskStore();
const statusStore = useStatusStore();
const modalStore = useModalStore();
const boardStore = useBoardStore();
const userStore = useUserStore();
const collabStore = useCollabStore();
const globalSettingStore = useGlobalSettingStore();

const attachFileStore = useAttachFileStore();

const isLoading = ref(false);
const uploadStatus = ref(null);

const attachmentsFileNames = ref([]);
const { attachments } = storeToRefs(attachFileStore);

const getAttachmentsFileName = async (boardId, taskId) => {
  if (!taskId) {
    return;
  }

  const attachments = await attachFileStore.getAttachments(boardId, taskId);

  if (Array.isArray(attachments) && attachments.length > 0) {
    const fileNames = attachments.map((attachment) => attachment.fileName);
    attachmentsFileNames.value = fileNames;
  } else {
    attachmentsFileNames.value = [];
  }
};

const uploadStatusClass = computed(() => {
  if (uploadStatus.value === "Files uploaded successfully!") {
    return "success";
  } else if (uploadStatus.value) {
    return "error";
  }
});

// Initialize attachFile as a ref
const attachFile = ref([]);

// Watch for changes in attachFileStore.selectedFiles and update attachFile
watch(
  () => attachFileStore.selectedFiles,
  (newFiles) => {
    attachFile.value = newFiles;

  },
  { immediate: true }
);
// Function to handle file selection
const handleFileChange = (event) => {
  const files = event.target.files;
  attachFileStore.setSelectedFiles(files); // Set files to the store
  // console.log('Selected files:', attachFileStore.selectedFiles);
};

const uploadFiles = async () => {
  if (isLoading.value) return;

  isLoading.value = true;

  const boardId = route.params.boardId;
  const taskId = route.params.id;

  if (!taskId) {
    uploadStatus.value = "Task ID is missing.";
    isLoading.value = false;
    return;
  }

  const filesToUpload = Array.from(attachFileStore.selectedFiles).map(
    (file) => {
      console.log(file);
      return new File([file], file.name, { type: file.type });
    }
  );

  attachFileStore.setSelectedFiles(filesToUpload);

  try {
    const uploadedFile = await attachFileStore.uploadFiles(boardId, taskId);
    getAttachmentsFileName(boardId, taskId);
    if (uploadedFile) {
      uploadStatus.value = "Files uploaded successfully!";
    } else {
      uploadStatus.value = attachFileStore.uploadStatus;
    }
  } catch (error) {
    uploadStatus.value = "An error occurred during upload.";
  } finally {
    isLoading.value = false;
  }
};

let attachmentsToDelete = ref([]);

const handleDeleteAttachment = (index) => {
  const attachmentId = attachFileStore.attachments[index]?.id;
  attachFileStore.deleteAttachmentF(attachmentId);
  if (attachmentId) {
    attachmentsToDelete.value.push(attachmentId);
    console.log(`Attachment with ID ${attachmentId} marked for deletion.`);
  } else {
    console.error("Attachment ID not found.");
  }
};

const handleDownloadAttachment = async (index) => {
  const boardId = route.params.boardId;
  const taskId = route.params.id;
  const attachmentId = attachFileStore.attachments[index]?.id;

  console.log("Downloading attachment:", { boardId, taskId, attachmentId });

  if (!attachmentId) {
    console.error("Attachment ID not found.");
    return;
  }

  try {
    const success = await attachFileStore.downloadAttachment(
      boardId,
      taskId,
      attachmentId
    );

    if (success) {
      console.log(
        `Attachment with ID ${attachmentId} downloaded successfully.`
      );
    } else {
      console.error("Failed to download the attachment.");
      alert("Failed to download the attachment. Please try again.");
    }
  } catch (error) {
    console.error("An error occurred while downloading the attachment:", error);
    alert("An error occurred while downloading the file. Please try again.");
  }
};

let boardId = route.params.boardId;
const userOid = ref("");
const boardOid = ref(null);
const hasWriteAccess = computed(() => {
  const collaboratorData = collabStore.getCollaboratorByBoardAndUser(
    userOid.value
  );
  return collaboratorData?.accessRight === "WRITE";
});

const isSameOidOrHasWriteAccess = computed(() => {
  if (!boardOid.value || !userOid.value) {
    return false;
  }

  return boardOid.value === userOid.value || hasWriteAccess.value;
});
const fetchBoardAndUserOids = async (boardId) => {
  try {
    const boardData = await boardStore.getBoardById(boardId);
    if (!boardData) {
      throw new Error(`Board with id ${boardId} not found.`);
    }

    boardOid.value = boardData.owner.oid;
    userOid.value = userStore.getUserOid || null;
    // const visibility = boardData.visibility;
    // console.log("Board Visibility:", visibility);
    // console.log(isSameOidOrHasWriteAccess.value)

    if (!isSameOidOrHasWriteAccess.value) {
      router.push({ name: "AccessDenied" });
    }
  } catch (error) {
    console.error("Error fetching board or user OID:", error);
    router.push({ name: "AccessDenied" });
  }
};

const generalSetting = computed(
  () => globalSettingStore.getCurrentGlobalSetting
);
const maxTasks = ref(generalSetting.value?.maximumTaskLimit);

const isLimitEnabled = ref(generalSetting.value?.limitMaximumTask);
watch(
  () => route.params.boardId,
  async (newBoardId) => {
    boardId = newBoardId;
    await fetchBoardAndUserOids(newBoardId); // เรียกฟังก์ชันเพื่อ fetch boardOid และ userOid
  }
);

watch(
  () => route.params.boardId,
  async (newBoardId) => {
    boardId = newBoardId;
    await fetchTaskAndStatus(boardId);
  }
);

watch(generalSetting, (newValue) => {
  isLimitEnabled.value = newValue?.limitMaximumTask;
  maxTasks.value = newValue?.maximumTaskLimit;
});

const props = defineProps({
  mode: {
    type: String,
    default: "add",
  },
});

const editedTask = ref({
  id: "",
  title: "",
  description: "",
  assignees: "",
  status: null,
});

const isTitleTooLong = computed(() => editedTask.value?.title?.length > 100);
const isDescriptionTooLong = computed(
  () => editedTask.value?.description?.length > 500
);
const areAssigneesTooLong = computed(
  () => editedTask.value?.assignees?.length > 30
);

const checkAttachFile = computed(() => {
  return attachFile.value.length + attachments.value.length > 10;
});

const isDuplicateFileName = computed(() => {
  return attachFile.value.some((file) =>
    attachments.value.some((attachment) => attachment.fileName === file.name)
  );
});
const checkFileSize = computed(() => {
      return attachFile.value.some((file) => file.size > 20000000);
});

const isTitleValid = computed(() => {
  // console.log(attachFile.value.map((file) => file.name));
  // console.log(attachments.value.map((attachment) => attachment.fileName));
  console.log(attachFile.value.length);
  console.log(attachments.value.length);
  console.log(isDuplicateFileName.value)
  return (
    (editedTask.value.title.trim() !== "" &&
      !isTitleTooLong.value &&
      !isDescriptionTooLong.value &&
      !areAssigneesTooLong.value &&
      (props.mode === "add" ||
        (props.mode === "edit" &&
          editedTask.value &&
          (editedTask.value.title !== originalTask.value.title ||
            editedTask.value.description !== originalTask.value.description ||
            editedTask.value.assignees !== originalTask.value.assignees ||
            editedTask.value.status !== originalTask.value.status)))) ||
    (attachFileStore.selectedFiles.length > 0 && !isDuplicateFileName.value) ||
    attachmentsToDelete.value.length > 0 

  );
});

const originalTask = ref(null);

watch(
  () => router.currentRoute.value.params.id,
  async (id) => {
    if (props.mode === "edit") {
      try {
        let response;

        // ตรวจสอบว่า authenticated หรือไม่
        if (userStore.getIsAuthenticated) {
          // ถ้า authenticated ให้เรียก getItems
          response = await getItems(
            `${
              import.meta.env.VITE_API_ENDPOINTV3
            }/boards/${boardId}/tasks/${id}`
          );
        } else {
          // ถ้าไม่ authenticated ให้เรียก getItemsNoUser
          response = await getItemsNoUser(
            `${
              import.meta.env.VITE_API_ENDPOINTV3
            }/boards/${boardId}/tasks/${id}`
          );
        }
        if (response.status === 404) {
          editedTask.value = null;
          setTimeout(() => {
            closeModal();
          }, 3000);
        } else {
          const data = await response;

          const statusId = statusStore.getStatusByName(data.status)?.id;
          editedTask.value = {
            ...data,
            description: data.description || "",
            assignees: data.assignees || "",
            status: statusId || 1,
          };
          originalTask.value = {
            ...data,
            description: data.description || "",
            assignees: data.assignees || "",
            status: statusId || 1,
          };
        }
      } catch (error) {
        console.error("Error fetching task:", error);
        editedTask.value = null;
      }
    }
  },
  { immediate: true }
);

function resetForm() {
  editedTask.value = {
    title: "",
    description: "",
    assignees: "",
    status: null,
  };
}

async function saveTask() {
  let taskId = null;

  if (props.mode === "edit") {
    taskId = router.currentRoute.value.params.id;
    if (taskId) {
      try {
        errorMessage.value = "";

        // ตรวจสอบสิทธิ์เฉพาะเมื่อทำการบันทึก task
        if (!isSameOidOrHasWriteAccess.value) {
          console.error("You don't have permission to edit this task.");
          errorMessage.value = "You don't have permission to edit this task.";
          return;
        }

        const updatedTask = {
          title: editedTask.value.title.trim(),
          description: editedTask.value.description
            ? editedTask.value.description.trim()
            : "",
          assignees: editedTask.value.assignees
            ? editedTask.value.assignees.trim()
            : "",
          status: { id: editedTask.value.status },
        };

        await taskStore.updateTask(boardId, taskId, updatedTask);
        fetchTaskAndStatus(boardId);
        modalStore.openTaskSuccessModal(updatedTask.title, props.mode);
      } catch (error) {
        errorMessage.value = error.message;
      }
    } else {
      console.error("The task does not exist.");
    }
  } else {
    try {
      errorMessage.value = "";

      // ตรวจสอบสิทธิ์เฉพาะเมื่อทำการบันทึก task
      if (!isSameOidOrHasWriteAccess.value) {
        console.error("You don't have permission to edit this task.");
        errorMessage.value = "You don't have permission to edit this task.";
        return;
      }

      const newTask = {
        title: editedTask.value.title.trim(),
        description: editedTask.value.description
          ? editedTask.value.description.trim()
          : "",
        assignees: editedTask.value.assignees
          ? editedTask.value.assignees.trim()
          : "",
        status: editedTask.value.status,
      };
      // console.log(boardId, newTask);
      const response = await taskStore.addTask(boardId, newTask);
      taskId = response.id;
      fetchTaskAndStatus(boardId);
      router.push({ name: "home" });
      modalStore.openTaskSuccessModal(newTask.title, props.mode);

      const status = statusStore.statuses.find((s) => s.id === newTask.status);
      if (status) {
        status.noOfTasks++;
      }

      closeModal();
    } catch (error) {
      errorMessage.value = error.message;
    }
  }

  await uploadFiles();
  attachFileStore.clearSelectedFiles();
  router.push({ name: "home" });
  fetchTaskAndStatus(boardId);

  // Perform the actual deletion of attachments
  for (const attachmentId of attachmentsToDelete.value) {
    try {
      console.log(attachmentId);
      await attachFileStore.deleteAttachment(boardId, taskId, attachmentId);
      console.log(`Attachment with ID ${attachmentId} deleted successfully.`);
    } catch (error) {
      console.error(
        `Failed to delete attachment with ID ${attachmentId}:`,
        error
      );
    }
  }
  attachmentsToDelete.value = []; // Clear the list after deletion
}

const errorMessage = ref("");

const localTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
const parseAndFormatDate = (dateString) => {
  const date = new Date(dateString);
  if (isNaN(date)) {
    return "Invalid Date";
  }
  return date.toLocaleString("en-GB", { timeZone: localTimeZone });
};

const formatStatus = (status) => {
  return status;
};

function closeModal() {
  router.push({ name: "home" });
  attachFileStore.clearSelectedFiles();
  resetForm();

  taskStore.fetchAllTasks(boardId);
}

const getStatusUsage = (statusId) => {
  const status = statusStore.statuses.find((s) => s.id === statusId);
  if (status) {
    if (
      status.name.toLowerCase() === "no status" ||
      status.name.toLowerCase() === "done"
    ) {
      return "";
    }
    return `${status.noOfTasks || 0}/${maxTasks.value || 0} tasks`;
  }
  return "0/0 tasks";
};

const fetchTaskAndStatus = async (boardId) => {
  await statusStore.fetchAllStatuses(boardId);
  await taskStore.fetchAllTasks(boardId);
};

const fileUrls = ref({});

const loadFileLocation = async (boardId, taskId, attachmentId, fileName) => {
  try {
    const fileUrl = await attachFileStore.loadFileDisplay(
      boardId,
      taskId,
      attachmentId
    );
    fileUrls.value[fileName] = fileUrl; // Ensure this line works correctly
    console.log("File URL loaded:", fileUrls.value[fileName]); // Log for debugging
  } catch (error) {
    console.error(`Failed to load file: ${fileName}`, error);
  }
};

onMounted(async () => {
  userStore.loadFromLocalStorage();
  let boardId = route.params.boardId;

  try {
    await fetchTaskAndStatus(boardId);
    const boardData = await boardStore.getBoardById(boardId);
    boardOid.value = boardData.owner.oid;
    userOid.value = userStore.getUserOid;
    if (props.mode === "add" || !editedTask.value.status) {
      editedTask.value.status = statusStore.statuses[0]?.id || null;
    }

    // Fetch attachments
    const taskId = route.params.id;
    const attachments = await attachFileStore.getAttachments(boardId, taskId);

    // Load files
    for (const attachment of attachments) {
      const fileName = attachment.fileName; // Assuming this exists
      const attachmentId = attachment.id;
      await loadFileLocation(boardId, taskId, attachmentId, fileName);
    }
  } catch (error) {
    console.error("Error fetching board or user OID:", error);
  }
  await fetchBoardAndUserOids(boardId);
  if (isSameOidOrHasWriteAccess.value) {
    await fetchTaskAndStatus(boardId);
  } else {
    console.warn("You do not have permission to access this board.");
  }

  const taskId = route.params.id;
  // // After fetching, you can access the attachments in the store:
  // const kut = await attachFileStore.getAttachments(boardId,taskId)
  // console.log(kut)
  userStore.loadFromLocalStorage();
  getAttachmentsFileName(boardId, taskId);
});
</script>

<template>
  <div v-if="editedTask !== null" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>

      <div
        class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-4xl sm:w-full mx-auto"
      >
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-6">
          <div
            class="itbkk-title text-lg leading-6 font-medium text-gray-900 mb-4"
          >
            {{ mode === "add" ? "New Task" : "Edit Task" }}
          </div>

          <div class="sm:flex sm:items-start">
            <div class="mt-3 text-center sm:mt-5 sm:text-left w-full mr-6 ml-6">
              <div>
                <label class="block text-sm font-bold mb-1">Title:</label>
                <textarea
                  rows="2"
                  v-model="editedTask.title"
                  class="itbkk-title shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                ></textarea>
                <p v-if="!editedTask.title" class="text-red-500 text-sm mt-1">
                  Title is required.
                </p>
                <p
                  class="text-gray-500 text-sm mt-1"
                  :class="{ 'text-red-500': isTitleTooLong }"
                >
                  {{ editedTask.title ? editedTask.title.length : 0 }}/100
                  characters
                </p>
              </div>

              <div class="mt-4">
                <label class="block text-sm font-bold mb-1">Description:</label>
                <textarea
                  rows="5"
                  v-model="editedTask.description"
                  class="itbkk-description shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                  placeholder="No Description Provided"
                ></textarea>
                <p
                  class="text-gray-500 text-sm mt-1"
                  :class="{ 'text-red-500': isDescriptionTooLong }"
                >
                  {{
                    editedTask.description ? editedTask.description.length : 0
                  }}/500 characters
                </p>
              </div>

              <div v-if="mode === 'edit'">
                <label class="block text-sm font-bold mb-1"
                  >Attachments :</label
                >

                <div v-if="attachmentsFileNames.length > 0">
                  <h3 class="text-lg font-bold mb-2">
                    Uploaded Files: {{ attachmentsFileNames.length }}
                  </h3>

                  <ul>
                    <div class="flex flex-col overflow-y-auto max-h-40 gap-2">
                      <li
                        v-for="(fileName, index) in attachments"
                        :key="index"
                        class="flex items-center justify-between bg-gray-100 p-2 rounded-md shadow"
                      >
                        <span
                          @click="handleDownloadAttachment(index)"
                          class="text-sm font-medium text-blue-600 cursor-pointer truncate hover:underline"
                        >
                          {{ fileName.fileName }}
                        </span>
                        <div class="flex">
                          <img
                            v-if="
                              fileName.fileName.endsWith('.png') ||
                              fileName.fileName.endsWith('.jpg') ||
                              fileName.fileName.endsWith('.jpeg')
                            "
                            :src="fileUrls[fileName.fileName]"
                            alt="Attachment preview"
                            class="w-auto h-[40px]"
                          />

                          <button
                            @click="handleDeleteAttachment(index)"
                            class="ml-4 bg-red-500 text-white font-bold rounded px-2 py-1 hover:bg-red-600"
                          >
                            Delete
                          </button>
                        </div>
                      </li>
                    </div>
                  </ul>
                </div>

                <div v-else>
                  <p>No files uploaded. (-)</p>
                  <!-- <p>Debug: {{ attachmentsFileNames }}</p> -->
                </div>

                <!-- File input to select multiple files -->
                <input type="file" @change="handleFileChange" multiple  />

                <!-- Display selected files with styling and remove button -->
                <div v-if="attachFileStore.selectedFiles.length">
                  <h3>
                    Selected Files: {{ attachFileStore.selectedFiles.length }}
                  </h3>
                  <div class="space-y-2">
                    <div class="flex flex-col overflow-y-auto max-h-20 gap-2">
                      <div
                        v-for="(file, index) in attachFile"
                        :key="file.name"
                        class="flex justify-between items-center bg-gray-200 text-black p-2 rounded-lg"
                      >
                        <!-- Display the file name -->
                        <span class="text-sm">{{ file.name }}</span>

                        <!-- Button to remove the file -->
                        <button
                          type="button"
                          class="text-red-500"
                          @click="attachFileStore.removeFile(index)"
                        >
                          X
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else>
                  <p>No files selected. (-)</p>
                  <!-- Show '-' when no selected files -->
                </div>

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
                <textarea
                  rows="6"
                  v-model="editedTask.assignees"
                  class="itbkk-assignees shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                  placeholder="Unassigned"
                ></textarea>
                <p
                  class="text-gray-500 text-sm mt-1"
                  :class="{ 'text-red-500': areAssigneesTooLong }"
                >
                  {{
                    editedTask.assignees ? editedTask.assignees.length : 0
                  }}/30 characters
                </p>
              </div>

              <div class="mt-2.5 mb-4">
                <label class="block text-sm font-bold mb-1">Status:</label>
                <select
                  v-model="editedTask.status"
                  class="itbkk-status shadow-sm p-3 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                >
                  <option
                    v-for="status in statusStore.statuses"
                    :key="status.id"
                    :value="status.id"
                  >
                    {{ formatStatus(status.name) }}
                  </option>
                </select>
              </div>

              <div
                v-if="mode === 'add' || mode === 'edit'"
                class="text-sm text-gray-500 mb-4"
              >
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
                  <label class="itbkk-timezone block text-sm font-bold mb-1"
                    >TimeZone : {{ localTimeZone }}</label
                  >
                </div>
                <div class="mt-4">
                  <label class="itbkk-created-on block text-sm font-bold mb-1"
                    >Created On :
                    {{ parseAndFormatDate(editedTask.createdOn) }}</label
                  >
                </div>
                <div class="mt-4">
                  <label class="itbkk-updated-on block text-sm font-bold mb-1"
                    >Updated On :
                    {{ parseAndFormatDate(editedTask.updatedOn) }}</label
                  >
                  <p class="mt-1 text-sm text-gray-900"></p>
                </div>
              </div>
              <div class="alertData flex flex-col text-red-400">
                <p v-if="checkAttachFile">
                  &#9888;
                  <span class="text-xs"
                    >Each Task can have atmost 10 files</span
                  >
                </p>
                <p v-if="checkFileSize">
                  &#9888;
                  <span class="text-xs"
                    >Each file cannot be larger than 20 MB</span
                  >
                </p>
                <p v-if="isDuplicateFileName">
                  &#9888;
                  <span class="text-xs"
                    >File with the same Filename cannot be add</span
                  >
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <div class="flex space-x-4">
            <!-- Button for non-board owners (when OIDs are not the same) -->
            <button
              v-if="isSameOidOrHasWriteAccess"
              type="button"
              class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
              :class="{
                'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500':
                  isTitleValid && !checkAttachFile && !checkFileSize,
                'bg-gray-300 cursor-not-allowed':
                  !isTitleValid || checkAttachFile || checkFileSize,
              }"
              @click="saveTask()"
              :disabled="
                !isTitleValid ||
                checkAttachFile ||
                checkFileSize ||
                isTitleTooLong ||
                isDescriptionTooLong ||
                areAssigneesTooLong
              "
            >
              {{ mode === "add" ? "Add" : "Save" }}
            </button>

            <!-- Button for board owners (when OIDs are the same) -->
            <button
              v-if="!isSameOidOrHasWriteAccess"
              type="button"
              class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm tooltip tooltip-secondary tooltip-left"
              :class="{
                'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500':
                  isSameOidOrHasWriteAccess,
                'bg-gray-300 cursor-not-allowed': !isSameOidOrHasWriteAccess,
              }"
              @click="isSameOidOrHasWriteAccess && saveTask()"
              :disabled="!isSameOidOrHasWriteAccess"
              data-tip="You need to be the board owner to perform this action."
            >
              {{ mode === "add" ? "Add" : "Save" }}
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
  <div v-else class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>
      <div
        class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full mx-auto"
      >
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="sm:flex sm:items-start">
            <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
              <h3
                class="itbkk-message text-lg leading-6 font-medium text-red-500"
              >
                The task does not exist.
              </h3>
            </div>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button
            type="button"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm"
            @click="closeModal"
          >
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
      <div
        class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full mx-auto"
      >
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
          <button
            type="button"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm"
            @click="errorMessage = ''"
          >
            OK
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
