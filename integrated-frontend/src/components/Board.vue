<script setup>
import { ref } from "vue";
import HeaderBoard from "./HeaderBoard.vue";
import { RouterView } from "vue-router";
import CollabBoard from "./CollabBoard.vue";
import PersonalBoard from "./PersonalBoard.vue";
</script>

<template>
  <div
    id="app"
    class="bg-gradient-to-br from-[#c4e4e0] to-[#9ad4d3] min-h-screen"
  >
    <HeaderBoard />
    <PersonalBoard />
    <CollabBoard />
    <RouterView />
  </div>
</template>

<style></style>
