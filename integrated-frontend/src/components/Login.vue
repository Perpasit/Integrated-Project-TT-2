<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { postItemLogin } from "../utils/fetch";
import { useUserStore } from "../stores/userStore";
import * as jwtDecode from "jwt-decode";

const router = useRouter();
const userStore = useUserStore();

// Form state
const username = ref("");
const password = ref("");
const errorMessage = ref("");

// Computed property to validate the form
const isFormValid = computed(() => {
  return (
    username.value.trim() !== "" &&
    password.value.trim() !== "" &&
    username.value.length <= 50 &&
    password.value.length <= 14
  );
});

const clearErrorMessage = () => {
  errorMessage.value = "";
};

// Handle form submission
const handleSubmit = async () => {
  if (isFormValid.value) {
    try {
      const response = await postItemLogin(
        `${import.meta.env.VITE_API_ENDPOINT2}/login`,
        {
          userName: username.value,
          password: password.value,
        }
      );

      if (response) {
        errorMessage.value = ""
        const decodedToken = jwtDecode.jwtDecode(response.access_token)
        userStore.setUserInfo(
          decodedToken,
          response.access_token,
          response.refresh_token
        )

        router.push({ name: "Board" })
      }
    } catch (error) {
      errorMessage.value = "Username or Password is incorrect";
    }
  } else {
    errorMessage.value = "Please fill in the required fields correctly.";
  }
};

onMounted(() => {
  userStore.loadFromLocalStorage();
  if (userStore.isAuthenticated) {
    router.push({ name: "Board" });
  }
});
</script>

<template>
  <div class="flex items-center justify-center bg-gradient-to-r from-[#F1D3CE] to-[#e6a195] min-h-screen">
    <div class="w-full max-w-md">
      <form @submit.prevent="handleSubmit"
        class="bg-white bg-opacity-30 backdrop-filter backdrop-blur-lg shadow-2xl rounded-lg px-8 pt-6 pb-8 mb-4">
        <h2 class="text-3xl font-bold mb-6 text-center text-[#ed8ba7]">
          Welcome to ITB-KK
        </h2>


        <!-- Error Message -->
        <div v-if="errorMessage" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4" role="alert">
          <p class="itbkk-message">{{ errorMessage }}</p>
        </div>

        <!-- Username Field -->
        <div class="mb-4">
          <label class="block text-[#e295ab] text-sm font-bold mb-2" for="username">
            Username
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-[#e7b0c0]" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <input v-model="username" @input="clearErrorMessage"
              class="itbkk-username shadow appearance-none border rounded w-full py-2 pl-10 pr-3 text-[#ec4e7b] leading-tight focus:outline-none focus:ring-2 focus:ring-[#e7b0c0]"
              id="username" type="text" placeholder="Enter your username" maxlength="50" />
          </div>
        </div>

        <!-- Password Field -->
        <div class="mb-6">
          <label class="block text-[#e295ab] text-sm font-bold mb-2" for="password">
            Password
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-[#e7b0c0]" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            <input v-model="password" @input="clearErrorMessage"
              class="itbkk-password shadow appearance-none border rounded w-full py-2 pl-10 pr-3 text-[#ec4e7b] leading-tight focus:outline-none focus:ring-2 focus:ring-[#e7b0c0]"
              id="password" type="password" placeholder="Enter your password" maxlength="14" />
          </div>
        </div>

        <!-- Sign In Button -->
        <div class="flex items-center justify-between">
          <button :disabled="!isFormValid" :class="[
            'itbkk-button-signin',
            'font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline transform transition-all duration-300 ease-in-out',
            isFormValid
              ? 'bg-[#ec4e7b]  text-white hover:scale-105'
              : 'bg-[#ebebeb] bg-opacity-50 text-gray-500 cursor-not-allowed disabled',
          ]" type="submit">
            Sign In
          </button>
          <a class="inline-block align-baseline font-bold text-sm text-[#e295ab] hover:text-[#e0829d]" href="#">
            Forgot Password?
          </a>
        </div>
      </form>
      <p class="text-center text-white text-xs">
        &copy;2024 ITB-KK. All rights reserved.
      </p>
    </div>
  </div>
</template>