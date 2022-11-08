import { WORKINFO } from '../mutation-types'
import workingInfoApi from '../../api/workingInfoApi'

const state = {
  workingData: {
    workingInfos: [],
    totalWorkingInfo: {
      realTimeCalc: false,
      totalDiffTime: 0,
      totalMinusTime: 0,
      totalWorkingTime: 0
    }
  }
}

const getters = {
  getWorkingData: (state) => {
    return state.workingData
  }
}

const actions = {
  async retrieveWorkingData ({commit}, data) {
    workingInfoApi.retrieveWorkingData(data.workingMonth, data.username, data.sessionId).then(res => {
      commit(WORKINFO.SET_WORKING_DATA, res)
    })
  },
  async leaveWork ({commit}) {
    commit(WORKINFO.SET_WORKING_END, false)
  }
}

const mutations = {
  [WORKINFO.SET_WORKING_DATA] (state, payload) {
    console.log(payload)
    state.workingData = payload
  },
  [WORKINFO.SET_WORKING_END] (state, payload) {
    state.workingData.totalWorkingInfo.realTimeCalc = payload
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
