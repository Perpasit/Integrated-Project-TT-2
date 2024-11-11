<script setup>
import Header from "./Header.vue";
import { useRouter, useRoute } from "vue-router";
import { onMounted, ref } from "vue";
import { useCollabStore } from "../stores/collaboratorStore";
import { useUserStore } from "../stores/userStore";

const router = useRouter();
const route = useRoute();
const collabStore = useCollabStore();
const userStore = useUserStore();
const boardId = route.params.boardId;
const inviteName = ref(null);
const inviteData = ref(null);
const currUser = ref(null);
const activeInvitation = ref(false);

const fetchInvitation = async (boardId) => {
  const data = await collabStore.fetchInvitations(boardId);
  inviteData.value = data;
  const user = userStore.getUserInfo;

  inviteName.value = data.inviteeName;
  currUser.value = user.name;
  activeInvitation.value = currUser.value === inviteName.value;

  console.log(currUser.value);
  console.log(inviteName.value);
  console.log(activeInvitation.value);
  console.log(data);
};

async function acceptInvitation(boardId, inviteData) {
  await collabStore.acceptInvitation(boardId, inviteData.invitationId, true);
  router.push({ name: "home", params: { boardId: boardId } });
}

async function declineInvitation(boardId, inviteData) {
  await collabStore.acceptInvitation(boardId, inviteData.invitationId, false);
  router.push({ name: "Board" });
}

function goBack() {
  router.push({ name: "Board" });
}
onMounted(async () => {
  await fetchInvitation(boardId);
});
</script>

<template>
  <div class="fixed inset-0 flex items-center justify-center bg-gray-100 p-4">
    <div class="bg-white shadow-lg rounded-lg pt-5 pb-8 px-8 text-center max-w-md w-full">
      <p 
        class="flex justify-end text-xl"
        @click="goBack"
      >
        &times;
      </p>
      <h1 class="text-xl font-semibold text-gray-800 mb-4">INVITATION</h1>

      <p v-if="activeInvitation" class="text-gray-600 mb-6">
        <strong>{{ inviteData.inviterName }}</strong> has invited
        <strong>{{ inviteData.inviteeName }}</strong> to join the board
        <strong>{{ inviteData.boardName }}</strong> with
        <span class="text-blue-500 font-medium">{{
          inviteData.accessRight
        }}</span>
        access rights.
      </p>

      <div v-if="activeInvitation" class="flex justify-between space-x-4">
        <button
          @click="() => declineInvitation(boardId, inviteData)"
          class="w-full px-6 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition duration-300"
        >
          Decline
        </button>
        <button
          @click="() => acceptInvitation(boardId, inviteData)"
          class="w-full px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition duration-300"
        >
          Accept
        </button>
      </div>

      <div v-else class="text-red-500 mt-6">
        <div class="text-lg font-semibold mb-4">
          Sorry, we couldn't find your active invitation to this board.
        </div>
      </div>
    </div>
  </div>
</template>
