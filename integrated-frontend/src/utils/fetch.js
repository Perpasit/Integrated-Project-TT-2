import { useRouter } from "vue-router"
import { useUserStore } from "../stores/userStore"

// Define the fetch function to include token dynamically
const getToken = () => {
  const userStore = useUserStore() // Access the store within the function
  return userStore.token || JSON.parse(localStorage.getItem("userStore"))?.accessToken
}

export const fetchWithAuth = async (url, options = {}) => {
  const userStore = useUserStore()

  const router = useRouter()


  // Load the stored tokens
  userStore.loadFromLocalStorage()

  const token = userStore.accessToken
  const refreshToken = userStore.refreshToken

  // Check if the access token is expired
  const tokenExpirationTime = JSON.parse(atob(token.split(".")[1])).exp * 1000
  if (Date.now() >= tokenExpirationTime) {
    try {
      // Send refresh token request to get a new access token
      const refreshResponse = await window.fetch(
        "http://localhost:8080/token",
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${refreshToken}`,
            "Content-Type": "application/json"
          }
        }
      )

      if (refreshResponse.ok) {
        const data = await refreshResponse.json()
        userStore.accessToken = data.access_token
        userStore.saveToLocalStorage() // Save the new access token
      } else {
        // Refresh token failed or expired
        userStore.logout()
        throw new Error("Session expired, please login again.")
      }
    } catch (error) {
      throw new Error("Failed to refresh token.")
    }
  }

  // Proceed with the actual API call using the new or valid token
  const response = await window.fetch(url, {
    ...options,
    headers: {
      ...options.headers,
    }
  })

  const contentType = response.headers.get("content-type")

  if (response.status === 401) {
    userStore.logout()
    throw new Error("Unauthorized. Redirecting to login.")
  }

  if (response.status === 403) {
    router.push({ name: "AccessDenied" })
    throw new Error("You do not have permission.")
  }

  if (contentType && contentType.includes("application/json")) {
    return await response.json()
  } else {
    return response.text()
  }
}

const checkAndRefreshToken = async () => {
  const userStore = useUserStore()

  // const router = useRouter();

  // Load the stored tokens
  userStore.loadFromLocalStorage()

  const token = userStore.accessToken
  const refreshToken = userStore.refreshToken

  // Check if the access token is expired
  const tokenExpirationTime = JSON.parse(atob(token.split(".")[1])).exp * 1000
  if (Date.now() >= tokenExpirationTime) {
    try {
      // Send refresh token request to get a new access token
      const refreshResponse = await window.fetch(
        "http://localhost:8080/token",
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${refreshToken}`,
            "Content-Type": "application/json"
          }
        }
      )

      if (refreshResponse.ok) {
        const data = await refreshResponse.json()
        userStore.accessToken = data.access_token
        userStore.saveToLocalStorage() // Save the new access token
      } else {
        // Refresh token failed or expired
        userStore.logout()
        throw new Error("Session expired, please login again.")
      }
    } catch (error) {
      throw new Error("Failed to refresh token.")
    }
  }
}

export const getItems = async (url) => {
  try {
    await checkAndRefreshToken()
    return await fetchWithAuth(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },

    })
  } catch (error) {
    throw error
  }
}

export const postItem = async (url, item) => {

  const userStore = useUserStore()
  const token = getToken()

  try {
    await checkAndRefreshToken()
    return await fetchWithAuth(url, {
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
    await checkAndRefreshToken()
    return await fetchWithAuth(url, {
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
    await checkAndRefreshToken()
    return await fetchWithAuth(url, {
      method: "DELETE",
      Authorization: `Bearer ${token}`

    })
  } catch (error) {
    throw error
  }
}

export const patchItem = async (url, item) => {

  const userStore = useUserStore()
  const token = getToken()
  try {
    await checkAndRefreshToken()
    return await fetchWithAuth(url, {
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
      },
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
