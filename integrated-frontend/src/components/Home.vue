
<script setup>
import { ref } from "vue"
import TaskTable from "./TaskTable.vue"
import Header from "./Header.vue"
import { useModalStore } from "../stores/useModalStore"

const modalStore = useModalStore()

</script>

<template>
  
  <div
    id="app"
    class="bg-gradient-to-br from-[#9194eb] to-[#bff7ff] min-h-screen"
  >
  
    <Header />
    <TaskTable />

    <div
      v-if="modalStore.showTaskSuccessModal"
      class="fixed z-50 inset-0 overflow-y-auto"
    >
      <div class="flex items-start justify-center min-h-screen">
        <div
          class="bg-white mt-2 rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full"
        >
          <div class="bg-white px-4 sm:p-4 sm:pb-4 py-0">
            <div class="flex justify-between items-center">
              <div class="text-lg leading-6 font-medium text-gray-900">
                Success
              </div>
              <button
                type="button"
                class="text-gray-400 hover:text-gray-500 focus:outline-none focus:text-gray-500 transition ease-in-out duration-150"
                @click="modalStore.closeTaskSuccessModal()"
              >
                <svg
                  class="h-6 w-6"
                  stroke="currentColor"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M6 18L18 6M6 6l12 12"
                  ></path>
                </svg>
              </button>
            </div>
            <div>
              <p class="itbkk-message text-sm leading-5 text-gray-500">
                The task "{{ modalStore.taskTitle }}" is
                {{
                  modalStore.mode === "add" ? "added" : "edited"
                }}
                successfully.
              </p>
            </div>
          </div>
          <div class="bg-gray-50 px-4 py-1 sm:px-6 sm:flex sm:flex-row-reverse">
            <span class="flex w-full rounded-md shadow-sm sm:ml-3 sm:w-auto">
              <button
                type="button"
                class="inline-flex justify-center w-full rounded-md border border-transparent px-4 py-2 bg-indigo-600 text-base leading-6 font-medium text-white shadow-sm hover:bg-indigo-500 focus:outline-none focus:border-indigo-700 focus:shadow-outline-indigo transition ease-in-out duration-150 sm:text-sm sm:leading-3"
                @click="modalStore.closeTaskSuccessModal()"
              >
                OK
              </button>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>

  <router-view />
</template>

<style>
@import url("https://fonts.googleapis.com/css2?family=Italiana&display=swap");

.font-italiana {
  font-family: "Italiana", serif;
}
</style>