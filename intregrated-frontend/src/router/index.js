import App from '../App.vue'
import {createRouter,createWebHistory} from 'vue-router'


const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [{
        path: '/task',
        name: 'main',
        component: App
    }]
})

export default router