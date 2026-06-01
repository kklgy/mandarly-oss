import legalPrivacy from './legal-privacy/zh-TW.js'
import legalTerms from './legal-terms/zh-TW.js'

export default {
  legal: {
    ...legalPrivacy.legal,
    ...legalTerms.legal,
    toc: { title: '目錄', close: '關閉' },
    print: '列印',
    progressLabel: '閱讀進度',
    privacy: {
      ...legalPrivacy.legal.privacy,
      lastUpdatedLabel: '最後更新:{date}'
    },
    terms: {
      ...legalTerms.legal.terms,
      lastUpdatedLabel: '最後更新:{date}'
    }
  },
  app: {
    name: 'Mandarly',
    tagline: '和母語老師一對一學中文'
  },
  common: {
    next: '下一題',
    back: '上一題',
    submit: '提交',
    skip: '跳過',
    retry: '重試',
    loading: '載入中...',
    error: '發生錯誤',
    cancel: '取消',
    confirm: '確認',
    delete: '刪除',
    close: '關閉',
    networkError: {
      title: '載入失敗',
      desc: '請檢查網絡後重試'
    },
    price: {
      free: '免費',
      loginToView: '登入後查看價格'
    }
  },
  home: {
    hero: {
      eyebrow: '線上 1 對 1 中文口語課',
      title: '面向兒童與成人的線上中文課',
      tagline: '與全球母語老師 1v1 學中文',
      subtitle: '跟友善的中文母語老師上即時一對一口語課,逐步建立普通話表達信心。',
      cta: {
        freeTrial: '預約免費試聽',
        curriculum: '查看課程體系',
        browseTeachers: '瀏覽老師',
        levelCheck: '30 秒等級測試'
      },
      trust: '中文母語老師 | 一對一即時課堂 | 以口語表達為核心',
      video: {
        title: '看看 Mandarly 課堂如何進行',
        caption: '與中文母語老師即時進行一對一普通話口語課。',
        play: '播放有聲版',
        soundOn: '聲音已開啟'
      }
    },
    feature: {
      title: '為什麼選 Mandarly',
      native: { title: '母語老師 1v1', desc: '全球認證中文母語老師,1 對 1 專屬定制課程' },
      levelCheck: { title: '零基礎友好', desc: '30 秒等級測試,智能推薦合適老師和入門課程' },
      flexible: { title: '靈活預約', desc: '半年/一年套餐,按節奏自由預約,可取消可改期' },
      // D25:第 4 張卡(對齊參考站 4 張圖文交錯)
      professional: { title: '專業認證師資', desc: '100% 持證上崗,嚴格篩選培訓,豐富的國際中文教學經驗' },
      // D25:圖文交錯 CTA 按鈕文案
      learnMore: '了解更多'
    },
    recommend: {
      title: '推薦老師',
      sub: '嚴選評分 4.8+ 的母語老師,新認證老師優先',
      viewAll: '查看全部',
      viewDetail: '查看詳情',
      empty: '更多老師即將上線',
      prev: '上一組',
      next: '下一組'
    },
    package: {
      title: '選擇適合你的學習節奏',
      sub: '免費體驗 + 半年/一年套餐,註冊後查看完整價格',
      viewAll: '查看完整套餐對比',
      recommended: '推薦',
      discount: '9 折省 {amount}',
      freeTrial: { name: '免費體驗' },
      halfYear: { name: '半年包(每週 1 節)' },
      fullYear: { name: '一年包(每週 2 節)' }
    },
    curriculumPath: {
      eyebrow: '四階段學習路徑',
      title: '跟隨你成長的中文課程體系',
      subtitle: '從零基礎中文到高級普通話溝通,每個階段都圍繞口語自信逐步提升。',
      cta: '查看完整課程體系',
      stages: [
        { label: '入門', name: '零基礎中文入門', desc: '從發音、拼音、聲調和簡單問候開始。' },
        { label: '基礎', name: '中文基礎提升', desc: '建立日常詞彙、基礎句型和簡單對話能力。' },
        { label: '中級', name: '中級中文表達', desc: '通過主題、故事、文化和長對話提升流利度。' },
        { label: '高級', name: '高級中文溝通', desc: '發展深度討論、演講、寫作輔助和 HSK 備考能力。' }
      ]
    },
    trustBar: {
      title: '受到全球 2,300+ 學員信任',
      items: {
        lesson: { title: '30 分鐘一對一課程', desc: '專注、高效的真人口語練習' },
        native: { title: '中文母語老師', desc: '專業認證,經驗豐富,耐心引導' },
        schedule: { title: '靈活預約時間', desc: '按照你的時區和節奏安排課程' },
        progress: { title: '學習進度追蹤', desc: '個性化反饋和持續學習支持' }
      }
    },
    how: {
      title: 'Mandarly 如何開始',
      subtitle: '4 個簡單步驟,開啟你的普通話學習旅程',
      cta: '預約免費試聽',
      steps: [
        { title: '免費水平測試', desc: '先完成輕量測試,了解目前中文水平。' },
        { title: '匹配合適老師', desc: '根據目標和基礎,推薦適合你的老師。' },
        { title: '預約上課時間', desc: '選擇最適合你時區和日程的課堂時間。' },
        { title: '開始學習進步', desc: '上個性化一對一課程,持續追蹤進步。' }
      ]
    },
    faq: {
      title: '常見問題',
      q1: { q: '學費怎麼收費?', a: '提供按週節奏的半年/一年套餐。註冊或登入後可查看完整價格與幣種,新用戶可領取 1 節免費體驗課。' },
      q2: { q: '老師都是中文母語嗎?有什麼資質?', a: '全部為中文母語老師(普通話/粵語),持有教師認證 + 中文教學經驗 ≥ 1 年。所有老師由 Mandarly 團隊人工審核(身份/資質/試講)。' },
      q3: { q: '怎麼上課?需要安裝軟件嗎?', a: '瀏覽器直接上課,無需安裝軟件(基於騰訊雲 LCIC)。支持 PC / 手機 / 平板,音視頻高清。課前 30 分鐘可進入課堂調試設備。' },
      q4: { q: '可以免費試聽一節嗎?', a: '是。註冊即送 1 節免費體驗課(25 分鐘),自由選老師,無需綁定信用卡。' },
      q5: { q: '不滿意可以退款嗎?', a: '課前 24 小時取消可全額退款(扣次返還);開課不滿意可申請退款,詳見服務條款 §5 退款規則。' },
      q6: { q: '完全零基礎也能上課嗎?適合小孩嗎?', a: '完全零基礎可以,所有老師都有零基礎學員經驗;6-12 歲學員可在套餐中選擇「少兒適用」標籤的老師。' }
    },
    cta: {
      title: '還在猶豫?先免費測一下你的中文水平',
      sub: '30 秒,4 道題,智能推薦適合你的老師與課程',
      btn: '30 秒等級測試',
      teacherJoin: '成為 Mandarly 老師'
    },

    // D25 新增:數字統計帶(StatsBar.vue)
    stats: {
      activeLearners: { value: '2,300+', label: '活躍學員' },
      professionalTutors: { value: '150+', label: '專業老師' },
      lessonsCompleted: { value: '50,000+', label: '已完成課程' },
      parentSatisfaction: { value: '98%', label: '家長滿意度' }
    },

    // D25 新增:學員見證(TestimonialCards.vue)
    testimonial: {
      title: '學員故事',
      subtitle: '來自全球各地的學員分享他們的學習體驗',
      items: [
        { name: 'Emma', country: '美國 · 學習 6 個月', quote: '我女兒現在每天主動要求上課,發音越來越標準。' },
        { name: 'Lucas', country: '英國 · 學習 1 年', quote: '老師很有耐心,我現在能用中文跟朋友聊天了。' },
        { name: 'Sofia', country: '法國 · 自學 8 個月', quote: '一對一教學真的很有效,進步速度超出預期。' }
      ]
    },

    // D25 新增:AppHeader 中性 tagline
    appTagline: '適合所有年齡,所有水平'
  },
  s0: {
    kicker: '免費水平測試',
    title: '30 秒,找到適合你的老師',
    subtitle: '回答 4 個快速問題,我們為你配對合適的老師和方案。',
    flowLabel: '測試步驟',
    cardLabel: '水平測試題目',
    proof: {
      questions: { value: '4 題', label: '不用複雜測評' },
      login: { value: '免登入', label: '提交後再決定是否註冊' },
      match: { value: '老師+方案', label: '結果直接銜接主路徑' }
    },
    flow: {
      level: '目前水平',
      goal: '學習目標',
      pace: '學習節奏',
      learner: '學習者'
    },
    progress: '第 {current} 題 / 共 {total} 題',
    optionalEmail: {
      label: '電郵(選填,後續推送)',
      placeholder: '你的電郵'
    },
    error: {
      loadFailed: '題目載入失敗,請重試。',
      submitFailed: '提交失敗,請重試。'
    },
    result: {
      subtitle: '根據你的答案,我們先給出一個輕量學習路徑。你可以直接預約老師,也可以聯繫真人客服協助確認。',
      subtitleGuest: '根據你的答案,我們先給出一個輕量學習路徑。註冊後可保存結果並繼續下一步。',
      proof: {
        level: '推斷水平',
        teachers: '推薦老師',
        plan: '推薦方案',
        planReady: '已配對',
        planLater: '可諮詢',
        nextStep: '下一步',
        nextReady: '可繼續'
      },
      teachersEyebrow: '老師推薦',
      heading: {
        beginner: '零基礎起步,正是開始的好時機',
        intermediate: '已經有基礎,繼續提升流利度',
        advanced: '已經流利,精進與專項突破'
      },
      level: {
        beginner: '入門',
        intermediate: '中級',
        advanced: '高級'
      },
      teachersTitle: '為你精選的 Top 3 老師',
      packageEyebrow: '學習節奏',
      packageTitle: '推薦方案',
      packageDesc: '先按你的投入節奏推薦,購買前仍可比較完整方案。',
      packageDefaultName: '為你推薦的方案',
      yearsExperience: '{n} 年教學',
      hasIntroVideo: '有自我介紹',
      bookNow: '立即預約',
      viewPackages: '查看完整方案比較',
      viewMoreTeachers: '查看更多老師',
      registerToContinue: '註冊後繼續',
      contactWhatsapp: '聯繫 WhatsApp 客服',
      contactSupport: '聯繫真人客服',
      empty: '暫無匹配的老師,請聯繫客服。',
      freePackage: '免費體驗',
      perWeek: '每週 {n} 節',
      perWeekPlural: '每週 {n} 節',
      total: '共 {n} 節',
      validity: '有效期 {days} 天'
    }
  },
  teachers: {
    title: '瀏覽我們的老師',
    subtitle: '挑一位與你目標契合的老師',
    intro: {
      eyebrow: '老師 marketplace',
      description: '不確定怎麼選? 先免費體驗 1 節,或 30 秒測水平。',
      descriptionNoTrial: '不確定怎麼選? 30 秒測水平,我們推薦合適老師。',
      freeTrial: '免費體驗課',
      levelCheck: '30 秒測水平'
    },
    filterAll: '全部',
    loadFailed: '老師列表載入失敗,請重試。',
    yearsExperience: '{n} 年教學',
    introVideo: '介紹影片',
    bookNow: '立即預約',
    search: { placeholder: '搜尋老師名 / 標籤', clear: '清空' },
    filter: {
      title: '篩選',
      reset: '重置',
      apply: '套用篩選({count} 位)',
      applyEmpty: '無匹配老師',
      accent: { title: '口音', mandarin_cn: '普通話(大陸)', mandarin_tw: '台灣國語', cantonese: '粵語' },
      priceBuckets: { title: '價格區間', lt200: '< HK$200', '200_500': 'HK$200 - 500', '500_1000': 'HK$500 - 1000', gt1000: '> HK$1000' },
      available: '今日可約',
      minRating: '最低評分',
      expertise: { title: '教學方向' },
      tags: { title: '標籤', beginner: '適合零基礎', kids: '少兒適用', hasVideo: '有試講影片' }
    },
    sort: { label: '排序', recommend: '推薦', rating: '評分高 → 低', priceAsc: '價格低 → 高', priceDesc: '價格高 → 低', reviewCount: '評價數多 → 少' },
    list: { summary: '共 {total} · 已載入 {loaded}', loadMore: '載入更多({remaining} 位)', loadingMore: '載入中...', allLoaded: '已全部載入' },
    empty: {
      noResult: {
        title: '沒有找到匹配的老師',
        desc: '試試放寬篩選條件,或瀏覽所有老師',
        clearFilters: '清除篩選',
        browseAll: '瀏覽全部',
        levelCheck: '做水平測試'
      }
    },
    stats: {
      teachers: '{n} 位老師',
      languages: '{n} 種語言',
      avgRating: '平均評分 {r}'
    }
  },
  teacher: {
    backToList: '返回老師列表',
    about: '老師介紹',
    expertise: {
      title: '教學方向',
      business: '商務中文',
      daily: '日常會話',
      kids: '少兒教學',
      beginner: '零基礎中文',
      HSK: 'HSK 備考',
      hsk: 'HSK 備考',
      speaking: '口語提升'
    },
    accent: {
      title: '口音',
      mainland: '普通話',
      mandarin_cn: '普通話(大陸)',
      taiwan: '台灣國語',
      mandarin_tw: '台灣國語',
      cantonese: '粵語'
    },
    languages: '語言',
    introVideo: '介紹影片',
    yearsExperience: '{n} 年教學經驗',
    scheduleTitle: '可約時段',
    notFound: '沒有找到這位老師。'
  },
  booking: {
    bookNow: '立即預約',
    pickSlotFirst: '請先選擇上課時段',
    picker: {
      tzHint: '時段已轉換為你的時區({tz})',
      today: '今天',
      empty: '近 7 天暫無可約時段。',
      weekday: {
        0: '週日',
        1: '週一',
        2: '週二',
        3: '週三',
        4: '週四',
        5: '週五',
        6: '週六'
      }
    },
    dialog: {
      title: '確認預約',
      teacher: '老師',
      when: '上課時間',
      choosePackage: '選擇扣次方案',
      freeTrialBadge: '免費體驗',
      freeTrialFallback: '免費體驗課',
      packageFallback: '{count} 節方案',
      remaining: '剩餘 {n} 節',
      expireBy: '有效期至 {date}',
      submitting: '預約中...',
      confirm: '確認預約',
      noPackages: {
        title: '你還沒有可用方案',
        hint: '完成 30 秒水平測試,我們為你推薦合適的方案。',
        cta: '去做水平測試'
      }
    },
    success: {
      title: '預約成功!',
      subtitle: '我們已為你保留這個時段。',
      orderTitle: '訂單詳情',
      teacher: '老師',
      scheduledAt: '上課時間',
      tzNote: '已按你的時區顯示({tz})',
      duration: '時長',
      minutes: '{n} 分鐘',
      package: '扣次方案',
      packageFallback: '已扣 1 節',
      status: '狀態',
      emailNote: '我們會在課前向你的電郵發送提醒。課程結束後會進入評價。',
      backHome: '返回首頁',
      viewOrders: '查看我的訂單'
    },
    status: {
      upcoming: '待上課',
      cancelled: '已取消',
      finished: '已完成',
      refunding: '退款中',
      refunded: '已退款',
      no_show_student: '學生缺席',
      no_show_teacher: '教師缺席',
      abnormal: '異常',
      to_review: '待評價'
    },
    teacherBlocked: {
      title: '提示',
      message: '教師帳號不可購買課程或預約課時,請使用學生帳號操作。',
      tooltip: '教師帳號不可購買,請切換學生帳號',
      shortHint: '教師帳號不可購買'
    }
  },
  myOrders: {
    title: '我的訂單',
    subtitle: '你的全部預約課程',
    backHome: '返回首頁',
    loadFailed: '訂單列表載入失敗,請重試。',
    tabs: {
      all: '全部',
      upcoming: '待上課',
      toReview: '待評價',
      finished: '已完成',
      cancelled: '已取消',
      refunding: '退款中'
    },
    empty: {
      all: '你還沒有任何訂單。先去挑一位老師吧。',
      upcoming: '暫無待上課訂單。',
      toReview: '沒有待評價的課程。',
      finished: '還沒有完成的課程。',
      cancelled: '沒有已取消的訂單。',
      refunding: '沒有退款中的訂單。'
    },
    browseTeachers: '瀏覽老師',
    card: {
      scheduledAt: '上課時間',
      tzSuffix: '你的時區({tz})',
      duration: '時長 {n} 分鐘',
      package: '扣次方案',
      packageFallback: '已扣 1 節',
      cancelReason: '取消原因',
      refundedClass: '已返還 1 節課次',
      notRefundedClass: '未返還課次(課前不足 24h)',
      enterClass: '進入課堂',
      enterClassNotYet: '課前 5 分鐘開放進入',
      cancel: '取消訂單'
    },
    cancelDialog: {
      title: '確認取消訂單?',
      policyHeading: '取消規則',
      policy24hPlus: '課前 24 小時及以上 — 全額返還 1 節課次',
      policy24hWithin: '課前 24 小時內 — 不返還課次,不退款',
      willRefund: '當前距上課 {hours} 小時,取消後 1 節課次將返還到原方案。',
      willNotRefund: '當前距上課不足 24 小時,取消後課次不返還。',
      alreadyStarted: '上課時間已過,無法在線取消,請聯繫客服。',
      reasonLabel: '取消原因(選填)',
      reasonPlaceholder: '簡短說明,方便我們改進...',
      keepIt: '我再想想',
      confirm: '確認取消',
      submitting: '取消中...',
      success: '訂單已取消',
      error: '取消失敗,請稍後重試'
    }
  },
  classroom: {
    title: '課堂',
    back: '返回',
    backToOrders: '返回我的訂單',
    loading: '正在進入課堂...',
    refresh: '重新載入課堂',
    subtitle: {
      student: '老師 {nickname} · {duration} 分鐘',
      teacher: '學生 {nickname} · {duration} 分鐘'
    },
    network: {
      online: '已連線',
      offline: '已離線',
      offlineTip: '網路已離線,請檢查 WiFi 或 4G'
    },
    error: {
      loadFailed: '課堂載入失敗',
      retryHint: '請檢查網路後重試,或聯繫客服',
      contactSupport: `如持續無法連線,請聯繫客服 hello{'@'}mandarly.com`,
      autoRetry: '{seconds} 秒後第 {n} 次自動重試...',
      manualRetry: '手動重連',
      noJoinUrl: '課堂入場憑證缺失,請聯繫客服。'
    },
    leaveConfirm: {
      title: '離開課堂',
      message: '確認要離開課堂嗎?目前課程仍在進行,提前離開可能影響課時記錄。',
      ok: '確認離開',
      cancel: '繼續上課'
    },
    fallback: {
      title: '課程結束',
      message: '課程預定時長已超過 5 分鐘,系統未收到結束訊號。是否前往評價或返回訂單?',
      toReview: '前往評價',
      toOrders: '返回訂單',
      continue: '繼續上課'
    },
    stub: {
      title: 'Mock 課堂(本地開發)',
      desc: '憑證未就位時的佔位頁。LCIC_MODE=real + 真實憑證就位後,這裡會被騰訊雲 LCIC 真實課堂替換。',
      classId: 'Class ID',
      role: '角色',
      userId: 'User ID',
      token: 'Token',
      hint: '本頁僅在本地 / dev 顯示。生產環境永遠不會進到這裡。'
    }
  },
  auth: {
    login: {
      title: '登入 Mandarly',
      emailTab: '電郵',
      phoneTab: '手機號',
      socialTab: '第三方',
      methodLabel: '登入方式',
      email: '電郵',
      phone: '手機號',
      password: '密碼',
      code: '驗證碼',
      submit: '登入',
      submitting: '登入中...',
      googleBtn: 'Google',
      appleBtn: 'Apple ID',
      forgotPassword: '忘記密碼?',
      noAccount: '還沒有帳號?',
      register: '立即註冊',
      socialDivider: '或使用以下帳號繼續',
      processing: '處理中...',
      socialNotConfigured: '該登入方式即將上線',
      googleUnsupportedBrowserTitle: '請在瀏覽器中開啟',
      googleUnsupportedBrowserMessage: '目前正在 {browser} 內建瀏覽器中開啟,Google 登入會被攔截。請點擊右上角選單選擇「在瀏覽器開啟」,或複製連結到 Safari / Chrome 後再使用 Google 登入。你也可以先使用電郵或手機號登入。'
    },
    phone: {
      countryCode: '國家/地區區號',
      nationalPlaceholder: '請輸入手機號',
      required: '請輸入手機號',
      invalid: '請輸入有效的國際手機號'
    },
    validation: {
      emailRequired: '請輸入電郵',
      emailInvalid: '電郵格式不正確',
      codeRequired: '請輸入驗證碼',
      codeLength: '請輸入 6 位驗證碼',
      termsRequired: '請閱讀並同意服務條款',
      realNameRequired: '請輸入真實姓名'
    },
    register: {
      title: '註冊 Mandarly',
      methodLabel: '註冊方式',
      socialDivider: '或使用以下帳號快速註冊',
      iAmStudent: '我是學生',
      iAmTeacher: '我是教師',
      studentHint: '註冊學生帳號,領取體驗課並開始預約老師。',
      teacherEntryText: '想以老師身份加入 Mandarly?',
      teacherEntryAction: '申請成為老師',
      teacherReviewNotice: '教師註冊需通過資質審核(平均 1-3 個工作日),與學生註冊不同入口。',
      teacherIntro: {
        heading: '加入 Mandarly 教師團隊',
        points: [
          '面向香港及海外中文學習者提供 1v1 中文口語課',
          '收入按次結算,自主排課',
          '註冊後需提交資質審核(普通話證書 / 學歷 / 教學經驗)'
        ]
      },
      nickname: '暱稱(選填)',
      realName: '真實姓名',
      realNamePlaceholder: '請輸入您的真實姓名',
      referralCode: '推薦碼(選填)',
      agreeTerms: '我已閱讀並同意 {terms} 與 {privacy}',
      termsLabel: '服務條款',
      privacyLabel: '隱私政策',
      submit: '註冊',
      haveAccount: '已有帳號?',
      goLogin: '去登入'
    },
    resetPassword: {
      title: '重置密碼',
      email: '電郵',
      code: '驗證碼',
      newPassword: '新密碼',
      confirmPassword: '確認新密碼',
      confirmPasswordRequired: '請再次輸入密碼',
      passwordMismatch: '兩次密碼不一致',
      sendCode: '發送驗證碼',
      sentTo: '驗證碼已發送至 {email}',
      next: '下一步',
      success: '密碼重置成功,請重新登入',
      unavailable: '該功能暫未開放',
      submit: '提交',
      backToLogin: '返回登入'
    },
    code: {
      send: '發送驗證碼',
      resend: '重新發送 ({n}s)',
      sent: '已發送',
      cooldown: '請稍後再試'
    },
    callback: {
      processing: '正在登入,請稍候...',
      failed: '登入失敗,請重試'
    },
    profile: {
      title: '用戶中心',
      basicInfo: '基本資料',
      nickname: '暱稱',
      avatar: '頭像',
      locale: '語言',
      timezone: '時區',
      bindings: '帳號綁定',
      bindEmail: '綁定電郵',
      bindPhone: '綁定手機',
      bindGoogle: '綁定 Google',
      bindApple: '綁定 Apple',
      unbind: '解綁',
      boundAt: '綁定時間',
      save: '儲存',
      saved: '儲存成功',
      referralCode: '我的推薦碼'
    },
    error: {
      invalidCredentials: '電郵或密碼錯誤',
      codeExpired: '驗證碼已過期',
      codeInvalid: '驗證碼不正確',
      codeCooldown: '請求過於頻繁,請稍後再試',
      emailExists: '該電郵已註冊',
      phoneExists: '該手機號已註冊',
      teacherEmailExists: '該電郵已被使用,請使用其他電郵完成教師註冊',
      teacherPhoneExists: '該手機號已被使用,請使用其他手機號完成教師註冊',
      userNotFound: '帳號不存在',
      userFrozen: '帳號已凍結,請聯繫客服',
      referralInvalid: '推薦碼無效',
      socialNotConfigured: '該登入方式暫不可用',
      socialFetchFailed: '第三方服務暫時不可用,請改用其他方式',
      tokenExpired: '登入已過期',
      smtpNotConfigured: '郵件服務暫不可用',
      passwordWeak: '密碼至少 8 位,包含字母 + 數字',
      rateLimitDaily: '今日發送次數已達上限',
      loginFailedLocked: '登入失敗次數過多,請 15 分鐘後再試',
      ipRegisterLimit: '註冊請求過於頻繁',
      socialAccountConflict: '該帳號已綁定其他用戶'
    },
    heroQuote: {
      login: '歡迎回來 · 繼續你的中文學習之旅',
      register: '加入全球 200+ 位母語老師 · 開啟自信開口說中文'
    }
  },
  authHero: {
    login: { title: '與全球母語老師 1v1 學中文', subtitle: '600+ 認證老師 · 20+ 國家學員' },
    register: { title: '30 秒註冊,開啟你的中文學習之旅', subtitle: '註冊即送 1 節免費體驗課' },
    teacherRegister: { title: '加入 Mandarly,把你的中文教給世界', subtitle: '靈活授課 · 全球學員 · 週結算' },
    resetPassword: { title: '別擔心,重設密碼很簡單', subtitle: '' },
    trust: {
      rating: '⭐ {rating} 平均評分',
      teachers: '{count}+ 認證老師',
      countries: '{count}+ 國家學員'
    }
  },
  level_check: {
    q1: {
      text: '你目前的中文水平是?',
      opt: {
        complete_beginner: '完全零基礎 — 從未學過',
        some_basics: '學過一點 — 認識一些字詞',
        simple_conversation: '可以進行簡單對話',
        fairly_fluent: '比較流利 — 想精進'
      }
    },
    q2: {
      text: '你的主要學習目標是?',
      opt: {
        career: '求職 / 工作',
        business: '商務溝通',
        travel: '旅行 / 生活',
        hsk_exam: '準備 HSK 考試',
        for_kids: '給孩子學',
        just_for_fun: '純粹興趣'
      }
    },
    q3: {
      text: '你計劃每週上幾節課?',
      opt: {
        one_per_week: '每週 1 節',
        two_per_week: '每週 2 節',
        intensive: '強化(每週 3 節及以上)',
        not_sure: '還不確定'
      }
    },
    q4: {
      text: '學習者是誰?',
      opt: {
        myself: '我自己',
        child: '孩子(12 歲以下)',
        teen: '青少年(12-18 歲)',
        multiple: '多人'
      }
    }
  },
  review: {
    title: { write: '為這節課寫評價', edit: '修改評價' },
    subtitle: {
      write: '分享你的上課體驗,幫助其他同學選老師',
      edit: '可在首次提交後 24 小時內修改'
    },
    courseTime: '上課時間 {time}',
    step1: { title: '整體評分', guide: '請給老師打分(支援半星)' },
    step2: { title: '具體回饋' },
    step3: { title: '確認提交' },
    rating: { label: '評分' },
    rate: {
      text: { 1: '很差', 2: '差', 3: '一般', 4: '好', 5: '非常棒' }
    },
    label: { rating: '評分', content: '評價內容', tags: '亮點標籤' },
    placeholder: { content: '說一兩句課程感受...(選填)' },
    ratingDesc: {
      1: '一般', 2: '不太好', 3: '還可以', 4: '不錯', 5: '很棒'
    },
    tags: {
      addCustom: '自訂標籤',
      customLimit: '最多 {n} 條 × {len} 字元',
      customLimitReached: '已達自訂標籤上限({n} 條)',
      customPlaceholder: '輸入自訂標籤(≤{n} 字)',
      maxHint: '至多選 {n} 項',
      remove: '移除 {tag}'
    },
    text: { label: '評價內容(選填)', placeholder: '具體說說這節課吧...' },
    anonymous: { label: '匿名發布', desc: '公開列表展示為「匿名學員」' },
    editWindow: {
      expired: '編輯視窗已過',
      remaining: '可編輯剩餘 {hours}h',
      urgent: '可編輯剩餘 {hours} 分鐘'
    },
    action: { cancel: '取消', next: '下一步', prev: '上一步' },
    submit: '提交評價',
    submitting: '提交中…',
    hint: {
      tagsLimit: '最多選 3 個',
      editWindowLeft: '剩 {hours}h 可修改',
      expired: '修改視窗已過期'
    },
    btn: {
      write: '寫評價', edit: '修改評價',
      submit: '提交評價', save: '儲存修改', back: '返回'
    },
    error: {
      notFinished: '只有已完成的課才能評價',
      tooManyTags: '最多選 3 個標籤',
      editExpired: '修改視窗已過期(限首次提交後 24h 內)',
      submitFailed: '提交失敗,請重試',
      loadFailed: '載入失敗,請重試'
    },
    success: {
      submitted: '評價已提交',
      updated: '評價已更新',
      title: '評價已提交',
      subtitle: '感謝你的回饋,72h 內可編輯',
      redirect: '即將返回我的課',
      backToOrders: '返回我的課'
    },
    empty: { teacherList: '該老師暫無評價' },
    publicSection: {
      title: '學生評價({n})',
      loadMore: '載入更多',
      end: '— 已載入全部 —'
    },
    card: {
      starsAria: '{n} 星',
      submittedAt: '評價於 {time}',
      editedAt: '編輯於 {time}',
      youWrote: '你的評價'
    },
    tag: {
      patient: '耐心',
      native_accent: '口音道地',
      good_pace: '節奏好',
      well_prepared: '備課充分',
      audio_issue: '聲音問題',
      late: '遲到',
      interactive: '互動好',
      good_material: '選材合適',
      std_pronunciation: '發音標準',
      humorous: '幽默',
      encouraging: '鼓勵學生',
      proper_difficulty: '難度合適'
    }
  },
  myPackages: {
    title: '我的套餐',
    subtitle: '持有的課程套餐與歷史購買',
    tabs: { all: '全部', active: '可用', expired: '已過期', exhausted: '已用完' },
    empty: {
      all: '還沒有套餐,先去預約一節課吧',
      active: '沒有可用套餐',
      expired: '沒有已過期的套餐',
      exhausted: '沒有用完的套餐'
    },
    loadFailed: '載入失敗,請重試',
    browseTeachers: '瀏覽教師',
    card: {
      remaining: '剩餘 {n} 節',
      total: '共 {n} 節',
      weekly: '每週 {n} 節',
      expireAt: '有效期至 {date}',
      purchasedAt: '取得時間 {date}',
      free: '免費贈送',
      statusActive: '可用',
      statusExpired: '已過期',
      statusExhausted: '已用完'
    },
    source: {
      purchase: '購買',
      free_trial: '註冊贈送',
      register_grant: '註冊贈送',
      admin_grant: '營運贈送',
      referral_reward: '推薦獎勵'
    }
  },
  profileTeacher: {
    stat: {
      title: '我的教學統計',
      avgRating: '平均評分',
      reviewCount: '收到評價',
      orderCount: '完成訂單',
      noReviews: '— 暫無 —',
      loadFailed: '統計載入失敗'
    },
    myReviews: {
      title: '我收到的評價',
      empty: '還沒有學生給你寫評價',
      loadMore: '載入更多',
      end: '— 已載入全部 —'
    }
  },
  payment: {
    button: {
      checkout: '立即購買',
      retry: '重試'
    },
    processing: {
      title: '支付處理中',
      tip: '請稍候,我們正在確認你的支付...'
    },
    success: {
      title: '支付成功！',
      message: '套餐已到帳,開始預約你的課程吧！'
    },
    cancel: {
      title: '支付已取消',
      message: '你取消了支付流程,套餐未購買。',
      retry: '重新購買',
      back: '返回我的套餐'
    },
    expired: {
      title: '支付處理中,請稍後查看',
      message: '支付結果可能需要幾秒同步。如果你已經完成扣款,套餐到帳後會出現在我的套餐。'
    },
    amount: {
      usd: '退款金額 (USD)'
    },
    discount: {
      applied: '推薦碼折扣已套用'
    },
    status: {
      pending: '待支付',
      paid: '已支付',
      failed: '支付失敗',
      expired: '已過期',
      refunded: '已退款',
      partial_refunded: '部分退款'
    },
    viewMyPackages: '查看我的套餐'
  },
  refund: {
    apply: {
      button: '申請退款',
      title: '申請退款',
      reason: {
        label: '退款原因',
        placeholder: '請說明退款原因（至少 10 個字）',
        min10: '退款原因至少需要 10 個字'
      },
      pendingButton: '退款審核中',
      success: '退款申請已提交,等待審核',
      error: '提交失敗,請稍後重試'
    },
    myList: {
      title: '我的退款記錄',
      empty: '暫無退款記錄'
    },
    status: {
      pending: '審核中',
      approved: '已批准',
      refunded: '已到帳',
      rejected: '已拒絕',
      failed: '退款失敗'
    },
    auditNote: {
      label: '審核備註'
    }
  },
  referral: {
    title: '我的推薦戰績',
    code: {
      your: '我的推薦碼',
      copy: '複製',
      copied: '推薦碼已複製'
    },
    stat: {
      invited: '已邀請人數',
      reward: '累計獎勵課次'
    },
    discount: {
      applied: '推薦碼折扣已套用'
    },
    tip: {
      invite: '邀請好友註冊並購買套餐,你將獲得免費體驗課',
      firstOrder: '好友首單可享折扣',
      howItWorks: '好友首次購買套餐後,雙方均得獎勵'
    }
  },
  package: {
    title: '選擇套餐',
    subtitle: '購買課程套餐,開始你的中文學習之旅',
    empty: '暫無可購套餐,請聯絡客服',
    buyNow: '立即購買',
    weeklyCount: '每週課次',
    validityWeeks: '總課次',
    priceUsd: '價格',
    referralCode: {
      placeholder: '輸入推薦碼（選填）'
    }
  },
  packages: {
    title: '課程套餐',
    subtitle: '按你的學習節奏選擇,登入後查看完整價格',
    pricing: {
      eyebrow: '適合每位學習者的彈性方案',
      titlePrefix: '為你的',
      titleHighlight: '中文',
      titleSuffix: '學習目標選擇合適套餐',
      subtitle: '',
      lessonFormat: '所有套餐均包含 30 分鐘 1 對 1 私教中文課,由母語中文老師授課。',
      lessonFormatParts: {
        prefix: '所有套餐均包含 ',
        duration: '30 分鐘',
        middle1: ' ',
        oneOnOne: '1 對 1',
        middle2: ' ',
        private: '私教',
        suffix: ' 中文課,由母語中文老師授課。'
      },
      pills: {
        tutors: '母語中文老師',
        scheduling: '彈性預約時間',
        personalized: '個人化學習',
        secure: '安全可信賴'
      },
      badge: {
        mostPopular: '最受歡迎',
        bestValue: '最具性價比'
      },
      priceLocked: '登入後查看價格',
      perLesson: '/ 節',
      lessonsCount: '{n} 節課',
      durationMonths: '{n} 個月',
      durationDays: '{n} 天',
      ctaLoggedOut: '預約免費試聽',
      ctaLoggedIn: '選擇套餐',
      ctaLoggedInPlan: '選擇{plan}',
      ctaDefaultPlan: '套餐',
      cards: {
        starter: {
          name: '入門套餐',
          ctaName: '入門套餐',
          subtitle: '適合輕量學習者',
          point1: '每週 1 節課',
          point2: '彈性預約',
          point3: '母語老師授課',
          point4: '零基礎友好'
        },
        standard: {
          name: '標準套餐',
          ctaName: '標準套餐',
          subtitle: '適合穩定進步',
          point1: '每週 2 節課',
          point2: '提升更快',
          point3: '優先預約',
          point4: '個人化學習路徑'
        },
        annualLite: {
          name: '年度套餐',
          ctaName: '年度套餐',
          subtitle: '適合持續學習者',
          point1: '每週 1 節課',
          point2: '長期穩定學習',
          point3: '更好鞏固效果',
          point4: '年付更省'
        },
        premiumAnnual: {
          name: '尊享年度套餐',
          ctaName: '尊享年度套餐',
          subtitle: '適合長期深度學習者',
          point1: '每週 2 節課',
          point2: '長期價值更高',
          point3: '專屬學習支援',
          point4: '家長進度回饋'
        }
      },
      why: {
        titlePrefix: '為什麼選擇',
        titleHighlight: 'Mandarly?',
        native: {
          title: '母語老師',
          desc: '嚴選並認證的中文老師。'
        },
        schedule: {
          title: '彈性預約',
          desc: '跨時區自由選擇合適課時。'
        },
        personalized: {
          title: '個人化學習',
          desc: '課程圍繞你的目標與學習風格設計。'
        },
        progress: {
          title: '結構化進步',
          desc: '持續追蹤提升,一步步達成目標。'
        }
      },
      testimonials: {
        title: '全球學習者喜愛的中文課',
        emily: {
          name: 'Emily, California',
          quote: '我的女兒終於開始喜歡學中文了!'
        },
        kevin: {
          name: 'Kevin, Singapore',
          quote: '課程彈性、專業,課堂也很有參與感。'
        },
        sophie: {
          name: 'Sophie, London',
          quote: '老師很棒,學習路徑也清晰,非常推薦!'
        }
      }
    },
    banner: {
      hero: '套餐價格 · 靈活按月或一年承擔',
      freeTrial: {
        text: '註冊即送 1 節免費體驗課(25 分鐘)',
        subtext: '自由選老師 · 無需綁定信用卡',
        cta: '立即領取',
        claimed: '您已領取過免費體驗課'
      }
    },
    teacherEntry: {
      eyebrow: '繼續預約',
      title: '為這位老師選擇合適的套餐',
      titleWithName: '為 {name} 選擇合適的套餐',
      desc: '購買課時後可回到老師詳情頁,繼續完成剛才選擇的 1 對 1 中文課預約。',
      backToTeacher: '返回老師詳情'
    },
    currency: { label: '幣種', hkd: 'HKD', usd: 'USD', cny: 'CNY' },
    rules: {
      title: '套餐規則',
      multiPackage: '多套餐管理:可同時持有多個套餐,預約時優先扣即將過期的課次',
      expiry: '到期作廢:套餐有效期內未使用的課次,到期後作廢(到期前 7/3/1 天提醒)',
      refund: '退款規則:課前 24 小時取消可全額退,詳見服務條款 § 5',
      currency: '幣種說明:套餐按自身標註幣種收款,後台可按幣種獨立上架或下架'
    },
    compare: {
      title: '完整套餐對比', expand: '展開對比表', collapse: '收起',
      buy: '立即購買', loginToViewPrice: '登入查看價格', loginAction: '登入 / 註冊', priceLockedNote: '登入後查看各套餐完整價格,套餐權益仍可對比', yes: '✓', no: '—', free: '免費',
      durationMonths: '{n} 個月', durationDays: '{n} 天',
      col: { package: '套餐' },
      row: {
        pricePerLesson: '單節價格', totalPrice: '總價', totalLessons: '總課次',
        validity: '有效期', booking: '自由預約', refund: '24h 退款',
        upgrade: '升級 / 降級費', discount: '折扣', action: '操作'
      }
    },
    confirm: {
      title: '確認訂單', summary: '套餐摘要',
      tip: '點擊「前往支付」將跳轉 Stripe 安全支付頁面',
      refundTip: '課前 24 小時取消可全額退款,詳見',
      termsLink: '服務條款 § 5',
      cancel: '取消', confirm: '前往支付', confirming: '處理中…',
      durationMonths: '{n} 個月(自購買日起)', durationDays: '{n} 天',
      sessionsCount: '約 {n} 節', sessionsCountWeekly: '約 {n} 節({weekly} 節/週)',
      field: {
        package: '套餐', sessions: '課次', duration: '有效期',
        currency: '幣種', subtotal: '小計', discount: '優惠', total: '合計'
      }
    },
    success: { message: '套餐已到帳', returnTo: '返回繼續預約' },
    empty: {
      title: '暫無可購套餐,請聯絡客服',
      desc: '套餐已售罄或正在維護',
      currencyUnavailable: '目前暫無 {currency} 可購套餐,可先查看 HKD 套餐',
      viewHkd: '查看 HKD 套餐'
    },
    error: { loadFailed: '套餐載入失敗,請重試', checkoutFailed: '發起支付失敗,請稍後重試' }
  },
  seo: {
    home: {
      title: '面向兒童與成人的線上中文課 | Mandarly',
      description: 'Mandarly 提供面向兒童、青少年和成人的線上一對一中文課,由友善的中文母語老師幫助學生提升普通話口語。',
      keywords: '線上中文課,中文口語課,兒童普通話課,一對一普通話課,線上學中文,中文母語老師'
    },
    teachers: {
      title: '中文老師 · 1 對 1 線上授課 · Mandarly',
      description: '瀏覽 Mandarly 嚴選的母語中文老師,可按口語、商務中文、HSK、兒童中文篩選。選你方便的時段預約 1 對 1 課程,新用戶可免費試課。',
      keywords: '中文老師,普通話老師,線上中文課,1對1中文,商務中文老師,HSK 老師'
    },
    packages: {
      title: '價格方案 · 彈性中文課程套餐 · Mandarly',
      description: '選擇適合兒童、零基礎與全球學習者的彈性中文課程套餐,在 Mandarly 與母語老師進行 1 對 1 學習。',
      keywords: '中文課價格,線上中文套餐,1對1中文課,中文培訓價格,普通話課程',
      catalogName: 'Mandarly 中文課程方案'
    },
    teacherDetail: {
      titleTemplate: '{name} 老師 · 中文 1 對 1 教學 · Mandarly',
      titleFallback: '中文老師 · 1 對 1 線上授課 · Mandarly',
      descriptionTemplate: '在 Mandarly 預約 {name} 老師的 1 對 1 中文課。{intro}',
      descriptionFallback: '在 Mandarly 預約這位母語中文老師的 1 對 1 課程,選你方便的時段開課。'
    },
    levelCheck: {
      title: '免費中文水平測驗 · 30 秒配對老師 · Mandarly',
      description: 'Mandarly 免費中文水平測驗,30 秒答完 4 道題,智能配對母語老師與個人化課程方案。無需註冊,無需信用卡。',
      keywords: '中文水平測驗,普通話測驗,HSK 等級測驗,免費中文測驗,中文分級'
    },
    levelCheckResult: {
      title: '中文水平測驗結果 · Mandarly',
      description: '查看你的中文水平測驗結果,註冊後繼續完成學習路徑。'
    },
    curriculum: {
      title: '線上中文課程體系 | Mandarly 普通話口語課程',
      description: '了解 Mandarly 線上中文課程體系,從零基礎中文到高級普通話表達,適合兒童、青少年和成人的一對一課程。',
      keywords: '線上中文課程體系,普通話口語課程,零基礎中文,HSK中文課,兒童中文課'
    }
  },
  curriculum: {
    hero: {
      eyebrow: '課程體系',
      title: 'Mandarly 中文課程體系',
      subtitle: '從零基礎中文到自信普通話溝通,一條清晰的學習路徑。',
      cta: '預約試聽課'
    },
    overview: {
      eyebrow: '課程總覽',
      title: '圍繞真實口語進步設計的課程路徑',
      desc: 'Mandarly 的線上中文課程體系專為海外學習者設計,通過一對一普通話口語課,幫助學生從基礎發音和日常對話逐步提升到高級中文溝通。'
    },
    labels: {
      learners: '適合學生',
      goal: '學習目標',
      skills: '技能概覽'
    },
    stages: [
      {
        name: '零基礎中文入門',
        chineseName: '發音與拼音起步',
        learners: '完全沒有中文基礎,或只會「你好」「謝謝」等簡單詞語的學生。',
        goal: '建立中文學習興趣,掌握基礎發音,能夠完成簡單問候和自我介紹。',
        hsk: 'Pre-HSK / HSK 1',
        skills: ['中文發音基礎', '拼音入門', '普通話四聲練習', '問候與自我介紹', '數字、年齡、家庭、顏色、食物'],
        button: '了解零基礎中文'
      },
      {
        name: '中文基礎提升',
        chineseName: '日常會話基礎',
        learners: '學過一點中文、認識拼音、能說簡單句子但表達還不夠流利的學生。',
        goal: '建立穩定基礎,擴大詞彙量,學習基礎句型,能夠圍繞日常生活進行簡單對話。',
        hsk: 'HSK 1-2',
        skills: ['拼音與聲調鞏固', '日常會話練習', '基礎句型', '聽力理解', '簡短表達任務'],
        button: '了解基礎普通話'
      },
      {
        name: '中級中文表達',
        chineseName: '主題表達進階',
        learners: '具備基礎中文能力,想提高表達完整度、聽力理解和口語流利度的學生。',
        goal: '提升圍繞生活、學習、旅行、文化等話題進行完整自然交流的能力。',
        hsk: 'HSK 2-4',
        skills: ['流利度口語訓練', '長句表達訓練', '主題式對話', '講故事練習', '表達觀點'],
        button: '了解中級中文'
      },
      {
        name: '高級中文溝通',
        chineseName: '深度溝通提升',
        learners: '已有較好中文基礎,希望提升表達深度、文化理解、演講、寫作或考試能力的學生。',
        goal: '從「會說中文」提升到「說得自然、有邏輯、有深度」。',
        hsk: 'HSK 4-6+',
        skills: ['高級中文對話', '觀點討論與辯論', '中文演講能力', '新聞與文化話題', 'HSK 考試支持'],
        button: '了解高級普通話'
      }
    ],
    bottomCta: {
      title: '不確定適合哪個階段?',
      desc: '預約試聽課,老師會根據年齡、基礎、目標和口語能力推薦合適課程階段。',
      button: '預約試聽課'
    }
  },
  comingSoon: {
    badge: '敬請期待',
    cta: { backHome: '返回首頁' },
    textbook: {
      title: '教材正在編排中',
      desc: '我們正與教研老師一起整理面向不同水平的精選教材,完成後將在此發佈配套講義與練習。'
    },
    resources: {
      title: '資源中心即將上線',
      desc: '學習指南、詞彙卡、文化短文與免費練習材料將陸續上傳,敬請關注。'
    }
  },
  header: {
    nav: {
      home: '首頁',
      curriculum: '課程體系',
      teachers: '老師',
      packages: '價格',
      levelCheck: '等級測試',
      myOrders: '我的課',
      // D26:未登錄態 home 錨點 nav
      features: '教學特色',
      recommendedTeachers: '推薦老師',
      packagesSection: '課程套餐',
      stories: '學員故事',
      faq: '常見問題',
      // D27:外部內容入口
      textbook: '教材',
      resources: '資源中心',
      podcast: '播客',
      // D27:抽屉分組父項
      aboutGroup: '了解 Mandarly',
      resourcesGroup: '資源'
    },
    action: {
      login: '登入',
      register: '註冊',
      becomeTeacher: '成為老師'
    },
    menu: {
      profile: '個人中心',
      profileTeacher: '個人資料',
      myPackages: '我的套餐',
      myOrdersTeacher: '我的訂單',
      income: '我的收入',
      withdrawal: '申請提現',
      myRefunds: '我的退款',
      logout: '登出'
    }
  },
  tabbar: {
    home: '首頁',
    teachers: '老師',
    myOrders: '我的課',
    profile: '我的'
  },
  footer: {
    section: {
      about: '關於我們',
      service: '服務',
      legal: '法律',
      contact: '聯絡',
      payment: '支持的付款方式',
      follow: '關注我們'
    },
    about: {
      intro: '公司簡介',
      jobs: '加入我們',
      teacherJoin: '成為老師',
      contact: '聯絡方式'
    },
    legal: {
      privacy: '隱私政策',
      terms: '服務條款'
    },
    social: {
      followOn: '在 {name} 關注我們',
      wechatScan: '掃碼透過微信聯絡我們',
      wechatZoomHint: '點擊放大查看微信二維碼'
    },
    copyright: '© 2026 曼德勵科技有限公司 · MANDARLY TECHNOLOGY LIMITED · 香港'
  },
  support: {
    open: '打開客服',
    close: '關閉客服',
    title: 'Mandarly 客服',
    subtitle: '微信 · WhatsApp · Email',
    directHint: '請選擇一個渠道,客服團隊會盡快跟進。',
    greeting: 'Hi! 輸入您的問題,我幫您查找答案。或直接聯絡真人客服。',
    placeholder: '輸入問題,按 Enter 發送',
    send: '發送',
    noMatch: '暫時沒有找到合適答案,您可以聯絡真人客服。',
    wechatCopied: '微信號已複製',
    wechatCopyFailed: '複製失敗,請手動複製微信號:{id}',
    openFailed: '無法開啟連結,請稍後重試或截圖聯絡運營。',
    error: '客服暫時不可用,請稍後再試。',
    channel: {
      wechat: '微信',
      whatsapp: 'WhatsApp',
      email: 'Email',
      other: '聯絡方式',
      pending: '該聯絡方式正在配置中。'
    }
  },
  teacherDetail: {
    hero: {
      rating: '評分',
      reviews: '{count} 條評價',
      todayAvailable: '今日可約 {count} 節',
      favorite: '收藏'
    },
    video: { title: '試講影片', placeholder: '點擊播放' },
    intro: { title: '關於我', more: '展開', less: '收起' },
    expertise: { title: '教學專長' },
    book: {
      title: '預約',
      priceUnit: '/ {n} 分鐘',
      selectSlot: '選擇上課時段',
      selectedSlot: '已選',
      bookNow: '立即預約',
      buyPackageForTeacher: '購買套餐(為本老師預約)',
      viewOtherTeachers: '查看其他老師',
      emptyHint: '請先選擇時段',
      packageCreditPricing: '按套餐課次預約',
      loginToViewPackages: '登入後查看套餐',
      refundTip: '課前 24 小時取消可全額退款'
    },
    review: {
      title: '學員評價 ({n})',
      empty: '暫無評價 · 成為第一位評價的學員',
      lowVolume: '期待你成為第一位評價的學員',
      seeAll: '載入更多(還有 {count} 條)'
    },
    related: {
      title: '相關老師',
      viewAll: '查看全部',
      emptyTitle: '暫時沒有更多相關老師',
      emptyDesc: '可以回到老師列表,按時間、口音或學習目標重新篩選。',
      emptyAction: '瀏覽全部老師',
      loadFailed: '相關老師暫時載入失敗'
    },
    balance: {
      insufficient: '您的套餐課次不足,前往購買?',
      goPackages: '前往購買'
    }
  },
  teacherCard: {
    badge: { todayAvailable: '今日可約', newTeacher: '新教師', introVideo: '影片' },
    avatarAlt: '教師 {name} 頭像',
    favorite: '收藏',
    favoriteSoon: '敬請期待',
    priceUnit: '/ 25 分鐘',
    reviewCount: '({count} 則評價)',
    noReviews: '暫無評價',
    finishedLessons: '已完成 {count} 節',
    newTeacherQualified: '新教師 · 已通過資質審核',
    timezoneMeta: '{location} · {offset}'
  },
  packageCard: {
    cta: { buy: '立即購買', claimTrial: '立即領取', loginToViewPrice: '登入查看價格', loginRegister: '登入 / 註冊', loading: '載入中…' },
    purchased: '已購',
    perSessionUnit: '/ 節',
    sessions: '{n} 節',
    period: { months: '{n} 個月有效', days: '{n} 天有效' }
  },
  profile: {
    title: '個人中心',
    user: { guest: '未登入' },
    menu: {
      account: '帳戶',
      referral: '推薦戰績',
      teacherStats: '教學統計',
      myPackages: '我的套餐',
      myOrders: '我的訂單',
      myRefunds: '我的退款',
      logout: '退出登入'
    },
    account: {
      saved: '儲存成功',
      section: { basic: '基本資料', bindings: '帳號綁定', password: '修改密碼', danger: '危險區' },
      field: { nickname: '暱稱', email: '電郵', locale: '語言', timezone: '時區', referralCode: '我的推薦碼' },
      action: { save: '儲存', changeAvatar: '更換頭像' },
      avatar: {
        uploading: '上傳中...',
        uploadSuccess: '頭像更新成功',
        uploadFailed: '頭像上傳失敗,請重試',
        invalidType: '請上傳圖片檔案(PNG / JPG / WebP / GIF)',
        tooLarge: '圖片不能超過 {mb}MB'
      },
      bindings: {
        email: '電郵', phone: '手機', google: 'Google', apple: 'Apple',
        bound: '已綁定', notBound: '未綁定', bindNow: '立即綁定',
        bindEmail: '綁定電郵', bindPhone: '綁定手機', unbind: '解綁',
        unbindConfirm: '確定要解綁該登入方式嗎?', unbindSuccess: '解綁成功',
        bindEmailSuccess: '電郵綁定成功', bindPhoneSuccess: '手機綁定成功',
        primaryEmail: '主電郵'
      },
      password: {
        current: '當前密碼', new: '新密碼', confirm: '確認新密碼', action: '修改密碼',
        required: '請填寫完整密碼', mismatch: '兩次輸入的新密碼不一致',
        success: '密碼修改成功', comingSoon: '修改密碼功能即將上線'
      },
      logout: {
        title: '退出登入',
        desc: '退出當前帳號,未支付訂單不會受影響。',
        confirm: '確認要退出當前帳號嗎?未支付訂單不會受影響。',
        confirmBtn: '確認退出',
        success: '已退出登入'
      }
    },
    referral: {
      title: '推薦戰績',
      codeLabel: '推薦碼', codeCopied: '推薦碼已複製',
      linkLabel: '你的邀請連結', linkCopied: '邀請連結已複製',
      copy: '複製', copied: '已複製',
      shareLabel: '分享:',
      shareText: '和我一起在 Mandarly 學中文,用我的連結註冊有優惠 → {link}',
      recordsTitle: '邀請記錄',
      stat: { invited: '已邀請', rewarded: '已獎勵', totalReward: '累計獎勵' },
      status: { rewarded: '已獎勵', pending: '待支付' },
      empty: {
        title: '還沒有邀請過朋友',
        desc: '複製邀請連結分享給好友,首單成功後雙方都有獎勵',
        action: '複製邀請連結'
      }
    },
    teacherStats: {
      title: '教學統計',
      stat: { avgRating: '平均評分', lessonCount: '已完成課時', reviewCount: '評價數' },
      monthlyTrend: '本月趨勢',
      monthlyPlaceholder: '本月資料匯總即將上線,屆時將展示本月完成課時與待上課次。'
    }
  },
  orders: {
    title: '我的課',
    loadFailed: '載入失敗,請稍後重試',
    tabs: {
      upcoming: '待上課', toReview: '待評價', finished: '已完成',
      cancelled: '已取消', refunding: '退款中', all: '全部'
    },
    empty: {
      upcoming: { title: '暫無待上課訂單', desc: '去看看老師,預約第一堂課吧' },
      toReview: { title: '沒有需要評價的課', desc: '完成的課程會出現在這裡' },
      finished: { title: '還沒有完成過課程', desc: '完成的課程會出現在這裡' },
      cancelled: { title: '沒有已取消訂單', desc: '保持出席率有助於學習連續性' },
      refunding: { title: '沒有退款中訂單', desc: '退款進度會在這裡即時同步' },
      all: { title: '暫無訂單', desc: '去看看老師,預約第一堂課吧' },
      browseTeachers: '瀏覽老師'
    },
    card: {
      scheduledAt: '上課時間', tzSuffix: '{tz} 時區', duration: '{n} 分鐘',
      package: '套餐', packageFallback: '單次預約', freeTrialBadge: '免費體驗',
      cancelReason: '取消原因',
      refundedClass: '✓ 課次已返還', notRefundedClass: '✗ 課次未返還',
      refundProgress: '退款進度', channelRefundId: '退款流水號',
      status: {
        upcoming: '待上課', finished: '已完成', finishedSettling: '等待結算',
        finishedReview: '待評價', cancelled: '已取消', refunded: '已退款',
        refunding: '退款中', no_show_student: '未出席', no_show_teacher: '老師缺席',
        abnormal: '異常', reviewExpired: '編輯視窗已過'
      },
      action: {
        enterClass: '進入課堂', returnClass: '重返課堂',
        review: '★ 寫評價', editReview: '編輯評價',
        cancel: '取消預約', viewDetail: '查看詳情'
      }
    },
    countdown: {
      daysHours: '{days} 天 {hours} 小時',
      hoursMinutes: '{hours} 小時 {minutes} 分',
      minutesSeconds: '{minutes} 分 {seconds} 秒',
      inProgress: '上課中', expired: '已結束'
    },
    refundWindow: {
      over24h: 'ⓘ 24 小時前取消可全額退',
      under24h: '⚠️ 距上課不足 24 小時,取消課次不返還'
    },
    refundProgress: { applied: '已申請', reviewing: '審核中', refunded: '已退款' },
    cancelDialog: {
      title: '取消預約',
      summary: { teacher: '老師', time: '上課時間', distance: '距上課' },
      countdown24Plus: '✓ 距上課 ≥ 24h,取消後課次將全額返還',
      countdown24Minus: '⚠️ 距上課不足 24h,取消課次將不返還',
      alreadyStarted: '課程已開始,無法取消',
      reasonLabel: '取消原因(選填,幫助我們改進)',
      reasonPlaceholder: '請選擇取消原因',
      notePlaceholder: '補充說明(可選)',
      reasonOther: '其它',
      reason: {
        conflict: '時間衝突', illness: '身體不適', travel: '臨時出行',
        noShow: '老師未確認', changedMind: '改變主意'
      },
      cancel: '保留預約', confirm: '確認取消', confirming: '提交中…',
      success: '已取消', error: '取消失敗,請稍後重試'
    }
  },
  teacherCenter: {
    nav: {
      dashboard: '工作台',
      profileEdit: '我的檔案',
      qualification: '資質材料',
      schedule: '排課',
      orders: '訂單',
      income: '收入',
      withdrawal: '提現'
    },
    audit: {
      banner: {
        draft: '資料尚未提交,請完善檔案與資質後提交審核',
        pending: '資料已提交,等待管理員審核',
        rejected: '審核未通過:{reason}',
        draftAction: '去完善',
        action: '去補充'
      }
    },
    dashboard: {
      title: '教師中心',
      welcome: '你好,{name}',
      weeklyClassesLabel: '本週課次',
      monthlyIncomeLabel: '本月收入',
      pendingSettleLabel: '待結算',
      ratingLabel: '評價分',
      quickActionsTitle: '快捷操作',
      quickActionSchedule: '管理排課',
      quickActionOrders: '查看訂單',
      quickActionIncome: '查看收入',
      quickActionWithdrawal: '申請提現'
    },
    profile: {
      title: '我的檔案',
      subtitle: '完善個人介紹與教學資訊,資料儲存後由管理員審核',
      intro: {
        label: '自我介紹',
        placeholder: '簡單介紹一下你的教學風格、背景與擅長的課程類型',
        hint: '建議 200~600 字,語氣親切自然,方便學生快速了解你'
      },
      avatar: {
        label: '教師頭像 / 卡片主圖',
        upload: '上傳照片',
        uploading: '上傳中...',
        hint: '會顯示在學生端老師卡片和詳情頁。建議使用清晰的正方形或 4:3 頭像照,最大 {mb} MB。'
      },
      expertise: {
        label: '教學方向',
        hint: '可多選;勾選會顯示在學生篩選條件裡'
      },
      languages: {
        label: '掌握的語言',
        placeholder: '請選擇你能教學或溝通的語言'
      },
      languageOptions: {
        zh: '中文(普通話)',
        en: '英文',
        ar: '阿拉伯文',
        yue: '粵語',
        ja: '日文',
        ko: '韓文'
      },
      accent: {
        label: '口音'
      },
      accentOptions: {
        mandarin_cn: '普通話(大陸)',
        mandarin_tw: '台灣國語',
        cantonese: '粵語',
        mixed: '其他/混合'
      },
      yearsExperience: {
        label: '教學年資',
        hint: '以整年計,無相關經驗請填 0'
      },
      introVideo: {
        label: '自我介紹影片',
        upload: '上傳介紹影片',
        replace: '更換影片',
        remove: '移除',
        hint: '支援 mp4,單檔 ≤ {mb} MB,時長 ≤ {sec} 秒'
      },
      meta: {
        qualificationCount: '已上傳資質材料',
        auditedAt: '上次審核時間'
      },
      actions: {
        save: '儲存',
        saving: '儲存中…',
        submitAudit: '提交審核',
        submitAuditConfirm: '提交'
      },
      messages: {
        loadFailed: '檔案載入失敗,請稍後重試',
        saveSuccess: '檔案已儲存',
        saveFailed: '儲存失敗,請稍後重試',
        submitAuditConfirm: '資料修改完成後再提交審核。提交後將進入待審核狀態,審核期間無法再次提交,確定繼續嗎?',
        submitAuditSuccess: '已提交,資料審核中',
        submitAuditFailed: '提交失敗,請稍後重試',
        videoFormatInvalid: '僅支援 mp4 影片',
        videoTooLarge: '影片大小不能超過 {mb} MB',
        videoTooLong: '影片時長不能超過 {sec} 秒',
        videoUnreadable: '影片讀取失敗,請更換檔案後重試',
        videoUploadSuccess: '影片上傳成功',
        videoUploadFailed: '影片上傳失敗,請稍後重試',
        avatarFormatInvalid: '請上傳圖片檔案',
        avatarTooLarge: '圖片大小不能超過 {mb} MB',
        avatarUploadSuccess: '頭像已更新',
        avatarUploadFailed: '頭像上傳失敗,請稍後重試'
      }
    },
    qualification: {
      title: '資質材料',
      subtitle: '上傳身分證件、學歷證書等材料,提交後由管理員審核',
      requiredHint: '必填:身分證件、學歷證書。選填:教師資格證、英語四級或六級證書、教學經驗證明。',
      docType: {
        label: '材料類型',
        placeholder: '請選擇材料類型',
        idCard: '身分證件',
        passport: '護照',
        degreeCert: '學歷證書',
        teachingCert: '教學證書',
        englishCert: '英語四級或六級證書',
        experienceProof: '教學經驗證明'
      },
      upload: {
        sectionTitle: '上傳一份',
        hint: '支援 jpg / png / pdf,單檔 ≤ {mb} MB',
        button: '選擇檔案並上傳',
        success: '已上傳',
        failed: '上傳失敗,請稍後重試',
        errorFormat: '僅支援 jpg / png / pdf 格式',
        errorSize: '檔案超過 {mb} MB,請壓縮後重試',
        noDocType: '請先選擇材料類型'
      },
      list: {
        sectionTitle: '已上傳材料',
        empty: '尚未上傳任何資質材料',
        auditStatus: {
          pending: '審核中',
          approved: '已通過',
          rejected: '未通過'
        },
        rejectReasonPrefix: '未通過原因:',
        delete: '刪除',
        deleteConfirm: '確定刪除這份資質材料?刪除後無法復原',
        deleteSuccess: '已刪除',
        deleteFailed: '刪除失敗,請稍後重試'
      },
      messages: {
        loadFailed: '資質材料載入失敗,請稍後重試'
      }
    },
    schedule: {
      title: '我的排課',
      subtitle: '設定每週可約時段,學生將看到對應空檔預約',
      weekViewTitle: '每週可約時段',
      weekRange: '{from} ~ {to}',
      prevWeek: '上一週',
      nextWeek: '下一週',
      currentWeek: '本週',
      timezoneLabel: '時區:{tz}',
      modeLabel: '編輯模式',
      modeTemplate: '編輯每週固定時間',
      modeTemplateDesc: '影響以後每一週,適合設定長期可約時間',
      modeException: '調整本週例外',
      modeExceptionDesc: '只影響目前這週的具體日期,適合臨時開放或關閉',
      periodLabel: '時間段',
      periodAll: '全部 07:00-23:00',
      periodMorning: '上午 07:00-12:00',
      periodAfternoon: '下午 12:00-18:00',
      periodEvening: '晚上 18:00-23:00',
      applyToNextWeek: '套用模板到下週',
      applyToNextWeekConfirm: '將本週模板套用到下週?已設定的下週例外不會被覆蓋。',
      applyToNextWeekSuccess: '已套用到下週',
      addException: '新增本週例外',
      slotAvailable: '可約',
      slotWeeklyAvailable: '每週可約',
      slotUnavailable: '不可約',
      slotException: '本週例外',
      legendAvailable: '每週可約',
      legendException: '本週例外',
      legendWeeklyAvailable: '每週固定可約',
      legendExceptionClosed: '本週臨時關閉',
      legendExceptionExtra: '本週臨時開放',
      slotClosedThisWeek: '本週關閉',
      slotOpenThisWeek: '本週開放',
      bulkWeekday: '工作日批次',
      bulkColumn: '整列勾選',
      exceptionDrawerTitle: '本週例外',
      exceptionEmpty: '本週暫無例外',
      exceptionDate: '日期',
      exceptionTime: '時間',
      exceptionAction: '動作',
      exceptionActionMake: '改為可約',
      exceptionActionBlock: '改為不可約',
      exceptionActionMakeOnce: '僅本週開放這個時間',
      exceptionActionBlockOnce: '僅本週關閉這個時間',
      exceptionAddSuccess: '已新增例外',
      exceptionRemoveSuccess: '已刪除例外',
      toggleSuccess: '已儲存',
      toggleFailed: '儲存失敗,已回滾',
      loadFailed: '排課載入失敗,請稍後重試',
      mobileDayDot: '週{day}',
      onboarding: {
        title: '第一次設定排課?',
        step1: '① 點擊下方任一時間格子,標記為「可約」(再點一次取消)',
        step2: '② 範本按週循環,設好後每週自動生效;某週臨時調整用「本週例外」',
        step3: '③ 儲存即時生效,學生會在「老師詳情」頁看到你的空檔下單',
        dismiss: '知道了,開始設定'
      }
    },
    orders: {
      title: '我的訂單',
      subtitle: '查看你的全部預約課程',
      loadFailed: '訂單載入失敗,請稍後重試',
      tabs: {
        all: '全部',
        upcoming: '待上課',
        finished: '已上課',
        cancelled: '已取消'
      },
      empty: {
        all: '暫無訂單',
        upcoming: '暫無待上課訂單',
        finished: '暫無已上課訂單',
        cancelled: '暫無已取消訂單'
      },
      card: {
        student: '學生',
        scheduledAt: '上課時間',
        duration: '時長 {n} 分鐘',
        package: '使用套餐',
        settleStatusLabel: '結算狀態',
        settleFrozen: '凍結中(T+7 解凍)',
        settleAvailable: '已入帳可提現',
        settleCancelled: '已取消不結算',
        feeLabel: '課時費',
        enterClass: '進入課堂',
        enterClassNotYet: '課前 5 分鐘開放',
        cancelledByStudent: '學生已取消',
        viewDetail: '查看詳情'
      }
    },
    income: {
      title: '我的收入',
      subtitle: '收入入帳與提現總覽',
      loadFailed: '收入載入失敗,請稍後重試',
      totalEarnedLabel: '累計入帳',
      availableLabel: '可提現餘額',
      frozenLabel: '在途 T+7',
      pendingWithdrawalLabel: '申請中凍結',
      availableHint: '已解凍可申請提現的餘額',
      frozenHint: '近期上課入帳,T+7 自動解凍',
      pendingWithdrawalHint: '有提現申請審核中,暫不可再申請',
      nextUnfreezeLabel: '最近解凍 {date}',
      applyWithdrawalBtn: '申請提現 →',
      detailTitle: '收入明細',
      filterFrom: '開始日期',
      filterTo: '結束日期',
      filterType: '收入類型',
      filterTypeAll: '全部',
      filterTypeClass: '課時收入',
      filterTypeAdjust: '人工調整',
      exportCsv: '匯出 CSV',
      column: {
        classTime: '課程時間',
        student: '學生',
        type: '類型',
        fee: '課時費(USD)',
        status: '狀態',
        unfreezeAt: '解凍時間'
      },
      statusFrozen: '凍結',
      statusAvailable: '可提現',
      statusWithdrawn: '已提現',
      empty: '暫無收入明細'
    },
    withdrawal: {
      apply: {
        title: '申請提現',
        subtitle: '填寫收款資訊,提交後由管理員人工審核打款',
        balanceLabel: '可提現餘額',
        balanceUnit: 'USD',
        amountLabel: '提現金額',
        amountUnit: 'USD',
        amountPlaceholder: '請輸入提現金額',
        amountHint: '最低提現 {min} USD,手續費由教師承擔',
        amountAllInBtn: '全部',
        payeeMethodLabel: '收款方式',
        payeeMethodWechat: '微信',
        payeeMethodAlipay: '支付寶',
        payeeMethodPaypal: 'PayPal',
        payeeMethodBankCard: '銀行卡',
        payeeMethodOther: '其他',
        payeeInfoLabel: '收款資訊',
        payeeInfoPlaceholderWechat: '請輸入微信號',
        payeeInfoPlaceholderAlipay: '請輸入支付寶帳號(手機號或信箱)',
        payeeInfoPlaceholderPaypal: '請輸入 PayPal 註冊信箱',
        payeeInfoPlaceholderBankCard: '開戶行 / 卡號 / 戶名',
        payeeInfoPlaceholderOther: '請詳細填寫收款方式與帳號',
        bankNameLabel: '開戶行',
        bankCardNoLabel: '卡號',
        bankHolderNameLabel: '戶名',
        bankNamePlaceholder: '請填寫開戶行',
        bankCardNoPlaceholder: '請填寫銀行卡號',
        bankHolderNamePlaceholder: '請填寫戶名',
        payeeInfoHintWechat: '請填寫微信號或可透過掃碼識別的標識',
        payeeInfoHintAlipay: '請填寫支付寶帳號(手機號 / 信箱)',
        payeeInfoHintPaypal: '請填寫有效的 PayPal 註冊信箱,打款將透過 PayPal 完成',
        payeeInfoHintBankCard: '請填寫完整開戶行 + 卡號 + 戶名,資訊錯誤可能導致打款失敗',
        payeeInfoHintOther: '請詳細填寫收款方式與帳號,以便管理員核對',
        payeeInfoSecurityNote: '為保護資訊安全,系統不會保存歷史收款資訊,每次申請需重新填寫',
        confirmCheckbox: '我確認上述收款資訊無誤,知悉打款由管理員人工處理,資訊錯誤可能導致打款失敗',
        cancelBtn: '取消',
        submitBtn: '提交申請',
        submitting: '提交中...',
        submitSuccess: '提現申請已提交,等待審核',
        submitFailed: '提交失敗,請稍後重試'
      },
      list: {
        title: '提現記錄',
        subtitle: '歷史提現申請與打款狀態',
        applyBtn: '申請提現',
        loadFailed: '記錄載入失敗,請稍後重試',
        empty: '暫無提現記錄',
        filterStatus: '狀態篩選',
        filterStatusAll: '全部狀態',
        column: {
          appliedAt: '申請時間',
          amount: '金額(USD)',
          method: '收款方式',
          payeeMasked: '收款帳號',
          status: '狀態',
          action: '操作'
        },
        statusPending: '待審核',
        statusApproved: '已通過 待打款',
        statusPaid: '已打款',
        statusRejected: '已駁回',
        statusFailed: '打款失敗',
        viewDetailBtn: '詳情',
        reapplyBtn: '重新申請',
        detailTitle: '提現詳情',
        timelineTitle: '狀態流轉',
        timelineApplied: '提交申請',
        timelineApproved: '審核通過',
        timelineRejected: '審核駁回',
        timelinePaid: '已打款',
        timelineFailed: '打款失敗',
        rejectReasonLabel: '駁回原因',
        failReasonLabel: '失敗原因',
        paidRemarkLabel: '打款備註',
        paidProofLabel: '打款憑證',
        payeeMaskedHint: '收款帳號僅展示後 4 位,完整資訊已加密保存'
      }
    },
    errors: {
      balanceInsufficient: '餘額不足',
      withdrawalPending: '您已有進行中的提現申請,請等待審核完成後再申請',
      withdrawalBelowMin: '提現金額低於最低額度 {min} USD',
      withdrawalAboveBalance: '提現金額超過可提現餘額',
      amountInvalid: '請輸入有效的提現金額',
      payeeRequired: '請填寫收款資訊',
      methodRequired: '請選擇收款方式',
      confirmRequired: '請先勾選確認收款資訊無誤',
      permissionDenied: '您沒有權限存取教師中心',
      networkError: '網路錯誤,請稍後重試'
    }
  }
}
