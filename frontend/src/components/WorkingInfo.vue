<template>
  <div style="display:flex; justify-content: center">
    <div style="width: 1100px; margin: 20px 0 100px 20px; padding-bottom: 50px">
      <button class="btn btn-md btn-primary btn-block" @click="logout" style="float: right">로그아웃</button>
      <h1 style="text-align: center;">급여차감 방지기 </h1>
      <div style="float:left; width: 30%; padding-bottom: 8px">
        <b-spinner v-show="!showDateSelector"></b-spinner>
        <select id="year" class="form-select" style="width: 35%;display: inline;" v-model="selectedYear"
                v-show="showDateSelector"
                @change="changeDate()">
          <option value="2022">2022</option>
          <option value="2023">2023</option>
          <option value="2024">2024</option>
        </select>
        <select class="form-select" style="width: 25%;display: inline;" v-model="selectedMonth"
                v-show="showDateSelector" @change="changeDate()">
          <option v-for="index in 12" :key="index" :value="index.toString().padStart(2, '0')">
            {{ index.toString().padStart(2, '0') }}
          </option>
        </select>

        <input type="hidden" id="workingMonth" name="workingMonth">
      </div>
      <div style="float:right; padding-bottom: 8px">
      </div>
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'WorkingInfo',
  created () {
    const now = new Date()
    this.selectedYear = now.getFullYear()
    this.selectedMonth = (now.getMonth() + 1).toString().padStart(2, '0')

    const data = {
      username: localStorage.getItem('username'),
      sessionId: localStorage.getItem('sessionId'),
      workingMonth: now.getFullYear() + (now.getMonth() + 1).toString().padStart(2, '0')
    }

    this.retrieveWorkingData(data)
  },
  computed: {
    ...mapGetters({
      workingData: 'WorkingInfoStore/getWorkingData'
    })
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
    ...mapActions(
      'WorkingInfoStore', {
        retrieveWorkingData: 'retrieveWorkingData'
      }
    ),
    exportTableToCsv () {
      const filename = this.selectedYear + this.selectedMonth + '_' + this.username + '_working.csv'
      const BOM = '\uFEFF'

      const table = document.getElementById('workingTable')
      let csvString = BOM
      for (let rowCnt = 0; rowCnt < table.rows.length; rowCnt++) {
        const rowData = table.rows[rowCnt].cells
        if (rowData.length > 1) {
          for (let colCnt = 0; colCnt < rowData.length; colCnt++) {
            let columnData = rowData[colCnt].innerHTML
            if (columnData == null || columnData.length === 0) {
              columnData = ''.replace(/"/g, '""')
            } else {
              columnData = columnData.toString().replace(/"/g, '""').replace(/<[^>]*>?/g, '') // escape double quotes
            }
            csvString = csvString + '"' + columnData + '",'
          }
          csvString = csvString.substring(0, csvString.length - 1)
          csvString = csvString + '\r\n'
        }
      }
      csvString = csvString.substring(0, csvString.length - 1)

      // IE 10, 11, Edge Run
      if (window.navigator && window.navigator.msSaveOrOpenBlob) {
        const blob = new Blob([decodeURIComponent(csvString)], {
          type: 'text/csv;charset=utf8'
        })
        window.navigator.msSaveOrOpenBlob(blob, filename)
      } else if (window.Blob && window.URL) {
        // HTML5 Blob
        const blob = new Blob([csvString], {type: 'text/csv;charset=utf8'})
        const csvUrl = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.setAttribute('style', 'display:none')
        a.setAttribute('href', csvUrl)
        a.setAttribute('download', filename)
        document.body.appendChild(a)

        a.click()
        a.remove()
      } else {
        // Data URI
        const csvData = 'data:application/csv;charset=utf-8,' + encodeURIComponent(csvString)
        // const blob = new Blob([csvString], { type: 'text/csv;charset=utf8' });
        // const csvUrl = URL.createObjectURL(blob);
        const a = document.createElement('a')
        a.setAttribute('style', 'display:none')
        a.setAttribute('target', '_blank')
        a.setAttribute('href', csvData)
        a.setAttribute('download', filename)
        document.body.appendChild(a)
        a.click()
        a.remove()
      }
    },
    logout () {
      localStorage.clear()
      this.$router.push('/')
    },
    changeDate () {
      this.showDateSelector = false
      const data = {
        username: localStorage.getItem('username'),
        sessionId: localStorage.getItem('sessionId'),
        workingMonth: this.selectedYear + '' + this.selectedMonth
      }
      this.retrieveWorkingData(data).then(() => {
        this.showDateSelector = true
      })
    }
  }
}
</script>
