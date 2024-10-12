<script setup>
import { ref, onMounted } from 'vue';
import { useCollabStore } from "../stores/collaboratorStore";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const email = ref("");
const accessRight = ref("READ");
const boardId = ref(route.params.boardId);
const collabStore = useCollabStore();

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
      console.log('Fetched collaborators:', collaborators.value);
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


const handleSubmit = async () => {
  try {
    // เพิ่ม collaborator ใหม่
    await collabStore.addCollaborator(boardId.value, email.value, accessRight.value);
    
    // ดึงข้อมูล collaborators ใหม่
    await fetchCollaborator();
    
    // ตรวจสอบว่าข้อมูล collaborators ถูกอัปเดตหรือไม่
    console.log('Collaborators after adding:', collaborators.value);
    
    // ล้างค่า email และ accessRight หลังจากเพิ่มเสร็จ
    email.value = "";
    accessRight.value = "READ";

    // นำทางกลับไปที่หน้า Collaborator Management (หรือไม่ขึ้นอยู่กับความต้องการ)
    navigateBack();
  
  } catch (error) {
    console.error("Error adding collaborator:", error);
  }

};


// ฟังก์ชันนำทางกลับไปที่หน้า Collaborator Management
const navigateBack = () => {
  router.push({ name: "CollabManage", params: { boardId: boardId.value } });
};

// เรียก fetchCollaborator เมื่อ component ถูก mount
onMounted(async () => {
  await fetchCollaborator();
  console.log(collaborators.value);

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
            type="submit"
            class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Add Collaborator
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
