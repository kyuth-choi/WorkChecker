import Vue from 'vue'
import Vuex from 'vuex'

import WorkingInfoStore from './modules/workingInfoStore'

Vue.use(Vuex)
export default new Vuex.Store({
  modules: {
    WorkingInfoStore
  }
})
