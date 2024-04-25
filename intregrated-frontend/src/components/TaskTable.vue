<template>
  <div class="container mx-auto p-4">
    <table class="table-auto w-full border-collapse">
      <thead>
        <tr class="bg-gray-200">
          <th class="px-4 py-2 border"></th>
          <th class="px-4 py-2 border">Title</th>
          <th class="px-4 py-2 border">Assignees</th>
          <th class="px-4 py-2 border">Status</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(task, index) in tasks" :key="index" class="border-b hover:bg-gray-100">
          <td class="px-4 py-2">{{ index + 1 }}</td>
          <td class="px-4 py-2 itbkk-title">{{ task.title }}</td>
          <td class="px-4 py-2 itbkk-assignees text-center">{{ task.assignees.join(', ') }}</td>
          <td class="px-4 py-2 itbkk-status">
          <span class="px-2 py-1 rounded-full text-center" :class="getStatusClass(task.status)">{{ task.status }}</span>
        </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      tasks: [
        { title: 'Design Backend API for PBI 1.2', assignees: ['Pang', 'Pong'], status: 'No status' },
        { title: 'Design UI for PBI 1.2', assignees: ['Ping'], status: 'Todo' },
        { title: 'Gut', assignees: ['teaw'], status: 'Done' }
      ]
    }
  },
  methods: {
    getStatusClass(status) {
      switch (status) {
        case 'No status':
          return 'bg-gray-400 text-white';
        case 'Todo':
          return 'bg-yellow-400 text-black';
        case 'Doing':
          return 'bg-blue-400 text-white';
        case 'Done':
          return 'bg-green-400 text-white';
        default:
          return 'bg-gray-400 text-white';
      }
    }
  }
}
</script>