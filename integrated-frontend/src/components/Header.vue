<script setup>
import { computed, ref } from "vue"
import { useRouter } from "vue-router"
import { useUserStore } from "../stores/userStore"

const router = useRouter()

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

const userStore = useUserStore()
const accountName = computed(() => userStore.accountName)
const showDropdown = ref(false)

const logout = () => {
  userStore.reset()
  router.push({ name: "Login" })
}

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}
</script>

<template>
  <div class="container mx-auto px-0 pt-5 flex flex-row justify-around items-center">
    <h1
      class="cursor-default text-5xl font-bold font-italiana pt-2 text-lime-100 mr-96 hover:scale-110 transition-all duration-200 ease-in-out"
      @click="goHome">
      Kanban
    </h1>
    <div class="flex items-center space-x-4">
      <span :class="[
        'itbkk-button-home text-xl block cursor-pointer py-2 px-3 rounded hover:scale-110 transition-all duration-200 ease-in-out',
        isHomePage ? 'text-white' : 'text-blue-600 hover:text-white'
      ]" @click="goHome">
        Home
      </span>
      <span :class="[
        'itbkk-manage-status text-xl block cursor-pointer py-2 px-3 rounded hover:scale-110 transition-all duration-200 ease-in-out',
        isManageStatusPage ? 'text-white' : 'text-blue-600 hover:text-white'
      ]" @click="goToManageStatus">
        Manage Status
      </span>
      <button v-show="isTaskPage" @click="addTask"
        class="itbkk-button-add hover:scale-110 transition-all duration-200 ease-in-out ml-4 bg-transparent hover:bg-gradient-to-r from-cyan-500 to-blue-500 text-blue-700 font-semibold hover:text-white py-3 px-5 border border-blue-500 shadow-lg shadow-blue-500/50 hover:border-transparent rounded">
        + NEW TASK
      </button>
      <button v-show="isManageStatusPage" @click="addStatus"
        class="itbkk-button-add hover:scale-110 transition-all duration-200 ease-in-out ml-4 bg-transparent hover:bg-gradient-to-r from-orange-300 to-amber-300 text-orange-700 font-semibold hover:text-white py-3 px-5 border border-orange-500 shadow-lg shadow-amber-500 hover:border-transparent rounded">
        ADD STATUS
      </button>

    </div>

    <header class="absolute right-0 p-4 flex items-center">
      <div class="flex items-center space-x-4 relative">
        <span
          class="itbkk-fullname bg-gradient-to-r from-purple-400 via-pink-500 to-red-500 bg-clip-text text-transparent font-medium">
          {{ accountName }}</span>
        <div
          class="border border-gradient-to-r from-blue-400 via-purple-500 to-red-500 p-2 rounded-full cursor-pointer flex items-center justify-center shadow-md hover:shadow-lg transition-all duration-200"
          @click="toggleDropdown">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-slate-500" fill="none" viewBox="0 0 24 24"
            stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
        </div>

        <div v-if="showDropdown" class="absolute top-full right-0 mt-2 w-32 bg-white shadow-lg rounded-lg p-2">
          <button @click="logout"
            class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left">
            Logout
          </button>
          <button @click="goToProfile"
            class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left mt-2">
            Profile
          </button>
          <button @click="goToSettings"
            class="text-gray-700 hover:text-white hover:bg-gray-700 px-4 py-2 rounded transition duration-150 ease-in-out w-full text-left mt-2">
            Settings
          </button>
        </div>
      </div>
    </header>
  </div>
</template>