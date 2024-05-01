<script setup>
const emit = defineEmits(["openModal"])
</script>

<template>
  <div class="container mx-auto px-0 pt-5 flex flex-row justify-between">
    <h1 class="text-4xl font-bold font-italiana pt-2 text-lime-100">Kanban</h1>
    <button
      @click="$emit('openModal',true)"
      class="itbkk-button-add bg-transparent hover:bg-gradient-to-r from-cyan-500 to-blue-500 text-blue-700 font-semibold hover:text-white py-2 px-4 border border-blue-500 shadow-lg shadow-blue-500/50 hover:border-transparent rounded"
    >
      + NEW TASK
    </button>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Italiana&display=swap");

.font-italiana {
  font-family: "Italiana", serif;
}
</style>
