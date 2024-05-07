<!-- ManageStatus.vue -->
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Header from './Header.vue'
import AddEditStatus from './AddEditStatus.vue'

const router = useRouter()
const statuses = ref([
    { id: 1, name: 'No Status', description: 'The default status' },
    { id: 2, name: 'To Do', description: '' },
    { id: 3, name: 'Doing', description: '' },
    { id: 4, name: 'Done', description: '' },
])
const selectedStatus = ref(null)
const showDeleteConfirmation = ref(false)
let selectedStatusToDelete = null

const goHome = () => {
    router.push({ name: 'TaskTable' })
}

const editStatus = (status) => {
    selectedStatus.value = { ...status }
}

const saveStatus = (status) => {
    if (status.id) {
        // update status
        const index = statuses.value.findIndex(s => s.id === status.id)
        statuses.value[index] = status
    } else {
        // add new status  
        status.id = Date.now()
        statuses.value.push(status)
    }
    selectedStatus.value = null
}

const confirmDelete = (status) => {
    selectedStatusToDelete = status
    showDeleteConfirmation.value = true
}

const deleteStatus = () => {
    const index = statuses.value.findIndex(s => s.id === selectedStatusToDelete.id)
    statuses.value.splice(index, 1)
    selectedStatusToDelete = null
    showDeleteConfirmation.value = false
}

const cancelDelete = () => {
    selectedStatusToDelete = null
    showDeleteConfirmation.value = false
}
</script>

<template>
    <div class="manage-status-page bg-gradient-to-t from-[#ffc3ee] to-[#A9C9FF] min-h-screen">
        <Header @addStatus="selectedStatus = { id: null, name: '', description: '' }" />

        <div class="container mx-auto px-4 pt-12">
            <div class="overflow-x-auto">
                <table class="table-fixed w-full max-w-6xl mx-auto border-separate border-spacing-y-2">

                    <thead>
                        <tr class="text-white w-full">
                            <th class="w-1/6 px-4 py-2 rounded-l-lg text-xl font-medium">No.</th>
                            <th class="w-1/4 px-4 py-2 text-xl font-medium">Name</th>
                            <th class="w-1/2 px-4 py-2 text-xl font-medium">Description</th>
                            <th class="w-1/4 px-4 py-2 rounded-r-lg text-xl font-medium">Action</th>
                        </tr>
                    </thead>


                    <tbody>
                        <tr v-for="(status, index) in statuses" :key="status.id"
                            class="bg-white shadow-lg hover:bg-gray-100">
                            <td class="px-4 py-5 rounded-l-lg text-center">{{ index + 1 }}</td>

                            <td class="itbkk-status-name px-4 py-5 truncate text-center">{{ status.name }}</td>

                            <td class="itbkk-status-description px-4 py-2 truncate text-center">{{ status.description }}
                            </td>

                            <td class="px-4 py-2 rounded-r-lg text-center">

                                <button
                                    class="itbkk-button-edit hover:scale-110 transition-all duration-200 ease-in-out"
                                    @click="editStatus(status)">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none"
                                        viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                    </svg>
                                </button>

                                <button
                                    class="itbkk-button-delete hover:scale-110 transition-all duration-200 ease-in-out ml-2"
                                    @click="confirmDelete(status)">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none"
                                        viewBox="0 0 24 24" stroke="red">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                    </svg>
                                </button>

                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>



            <div v-if="statuses.length <= 0" class="text-center mt-4 text-gray-500">No status</div>

            <AddEditStatus v-if="selectedStatus" :show="!!selectedStatus" :status="selectedStatus"
                @close="selectedStatus = null" @save="saveStatus" />
        </div>

        <div v-if="showDeleteConfirmation"
            class="fixed inset-0 flex items-center justify-center bg-gray-500 bg-opacity-75">
            <div class="bg-white p-8 rounded-lg">
                <p class="text-lg mb-4 itbkk-message">
                    Do you want to delete the <span class="text-red-500">{{ selectedStatusToDelete.name }}</span>
                    status?
                </p>

                <div class="flex justify-end">
                    <button class="itbkk-button-cancel bg-gray-200 text-gray-800 px-4 py-2 rounded mr-2"
                        @click="cancelDelete">Cancel</button>
                    <button class="itbkk-button-confirm bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded"
                        @click="deleteStatus">Delete</button>
                </div>

            </div>
        </div>
    </div>
</template>