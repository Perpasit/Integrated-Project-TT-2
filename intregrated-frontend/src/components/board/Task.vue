<template>
  <article class="group flex flex-col bg-white dark:bg-dark-grey p-4 rounded-lg cursor-pointer shadow-task max-w-[280px] border-2">
    <h3 :class="{'text-main-purple': managerStore.dragging, 'text-black': !managerStore.dragging}" class="font-bold select-none pointer-events-none">
      {{ task.title }}
    </h3>
    <p class="text-xs text-medium-grey font-bold select-none pointer-events-none">{{ task.description }} (Desc)</p>
  </article>
</template>

<script setup>
import { computed } from 'vue';
import { useManagerStore } from '../../stores/manager';

const props = defineProps({
  task: {
    type: Object,
    required: true
  }
});

const managerStore = useManagerStore();

const subtasksCompleted = computed(() => {
  const completed = props.task.subtasks.filter((sub) => sub.isCompleted).length;
  const total = props.task.subtasks.length;
  return `${completed} of ${total}`
});
</script>
