import { createRouter, createWebHistory } from "vue-router";
import Home from "../components/Home.vue";
// import Modal from "../components/Modal.vue";
import Error from "../components/Error.vue";

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
      component: Home
    },
    // {
    //   path: "/task/:id",
    //   redirect: "/task"
    // },
    {
      path: "/task/:pathMatch(.*)*",
      name: "error",
      component:Error ,
    //   redirect:"/task"
    },
    {
      path: "/:pathMatch(.*)*",
      name: "NotFound",
      component: Error
    }
  ]
});

export default router;