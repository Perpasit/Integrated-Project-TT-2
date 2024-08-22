<script setup>
import { computed } from "vue"
import { useRouter } from "vue-router"

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
</script>

<template>
  <div
    class="container mx-auto px-0 pt-5 flex flex-row justify-around items-center"
  >
    <h1
      class="cursor-default text-5xl font-bold font-italiana pt-2 text-lime-100 mr-96 hover:scale-110 transition-all duration-200 ease-in-out"
      @click="goHome"
    >
      Kanban
    </h1>
    <div class="flex items-center space-x-4">
      <span
        :class="[
          'itbkk-button-home text-xl block cursor-pointer py-2 px-3 rounded hover:scale-110 transition-all duration-200 ease-in-out',
          isHomePage ? 'text-white' : 'text-blue-600 hover:text-white'
        ]"
        @click="goHome"
      >
        Home
      </span>
      <span
        :class="[
           'itbkk-manage-status text-xl block cursor-pointer py-2 px-3 rounded hover:scale-110 transition-all duration-200 ease-in-out',
          isManageStatusPage ? 'text-white' : 'text-blue-600 hover:text-white'
        ]"
        @click="goToManageStatus"
      >
        Manage Status
      </span>
      <button
        v-show="isTaskPage"
        @click="addTask"
        class="itbkk-button-add hover:scale-110 transition-all duration-200 ease-in-out ml-4 bg-transparent hover:bg-gradient-to-r from-cyan-500 to-blue-500 text-blue-700 font-semibold hover:text-white py-3 px-5 border border-blue-500 shadow-lg shadow-blue-500/50 hover:border-transparent rounded"
      >
        + NEW TASK
      </button>
      <button
        v-show="isManageStatusPage"
        @click="addStatus"
        class="itbkk-button-add hover:scale-110 transition-all duration-200 ease-in-out ml-4 bg-transparent hover:bg-gradient-to-r from-orange-300 to-amber-300 text-orange-700 font-semibold hover:text-white py-3 px-5 border border-orange-500 shadow-lg shadow-amber-500 hover:border-transparent rounded"
      >
        ADD STATUS
      </button>
     
    </div>
  </div>
</template>