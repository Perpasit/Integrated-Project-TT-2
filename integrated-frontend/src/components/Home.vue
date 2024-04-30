<template>
  <div id="app" class="bg-gradient-to-br from-[#9194eb] to-[#bff7ff] min-h-screen ">
      <Header/>
      <TaskTable />   
  </div>

</template>
<script setup>
import TaskTable from "./TaskTable.vue"
import Header from "./Header.vue"
</script>
<style>
@import url('https://fonts.googleapis.com/css2?family=Italiana&display=swap');

.font-italiana {
  font-family: 'Italiana', serif;
}
</style>