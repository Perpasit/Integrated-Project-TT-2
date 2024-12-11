<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { postItemLogin } from "../utils/fetch";
import { useUserStore } from "../stores/userStore";
import { useBoardStore } from '../stores/boardStore';
import * as jwtDecode from "jwt-decode";

const router = useRouter();
const userStore = useUserStore();
const boardStore = useBoardStore();
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
        await boardStore.fetchAllBoards();
        const boards = boardStore.getBoardsPersonal ;

        if (boards && boards.length > 0) {
          const firstBoard = boards[0];
          console.log(firstBoard.id)
          router.push({ name: "home", params: { boardId: firstBoard.id } });
        } else {
          router.push({ name: "Board" });
        }
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
  <div class="flex items-center justify-center bg-gradient-to-r from-[#C8B2D4] to-[#E9BDCD] min-h-screen">
    <div class="w-full max-w-md">
      <form @submit.prevent="handleSubmit"
        class="bg-white bg-opacity-30 backdrop-filter backdrop-blur-lg shadow-2xl rounded-3xl px-12 pt-12 pb-12">

        <p class="text-3xl font-bold text-left mb-2 text-white">
          Login
        </p>
        <p class="text-xs  mb-6 text-left text-white font-light">
          Welcome to ITB-KK
        </p>

        <!-- Error Message -->
        <div v-if="errorMessage" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4" role="alert">
          <p class="itbkk-message">{{ errorMessage }}</p>
        </div>

        <!-- Username Field -->
        <div class="mb-6">
          <div class="relative flex items-center">
            <input v-model="username" @input="clearErrorMessage"
              class="w-full text-white text-sm border-b border-white bg-transparent py-2 pl-3 focus:outline-none focus:border-b-2 focus:border-[#ffffff] placeholder-opacity-70 placeholder-white"
              id="username" type="text" placeholder="Username" maxlength="50" />
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-white opacity-50 absolute right-2" fill="none"
              viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
          </div>
        </div>

        <!-- Password Field -->
        <div class="mb-6">
          <div class="relative flex items-center">
            <input v-model="password" @input="clearErrorMessage"
              class="w-full text-white text-sm border-b border-white bg-transparent py-2 pl-3 focus:outline-none focus:border-b-2 focus:border-[#ffffff] placeholder-opacity-70 placeholder-white"
              id="password" type="password" placeholder="Password" maxlength="14" />
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-white opacity-50 absolute right-2" fill="none"
              viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
          </div>
        </div>

        <!-- Forgot Password -->
        <div class="mb-6">
          <label class="block text-white text-sm font-light opacity-80 text-left" for="forgot-password">
            <a href="#" class="hover:underline">Forgot Password?</a>
          </label>
        </div>

        <!-- Sign In Button -->
        <div class="flex items-center justify-center">
          <button :disabled="!isFormValid" :class="[
            'itbkk-button-signin',
            'w-full font-light py-2 focus:outline-none focus:shadow-outline rounded-xl transform transition-all duration-300 ease-in-out',
            isFormValid
              ? 'bg-[#e38f80] text-white hover:scale-105'
              : 'bg-[#ebebeb] bg-opacity-30 text-white cursor-not-allowed disabled',
          ]" type="submit">
            Sign In
          </button>
        </div>
      </form>

      <p class="text-center text-white text-xs mt-4">
        &copy;2024 ITB-KK. All rights reserved.
      </p>
    </div>
  </div>
</template>
