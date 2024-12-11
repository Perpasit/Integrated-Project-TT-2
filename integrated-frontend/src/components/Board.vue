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
    class="bg-gradient-to-br from-[#bdc2e8] to-[#e6dee9] min-h-screen"
  >
    <HeaderBoard />
    <PersonalBoard />
    <CollabBoard />
    <RouterView />
  </div>
</template>

<style></style>
