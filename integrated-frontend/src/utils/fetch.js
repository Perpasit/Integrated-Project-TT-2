//import { useRouter } from "vue-router"
import { useUserStore } from "../stores/userStore"

// Define the fetch function to include token dynamically
const getToken = () => {
  const userStore = useUserStore() // Access the store within the function
  return (
    userStore.accessToken ||
    JSON.parse(localStorage.getItem("userStore"))?.accessToken
  )
}
const getRefreshToken = () => {
  const userStore = useUserStore() // Access the store within the function
  return (
    userStore.refreshToken ||
    JSON.parse(localStorage.getItem("userStore"))?.refreshToken
  )
}

export const fetch = async (url, options = {}) => {
  const userStore = useUserStore()

  let response = await window.fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      Authorization: `Bearer ${userStore.accessToken}`
    }
  })

  const contentType = response.headers.get("content-type")

  if (response.status === 401) {
    console.log("401 Unauthorized: Token expired. Attempting to refresh...")

    if (!userStore.refreshInProgress && userStore.isAccessTokenExpired()) {
      try {
        await userStore.refreshAccessToken()
        console.log("Access token refreshed successfully. Retrying request...")

        response = await window.fetch(url, {
          ...options,
          headers: {
            ...options.headers,
            Authorization: `Bearer ${userStore.accessToken}`
          }
        })
      } catch (error) {
        console.error("Failed to refresh access token:", error)
        userStore.logout()
        throw new Error("Unauthorized. Redirecting to login.")
      }
    } else {
      userStore.logout()
      throw new Error("Unauthorized. Redirecting to login.")
    }
  }

  if (contentType && contentType.includes("application/json")) {
    return await response.json()
  } else {
    return await response.text()
  }
}

export const fetchWithAuthCheck = async (url, options = {}) => {
  const token = getToken()

  if (!token) {
    console.error("No token available. Redirecting to login.")
    router.push({ name: "login" })
    return
  }

  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        Authorization: `Bearer ${token}`
      }
    })

    if (response.status === 401) {
      console.error("Access token expired. Redirecting to login.")
      router.push({ name: "login" })
    }

    return response
  } catch (error) {
    console.error("Fetch Error:", error)
    throw error
  }
}

export const postRefreshToken = async (url) => {
  const refreshToken = getRefreshToken()
  try {
    return await fetch(url, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${refreshToken}`
      }
    })
  } catch (error) {
    throw error
  }
}

export const getItems = async (url) => {
  const userStore = useUserStore()
  const token = getToken()
  try {
    userStore.loadFromLocalStorage()
    return await fetchWithAuthCheck(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      }
    })
  } catch (error) {
    throw error
  }
}

export const postItem = async (url, item) => {
  const userStore = useUserStore()
  const token = getToken()
  try {
    userStore.loadFromLocalStorage()
    return await fetchWithAuthCheck(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(item)
    })
  } catch (error) {
    throw error
  }
}

export const putItem = async (url, item) => {
  const userStore = useUserStore()
  const token = getToken()
  try {
    userStore.loadFromLocalStorage()
    return await fetchWithAuthCheck(url, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(item)
    })
  } catch (error) {
    throw error
  }
}

export const deleteItem = async (url) => {
  const userStore = useUserStore()
  const token = getToken()
  try {
    userStore.loadFromLocalStorage()
    return await fetchWithAuthCheck(url, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      }
    })
  } catch (error) {
    throw error
  }
}

export const patchItem = async (url, item) => {
  const userStore = useUserStore()
  const token = getToken()
  try {
    userStore.loadFromLocalStorage()
    return await fetchWithAuthCheck(url, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(item)
    })
  } catch (error) {
    throw error
  }
}

// Correct the fetchLogin function to use window.fetch
export const fetchLogin = async (url, options) => {
  const response = await window.fetch(url, options)
  const contentType = response.headers.get("content-type")

  if (!response.ok) {
    if (contentType && contentType.includes("application/json")) {
      const errorData = await response.json()
      throw new Error(errorData.message)
    } else {
      const errorMessage = await response.text()
      throw new Error(errorMessage)
    }
  }

  if (contentType && contentType.includes("application/json")) {
    return await response.json()
  } else {
    return response.text()
  }
}

export const postItemLogin = async (url, item) => {
  try {
    return await fetchLogin(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(item)
    })
  } catch (error) {
    throw error
  }
}

export const fetchNoUser = async (url, options = {}) => {
  const response = await window.fetch(url, options)
  const contentType = response.headers.get("content-type")

  if (!response.ok) {
    if (contentType && contentType.includes("application/json")) {
      const errorData = await response.json()
      throw new Error(errorData.message)
    } else {
      const errorMessage = await response.text()
      throw new Error(errorMessage)
    }
  }

  if (contentType && contentType.includes("application/json")) {
    return await response.json()
  } else {
    return response.text()
  }
}

export const getItemsNoUser = async (url) => {
  const userStore = useUserStore()
  try {
    userStore.loadFromLocalStorage()
    return await fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json"
      }
    })
  } catch (error) {
    throw error
  }
}

// import { useRouter } from "vue-router";
// import { useUserStore } from "../stores/userStore";

// // Get the token
// const getToken = () => {
//   const userStore = useUserStore(); // Access the store within the function
//   return userStore.token || JSON.parse(localStorage.getItem("userStore"))?.token;
// };

// export const fetch = async (url, options = {}) => {
//   const token = getToken();
//   const router = useRouter(); // Initialize router for redirecting

//   const response = await window.fetch(url, {
//     ...options,
//     headers: {
//       ...options.headers,
//       Authorization: `Bearer ${token}`,
//     },
//   });

//   const contentType = response.headers.get("content-type");

//   if (!response.ok) {
//     if (response.status === 401) {
//       // Redirect to login if 401 error is encountered
//       router.push({ name: "login" });
//       return; // Stop execution once redirected
//     }

//     if (contentType && contentType.includes("application/json")) {
//       const errorData = await response.json();
//       throw new Error(errorData.message);
//     } else {
//       const errorMessage = await response.text();
//       throw new Error(errorMessage);
//     }
//   }

//   if (contentType && contentType.includes("application/json")) {
//     return await response.json();
//   } else {
//     return response.text();
//   }
// };
