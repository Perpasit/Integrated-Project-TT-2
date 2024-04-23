<template>
    <div>
      <table v-if="filteredData.length" class="table-auto border border-gray-200 shadow-md bg-white rounded-lg">
        <thead class="bg-green-500 text-white">
          <tr>
            <th v-for="key in columns" @click="sortBy(key)" :class="{ 'bg-green-700': sortKey === key }" class="cursor-pointer select-none px-4 py-2">
              {{ capitalize(key) }}
              <span class="arrow" :class="sortOrders[key] > 0 ? 'asc' : 'dsc'"></span>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="entry in filteredData" :key="entry.id">
            <td v-for="key in columns" :key="key" class="border px-4 py-2">{{ entry[key] }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else class="text-gray-500">No matches found.</p>
    </div>
  </template>
  
  <script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  data: Array,
  columns: Array,
  filterKey: String
})

const sortKey = ref('')
const sortOrders = ref(
  props.columns.reduce((o, key) => ((o[key] = 1), o), {})
)

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
  const key = sortKey.value
  if (key) {
    const order = sortOrders.value[key]
    data = data.slice().sort((a, b) => {
      a = a[key]
      b = b[key]
      return (a === b ? 0 : a > b ? 1 : -1) * order
    })
  }
  return data
})

function sortBy(key) {
  sortKey.value = key
  sortOrders.value[key] *= -1
}

function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1)
}  </script>
  