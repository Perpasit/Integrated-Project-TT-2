<script setup>
import HeaderBoard from "./HeaderBoard.vue"
</script>

<template>
  <div class="bg-gradient-to-br from-[#9194eb] to-[#bff7ff] min-h-screen">
    <HeaderBoard />
    <div class="flex items-center justify-center min-h-[75vh]">
      <div class="text-center flex flex-col bg-white rounded-xl p-16 px-36 shadow-lg border-t-[35px] border-red-500">
        <h1 class="text-5xl font-extrabold text-red-600 mb-6 leading-tight">
          Access Denied
        </h1>
        <p class="text-lg text-gray-700 mb-8">
          You do not have permission to view this page.
        </p>
        
      </div>
    </div>
  </div>
</template>

<style scoped></style>
