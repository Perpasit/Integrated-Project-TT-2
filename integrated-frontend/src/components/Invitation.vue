<script setup>
import Header from "./Header.vue";
import { useRouter, useRoute } from "vue-router";
import { onMounted } from "vue";
import { useCollabStore } from "../stores/collaboratorStore";

const router = useRouter();
const route = useRoute();
const collabStore = useCollabStore();

function declineInvitation(){
    router.push({name:"Board"})
}

const fetchInvitation = async (boardId) => {
  await collabStore.fetchInvitationsCheck(boardId)
}
onMounted(async()=>{
    await fetchInvitation
})
</script>

<template>
  <div class="fixed inset-0 flex items-center justify-center bg-white">
    <div class="text-center">
      <h1 class="text-2xl font-semibold mb-6">Invitation to SomeOne Board</h1>
      <div class="flex space-x-4 justify-center">
        <button
          @click="declineInvitation"
          class="px-6 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition duration-300"
        >
          Decline
        </button>
        <button
          @click="acceptInvitation"
          class="px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition duration-300"
        >
          Accept
        </button>
      </div>
    </div>
  </div>
</template>
