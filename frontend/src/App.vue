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
@import "/assets/css/styles.css";
#app {
  height: 100%;
  /*font-family: 'Avenir', Helvetica, Arial, sans-serif;*/
  /*-webkit-font-smoothing: antialiased;*/
  /*-moz-osx-font-smoothing: grayscale;*/
  /*text-align: center;*/
  color: #2c3e50;
  /*margin-top: 60px;*/
}
body{
  height: 98%;}
html{
  height: 100%;
}
</style>
