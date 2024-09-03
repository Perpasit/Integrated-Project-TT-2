<script setup>
import { ref } from "vue";
import HeaderBoard from "./HeaderBoard.vue";
import BoardTable from "./BoardTable.vue";
</script>

<template>
  <div
    id="app"
    class="bg-gradient-to-br from-[#9194eb] to-[#bff7ff] min-h-screen"
  >
    <HeaderBoard />
    <BoardTable />
  </div>
</template>

<style></style>
