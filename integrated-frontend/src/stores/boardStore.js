import { defineStore } from "pinia";
import { getItems, postItem, putItem, deleteItem } from "../utils/fetch";

export const useBoardStore = defineStore("board", {
  state: () => ({
    boards: [],
  }),

  getters: {
    getBoards: (state) => state.boards,
    getBoardById: (state) => (boardId) => state.boards.find((board) => board.id === boardId),
  },

  actions: {
    async fetchAllBoards() {
      try {
        const data = await getItems(`${import.meta.env.VITE_API_ENDPOINTV3}/boards`);
        this.boards = data;
      } catch (error) {
        // Remove router handling; just rethrow the error
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
        const response = await putItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${id}`, updatedData);
        const index = this.boards.findIndex((board) => board.id === id);
        if (index !== -1) {
          this.boards[index] = response;
        }
      } catch (error) {
        throw error;
      }
    },

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
