<script setup>
import { ref, onMounted } from "vue"
import { useRouter } from "vue-router"

const router = useRouter()
const countdown = ref(3)

onMounted(() => {
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value === 0) {
      clearInterval(timer)
      router.push("/")
    }
  }, 1000)
})
</script>

<template>
  <div
    class="mx-auto flex flex-col justify-center items-center min-h-screen text-center"
  >
    <h1 class="text-2xl font-bold mb-4 text-red-600">
      The requested task does not exist !!!
    </h1>
    <p>Go to home page in {{ countdown }} seconds...</p>
  </div>
</template>

<style scoped></style>
