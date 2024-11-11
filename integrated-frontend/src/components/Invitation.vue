<script setup>
import Header from "./Header.vue";
import { useRouter, useRoute } from "vue-router";
import { onMounted, ref } from "vue";
import { useCollabStore } from "../stores/collaboratorStore";

const router = useRouter();
const route = useRoute();
const collabStore = useCollabStore();
const boardId = route.params.boardId;

let inviteData ;

const fetchInvitation = async (boardId) => {
  const data = await collabStore.fetchInvitations(boardId);
  inviteData = data;
};

async function acceptInvitation(boardId, inviteData) {
  // Await the function to confirm the invitation
//   console.log(inviteData)
  await collabStore.acceptInvitation(boardId, inviteData.invitationId, true );
  router.push({ name: "home" , params:{boardId : boardId}});

}

async function declineInvitation(boardId, inviteData) {
  await collabStore.acceptInvitation(boardId, inviteData.invitationId, false );

  router.push({ name: "Board" });
}

onMounted(async () => {
  await fetchInvitation(boardId);
});
</script>

<template>
  <div class="fixed inset-0 flex items-center justify-center bg-white">
    <div class="text-center">
      <h1 class="text-2xl font-semibold mb-6">Invitation to SomeOne Board</h1>
      <div class="flex space-x-4 justify-center">
        <button
          @click="() => declineInvitation(boardId, inviteData)"
          class="px-6 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition duration-300"
        >
          Decline
        </button>
        <button
          @click="() => acceptInvitation(boardId, inviteData)"
          class="px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition duration-300"
        >
          Accept
        </button>
      </div>
    </div>
  </div>
</template>
