<script setup>
import DemoGrid from "./components/Grid.vue"
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
