<!-- AddEditStatus.vue -->
<script setup>
import { ref, computed, watch, onMounted } from "vue"
import { useStatusStore } from "../stores/status"
import { useModalStore } from "../stores/useModalStore"
import { useRouter } from "vue-router"

const router = useRouter()
const statusStore = useStatusStore()
const modalStore = useModalStore()
const successModalTimeout = ref(null)

const props = defineProps({
  show: {
    type: Boolean,
    required: true,
  },
  status: {
    type: Object,
    default: () => ({
      id: "",
      name: "",
      description: "",
      colorCode: "#000000",
    }),
  },
  mode: {
    type: String,
    default: "add",
  },
})

const emit = defineEmits(["close", "statusSaved"])
const editedStatus = ref({
  id: "",
  name: "",
  description: "",
  colorCode: "#000000",
});

const nameError = ref("")

const isDuplicateName = computed(() => {
  return statusStore.statuses.some(
    (status) =>
      status.name.toLowerCase() === editedStatus.value.name.toLowerCase() &&
      status.id !== editedStatus.value.id
  )
})

const isNameValid = computed(() => {
  return (
    editedStatus.value &&
    editedStatus.value.name &&
    editedStatus.value.name.trim() !== "" &&
    (props.mode === "add" ||
      (props.mode === "edit" &&
        originalStatus.value &&
        (editedStatus.value.name !== originalStatus.value.name ||
          editedStatus.value.description !== originalStatus.value.description ||
          editedStatus.value.colorCode !== originalStatus.value.colorCode)))
  )
})

const originalStatus = ref(null)

watch(
  () => router.currentRoute.value.params.id,
  async (id) => {
    if (props.mode === "edit") {
      try {
        const response = await fetch(`${import.meta.env.VITE_API_ENDPOINT}/statuses/${id}`);
        if (response.status === 404) {
          setTimeout(() => {
            router.push("/status/manage")
          }, 3000)
          throw new Error("Status not found");
        } else {
          const data = await response.json();
          editedStatus.value = {
            ...data,
            name: data.name || "",
            description: data.description || "",
            colorCode: data.colorCode,
          };
          // console.log("Fetched status data:", editedStatus.value); 
          originalStatus.value = { ...data };
          nameError.value = "";
        }
      } catch (error) {
        editedStatus.value = null;
      }
    } else {
      resetForm();
      nameError.value = "Name is required.";
    }
  },
  { immediate: true }
);
function resetForm() {
  editedStatus.value = {
    name: "",
    description: "",
    colorCode: "#000000",
  }
}

const closeSuccessModal = () => {
  modalStore.closeSuccessModal()
  clearTimeout(successModalTimeout.value)
}

async function saveStatus() {
  if (!editedStatus.value.name) {
    nameError.value = "Name is required."
    return
  }

  if (isDuplicateName.value) {
    nameError.value = "Status name must be unique, please choose another name."
    return
  }

  nameError.value = ""

  try {
    const updatedStatus = {
      name: editedStatus.value.name.trim(),
      description: editedStatus.value.description.trim(),
      colorCode: editedStatus.value.colorCode,
    }

    if (props.mode === "edit") {
      const statusId = router.currentRoute.value.params.id
      if (statusId) {
        await statusStore.updateStatus(statusId, updatedStatus)
        await statusStore.fetchAllStatuses()
        emit("statusSaved")
        modalStore.openSuccessModal(updatedStatus.name, props.mode, "status")

        // Set a timeout to close the success modal after 3 seconds
        successModalTimeout.value = setTimeout(() => {
          modalStore.closeSuccessModal()
        }, 3000)
      } else {
        console.error("The status does not exist.")
      }
    } else {
      await statusStore.createStatus(updatedStatus)
      await statusStore.fetchAllStatuses()
      modalStore.openSuccessModal(updatedStatus.name, props.mode, "status")

      // Set a timeout to close the success modal after 3 seconds
      successModalTimeout.value = setTimeout(() => {
        modalStore.closeSuccessModal()
      }, 3000)
    }
    router.push("/status/manage")
  } catch (error) {
    console.error("Error saving status:", error)
  }
}

function formatStatus(status) {
  return status
    .toUpperCase()
    .replace(/(?:^|\s)\S/g, (char) => char.toUpperCase())
}

function closeModal() {
  router.push("/status/manage")
  resetForm()
}
</script>

<template>
  <div v-if="editedStatus !== null" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>

      <div
        class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-4xl sm:w-full mx-auto"
      >
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-6">
          <div class="text-lg leading-6 font-medium text-gray-900 mb-4">
            {{ mode === "add" ? "New Status" : "Edit Status" }}
          </div>

          <div class="sm:flex sm:items-start">
            <div class="mt-3 text-center sm:mt-5 sm:text-left w-full mr-6 ml-6">
              <div>
                <label class="block text-sm font-bold mb-1">Name:</label>
                <textarea
                  rows="2"
                  v-model="editedStatus.name"
                  class="shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                  :maxlength="50"
                ></textarea>
                <p v-if="!editedStatus.name" class="text-red-500 text-sm mt-1">
  Name is required.
</p>
<p v-else-if="isDuplicateName" class="text-red-500 text-sm mt-1">
  Status name must be unique, please choose another name.
</p>
                <p class="text-gray-500 text-sm mt-1">
                  {{ editedStatus.name ? editedStatus.name.length : 0 }}/50 characters
                </p>
              </div>

              <div class="mt-4">
                <label class="block text-sm font-bold mb-1">Description:</label>
                <textarea
                  rows="5"
                  v-model="editedStatus.description"
                  class="shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                  placeholder="No Description Provided"
                  :maxlength="200"
                ></textarea>
                <p class="text-gray-500 text-sm mt-1">
                  {{
                    editedStatus.description
                      ? editedStatus.description.length
                      : 0
                  }}/200 characters
                </p>
              </div>

              <div class="mt-4 flex">
                <label class="block text-sm font-bold mt-1">Color Code:</label>
                <input
                  type="color"
                  v-model="editedStatus.colorCode"
                  class="shadow-sm px-1 ml-4 p- block border border-gray-300 rounded-md h-8 w-20"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button
  type="button"
  class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
  :class="{
    'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500': isNameValid && !isDuplicateName,
    'bg-gray-300 cursor-not-allowed': !isNameValid || isDuplicateName,
  }"
  @click="saveStatus"
  :disabled="!isNameValid || isDuplicateName"
>
  {{ mode === "add" ? "Add" : "Save" }}
</button>
          <button
            type="button"
            class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
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
                The status does not exist.
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
</template>

<style scoped></style>
