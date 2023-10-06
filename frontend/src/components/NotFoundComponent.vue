<template>
  <div class="not-found-page">
    <h1>404</h1>
    <h4>페이지를 찾을 수 없습니다.</h4>
    <p>관리자에게 문의하세요.</p>
  </div>
</template>

<script>
export default {
  name: 'NotFoundComponent',
  components: {
  },
  methods: {
    moveToMainPage () {
      this.$router.push({ name: 'dashboard' })
    },
    moveToPrevPage () {
      this.$router.go(-1)
    }
  }
}
</script>
