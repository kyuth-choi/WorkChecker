<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>
<style>
@import '/assets/css/styles.css';
#app {
  /*height: 100%;*/
  color: #2c3e50;
}
main {
  font-size: small;
  line-height: initial;
}
.swal2-button {
  width: 264px;
  height: 58px;
  margin-top: 34px;
  background-color: #111626;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  line-height: 20px;
  color: #fff;
}
.swal2-popup {
  border-radius: 36px !important;
}
</style>
