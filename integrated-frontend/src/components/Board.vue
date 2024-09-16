<script setup>
import { ref } from "vue";
import HeaderBoard from "./HeaderBoard.vue";
import BoardTable from "./BoardTable.vue";
import { RouterView } from "vue-router";
</script>

<template>
  <div
    id="app"
    class="bg-gradient-to-br from-[#c4e4e0] to-[#9ad4d3] min-h-screen"
  >
    <HeaderBoard />
    <BoardTable />
    <RouterView />
  </div>
</template>

<style></style>
