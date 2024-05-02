import { createRouter, createWebHistory } from "vue-router";
import Home from "../components/Home.vue";
// import Modal from "../components/Modal.vue";
import Error from "../components/Error.vue";
import Modal from "../components/Modal.vue";

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
      children:[{
        path: "/task/:taskId",
        name: "taskDetail",
        component: Modal
      },]
    }
    
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
});

export default router;