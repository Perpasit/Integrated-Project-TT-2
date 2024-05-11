<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useStatusStore } from "../stores/status";
import { useModalStore } from "../stores/useModalStore";
import { useRouter } from "vue-router";

const router = useRouter();
const props = defineProps({
  show: { type: Boolean, default: false },
  status: { type: Object, default: null },
  mode: { type: String, default: "add" },
});

const emit = defineEmits(["close", "statusSaved"]);
const modalStore = useModalStore();
const statusStore = useStatusStore();
const editedStatus = ref({ id: null, name: "", description: "", colorCode: "#000000" });
const originalStatus = ref(null);
const nameError = ref("");

const isNameValid = computed(() => {
  return (
    editedStatus.value.name.trim() !== "" &&
    (props.mode === "add" ||
      (props.mode === "edit" &&
        editedStatus.value &&
        originalStatus.value &&
        (editedStatus.value.name !== originalStatus.value.name ||
          editedStatus.value.description !== originalStatus.value.description ||
          editedStatus.value.colorCode !== originalStatus.value.colorCode)))
  );
});

watch(
  () => editedStatus.value.name,
  (newName) => {
    if (newName.trim() === "") {
      nameError.value = "Name is required.";
    } else {
      nameError.value = "";
    }
  }
);

onMounted(async () => {
  await statusStore.fetchAllStatuses()
  if (props.mode === "edit") {
    const id = router.currentRoute.value.params.id;
    try {
      const data = await statusStore.getStatusById(id);
      editedStatus.value = { ...data };
      originalStatus.value = { ...data };
      nameError.value = "";
    } catch (error) {
      console.error("Error fetching status:", error);
      editedStatus.value = null;
    }
  } else {
    resetForm();
    nameError.value = "Name is required.";
  }
});

function resetForm() {
  editedStatus.value = { name: "", description: "", colorCode: "#000000" };
}

async function saveStatus() {
 
  if (!isNameValid.value) {
    nameError.value = "Name is required.";
    return;
  }

  try {
    const updatedStatus = {
      ...editedStatus.value,
      name: editedStatus.value.name.trim(),
      description: editedStatus.value.description ? editedStatus.value.description.trim() : "",
      updatedOn: new Date().toISOString(),
    };

    if (props.mode === "edit") {
      await statusStore.updateStatus(updatedStatus.id, updatedStatus);
      emit("statusSaved");
    } else {
      await statusStore.createStatus(updatedStatus);
    }
    modalStore.openSuccessModal(updatedStatus.name, props.mode);
    closeModal();
  } catch (error) {
    console.error("Error saving status:", error);
  }
  await statusStore.fetchAllStatuses()
  router.push("/status/manage");
}

function closeModal() {
  router.push("/status/manage");
  resetForm();
}
</script>


<template>
  <div v-if="show" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>

      <div
        class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-4xl sm:w-full mx-auto"
      >
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-6">
          <div
            class="text-lg leading-6 font-medium text-gray-900 mb-4"
          >
            {{ mode === "add" ? "New Status" : "Edit Status" }}
          </div>

          <div class="sm:flex sm:items-start">
            <div class="mt-3 text-center sm:mt-5 sm:text-left w-full mr-6 ml-6">
              <div>
                <label class="block text-sm font-bold mb-1">Name:</label>
                <textarea
                  rows="2"
                  v-model="editedStatus.name"
                  class="shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                ></textarea>
                <p v-if="nameError" class="text-red-500 text-sm mt-1">
                  {{ nameError }}
                </p>
              </div>

              <div class="mt-4">
                <label class="block text-sm font-bold mb-1">Description:</label>
                <textarea
                  rows="5"
                  v-model="editedStatus.description"
                  class="shadow-sm p-1 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                  placeholder="No Description Provided"
                ></textarea>
              </div>

              <div class="mt-4">
                <label class="block text-sm font-bold mb-1">Color Code:</label>
                <input
                  type="color"
                  v-model="editedStatus.colorCode"
                  class="shadow-sm p-1 mt-1 block border border-gray-300 rounded-md"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button
            type="button"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
            :class="{
              'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500':
                isNameValid,
              'bg-gray-300 cursor-not-allowed': !isNameValid
            }"
            @click="saveStatus"
            :disabled="!isNameValid"
          >
            {{ mode === "add" ? "Add" : "Save" }}
          </button>
          <button
            type="button"
            class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            @click="closeModal"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>