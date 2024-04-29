<script setup>
import { onMounted, ref } from "vue"
import { getItems, postItem, putItem, deleteItem } from "../utils/fetch"

const props = defineProps({
  task: { type: Object, required: true }
})

const editedTask = ref({})
const focusedOnDescription = ref(false)

const saveTask = async (updatedTask) => {
  try {
    if (updatedTask.id) {
      await putItem(
        `${import.meta.env.VITE_API_ENDPOINT}/tasks/${updatedTask.id}`,
        updatedTask
      )
    } else {
      await postItem(`${import.meta.env.VITE_API_ENDPOINT}/tasks`, updatedTask)
    }
    closeModal()
  } catch (error) {
    alert(error.message)

    window.history.back()
  }
}




const showModal = async (task) => {
  try {
    const response = await taskStore.getTask(task.id)
    if (response.status === 404) {
      throw new Error("The requested task does not exist")
    }
    selectedTask.value = task
  } catch (error) {
    alert(error.message)
    window.history.back()

  }
}

const localTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
const parseAndFormatDate = (dateString) => {
  const date = new Date(dateString)
  if (isNaN(date)) {

    return 'Invalid Date'; 
  }
  return date.toLocaleString('en-GB', { timeZone: localTimeZone})
};




const closeModal = () => {
  editedTask.value = {}
  focusedOnDescription.value = false
  // Add your modal close logic here
}


onMounted(async () => {
  try {
    const data = await getItems(
      `${import.meta.env.VITE_API_ENDPOINT}/tasks/${props.task.id}`
    )
    editedTask.value = data
  } catch (error) {
    console.error("Error fetching tasks:", error)
  }
})
</script>

<template>
  <div v-if="task" class="fixed z-10 inset-0 overflow-y-auto">
    <div
      class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0"
    >
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>
      <span
        class="hidden sm:inline-block sm:align-middle sm:h-screen"
        aria-hidden="true"
        >&#8203;</span
      >
      <div
        class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-headline"
      >
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div>
            <h3
              class="text-lg leading-6 font-medium text-gray-900"
              id="modal-headline"
            >
              <strong>Title:</strong>
              <textarea
                type="text"
                v-model="editedTask.title"
                class="w-full border itbkk-title"
              />
            </h3>
            <strong class="text-sm text-gray-500">Description:</strong>
        <template v-if="editedTask.description">
          <textarea v-model="editedTask.description" class="w-full border itbkk-description h-36 text-sm text-gray-600 mt-2"></textarea>
        </template>
        <template v-else>
          <textarea v-model="editedTask.description" class="w-full border itbkk-description h-48 text-gray-400 italic" placeholder="No Description Provided"></textarea>
        </template>

            <p class="text-sm text-gray-600 mt-2">
              <strong>Assignees:</strong>
              <template v-if="editedTask.assignees">
                <input
                  type="text"
                  v-model="editedTask.assignees"
                  class="w-full border itbkk-assignees"
                />
              </template>
              <template v-else>
                <input
                  type="text"
                  class="w-full border italic text-gray-400 itbkk-assignees"
                  readonly
                  value="Unassigned"
                />
              </template>
            </p>
            <p class="text-sm text-gray-600 mt-2">
              <strong>Status:</strong>
              <select
                v-model="editedTask.status"
                class="w-full border itbkk-status"
              >
                <option value="NO_STATUS" class="itbkk-status">
                  No Status
                </option>
                <option value="TO_DO" class="itbkk-status">To Do</option>
                <option value="DOING" class="itbkk-status">Doing</option>
                <option value="DONE" class="itbkk-status">Done</option>
              </select>
            </p>

            <p class="text-sm text-gray-600 mt-2">
              <strong>TimeZone: </strong>
              <span class="itbkk-timezone">{{localTimeZone}}</span>
            </p>

            <p class="text-sm text-gray-600 mt-2 itbkk-created-on">
              <strong>Created On:</strong>
              {{ parseAndFormatDate(editedTask.createdOn) }}
            </p>
            <p class="text-sm text-gray-600 mt-2 itbkk-updated-on">
              <strong>Updated On:</strong>
              {{ parseAndFormatDate(editedTask.updatedOn) }}
            </p>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button
            type="button"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-blue-600 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:ml-3 sm:w-auto sm:text-sm"
            @click="saveTask"
          >
            OK
          </button>
          <button
            type="button"
            class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            @click="$emit('close')"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
