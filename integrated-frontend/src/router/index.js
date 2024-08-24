import { createRouter, createWebHistory } from "vue-router"
import Home from "../components/Home.vue"
import { useTaskStore } from "../stores/task"
import { useStatusStore } from "../stores/status"
import Error from "../components/Error.vue"
import Modal from "../components/Modal.vue"
import AddEditTask from "../components/AddEditTask.vue"
import ManageStatus from "../components/ManageStatus.vue"
import AddEditStatus from "../components/AddEditStatus.vue"
import ModalStatus from "../components/ModalStatus.vue"
import Login from "../components/Login.vue"
import TaskTable from "../components/TaskTable.vue"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      redirect: "/login",
    },
    {
      path: "/login",
      name: "Login", 
      component: Login,
    },
    {
      path: "/tasks",
      name: "TaskTable", 
      component: TaskTable,
    },
    {
      path: "/task",
      name: "home",
      component: Home,

      children: [
        {
          path: "/task/:taskId",
          name: "taskDetail",
          component: Modal,
        },
        {
          path: "/task/add",
          name: "AddTask",
          component: AddEditTask,
          props: () => ({
            mode: "add",
          }),
        },
        {
          path: "/task/:id/edit",
          name: "EditTask",
          component: AddEditTask,
          props: () => {
            return {
              mode: "edit"
            }
          },
        },
      ],
    },
    {
      path: "/status/manage",
      name: "ManageStatus",
      component: ManageStatus,
      children:[
        {
          path: "/status/manage/:statusId",
          name: "statusDetail",
          component: ModalStatus
        },
        {
          path: "/status/manage/add",
          name: "AddStatus",
          component: AddEditStatus,
          props: () => ({
            mode: "add",
          }),
        },
        {
          path: "/status/manage/:id/edit",
          name: "EditStatus",
          component: AddEditStatus,
          props: () => ({
            mode: "edit",
          }),
        },
      ]
    },

    {
      path: "/:pathMatch(.)",
      name: "NotFound",
      component: Error,
    },
  ],
})

export default router