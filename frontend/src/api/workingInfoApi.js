import axios from 'axios'

export default {
  async retrieveWorkingData (workingMonth, username, sessionId) {
    return new Promise((resolve, reject) => {
      const formData = new FormData()
      formData.append('workingMonth', workingMonth)
      formData.append('username', username)
      formData.append('sessionId', sessionId)
      const workingData = {
        workingInfos: [],
        totalWorkingInfo: {}
      }
      axios
        .post('http://localhost/api/workList', formData)
        .then(response => {
          if (response.data.responseCode === 0 && response.data.data !== null) {
            workingData.workingInfos = response.data.data.workingInfos
            workingData.totalWorkingInfo = {
              realTimeCalc: response.data.data.realTimeCalc,
              totalDiffTime: response.data.data.totalDiffTime,
              totalMinusTime: response.data.data.totalMinusTime,
              totalWorkingTime: response.data.data.totalWorkingTime
            }
            resolve(workingData)
          } else {
            if (response.data.responseCode === -1) {
              localStorage.clear()
              this.$router.push('/login')
            }
          }
        })
        .catch(e => {
          console.log(e)
          reject(e)
        })
    })
    // return workingData
  }

}
