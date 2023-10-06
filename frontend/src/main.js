// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import VModal from 'vue-js-modal'
import VMoment from 'vue-moment'
import BootstrapVue from 'bootstrap-vue'

import store from './vuex/index'

/* eslint-disable no-new */
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.config.productionTip = false
Vue.use(VMoment)
Vue.use(BootstrapVue)
Vue.use(VModal, { dynamic: true, injectModalsContainer: true, dialog: true })

new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>'
})
