export const fetch = async (url, options) => {
  const response = await window.fetch(url, options)
  return await response.json()
}

export const getItems = async (url) => {
  try {
    return await fetch(url)
  } catch (error) {
    console.error('Error:', error)
  }
}

export const postItem = async (url, item) => {
  try {
    return await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(item),
    })
  } catch (error) {
    console.error('Error:', error)
  }
}

export const putItem = async (url, item) => {
  try {
    return await fetch(url, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(item),
    })
  } catch (error) {
    console.error('Error:', error)
  }
}

export const deleteItem = async (url) => {
  try {
    return await fetch(url, {
      method: 'DELETE',
    })
  } catch (error) {
    console.error('Error:', error)
  }
}
