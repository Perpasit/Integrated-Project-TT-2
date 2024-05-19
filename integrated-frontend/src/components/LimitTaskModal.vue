<!-- LimitTaskModal.vue -->
<script setup>
import { ref, computed, onMounted, watch } from "vue"
import { useGlobalSettingStore } from "../stores/global_setting"

function saveIsLimitEnabledToLocalStorage(value) {
  localStorage.setItem("isLimitEnabled", value)
}

function getIsLimitEnabledFromLocalStorage() {
  return localStorage.getItem("isLimitEnabled") === "true"
}


const props = defineProps({
  show: {
    type: Boolean,
    required: true,
  },
  currentTaskCounts: {
    type: Object,
    required: true,
  },
  currentStatusNames: {
    type: Array,
    required: true,
  },
});

const emit = defineEmits(["close", "save", "toggle"])

const globalSettingStore = useGlobalSettingStore()

const generalSetting = computed(
  () => globalSettingStore.getCurrentGlobalSetting
)

const maxTasks = ref(generalSetting.value?.maximumTaskLimit)
const isLimitEnabled = ref(generalSetting.value?.limitMaximumTask)

const isMaxTasksValid = computed(() => {
  return (
    isLimitEnabled.value !==
      globalSettingStore.getCurrentGlobalSetting.limitMaximumTask ||
    maxTasks.value !==
      globalSettingStore.getCurrentGlobalSetting.maximumTaskLimit
  )
})

watch(
  () => [maxTasks.value, isLimitEnabled.value],
  () => {
    emit("save", {
      maxTasks: maxTasks.value,
      isLimitEnabled: isLimitEnabled.value,
    })
  }
)

onMounted(async () => {
  try {
    await globalSettingStore.fetchGlobalSetting("General")
    const currentGlobalSetting = globalSettingStore.getCurrentGlobalSetting
    const limitMaximumTask = currentGlobalSetting?.limitMaximumTask
    const maximumTaskLimit = currentGlobalSetting?.maximumTaskLimit

    isLimitEnabled.value =
      limitMaximumTask || getIsLimitEnabledFromLocalStorage()
    maxTasks.value = maximumTaskLimit || 10
    globalSettingStore.setIsLimitEnabled(isLimitEnabled.value)
    saveIsLimitEnabledToLocalStorage(isLimitEnabled.value)

    watch(
      () => currentGlobalSetting?.limitMaximumTask,
      (newValue) => {
        isLimitEnabled.value = newValue
        globalSettingStore.setIsLimitEnabled(newValue)
        saveIsLimitEnabledToLocalStorage(newValue)
      }
    )
  } catch (error) {
    console.error("Error fetching global setting:", error)
  }
})

function closeModal() {
  emit("close")
}

async function saveMaxTasks() {
  if (isMaxTasksValid.value) {
    const updatedSetting = {
      maximumTaskLimit: maxTasks.value,
      limitMaximumTask: isLimitEnabled.value,
    }
    await globalSettingStore.updateGlobalSetting("General", updatedSetting)
    saveIsLimitEnabledToLocalStorage(isLimitEnabled.value) // Save to Local Storage
    emit("save", {
      maxTasks: maxTasks.value,
      isLimitEnabled: isLimitEnabled.value,
    })
    closeModal()
  }
}

function toggleLimitEnabled() {
  isLimitEnabled.value = !isLimitEnabled.value
  globalSettingStore.setIsLimitEnabled(isLimitEnabled.value)
  saveIsLimitEnabledToLocalStorage(isLimitEnabled.value)
  emit("toggle", isLimitEnabled.value)
}
const limitReachedStatusesWithCounts = computed(() => {
  return props.currentStatusNames
    .filter((statusName) => {
      const taskCount = props.currentTaskCounts[statusName];
      return (
        taskCount >= maxTasks.value &&
        statusName !== 'No Status' &&
        statusName !== 'Done'
      );
    })
    .map((statusName) => `"${statusName}"(${props.currentTaskCounts[statusName]}Task)`);
});
const isTaskLimitReached = computed(() => {
  if (!isLimitEnabled.value) return false;

  return limitReachedStatusesWithCounts.value.length > 0;
});
const limitReachedStatuses = computed(() => {
  return props.currentStatusNames.filter((statusName, index) => {
    const taskCount = props.currentTaskCounts[statusName];
    return taskCount >= maxTasks.value && statusName !== 'No Status' && statusName !== 'Done';
  });
});


</script>

<template>
  <div v-if="show" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen px-4">
      <div class="fixed inset-0 transition-opacity" aria-hidden="true">
        <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
      </div>
      <div
        class="bg-white rounded-lg overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full mx-auto"
      >
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="sm:flex sm:items-start">
            <div class="mt-3 text-center sm:mt-0 sm:text-left w-full">
              <h3
                class="itbkk-modal-setting text-lg leading-6 font-medium text-gray-900"
              >
                Status Settings
              </h3>
              <div class="mt-4">
                <label class="flex items-center mb-2">
                  <label
                    class="tbkk-limit-task inline-flex items-center cursor-pointer"
                  >
                    <input
                      type="checkbox"
                      class="sr-only peer"
                      :checked="isLimitEnabled"
                      @change="toggleLimitEnabled"
                    />
                    <div
                      class="relative w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-200 peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"
                    ></div>
                  </label>

                  <span class="ml-2 text-gray-700"
                    >Limit tasks in this status</span
                  >
                </label>
                <div v-if="isLimitEnabled">
                  <input
                  type="number"
                v-model.number="maxTasks"
                class="itbkk-max-task shadow-sm p-2 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"
                min="1"
                max="100"
                  />
                  <p class="text-gray-500 text-sm mt-1">
                    Users can limit the task in a status by setting maximum
                    tasks in each status (except "No Status" and "Done"
                    statuses).
                  </p>
                  <!-- <p class="itbkk-limit-task text-lg">{{ maxTasks }}</p> -->
                </div>
                <div v-if="isTaskLimitReached" class="mt-2 bg-yellow-100 border-l-4 border-yellow-400 p-4">
  <p class="text-sm text-yellow-700">
    The status <br>
    {{ limitReachedStatusesWithCounts.join(', ') }} <br>
    These Status has reached the task limit ({{ maxTasks }}). No additional tasks can be added to this status.
  </p>
</div>
              </div>
            </div>
          </div>
        </div>
        <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button
            type="button"
            class="itbkk-button-confirm w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
            :class="{
              'bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500':
                isMaxTasksValid,
              'bg-gray-300 cursor-not-allowed': !isMaxTasksValid,
            }"
            @click="saveMaxTasks"
            :disabled="!isMaxTasksValid"
          >
            Save
          </button>
          <button
            type="button"
            class="itbkk-button-cancel mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
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
