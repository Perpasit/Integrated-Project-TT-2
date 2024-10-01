//import { useRouter } from "vue-router"
import { useUserStore } from "../stores/userStore"

// Define the fetch function to include token dynamically
const getToken = () => {
  const userStore = useUserStore() // Access the store within the function
  return userStore.accessToken  || JSON.parse(localStorage.getItem("userStore"))?.accessToken
}
const getRefreshToken = () => {
  const userStore = useUserStore() // Access the store within the function
  return userStore.refreshToken  || JSON.parse(localStorage.getItem("userStore"))?.refreshToken
}


export const fetch = async (url, options = {}) => {

  const userStore = useUserStore()
  // const router = useRouter()


  const response = await window.fetch(url, {
    ...options,
    headers: {
      ...options.headers,
    }
  })
  const contentType = response.headers.get("content-type")

  // Handle non-ok responses, especially 401 Unauthorized
  if (response.status === 401) {
    // Clear token if needed and redirect to login page
    userStore.logout() // Assuming you have a logout action in your store
    throw new Error("Unauthorized. Redirecting to login.")
  }


  // if (response.status === 403) {
  //   router.push({ name: "AccessDenied" }) // Redirect to the AccessDenied component
  //   throw new Error("You do not have permission to change board visibility mode.")
  // }

  if (contentType && contentType.includes("application/json")) {
    return await response.json()
  } else {
    return response.text()
  }
}


export const fetchWithAuthCheck = async (url, options = {}) => {
  const userStore = useUserStore();
  
  if (!userStore.refreshInProgress && userStore.isAccessTokenExpired()) {
    console.log("Access token expired or about to expire, attempting refresh...");
    await userStore.refreshAccessToken(); 
  }
  const token = getToken(); 

  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        Authorization: `Bearer ${token}`,
      },
    });

    return response; 
    
  } catch (error) {
    console.error("Fetch error:", error);
    throw error; 
  }
  
};

export const postRefreshToken = async (url) => {
  const refreshToken = getRefreshToken()
  try {
    return await fetch(url, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${refreshToken}`
      },
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
      },
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
