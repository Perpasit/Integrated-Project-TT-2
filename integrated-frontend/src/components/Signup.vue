<template>
    <div class="signup-container">
      <h2>Create Account</h2>
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
          <input
            type="password"
            id="password"
            v-model="password"
            required
            placeholder="Enter your password"
          />
          <span v-if="errors.password" class="error">{{ errors.password }}</span>
        </div>
  
        <button type="submit">Create Account</button>
      </form>
      <p v-if="signupError" class="signup-error">{{ signupError }}</p>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  
  const name = ref('');
  const password = ref('');
  const errors = ref({ name: '', password: '' });
  const signupError = ref('');
  const router = useRouter();
  
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
  
    return valid;
  };
  
  const handleSignup = async () => {
    if (validateForm()) {
      try {
        // Simulate API call
        const response = await fetch('/api/signup', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ name: name.value, password: password.value }),
        });
  
        if (!response.ok) {
          throw new Error('Signup failed');
        }
  
        // On success, redirect to login page
        router.push({ name: 'Login' });
  
      } catch (error) {
        signupError.value = 'Failed to create account.';
      }
    }
  };
  </script>
  
  <style scoped>
  .signup-container {
    max-width: 400px;
    margin: 0 auto;
    padding: 20px;
    border: 1px solid #ccc;
    border-radius: 4px;
    background-color: #fff;
  }
  
  /* Similar styles as Login.vue */
  </style>
  