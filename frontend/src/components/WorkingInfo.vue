<template>
  <div style="width: 1100px; margin: 20px 0 100px 20px;">
    <a href="#" @click="logout" style="float:right">로그아웃</a>
    <h1 style="text-align: center;">근태 계산기 </h1>
    <div style="float:left; width: 30%; padding-bottom: 8px">
      <div class="spinner-border" id="loading" role="status" style="display: none">
      </div>
        <select id="year" class="form-select" style="width: 35%;display: inline;" v-model="selectedYear" @change="changeDate()">
          <option value="2022">2022</option>
          <option value="2023">2023</option>
          <option value="2024">2024</option>
        </select>
        <select class="form-select" style="width: 25%;display: inline;" v-model="selectedMonth"  @change="changeDate()">
          <option v-for="index in 12" :key="index" :value="index.toString().padStart(2, '0')">{{ index.toString().padStart(2, '0') }}</option>
        </select>

        <input type="hidden" id="workingMonth" name="workingMonth" >
    </div>
    <div style="float:right; padding-bottom: 8px">
<!--      <button class="btn btn-md btn-primary btn-block" id="calendarForm" onclick="changeForm()"> 캘린더 beta</button>-->
<!--      <button class="btn btn-md btn-primary btn-block" id="excelDownBtn" onclick="exportTableToCsv()">-->
<!--        엑셀다운-->
<!--      </button>-->
    </div>
    <router-view :workingInfos="workingInfos" :totalWorkingData="totalWorkingData" :toDayWorking="workingInfos[workingInfos.length - 1]" :key="$route.fullPath"></router-view>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'WorkingInfo',
  created () {
    const sessionId = localStorage.getItem('sessionId')
    const username = localStorage.getItem('username')
    if (!sessionId || !username) {
      this.$router.push('/login')
    } else {
      this.username = username
      this.sessionId = sessionId
      this.getWorkingData()
    }
    const now = new Date()
    this.selectedYear = now.getFullYear()
    this.selectedMonth = (now.getMonth() + 1).toString().padStart(2, '0')
  },
  data () {
    return {
      sessionId: '',
      username: '',
      loading: false,
      selectedYear: '',
      selectedMonth: '',
      workingInfos: [],
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
          this.workingInfos = response.data.data.workingInfos
          this.totalWorkingData = {
            realTimeCalc: response.data.data.realTimeCalc,
            totalDiffTime: response.data.data.totalDiffTime,
            totalMinusTime: response.data.data.totalMinusTime,
            totalWorkingTime: response.data.data.totalWorkingTime
          }
          if (response.data.data.realTimeCalc) {
          }
          if (this.$route.path === '/workingInfo') { this.$router.push({name: 'WorkingTable'}) }
        })
        .catch(e => {

        })
    }
  }
}
</script>

<style scoped>

</style>
