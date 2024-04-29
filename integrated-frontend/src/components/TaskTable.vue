<script setup>
import { ref, onMounted, computed } from 'vue'
import { useTaskStore } from '../stores/task'
import Modal from './Modal.vue'


const taskStore = useTaskStore()
const tasks = ref([])
const selectedTask = ref(null)
const sortOrder = ref('asc')


const showModal = (task) => {
  selectedTask.value = task
}

const closeModal = () => {
  selectedTask.value = null
}

const saveTask = async (updatedTask) => {
  if (updatedTask.id) {
    await taskStore.updateTask(updatedTask.id, updatedTask)
  } else {
    await taskStore.addTask(updatedTask)
  }
  closeModal()
}

const formattedStatus = computed(() => {
  return (task) => {
    const status = task.status.toLowerCase();
    let formatted = '';
    let capitalizeNext = true;

    for (let i = 0; i < status.length; i++) {
      const char = status[i];
      if (char === '_') {
        formatted += ' ';
        capitalizeNext = true;
      } else {
        formatted += capitalizeNext ? char.toUpperCase() : char;
        capitalizeNext = false;
      }
    }

    return formatted;
  }
})



const sortedTasks = computed(() => {
  return [...tasks.value].sort((a, b) => {
    if (sortOrder.value === 'asc') {
      return new Date(a.createdOn) - new Date(b.createdOn)
    } else {
      return new Date(b.createdOn) - new Date(a.createdOn)
    }
  })
})

const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
}



onMounted(async () => {
  await taskStore.fetchAllTasks()
  tasks.value = taskStore.getTasks

})

</script>

<template>
  
  <div class="container mx-auto p-4">

    <table class="table-fixed w-full max-w-4xl mx-auto border-collapse">
      
      <thead>
        <tr class="bg-gray-200">
          <th class="w-12 px-4 py-2 border">#</th>
          <th class="w-1/3 px-4 py-2 border cursor-pointer" @click="toggleSortOrder">Title</th>
          <th class="w-1/3 px-4 py-2 border">Assignees</th>
          <th class="w-1/6 px-4 py-2 border">Status</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(task, index) in sortedTasks" :key="index" class="border-b hover:bg-gray-100 itbkk-item">
          <td class="px-4 py-2 text-center">{{ index + 1 }}</td>
          <td class="px-4 py-2 itbkk-title"  >  
            <p style="word-break: break-word; overflow-wrap: break-word" @click="showModal(task)" class="cursor-pointer" > 
              {{ task.title }} </p>
          </td>
          <td class="px-4 py-2 itbkk-assignees text-center">
            <template v-if="task.assignees">
              <span v-for="(assignee, index) in task.assignees.split(',')" :key="index">
                {{ assignee.trim() }}
              </span>
            </template>
            <span v-else class="text-gray-400 italic itbkk-assignees">Unassigned</span>
          </td>
          <td class="px-4 py-2 itbkk-status text-center">
            <span class="inline-block px-2 py-1 rounded-full text-center"
              :class="taskStore.getStatusClass(formattedStatus(task))">
              {{ formattedStatus(task) }}
            </span>
          </td>
          <!-- <td class="px-4 py-2 itbkk-status text-center"> -->
            <!-- <span class="inline-block px-2 py-1 rounded-full text-center"
              :class="taskStore.getStatusClass(formattedStatus(task))">
              {{ formattedStatus(task).toUpperCase() }}
            </span> -->
            <!-- {{ formattedStatus(task.status) }}
          </td> -->
        </tr>
      </tbody>
    </table>
    <span v-if="tasks.length<=0" class="flex justify-center">No task</span>
    <modal v-if="selectedTask" :task="selectedTask" @close="closeModal" @save="saveTask" />
  </div>
</template>
