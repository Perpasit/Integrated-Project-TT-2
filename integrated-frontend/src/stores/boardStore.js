import { defineStore } from "pinia"
import { getItems, postItem, putItem, deleteItem } from "../utils/fetch"

export const useBoardStore = defineStore("board", {
  state: () => ({
    boards: [],
  }),

  getters: {
    getBoards: (state) => state.boards,
    getBoardById: (state) => (boardId) => state.boards.find((board) => board.id === boardId)
  },

  actions: {
    async fetchAllBoards() {
      try {
        const data = await getItems(
          `${import.meta.env.VITE_API_ENDPOINTV3}/boards`
        )
        
        this.boards = data
      } catch (error) {
        console.error("Error fetching boards:", error)
        // Optionally handle the error more comprehensively here
      }
    },

    async addBoard(newBoard) {
      try {
        const formattedBoard = {
          name: newBoard.trim(),
        };
    
        // Perform the POST request
        const createdBoard = await postItem(
          `${import.meta.env.VITE_API_ENDPOINTV3}/boards`,
          formattedBoard
        );
        this.boards.push(createdBoard);
        
      } catch (error) {
        console.error("Error adding board:", error);
      }
    },
    



    async updateBoard(id, updatedData) {
      try {
        const response = await fetch(`/api/boards/${id}`, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(updatedData),
        })
        if (!response.ok) {
          throw new Error("Network response was not ok")
        }
        const updatedBoard = await response.json()
        const index = this.boards.findIndex((board) => board.id === id)
        if (index !== -1) {
          this.boards[index] = updatedBoard
        }
      } catch (error) {
        console.error("Error updating board:", error)
      }
    },




    
    async deleteBoard(id) {
      try {
        const response = await deleteItem(`${import.meta.env.VITE_API_ENDPOINTV3}/boards/${id}`)
        this.boards = this.boards.filter((board) => board.id !== id)
        return response
      }catch (error) {
        throw error;
      }

    },
  },
})
