import Vue from 'vue'
import Router from 'vue-router'
import swal from 'sweetalert2'

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
      component: () => import('@/components/WorkingInfo.vue'),
      // redirect: '/workingInfo/type1',
      children: [
        {
          path: '/workingInfo/type1',
          name: 'WorkingTable',
          component: () => import('@/components/WorkingTable.vue')
        },
        {
          path: '/workingInfo/type2',
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
const swalConfirm = swal.mixin({
  icon: 'error',
  title: '로그인정보 확인에 실패하였습니다. </br> 재접속 해주시기 바랍니다.'
})
router.beforeEach((to, from, next) => {
  const sessionId = localStorage.getItem('sessionId')
  const username = localStorage.getItem('username')
  if (to.path === '/login') {
    if (!!sessionId && !!username) {
      next('/workingInfo')
    } else {
      next()
    }
  } else {
    if (!!sessionId && !!username) {
      return next()
    } else {
      swalConfirm.fire({
        didClose () {
          next('/login')
        }
      })
    }
  }
})

export default router
