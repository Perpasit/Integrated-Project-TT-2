<script setup>
import { defineProps } from 'vue';

const props = defineProps({
  task: Object
});

const changeStatus = (event) => {
  const completed = event.target.value === 'true';
  props.task.completed = completed;
};
</script>
 
<template>
  <div :class="{'bg-gray': task.completed}" class="rounded-lg p-4 shadow-md m-4 border-2">
    <h2 class="text-lg font-semibold">{{ task.title }}</h2>
    <p class="text-gray-700">Description: {{ task.description }}</p>
    <div class="mt-2 flex items-center">
      <span class="text-gray-600 mr-2">Assignees: </span>
      <ul class="flex">
        <li v-for="assignee in task.assignees" :key="assignee.id" class="mr-2">{{ assignee.name }}</li>
      </ul>
    </div>
    <div class="mt-2 flex items-center">
      <span class="text-gray-600 mr-2">Created On: </span>
      <span>{{ task.createdAt }}</span>
    </div>
    <div class="mt-2 flex items-center">
      <span class="text-gray-600 mr-2">Updated On:</span>
      <span>{{ task.updatedAt }}</span>
    </div>
    <span>Status: </span>
    <select @change="changeStatus" class="mt-2 px-2 text-white rounded-md" :class="{'bg-green-500': task.completed, 'bg-red-500': !task.completed }">
        
      <option :value="true" :selected="task.completed">Complete</option>
      <option :value="false" :selected="!task.completed">Incomplete</option>
    </select>
  </div>
</template>
 
<style scoped>
</style>
