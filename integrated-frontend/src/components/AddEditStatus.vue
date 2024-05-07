<!-- AddEditStatus.vue -->
<script setup>
import { ref } from 'vue'

const props = defineProps({
  status: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['save', 'cancel'])

const name = ref(props.status.name)
const description = ref(props.status.description)

const save = () => {
  emit('save', {
    id: props.status.id,
    name: name.value,
    description: description.value
  })
}

const cancel = () => {
  emit('cancel')
}
</script>

<template>
  <template>
  <div class="modal-overlay fixed inset-0 flex items-center justify-center z-50" @click.self="cancel">
    <form class="modal itbkk-modal-status bg-white rounded-lg shadow-lg p-6" @submit.prevent>

      <h2 class="text-xl font-semibold mb-4">{{ props.status.id ? 'Edit Status' : 'Add Status' }}</h2>

      <div class="mb-4">
        <label class="block text-gray-700 font-semibold mb-2" for="name">Name:</label>
        <input class="itbkk-status-name border border-gray-400 p-2 w-full rounded" id="name" type="text" v-model="name" required>
      </div>

      <div class="mb-6">
        <label class="block text-gray-700 font-semibold mb-2" for="description">Description:</label>
        <textarea class="itbkk-status-description border border-gray-400 p-2 w-full rounded" id="description" v-model="description" rows="3"></textarea>
      </div>

      <div class="flex justify-end">
        <button @click.prevent="cancel" class="itbkk-button-cancel bg-red-500 hover:bg-red-700 text-white font-semibold py-2 px-4 rounded mr-2">Cancel</button>
        <button type="submit" @click.prevent="save" class="itbkk-button-confirm bg-green-500 hover:bg-green-700 text-white font-semibold py-2 px-4 rounded">Save</button>
      </div>

    </form>
    
  </div>
</template>
</template>