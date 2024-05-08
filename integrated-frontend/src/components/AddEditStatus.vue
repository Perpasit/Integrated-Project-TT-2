<!-- AddEditStatus.vue -->
<script setup>
import { ref } from 'vue'

const props = defineProps({
    show: {
        type: Boolean,
        default: false
    },
    status: {
        type: Object,
        required: true
    }
})

const emit = defineEmits(['save', 'close'])

const name = ref(props.status.name)
const description = ref(props.status.description)

const save = () => {
    emit('save', {
        id: props.status.id,
        name: name.value,
        description: description.value
    })
}

const close = () => {
    emit('close')
}
</script>

<template>
    <div v-if="show" class="fixed z-10 inset-0 overflow-y-auto">
        <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            <div class="fixed inset-0 transition-opacity" aria-hidden="true">
                <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
            </div>

            <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>

            <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-3xl sm:w-full">
                <form @submit.prevent="save">

                    <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4 ">
                        <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">
                            {{ props.status.id ? 'Edit Status' : 'Add Status' }}
                        </h3>

                        <div class="mb-4 ">
                            <label for="name" class="block text-sm font-medium text-gray-700">Name</label>
                            <input type="text" name="name" id="name" class="itbkk-status-name mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-2xl border-gray-300 rounded-md" v-model="name" required>
                        </div>

                        <div>
                            <label for="description" class="block text-sm font-medium text-gray-700">Description</label>
                            <textarea id="description" name="description" rows="5" class="itbkk-status-description mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md" v-model="description"></textarea>
                        </div>

                    </div>

                    <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">

                        <button type="submit" class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-green-600 text-base font-medium text-white hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 sm:ml-3 sm:w-auto sm:text-sm">
                            Save
                        </button>

                        <button type="button" class="itbkk-button-cancel mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm" @click="close">
                            Cancel
                        </button>

                    </div>
                </form>
            </div>
        </div>
    </div>
</template>