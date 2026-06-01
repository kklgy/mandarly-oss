import legalPrivacy from './legal-privacy/zh-CN.js'
import legalTerms from './legal-terms/zh-CN.js'

export default {
  legal: {
    ...legalPrivacy.legal,
    ...legalTerms.legal,
    toc: { title: '目录', close: '关闭' },
    print: '打印',
    progressLabel: '阅读进度',
    privacy: {
      ...legalPrivacy.legal.privacy,
      lastUpdatedLabel: '最后更新:{date}'
    },
    terms: {
      ...legalTerms.legal.terms,
      lastUpdatedLabel: '最后更新:{date}'
    }
  },
  app: {
    name: 'Mandarly',
    tagline: '和母语老师一对一学中文'
  },
  common: {
    next: '下一题',
    back: '上一题',
    submit: '提交',
    skip: '跳过',
    retry: '重试',
    loading: '加载中...',
    error: '出错了',
    cancel: '取消',
    confirm: '确认',
    delete: '删除',
    close: '关闭',
    networkError: {
      title: '加载失败',
      desc: '请检查网络后重试'
    },
    price: {
      free: '免费',
      loginToView: '登录后查看价格'
    }
  },
  home: {
    hero: {
      eyebrow: '在线 1 对 1 中文口语课',
      title: '面向儿童与成人的在线中文课',
      tagline: '与全球母语老师 1v1 学中文',
      subtitle: '跟友好的中文母语老师上实时一对一口语课,逐步建立普通话表达信心。',
      cta: {
        freeTrial: '预约免费试听',
        curriculum: '查看课程体系',
        browseTeachers: '浏览老师',
        levelCheck: '30 秒等级测试'
      },
      trust: '中文母语老师 | 一对一实时课堂 | 以口语表达为核心',
      video: {
        title: '看看 Mandarly 课堂如何进行',
        caption: '与中文母语老师实时进行一对一普通话口语课。',
        play: '播放有声版',
        soundOn: '声音已开启'
      }
    },
    feature: {
      title: '为什么选 Mandarly',
      native: { title: '母语老师 1v1', desc: '全球认证中文母语老师,1 对 1 专属定制课程' },
      levelCheck: { title: '零基础友好', desc: '30 秒等级测试,智能推荐合适老师和入门课程' },
      flexible: { title: '灵活预约', desc: '半年/一年套餐,按节奏自由预约,可取消可改期' },
      // D25 新增:第 4 张卡(对齐参考站 4 张图文交错)
      professional: { title: '专业认证师资', desc: '100% 持证上岗,严格筛选培训,丰富的国际中文教学经验' },
      // D25 新增:图文交错 CTA 按钮文案
      learnMore: '了解更多'
    },
    recommend: {
      title: '推荐老师',
      sub: '严选评分 4.8+ 的母语老师,新认证老师优先',
      viewAll: '查看全部',
      viewDetail: '查看详情',
      empty: '更多老师即将上线',
      prev: '上一组',
      next: '下一组'
    },
    package: {
      title: '选择适合你的学习节奏',
      sub: '免费体验 + 半年/一年套餐,注册后查看完整价格',
      viewAll: '查看完整套餐对比',
      recommended: '推荐',
      discount: '9 折省 {amount}',
      freeTrial: { name: '免费体验' },
      halfYear: { name: '半年包(每周 1 节)' },
      fullYear: { name: '一年包(每周 2 节)' }
    },
    curriculumPath: {
      eyebrow: '四阶段学习路径',
      title: '跟随你成长的中文课程体系',
      subtitle: '从零基础中文到高级普通话沟通,每个阶段都围绕口语自信逐步提升。',
      cta: '查看完整课程体系',
      stages: [
        { label: '入门', name: '零基础中文入门', desc: '从发音、拼音、声调和简单问候开始。' },
        { label: '基础', name: '中文基础提升', desc: '建立日常词汇、基础句型和简单对话能力。' },
        { label: '中级', name: '中级中文表达', desc: '通过主题、故事、文化和长对话提升流利度。' },
        { label: '高级', name: '高级中文沟通', desc: '发展深度讨论、演讲、写作辅助和 HSK 备考能力。' }
      ]
    },
    trustBar: {
      title: '受到全球 2,300+ 学员信任',
      items: {
        lesson: { title: '30 分钟一对一课程', desc: '专注、高效的真人口语练习' },
        native: { title: '中文母语老师', desc: '专业认证,经验丰富,耐心引导' },
        schedule: { title: '灵活预约时间', desc: '按照你的时区和节奏安排课程' },
        progress: { title: '学习进度追踪', desc: '个性化反馈和持续学习支持' }
      }
    },
    how: {
      title: 'Mandarly 如何开始',
      subtitle: '4 个简单步骤,开启你的普通话学习旅程',
      cta: '预约免费试听',
      steps: [
        { title: '免费水平测试', desc: '先完成轻量测试,了解当前中文水平。' },
        { title: '匹配合适老师', desc: '根据目标和基础,推荐适合你的老师。' },
        { title: '预约上课时间', desc: '选择最适合你时区和日程的课堂时间。' },
        { title: '开始学习进步', desc: '上个性化一对一课程,持续追踪进步。' }
      ]
    },
    faq: {
      title: '常见问题',
      q1: { q: '学费怎么收费?', a: '提供按周节奏的半年/一年套餐。注册或登录后可查看完整价格与币种,新用户可领取 1 节免费体验课。' },
      q2: { q: '老师都是中文母语吗?有什么资质?', a: '全部为中文母语老师(普通话/粤语),持有教师认证 + 中文教学经验 ≥ 1 年。所有老师由 Mandarly 团队人工审核(身份/资质/试讲)。' },
      q3: { q: '怎么上课?需要安装软件吗?', a: '浏览器直接上课,无需安装软件(基于腾讯云 LCIC)。支持 PC / 手机 / 平板,音视频高清。课前 30 分钟可进入课堂调试设备。' },
      q4: { q: '可以免费试听一节吗?', a: '是。注册即送 1 节免费体验课(25 分钟),自由选老师,无需绑定信用卡。' },
      q5: { q: '不满意可以退款吗?', a: '课前 24 小时取消可全额退款(扣次返还);开课不满意可申请退款,详见服务条款 §5 退款规则。' },
      q6: { q: '完全零基础也能上课吗?适合小孩吗?', a: '完全零基础可以,所有老师都有零基础学员经验;6-12 岁学员可在套餐中选择"少儿适用"标签的老师。' }
    },
    cta: {
      title: '还在犹豫?先免费测一下你的中文水平',
      sub: '30 秒,4 道题,智能推荐适合你的老师与课程',
      btn: '30 秒等级测试',
      teacherJoin: '成为 Mandarly 老师'
    },

    // D25 新增:数字统计带(<StatsBar />)— 静态 hardcode 占位,运营有真数据后切
    stats: {
      activeLearners: { value: '2,300+', label: '活跃学员' },
      professionalTutors: { value: '150+', label: '专业老师' },
      lessonsCompleted: { value: '50,000+', label: '已完成课程' },
      parentSatisfaction: { value: '98%', label: '家长满意度' }
    },

    // D25 新增:学员见证(<TestimonialCards />)— 示例文案,3 种语气对冲视觉(家长 / 中性 / 成人)
    testimonial: {
      title: '学员故事',
      subtitle: '来自全球各地的学员分享他们的学习体验',
      items: [
        { name: 'Emma', country: '美国 · 学习 6 个月', quote: '我女儿现在每天主动要求上课,发音越来越标准。' },
        { name: 'Lucas', country: '英国 · 学习 1 年', quote: '老师很有耐心,我现在能用中文跟朋友聊天了。' },
        { name: 'Sofia', country: '法国 · 自学 8 个月', quote: '一对一教学真的很有效,进步速度超出预期。' }
      ]
    },

    // D25 新增:AppHeader logo 旁中性 tagline(对冲 §3.1 视觉偏儿童)
    appTagline: '面向所有年龄,所有水平'
  },
  s0: {
    kicker: '免费水平测试',
    title: '30 秒,找到适合你的老师',
    subtitle: '回答 4 个快速问题,我们为你匹配合适的老师和套餐。',
    flowLabel: '测试步骤',
    cardLabel: '水平测试题目',
    proof: {
      questions: { value: '4 题', label: '不做复杂测评' },
      login: { value: '免登录', label: '提交后再决定是否注册' },
      match: { value: '老师+套餐', label: '结果直接汇合主路径' }
    },
    flow: {
      level: '当前水平',
      goal: '学习目标',
      pace: '学习节奏',
      learner: '学习者'
    },
    progress: '第 {current} 题 / 共 {total} 题',
    optionalEmail: {
      label: '邮箱(选填,后续推送)',
      placeholder: '你的邮箱'
    },
    error: {
      loadFailed: '题目加载失败,请重试。',
      submitFailed: '提交失败,请重试。'
    },
    result: {
      subtitle: '根据你的答案,我们先给出一个轻量学习路径。你可以直接预约老师,也可以联系真人客服帮你确认。',
      subtitleGuest: '根据你的答案,我们先给出一个轻量学习路径。注册后可保存结果并继续下一步。',
      proof: {
        level: '推断水平',
        teachers: '推荐老师',
        plan: '推荐方案',
        planReady: '已匹配',
        planLater: '可咨询',
        nextStep: '下一步',
        nextReady: '可继续'
      },
      teachersEyebrow: '老师推荐',
      heading: {
        beginner: '零基础起步,正是开始的好时机',
        intermediate: '已经有基础,继续提升流利度',
        advanced: '已经流利,精进与专项突破'
      },
      level: {
        beginner: '入门',
        intermediate: '中级',
        advanced: '高级'
      },
      teachersTitle: '为你精选的 Top 3 老师',
      packageEyebrow: '学习节奏',
      packageTitle: '推荐套餐',
      packageDesc: '先按你的投入节奏推荐,购买前仍可对比完整套餐。',
      packageDefaultName: '为你推荐的方案',
      yearsExperience: '{n} 年教学',
      hasIntroVideo: '有自我介绍',
      bookNow: '立即预约',
      viewPackages: '查看完整套餐对比',
      viewMoreTeachers: '查看更多老师',
      registerToContinue: '注册后继续',
      contactWhatsapp: '联系 WhatsApp 客服',
      contactSupport: '联系真人客服',
      empty: '暂无匹配的老师,请联系客服。',
      freePackage: '免费体验',
      perWeek: '每周 {n} 节',
      perWeekPlural: '每周 {n} 节',
      total: '共 {n} 节',
      validity: '有效期 {days} 天'
    }
  },
  teachers: {
    title: '浏览我们的老师',
    subtitle: '挑一位与你目标契合的老师',
    intro: {
      eyebrow: '老师 marketplace',
      description: '不确定怎么选? 先免费体验 1 节,或 30 秒测水平。',
      descriptionNoTrial: '不确定怎么选? 30 秒测水平,我们推荐合适老师。',
      freeTrial: '免费体验课',
      levelCheck: '30 秒测水平'
    },
    filterAll: '全部',
    loadFailed: '老师列表加载失败,请重试。',
    yearsExperience: '{n} 年教学',
    introVideo: '介绍视频',
    bookNow: '立即预约',
    search: { placeholder: '搜索老师名 / 标签', clear: '清空' },
    filter: {
      title: '筛选',
      reset: '重置',
      apply: '应用筛选({count} 位)',
      applyEmpty: '无匹配老师',
      accent: { title: '口音', mandarin_cn: '普通话(大陆)', mandarin_tw: '台湾国语', cantonese: '粤语' },
      priceBuckets: { title: '价格区间', lt200: '< HK$200', '200_500': 'HK$200 - 500', '500_1000': 'HK$500 - 1000', gt1000: '> HK$1000' },
      available: '今日可约',
      minRating: '最低评分',
      expertise: { title: '教学方向' },
      tags: { title: '标签', beginner: '适合零基础', kids: '少儿适用', hasVideo: '有试讲视频' }
    },
    sort: { label: '排序', recommend: '推荐', rating: '评分高 → 低', priceAsc: '价格低 → 高', priceDesc: '价格高 → 低', reviewCount: '评价数多 → 少' },
    list: { summary: '共 {total} · 已加载 {loaded}', loadMore: '加载更多({remaining} 位)', loadingMore: '加载中...', allLoaded: '已全部加载' },
    empty: {
      noResult: {
        title: '没有找到匹配的老师',
        desc: '试试放宽筛选条件,或浏览所有老师',
        clearFilters: '清除筛选',
        browseAll: '浏览全部',
        levelCheck: '做水平测试'
      }
    },
    // D25: 列表头部 stats 一行
    stats: {
      teachers: '{n} 位教师',
      languages: '{n} 种语言',
      avgRating: '平均评分 {r}'
    }
  },
  teacher: {
    backToList: '返回老师列表',
    about: '老师介绍',
    expertise: {
      title: '教学方向',
      business: '商务中文',
      daily: '日常会话',
      kids: '少儿教学',
      beginner: '零基础中文',
      HSK: 'HSK 备考',
      hsk: 'HSK 备考',
      speaking: '口语提升'
    },
    accent: {
      title: '口音',
      mainland: '普通话',
      mandarin_cn: '普通话(大陆)',
      taiwan: '台湾国语',
      mandarin_tw: '台湾国语',
      cantonese: '粤语'
    },
    languages: '语言',
    introVideo: '介绍视频',
    yearsExperience: '{n} 年教学经验',
    scheduleTitle: '可约时段',
    notFound: '没有找到这位老师。'
  },
  booking: {
    bookNow: '立即预约',
    pickSlotFirst: '请先选择上课时段',
    picker: {
      tzHint: '时段已转换为你的时区({tz})',
      today: '今天',
      empty: '近 7 天暂无可约时段。',
      weekday: {
        0: '周日',
        1: '周一',
        2: '周二',
        3: '周三',
        4: '周四',
        5: '周五',
        6: '周六'
      }
    },
    dialog: {
      title: '确认预约',
      teacher: '老师',
      when: '上课时间',
      choosePackage: '选择扣次套餐',
      freeTrialBadge: '免费体验',
      freeTrialFallback: '免费体验课',
      packageFallback: '{count} 节套餐',
      remaining: '剩余 {n} 节',
      expireBy: '有效期至 {date}',
      submitting: '预约中...',
      confirm: '确认预约',
      noPackages: {
        title: '你还没有可用套餐',
        hint: '完成 30 秒水平测试,我们为你推荐合适的套餐。',
        cta: '去做水平测试'
      }
    },
    success: {
      title: '预约成功!',
      subtitle: '我们已为你保留这个时段。',
      orderTitle: '订单详情',
      teacher: '老师',
      scheduledAt: '上课时间',
      tzNote: '已按你的时区显示({tz})',
      duration: '时长',
      minutes: '{n} 分钟',
      package: '扣次套餐',
      packageFallback: '已扣 1 节',
      status: '状态',
      emailNote: '我们会在课前向你的邮箱发送提醒。课程结束后会进入评价。',
      backHome: '返回首页',
      viewOrders: '查看我的订单'
    },
    status: {
      upcoming: '待上课',
      cancelled: '已取消',
      finished: '已完成',
      refunding: '退款中',
      refunded: '已退款',
      no_show_student: '学生缺席',
      no_show_teacher: '教师缺席',
      abnormal: '异常',
      to_review: '待评价'
    },
    teacherBlocked: {
      title: '提示',
      message: '教师账号不可购买课程或预约课时,请使用学生账号操作。',
      tooltip: '教师账号不可购买,请切换学生账号',
      shortHint: '教师账号不可购买'
    }
  },
  myOrders: {
    title: '我的订单',
    subtitle: '你的全部预约课程',
    backHome: '返回首页',
    loadFailed: '订单列表加载失败,请重试。',
    tabs: {
      all: '全部',
      upcoming: '待上课',
      toReview: '待评价',
      finished: '已完成',
      cancelled: '已取消',
      refunding: '退款中'
    },
    empty: {
      all: '你还没有任何订单。先去挑一位老师吧。',
      upcoming: '暂无待上课订单。',
      toReview: '没有待评价的课程。',
      finished: '还没有完成的课程。',
      cancelled: '没有已取消的订单。',
      refunding: '没有退款中的订单。'
    },
    browseTeachers: '浏览老师',
    card: {
      scheduledAt: '上课时间',
      tzSuffix: '你的时区({tz})',
      duration: '时长 {n} 分钟',
      package: '扣次套餐',
      packageFallback: '已扣 1 节',
      cancelReason: '取消原因',
      refundedClass: '已返还 1 节课次',
      notRefundedClass: '未返还课次(课前不足 24h)',
      enterClass: '进入课堂',
      enterClassNotYet: '课前 5 分钟开放进入',
      cancel: '取消订单'
    },
    cancelDialog: {
      title: '确认取消订单?',
      policyHeading: '取消规则',
      policy24hPlus: '课前 24 小时及以上 — 全额返还 1 节课次',
      policy24hWithin: '课前 24 小时内 — 不返还课次,不退款',
      willRefund: '当前距上课 {hours} 小时,取消后 1 节课次将返还到原套餐。',
      willNotRefund: '当前距上课不足 24 小时,取消后课次不返还。',
      alreadyStarted: '上课时间已过,无法在线取消,请联系客服。',
      reasonLabel: '取消原因(选填)',
      reasonPlaceholder: '简短说明,方便我们改进...',
      keepIt: '我再想想',
      confirm: '确认取消',
      submitting: '取消中...',
      success: '订单已取消',
      error: '取消失败,请稍后重试'
    }
  },
  classroom: {
    title: '课堂',
    back: '返回',
    backToOrders: '返回我的订单',
    loading: '正在进入课堂...',
    refresh: '刷新课堂',
    subtitle: {
      student: '老师 {nickname} · {duration} 分钟',
      teacher: '学生 {nickname} · {duration} 分钟'
    },
    network: {
      online: '已连接',
      offline: '已断开',
      offlineTip: '网络已断开,请检查 WiFi 或 4G'
    },
    error: {
      loadFailed: '课堂加载失败',
      retryHint: '请检查网络后重试,或联系客服',
      contactSupport: `如持续无法连接,请联系客服 hello{'@'}mandarly.com`,
      autoRetry: '{seconds} 秒后第 {n} 次自动重试...',
      manualRetry: '手动重连',
      noJoinUrl: '课堂入场凭证缺失,请联系客服。'
    },
    leaveConfirm: {
      title: '离开课堂',
      message: '确认要离开课堂吗?当前课程仍在进行,提前离开可能影响课时记录。',
      ok: '确认离开',
      cancel: '继续上课'
    },
    fallback: {
      title: '课程结束',
      message: '课程预定时长已超过 5 分钟,系统未收到结束信号。是否前往评价或返回订单?',
      toReview: '前往评价',
      toOrders: '返回订单',
      continue: '继续上课'
    },
    stub: {
      title: 'Mock 课堂(本地开发)',
      desc: '凭证未就位时的占位页。LCIC_MODE=real + 真实凭证就位后,这里会被腾讯云 LCIC 真实课堂替换。',
      classId: 'Class ID',
      role: '角色',
      userId: 'User ID',
      token: 'Token',
      hint: '本页仅在本地 / dev 显示。生产环境永远不会进到这里。'
    }
  },
  auth: {
    login: {
      title: '登录 Mandarly',
      emailTab: '邮箱',
      phoneTab: '手机号',
      socialTab: '第三方',
      methodLabel: '登录方式',
      email: '邮箱',
      phone: '手机号',
      password: '密码',
      code: '验证码',
      submit: '登录',
      submitting: '登录中...',
      googleBtn: 'Google',
      appleBtn: 'Apple ID',
      forgotPassword: '忘记密码?',
      noAccount: '还没账号?',
      register: '立即注册',
      socialDivider: '或使用以下账号继续',
      processing: '处理中...',
      socialNotConfigured: '该登录方式即将上线',
      googleUnsupportedBrowserTitle: '请在浏览器中打开',
      googleUnsupportedBrowserMessage: '当前在 {browser} 内置浏览器中打开,Google 登录会被拦截。请点击右上角菜单选择“在浏览器打开”,或复制链接到 Safari / Chrome 后再使用 Google 登录。你也可以先使用邮箱或手机号登录。'
    },
    phone: {
      countryCode: '国家/地区区号',
      nationalPlaceholder: '请输入手机号',
      required: '请输入手机号',
      invalid: '请输入有效的国际手机号'
    },
    validation: {
      emailRequired: '请输入邮箱',
      emailInvalid: '邮箱格式不正确',
      codeRequired: '请输入验证码',
      codeLength: '请输入 6 位验证码',
      termsRequired: '请阅读并同意服务条款',
      realNameRequired: '请输入真实姓名'
    },
    register: {
      title: '注册 Mandarly',
      methodLabel: '注册方式',
      socialDivider: '或使用以下账号快速注册',
      iAmStudent: '我是学生',
      iAmTeacher: '我是教师',
      studentHint: '注册学生账号,领取体验课并开始预约老师。',
      teacherEntryText: '想作为老师加入 Mandarly?',
      teacherEntryAction: '申请成为老师',
      teacherReviewNotice: '教师注册需通过资质审核(平均 1-3 个工作日),与学生注册不同入口。',
      teacherIntro: {
        heading: '加入 Mandarly 教师团队',
        points: [
          '面向香港及海外中文学习者提供 1v1 中文口语课',
          '收入按次结算,自主排课',
          '注册后需提交资质审核(普通话证书 / 学历 / 教学经验)'
        ]
      },
      nickname: '昵称(选填)',
      realName: '真实姓名',
      realNamePlaceholder: '请输入您的真实姓名',
      referralCode: '推荐码(选填)',
      agreeTerms: '我已阅读并同意 {terms} 与 {privacy}',
      termsLabel: '服务条款',
      privacyLabel: '隐私政策',
      submit: '注册',
      haveAccount: '已有账号?',
      goLogin: '去登录'
    },
    resetPassword: {
      title: '重置密码',
      email: '邮箱',
      code: '验证码',
      newPassword: '新密码',
      confirmPassword: '确认新密码',
      confirmPasswordRequired: '请再次输入密码',
      passwordMismatch: '两次密码不一致',
      sendCode: '发送验证码',
      sentTo: '验证码已发送至 {email}',
      next: '下一步',
      success: '密码重置成功,请重新登录',
      unavailable: '该功能暂未开放',
      submit: '提交',
      backToLogin: '返回登录'
    },
    code: {
      send: '发送验证码',
      resend: '重新发送 ({n}s)',
      sent: '已发送',
      cooldown: '请稍后再试'
    },
    callback: {
      processing: '正在登录,请稍候...',
      failed: '登录失败,请重试'
    },
    profile: {
      title: '用户中心',
      basicInfo: '基本信息',
      nickname: '昵称',
      avatar: '头像',
      locale: '语言',
      timezone: '时区',
      bindings: '账号绑定',
      bindEmail: '绑定邮箱',
      bindPhone: '绑定手机',
      bindGoogle: '绑定 Google',
      bindApple: '绑定 Apple',
      unbind: '解绑',
      boundAt: '绑定时间',
      save: '保存',
      saved: '保存成功',
      referralCode: '我的推荐码'
    },
    error: {
      invalidCredentials: '邮箱或密码错误',
      codeExpired: '验证码已过期',
      codeInvalid: '验证码不正确',
      codeCooldown: '请求过于频繁,请稍后再试',
      emailExists: '该邮箱已注册',
      phoneExists: '该手机号已注册',
      teacherEmailExists: '该邮箱已被使用,请使用其他邮箱完成教师注册',
      teacherPhoneExists: '该手机号已被使用,请使用其他手机号完成教师注册',
      userNotFound: '账号不存在',
      userFrozen: '账号已冻结,请联系客服',
      referralInvalid: '推荐码无效',
      socialNotConfigured: '该登录方式暂不可用',
      socialFetchFailed: '三方服务暂时不可用,请改用其他方式',
      tokenExpired: '登录已过期',
      smtpNotConfigured: '邮件服务暂不可用',
      passwordWeak: '密码至少 8 位,包含字母 + 数字',
      rateLimitDaily: '今日发送次数已达上限',
      loginFailedLocked: '登录失败次数过多,请 15 分钟后再试',
      ipRegisterLimit: '注册请求过于频繁',
      socialAccountConflict: '该账号已绑定其他用户'
    },
    heroQuote: {
      login: '欢迎回来 · 继续你的中文学习之旅',
      register: '加入全球 200+ 位母语老师 · 开启自信开口说中文'
    }
  },
  authHero: {
    login: { title: '与全球母语老师 1v1 学中文', subtitle: '600+ 认证老师 · 20+ 国家学员' },
    register: { title: '30 秒注册,开启你的中文学习之旅', subtitle: '注册即送 1 节免费体验课' },
    teacherRegister: { title: '加入 Mandarly,把你的中文教给世界', subtitle: '灵活授课 · 全球学员 · 周结算' },
    resetPassword: { title: '别担心,重置密码很简单', subtitle: '' },
    trust: {
      rating: '⭐ {rating} 平均评分',
      teachers: '{count}+ 认证老师',
      countries: '{count}+ 国家学员'
    }
  },
  level_check: {
    q1: {
      text: '你目前的中文水平是?',
      opt: {
        complete_beginner: '完全零基础 — 从未学过',
        some_basics: '学过一点 — 认识一些字词',
        simple_conversation: '可以进行简单对话',
        fairly_fluent: '比较流利 — 想精进'
      }
    },
    q2: {
      text: '你的主要学习目标是?',
      opt: {
        career: '求职 / 工作',
        business: '商务沟通',
        travel: '旅行 / 生活',
        hsk_exam: '准备 HSK 考试',
        for_kids: '给孩子学',
        just_for_fun: '纯粹兴趣'
      }
    },
    q3: {
      text: '你计划每周上几节课?',
      opt: {
        one_per_week: '每周 1 节',
        two_per_week: '每周 2 节',
        intensive: '强化(每周 3 节及以上)',
        not_sure: '还不确定'
      }
    },
    q4: {
      text: '学习者是谁?',
      opt: {
        myself: '我自己',
        child: '孩子(12 岁以下)',
        teen: '青少年(12-18 岁)',
        multiple: '多人'
      }
    }
  },
  review: {
    title: { write: '为这节课写评价', edit: '修改评价' },
    subtitle: {
      write: '分享你的上课体验,帮助其他同学选老师',
      edit: '可在首次提交后 24 小时内修改'
    },
    courseTime: '上课时间 {time}',
    step1: { title: '整体评分', guide: '请给老师打分(支持半星)' },
    step2: { title: '具体反馈' },
    step3: { title: '确认提交' },
    rating: { label: '评分' },
    rate: {
      text: { 1: '很差', 2: '差', 3: '一般', 4: '好', 5: '非常棒' }
    },
    label: { rating: '评分', content: '评价内容', tags: '亮点标签' },
    placeholder: { content: '说一两句课程感受...(选填)' },
    ratingDesc: {
      1: '一般', 2: '不太好', 3: '还可以', 4: '不错', 5: '很棒'
    },
    tags: {
      addCustom: '自定义标签',
      customLimit: '最多 {n} 条 × {len} 字符',
      customLimitReached: '已达自定义标签上限({n} 条)',
      customPlaceholder: '输入自定义标签(≤{n} 字)',
      maxHint: '至多选 {n} 项',
      remove: '移除 {tag}'
    },
    text: { label: '评价内容(选填)', placeholder: '具体说说这节课吧...' },
    anonymous: { label: '匿名发布', desc: '公开列表展示为"匿名学员"' },
    editWindow: {
      expired: '编辑窗口已过',
      remaining: '可编辑剩余 {hours}h',
      urgent: '可编辑剩余 {hours} 分钟'
    },
    action: { cancel: '取消', next: '下一步', prev: '上一步' },
    submit: '提交评价',
    submitting: '提交中…',
    hint: {
      tagsLimit: '最多选 3 个',
      editWindowLeft: '剩 {hours}h 可修改',
      expired: '修改窗口已过期'
    },
    btn: {
      write: '写评价', edit: '修改评价',
      submit: '提交评价', save: '保存修改', back: '返回'
    },
    error: {
      notFinished: '只有已完成的课才能评价',
      tooManyTags: '最多选 3 个标签',
      editExpired: '修改窗口已过期(限首次提交后 24h 内)',
      submitFailed: '提交失败,请重试',
      loadFailed: '加载失败,请重试'
    },
    success: {
      submitted: '评价已提交',
      updated: '评价已更新',
      title: '评价已提交',
      subtitle: '感谢你的反馈,72h 内可编辑',
      redirect: '即将返回我的课',
      backToOrders: '返回我的课'
    },
    empty: { teacherList: '该老师暂无评价' },
    publicSection: {
      title: '学生评价({n})',
      loadMore: '加载更多',
      end: '— 已加载全部 —'
    },
    card: {
      starsAria: '{n} 星',
      submittedAt: '评价于 {time}',
      editedAt: '编辑于 {time}',
      youWrote: '你的评价'
    },
    tag: {
      patient: '耐心',
      native_accent: '口音地道',
      good_pace: '节奏好',
      well_prepared: '备课充分',
      audio_issue: '声音问题',
      late: '迟到',
      interactive: '互动好',
      good_material: '选材合适',
      std_pronunciation: '发音标准',
      humorous: '幽默',
      encouraging: '鼓励学生',
      proper_difficulty: '难度合适'
    }
  },
  myPackages: {
    title: '我的套餐',
    subtitle: '持有的课程套餐与历史购买',
    tabs: { all: '全部', active: '可用', expired: '已过期', exhausted: '已用完' },
    empty: {
      all: '还没有套餐,先去预约一节课吧',
      active: '没有可用套餐',
      expired: '没有已过期的套餐',
      exhausted: '没有用完的套餐'
    },
    loadFailed: '加载失败,请重试',
    browseTeachers: '浏览教师',
    card: {
      remaining: '剩余 {n} 节',
      total: '共 {n} 节',
      weekly: '每周 {n} 节',
      expireAt: '有效期至 {date}',
      purchasedAt: '获取时间 {date}',
      free: '免费赠送',
      statusActive: '可用',
      statusExpired: '已过期',
      statusExhausted: '已用完'
    },
    source: {
      purchase: '购买',
      free_trial: '注册赠送',
      register_grant: '注册赠送',
      admin_grant: '运营赠送',
      referral_reward: '推荐奖励'
    }
  },
  profileTeacher: {
    stat: {
      title: '我的教学统计',
      avgRating: '平均评分',
      reviewCount: '收到评价',
      orderCount: '完成订单',
      noReviews: '— 暂无 —',
      loadFailed: '统计加载失败'
    },
    myReviews: {
      title: '我收到的评价',
      empty: '还没有学生给你写评价',
      loadMore: '加载更多',
      end: '— 已加载全部 —'
    }
  },
  payment: {
    button: {
      checkout: '立即购买',
      retry: '重试'
    },
    processing: {
      title: '支付处理中',
      tip: '请稍候,我们正在确认你的支付...'
    },
    success: {
      title: '支付成功!',
      message: '套餐已到账,开始预约你的课程吧!'
    },
    cancel: {
      title: '支付已取消',
      message: '你取消了支付流程,套餐未购买。',
      retry: '重新购买',
      back: '返回我的套餐'
    },
    expired: {
      title: '支付处理中,请稍后查看',
      message: '支付结果可能需要几秒同步。如果你已经完成扣款,套餐到账后会出现在我的套餐。'
    },
    amount: {
      usd: '退款金额 (USD)'
    },
    discount: {
      applied: '推荐码折扣已应用'
    },
    status: {
      pending: '待支付',
      paid: '已支付',
      failed: '支付失败',
      expired: '已过期',
      refunded: '已退款',
      partial_refunded: '部分退款'
    },
    viewMyPackages: '查看我的套餐'
  },
  refund: {
    apply: {
      button: '申请退款',
      title: '申请退款',
      reason: {
        label: '退款原因',
        placeholder: '请说明退款原因(至少 10 个字)',
        min10: '退款原因至少需要 10 个字'
      },
      pendingButton: '退款审核中',
      success: '退款申请已提交,等待审核',
      error: '提交失败,请稍后重试'
    },
    myList: {
      title: '我的退款记录',
      empty: '暂无退款记录'
    },
    status: {
      pending: '审核中',
      approved: '已批准',
      refunded: '已到账',
      rejected: '已拒绝',
      failed: '退款失败'
    },
    auditNote: {
      label: '审核备注'
    }
  },
  referral: {
    title: '我的推荐战绩',
    code: {
      your: '我的推荐码',
      copy: '复制',
      copied: '推荐码已复制'
    },
    stat: {
      invited: '已邀请人数',
      reward: '累计奖励课次'
    },
    discount: {
      applied: '推荐码折扣已应用'
    },
    tip: {
      invite: '邀请好友注册并购买套餐,你将获得免费体验课',
      firstOrder: '好友首单可享折扣',
      howItWorks: '好友首次购买套餐后,双方均得奖励'
    }
  },
  package: {
    title: '选择套餐',
    subtitle: '购买课程套餐,开始你的中文学习之旅',
    empty: '暂无可购套餐,请联系客服',
    buyNow: '立即购买',
    weeklyCount: '每周课次',
    validityWeeks: '总课次',
    priceUsd: '价格',
    referralCode: {
      placeholder: '输入推荐码(可选)'
    }
  },
  packages: {
    title: '课程套餐',
    subtitle: '按你的学习节奏选择,登录后查看完整价格',
    pricing: {
      eyebrow: '适合每位学习者的灵活方案',
      titlePrefix: '为你的',
      titleHighlight: '中文',
      titleSuffix: '学习目标选择合适套餐',
      subtitle: '',
      lessonFormat: '所有套餐均包含 30 分钟 1 对 1 私教中文课,由母语中文老师授课。',
      lessonFormatParts: {
        prefix: '所有套餐均包含 ',
        duration: '30 分钟',
        middle1: ' ',
        oneOnOne: '1 对 1',
        middle2: ' ',
        private: '私教',
        suffix: ' 中文课,由母语中文老师授课。'
      },
      pills: {
        tutors: '母语中文老师',
        scheduling: '灵活预约时间',
        personalized: '个性化学习',
        secure: '安全可信赖'
      },
      badge: {
        mostPopular: '最受欢迎',
        bestValue: '最具性价比'
      },
      priceLocked: '登录后查看价格',
      perLesson: '/ 节',
      lessonsCount: '{n} 节课',
      durationMonths: '{n} 个月',
      durationDays: '{n} 天',
      ctaLoggedOut: '预约免费试听',
      ctaLoggedIn: '选择套餐',
      ctaLoggedInPlan: '选择{plan}',
      ctaDefaultPlan: '套餐',
      cards: {
        starter: {
          name: '入门套餐',
          ctaName: '入门套餐',
          subtitle: '适合轻量学习者',
          point1: '每周 1 节课',
          point2: '灵活预约',
          point3: '母语老师授课',
          point4: '零基础友好'
        },
        standard: {
          name: '标准套餐',
          ctaName: '标准套餐',
          subtitle: '适合稳定进步',
          point1: '每周 2 节课',
          point2: '提升更快',
          point3: '优先预约',
          point4: '个性化学习路径'
        },
        annualLite: {
          name: '年度套餐',
          ctaName: '年度套餐',
          subtitle: '适合持续学习者',
          point1: '每周 1 节课',
          point2: '长期稳定学习',
          point3: '更好巩固效果',
          point4: '年付更省'
        },
        premiumAnnual: {
          name: '尊享年度套餐',
          ctaName: '尊享年度套餐',
          subtitle: '适合长期深度学习者',
          point1: '每周 2 节课',
          point2: '长期价值更高',
          point3: '专属学习支持',
          point4: '家长进度反馈'
        }
      },
      why: {
        titlePrefix: '为什么选择',
        titleHighlight: 'Mandarly?',
        native: {
          title: '母语老师',
          desc: '严选并认证的中文老师。'
        },
        schedule: {
          title: '灵活预约',
          desc: '跨时区自由选择合适课时。'
        },
        personalized: {
          title: '个性化学习',
          desc: '课程围绕你的目标与学习风格设计。'
        },
        progress: {
          title: '结构化进步',
          desc: '持续追踪提升,一步步达成目标。'
        }
      },
      testimonials: {
        title: '全球学习者喜爱的中文课',
        emily: {
          name: 'Emily, California',
          quote: '我的女儿终于开始喜欢学中文了!'
        },
        kevin: {
          name: 'Kevin, Singapore',
          quote: '课程灵活、专业,课堂也很有参与感。'
        },
        sophie: {
          name: 'Sophie, London',
          quote: '老师很棒,学习路径也清晰,非常推荐!'
        }
      }
    },
    banner: {
      hero: '套餐价格 · 灵活按月或一年承担',
      freeTrial: {
        text: '注册即送 1 节免费体验课(25 分钟)',
        subtext: '自由选老师 · 无需绑定信用卡',
        cta: '立即领取',
        claimed: '您已领取过免费体验课'
      }
    },
    teacherEntry: {
      eyebrow: '继续预约',
      title: '为这位老师选择合适的套餐',
      titleWithName: '为 {name} 选择合适的套餐',
      desc: '购买课时后可回到老师详情页,继续完成刚才选择的 1 对 1 中文课预约。',
      backToTeacher: '返回老师详情'
    },
    currency: { label: '币种', hkd: 'HKD', usd: 'USD', cny: 'CNY' },
    rules: {
      title: '套餐规则',
      multiPackage: '多套餐管理:可同时持有多个套餐,预约时优先扣即将过期的课次',
      expiry: '到期作废:套餐有效期内未使用的课次,到期后作废(到期前 7/3/1 天提醒)',
      refund: '退款规则:课前 24 小时取消可全额退,详见服务条款 § 5',
      currency: '币种说明:套餐按自身标注币种收款,后台可按币种独立上架或下架'
    },
    compare: {
      title: '完整套餐对比', expand: '展开对比表', collapse: '收起',
      buy: '立即购买', loginToViewPrice: '登录查看价格', loginAction: '登录 / 注册', priceLockedNote: '登录后查看各套餐完整价格,套餐权益仍可对比', yes: '✓', no: '—', free: '免费',
      durationMonths: '{n} 个月', durationDays: '{n} 天',
      col: { package: '套餐' },
      row: {
        pricePerLesson: '单节价格', totalPrice: '总价', totalLessons: '总课次',
        validity: '有效期', booking: '自由预约', refund: '24h 退款',
        upgrade: '升级 / 降级费', discount: '折扣', action: '操作'
      }
    },
    confirm: {
      title: '确认订单', summary: '套餐摘要',
      tip: '点击"前往支付"将跳转 Stripe 安全支付页面',
      refundTip: '课前 24 小时取消可全额退款,详见',
      termsLink: '服务条款 § 5',
      cancel: '取消', confirm: '前往支付', confirming: '处理中…',
      durationMonths: '{n} 个月(自购买日起)', durationDays: '{n} 天',
      sessionsCount: '约 {n} 节', sessionsCountWeekly: '约 {n} 节({weekly} 节/周)',
      field: {
        package: '套餐', sessions: '课次', duration: '有效期',
        currency: '币种', subtotal: '小计', discount: '优惠', total: '合计'
      }
    },
    success: { message: '套餐已到账', returnTo: '返回继续预约' },
    empty: {
      title: '暂无可购套餐,请联系客服',
      desc: '套餐已售罄或正在维护',
      currencyUnavailable: '当前暂无 {currency} 可购套餐,可先查看 HKD 套餐',
      viewHkd: '查看 HKD 套餐'
    },
    error: { loadFailed: '套餐加载失败,请重试', checkoutFailed: '发起支付失败,请稍后重试' }
  },
  seo: {
    home: {
      title: '面向儿童与成人的在线中文课 | Mandarly',
      description: 'Mandarly 提供面向儿童、青少年和成人的在线一对一中文课,由友好的中文母语老师帮助学生提升普通话口语。',
      keywords: '在线中文课,中文口语课,儿童普通话课,一对一普通话课,在线学中文,中文母语老师'
    },
    teachers: {
      title: '中文老师 · 1 对 1 在线授课 · Mandarly',
      description: '浏览 Mandarly 严选的母语中文老师,可按口语、商务中文、HSK、儿童中文标签筛选。选你方便的时段预约 1 对 1 课程,新用户可免费试课。',
      keywords: '中文老师,普通话老师,在线中文课,1对1中文,商务中文老师,HSK 老师'
    },
    packages: {
      title: '价格方案 · 灵活中文课程套餐 · Mandarly',
      description: '选择适合儿童、零基础与全球学习者的灵活中文课程套餐,在 Mandarly 与母语老师进行 1 对 1 学习。',
      keywords: '中文课价格,在线中文套餐,1对1中文课,中文培训价格,普通话课程',
      catalogName: 'Mandarly 中文课程方案'
    },
    teacherDetail: {
      titleTemplate: '{name} 老师 · 中文 1 对 1 教学 · Mandarly',
      titleFallback: '中文老师 · 1 对 1 在线授课 · Mandarly',
      descriptionTemplate: '在 Mandarly 预约 {name} 老师的 1 对 1 中文课。{intro}',
      descriptionFallback: '在 Mandarly 预约这位母语中文老师的 1 对 1 课程,选你方便的时段开课。'
    },
    levelCheck: {
      title: '免费中文水平测试 · 30 秒匹配老师 · Mandarly',
      description: 'Mandarly 免费中文水平测试,30 秒答完 4 道题,智能匹配母语老师与个性化课程方案。无需注册,无需信用卡。',
      keywords: '中文水平测试,普通话测试,HSK 等级测试,免费中文测试,中文分级'
    },
    levelCheckResult: {
      title: '中文水平测试结果 · Mandarly',
      description: '查看你的中文水平测试结果,注册后继续完成学习路径。'
    },
    curriculum: {
      title: '在线中文课程体系 | Mandarly 普通话口语课程',
      description: '了解 Mandarly 在线中文课程体系,从零基础中文到高级普通话表达,适合儿童、青少年和成人的一对一课程。',
      keywords: '在线中文课程体系,普通话口语课程,零基础中文,HSK中文课,儿童中文课'
    }
  },
  curriculum: {
    hero: {
      eyebrow: '课程体系',
      title: 'Mandarly 中文课程体系',
      subtitle: '从零基础中文到自信普通话沟通,一条清晰的学习路径。',
      cta: '预约试听课'
    },
    overview: {
      eyebrow: '课程总览',
      title: '围绕真实口语进步设计的课程路径',
      desc: 'Mandarly 的在线中文课程体系专为海外学习者设计,通过一对一普通话口语课,帮助学生从基础发音和日常对话逐步提升到高级中文沟通。'
    },
    labels: {
      learners: '适合学生',
      goal: '学习目标',
      skills: '技能概览'
    },
    stages: [
      {
        name: '零基础中文入门',
        chineseName: '发音与拼音起步',
        learners: '完全没有中文基础,或只会“你好”“谢谢”等简单词语的学生。',
        goal: '建立中文学习兴趣,掌握基础发音,能够完成简单问候和自我介绍。',
        hsk: 'Pre-HSK / HSK 1',
        skills: ['中文发音基础', '拼音入门', '普通话四声练习', '问候与自我介绍', '数字、年龄、家庭、颜色、食物'],
        button: '了解零基础中文'
      },
      {
        name: '中文基础提升',
        chineseName: '日常会话基础',
        learners: '学过一点中文、认识拼音、能说简单句子但表达还不够流利的学生。',
        goal: '建立稳定基础,扩大词汇量,学习基础句型,能够围绕日常生活进行简单对话。',
        hsk: 'HSK 1-2',
        skills: ['拼音与声调巩固', '日常会话练习', '基础句型', '听力理解', '简短表达任务'],
        button: '了解基础普通话'
      },
      {
        name: '中级中文表达',
        chineseName: '主题表达进阶',
        learners: '具备基础中文能力,想提高表达完整度、听力理解和口语流利度的学生。',
        goal: '提升围绕生活、学习、旅行、文化等话题进行完整自然交流的能力。',
        hsk: 'HSK 2-4',
        skills: ['流利度口语训练', '长句表达训练', '主题式对话', '讲故事练习', '表达观点'],
        button: '了解中级中文'
      },
      {
        name: '高级中文沟通',
        chineseName: '深度沟通提升',
        learners: '已有较好中文基础,希望提升表达深度、文化理解、演讲、写作或考试能力的学生。',
        goal: '从“会说中文”提升到“说得自然、有逻辑、有深度”。',
        hsk: 'HSK 4-6+',
        skills: ['高级中文对话', '观点讨论与辩论', '中文演讲能力', '新闻与文化话题', 'HSK 考试支持'],
        button: '了解高级普通话'
      }
    ],
    bottomCta: {
      title: '不确定适合哪个阶段?',
      desc: '预约试听课,老师会根据年龄、基础、目标和口语能力推荐合适课程阶段。',
      button: '预约试听课'
    }
  },
  comingSoon: {
    badge: '敬请期待',
    cta: { backHome: '返回首页' },
    textbook: {
      title: '教材正在编排中',
      desc: '我们正与教研老师一起整理面向不同水平的精选教材,完成后将在此发布配套讲义与练习。'
    },
    resources: {
      title: '资源中心即将上线',
      desc: '学习指南、词汇卡、文化短文与免费练习材料将陆续上传,敬请关注。'
    }
  },
  header: {
    nav: {
      home: '首页',
      curriculum: '课程体系',
      teachers: '老师',
      packages: '价格',
      levelCheck: '等级测试',
      myOrders: '我的课',
      // D26:未登录态 home 锚点 nav
      features: '教学特色',
      recommendedTeachers: '推荐老师',
      packagesSection: '课程套餐',
      stories: '学员故事',
      faq: '常见问题',
      // D27:外部内容入口
      textbook: '教材',
      resources: '资源中心',
      podcast: '播客',
      // D27:抽屉分组父项
      aboutGroup: '了解 Mandarly',
      resourcesGroup: '资源'
    },
    action: {
      login: '登录',
      register: '注册',
      becomeTeacher: '成为老师'
    },
    menu: {
      profile: '个人中心',
      profileTeacher: '个人资料',
      myPackages: '我的套餐',
      myOrdersTeacher: '我的订单',
      income: '我的收入',
      withdrawal: '申请提现',
      myRefunds: '我的退款',
      logout: '退出登录'
    }
  },
  tabbar: {
    home: '首页',
    teachers: '老师',
    myOrders: '我的课',
    profile: '我的'
  },
  footer: {
    section: {
      about: '关于我们',
      service: '服务',
      legal: '法律',
      contact: '联系',
      payment: '支持的付款方式',
      follow: '关注我们'
    },
    about: {
      intro: '公司简介',
      jobs: '加入我们',
      teacherJoin: '成为老师',
      contact: '联系方式'
    },
    legal: {
      privacy: '隐私政策',
      terms: '服务条款'
    },
    social: {
      followOn: '在 {name} 关注我们',
      wechatScan: '扫码通过微信联系我们',
      wechatZoomHint: '点击放大查看微信二维码'
    },
    copyright: '© 2026 曼德勵科技有限公司 · MANDARLY TECHNOLOGY LIMITED · 香港'
  },
  support: {
    open: '打开客服',
    close: '关闭客服',
    title: 'Mandarly 客服',
    subtitle: '微信 · WhatsApp · Email',
    directHint: '请选择一个渠道,客服团队会尽快跟进。',
    greeting: 'Hi! 输入您的问题,我帮您查找答案。或直接联系真人客服。',
    placeholder: '输入问题,按 Enter 发送',
    send: '发送',
    noMatch: '暂时没有找到合适答案,您可以联系真人客服。',
    wechatCopied: '微信号已复制',
    wechatCopyFailed: '复制失败,请手动复制微信号:{id}',
    openFailed: '打开链接失败,请稍后重试或截屏联系运营。',
    error: '客服暂时不可用,请稍后重试。',
    channel: {
      wechat: '微信',
      whatsapp: 'WhatsApp',
      email: 'Email',
      other: '联系方式',
      pending: '该联系方式正在配置中。'
    }
  },
  teacherDetail: {
    hero: {
      rating: '评分',
      reviews: '{count} 条评价',
      todayAvailable: '今日可约 {count} 节',
      favorite: '收藏'
    },
    video: { title: '试讲视频', placeholder: '点击播放' },
    intro: { title: '关于我', more: '展开', less: '收起' },
    expertise: { title: '教学专长' },
    book: {
      title: '预约',
      priceUnit: '/ {n} 分钟',
      selectSlot: '选择上课时段',
      selectedSlot: '已选',
      bookNow: '立即预约',
      buyPackageForTeacher: '购买套餐(为本老师预约)',
      viewOtherTeachers: '查看其他老师',
      emptyHint: '请先选择时段',
      packageCreditPricing: '按套餐课次预约',
      loginToViewPackages: '登录后查看套餐',
      refundTip: '课前 24 小时取消可全额退款'
    },
    review: {
      title: '学员评价 ({n})',
      empty: '暂无评价 · 成为第一位评价的学员',
      lowVolume: '期待你成为第一位评价的学员',
      seeAll: '加载更多(还有 {count} 条)'
    },
    related: {
      title: '相关老师',
      viewAll: '查看全部',
      emptyTitle: '暂时没有更多相关老师',
      emptyDesc: '可以回到老师列表,按时间、口音或学习目标重新筛选。',
      emptyAction: '浏览全部老师',
      loadFailed: '相关老师暂时加载失败'
    },
    balance: {
      insufficient: '您的套餐课次不足,前往购买?',
      goPackages: '前往购买'
    }
  },
  teacherCard: {
    badge: { todayAvailable: '今日可约', newTeacher: '新教师', introVideo: '视频' },
    avatarAlt: '教师 {name} 头像',
    favorite: '收藏',
    favoriteSoon: '敬请期待',
    priceUnit: '/ 25 分钟',
    reviewCount: '({count} 条评价)',
    noReviews: '暂无评价',
    finishedLessons: '已完成 {count} 节',
    newTeacherQualified: '新教师 · 已通过资质审核',
    timezoneMeta: '{location} · {offset}'
  },
  packageCard: {
    cta: { buy: '立即购买', claimTrial: '立即领取', loginToViewPrice: '登录查看价格', loginRegister: '登录 / 注册', loading: '加载中…' },
    purchased: '已购',
    perSessionUnit: '/ 节',
    sessions: '{n} 节',
    period: { months: '{n} 个月有效', days: '{n} 天有效' }
  },
  profile: {
    title: '个人中心',
    user: { guest: '未登录' },
    menu: {
      account: '账户',
      referral: '推荐战绩',
      teacherStats: '教学统计',
      myPackages: '我的套餐',
      myOrders: '我的订单',
      myRefunds: '我的退款',
      logout: '退出登录'
    },
    account: {
      saved: '保存成功',
      section: { basic: '基本资料', bindings: '账号绑定', password: '修改密码', danger: '危险区' },
      field: { nickname: '昵称', email: '邮箱', locale: '语言', timezone: '时区', referralCode: '我的推荐码' },
      action: { save: '保存', changeAvatar: '更换头像' },
      avatar: {
        uploading: '上传中...',
        uploadSuccess: '头像更新成功',
        uploadFailed: '头像上传失败,请重试',
        invalidType: '请上传图片文件(PNG / JPG / WebP / GIF)',
        tooLarge: '图片不能超过 {mb}MB'
      },
      bindings: {
        email: '邮箱', phone: '手机', google: 'Google', apple: 'Apple',
        bound: '已绑定', notBound: '未绑定', bindNow: '立即绑定',
        bindEmail: '绑定邮箱', bindPhone: '绑定手机', unbind: '解绑',
        unbindConfirm: '确定要解绑该登录方式吗?', unbindSuccess: '解绑成功',
        bindEmailSuccess: '邮箱绑定成功', bindPhoneSuccess: '手机号绑定成功',
        primaryEmail: '主邮箱'
      },
      password: {
        current: '当前密码', new: '新密码', confirm: '确认新密码', action: '修改密码',
        required: '请填写完整密码', mismatch: '两次输入的新密码不一致',
        success: '密码修改成功', comingSoon: '修改密码功能即将上线'
      },
      logout: {
        title: '退出登录',
        desc: '退出当前账号,未支付订单不会受影响。',
        confirm: '确认要退出当前账号吗?未支付订单不会受影响。',
        confirmBtn: '确认退出',
        success: '已退出登录'
      }
    },
    referral: {
      title: '推荐战绩',
      codeLabel: '推荐码', codeCopied: '推荐码已复制',
      linkLabel: '你的邀请链接', linkCopied: '邀请链接已复制',
      copy: '复制', copied: '已复制',
      shareLabel: '分享:',
      shareText: '和我一起在 Mandarly 学中文,用我的链接注册有优惠 → {link}',
      recordsTitle: '邀请记录',
      stat: { invited: '已邀请', rewarded: '已奖励', totalReward: '累计奖励' },
      status: { rewarded: '已奖励', pending: '待支付' },
      empty: {
        title: '还没有邀请过朋友',
        desc: '复制邀请链接分享给好友,首单成功后双方都有奖励',
        action: '复制邀请链接'
      }
    },
    teacherStats: {
      title: '教学统计',
      stat: { avgRating: '平均评分', lessonCount: '已完成课时', reviewCount: '评价数' },
      monthlyTrend: '本月趋势',
      monthlyPlaceholder: '本月数据汇总即将上线,届时将展示本月完成课时与待上课次。'
    }
  },
  orders: {
    title: '我的课',
    loadFailed: '加载失败,请稍后重试',
    tabs: {
      upcoming: '待上课', toReview: '待评价', finished: '已完成',
      cancelled: '已取消', refunding: '退款中', all: '全部'
    },
    empty: {
      upcoming: { title: '暂无待上课订单', desc: '去看看老师,预约第一节课吧' },
      toReview: { title: '没有需要评价的课', desc: '完成的课程会出现在这里' },
      finished: { title: '还没有完成过课程', desc: '完成的课程会出现在这里' },
      cancelled: { title: '没有已取消订单', desc: '保持出席率有助于学习连续性' },
      refunding: { title: '没有退款中订单', desc: '退款进度会在这里实时同步' },
      all: { title: '暂无订单', desc: '去看看老师,预约第一节课吧' },
      browseTeachers: '浏览老师'
    },
    card: {
      scheduledAt: '上课时间', tzSuffix: '{tz} 时区', duration: '{n} 分钟',
      package: '套餐', packageFallback: '单次预约', freeTrialBadge: '免费体验',
      cancelReason: '取消原因',
      refundedClass: '✓ 课次已返还', notRefundedClass: '✗ 课次未返还',
      refundProgress: '退款进度', channelRefundId: '退款流水号',
      status: {
        upcoming: '待上课', finished: '已完成', finishedSettling: '等待结算',
        finishedReview: '待评价', cancelled: '已取消', refunded: '已退款',
        refunding: '退款中', no_show_student: '未出席', no_show_teacher: '老师缺席',
        abnormal: '异常', reviewExpired: '编辑窗口已过'
      },
      action: {
        enterClass: '进入课堂', returnClass: '重返课堂',
        review: '★ 写评价', editReview: '编辑评价',
        cancel: '取消预约', viewDetail: '查看详情'
      }
    },
    countdown: {
      daysHours: '{days} 天 {hours} 小时',
      hoursMinutes: '{hours} 小时 {minutes} 分',
      minutesSeconds: '{minutes} 分 {seconds} 秒',
      inProgress: '上课中', expired: '已结束'
    },
    refundWindow: {
      over24h: 'ⓘ 24 小时前取消可全额退',
      under24h: '⚠️ 距上课不足 24 小时,取消课次不返还'
    },
    refundProgress: { applied: '已申请', reviewing: '审核中', refunded: '已退款' },
    cancelDialog: {
      title: '取消预约',
      summary: { teacher: '老师', time: '上课时间', distance: '距上课' },
      countdown24Plus: '✓ 距上课 ≥ 24h,取消后课次将全额返还',
      countdown24Minus: '⚠️ 距上课不足 24h,取消课次将不返还',
      alreadyStarted: '课程已开始,无法取消',
      reasonLabel: '取消原因(选填,帮助我们改进)',
      reasonPlaceholder: '请选择取消原因',
      notePlaceholder: '补充说明(可选)',
      reasonOther: '其它',
      reason: {
        conflict: '时间冲突', illness: '身体不适', travel: '临时出行',
        noShow: '老师未确认', changedMind: '改变主意'
      },
      cancel: '保留预约', confirm: '确认取消', confirming: '提交中…',
      success: '已取消', error: '取消失败,请稍后重试'
    }
  },
  teacherCenter: {
    nav: {
      dashboard: '工作台',
      profileEdit: '我的档案',
      qualification: '资质材料',
      schedule: '排课',
      orders: '订单',
      income: '收入',
      withdrawal: '提现'
    },
    audit: {
      banner: {
        draft: '资料尚未提交,请完善档案与资质后提交审核',
        pending: '资料已提交,等待管理员审核',
        rejected: '审核未通过:{reason}',
        draftAction: '去完善',
        action: '去补充'
      }
    },
    dashboard: {
      title: '教师中心',
      welcome: '你好,{name}',
      weeklyClassesLabel: '本周课次',
      monthlyIncomeLabel: '本月收入',
      pendingSettleLabel: '待结算',
      ratingLabel: '评价分',
      quickActionsTitle: '快捷操作',
      quickActionSchedule: '管理排课',
      quickActionOrders: '查看订单',
      quickActionIncome: '查看收入',
      quickActionWithdrawal: '申请提现'
    },
    profile: {
      title: '我的档案',
      subtitle: '完善个人介绍与教学信息,资料保存后由管理员审核',
      intro: {
        label: '自我介绍',
        placeholder: '简单介绍一下你的教学风格、背景与擅长的课程类型',
        hint: '建议 200~600 字,语气亲切自然,便于学生快速了解你'
      },
      avatar: {
        label: '教师头像 / 卡片主图',
        upload: '上传照片',
        uploading: '上传中...',
        hint: '会展示在学生端老师卡片和详情页。建议使用清晰的正方形或 4:3 头像照,最大 {mb} MB。'
      },
      expertise: {
        label: '教学方向',
        hint: '可多选;勾选会出现在学生筛选条件里'
      },
      languages: {
        label: '掌握的语言',
        placeholder: '请选择你能教学或沟通的语言'
      },
      languageOptions: {
        zh: '中文(普通话)',
        en: '英语',
        ar: '阿拉伯语',
        yue: '粤语',
        ja: '日语',
        ko: '韩语'
      },
      accent: {
        label: '口音'
      },
      accentOptions: {
        mandarin_cn: '普通话(大陆)',
        mandarin_tw: '台湾国语',
        cantonese: '粤语',
        mixed: '其他/混合'
      },
      yearsExperience: {
        label: '教学年限',
        hint: '按整年计,无相关经验填 0'
      },
      introVideo: {
        label: '自我介绍视频',
        upload: '上传介绍视频',
        replace: '更换视频',
        remove: '移除',
        hint: '支持 mp4,单文件 ≤ {mb} MB,时长 ≤ {sec} 秒'
      },
      meta: {
        qualificationCount: '已上传资质材料',
        auditedAt: '上次审核时间'
      },
      actions: {
        save: '保存',
        saving: '保存中…',
        submitAudit: '提交审核',
        submitAuditConfirm: '提交'
      },
      messages: {
        loadFailed: '档案加载失败,请稍后重试',
        saveSuccess: '档案已保存',
        saveFailed: '保存失败,请稍后重试',
        submitAuditConfirm: '资料修改完成后再提交审核。提交后将进入待审核状态,审核期间无法再次提交,确定继续吗?',
        submitAuditSuccess: '已提交,资料正在审核中',
        submitAuditFailed: '提交失败,请稍后重试',
        videoFormatInvalid: '仅支持 mp4 视频',
        videoTooLarge: '视频大小不能超过 {mb} MB',
        videoTooLong: '视频时长不能超过 {sec} 秒',
        videoUnreadable: '视频读取失败,请更换文件后重试',
        videoUploadSuccess: '视频上传成功',
        videoUploadFailed: '视频上传失败,请稍后重试',
        avatarFormatInvalid: '请上传图片文件',
        avatarTooLarge: '图片大小不能超过 {mb} MB',
        avatarUploadSuccess: '头像已更新',
        avatarUploadFailed: '头像上传失败,请稍后重试'
      }
    },
    qualification: {
      title: '资质材料',
      subtitle: '上传身份证件、学历证书等材料,提交后由管理员审核',
      requiredHint: '必填:身份证件、学历证书。选填:教师资格证、英语四级或六级证书、教学经验证明。',
      docType: {
        label: '材料类型',
        placeholder: '请选择材料类型',
        idCard: '身份证件',
        passport: '护照',
        degreeCert: '学历证书',
        teachingCert: '教学证书',
        englishCert: '英语四级或六级证书',
        experienceProof: '教学经验证明'
      },
      upload: {
        sectionTitle: '上传一份',
        hint: '支持 jpg / png / pdf,单文件 ≤ {mb} MB',
        button: '选择文件并上传',
        success: '已上传',
        failed: '上传失败,请稍后重试',
        errorFormat: '仅支持 jpg / png / pdf 格式',
        errorSize: '文件超过 {mb} MB,请压缩后重试',
        noDocType: '请先选择材料类型'
      },
      list: {
        sectionTitle: '已上传材料',
        empty: '尚未上传任何资质材料',
        auditStatus: {
          pending: '审核中',
          approved: '已通过',
          rejected: '未通过'
        },
        rejectReasonPrefix: '未通过原因:',
        delete: '删除',
        deleteConfirm: '确定删除该份资质材料?删除后无法恢复',
        deleteSuccess: '已删除',
        deleteFailed: '删除失败,请稍后重试'
      },
      messages: {
        loadFailed: '资质材料加载失败,请稍后重试'
      }
    },
    schedule: {
      title: '我的排课',
      subtitle: '设置每周可约时段,学生将看到对应空档预约',
      weekViewTitle: '每周可约时段',
      weekRange: '{from} ~ {to}',
      prevWeek: '上一周',
      nextWeek: '下一周',
      currentWeek: '本周',
      timezoneLabel: '时区:{tz}',
      modeLabel: '编辑模式',
      modeTemplate: '编辑每周固定时间',
      modeTemplateDesc: '影响以后每一周,适合设置长期可约时间',
      modeException: '调整本周例外',
      modeExceptionDesc: '只影响当前这周的具体日期,适合临时开放或关闭',
      periodLabel: '时间段',
      periodAll: '全部 07:00-23:00',
      periodMorning: '上午 07:00-12:00',
      periodAfternoon: '下午 12:00-18:00',
      periodEvening: '晚上 18:00-23:00',
      applyToNextWeek: '应用模板到下周',
      applyToNextWeekConfirm: '将本周模板应用到下周?已设置的下周例外不会被覆盖。',
      applyToNextWeekSuccess: '已应用到下周',
      addException: '添加本周例外',
      slotAvailable: '可约',
      slotWeeklyAvailable: '每周可约',
      slotUnavailable: '不可约',
      slotException: '本周例外',
      legendAvailable: '每周可约',
      legendException: '本周例外',
      legendWeeklyAvailable: '每周固定可约',
      legendExceptionClosed: '本周临时关闭',
      legendExceptionExtra: '本周临时开放',
      slotClosedThisWeek: '本周关闭',
      slotOpenThisWeek: '本周开放',
      bulkWeekday: '工作日批量',
      bulkColumn: '整列勾选',
      exceptionDrawerTitle: '本周例外',
      exceptionEmpty: '本周暂无例外',
      exceptionDate: '日期',
      exceptionTime: '时间',
      exceptionAction: '动作',
      exceptionActionMake: '改为可约',
      exceptionActionBlock: '改为不可约',
      exceptionActionMakeOnce: '仅本周开放这个时间',
      exceptionActionBlockOnce: '仅本周关闭这个时间',
      exceptionAddSuccess: '已添加例外',
      exceptionRemoveSuccess: '已删除例外',
      toggleSuccess: '已保存',
      toggleFailed: '保存失败,已回滚',
      loadFailed: '排课加载失败,请稍后重试',
      mobileDayDot: '周{day}',
      onboarding: {
        title: '第一次设置排课?',
        step1: '① 点击下方任意时间格子,标记为「可约」(再点一次取消)',
        step2: '② 模板按周循环,设好后每周自动生效;某周临时调整用「本周例外」',
        step3: '③ 保存即时生效,学生会在「老师详情」页看到你的空档下单',
        dismiss: '知道了,开始设置'
      }
    },
    orders: {
      title: '我的订单',
      subtitle: '查看你的全部预约课程',
      loadFailed: '订单加载失败,请稍后重试',
      tabs: {
        all: '全部',
        upcoming: '待上课',
        finished: '已上课',
        cancelled: '已取消'
      },
      empty: {
        all: '暂无订单',
        upcoming: '暂无待上课订单',
        finished: '暂无已上课订单',
        cancelled: '暂无已取消订单'
      },
      card: {
        student: '学生',
        scheduledAt: '上课时间',
        duration: '时长 {n} 分钟',
        package: '使用套餐',
        settleStatusLabel: '结算状态',
        settleFrozen: '冻结中(T+7 解冻)',
        settleAvailable: '已入账可提现',
        settleCancelled: '已取消不结算',
        feeLabel: '课时费',
        enterClass: '进入课堂',
        enterClassNotYet: '课前 5 分钟开放',
        cancelledByStudent: '学生已取消',
        viewDetail: '查看详情'
      }
    },
    income: {
      title: '我的收入',
      subtitle: '收入入账与提现总览',
      loadFailed: '收入加载失败,请稍后重试',
      totalEarnedLabel: '累计入账',
      availableLabel: '可提现余额',
      frozenLabel: '在途 T+7',
      pendingWithdrawalLabel: '申请中冻结',
      availableHint: '已解冻可申请提现的余额',
      frozenHint: '近期上课入账,T+7 自动解冻',
      pendingWithdrawalHint: '有提现申请审核中,暂不可再申请',
      nextUnfreezeLabel: '最近解冻 {date}',
      applyWithdrawalBtn: '申请提现 →',
      detailTitle: '收入明细',
      filterFrom: '开始日期',
      filterTo: '结束日期',
      filterType: '收入类型',
      filterTypeAll: '全部',
      filterTypeClass: '课时收入',
      filterTypeAdjust: '人工调整',
      exportCsv: '导出 CSV',
      column: {
        classTime: '课程时间',
        student: '学生',
        type: '类型',
        fee: '课时费(USD)',
        status: '状态',
        unfreezeAt: '解冻时间'
      },
      statusFrozen: '冻结',
      statusAvailable: '可提现',
      statusWithdrawn: '已提现',
      empty: '暂无收入明细'
    },
    withdrawal: {
      apply: {
        title: '申请提现',
        subtitle: '填写收款信息,提交后由管理员人工审核打款',
        balanceLabel: '可提现余额',
        balanceUnit: 'USD',
        amountLabel: '提现金额',
        amountUnit: 'USD',
        amountPlaceholder: '请输入提现金额',
        amountHint: '最低提现 {min} USD,手续费由教师承担',
        amountAllInBtn: '全部',
        payeeMethodLabel: '收款方式',
        payeeMethodWechat: '微信',
        payeeMethodAlipay: '支付宝',
        payeeMethodPaypal: 'PayPal',
        payeeMethodBankCard: '银行卡',
        payeeMethodOther: '其他',
        payeeInfoLabel: '收款信息',
        payeeInfoPlaceholderWechat: '请输入微信号',
        payeeInfoPlaceholderAlipay: '请输入支付宝账号(手机号或邮箱)',
        payeeInfoPlaceholderPaypal: '请输入 PayPal 注册邮箱',
        payeeInfoPlaceholderBankCard: '开户行 / 卡号 / 户名',
        payeeInfoPlaceholderOther: '请详细填写收款方式与账号',
        bankNameLabel: '开户行',
        bankCardNoLabel: '卡号',
        bankHolderNameLabel: '户名',
        bankNamePlaceholder: '请填写开户行',
        bankCardNoPlaceholder: '请填写银行卡号',
        bankHolderNamePlaceholder: '请填写户名',
        payeeInfoHintWechat: '请填写微信号或可通过扫码识别的标识',
        payeeInfoHintAlipay: '请填写支付宝账号(手机号 / 邮箱)',
        payeeInfoHintPaypal: '请填写有效的 PayPal 注册邮箱,打款将通过 PayPal 完成',
        payeeInfoHintBankCard: '请填写完整开户行 + 卡号 + 户名,信息错误可能导致打款失败',
        payeeInfoHintOther: '请详细填写收款方式与账号,以便管理员核对',
        payeeInfoSecurityNote: '为保护信息安全,系统不会保存历史收款信息,每次申请需重新填写',
        confirmCheckbox: '我确认上述收款信息无误,知悉打款由管理员人工处理,信息错误可能导致打款失败',
        cancelBtn: '取消',
        submitBtn: '提交申请',
        submitting: '提交中...',
        submitSuccess: '提现申请已提交,等待审核',
        submitFailed: '提交失败,请稍后重试'
      },
      list: {
        title: '提现记录',
        subtitle: '历史提现申请与打款状态',
        applyBtn: '申请提现',
        loadFailed: '记录加载失败,请稍后重试',
        empty: '暂无提现记录',
        filterStatus: '状态筛选',
        filterStatusAll: '全部状态',
        column: {
          appliedAt: '申请时间',
          amount: '金额(USD)',
          method: '收款方式',
          payeeMasked: '收款账号',
          status: '状态',
          action: '操作'
        },
        statusPending: '待审核',
        statusApproved: '已通过 待打款',
        statusPaid: '已打款',
        statusRejected: '已驳回',
        statusFailed: '打款失败',
        viewDetailBtn: '详情',
        reapplyBtn: '重新申请',
        detailTitle: '提现详情',
        timelineTitle: '状态流转',
        timelineApplied: '提交申请',
        timelineApproved: '审核通过',
        timelineRejected: '审核驳回',
        timelinePaid: '已打款',
        timelineFailed: '打款失败',
        rejectReasonLabel: '驳回原因',
        failReasonLabel: '失败原因',
        paidRemarkLabel: '打款备注',
        paidProofLabel: '打款凭证',
        payeeMaskedHint: '收款账号仅展示后 4 位,完整信息已加密保存'
      }
    },
    errors: {
      balanceInsufficient: '余额不足',
      withdrawalPending: '您已有进行中的提现申请,请等待审核完成后再申请',
      withdrawalBelowMin: '提现金额低于最低额度 {min} USD',
      withdrawalAboveBalance: '提现金额超过可提现余额',
      amountInvalid: '请输入有效的提现金额',
      payeeRequired: '请填写收款信息',
      methodRequired: '请选择收款方式',
      confirmRequired: '请先勾选确认收款信息无误',
      permissionDenied: '您没有权限访问教师中心',
      networkError: '网络错误,请稍后重试'
    }
  }
}
