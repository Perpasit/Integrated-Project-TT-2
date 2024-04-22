<script setup>
import HelloWorld from "./components/HelloWorld.vue"
import TaskManager from "./components/TaskManager.vue"

const tasksData = [
  {
    id: 1,
    title: "Task 1",
    description: "Description for Task 1",
    assignees: [
      { id: 1, name: "John Doe" },
      { id: 2, name: "Jane Smith" }
    ],
    status: "In Progress",
    createdAt: "2024-04-22 10:00:00",
    updatedAt: "2024-04-22 12:30:00",
    completed: false
  },
  {
    id: 2,
    title: "Task 2",
    description: "Description for Task 2",
    assignees: [{ id: 3, name: "Alice Johnson" }],
    status: "Not Started",
    createdAt: "2024-04-21 09:00:00",
    updatedAt: "2024-04-21 09:30:00",
    completed: true
  }
]
</script>

<template>
  <div>
    <!-- <HelloWorld msg="Vite + Vue" /> -->
    <TaskManager :tasks="tasksData" />
  </div>
</template>

<style scoped></style>
