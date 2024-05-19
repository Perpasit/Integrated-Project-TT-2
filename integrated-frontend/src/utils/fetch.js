export const fetch = async (url, options) => {
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

export const getItems = async (url) => {
  try {
    return await fetch(url);
  } catch (error) {
    // console.error("Error:", error);
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
    // console.error("Error:", error);
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
    // console.error("Error:", error);
    throw error;
  }
};

export const deleteItem = async (url) => {
  try {
    return await fetch(url, {
      method: "DELETE",
    });
  } catch (error) {
    // console.error("Error:", error);
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
    // console.error("Error:", error);
    throw error;
  }
};