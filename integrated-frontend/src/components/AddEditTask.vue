<script setup>
import { ref, computed, watch, onMounted, defineProps, defineEmits } from "vue"
import { useTaskStore } from "../stores/task"
import { useStatusStore } from "../stores/status"
import { useModalStore } from "../stores/useModalStore"
import { useRouter } from "vue-router"

const router = useRouter()
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  task: {
    type: Object,
    required: true
  },
  mode: {
    type: String,
    default: "add"
  }
})

const emit = defineEmits(["close", "taskSaved"])
const modalStore = useModalStore()
const statusStore = useStatusStore()
const originalTask = ref(null)

const taskStore = useTaskStore()
const editedTask = ref({
  id: "",
  title: "",
  description: "",
  assignees: "",
  status: 1
})

const titleError = ref("")

const isTitleValid = computed(() => {
  return (
    editedTask.value.title.trim() !== "" &&
    (props.mode === "add" ||
      (props.mode === "edit" &&
        editedTask.value &&
        originalTask.value &&
        (editedTask.value.title !== originalTask.value.title ||
          editedTask.value.description !== originalTask.value.description ||
          editedTask.value.assignees !== originalTask.value.assignees ||
          editedTask.value.status !== originalTask.value.status)))
  )
})

watch(
  () => props.task,
  (newTask) => {
    if (newTask) {
      editedTask.value = {
        id: newTask.id || "",
        title: newTask.title || "",
        description: newTask.description || "",
        assignees: newTask.assignees || "",
        status: newTask.status // ใช้ค่า status id โดยตรง
      }
    }
  }
)

onMounted(async () => {
  await statusStore.fetchAllStatuses()

  if (props.mode === "edit") {
    const id = router.currentRoute.value.params.id
    try {
      const response = await fetch(
        `${import.meta.env.VITE_API_ENDPOINT}/tasks/${id}`
      )
      if (response.status === 404) {
        editedTask.value = null
        setTimeout(() => {
          router.push("/task")
        }, 3000)
      } else {
        const data = await response.json()
        editedTask.value = {
          ...data,
          status: data.status
        }

        originalTask.value = { ...data }
        titleError.value = ""
      }
    } catch (error) {
      console.error("Error fetching task:", error)
      editedTask.value = null
    }
  } else {
    resetForm()
    titleError.value = "Title is required."
  }
})

function resetForm() {
  editedTask.value = {
    title: "",
    description: "",
    assignees: "",
    status: 1
  }
}

async function saveTask() {
  if (!isTitleValid.value) {
    titleError.value = "Title is required."
    return
  }

  try {
    const updatedTask = {
      ...editedTask.value,
      title: editedTask.value.title.trim(),
      description: editedTask.value.description.trim(),
      assignees: editedTask.value.assignees.trim(),
      updatedOn: new Date().toISOString(),

      status: editedTask.value.status
    }

    if (props.mode === "edit") {
      await taskStore.updateTask(updatedTask.id, updatedTask)
      emit("taskSaved")
    } else {
      await taskStore.addTask(updatedTask)
    }
    modalStore.openSuccessModal(updatedTask.title, props.mode)
    closeModal()
  } catch (error) {
    console.error("Error saving task:", error)
  }
  router.push("/")
}

function goBack() {
  router.push("/task")
}

function closeModal() {
  router.push("/")
  resetForm()
}

const formattedStatus = computed(() => {
  return (task) => {
    const status = statusStore.getStatusById(task.status)
    if (status) {
      return status.name
        .replace(/_/g, " ")
        .replace(/(?:^|\s)\S/g, (char) => char.toUpperCase())
    } else {
      return "Unknown"
    }
  }
})
const statusNameById = computed(() => {
  return (statusId) => {
    const status = statusStore.getStatusById(statusId)
    return status ? status.name : "Unknown"
  }
})

console.log(editedTask)
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
            class="itbkk-button-add text-lg leading-6 font-medium text-gray-900 mb-4"
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
                <p v-if="titleError" class="text-red-500 text-sm mt-1">
                  {{ titleError }}
                </p>
              </div>

              <div class="mt-4">
                <label class="block text-sm font-bold mb-1">Description:</label>
                <textarea
                  rows="10"
                  v-model="editedTask.description"
                  class="itbkk-description shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                  placeholder="No Description Provided"
                ></textarea>
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
                    {{ status.name }}
                  </option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button
            type="button"
            class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
            :class="{
              'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500':
                isTitleValid,
              'bg-gray-300 cursor-not-allowed': !isTitleValid
            }"
            @click="saveTask"
            :disabled="!isTitleValid"
          >
            {{ mode === "add" ? "Add" : "Save" }}
          </button>
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
              <h3 class="text-lg leading-6 font-medium text-red-500">
                The task does not exist.
              </h3>
            </div>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button
            type="button"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm"
            @click="goBack"
          >
            OK
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
