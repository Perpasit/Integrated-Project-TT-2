// async function getAllTasks() {
//   try {
//       const data = await fetch(`${url}`)
//       const tasks = await data.json()
//       return tasks
//   } catch (error) {
//       console.log(`error: ${error}`)
//   }
// }

// async function getTaskById(id) {
//   try {
//       const data = await fetch(`${url}/${id}`)
//       const task = await data.json()
//       return task
//   } catch (error) {
//       console.log(`error: ${error}`)
//   }
// }

// async function addTask(task) {
//   try {
//       const response = await fetch(`${url}`, {
//           method: 'POST',
//           headers: {
//               'Content-Type': 'application/json'
//           },
//           body: JSON.stringify({ ...task })
//       })
//       const newTask = await response.json()
//       return newUser
//   } catch (error) {
//       console.error(`Error adding user: ${error}`)
//   }
// }

// async function updateTask(task) {
//   try {
//       const response = await fetch(`${url}/${user.id}`, {
//           method: 'PATCH',
//           headers: {
//               'Content-Type': 'application/json'
//           },
//           body: JSON.stringify({ ...task })
//       })
//       const updatedUser = await response.json()
//       return updatedUser
//   } catch (error) {
//       console.error(`Error updating user: ${error}`)
//   }
// }

// async function deleteTask(id) {
//   try {
//       const response = await fetch(`${url}/${id}`, {
//           method: 'DELETE'
//       })
//       const deletedTask = await response.json()
//       return deletedTask
//   } catch (error) {
//       console.error(`Error deleting user: ${error}`)
//   }
// }

// export { getAllTasks, getTaskById, addTask, updateTask, deleteTask }