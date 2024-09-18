import { useUserStore } from "../stores/userStore";
import { useRouter } from "vue-router"; // Import Vue Router

// Define the fetch function to include token dynamically
const getToken = () => {
  const userStore = useUserStore(); // Access the store within the function
  return userStore.token || JSON.parse(localStorage.getItem("userStore"))?.token;
};

export const fetch = async (url, options = {}) => {
  const token = getToken();
  const userStore = useUserStore();
  const response = await window.fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      Authorization: `Bearer ${token}`,
    },
  });
  const contentType = response.headers.get("content-type");

  // Handle non-ok responses, especially 401 Unauthorized
    if (response.status === 401) {
      // Clear token if needed and redirect to login page
      userStore.logout(); // Assuming you have a logout action in your store
      throw new Error("Unauthorized. Redirecting to login.");
    }

    // if (contentType && contentType.includes("application/json")) {
    //   const errorData = await response.json();
    //   throw new Error(errorData.message);
    // } else {
    //   const errorMessage = await response.text();
    //   throw new Error(errorMessage);
    // }
  

  if (contentType && contentType.includes("application/json")) {
    return await response.json();
  } else {
    return response.text();
  }
};

export const getItems = async (url) => {
  try {
    return await fetch(url, {
      method: "GET",
    });
  } catch (error) {
    throw error;
  }
};

export const postItem = async (url, item) => {
  try {
    return await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(item),
    });
  } catch (error) {
    throw error;
  }
};

export const putItem = async (url, item) => {
  try {
    return await fetch(url, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(item),
    });
  } catch (error) {
    throw error;
  }
};

export const deleteItem = async (url) => {
  try {
    return await fetch(url, {
      method: "DELETE",
    });
  } catch (error) {
    throw error;
  }
};

export const patchItem = async (url, item) => {
  try {
    return await fetch(url, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(item),
    });
  } catch (error) {
    throw error;
  }
};


// Correct the fetchLogin function to use window.fetch
export const fetchLogin = async (url, options) => {
  const response = await window.fetch(url, options);
  const contentType = response.headers.get("content-type");

  if (!response.ok) {
    if (contentType && contentType.includes("application/json")) {
      const errorData = await response.json();
      throw new Error(errorData.message);
    } else {
      const errorMessage = await response.text();
      throw new Error(errorMessage);
    }
  }

  if (contentType && contentType.includes("application/json")) {
    return await response.json();
  } else {
    return response.text();
  }
};

export const postItemLogin = async (url, item) => {
  try {
    return await fetchLogin(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(item),
    });
  } catch (error) {
    throw error;
  }
};