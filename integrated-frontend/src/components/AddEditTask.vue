<script setup>
import { ref, computed, watch , watchEffect  } from "vue";
import { useTaskStore } from "../stores/task";
import { useRoute } from 'vue-router';

const route = useRoute();
const taskStore = useTaskStore();
const showModal = ref(false);


const props = defineProps({
  taskId: Number  // Receive taskId if using routing
});

const editedTask = ref({
  title: "",
  description: "",
  assignees: "",
  status: "NO_STATUS"
});

const titleError = ref("");

const isTitleValid = computed(() => {
  return editedTask.value.title.trim() !== "";
});



watchEffect(async () => {
  const taskId = route.params.id;
  if (taskId) {
    editedTask.value = await taskStore.fetchTaskById(taskId);
  }
});

function openModal() {
  resetForm();
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
  this.$router.back();  // If using router, navigate back on close
}

async function saveTask() {
  if (!isTitleValid.value) {
    titleError.value = "Title is required";
    return;
  }

  try {
    await taskStore.addTask(editedTask.value);
    emit("closeModal", false, editedTask.value);
    emit("showSuccessModal", true);
  } catch (error) {
    console.error("Error saving task:", error);
  }
}

const goBack = () => {
  router.push('/')
}
const emit = defineEmits(["closeModal", "showSuccessModal"]);
</script>

<template>
  <div class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>

      <div
        class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-4xl sm:w-full mx-auto"
      >
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-6">
          <div class="itbkk-button-add text-lg leading-6 font-medium text-gray-900 mb-4">
            New Task
          </div>

          <div class="sm:flex sm:items-start">
            <div class="mt-3 text-center sm:mt-5 sm:text-left w-full mr-6 ml-6">
              <div>
                <label class="block text-sm font-bold mb-1">Title :</label>
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
                <label class="block text-sm font-bold mb-1">Description :</label>
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
                <label class="block text-sm font-bold mb-1">Assignees :</label>
                <textarea
                  rows="6"
                  v-model="editedTask.assignees"
                  class="itbkk-assignees shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                  placeholder="Unassigned"
                ></textarea>
              </div>

              <div>
                <label class="block text-sm font-bold mb-1">Status :</label>
                <select
                  v-model="editedTask.status"
                  class="itbkk-status shadow-sm p-3 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                >
                  <option value="NO_STATUS">No Status</option>
                  <option value="TO_DO">To Do</option>
                  <option value="DOING">Doing</option>
                  <option value="DONE">Done</option>
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
              'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500': isTitleValid,
              'bg-gray-300 cursor-not-allowed': !isTitleValid,
            }"
            @click="saveTask"
            :disabled="!isTitleValid"
          >
            SAVE
          </button>

          <button
            type="button"
            class="itbkk-button-cancel mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            @click="goBack()"
          >
          
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
  
</template>

<style scoped></style>