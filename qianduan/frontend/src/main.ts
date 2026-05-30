/**
 * 前端应用入口模块。
 *
 * 该文件负责创建 Vue 应用实例，注册 Vue Router 和 Element Plus，
 * 并把根组件挂载到 index.html 中的 #app 节点。
 */
import './assets/main.css'
import 'element-plus/dist/index.css'

import ElementPlus from 'element-plus'
import { createApp } from 'vue'

import App from './App.vue'
import router from './router'

<<<<<<< HEAD
// 创建 Vue 应用，注册全局插件，再挂载根组件。
=======
>>>>>>> c12f37e6a94c8b1bb7c85f65a53ba557c69edd43
createApp(App).use(router).use(ElementPlus).mount('#app')
