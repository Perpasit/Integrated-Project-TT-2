<script setup>
import { ref, onMounted , computed ,watch} from 'vue';
import { useCollabStore } from "../stores/collaboratorStore";
import { useUserStore } from "../stores/userStore";
import { useBoardStore } from "../stores/boardStore";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const email = ref("");
const accessRight = ref("READ");
let boardId = ref(route.params.boardId);
const collabStore = useCollabStore();
const userStore = useUserStore();
const boardStore = useBoardStore();
const boardOid = ref(null); 
const userOid = ref();
const hasWriteAccess = computed(() => {
  const collaboratorData = collabStore.getCollaboratorByBoardAndUser(userOid.value);
  return collaboratorData?.accessRight === "WRITE";
});
const isSameOid = computed(() => {
  if (!boardOid.value || !userOid.value) {
    return false // Return false ถ้าไม่มีค่า boardOid หรือ userOid เพื่อให้หน้าโหลดขึ้นมาได้
  }
  return boardOid.value === userOid.value
})
const isSameOidOrHasWriteAccess = computed(() => {
  if (!boardOid.value || !userOid.value) {
    return false;
  }

  return boardOid.value === userOid.value || hasWriteAccess.value;
});

const fetchBoardAndUserOids = async (boardId) => {
  try {
    const boardData = await boardStore.getBoardById(boardId);
    // console.log(boardData)
    if (!boardData) {
      throw new Error(`Board with id ${boardId} not found.`);
    }
    console.log(userStore.getUserOid)
    boardOid.value = boardData.owner.oid; 
    userOid.value = userStore.getUserOid || null; 
    // const visibility = boardData.visibility;
    // console.log("Board Visibility:", visibility);
    // console.log(isSameOidOrHasWriteAccess.value)

    if (!isSameOidOrHasWriteAccess.value || !isSameOid.value) {
      router.push({ name: "AccessDenied" });
    }


  } catch (error) {
    console.error("Error fetching board or user OID:", error);
    router.push({ name: "AccessDenied" }); 
  }
};

const isAddDisabled = computed(() => {
  const userEmail = userStore.getUserEmail; // Access directly without ref
  // Disable if email is the same as the user's or if email does not contain '@'
  return email.value === userEmail || !email.value.includes('@');
});

watch(email, () => {
  if (isAddDisabled.value && email.value.includes('@')) {
    errorText.value = "Board owner cannot be collaborator of his/her own board";
  } else {
    errorText.value = ""; // Clear the error when the condition is no longer met
  }
});

const collaborators = ref([]); // ประกาศ ref สำหรับเก็บข้อมูล collaborators

// ฟังก์ชันดึงข้อมูล collaborators
const fetchCollaborator = async () => {
  try {


    const data = await collabStore.fetchCollaborators(boardId.value);

    const invitationData = data.invitationList.map(d=>({name:d.inviteeName,email:d.inviteeEmail,accessRight:d.accessRight,status:d.status}))
    const collabAll =  [...data.collaboratorList ,...invitationData]

    console.log(collabAll)
    if (Array.isArray(collabAll)) {
      // ล้างค่า collaborators และบังคับให้ Vue ตรวจจับการเปลี่ยนแปลง
      collaborators.value = [];  // ล้างค่าเดิม
      collaborators.value = collabAll.map((collab) => ({
        ...collab,
        accessRight: collab.accessRight,
      }));

      // ตรวจสอบผลลัพธ์ใน console
      // console.log('Fetched collaborators:', collaborators.value);
    } else {
      console.error("Unexpected data format:", collabAll);
      collaborators.value = [];
    }
  } catch (error) {
    if (error.response && error.response.status === 404) {
      console.warn("No collaborators found for this board.");
      collaborators.value = [];
    } else {
      console.error("Failed to fetch collaborators:", error);
    }
  }
};


const errorText = ref("")

const handleSubmit = async () => {
  try {
    // เพิ่ม collaborator ใหม่
    const data = await collabStore.addCollaborator(boardId.value, email.value, accessRight.value);
    // ตรวจสอบว่าผู้ใช้นี้เป็น collaborator อยู่แล้วหรือไม่
    if (data.status === 500 || data === 'The user is already a collaborator or has a pending invitation for this board.') {
      errorText.value = 'The user is already a collaborator or has a pending invitation for this board.';
      return;
    }
    // ตรวจสอบว่าผู้ใช้นี้ไม่พบอีเมลหรือไม่
    if (typeof data === 'string' && data.includes("User not found with email")) {
      errorText.value = "The user does not exists";
      return;
    }

   await fetchCollaborator();
    // ถ้าไม่มีปัญหาใด ๆ ให้รีเซ็ตฟิลด์ข้อมูล
    email.value = "";
    errorText.value = "";
    accessRight.value = "READ";
    navigateBack(); // กลับไปหน้าก่อนหน้า

  } catch (error) {
    // จัดการข้อผิดพลาดที่เกิดขึ้น
    console.error("Error adding collaborator:", error);
    console.log("cant add");
  }
};


// ฟังก์ชันนำทางกลับไปที่หน้า Collaborator Management
const navigateBack = () => {
  router.push({ name: "CollabManage", params: { boardId: boardId.value } });
  fetchCollaborator()
};

// เรียก fetchCollaborator เมื่อ component ถูก mount
onMounted(async () => {
  let boardId = route.params.boardId;
  await fetchCollaborator();
  // console.log(collaborators.value);
  await fetchBoardAndUserOids(boardId)
});
</script>



<template>
  <div
    class="fixed inset-0 bg-gray-500 bg-opacity-75 flex justify-center items-center"
  >
    <div
      class="bg-white w-full max-w-md mx-auto rounded-lg p-6 shadow-lg relative"
    >
      <h3 class="text-xl font-semibold mb-4 text-gray-900">
        Add New Collaborator
      </h3>

      <form @submit.prevent="handleSubmit">
        <div class="mb-4">
          <label for="email" class="block text-sm font-medium text-gray-700"
            >Collaborator Email</label
          >
          
          <input
            v-model="email"
            type="email"
            id="email"
            required
            class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          />
          <p v-if="errorText" class="text-xs font-medium text-red-500">{{ errorText }}</p>
        </div>

        <div class="mb-4">
          <label
            for="accessRight"
            class="block text-sm font-medium text-gray-700"
            >Access Right</label
          >
          <select
            v-model="accessRight"
            id="accessRight"
            class="mt-1 block w-full p-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          >
            <option value="READ">Read</option>
            <option value="WRITE">Write</option>
          </select>
        </div>

        <div class="flex justify-end">
          <button
            type="button"
            @click="navigateBack"
            class="px-4 py-2 mr-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400"
          >
            Cancel
          </button>
          <button
            :disabled="isAddDisabled"
            type="submit"
            class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
          >
            Add Collaborator
          </button>
        </div>
      </form>
    </div>

    
  </div>
</template>
