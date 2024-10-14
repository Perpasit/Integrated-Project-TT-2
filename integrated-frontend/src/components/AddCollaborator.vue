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
const boardId = ref(route.params.boardId);
const collabStore = useCollabStore();
const userStore = useUserStore();
const boardStore = useBoardStore();
const boardOid = ref(null); 
const userOid = ref();
const hasWriteAccess = computed(() => {
  const collaboratorData = collabStore.getCollaboratorByBoardAndUser(userOid.value);
  return collaboratorData?.accessRight === "WRITE";
});

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

    if (!isSameOidOrHasWriteAccess.value) {
      router.push({ name: "AccessDenied" });
    }

  } catch (error) {
    console.error("Error fetching board or user OID:", error);
    router.push({ name: "AccessDenied" }); 
  }
};

const isAddDisabled = computed(() => {
  const userEmail = userStore.getUserEmail; // Access directly without ref
  return email.value === userEmail;
});

watch(email, () => {
  if (isAddDisabled.value) {
    errorText.value = "Board owner cannot be collaborator of his/her own board";
  } else {
    errorText.value = ""; // Clear the error when the condition is no longer met
  }
});

const collaborators = ref([]); // ประกาศ ref สำหรับเก็บข้อมูล collaborators

// ฟังก์ชันดึงข้อมูล collaborators
const fetchCollaborator = async () => {
  try {
    const collabAll = await collabStore.fetchCollaborators(boardId.value);

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
    if (data === "The user is already the collaborator of this board.") { 
      // === "The user is already the collaborator of this board"
      errorText.value = data
      // await fetchCollaborator();
      return;
    }
    if (data.status === 404){
      errorText.value = data.message
      // await fetchCollaborator();
      return;
    }
      email.value = "";
      errorText.value = ""
      accessRight.value = "READ";
      navigateBack();
    // await fetchCollaborator();
    // ตรวจสอบว่าข้อมูล collaborators ถูกอัปเดตหรือไม่
    // console.log('Collaborators after adding:', collaborators.value);
    
    // ล้างค่า email และ accessRight หลังจากเพิ่มเสร็จ
    

    // นำทางกลับไปที่หน้า Collaborator Management (หรือไม่ขึ้นอยู่กับความต้องการ)
    // 
  
  } catch (error) {
    console.error("Error adding collaborator:", error);
    console.log("cant add")
  }

};


// ฟังก์ชันนำทางกลับไปที่หน้า Collaborator Management
const navigateBack = () => {
  router.push({ name: "CollabManage", params: { boardId: boardId.value } });
};

// เรียก fetchCollaborator เมื่อ component ถูก mount
onMounted(async () => {
  const boardId = route.params.boardId;
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
