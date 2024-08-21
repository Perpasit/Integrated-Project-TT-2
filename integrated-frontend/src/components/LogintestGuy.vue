<script setup lang="ts">
import { ref, computed } from "vue"
import { useRouter } from "vue-router"
import { postItem } from "../utils/fetch" // Update with the correct path to fetch.js

const username = ref("")
const password = ref("")
const errors = ref({ username: "", password: "" })
const loginError = ref("")
const router = useRouter()

const validateForm = () => {
  let valid = true
  if (!username.value || username.value.length > 50) {
    errors.value.username = "Username is required and must be less than 50 characters."
    valid = false
  } else {
    errors.value.username = ""
  }

  if (!password.value) {
    errors.value.password = "Password is required."
    valid = false
  } else {
    errors.value.password = ""
  }

  return valid
}

// Computed property to determine if the button should be disabled
const isButtonDisabled = computed(() => {
  return !username.value || username.value.length > 50 || !password.value
})

const clearErrorMessage = () => {
  loginError.value = ""
}

const handleSubmit = async () => {
  if (validateForm()) {
    try {
      // Use the postItem function from fetch.js
      const response = await postItem("/api/login", {
        name: username.value,
        password: password.value,
      })

      // Handle the response from the API
      if (response.success) {
        // On success, redirect to the home page
        router.push({ name: "home" })
      } else {
        // Handle any additional error messages from the API
        loginError.value = response.message || "Username or Password is incorrect."
      }
    } catch (error) {
      loginError.value = error.message || "Username or Password is incorrect."
    }
  }
}
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gradient-to-r from-blue-500 to-purple-600">
    <div class="w-full max-w-md">
      <form @submit.prevent="handleSubmit" class="bg-white shadow-2xl rounded-lg px-8 pt-6 pb-8 mb-4">
        <h2 class="text-3xl font-bold mb-6 text-center text-gray-800">Welcome to ITB-KK</h2>
        <div v-if="loginError" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-4" role="alert">
          <p>{{ loginError }}</p>
        </div>
        <div class="mb-4">
          <label class="block text-gray-700 text-sm font-bold mb-2" for="username">
            Username
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <input v-model="username" @input="clearErrorMessage"
              class="shadow appearance-none border rounded w-full py-2 pl-10 pr-3 text-gray-700 leading-tight focus:outline-none focus:ring-2 focus:ring-blue-500"
              id="username" type="text" placeholder="Enter your username">
            <span v-if="errors.username" class="text-red-500 text-sm">{{ errors.username }}</span>
          </div>
        </div>
        <div class="mb-6">
          <label class="block text-gray-700 text-sm font-bold mb-2" for="password">
            Password
          </label>
          <div class="relative">
            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            <input v-model="password" @input="clearErrorMessage"
              class="shadow appearance-none border rounded w-full py-2 pl-10 pr-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:ring-2 focus:ring-blue-500"
              id="password" type="password" placeholder="Enter your password">
            <span v-if="errors.password" class="text-red-500 text-sm">{{ errors.password }}</span>
          </div>
        </div>
        <div class="flex items-center justify-between">
          <button
            :disabled="isButtonDisabled"
            :class="[
              'font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline transform transition-all duration-300 ease-in-out',
              isButtonDisabled 
                ? 'bg-gray-300 text-gray-500 cursor-not-allowed' 
                : 'bg-blue-500 hover:bg-blue-700 text-white hover:scale-105'
            ]"
            type="submit">
            Sign In
          </button>
          <a class="inline-block align-baseline font-bold text-sm text-blue-500 hover:text-blue-800" href="#">
            Forgot Password?
          </a>
        </div>
      </form>
      <p class="text-center text-gray-200 text-xs">
        &copy;2024 ITB-KK. All rights reserved.
      </p>
    </div>
  </div>
</template>
