<template>
  <div class="container" style="height: 760px; min-width: 1440px; padding:0">
    <div class="top-banner">
      <div class="top-banner__text">
        <img src="../assets/images/ic-info.svg" alt="info" />
        참고용으로 사용하시기 바랍니다.<br />
        09:00 ~ 10:30 그룹웨어 서버 부하로 지연될 수 있으니 해당 시간 외에
        사용해주세요.
      </div>
      <div class="top-banner__button">
        <a class="btn-groupware" href="http://gwintra.lunasoft.co.kr/loginForm.do" target="_blank" alt="루나소프트 그룹웨어 가기">루나소프트 그룹웨어 가기</a>
        <a class="btn-groupware" href="http://gwintra.cellook.kr/loginForm.do" target="_blank" alt="그린앤그레이 그룹웨어 가기">그린앤그레이 그룹웨어 가기</a>
      </div>
    </div>
    <main>
      <div class="main__image">
        <img src="../assets/images/bg-sign.png" alt="login" />
      </div>
      <div class="main__form">
        <div class="main__sign-form">
          <h1>급여차감 방지기</h1>
          <label for="id">그룹웨어ID </label>
          <input
            type="text"
            v-model="username"
            id="id"
            name="id"
            class="sign-id check"
            placeholder="그룹웨어 ID(사번)를 입력해주세요."
            @keyup.enter="login"
          />
          <label for="password">그룹웨어PW</label>
          <input
            type="password"
            v-model="password"
            id="password"
            name="password"
            class="sign-password"
            placeholder="그룹웨어 Password를 입력해주세요."
            @keyup.enter="login"
          />
          <!-- 👇 버튼에 .active 클래스 붙으면 버튼 컬러 변경 -->
          <button class="btn-sign active" @click="login" :disabled="loginButtonIsDisabled">Log in</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import axios from 'axios'
import Swal from 'sweetalert2'

export default {
  name: 'LoginForm',
  created () {
  },
  data () {
    return {
      username: '',
      password: '',
      loginButtonIsDisabled: false
    }
  },
  methods: {
    loginValidator () {
      let flag = false
      let validMsg = ''
      if (!this.username) {
        flag = true
        validMsg = '계정을 입력해주세요'
      } else if (!this.password) {
        flag = true
        validMsg = '비밀번호를 입력해주세요'
      }
      if (flag) {
        Swal.fire({
          title: validMsg,
          imageUrl: '/static/fail.png',
          imageHeight: 360,
          imageWidth: 362,
          width: 525,
          height: 455,
          showCloseButton: false,
          confirmButtonText: '다시 시도하기',
          customClass: {
            confirmButton: 'swal2-button',
            popup: 'swal2-popup'
          },
          buttonsStyling: false
        })
      }
      return flag
    },
    login () {
      this.loginButtonIsDisabled = true
      if (this.loginValidator()) {
        this.loginButtonIsDisabled = false
        return
      }
      const formData = new FormData()
      formData.append('username', this.username)
      formData.append('password', this.password)

      axios
        .post('http://localhost/api/login', formData)
        .then(response => {
          if (response.data.responseCode === 0) {
            console.log(response)
            localStorage.setItem('sessionId', response.data.data.sessionId)
            localStorage.setItem('username', response.data.data.username)
            this.$router.push({name: 'WorkingInfo'})
          } else {
            Swal.fire({
              imageUrl: '/static/login-failed.png',
              imageHeight: 247,
              imageWidth: 362,
              width: 525,
              height: 455,
              showCloseButton: false,
              confirmButtonText: '다시 시도하기',
              customClass: {
                confirmButton: 'swal2-button',
                popup: 'swal2-popup'
              },
              buttonsStyling: false
            })
          }
        })
        .catch(e => {
          console.log(e)
        }).finally(() => {
          this.loginButtonIsDisabled = false
        })
    }
  }
}
</script>
<style scoped>
.container {
  background-color: white;
  padding-top: 0;
}
main input,button {
  font-size: small;
  line-height: initial;
  font-family: auto;
}
</style>
