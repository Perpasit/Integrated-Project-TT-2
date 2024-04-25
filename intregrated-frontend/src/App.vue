<script setup>
import Board from "./components/board/Board.vue"
import HeaderVue from "./components/Header.vue"
import TaskView from "./components/manager/TaskView.vue"


import { onMounted } from "vue"
import { useBoardsStore } from "./stores/boards.js"
import { useManagerStore } from "./stores/manager.js"

const boardsStore = useBoardsStore()
const managerStore = useManagerStore()

onMounted(async () => {
  await boardsStore.$subscribe((mutations, state) => {
    localStorage.setItem("boards", JSON.stringify(state))
  })
  const storageData = localStorage.getItem("boards")
  if (storageData === null) {
    const jsonData = await import("../data/db.json")
    boardsStore.boards = jsonData.boards
  } else {
    boardsStore.$state = JSON.parse(storageData)
  }

})


</script>

<template>
  <HeaderVue />
  <main>
    <div class="flex w-full">
      <div
        v-dragscroll:nochilddrag
        class="relative h-full w-screen max-h-[calc(100vh-64px)] bg-light-grey dark:bg-very-dark-grey"
      >
        <div
          data-dragscroll
          class="p-6 w-full overflow-auto max-h-[calc(100vh-64px)] transition-all"
        >
          <Board data-dragscroll v-if="boardsStore.getColumns" />
          
        </div>
      </div>
    </div>
  </main> 
  
  

</template>
