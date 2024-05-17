<!-- LimitTaskModal.vue -->
<script setup>
import { ref, computed } from "vue";

const props = defineProps({
    show: {
        type: Boolean,
        required: true,
    },
    maxTaskLimit: {
        type: Number,
        default: 10,
    },
    isLimitEnabled: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(["close", "save", "toggle"]);

const maxTasks = ref(props.maxTaskLimit);
const isLimitEnabled = ref(props.isLimitEnabled);

const isMaxTasksValid = computed(() => {
    return maxTasks.value >= 1 && maxTasks.value <= 100;
});

function closeModal() {
    emit("close");
}

function saveMaxTasks() {
    if (isMaxTasksValid.value) {
        emit("save", { maxTasks: maxTasks.value, isLimitEnabled: isLimitEnabled.value });
        closeModal();
    }
}

function toggleLimitEnabled() {
    isLimitEnabled.value = !isLimitEnabled.value;
    emit("toggle", isLimitEnabled.value);
}
</script>

<template>
    <div v-if="show" class="fixed z-10 inset-0 overflow-y-auto">
        <div class="flex items-center justify-center min-h-screen px-4">
            <div class="fixed inset-0 transition-opacity" aria-hidden="true">
                <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
            </div>
            <div
                class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full mx-auto">
                <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                    <div class="sm:flex sm:items-start">
                        <div class="mt-3 text-center sm:mt-0 sm:text-left w-full">
                            <h3 class="itbkk-modal-setting text-lg leading-6 font-medium text-gray-900">
                                Status Settings
                            </h3>
                            <div class="mt-4">
                                <label class="flex items-center mb-2">
                                    <label class="tbkk-limit-task inline-flex items-center cursor-pointer" :checked="isLimitEnabled"
                                        @change="toggleLimitEnabled">
                                        <input type="checkbox" value="" class="sr-only peer">
                                        <div
                                            class="relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600">
                                        </div>
                                    </label>


                                    <span class="ml-2 text-gray-700">Limit tasks in this status</span>
                                </label>
                                <div v-if="isLimitEnabled">
                                    <input type="number" v-model.number="maxTasks"
                                        class="itbkk-max-task shadow-sm p-2 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                                        min="1" max="100" />
                                    <p class="text-gray-500 text-sm mt-1">Maximum tasks</p>
                                    <p class="itbkk-limit-task text-lg">{{ maxTasks }}</p>
                                </div>
                                <p v-else class="text-sm text-gray-500">
                                    Task limit is disabled for this status.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                    <button type="button"
                        class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-emerald-600 text-base font-medium text-white hover:bg-emerald-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-emerald-500 sm:ml-3 sm:w-auto sm:text-sm"
                        :disabled="!isMaxTasksValid" @click="saveMaxTasks">
                        Save
                    </button>
                    <button type="button"
                        class="itbkk-button-cancel mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
                        @click="closeModal">
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped></style>