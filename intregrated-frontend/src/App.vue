<script setup>
// import HelloWorld from './components/HelloWorld.vue'
import DemoGrid from './components/Grid.vue'
import { ref } from 'vue'

const searchQuery = ref('')
const gridColumns = ['id','Title', 'Assignees','Status']
const gridData = [
  {'id':'1',Title: 'Chuck Norris', Assignees: Infinity ,Status:'to do'},
  { 'id':'2',Title: 'Bruce Lee', Assignees: 9000,Status:'doing' },
  { 'id':'3',Title: 'Jackie Chan', Assignees: 7000 ,Status:'done'},
  { 'id':'4',Title: 'Jet Li', Assignees: 8000 ,Status:'No status'}
]
</script>

<template>
  <h1>IT-Bangmod Kradan Kandan</h1>
  <div class=" flex  justify-center ">
  <div><form id="search" class=" justify">
    Search <input name="query" v-model="searchQuery">
  </form>
  <DemoGrid
    :data="gridData"
    :columns="gridColumns"
    :filter-key="searchQuery">
  </DemoGrid></div>
</div>
</template>

<style scoped>

</style>
