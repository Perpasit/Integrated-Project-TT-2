import { defineStore } from 'pinia'

export const useBoardStore = defineStore('board', {
  state: () => ({
    boards: []
  }),

  getters: {
    getBoards: (state) => state.boards
  },

  actions: {
    async fetchAllBoards() {
      try {
        const response = await getItems(
          `${import.meta.env.VITE_API_ENDPOINT2}/v3/boards}`
        )
        if (!response.ok) {
          throw new Error('Network response was not ok')
        }
        const data = await response.json()
        this.boards = data
      } catch (error) {
        // console.error('Error fetching boards:', error)
      }
    },

    async addBoard(boardData) {
      try {
        const response = await fetch('/v3/boards', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(boardData)
        })
        if (!response.ok) {
          throw new Error('Network response was not ok')
        }
        const newBoard = await response.json()
        this.boards.push(newBoard)
      } catch (error) {
        console.error('Error adding board:', error)
      }
    },

    async updateBoard(id, updatedData) {
      try {
        const response = await fetch(`/v3/boards/${id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(updatedData)
        })
        if (!response.ok) {
          throw new Error('Network response was not ok')
        }
        const updatedBoard = await response.json()
        const index = this.boards.findIndex(board => board.id === id)
        if (index !== -1) {
          this.boards[index] = updatedBoard
        }
      } catch (error) {
        console.error('Error updating board:', error)
      }
    },

    async deleteBoard(id) {
      try {
        const response = await fetch(`/v3/boards/${id}`, {
          method: 'DELETE'
        })
        if (!response.ok) {
          throw new Error('Network response was not ok')
        }
        this.boards = this.boards.filter(board => board.id !== id)
      } catch (error) {
        console.error('Error deleting board:', error)
      }
    },

    getBoardById(id) {
      return this.boards.find(board => board.id === id)
    }
  }
})