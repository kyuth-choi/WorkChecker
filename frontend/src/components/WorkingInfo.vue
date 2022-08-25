<template>
  <div style="display:flex; justify-content: center">
    <div style="width: 1100px; margin: 20px 0 100px 20px; padding-bottom: 50px">
      <button class="btn btn-md btn-primary btn-block" @click="logout" style="float: right">로그아웃</button>
      <h1 style="text-align: center;">급여차감 방지기 </h1>
      <div style="float:left; width: 30%; padding-bottom: 8px">
        <b-spinner v-show="!showDateSelector"></b-spinner>
        <select id="year" class="form-select" style="width: 35%;display: inline;" v-model="selectedYear" v-show="showDateSelector"
                @change="changeDate()">
          <option value="2022">2022</option>
          <option value="2023">2023</option>
          <option value="2024">2024</option>
        </select>
        <select class="form-select" style="width: 25%;display: inline;" v-model="selectedMonth" v-show="showDateSelector" @change="changeDate()">
          <option v-for="index in 12" :key="index" :value="index.toString().padStart(2, '0')">
            {{ index.toString().padStart(2, '0') }}
          </option>
        </select>

        <input type="hidden" id="workingMonth" name="workingMonth">
      </div>
      <div style="float:right; padding-bottom: 8px">
      </div>
      <router-view :workingInfos="workingInfos" :totalWorkingData="totalWorkingData"
                   :toDayWorking="!workingInfos ? {} : workingInfos[workingInfos.length - 1]" :key="$route.fullPath"></router-view>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'WorkingInfo',
  created () {
    this.username = localStorage.getItem('username')
    this.sessionId = localStorage.getItem('sessionId')
    this.getWorkingData()
    const now = new Date()
    this.selectedYear = now.getFullYear()
    this.selectedMonth = (now.getMonth() + 1).toString().padStart(2, '0')
  },
  data () {
    return {
      showDateSelector: true,
      sessionId: '',
      username: '',
      loading: false,
      selectedYear: '',
      selectedMonth: '',
      workingInfos: undefined,
      totalWorkingData: {
        realTimeCalc: false,
        totalDiffTime: 0,
        totalMinusTime: 0,
        totalWorkingTime: 0
      }
    }
  },
  methods: {
    logout () {
      localStorage.clear()
      this.$router.push('/login')
    },
    changeDate () {
      this.showDateSelector = false
      this.getWorkingData()
    },
    getWorkingData () {
      const workingMonth = this.selectedYear + this.selectedMonth
      const formData = new FormData()
      formData.append('sessionId', this.sessionId)
      formData.append('username', this.username)
      formData.append('workingMonth', workingMonth)
      axios
        .post('http://localhost/api/workList', formData)
        .then(response => {
          this.showDateSelector = true
          this.workingInfos = response.data.data.workingInfos
          this.totalWorkingData = {
            realTimeCalc: response.data.data.realTimeCalc,
            totalDiffTime: response.data.data.totalDiffTime,
            totalMinusTime: response.data.data.totalMinusTime,
            totalWorkingTime: response.data.data.totalWorkingTime
          }
        })
        .catch(e => {
          console.log(e)
        })
    }
  }
}
</script>
