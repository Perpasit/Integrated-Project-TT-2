async function getItems(url) {
    try {
      const data = await fetch(url)
      const items = await data.json().then((response) => {
        return response
      })
      return items
    } catch (error) {
      console.log(`error: ${error}`)
    }
  }
  async function getItemById(url, id) {
    try {
      const data = await fetch(`${url}/${id}`)
      const item = await data.json()
      return item
    } catch (error) {
      console.log(`error: ${error}`)
    }
  }
  
  async function deleteTaskById(url, id) {
    try {
      const res = await fetch(`${url}/${id}`, {
        method: 'DELETE',
      })
  
      return res.status
    } catch (error) {
      console.log(`Error: ${error}`)
    }
  }
  
  async function addItem(url, newItem) {
    try {
      const res = await fetch(url, {
        method: 'POST', // add
        headers: {
          'content-type': 'application/json', // add contents
        },
        body: JSON.stringify({
          ...newItem, // sent add data . destructuring object
        }), // js to  json
      })
      const addedItem = await res.json()
      return addedItem
    } catch (error) {
      console.log(`error: ${error}`)
    }
  }
  
  async function editItem(url, id, editItem) {
    try {
      const res = await fetch(`${url}/${id}`, {
        method: 'PUT',
        headers: {
          'content-type': 'application/json',
        },
        body: JSON.stringify({
          ...editItem,
        }),
      })
      const editedItem = await res.json()
      return editedItem
    } catch (error) {
      console.log(`error: ${error}`)
    }
  }
  export {
    getItems,
    getItemById,
    deleteTaskById,
    addItem,
    editItem,
  }
  