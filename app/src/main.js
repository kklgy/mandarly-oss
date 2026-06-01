import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createHead } from '@vueuse/head'
import 'element-plus/dist/index.css'
import './assets/styles/main.scss'
import './assets/styles/element-overrides.scss'

import App from './App.vue'
import router from './router'
import i18n from './i18n'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(i18n)
app.use(createHead())
app.mount('#app')
