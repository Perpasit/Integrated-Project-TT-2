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

const deleteStatus = (status) => {
  // delete status
  const index = statuses.value.findIndex(s => s.id === status.id)
  statuses.value.splice(index, 1)
}
</script>

<template>
  <div class="manage-status-page bg-gradient-to-t from-[#ffc3ee] to-[#A9C9FF] min-h-screen">
    <Header @addStatus="selectedStatus = { id: null, name: '', description: '' }" />
    <div class="container mx-auto">
      <!-- ใช้ component AddEditStatus เมื่อมีการเลือก status -->
      <AddEditStatus v-if="selectedStatus" :status="selectedStatus" @save="saveStatus" @cancel="selectedStatus = null" />
      
      <table class="table-auto w-full">
        <thead>
          <tr>
            <th class="px-4 py-2"></th>
            <th class="px-4 py-2">Name</th>
            <th class="px-4 py-2">Description</th>
            <th class="px-4 py-2"></th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="status in statuses" :key="status.id">
            <td class="itbkk-status-name border px-4 py-2">{{ status.name }}</td>
            <td class="itbkk-status-description border px-4 py-2">{{ status.description }}</td>
            <td class="border px-4 py-2"></td>
            <td class="border px-4 py-2">
              <button class="itbkk-button-edit px-2 py-1 bg-blue-500 text-white rounded" @click="editStatus(status)">Edit</button>
              <button class="itbkk-button-delete px-2 py-1 bg-red-500 text-white rounded" @click="deleteStatus(status)">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>