import { defineStore } from "pinia";
import { getItems, patchItem ,getItemsNoUser} from "../utils/fetch";

export const useGlobalSettingStore = defineStore("globalSetting", {
  state: () => ({
    globalSettings: [],
    currentGlobalSetting: null,

  }),
  getters: {
    getAllGlobalSettings: (state) => state.globalSettings,
    getCurrentGlobalSetting: (state) => state.currentGlobalSetting,

  },
  actions: {
    async fetchAllGlobalSettings() {
      try {
        this.globalSettings = await getItemsNoUser(
          `${import.meta.env.VITE_API_ENDPOINT}/global-settings`
        );
      } catch (error) {
        console.error("Error fetching global settings:", error);
        throw error;
      }
    },
    async fetchGlobalSetting(name) {
      try {
        this.currentGlobalSetting = await getItemsNoUser(
          `${import.meta.env.VITE_API_ENDPOINT}/global-settings/${name}`
        );
      } catch (error) {
        console.error("Error fetching global setting:", error);
        throw error;
      }
    },
    async updateGlobalSetting(name, updatedSetting) {
      try {
        this.currentGlobalSetting = await patchItem(
          `${import.meta.env.VITE_API_ENDPOINT}/global-settings/${name}`,
          updatedSetting
        );
      } catch (error) {
        console.error("Error updating global setting:", error);
        throw error;
      }
    },
    setIsLimitEnabled(value) {
      this.isLimitEnabled = value;
    },
  },
});