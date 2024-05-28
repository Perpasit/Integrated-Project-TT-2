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

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      redirect: "/task",
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
            task: {
              name: "",
              description: "",
              colorCode: "#000000",
            },
            mode: "add",
            show: true,
          }),
        },
        {
          path: "/status/manage/:id/edit",
          name: "EditStatus",
          component: AddEditStatus,
          props: (route) => {
            const statusStore = useStatusStore();
            return {
              status: statusStore.getStatusById(route.params.id),
              mode: "edit",
              show: true,
            };
          },
        },
      ]
    },
    {
      path: "/:pathMatch(.*)*",
      name: "NotFound",
      component: Error,
    },
  ],
})

export default router