import { defineStore } from "pinia"
import router from "../router"
import { postRefreshToken } from "../utils/fetch";
import * as jwtDecode from "jwt-decode";

export const useUserStore = defineStore("user", {
  state: () => ({
    userInfo: {
      sub: "",
      role: "",
      iss: "",
      name: "",
      oid: "",
      exp: "",
      iat: "",
      email: "",
    },
    isAuthenticated: false,
    accessToken: "",
    refreshToken: "",
    refreshInProgress: false, // Add flag to track refresh status
  }),

  getters: {
    getUserInfo: (state) => state.userInfo,
    getAccountName: (state) => state.userInfo.name,
    getIsAuthenticated: (state) => state.isAuthenticated,
    getAccessToken: (state) => state.accessToken,
    getRefreshToken: (state) => state.refreshToken,
    getUserOid: (state) => state.userInfo.oid,
  },

  actions: {
    setUserInfo(info, accessToken, refreshToken) {
      this.userInfo = info;
      this.accessToken = accessToken;
      this.refreshToken = refreshToken;
      this.isAuthenticated = true;
      this.refreshInProgress = false;
      this.saveToLocalStorage();
    },

    logout() {
      this.userInfo = {
        sub: "",
        role: "",
        iss: "",
        name: "",
        oid: "",
        exp: "",
        iat: "",
        email: "",
      };
      this.isAuthenticated = false;
      this.accessToken = "";
      this.refreshToken = "";
      this.refreshInProgress = false; // Reset the flag on logout
      localStorage.removeItem("userStore");
      localStorage.removeItem("isLimitEnabled");
      router.push({ name: "Login" });
    },

    saveToLocalStorage() {
      const data = {
        userInfo: this.userInfo,
        isAuthenticated: this.isAuthenticated,
        accessToken: this.accessToken,
        refreshToken: this.refreshToken,
        refreshInProgress: this.refreshInProgress,
      };
      localStorage.setItem("userStore", JSON.stringify(data));
      
    },

    loadFromLocalStorage() {
      const storedData = localStorage.getItem("userStore");
      if (storedData) {
        const parsedData = JSON.parse(storedData);
        this.userInfo = parsedData.userInfo || this.userInfo; // ให้แน่ใจว่า userInfo ถูกโหลดขึ้นมา
        this.isAuthenticated = parsedData.isAuthenticated || false;
        this.accessToken = parsedData.accessToken || "";
        this.refreshToken = parsedData.refreshToken || "";
        this.refreshInProgress = parsedData.refreshInProgress || false;
      } else {
        console.warn("No stored user data found in localStorage.");
      }
    }
    ,

    // Check if access token is expired
    isAccessTokenExpired() {
      const currentTime = Math.floor(Date.now() / 1000); // Current time in seconds
      return this.userInfo.exp < currentTime; // Compare with token's exp time
    },

    async refreshAccessToken() {
      if (this.refreshInProgress) {
        return; 
      }
    
      this.refreshInProgress = true; 
      try {
        const response = await postRefreshToken(`${import.meta.env.VITE_API_ENDPOINT2}/token`);
        
        if (response) {
          const data = response;
          const decodedToken = jwtDecode.jwtDecode(response.access_token);
          
          this.accessToken = data.access_token; // Save the new access token
          this.userInfo = {
            ...this.userInfo, // Keep existing userInfo properties
            ...decodedToken, // Update with new token details (including oid)
          };
          
          console.log("New access token:", this.accessToken); // Log เพื่อตรวจสอบค่า accessToken
          console.log("New user OID:", this.userInfo.oid); // Log เพื่อตรวจสอบว่า oid ถูกอัปเดต
          
          this.saveToLocalStorage();
        }        
      } catch (error) {
        console.error("Failed to refresh access token", error);
        this.logout(); // Handle error and logout user if refresh fails
      } finally {
        this.refreshInProgress = false; // ตั้งค่าให้เป็น false หลังจากรีเฟรชเสร็จ
        this.saveToLocalStorage();
      }
    }
    ,
  },
});

