import { defineStore } from "pinia";
import { useUserStore } from "../stores/userStore"

import { getItems, postItem, putItem, patchItem,deleteItem ,getItemsNoUser } from "../utils/fetch";

export const useBoardStore = defineStore("board", {
  state: () => ({
    boards: {
      personalBoards: [],
      collabBoards: [],
    },
  }),

  getters: {
    getBoardsPersonal: (state) => state.boards.personalBoards,
    getBoardsCollab: (state) => state.boards.collabBoards,
    getBoardByBoardId: (state) => (boardId) => {
      const allBoards = [...state.boards.personalBoards, ...state.boards.collabBoards];
      return allBoards.find((board) => board.id === boardId);
    },
    getBoardName: (state) => (boardId) => {
      const allBoards = [...state.boards.personalBoards, ...state.boards.collabBoards];
      const board = allBoards.find((board) => board.id === boardId);
      return board ? board.name : null;
    },
    getBoardOid: (state) => (boardId) => {
      const allBoards = [...state.boards.personalBoards, ...state.boards.collabBoards];
      const board = allBoards.find((board) => board.id === boardId);
      return board ? board.owner.oid : null; // Return owner ID
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
        this.boards.personalBoards.push(createdBoard);
      } catch (error) {
        throw error;
      }
    },

    async updateBoard(boardId, updatedData) {
      try {
        const response = await patchItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${boardId}`, updatedData);
        
        const updateBoardList = (boardList) => {
          const index = boardList.findIndex((board) => board.id === boardId);
          if (index !== -1) {
            boardList[index] = { ...boardList[index], ...updatedData };
          }
        };

        // Update both personal and collaborative boards
        updateBoardList(this.boards.personalBoards);
        updateBoardList(this.boards.collabBoards);

        return response.data;
      } catch (error) {
        console.error("Error updating board:", error);
        throw error;
      }
    },

    async deleteBoard(id) {
      try {
        await deleteItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${id}`);
        // this.boards = this.boards.filter((board) => board.id !== id);
      } catch (error) {
        throw error;
      }
    },
  },
});
