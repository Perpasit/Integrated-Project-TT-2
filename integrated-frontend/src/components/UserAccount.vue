<script setup>
import { useUserStore } from "../";

const userStore = useUserStore();
const accountName = computed(() => userStore.accountName);
</script>

<template>
  <header class="p-4 flex justify-between items-center">
    <div class="flex items-center invisible">
      test
    </div>
    <div class="flex items-center space-x-4">
      <span class="text-gray-700 font-medium">Hello, {{ accountName }}</span>
      <img
        src="https://via.placeholder.com/40"
        alt="User Avatar"
        class="rounded-full h-10 w-10"
      />
    </div>
  </header>
</template>
