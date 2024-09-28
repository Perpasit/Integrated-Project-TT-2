<script setup>
import { ref, computed, watch } from "vue"
import { useStatusStore } from "../stores/status"
import { useModalStore } from "../stores/useModalStore"
import { useRouter, useRoute } from "vue-router"
import { getItems } from "../utils/fetch"
import { useBoardStore } from "../stores/boardStore"
import { useUserStore } from "../stores/userStore"

const router = useRouter()
const route = useRoute()
const statusStore = useStatusStore()
const modalStore = useModalStore()
const boardStore = useBoardStore()
const userStore = useUserStore()
const successModalTimeout = ref(null)

const boardId = route.params.boardId
const boardOid = computed(() => boardStore.getBoardOid(boardId))
const userOid = computed(() => userStore.getUserOid)

const isSameOid = computed(() => {
  return boardOid.value !== userOid.value // Disable if boardOid is not equal to userOid
})

const localTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone
const isNameTooLong = computed(() => editedStatus.value.name.length > 50)
const isDescriptionTooLong = computed(
  () => editedStatus.value.description.length > 200
)

const props = defineProps({
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
})

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
    !isNameTooLong.value &&
    !isDescriptionTooLong.value &&
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
        const response = await getItems(
          `${
            import.meta.env.VITE_API_ENDPOINTV3
          }/boards/${boardId}/statuses/${id}`
        )
        console.log(response)
        if (response.status === 404) {
          setTimeout(() => {
            router.push({ name: "ManageStatus" })
          }, 3000)
          throw new Error("Status not found")
        } else {
          const data = await response
          editedStatus.value = {
            ...data,
            name: data.name || null,
            description: data.description || "",
            colorCode: data.colorCode,
          }
          originalStatus.value = {
            ...data,
            description: data.description || "",
          }
        }
      } catch (error) {
        editedStatus.value = null
      }
    } else {
      resetForm()
    }
  },
  { immediate: true }
)
function resetForm() {
  editedStatus.value = {
    name: "",
    description: "",
    colorCode: "#000000",
  }
}

async function saveStatus() {
  try {
    const updatedStatus = {
      name: editedStatus.value.name.trim(),
      description: editedStatus.value.description
        ? editedStatus.value.description.trim()
        : null,
      colorCode: editedStatus.value.colorCode,
      globalSettingId: editedStatus.value.globalSetting?.id || null,
    }

    if (props.mode === "edit") {
      const statusId = router.currentRoute.value.params.id
      if (statusId) {
        await statusStore.updateStatus(boardId, statusId, updatedStatus)
        fetchStatus()
        emit("statusSaved")
        modalStore.openStatusSuccessModal(
          updatedStatus.name,
          props.mode,
          "status"
        )

        successModalTimeout.value = setTimeout(() => {
          modalStore.closeStatusSuccessModal()
          resetForm()
        }, 3000)
      } else {
        console.error("The status does not exist.")
      }
    } else {
      await statusStore.createStatus(boardId, updatedStatus)
      fetchStatus()
      modalStore.openStatusSuccessModal(
        updatedStatus.name,
        props.mode,
        "status"
      )

      successModalTimeout.value = setTimeout(() => {
        modalStore.closeStatusSuccessModal()
        resetForm()
      }, 3000)
    }

    router.push({ name: "ManageStatus" })
  } catch (error) {
    console.error("Error saving status:", error)
  }
}

const parseAndFormatDate = (dateString) => {
  const date = new Date(dateString)
  if (isNaN(date)) {
    return "Invalid Date"
  }
  return date.toLocaleString("en-GB", { timeZone: localTimeZone })
}

function closeModal() {
  router.push({ name: "ManageStatus" })
  resetForm()
}

const fetchStatus = async () => {
  await statusStore.fetchAllStatuses(boardId)
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

          <div class="itbkk-modal-status sm:flex sm:items-start">
            <div class="mt-3 text-center sm:mt-5 sm:text-left w-full mr-6 ml-6">
              <div>
                <label class="block text-sm font-bold mb-1">Name:</label>
                <textarea
                  rows="2"
                  v-model="editedStatus.name"
                  class="itbkk-status-name shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                ></textarea>
                <p v-if="!editedStatus.name" class="text-red-500 text-sm mt-1">
                  Name is required.
                </p>
                <p
                  v-else-if="isDuplicateName"
                  class="text-red-500 text-sm mt-1"
                >
                  Status name must be unique, please choose another name.
                </p>
                <p
                  class="text-gray-500 text-sm mt-1"
                  :class="{ 'text-red-500': isNameTooLong }"
                >
                  {{ editedStatus.name ? editedStatus.name.length : 0 }}/50
                  characters
                </p>
              </div>

              <div class="mt-4">
                <label class="block text-sm font-bold mb-1">Description:</label>
                <textarea
                  rows="5"
                  v-model="editedStatus.description"
                  class="itbkk-status-description shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                  placeholder="No Description Provided"
                ></textarea>
                <p
                  class="itbkk-status-description text-gray-500 text-sm mt-1"
                  :class="{ 'text-red-500': isDescriptionTooLong }"
                >
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
                  class="itbkk-status-colorCode shadow-sm px-1 ml-4 p- block border border-gray-300 rounded-md h-8 w-20"
                />
              </div>

              <div v-if="mode === 'edit'" class="flex justify-between mb-4">
                <div>
                  <label class="block text-sm font-bold mb-1">TimeZone</label>
                  <label class="itbkk-timezone block">{{
                    localTimeZone
                  }}</label>
                </div>
                <div>
                  <label class="block text-sm font-bold mb-1">Created On</label>
                  <label class="itbkk-created-on block">{{
                    parseAndFormatDate(editedStatus.createdOn)
                  }}</label>
                </div>
                <div>
                  <label class="block text-sm font-bold mb-1">Updated On</label>
                  <label class="itbkk-updated-on block">{{
                    parseAndFormatDate(editedStatus.updatedOn)
                  }}</label>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <div class="flex space-x-4">
            <!-- Button for non-board owners (when OIDs are not the same) -->
            <button
              v-if="!isSameOid"
              type="button"
              class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
              :class="{
                'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500':
                  isNameValid &&
                  !isDuplicateName &&
                  !isNameTooLong &&
                  !isDescriptionTooLong,
                'bg-gray-300 cursor-not-allowed':
                  !isNameValid ||
                  isDuplicateName ||
                  isNameTooLong ||
                  isDescriptionTooLong,
              }"
              @click="
                isNameValid &&
                  !isDuplicateName &&
                  !isNameTooLong &&
                  !isDescriptionTooLong &&
                  saveStatus()
              "
              :disabled="
                !isNameValid ||
                isDuplicateName ||
                isNameTooLong ||
                isDescriptionTooLong
              "
            >
              {{ mode === "add" ? "Add" : "Save" }}
            </button>

            <!-- Button for board owners (when OIDs are the same) -->
            <button
              v-if="isSameOid"
              type="button"
              class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm tooltip tooltip-secondary tooltip-left"
              :class="{
                'bg-gray-300 cursor-not-allowed': isSameOid,
              }"
              :disabled="true"
              data-tip="Only the board owner can perform this action."
            >
              {{ mode === "add" ? "Add" : "Save" }} (Board Owner Only)
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
