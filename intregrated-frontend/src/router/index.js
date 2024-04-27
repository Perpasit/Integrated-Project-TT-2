import App from '../App.vue'
import Modal from '../components/Modal.vue'
import {createRouter,createWebHistory} from 'vue-router'
import TaskTable from '../components/TaskTable.vue'


const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            redirect: '/task',
        },
        {
            path: '/task',
            name: 'home',
            component: App,
        },
        {
            path: '/task/:taskId',
            name: 'TaskDetailsPage',
            component: Modal,
            props: true,
        },
     
    ],
})
export default router