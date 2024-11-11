import { createRouter, createWebHistory } from "vue-router"
import Home from "../components/Home.vue"
import { useUserStore } from "../stores/userStore"
import Error from "../components/Error.vue"
import Modal from "../components/Modal.vue"
import AddEditTask from "../components/AddEditTask.vue"
import ManageStatus from "../components/ManageStatus.vue"
import AddEditStatus from "../components/AddEditStatus.vue"
import ModalStatus from "../components/ModalStatus.vue"
import Login from "../components/Login.vue"
import TaskTable from "../components/TaskTable.vue"
import Board from "../components/Board.vue"
import AddEditBoard from "../components/AddEditBoard.vue"
import AccessDenied from "../components/AccessDenied.vue"
import CollaboratorManagement from "../components/CollaboratorManagement.vue"
import AddCollaborator from "../components/AddCollaborator.vue"
import Invitation from "../components/Invitation.vue"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      redirect: "/login"
    },
    {
      path: "/login",
      name: "Login",
      component: Login
    },

    {
      path: "/board",
      name: "Board",
      component: Board,

      children: [
        {
          path: "/board/add",
          name: "AddBoard",
          component: AddEditBoard,
          props: () => ({
            mode: "add"
          })
        },
        {
          path: "/board/:boardId/edit",
          name: "EditBoard",
          component: AddEditBoard,
          props: () => ({
            mode: "edit"
          })
        }
      ]
    },
    {
      path: "/board/:boardId/collab",
      name: "CollabManage",
      component: CollaboratorManagement,

      children: [
        {
          path: "/board/:boardId/collab/add",
          name: "AddCollabManage",
          component: AddCollaborator
        },
 

      ]
    },
    {
      path: "/board/:boardId/collab/invitations",
      name: "Invitation",
      component: Invitation
    },
    {
      path: "/board/:boardId/task",
      name: "home",
      component: Home,

      children: [
        {
          path: "/board/:boardId/task/:taskId",
          name: "taskDetail",
          component: Modal
        },
        {
          path: "/board/:boardId/task/add",
          name: "AddTask",
          component: AddEditTask,
          props: () => ({
            mode: "add"
          })
        },
        {
          path: "/board/:boardId/task/:id/edit",
          name: "EditTask",
          component: AddEditTask,
          props: () => ({
            mode: "edit"
          })
        }
      ]
    },
    {
      path: "/board/:boardId/status",
      name: "ManageStatus",
      component: ManageStatus,
      children: [
        {
          path: "/board/:boardId/status/:statusId",
          name: "statusDetail",
          component: ModalStatus
        },
        {
          path: "/board/:boardId/status/add",
          name: "AddStatus",
          component: AddEditStatus,
          props: () => ({
            mode: "add"
          })
        },
        {
          path: "/board/:boardId/status/:id/edit",
          name: "EditStatus",
          component: AddEditStatus,
          props: () => ({
            mode: "edit"
          })
        }
      ]
    },

    {
      path: "/access-denied",
      name: "AccessDenied",
      component: AccessDenied
    },

    {
      path: "/:pathMatch(.)",
      name: "NotFound",
      component: Error
    }
  ]
})

export default router
