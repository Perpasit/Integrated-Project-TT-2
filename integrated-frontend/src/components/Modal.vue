<script setup>
import { onMounted, ref } from "vue"
import { getItems, postItem, putItem, deleteItem } from "../utils/fetch"

const props = defineProps({
  task: { type: Object, required: true }
})

const editedTask = ref({})
const focusedOnDescription = ref(false)



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

const localTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone
const parseAndFormatDate = (dateString) => {
  const date = new Date(dateString)
  if (isNaN(date)) {
    return "Invalid Date"
  }
  return date.toLocaleString("en-GB", { timeZone: localTimeZone })
}

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
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>

      <div class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-4xl sm:w-full">
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-6">
          <div class="sm:flex sm:items-start">

            <div class="mt-3 text-center sm:mt-5 sm:text-left w-full mr-6 ml-6 ">
              
              <div class="text-lg leading-6 font-medium text-gray-900">
                <label class="block text-sm font-bold mb-1">Title :</label>
                <textarea rows="2" readonly v-model="editedTask.title" class="itbkk-title shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"></textarea>
              </div>

              <div class="mt-4">

                <label class="block text-sm font-bold mb-1">Description :</label>
                <textarea v-if="editedTask.description" rows="12" v-model="editedTask.description"
                  class="w-full border itbkk-description text-sm text-gray-600 mt-2 shadow-sm p-1 block  sm:text-sm border-gray-300 rounded-md"
                  readonly> {{ editedTask.description }}</textarea>
                <textarea v-else rows="12" v-model="editedTask.description"
                  class="w-full border itbkk-description text-sm text-gray-400 italic mt-2 shadow-sm p-1 block  sm:text-sm border-gray-300 rounded-md"
                  placeholder="No Description Provided" readonly>No Description Provided</textarea>
              </div>
              
            </div>

            <div class="mt-3 text-center sm:mt-2 sm:text-left w-1/2 mr-6 ml-6">

              <div class="mt-2.5 mb-4">
                <label class="block text-sm font-bold mb-1">Assignees :</label>
                <textarea rows="6"  readonly v-model="editedTask.assignees" class="itbkk-assignees shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md" placeholder="Unassigned" ></textarea>
              </div>


              <div>
                <label class="block text-sm font-bold mb-1">Status :</label>
                <!-- <select readonly v-model="editedTask.status" class="shadow-sm p-3 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md">
                  <option value="NO_STATUS">No Status</option>
                  <option value="TO_DO">To Do</option>
                  <option value="DOING">Doing</option>
                  <option value="DONE">Done</option>
                </select> -->
                <input readonly v-model="editedTask.status" class="shadow-sm p-1 block w-full sm:text-sm border border-gray-300 rounded-md">
                  <!-- <option value="NO_STATUS">No Status</option>
                  <option value="TO_DO">To Do</option>
                  <option value="DOING">Doing</option>
                  <option value="DONE">Done</option> -->
              </input>
              </div>
              <div class="mt-4">
                <label class="itbkk-timezone block text-sm font-bold mb-1">TimeZone : {{ localTimeZone }}
                </label>
                <!-- <p class="mt-1 text-sm text-gray-900"></p> -->
              </div>
              <div class="mt-4">
                <label class="itbkk-created-on block text-sm font-bold mb-1">Created On : {{ parseAndFormatDate(editedTask.createdOn) }} </label>
                <!-- <p class="mt-1 text-sm text-gray-900"></p> -->
              </div>
              <div class="mt-4">
                <label class="itbkk-updated-on block text-sm font-bold mb-1">Updated On : {{ parseAndFormatDate(editedTask.updatedOn) }}</label>
                <p class="mt-1 text-sm text-gray-900"></p>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button type="button" class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm"
                  @click="$emit('close')">
            OK
          </button>
          <!-- <button type="button" class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
                  @click="$emit('close')">
            CANCEL
          </button> -->
        </div>
      </div>
    </div>
  </div>  
</template>