<script setup>
import { computed } from "vue"

const props = defineProps({
  data: Array,
  columns: Array,
  filterKey: String,
})

const filteredData = computed(() => {
  let { data, filterKey } = props
  if (filterKey) {
    filterKey = filterKey.toLowerCase()
    data = data.filter((row) => {
      return Object.keys(row).some((key) => {
        return String(row[key]).toLowerCase().indexOf(filterKey) > -1
      })
    })
  }

  return data
})

function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1)
}
</script>

<template>
  <div>
    <table
      v-if="filteredData.length"
      class="table-auto border border-gray-200 shadow-md bg-white rounded-lg"
    >
      <thead class="bg-green-500 text-white">
        <tr>
          <th v-for="key in columns" class="w-screen text-start px-4">
            {{ capitalize(key) }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="entry in filteredData" :key="entry.id">
          <td v-for="key in columns" :key="key" class="border px-4 py-2">
            {{ entry[key] }}
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="text-gray-500">No matches found.</p>
  </div>
</template>

<style scoped></style>
