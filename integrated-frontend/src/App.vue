<script setup>
import TaskTable from "./components/TaskTable.vue"
</script>

<template>
  <div
    id="app"
    class="bg-gradient-to-br from-[#9194eb] to-[#bff7ff] min-h-screen"
  >
    <div class="container mx-auto p-4">
      <h1 class="text-2xl font-bold mb-4 font-italiana">Kanban</h1>
      <TaskTable />
    </div>
  </div>
</template>

<style>
@import url("https://fonts.googleapis.com/css2?family=Italiana&display=swap");

.font-italiana {
  font-family: "Italiana", serif;
}
</style>
