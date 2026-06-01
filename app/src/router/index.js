import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// route.meta 约定(2026-05-10 M5 Wave 1.4):
//   layout: 'app' | 'auth' | 'fullscreen'   默认 'app'(AppLayout = AppHeader + main + AppFooter PC + AppTabBar H5)
//                                            'auth' 套 AuthLayout(Wave 5 G 填充内容)
//                                            'fullscreen' 套 FullscreenLayout(无 Header / Footer / TabBar)
//   hideHeaderH5: bool                       默认 false。true 表示 H5 视图隐藏 AppHeader(让位给页面 topbar);PC 仍显示
//   hideHeader: bool                         默认 false。true 表示无论视口都隐藏 AppHeader
//   hideSupportChatH5: bool                  默认 false。true 表示 H5 视图隐藏客服浮窗(避免遮挡关键 CTA)
//   public: bool                             已存在,免登录页面
//   guestOnly: bool                          已存在,已登录时跳首页

const routes = [
  // 公开页面
  { path: '/', name: 'Home', component: () => import('@/views/home/HomeView.vue'), meta: { public: true } },
  { path: '/curriculum', name: 'Curriculum', component: () => import('@/views/curriculum/CurriculumView.vue'), meta: { public: true } },
  { path: '/level-check', name: 'LevelCheck', component: () => import('@/views/levelCheck/QuestionsView.vue'), meta: { public: true } },
  { path: '/level-check/result/:submissionId', name: 'LevelCheckResult', component: () => import('@/views/levelCheck/ResultView.vue'), props: true, meta: { public: true } },
  { path: '/teachers', name: 'TeacherList', component: () => import('@/views/teacher/TeacherListView.vue'), meta: { public: true } },

  // D27:教材 / 资源中心 占位页(内容就位后替换 component;route + nav 入口先到位)
  { path: '/textbook', name: 'Textbook', component: () => import('@/views/marketing/ComingSoonView.vue'), meta: { public: true, pageKey: 'textbook' } },
  { path: '/resources', name: 'Resources', component: () => import('@/views/marketing/ComingSoonView.vue'), meta: { public: true, pageKey: 'resources' } },

  // 法务页(GCP OAuth consent screen 必填)
  { path: '/legal/privacy', name: 'LegalPrivacy', component: () => import('@/views/legal/PrivacyView.vue'), meta: { public: true } },
  { path: '/legal/terms', name: 'LegalTerms', component: () => import('@/views/legal/TermsView.vue'), meta: { public: true } },

  // guestOnly:已登录跳首页(/teacher/register 必须在 /teacher/:userId 之前定义,避免被静态-vs-动态 路由顺序坑)
  { path: '/login', name: 'Login', component: () => import('@/views/auth/LoginView.vue'), meta: { public: true, guestOnly: true, layout: 'auth' } },
  { path: '/register', name: 'Register', component: () => import('@/views/auth/RegisterView.vue'), meta: { public: true, guestOnly: true, layout: 'auth' } },
  { path: '/teacher/register', name: 'TeacherRegister', component: () => import('@/views/auth/TeacherRegisterView.vue'), meta: { public: true, guestOnly: true, layout: 'auth' } },
  { path: '/reset-password', name: 'ResetPassword', component: () => import('@/views/auth/ResetPasswordView.vue'), meta: { public: true, guestOnly: true, layout: 'auth' } },
  // OAuth 回跳:layout=fullscreen + hideHeader,避免登录瞬间闪现导航栏(plan review 修补 #4)
  { path: '/auth/callback/:provider', name: 'SocialCallback', component: () => import('@/views/auth/SocialCallbackView.vue'), props: true, meta: { public: true, layout: 'fullscreen', hideHeader: true } },

  // 教师详情(路径冲突 — 必须放在 /teacher/register 之后)
  // hideHeaderH5:H5 上深层页让位 topbar,PC 仍显示 AppHeader
  { path: '/teacher/:userId', name: 'TeacherDetail', component: () => import('@/views/teacher/TeacherDetailView.vue'), props: true, meta: { public: true, hideHeaderH5: true } },

  // 必登录
  { path: '/booking/success/:id', name: 'BookingSuccess', component: () => import('@/views/booking/BookingSuccessView.vue'), props: true, meta: { hideHeaderH5: true } },
  // 课堂:整屏 iframe,无任何全局壳
  { path: '/classroom/:orderId', name: 'Classroom', component: () => import('@/views/classroom/ClassroomView.vue'), props: true, meta: { layout: 'fullscreen' } },
  // /profile — 壳路由 + children(2026-05-10 M5 Wave 2 Subagent C 第 4 轮设计稿)
  // - 空 path child 保留 name 'Profile' — AppLayout / AppTabBar 已用此名做 active 高亮判断
  // - PC 视图:ProfileLayout 渲染 ProfileSidebar(左)+ <RouterView>(右,空 path 时 inline AccountView)
  // - H5 视图:空 path 渲染 ProfileMenuMobile;子路径渲染对应 view
  // - teacher-stats 仅 role=teacher 可入,beforeEnter 兜底跳 ProfileAccount
  // - D20 嵌套:packages / orders / refunds 套进 /profile 子页,旧路由 redirect 兼容书签 + 邮件
  {
    path: '/profile',
    component: () => import('@/views/profile/ProfileLayout.vue'),
    meta: { hideHeaderH5: true },
    children: [
      { path: '', name: 'Profile', component: () => import('@/views/profile/AccountView.vue') },
      { path: 'account', name: 'ProfileAccount', component: () => import('@/views/profile/AccountView.vue') },
      { path: 'referral', name: 'ProfileReferral', component: () => import('@/views/profile/ReferralView.vue') },
      { path: 'packages', name: 'MyPackages', component: () => import('@/views/package/MyPackagesView.vue') },
      { path: 'orders', name: 'MyOrders', component: () => import('@/views/booking/MyOrdersView.vue') },
      { path: 'refunds', name: 'MyRefunds', component: () => import('@/views/profile/RefundListView.vue') },
      {
        path: 'teacher-stats',
        name: 'ProfileTeacherStats',
        component: () => import('@/views/profile/TeacherStatsView.vue'),
        meta: { teacherOnly: true },
        beforeEnter: (to, from, next) => {
          const u = useUserStore()
          if (u.profile?.role === 'teacher') return next()
          return next({ name: 'ProfileAccount' })
        }
      }
    ]
  },
  { path: '/review/:orderId', name: 'ReviewForm', component: () => import('@/views/review/ReviewFormView.vue'), props: true, meta: { hideHeaderH5: true } },

  // D20 兼容 redirect:旧 /orders / /my/packages / /my/refunds → /profile/* 嵌套
  // 保持 deep link / 邮件链接 / 浏览器书签不打断
  { path: '/orders', redirect: { name: 'MyOrders' } },
  { path: '/my/packages', redirect: { name: 'MyPackages' } },
  { path: '/my/refunds', redirect: { name: 'MyRefunds' } },
  { path: '/packages', name: 'PackageList', component: () => import('@/views/package/PackageListView.vue'), meta: { public: true, hideSupportChatH5: true } },
  // Stripe Hosted Checkout 跳回页 — 必须 public(token 可能已过期),否则用户跳 /login 丢失支付提示上下文
  { path: '/payment/success', name: 'PaySuccess', component: () => import('@/views/payment/PaySuccessView.vue'), meta: { public: true } },
  { path: '/payment/cancel', name: 'PayCancel', component: () => import('@/views/payment/PayCancelView.vue'), meta: { public: true } },

  // dev stub
  { path: '/classroom-stub', name: 'ClassroomStub', component: () => import('@/views/classroom/ClassroomStubView.vue'), meta: { public: true, layout: 'fullscreen' } },

  // ===== 教师中心(M6 §3 / §4)=====
  // meta.teacherOnly + 套 TeacherCenterLayout(壳路由,内部按 children 渲染)
  // - 全局守卫 beforeEach 已升级:role !== 'teacher' → 重定向 /
  // - hideHeaderH5: 教师中心 H5 视图共用页内 topbar(TeacherCenterLayout 提供),让位顶栏
  // - WithdrawalApplyView 单独 hideHeaderH5(已通过父 meta 继承)
  {
    path: '/teacher-center',
    component: () => import('@/views/teacher-center/TeacherCenterLayout.vue'),
    meta: { teacherOnly: true, hideHeaderH5: true },
    children: [
      { path: '', name: 'TeacherCenter', component: () => import('@/views/teacher-center/DashboardView.vue') },
      // D19 Phase B / B9:档案 + 资质入口接入(B6/B7 占位 stub,正式实现见对应 task)
      { path: 'profile-edit', name: 'TeacherCenterProfileEdit', component: () => import('@/views/teacher-center/ProfileEditView.vue') },
      { path: 'qualification', name: 'TeacherCenterQualification', component: () => import('@/views/teacher-center/QualificationView.vue') },
      { path: 'schedule', name: 'TeacherCenterSchedule', component: () => import('@/views/teacher-center/ScheduleView.vue') },
      { path: 'orders', name: 'TeacherCenterOrders', component: () => import('@/views/teacher-center/OrdersView.vue') },
      { path: 'income', name: 'TeacherCenterIncome', component: () => import('@/views/teacher-center/IncomeView.vue') },
      { path: 'withdrawal', name: 'TeacherCenterWithdrawal', component: () => import('@/views/teacher-center/WithdrawalView.vue') },
      { path: 'withdrawal/apply', name: 'TeacherCenterWithdrawalApply', component: () => import('@/views/teacher-center/WithdrawalApplyView.vue') }
    ]
  }
]

// scrollBehavior(2026-05-10 M5 Wave 1.5.2,plan review 修补 A2):
//   - 锚点跳转 top: 100px(AppHeader 64 + 进度条 4 + 呼吸 32),Wave 5 Subagent I Legal 页 IntersectionObserver 必需
//   - 浏览器前进/后退恢复 savedPosition
//   - 默认顶部
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (to.hash) return { el: to.hash, top: 100, behavior: 'smooth' }
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 必登录页面 — 未登录跳 /login
  if (!to.meta.public && !userStore.isLoggedIn) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  // guestOnly 页面 — 已登录跳首页
  if (to.meta.guestOnly && userStore.isLoggedIn) {
    return next({ path: '/' })
  }
  // teacherOnly 页面 — role !== 'teacher' 跳首页(冗余兜底:ProfileTeacherStats 自身 beforeEnter 保留,M6 教师中心壳路由通过本守卫拦截)
  if (to.meta.teacherOnly && userStore.profile?.role !== 'teacher') {
    return next({ path: '/' })
  }
  next()
})

export default router
