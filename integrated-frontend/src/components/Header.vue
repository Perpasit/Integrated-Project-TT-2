<script setup>
import { computed, ref, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useUserStore } from "../stores/userStore"

const router = useRouter()
const userStore = useUserStore()

const isHomePage = computed(() => {
  return router.currentRoute.value.path === "/task"
})

const isManageStatusPage = computed(() => {
  return router.currentRoute.value.path.startsWith("/status/manage")
})

const isTaskPage = computed(() => {
  return router.currentRoute.value.path.startsWith("/task")
})

const addTask = () => {
  router.push({ name: "AddTask" })
}

const goHome = () => {
  router.push({ name: "home" })
}

const goToManageStatus = () => {
  router.push({ name: "ManageStatus" })
}

const addStatus = () => {
  router.push({ name: "AddStatus" })
}

const accountName = computed(() => userStore.getAccountName)
const showDropdown = ref(false)

const logout = () => {
  userStore.logout()
  router.push({ name: "Login" })
}

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

onMounted(() => {
  // Ensure the store is initialized with data from localStorage
  if (!userStore.getIsAuthenticated) {
    router.push({ name: "Login" })
  }
})
</script>

<template>
  <div class="container mx-auto px-0 pt-5 flex flex-row justify-around items-center">
    <div class="flex">
    <h1
      class="cursor-default text-6xl font-bold font-italiana pt-2 text-lime-100  hover:scale-110 transition-all duration-200 ease-in-out mr-4"
      @click="goHome">
      Kanban
    </h1>
    <span :class="[
        'itbkk-button-home text-2xl  cursor-pointer px-3 rounded hover:scale-110 transition-all duration-200 ease-in-out  flex justify-end items-end ',
        isHomePage ? 'text-white' : 'text-blue-600 hover:text-white'
      ]" @click="goHome">
        Home
      </span>
      <span :class="[
        'itbkk-manage-status text-2xl cursor-pointer  px-3 rounded hover:scale-110 transition-all duration-200 ease-in-out flex justify-end items-end',
        isManageStatusPage ? 'text-white' : 'text-blue-600 hover:text-white'
      ]" @click="goToManageStatus">
        Manage Status
      </span>
    </div>


    <div class="flex items-end space-x-4">

      <div class="account px-4 pt-4 pr-3 flex items-center">
      <div class="flex items-end space-x-4 relative">
        <span
          class="itbkk-fullname  text-blue-600 text-xl items-end font-medium">
          {{ accountName }}</span>
        <div
          class="border   p-2 rounded-full cursor-pointer flex items-center justify-center shadow-md hover:scale-110 transition-all duration-200 ease-in-out"
          @click="toggleDropdown">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-slate-500 " fill="none" viewBox="0 0 24 24"
            stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
        </div>

        <div v-if="showDropdown" class="absolute top-full right-0 mt-2 w-32 bg-white shadow-lg rounded-lg p-2">
        
          <button @click="goToProfile"
            class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left ">
            Profile
          </button>
          <button @click="goToSettings"
            class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left">
            Settings
          </button>
          <button @click="logout"
            class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left">
            Logout
          </button>
        </div>
      </div>
    </div>


      
      <button v-show="isTaskPage" @click="addTask"
        class="itbkk-button-add hover:scale-110 transition-all duration-200 ease-in-out ml-4 bg-transparent hover:bg-gradient-to-r from-cyan-500 to-blue-500 text-blue-700 font-semibold hover:text-white py-3 px-5 border border-blue-500 shadow-lg shadow-blue-500/50 hover:border-transparent rounded">
        + NEW TASK
      </button>
      <button v-show="isManageStatusPage" @click="addStatus"
        class="itbkk-button-add hover:scale-110 transition-all duration-200 ease-in-out ml-4 bg-transparent hover:bg-gradient-to-r from-orange-300 to-amber-300 text-orange-700 font-semibold hover:text-white py-3 px-5 border border-orange-500 shadow-lg shadow-amber-500 hover:border-transparent rounded">
        ADD STATUS
      </button>

    </div>

   

  </div>
</template>