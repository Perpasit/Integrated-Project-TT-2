<script setup>
import { ref, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { getItems } from "../utils/fetch"

const route = useRoute()
const router = useRouter()
const status = ref(null)
const loading = ref(true)
const localTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone

const closeModal = () => {
  router.push("/status/manage")
}

const parseAndFormatDate = (dateString) => {
  const date = new Date(dateString)
  if (isNaN(date)) {
    return "Invalid Date"
  }
  return date.toLocaleString("en-GB", { timeZone: localTimeZone })
}

onMounted(async () => {
  try {
    const id = route.params.statusId
    const data = await getItems(
      `${import.meta.env.VITE_API_ENDPOINTV3}/boards/1/statuses/${id}`
    )
    status.value = data
    // console.log(status)
  } catch (error) {
    console.error("Error fetching status:", error)
    status.value = null
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div
    class="fixed inset-0 flex items-center justify-center bg-gray-500 bg-opacity-75"
  >
    <div
      class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-3xl sm:w-full"
    >
      <div class="mt-5 sm:mt-6 sm:mx-6">
        <div v-if="status" class="px-4 py-5">
          <div class="mb-4">
            <label class="block text-sm font-bold mb-1">Name :</label>
            <textarea
              rows="2"
              readonly
              :value="status.name"
              class="itbkk-status-name w-full border border-gray-300 rounded-md p-1 shadow-sm mt-1"
            ></textarea>
          </div>
          <div class="mb-4">
            <label class="block text-sm font-bold mb-1">Description :</label>
            <textarea
              v-if="status.description"
              rows="6"
              v-model="status.description"
              class="itbkk-status-description w-full border border-gray-300 rounded-md p-1 shadow-sm mt-2 text-sm text-gray-600"
              readonly
              >{{ status.description }}</textarea
            >
            <textarea
              v-else
              rows="6"
              v-model="status.description"
              class="itbkk-status-description w-full border border-gray-300 rounded-md p-1 shadow-sm mt-2 text-sm text-gray-400 italic"
              placeholder="No Description Provided"
              readonly
            >
            No Description Provided</textarea
            >
          </div>
          <div class="flex justify-between mb-4">
            <div>
              <label class="block text-sm font-bold mb-1">TimeZone</label>
              <label class="itbkk-timezone block">{{ localTimeZone }}</label>
            </div>
            <div>
              <label class="block text-sm font-bold mb-1">Created On</label>
              <label class="itbkk-created-on block">{{
                parseAndFormatDate(status.createdOn)
              }}</label>
            </div>
            <div>
              <label class="block text-sm font-bold mb-1">Updated On</label>
              <label class="itbkk-updated-on block">{{
                parseAndFormatDate(status.updatedOn)
              }}</label>
            </div>
          </div>
        </div>
        <div v-else class="px-4 py-5 text-red-500 text-xl">
          The status does not exist.
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
</template>
