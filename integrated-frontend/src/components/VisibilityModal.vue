<script setup>
import { ref, watch } from 'vue'
const props = defineProps({
  board: Object,
  newVisibility: String,
})

// // Watch props to verify if they're passed correctly
// watch(() => props.board, (newVal) => {
//   console.log("Board in modal:", newVal)
// })

// watch(() => props.newVisibility, (newVal) => {
//   console.log("New visibility in modal:", newVal)
// })

const emit = defineEmits(['close', 'confirm'])

const handleConfirm = () => {
  emit('confirm')
}

const handleClose = () => {
  emit('close')
}
</script>

<template>
    <div v-if="board" class="flex items-center justify-center min-h-screen bg-gray-500 bg-opacity-75 fixed z-10 inset-0 overflow-y-auto">
      <div class="bg-white p-6 rounded-lg shadow-lg max-w-lg w-full">
        <h3 class="text-xl font-semibold text-gray-800 mb-4">Board visibility changed!</h3>
        <p class="text-gray-600 mb-6">
          <span v-if="board.visibility === 'PRIVATE'">
            In public, anyone can view the board, task list, and task details of tasks in the board. Do you want to change the visibility to Public?
          </span>
          <span v-else>
            In private, only the board owner can access/control the board. Do you want to change the visibility to Private?
          </span>
        </p>
        <div class="flex justify-end space-x-3">
          <button 
            @click="handleClose" 
            class="bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded transition ease-in-out">
            Cancel
          </button>
          <button 
            @click="handleConfirm" 
            class="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded transition ease-in-out">
            Confirm
          </button>
        </div>
      </div>
    </div>
  </template>
