<template>
  <div>
    <table id="workingTable" style="margin-top: 20px; font-size: 13px" class="table table-bordered">
      <colgroup>
        <col width="12%"/>
        <col width="18%"/>
        <col width="18%"/>
        <col width="11%"/>
        <col width="12%"/>
        <col width="13%"/>
        <col/>
      </colgroup>
      <thead>
      <tr>
        <th>근무일자</th>
        <th>출근시각</th>
        <th>퇴근시각</th>
        <th>근무시간 (분)</th>
        <th>일일근무시간 (분)</th>
        <th>근무시간<br/>-일일근무시간 (분)</th>
        <th>비고</th>
      </tr>
      </thead>
      <tbody>
      <template v-if="!workingInfos">
        <tr>
          <td colspan="7" style="text-align: center"><b-spinner v-show="true"></b-spinner></td>
        </tr>
      </template>
      <template v-else>
        <template v-if="workingInfos.length > 0">
          <tr v-for="(item, index) in workingInfos" :key="index">
            <template
              v-if="!!toDayWorking && totalWorkingData.realTimeCalc && $moment().format('YYYYMMDD') === item.carDate">
              <td>{{ $moment(String(toDayWorking.carDate)).format('YYYY.MM.DD') }}</td>
              <td style="text-align: left">{{ toDayWorking.startDate }}</td>
              <td style="text-align: left">{{ toDayWorking.endDate }}</td>
              <td>{{ toDayWorking.diffTime }}</td>
              <td>{{ toDayWorking.originTime }}</td>
              <td>{{ toDayWorking.minusTime }}</td>
              <td>{{ toDayWorking.note }}</td>
            </template>
            <template v-else>
              <td>{{ $moment(String(item.carDate)).format('YYYY.MM.DD') }}</td>
              <td style="text-align: left">{{ item.startDate }}</td>
              <td style="text-align: left">{{ item.endDate }}</td>
              <td>{{ item.diffTime }}</td>
              <td>{{ item.originTime }}</td>
              <td>{{ item.minusTime }}</td>
              <td>{{ item.note }}</td>
            </template>

          </tr>
          <tr>
            <td colspan="7"></td>
          </tr>
          <tr>
            <td></td>
            <td></td>
            <td>합계</td>
            <td> {{ Number(totalWorkingData.totalWorkingTime) + toDayDiffMin }}</td>
            <td> {{ totalWorkingData.totalDiffTime }}</td>
            <td> {{
                (Number(totalWorkingData.totalWorkingTime) + toDayDiffMin - Number(totalWorkingData.totalDiffTime))
              }}
            </td>
            <td></td>
          </tr>
        </template>
        <template v-else>
          <tr>
            <td colspan="7">데이터가 없습니다.</td>
          </tr>
        </template>
      </template>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'WorkingTable',
  mounted () {
    console.log(this.workingInfos)
  },
  created () {
    setInterval(() => this.realTimeCalc(), 500)
  },
  data () {
    return {
      point: '',
      toDayStartTime: '',
      realTimeDiffTime: 0,
      realTimeMinusTime: 0,
      toDayDiffMin: 0
    }
  },
  methods: {
    realTimeCalc () {
      if (this.totalWorkingData.realTimeCalc) {
        const now = new Date()
        // 13시부터 증가
        let lunchTime
        if (now.getHours() < 13) {
          lunchTime = 0
        } else if (now.getHours() > 14) {
          lunchTime = 60
        } else {
          lunchTime = (now - new Date().setHours(13, 0, 0, 0)) / 1000 / 60
          if (lunchTime < 0) {
            lunchTime = 0
          } else if (lunchTime > 60) {
            lunchTime = 60
          }
        }

        const toDayDiffTime = (now - new Date(this.toDayWorking.startDate)) / 1000
        this.toDayDiffMin = Math.floor(toDayDiffTime / 60) - lunchTime
        const toDayDiffSec = Math.floor(toDayDiffTime % 60)

        this.toDayWorking.diffTime = this.toDayDiffMin.toString() + '.' + toDayDiffSec.toString()
        this.toDayWorking.minusTime = (Number(this.toDayDiffMin) - Number(this.toDayWorking.originTime)).toString()

        if (this.point.length === 4) {
          this.point = '.'
        } else {
          this.point += '.'
        }

        this.toDayWorking.endDate = '업무 중 ' + this.point
      }
    }
  },
  props: {
    workingInfos: {
      type: Array
    },
    totalWorkingData: {
      type: Object
    },
    toDayWorking: {
      type: Object
    }
  }
}
</script>

<style scoped>

</style>
