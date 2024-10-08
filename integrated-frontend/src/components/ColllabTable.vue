<script setup>
import { ref, onMounted } from 'vue';
import { useCollabStore } from '../stores/collaboratorStore';
import { useRoute,useRouter } from "vue-router"

const route = useRoute();
const router = useRouter();
const collabStore = useCollabStore();
const collaborators = ref([]); // Initialize as an empty array

const boardId = route.params.boardId; // Get boardId from the route params

console.log(boardId); // Debugging, make sure you see this in the console

const fetchCollaborator = async () => {
  try {
    const collabAll = await collabStore.fetchCollaborators(boardId);
    collaborators.value = collabAll.map(collab => ({
      ...collab,
      // Use the backend format directly
      accessRight: collab.accessRight // Access rights should be either "READ" or "WRITE"
    })) || [];
  } catch (error) {
    console.error("Failed to fetch collaborators:", error);
    collaborators.value = [];
  }
};

// Function to handle changes in accessRight and update the collaborator
const updateAccessRight = async (collaborator) => {
  const updatedAccessRight = collaborator.accessRight === "WRITE" ? "WRITE" : "READ"; 
  try {
    await collabStore.updateCollaborator(boardId, collaborator.oid, updatedAccessRight); 
    console.log(`Access right updated to ${updatedAccessRight} for ${collaborator.name}`);
  } catch (error) {
    console.error("Failed to update access right:", error);
  }
};
const openAddCollaboratorModal = () => {
  console.log("open Modal")
  router.push({ name: "AddCollabManage", params: { boardId } }); 
};

onMounted(async () => {
  await fetchCollaborator(); 
});
</script>

<template>
<div class="container mx-auto px-4 pt-2">
  <div class="overflow-visible">
    <h2 class="text-center text-5xl font-bold text-white mb-4 py-1">Collaborator Table</h2>

    <div class="flex justify-end mb-6 pb-4 border-b border-white">
      <button @click="openAddCollaboratorModal"
        class="itbkk-button-create relative inline-flex items-center justify-center p-0.5 overflow-hidden text-sm font-medium text-white rounded-lg group bg-gradient-to-br from-[#eddbe8] to-[#e9b9de] group-hover:from-teal-300 transition duration-300 ease-in-out transform hover:scale-105">
        <span
          class="relative px-5 py-2.5 transition-all ease-in duration-75 dark:bg-gray-900 rounded-md group-hover:bg-opacity-0">
          Add Collaborator
        </span>
      </button>
    </div>

    <table class="table-fixed w-full max-w-6xl mx-auto border-separate border-spacing-y-2">
      <thead>
        <tr class="text-white w-full">
          <th class="w-1/12 py-2 rounded-l-lg text-xl font-medium">No</th>
          <th class="w-1/3 px-4 py-2 text-xl font-medium">Name</th>
          <th class="w-1/3 px-4 py-2 text-xl font-medium">E-mail</th>
          <th class="w-1/6 px-4 py-2 text-xl font-medium">Access Right</th>
          <th class="w-1/6 px-4 py-2 rounded-r-lg text-xl font-medium">Action</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(collaborator, index) in collaborators" :key="collaborator.oid"
          class="itbkk-item bg-white shadow-lg hover:bg-gray-100 hover:scale-105 transition-all duration-200 ease-in-out cursor-pointer">
          <td class="px-4 py-5 text-center rounded-l-lg">{{ index + 1 }}</td>
          <td class="px-4 py-2">
            <p class="itbkk-title truncate">{{ collaborator.name }}</p>
          </td>
          <td class="px-4 py-2">
            <p class="itbkk-email truncate">{{ collaborator.email }}</p>
          </td>
          <td class="px-4 py-2">
            <select v-model="collaborator.accessRight" @change="updateAccessRight(collaborator)"
              class="itbkk-select bg-white border w-full border-gray-300 rounded-md p-2 text-gray-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 ease-in-out">
              <option value="READ">Read</option> <!-- Match the backend value -->
              <option value="WRITE">Write</option> <!-- Match the backend value -->
            </select>
          </td>
          <td class="itbkk-button-action px-4 py-5 rounded-r-lg flex justify-evenly">
            <button class="itbkk-button-delete hover:scale-110 transition-all duration-200 ease-in-out ml-2"
              @click.stop="confirmDelete(collaborator)">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="red">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  

  <div v-if="collaborators.length <= 0" class="text-center mt-4 text-gray-500">
    No Collaborators
  </div>

  <router-view />
</div>
</template>

<style scoped>
</style>
