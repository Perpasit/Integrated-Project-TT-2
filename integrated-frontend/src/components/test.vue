<template>
    <div class="signup-container">
      <h2 class="justify-center text-center my-3 text-2xl">Create Account</h2>
      <form @submit.prevent="handleSignup">
        <div class="form-group">
          <label for="name">Name</label>
          <input
            type="text"
            id="name"
            v-model="name"
            required
            placeholder="Enter your name"
          />
          <span v-if="errors.name" class="error">{{ errors.name }}</span>
        </div>
  
        <div class="form-group">
          <label for="password">Password</label>
          <div class="password-wrapper">
            <input
              :type="showPassword ? 'text' : 'password'"
              id="password"
              v-model="password"
              required
              placeholder="Enter your password"
            />
            <button type="button" @click="togglePasswordVisibility" class="toggle-password">
              {{ showPassword ? 'Hide' : 'Show' }}
            </button>
          </div>
          <span v-if="errors.password" class="error">{{ errors.password }}</span>
        </div>
  
        <div class="form-group">
          <label for="confirmPassword">Confirm Password</label>
          <div class="password-wrapper">
            <input
              :type="showConfirmPassword ? 'text' : 'password'"
              id="confirmPassword"
              v-model="confirmPassword"
              required
              placeholder="Confirm your password"
            />
            <button type="button" @click="toggleConfirmPasswordVisibility" class="toggle-password">
              {{ showConfirmPassword ? 'Hide' : 'Show' }}
            </button>
          </div>
          <span v-if="errors.confirmPassword" class="error">{{ errors.confirmPassword }}</span>
        </div>
  
        <button type="submit">Create Account</button>
      </form>
      <p v-if="signupError" class="signup-error">{{ signupError }}</p>
      <p class="login">
        Already have an account? 
        <router-link to="/login">Login</router-link>
      </p>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { postItem } from '../utils/fetch';  // Update with the correct path to fetch.js
  
  const name = ref('');
  const password = ref('');
  const confirmPassword = ref('');
  const errors = ref({ name: '', password: '', confirmPassword: '' });
  const signupError = ref('');
  const router = useRouter();
  const showPassword = ref(false);
  const showConfirmPassword = ref(false);
  
  const validateForm = () => {
    let valid = true;
    if (!name.value) {
      errors.value.name = 'Name is required.';
      valid = false;
    } else {
      errors.value.name = '';
    }
  
    if (!password.value) {
      errors.value.password = 'Password is required.';
      valid = false;
    } else {
      errors.value.password = '';
    }
  
    if (password.value !== confirmPassword.value) {
      errors.value.confirmPassword = 'Passwords do not match.';
      valid = false;
    } else {
      errors.value.confirmPassword = '';
    }
  
    return valid;
  };
  
  const handleSignup = async () => {
    if (validateForm()) {
      try {
        const response = await postItem('/api/signup', { name: name.value, password: password.value });
  
        if (response.success) {
          router.push({ name: 'login' });
        } else {
          signupError.value = response.message || 'Failed to create account.';
        }
      } catch (error) {
        signupError.value = error.message || 'Failed to create account.';
      }
    }
  };
  
  const togglePasswordVisibility = () => {
    showPassword.value = !showPassword.value;
  };
  
  const toggleConfirmPasswordVisibility = () => {
    showConfirmPassword.value = !showConfirmPassword.value;
  };
  </script>
  
  <style scoped>
  .signup-container {
    max-width: 400px;
    margin: 10vh auto; /* Adjusted margin for better centering */
    padding: 20px;
    border: 1px solid #ccc;
    border-radius: 4px;
    background-color: #fff;
  }
  
  .form-group {
    margin-bottom: 15px;
  }
  
  label {
    display: block;
    margin-bottom: 5px;
    font-weight: bold;
  }
  
  input[type="text"],
  input[type="password"] {
    width: calc(100% - 50px); /* Adjust width to accommodate the toggle button */
    padding: 8px;
    box-sizing: border-box;
  }
  
  .password-wrapper {
    display: flex;
    align-items: center;
  }
  
  .toggle-password {
    margin-left: 10px;
    background: none;
    border: none;
    color: #007bff;
    cursor: pointer;
  }
  
  button {
    width: 100%;
    padding: 10px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
  
  button:hover {
    background-color: #0056b3;
  }
  
  .error {
    color: red;
    font-size: 12px;
  }
  
  .signup-error {
    margin-top: 15px;
    color: red;
    font-size: 14px;
  }
  
  .login {
    margin-top: 20px;
    text-align: center;
  }
  
  .login a {
    color: #007bff;
    text-decoration: none;
  }
  
  .login a:hover {
    text-decoration: underline;
  }
  </style>
  