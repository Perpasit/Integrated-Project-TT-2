import { createRouter, createWebHistory } from "vue-router"
import Home from "../components/Home.vue"
import { useTaskStore } from "../stores/task";
// import Modal from "../components/Modal.vue";
import Error from "../components/Error.vue"
import Modal from "../components/Modal.vue"
import AddEditTask from "../components/AddEditTask.vue"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      redirect: "/task"
    },

    {
      path: "/task",
      name: "home",
      component: Home,
      children: [
        {
          path: "/task/:taskId",
          name: "taskDetail",
          component: Modal
        },
        {
          path: "/task/:id/add",
          name: "AddTask",
          component: AddEditTask,
          props: (route) => ({
            task: {
              id: route.params.id,
              title: "",
              description: "",
              assignees: "",
              status: "NO_STATUS",
            },
            mode: "add",
            show: true,
          }),
        },
        {
          path: "/task/:id/edit",
          name: "EditTask",
          component: AddEditTask,
          props: (route) => {
            const taskStore = useTaskStore();
            return {
              task: taskStore.getTaskById(route.params.id),
              mode: "edit",
              show: true,
            };
          },
        },
      ]
    },
    
   

    // ,
    // {
    //   path: "/task/:taskId",
    //   name: "taskDetail",
    //   component: Modal,
    //   props: true
    // }

    // {
    //   path: "/task/:pathMatch(.*)*",
    //   name: "error",
    //   component:Error ,
    // },
    // {
    //   path: "/:pathMatch(.*)*",
    //   name: "NotFound",
    //   component: Error
    // }
  ]
})

export default router
