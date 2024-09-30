import { defineStore } from "pinia";
import { useUserStore } from "../stores/userStore"

import { getItems, postItem, putItem, patchItem,deleteItem ,getItemsNoUser } from "../utils/fetch";

export const useBoardStore = defineStore("board", {
  state: () => ({
    boards: [],
  }),

  getters: {
    getBoards: (state) => state.boards,
    getBoardByBoardId: (state) => (boardId) => state.boards.find((board) => board.id === boardId),
    getBoardName: (state) => (boardId) => {
      const board = state.boards.find((board) => board.id === boardId);
      return board ? board.name : null;
    },
    getBoardOid: (state) => (boardId) => {
      const board = state.boards.find((board) => board.id === boardId);
      return board ? board.owner.oid : "boom"; // Get owner ID
    },
  },

  actions: {
    async fetchAllBoards() {
      try {
        const data = await getItems(`${import.meta.env.VITE_API_ENDPOINTV3}/boards`);
        this.boards = data;
      } catch (error) {
        throw error;
      }
    },
    async getBoardById(id) {
      const userStore = useUserStore(); // Get access to the userStore
    
      try {
        let data;
        if (userStore.getIsAuthenticated) {
          // If the user is authenticated, use the accessToken for the authenticated request
          data = await getItems(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${id}`);
        } else {
          // If the user is not authenticated, make a request without token
          data = await getItemsNoUser(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${id}`);
        }
    
        console.log(data);
        return data;
      } catch (error) {
        throw error;
      }
    },
    async addBoard(newBoard) {
      try {
        const formattedBoard = { name: newBoard.trim() };
        const createdBoard = await postItem(
          `${import.meta.env.VITE_API_ENDPOINTV3}/boards`,
          formattedBoard
        );
        this.boards.push(createdBoard);
      } catch (error) {
        throw error;
      }
    },

    async updateBoard(id, updatedData) {
      try {
        const response = await patchItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${id}`, updatedData);
        // Optionally, update the board in local state
        const index = this.boards.findIndex(board => board.id === id);
        if (index !== -1) {
          this.boards[index] = { ...this.boards[index], ...updatedData };
        }
        return response.data;
      } catch (error) {
        console.error("Error updating board:", error);
        throw error;
      }
    },
    // async updateBoard(id, updatedData) {
    //   try {
    //     const response = await patchItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${id}`, updatedData);
    //     const index = this.boards.findIndex((board) => board.id === id);
    //     if (index !== -1) {
    //       this.boards[index] = response;
    //     }
    //   } catch (error) {
    //     throw error;
    //   }
    // },

    async deleteBoard(id) {
      try {
        await deleteItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${id}`);
        this.boards = this.boards.filter((board) => board.id !== id);
      } catch (error) {
        throw error;
      }
    },
  },
});
