<script setup>
import { computed } from "vue"
import { useUserStore } from "../stores/userStore"

const userStore = useUserStore()
const accountName = computed(() => userStore.accountName)
</script>

<template>
  <header class="p-4 flex justify-between items-center">
    <div class="flex items-center invisible">test</div>
    <div class="flex items-center space-x-4">
      <span class="text-gray-700 font-medium">Hello, {{ accountName }}</span>
      <div class="border border-black p-1 rounded-full"> 
        <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-5 w-5 text-black"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
        />
      </svg>
    </div>
     
    </div>
  </header>
</template>
