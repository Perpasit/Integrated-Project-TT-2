
<script>
import TaskTable from './components/TaskTable.vue'

export default {
  name: 'App',
  components: {
    TaskTable
  }
}

</script>

<template>
  <div id="app" class="container mx-auto p-4 ">
    <h1 class="text-2xl font-bold mb-4">IT-Bangmod Kradan Kanban (ITB-KK)</h1>
    <task-table />
  </div>
</template>
