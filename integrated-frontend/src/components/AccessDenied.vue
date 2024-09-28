<script setup>
import HeaderBoard from "./HeaderBoard.vue"
</script>

<template>
  <div class="bg-gradient-to-br from-[#9194eb] to-[#bff7ff] min-h-screen flex flex-col items-center justify-center">
    <HeaderBoard />
    <div class="flex items-center justify-center min-h-screen ">
      <div class="text-center">
        <h1 class="text-6xl font-extrabold text-red-600 mb-6">Access Denied</h1>
        <p class="text-2xl text-gray-700 mb-52">
          You do not have permission to view this page.
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
