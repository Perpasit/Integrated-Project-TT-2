<!-- ModalStatus.vue -->
<script setup>
import { ref } from 'vue'

const props = defineProps({
    status: {
        type: Object,
        required: true,
    },
    show: {
        type: Boolean,
        default: false,
    },
})

const emit = defineEmits(['close'])

const closeModal = () => {
    emit('close')
}

const localTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone

const parseAndFormatDate = (dateString) => {
    const date = new Date(dateString)
    if (isNaN(date)) {
        return "Invalid Date"
    }
    return date.toLocaleString("en-GB", { timeZone: localTimeZone })
}

</script>

<template>

    <div v-if="show"
        class="itbkk-modal-status fixed inset-0 flex items-center justify-center bg-gray-500 bg-opacity-75">
        <div
            class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-3xl sm:w-full">

            <div class="mt-5 sm:mt-6 sm:mx-6">
                <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">Details</h3>

                

                <div class="px-4 py-5">
                    <div class="mb-4">
                        <label class="block text-sm font-medium text-gray-700">Name</label>
                        <textarea rows="2" readonly v-model="status.name"
                            class="itbkk-status-name w-full border border-gray-300 rounded-md p-1 shadow-sm mt-1"></textarea>
                    </div>
                    <div class="mb-4">
                        <label class="text-sm font-medium text-gray-500">Description</label>
                        <textarea v-if="status.description" rows="6" v-model="status.description"
                            class="itbkk-status-description w-full border border-gray-300 rounded-md p-1 shadow-sm mt-2 text-sm text-gray-600"
                            readonly>{{ status.description }}</textarea>
                        <textarea v-else rows="6" v-model="status.description"
                            class="itbkk-status-description w-full border border-gray-300 rounded-md p-1 shadow-sm mt-2 text-sm text-gray-400 italic"
                            placeholder="No Description Provided" readonly>No Description Provided</textarea>
                    </div>
                    <div class="flex justify-between mb-4">
                        <div>
                            <label class="text-sm font-medium text-gray-500">TimeZone</label>
                            <label class="itbkk-timezone block">{{ localTimeZone }}</label>
                        </div>
                        <div>
                            <label class="text-sm font-medium text-gray-500">Created On</label>
                            <label class="itbkk-created-on block">{{ parseAndFormatDate(status.createdOn) }}</label>
                        </div>
                        <div>
                            <label class="text-sm font-medium text-gray-500">Updated On</label>
                            <label class="itbkk-updated-on block">{{ parseAndFormatDate(status.updatedOn) }}</label>
                        </div>
                    </div>
                </div>
            </div>
            <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                <button type="button"
                    class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm"
                    @click="closeModal">OK</button>
            </div>
        </div>
    </div>
</template>
