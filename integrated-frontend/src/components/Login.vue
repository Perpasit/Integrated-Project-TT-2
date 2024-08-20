<script setup lang="ts">
import { ref } from "vue"
import { useRouter } from "vue-router"
import { postItem } from "../utils/fetch" // Update with the correct path to fetch.js

const name = ref("")
const password = ref("")
const errors = ref({ name: "", password: "" })
const loginError = ref("")
const router = useRouter()

const validateForm = () => {
  let valid = true
  if (!name.value) {
    errors.value.name = "Name is required."
    valid = false
  } else {
    errors.value.name = ""
  }

  if (!password.value) {
    errors.value.password = "Password is required."
    valid = false
  } else {
    errors.value.password = ""
  }

  return valid
}

const handleLogin = async () => {
  if (validateForm()) {
    try {
      // Use the postItem function from fetch.js
      const response = await postItem("/api/login", {
        name: name.value,
        password: password.value,
      })

      // Handle the response from the API
      if (response.success) {
        // On success, redirect to the home page
        router.push({ name: "home" })
      } else {
        // Handle any additional error messages from the API
        loginError.value =
          response.message || "Username or Password is incorrect."
      }
    } catch (error) {
      loginError.value = error.message || "Username or Password is incorrect."
    }
  }
}

// const handleLogin = async () => {
//   if (validateForm()) {
//     try {
//       // Simulate API call
//       const response = await fetch('/api/login', {
//         method: 'POST',
//         headers: {
//           'Content-Type': 'application/json',
//         },
//         body: JSON.stringify({ name: name.value, password: password.value }),
//       });

//       if (!response.ok) {
//         throw new Error('Login failed');
//       }

//       // On success, redirect to a different page
//       router.push({ name: 'home' });

//     } catch (error) {
//       loginError.value = 'Invalid name or password.';
//     }
//   }
// };
// //test
// const handleLogin = async () => {
//   if (validateForm()) {
//     // Simple authentication check
//     if (name.value === 'abd' && password.value === '123') {
//       // Redirect to the /task route after successful login
//       router.push({ path: '/task' });
//     } else {
//       loginError.value = 'Invalid name or password.';
//     }
//   }
// };
</script>

<template>
  <div
    class="bg-gradient-to-br from-[#9194eb] to-[#bff7ff] min-h-screen flex justify-center align-middle"
  >
    <div class="login-container">
      <h2 class="justify-center text-center my-3 text-2xl">Login</h2>
      <form @submit.prevent="handleLogin">
        <div class="itbkk-username">
          <label for="name">Username</label>
          <input
            type="text"
            id="name"
            v-model="name"
            required
            placeholder="Enter your name"
          />
          <span v-if="errors.name" class="error">{{ errors.name }}</span>
        </div>

        <div class="itbkk-password">
          <label for="password">Password</label>
          <input
            type="password"
            id="password"
            v-model="password"
            required
            placeholder="Enter your password"
          />
          <span v-if="errors.password" class="error">{{
            errors.password
          }}</span>
        </div>

        <button type="submit" class="itbkk-button-signin">Sign In</button>
      </form>
      <p v-if="loginError" class="itbkk-message">{{ loginError }}</p>
      <p class="signup">
        Don't have an account?
        <router-link to="/signup">Create a new account</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 12lh auto;
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
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #28a745;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #218838;
}

.error {
  color: red;
  font-size: 0.875em;
}

.login-error {
  color: red;
  margin-top: 15px;
}

.signup {
  margin-top: 20px;
  text-align: center;
}

.signup a {
  color: #007bff;
  text-decoration: none;
}

.signup a:hover {
  text-decoration: underline;
}
</style>
