<script setup>

import { ref } from "vue"
import TaskTable from "./TaskTable.vue"
import Header from "./Header.vue"
import AddEditTask from "./AddEditTask.vue";

const showTask = ref(false)
const showModal = (flagModal) => {
  showTask.value = flagModal
}

const clearTask = (flagModal , newTask) =>{
  showTask.value = flagModal
}


</script>


<template>
  <div id="app" class="bg-gradient-to-br from-[#9194eb] to-[#bff7ff] min-h-screen ">
      <Header @openModal="showModal" />
      <TaskTable />   
  </div>
  <Teleport to="#AddEditTask">
    <AddEditTask v-if="showTask" @closeModal="clearTask"  />
  </Teleport>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Italiana&display=swap');

.font-italiana {
  font-family: 'Italiana', serif;
}
</style>