<<<<<<< Updated upstream
<template>
  <div id="app" class="container mx-auto p-4">
    <h1 class="text-2xl font-bold mb-4">IT-Bangmod Kradan Kanban (ITB-KK)</h1>
    <task-table />
  </div>
</template>

<script>
import TaskTable from './components/TaskTable.vue'

export default {
  name: 'App',
  components: {
    TaskTable
  }
}
</script>
=======
<script setup>
import DemoGrid from "./components/Grid.vue"
import test from "./components/test.vue"

import { ref } from "vue"

const searchQuery = ref("")
const gridColumns = ["id", "Title", "Assignees", "Status"]
const gridData = [
  { id: "1", Title: "Chuck Norris", Assignees: Infinity, Status: "to do" },
  { id: "2", Title: "Bruce Lee", Assignees: 9000, Status: "doing" },
  { id: "3", Title: "Jackie Chan", Assignees: 7000, Status: "done" },
  { id: "4", Title: "Jet Li", Assignees: 8000, Status: "No status" },
]
// const tasks = ref()
// getItems(`${import.meta.env.VITE_API_ENDPOINT}tasks`).then((res) => {
//   tasks.value = res
// })

</script>

<template>
  <test> </test>
  <h1 class="flex justify-center font-bold text-6xl">IT-Bangmod Kradan Kandan</h1>
  <div class="flex justify-end">
    <form id="search" class="">
      Search
      <input
        name="query"
        v-model="searchQuery"
        class="border border-black rounded-md"
      />
    </form>
  </div>
  <DemoGrid :data="gridData" :columns="gridColumns" :filter-key="searchQuery">
  </DemoGrid>
</template>

<style scoped></style>
>>>>>>> Stashed changes
