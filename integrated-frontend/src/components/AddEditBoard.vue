<script setup>
import { ref, watch, computed } from 'vue';
import { useBoardStore } from "../stores/boardStore";
import { useUserStore } from '../stores/userStore';

const props = defineProps({
    showModal: Boolean,
    editingBoard: Object
});

const emit = defineEmits(['update:showModal', 'boardSaved']);

const boardStore = useBoardStore();
const userStore = useUserStore();

const accountName = computed(() => userStore.getAccountName);
const defaultBoardName = computed(() => `${accountName.value} personal board`);

const boardName = ref(defaultBoardName.value);
const isEditing = ref(false);

watch(() => props.editingBoard, (newVal) => {
    if (newVal) {
        boardName.value = newVal.name;
        isEditing.value = true;
    } else {
        boardName.value = defaultBoardName.value;
        isEditing.value = false;
    }
}, { immediate: true });

const saveBoard = async () => {
    if (boardName.value.trim()) {
        try {
            const trimmedName = boardName.value.trim().replace(/ personal board$/, '');
            if (isEditing.value) {
                await boardStore.updateBoard(props.editingBoard.id, { ...props.editingBoard, name: trimmedName });
            } else {
                await boardStore.addBoard(trimmedName);
            }
            emit('boardSaved');
            closeModal();
        } catch (error) {
            console.error("Error saving board:", error);
        }
    }
};


const closeModal = () => {
    emit('update:showModal', false);
    boardName.value = defaultBoardName.value;
    isEditing.value = false;
};
</script>

<template>
    <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
        <div class="flex items-center justify-center min-h-screen px-4">
            <div class="fixed inset-0 transition-opacity" aria-hidden="true">
                <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
            </div>
            <div class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-2xl sm:w-full">
                <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                    <div class="sm:flex sm:items-start">
                        <div class="mt-3 text-center sm:mt-0 sm:text-left w-full">
                            <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">{{ isEditing ? 'Edit' : 'New' }} Board</h3>
                            <div class="mb-4">
                                <label for="boardName" class="block text-sm font-bold mb-2">Name</label>
                                <input type="text" id="boardName" v-model="boardName"
                                    class="itbkk-board-name shadow-sm p-2 mt-1 block w-full text-base border border-gray-300 rounded-md"
                                    placeholder="Enter board name">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                    <button @click="saveBoard" :class="{
                        'bg-gray-300 text-gray-500 cursor-not-allowed': !boardName.trim(),
                        'bg-indigo-600 text-white hover:bg-indigo-700': boardName.trim()
                    }" :disabled="!boardName.trim()"
                        class="itbkk-button-ok w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm">
                        Save
                    </button>
                    <button @click="closeModal"
                        class="itbkk-button-cancel mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>