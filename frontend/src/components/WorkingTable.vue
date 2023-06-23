<template>
  <div>
    <table id="workingTable" style="margin-top: 20px; font-size: 13px" class="table table-bordered">
      <colgroup>
        <col width="10%"/>
        <col width="18%"/>
        <col width="18%"/>
        <col width="12%"/>
        <col width="12%"/>
        <col width="14%"/>
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
      <template v-if="!workingData.workingInfos">
        <tr>
          <td colspan="7" style="text-align: center">
            <b-spinner v-show="true"></b-spinner>
          </td>
        </tr>
      </template>
      <template v-else>
        <template v-if="workingData.workingInfos.length > 0">
          <tr v-for="(item, index) in workingData.workingInfos" :key="index">
            <td>{{ $moment(String(item.carDate)).format('YYYY.MM.DD') }}</td>
            <td>{{ item.startDate }}</td>
            <td>{{ item.endDate }}</td>
            <td>{{ item.diffTime }}</td>
            <td>{{ item.originTime }}</td>
            <td>{{ item.minusTime }}</td>
            <td>{{ item.note }}</td>
          </tr>
          <tr>
            <td colspan="7" style="height: 0px"></td>
          </tr>
          <tr>
            <td></td>
            <td></td>
            <td>합계 </td>
            <td> {{ Number(workingData.totalWorkingInfo.totalWorkingTime) + toDayDiffMin }}</td>
            <td> {{ workingData.totalWorkingInfo.totalDiffTime }}</td>
            <td>
              {{
                (Number(workingData.totalWorkingInfo.totalWorkingTime) + toDayDiffMin - Number(workingData.totalWorkingInfo.totalDiffTime))
              }}
              <br/><span :style="[workingData.totalWorkingInfo.totalMinusTime < 0 ? {color:'red'} : {}]">(전일기준 : {{ workingData.totalWorkingInfo.totalMinusTime > 0 ? '+' : '' }}{{
                workingData.totalWorkingInfo.totalMinusTime
              }})</span>
            </td>
            <td></td>
          </tr>
        </template>
        <template v-else>
          <tr>
            <td colspan="7" style="text-align: center">데이터가 없습니다.</td>
          </tr>
        </template>
      </template>
      </tbody>
    </table>
  </div>
</template>

<script>
import {mapActions, mapGetters} from 'vuex'
export default {
  name: 'WorkingTable',
  computed: {
    ...mapGetters({
      workingData: 'WorkingInfoStore/getWorkingData'
    })
  },
  created () {
    this.interval = setInterval(() => this.realTimeCalc(), 500)
  },
  data () {
    return {
      interval: {},
      point: '',
      toDayStartTime: '',
      realTimeDiffTime: 0,
      realTimeMinusTime: 0,
      toDayDiffMin: 0
    }
  },
  methods: {
    ...mapActions(
      'WorkingInfoStore', {
        leaveWork: 'leaveWork'
      }
    ),
    realTimeCalc () {
      if (this.workingData && this.workingData.totalWorkingInfo.realTimeCalc) {
        const now = new Date()
        // 13시부터 증가
        let lunchTime

        if (new Date(this.workingData.workingInfos[this.workingData.workingInfos.length - 1].startDate).getHours() < 14) {
          if (now.getHours() < 13) {
            lunchTime = 0
          } else if (now.getHours() > 14) {
            lunchTime = 60
          } else {
            lunchTime = Math.floor((now - new Date().setHours(13, 30, 0, 0)) / 1000 / 60)
            if (lunchTime < 0) {
              lunchTime = 0
            } else if (lunchTime > 60) {
              lunchTime = 60
            }
          }
        }

        const toDayDiffTime = (now - new Date(this.workingData.workingInfos[this.workingData.workingInfos.length - 1].startDate)) / 1000
        this.toDayDiffMin = Math.floor(toDayDiffTime / 60) - lunchTime
        const toDayDiffSec = Math.floor(toDayDiffTime % 60)

        this.workingData.workingInfos[this.workingData.workingInfos.length - 1].diffTime = this.toDayDiffMin.toString() + '.' + toDayDiffSec.toString()
        this.workingData.workingInfos[this.workingData.workingInfos.length - 1].minusTime = (Number(this.toDayDiffMin) - Number(this.workingData.workingInfos[this.workingData.workingInfos.length - 1].originTime)).toString()

        if (this.point.length === 4) {
          this.point = '.'
        } else {
          this.point += '.'
        }

        this.workingData.workingInfos[this.workingData.workingInfos.length - 1].endDate = '업무 중 ' + this.point
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
#workingTable td {
  text-align: left;
}
</style>
