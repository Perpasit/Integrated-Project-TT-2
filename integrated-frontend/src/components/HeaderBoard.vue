<script setup>
import { computed, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/userStore";

const router = useRouter();
const userStore = useUserStore();

const addTask = () => {
  router.push({ name: " " });
};

const goHome = () => {
  router.push({ name: "Board" });
};

const accountName = computed(() => userStore.getAccountName);
const showDropdown = ref(false);

const logout = () => {
  userStore.logout();
  router.push({ name: "Login" });
};

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

onMounted(() => {
  if (!userStore.getIsAuthenticated) {
    router.push({ name: "Login" });
  }
});
</script>

<template>
  <div
    class="container mx-auto px-0  pt-5 flex flex-row justify-around items-center"
  >
    <div class="flex">
      <h1
        class="cursor-default text-6xl font-bold font-italiana pt-2 text-lime-100 hover:scale-110 transition-all duration-200 ease-in-out mr-4"
        @click="goHome"
      >
        Kanban
      </h1>
    </div>

    <div class="flex items-end space-x-4">
      <div class="account px-4 pt-4 pr-3 flex items-center">
        <div class="flex items-end space-x-4 relative">
          <span
            class="itbkk-fullname text-blue-600 text-xl items-end font-medium"
          >
            {{ accountName }}</span
          >
          <div
            class="border p-2 rounded-full cursor-pointer flex items-center justify-center shadow-md hover:scale-110 transition-all duration-200 ease-in-out"
            @click="toggleDropdown"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-6 w-6 text-slate-500"
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

          <div
            v-if="showDropdown"
            class="absolute top-full right-0 mt-2 w-32 bg-white shadow-lg rounded-lg p-2"
          >
            <button
              @click="goToProfile"
              class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left"
            >
              Profile
            </button>
            <button
              @click="goToSettings"
              class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left"
            >
              Settings
            </button>
            <button
              @click="logout"
              class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
