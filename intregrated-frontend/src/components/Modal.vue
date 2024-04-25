<template>
    <div v-if="task" class="fixed z-10 inset-0 overflow-y-auto">
      <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div class="fixed inset-0 transition-opacity" aria-hidden="true">
          <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
        </div>
        <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
        <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full" role="dialog" aria-modal="true" aria-labelledby="modal-headline">
          <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div>
              <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-headline">
                <strong>Title:</strong>
                <input type="text" v-model="editedTask.title" class="w-full border">
              </h3>
              <div class="mt-2">
                <p class="text-sm text-gray-500 ">
                  <strong>Description:</strong>
                  <textarea v-model="editedTask.description" class="w-full border"></textarea>
                </p>
                <p class="text-sm text-gray-500 mt-2">
                  <strong>Assignees:</strong>
                  <input type="text" v-model="editedTask.assignees" class="w-full border">
                </p>
                <p class="text-sm text-gray-500 mt-2">
                  <strong>Status:</strong>
                  <select v-model="editedTask.status" class="w-full border">
                    <option value="No status">No status</option>
                    <option value="Todo">Todo</option>
                    <option value="Doing">Doing</option>
                    <option value="Done">Done</option>
                  </select>
                </p>
                <p class="text-sm text-gray-500 mt-2">
                  <strong>Created On:</strong> {{ task.createdOn }}
                </p>
                <p class="text-sm text-gray-500 mt-2">
                  <strong>Updated On:</strong> {{ task.updatedOn }}
                </p>
              </div>
            </div>
          </div>
          <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button type="button" class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-blue-600 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:ml-3 sm:w-auto sm:text-sm" @click="saveTask">
              OK
            </button>
            <button type="button" class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm" @click="$emit('close')">
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    props: ['task'],
    data() {
      return {
        editedTask: { ...this.task }
      }
    },
    methods: {
      saveTask() {
        this.$emit('save', this.editedTask);
      }
    }
  }
  </script>