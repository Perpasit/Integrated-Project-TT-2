<script setup>
import Task from './Task.vue';
const props = defineProps({
    tasks: Array
})
</script>
 
<template>
<div>
    <h1 class="text-2xl font-bold mb-4">Task Manager</h1>

    <Task v-for="task in tasks" :key="task.id" :task="task"/>

    <p v-if="tasks.length === 0" class="text-gray-500">No task found.</p>
</div>
</template>
 
<style scoped>

</style>