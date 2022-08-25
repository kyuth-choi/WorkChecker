import Vue from 'vue'
import Router from 'vue-router'
// import swal from 'sweetalert2'

Vue.use(Router)

const router = new Router({
  mode: 'history',
  routes: [
    {
      path: '/',
      name: 'Root',
      redirect: '/login'
    },
    {
      path: '/workingInfo',
      name: 'WorkingInfo',
      redirect: '/workingInfo/type1',
      component: () => import('@/components/WorkingInfo.vue'),
      children: [
        {
          path: 'type1',
          name: 'WorkingTable',
          component: () => import('@/components/WorkingTable.vue')
        },
        {
          path: 'type2',
          name: 'WorkingCalendar',
          component: () => import('@/components/WorkingTable.vue')
        }
      ]
    },
    {
      path: '/login',
      name: 'LoginForm',
      component: () => import('@/components/LoginForm')
    },
    {
      name: 'notFound',
      path: '/404',
      component: () => import('@/components/NotFoundComponent.vue'),
      meta: {requiresAuth: false}
    },
    {
      path: '*',
      redirect: '/404'
    }

  ]
})
// const swalConfirm = swal.mixin({
//   icon: 'error',
//   title: '로그인정보 확인에 실패하였습니다. </br> 재접속 해주시기 바랍니다.'
// })
router.beforeEach((to, from, next) => {
  if (to.path === '/login') {
    next()
  } else {
    const sessionId = localStorage.getItem('sessionId')
    const username = localStorage.getItem('username')
    if (!!sessionId && !!username) {
      return next()
    } else {
      localStorage.clear()
      next('/login')
      // swalConfirm.fire({
      //   didClose () {
      //     next('/login')
      //   }
      // })
    }
  }
})

export default router
