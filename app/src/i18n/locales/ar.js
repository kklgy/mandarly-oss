// 阿拉伯语文案(MSA 现代标准阿拉伯语,2026-05-19 D6 全量收口)
// 翻译标准:对标 Cambly / Preply / iTalki 阿语版,覆盖海湾 + 北非 + 黎凡特用户
// 术语词典:docs/frontend/i18n-glossary-ar.md(新增 key 含术语时直接套用,避免漂移)
// 品牌词:"Mandarly" 保留拉丁字符;HSK / Google / Apple / Stripe 等保留原文
// 首发市场:阿联酋(UAE / 迪拜),RTL 布局;UI 数字保留西方阿拉伯数字(0-9)
// 未来如有阿语母语者校对,基于本基线做润色 PR,而非重做翻译
import legalPrivacy from './legal-privacy/ar.js'
import legalTerms from './legal-terms/ar.js'

export default {
  legal: {
    ...legalPrivacy.legal,
    ...legalTerms.legal,
    toc: { title: 'جدول المحتويات', close: 'إغلاق' },
    print: 'طباعة',
    progressLabel: 'تقدم القراءة',
    privacy: {
      ...legalPrivacy.legal.privacy,
      lastUpdatedLabel: 'آخر تحديث: {date}'
    },
    terms: {
      ...legalTerms.legal.terms,
      lastUpdatedLabel: 'آخر تحديث: {date}'
    }
  },
  app: {
    name: 'Mandarly',
    tagline: 'تعلم اللغة الصينية مع معلمين أصليين 1 مقابل 1'
  },
  common: {
    next: 'التالي',
    back: 'السابق',
    submit: 'إرسال',
    skip: 'تخطي',
    retry: 'إعادة المحاولة',
    loading: 'جارٍ التحميل...',
    error: 'حدث خطأ',
    cancel: 'إلغاء',
    confirm: 'تأكيد',
    delete: 'حذف',
    close: 'إغلاق',
    networkError: {
      title: 'فشل التحميل',
      desc: 'يرجى التحقق من اتصال الإنترنت وإعادة المحاولة'
    },
    price: {
      free: 'مجانًا',
      loginToView: 'سجّل الدخول لعرض السعر'
    }
  },
  home: {
    hero: {
      eyebrow: 'دروس ماندرين مباشرة فردية',
      title: 'دروس صينية عبر الإنترنت للأطفال والبالغين',
      tagline: 'تعلّم اللغة الصينية فردياً مع معلمين أصليين من حول العالم',
      subtitle: 'تعلّم الماندرين مع معلمين صينيين ودودين عبر دروس محادثة مباشرة فردية.',
      cta: {
        freeTrial: 'احجز درسًا تجريبيًا',
        curriculum: 'عرض المنهج',
        browseTeachers: 'تصفّح المعلمين',
        levelCheck: 'اختبار مستوى 30 ثانية'
      },
      trust: 'معلمون صينيون أصليون | دروس مباشرة فردية | تعلّم يركز على المحادثة',
      video: {
        title: 'شاهد كيف تعمل حصص Mandarly',
        caption: 'دروس محادثة ماندرين مباشرة فردية مع معلمين صينيين أصليين.',
        play: 'تشغيل بالصوت',
        soundOn: 'الصوت يعمل'
      }
    },
    feature: {
      title: 'لماذا تختار Mandarly',
      native: { title: 'معلّمون أصليون 1 إلى 1', desc: 'معلمو لغة صينية أصليون معتمدون من حول العالم، بدروس فردية مخصّصة لاحتياجاتك' },
      levelCheck: { title: 'مناسب للمبتدئين تمامًا', desc: 'اختبار مستوى في 30 ثانية يرشّح لك المعلم ومسار البدء المناسبين' },
      flexible: { title: 'حجز مرن', desc: 'باقات نصف سنوية أو سنوية، احجز بإيقاعك، مع إمكانية الإلغاء وإعادة الجدولة' },
      // D25: البطاقة الرابعة (مطابقة للموقع المرجعي)
      professional: { title: 'معلمون محترفون معتمدون', desc: '100% معلمون معتمدون، اختيار وتدريب صارمان، خبرة غنية في تدريس اللغة الصينية دوليًا' },
      // D25: زر الإجراء في تخطيط التناوب
      learnMore: 'اعرف أكثر'
    },
    recommend: {
      title: 'معلمون مقترحون',
      sub: 'معلمون أصليون بتقييم 4.8+ نجوم، مع أولوية للمعلمين المعتمدين حديثًا',
      viewAll: 'عرض الكل',
      viewDetail: 'عرض الملف الشخصي',
      empty: 'سيُضاف المزيد من المعلمين قريبًا',
      prev: 'المجموعة السابقة',
      next: 'المجموعة التالية'
    },
    package: {
      title: 'اختر إيقاع التعلّم الذي يناسبك',
      sub: 'درس تجريبي مجاني + باقات نصف سنوية أو سنوية. سجّل الدخول لعرض الأسعار كاملة.',
      viewAll: 'مقارنة جميع الباقات',
      recommended: 'موصى به',
      discount: 'خصم 10% — وفّر {amount}',
      freeTrial: { name: 'تجربة مجانية' },
      halfYear: { name: 'باقة نصف سنوية (درس واحد/أسبوع)' },
      fullYear: { name: 'باقة سنوية (درسان/أسبوع)' }
    },
    curriculumPath: {
      eyebrow: 'مسار تعلم من 4 مراحل',
      title: 'منهج ينمو مع مستواك',
      subtitle: 'من الصينية للمبتدئين إلى تواصل واثق بالماندرين، كل مرحلة تبني الثقة في المحادثة خطوة بخطوة.',
      cta: 'عرض المنهج الكامل',
      stages: [
        { label: 'مبتدئ', name: 'الصينية للمبتدئين', desc: 'ابدأ بالنطق والبينيين والنغمات والتحيات البسيطة.' },
        { label: 'أساسيات', name: 'أساسيات الماندرين', desc: 'ابنِ مفردات يومية وأنماط جمل ومحادثات أساسية.' },
        { label: 'متوسط', name: 'الصينية المتوسطة', desc: 'طوّر الطلاقة عبر موضوعات وقصص وثقافة ومحادثات أطول.' },
        { label: 'متقدم', name: 'ماندرين متقدم', desc: 'طوّر النقاش العميق والعروض ودعم الكتابة والتحضير لـ HSK.' }
      ]
    },
    trustBar: {
      title: 'يثق بنا أكثر من 2,300 متعلم حول العالم',
      items: {
        lesson: { title: 'دروس لمدة 30 دقيقة', desc: 'تعلّم فردي مركز وفعال' },
        native: { title: 'معلمون ناطقون أصليون', desc: 'محترفون ومعتمدون وذوو خبرة' },
        schedule: { title: 'جدولة مرنة', desc: 'احجز الدروس في الوقت الذي يناسبك' },
        progress: { title: 'متابعة تقدمك', desc: 'تقارير شخصية ودعم مستمر' }
      }
    },
    how: {
      title: 'كيف تبدأ مع Mandarly',
      subtitle: 'ابدأ رحلة تعلم الماندرين في 4 خطوات بسيطة',
      cta: 'ابدأ تجربة مجانية',
      steps: [
        { title: 'اختبار مستوى مجاني', desc: 'أكمل اختبارًا قصيرًا لمعرفة مستواك الحالي.' },
        { title: 'مطابقة مع معلم', desc: 'نرشّح لك المعلم الأنسب لأهدافك.' },
        { title: 'احجز درسك', desc: 'اختر الوقت الأنسب لجدولك.' },
        { title: 'ابدأ التعلم', desc: 'استمتع بدروس شخصية وتابع تقدمك.' }
      ]
    },
    faq: {
      title: 'الأسئلة الشائعة',
      q1: { q: 'ما هي تكلفة الدراسة؟', a: 'نوفر باقات نصف سنوية وسنوية بإيقاع أسبوعي. سجّل أو ادخل لحسابك لعرض الأسعار والعملات كاملة، وتحصل على درس تجريبي مجاني عند التسجيل.' },
      q2: { q: 'هل جميع المعلمين ناطقون بالصينية أصلًا؟ ما مؤهلاتهم؟', a: 'جميع المعلمين ناطقون بالصينية أصلًا (الماندرين أو الكانتونية)، حاصلون على شهادة تدريس وخبرة لا تقل عن سنة في تدريس اللغة الصينية. يخضع كل معلم لمراجعة يدوية من فريق Mandarly (الهوية والمؤهلات وفيديو تعريفي).' },
      q3: { q: 'كيف يتم الدرس؟ هل أحتاج إلى تثبيت برنامج؟', a: 'الدرس يجري مباشرة في المتصفح بدون تثبيت أي برنامج (بناءً على Tencent Cloud LCIC). يدعم الكمبيوتر والهاتف والجهاز اللوحي بجودة صوت وصورة عالية. يمكنك الدخول قبل 30 دقيقة من الدرس لاختبار الجهاز.' },
      q4: { q: 'هل يمكنني الحصول على درس تجريبي مجاني؟', a: 'نعم. عند التسجيل تحصل على درس تجريبي مجاني (25 دقيقة) مع المعلم الذي تختاره، بدون الحاجة إلى إدخال بيانات بطاقة ائتمان.' },
      q5: { q: 'هل يمكنني الاسترداد إذا لم أكن راضيًا؟', a: 'الإلغاء قبل الدرس بأكثر من 24 ساعة يستردّ الدرس بالكامل إلى خطتك. إن لم تكن راضيًا عن الدرس بعد بدئه، يمكنك تقديم طلب استرداد. راجع شروط الخدمة §5 لقواعد الاسترداد التفصيلية.' },
      q6: { q: 'هل يمكنني الدراسة بدون أي معرفة سابقة؟ وهل المنصة مناسبة للأطفال؟', a: 'نعم، يمكنك البدء من الصفر؛ جميع المعلمين لديهم خبرة في تدريس المبتدئين. للأعمار من 6 إلى 12 سنة يمكنك اختيار معلم يحمل وسم "مناسب للأطفال" في صفحة الباقات.' }
    },
    cta: {
      title: 'هل ما زلت متردّدًا؟ ابدأ باختبار مستواك في الصينية مجانًا',
      sub: '30 ثانية، 4 أسئلة، وسنرشّح لك المعلم والخطة المناسبة',
      btn: 'اختبار مستوى 30 ثانية',
      teacherJoin: 'كن معلّمًا في Mandarly'
    },

    // D25 جديد: شريط الإحصاءات (StatsBar.vue) — مسوّدة MSA، مراجعة Phase 3
    stats: {
      activeLearners: { value: '2,300+', label: 'متعلم نشط' },
      professionalTutors: { value: '150+', label: 'معلم محترف' },
      lessonsCompleted: { value: '50,000+', label: 'درس مكتمل' },
      parentSatisfaction: { value: '98%', label: 'رضا أولياء الأمور' }
    },

    // D25 جديد: شهادات المتعلمين (TestimonialCards.vue) — مسوّدة MSA
    testimonial: {
      title: 'قصص المتعلمين',
      subtitle: 'يشارك متعلمونا من جميع أنحاء العالم تجاربهم',
      items: [
        { name: 'Emma', country: 'الولايات المتحدة · 6 أشهر', quote: 'ابنتي تطلب الدرس كل يوم الآن، ونطقها يتحسّن باستمرار.' },
        { name: 'Lucas', country: 'بريطانيا · سنة واحدة', quote: 'معلمي صبور جدًا، وأستطيع الآن التحدث بالصينية مع أصدقائي.' },
        { name: 'Sofia', country: 'فرنسا · 8 أشهر دراسة ذاتية', quote: 'الدروس الفردية فعّالة فعلًا، وتقدّمي فاق ما توقعت.' }
      ]
    },

    // D25 جديد: شعار رأس الصفحة المحايد
    appTagline: 'لجميع الأعمار، لجميع المستويات'
  },
  s0: {
    kicker: 'اختبار مستوى مجاني',
    title: 'اعثر على معلمك في 30 ثانية',
    subtitle: 'أجب على 4 أسئلة سريعة، وسنرشح لك معلمًا مناسبًا وخطة دراسية.',
    flowLabel: 'خطوات اختبار المستوى',
    cardLabel: 'سؤال اختبار المستوى',
    proof: {
      questions: { value: '4 أسئلة', label: 'بدون اختبار طويل' },
      login: { value: 'بدون تسجيل دخول', label: 'سجّل فقط عندما تكون جاهزًا' },
      match: { value: 'معلم + خطة', label: 'انتقل مباشرة إلى المسار الرئيسي' }
    },
    flow: {
      level: 'المستوى الحالي',
      goal: 'الهدف',
      pace: 'الوتيرة',
      learner: 'المتعلم'
    },
    progress: 'السؤال {current} من {total}',
    optionalEmail: {
      label: 'البريد الإلكتروني (اختياري، للمتابعة)',
      placeholder: 'بريدك الإلكتروني'
    },
    error: {
      loadFailed: 'فشل تحميل الأسئلة، حاول مرة أخرى.',
      submitFailed: 'فشل الإرسال، حاول مرة أخرى.'
    },
    result: {
      subtitle: 'بناءً على إجاباتك، هذه خطة تعلم خفيفة كبداية. يمكنك حجز معلم الآن أو التواصل مع الدعم لتأكيد الاختيار.',
      subtitleGuest: 'بناءً على إجاباتك، هذه خطة تعلم خفيفة كبداية. سجّل لحفظ نتيجتك والمتابعة.',
      proof: {
        level: 'المستوى المتوقع',
        teachers: 'المعلمون المقترحون',
        plan: 'الخطة المقترحة',
        planReady: 'جاهزة',
        planLater: 'اسألنا',
        nextStep: 'الخطوة التالية',
        nextReady: 'جاهزة'
      },
      teachersEyebrow: 'ترشيح المعلمين',
      heading: {
        beginner: 'أنت مبتدئ — هذا هو الوقت المثالي للبدء',
        intermediate: 'لديك الأساسيات — لنبني الطلاقة',
        advanced: 'أنت متقدم — حان وقت التحسين والتخصص'
      },
      level: {
        beginner: 'مبتدئ',
        intermediate: 'متوسط',
        advanced: 'متقدم'
      },
      teachersTitle: 'أفضل 3 معلمين مختارون لك',
      packageEyebrow: 'إيقاع الدراسة',
      packageTitle: 'الخطة الموصى بها',
      packageDesc: 'اقتراح حسب وتيرة تعلمك، ويمكنك مقارنة كل الخطط قبل الشراء.',
      packageDefaultName: 'خطة مختارة',
      yearsExperience: '{n} سنوات خبرة',
      hasIntroVideo: 'فيديو تعريفي',
      bookNow: 'احجز درسًا',
      viewPackages: 'قارن كل الخطط',
      viewMoreTeachers: 'عرض المزيد من المعلمين',
      registerToContinue: 'سجّل للمتابعة',
      contactWhatsapp: 'تواصل عبر WhatsApp',
      contactSupport: 'تواصل مع الدعم',
      empty: 'لا يوجد معلمون مطابقون حاليًا — يرجى التواصل مع الدعم.',
      freePackage: 'تجربة مجانية',
      perWeek: '{n} درس/أسبوع',
      perWeekPlural: '{n} دروس/أسبوع',
      total: '{n} درسًا',
      validity: 'صالحة لمدة {days} يومًا'
    }
  },
  teachers: {
    title: 'تصفح المعلمين لدينا',
    subtitle: 'اختر معلمًا يناسب أهدافك',
    intro: {
      eyebrow: 'سوق المعلمين',
      description: 'لست متأكدًا من الاختيار؟ ابدأ بحصة تجربة مجانية أو اختبر مستواك في 30 ثانية.',
      descriptionNoTrial: 'لست متأكدًا من الاختيار؟ خذ اختبار المستوى خلال 30 ثانية وسنقترح معلمًا مناسبًا.',
      freeTrial: 'حصة تجربة مجانية',
      levelCheck: 'اختبار مستوى 30 ثانية'
    },
    filterAll: 'الكل',
    loadFailed: 'فشل تحميل المعلمين، حاول مرة أخرى.',
    yearsExperience: '{n} سنوات خبرة',
    introVideo: 'فيديو تعريفي',
    bookNow: 'احجز درسًا',
    search: { placeholder: 'ابحث باسم المعلم أو بالوسم', clear: 'مسح' },
    filter: {
      title: 'تصفية',
      reset: 'إعادة ضبط',
      apply: 'تطبيق التصفية ({count})',
      applyEmpty: 'لا يوجد معلمون مطابقون',
      accent: { title: 'اللهجة', mandarin_cn: 'الماندرين (الصين)', mandarin_tw: 'الماندرين (تايوان)', cantonese: 'الكانتونية' },
      priceBuckets: { title: 'نطاق السعر', lt200: '< HK$200', '200_500': 'HK$200 - 500', '500_1000': 'HK$500 - 1000', gt1000: '> HK$1000' },
      available: 'متاح اليوم',
      minRating: 'الحد الأدنى للتقييم',
      expertise: { title: 'التخصص التعليمي' },
      tags: { title: 'الوسوم', beginner: 'مناسب للمبتدئين', kids: 'مناسب للأطفال', hasVideo: 'يحتوي على فيديو تعريفي' }
    },
    sort: { label: 'الترتيب', recommend: 'موصى به', rating: 'التقييم: الأعلى أولاً', priceAsc: 'السعر: الأقل أولاً', priceDesc: 'السعر: الأعلى أولاً', reviewCount: 'عدد التقييمات: الأكثر أولاً' },
    list: { summary: 'الإجمالي {total} · المعروض {loaded}', loadMore: 'تحميل المزيد ({remaining})', loadingMore: 'جارٍ التحميل...', allLoaded: 'تم تحميل الجميع' },
    empty: {
      noResult: {
        title: 'لم نجد معلمين يطابقون اختياراتك',
        desc: 'جرّب توسيع شروط التصفية أو تصفّح جميع المعلمين',
        clearFilters: 'مسح التصفية',
        browseAll: 'تصفّح الجميع',
        levelCheck: 'اختبار المستوى'
      }
    },
    stats: {
      teachers: '{n} معلم',
      languages: '{n} لغة',
      avgRating: 'متوسط التقييم {r}'
    }
  },
  teacher: {
    backToList: 'العودة إلى القائمة',
    about: 'نبذة عني',
    expertise: {
      title: 'التخصصات',
      business: 'الصينية للأعمال',
      daily: 'المحادثة اليومية',
      kids: 'تعليم الأطفال',
      beginner: 'الصينية للمبتدئين',
      HSK: 'التحضير لاختبار HSK',
      hsk: 'التحضير لاختبار HSK',
      speaking: 'تطوير المحادثة'
    },
    accent: {
      title: 'اللهجة',
      mainland: 'الماندرين',
      mandarin_cn: 'الماندرين (الصين)',
      taiwan: 'الماندرين (تايوان)',
      mandarin_tw: 'الماندرين (تايوان)',
      cantonese: 'الكانتونية'
    },
    languages: 'اللغات',
    introVideo: 'فيديو تعريفي',
    yearsExperience: '{n} سنوات من الخبرة في التدريس',
    scheduleTitle: 'الأوقات المتاحة',
    notFound: 'لم يتم العثور على هذا المعلم.'
  },
  booking: {
    bookNow: 'احجز هذا الموعد',
    pickSlotFirst: 'اختر موعدًا أولاً',
    picker: {
      tzHint: 'الأوقات بتوقيتك ({tz})',
      today: 'اليوم',
      empty: 'لا توجد مواعيد متاحة في الأيام السبعة القادمة.',
      weekday: {
        0: 'الأحد',
        1: 'الإثنين',
        2: 'الثلاثاء',
        3: 'الأربعاء',
        4: 'الخميس',
        5: 'الجمعة',
        6: 'السبت'
      }
    },
    dialog: {
      title: 'تأكيد الحجز',
      teacher: 'المعلم',
      when: 'الموعد',
      choosePackage: 'اختر الخطة لخصم الدرس منها',
      freeTrialBadge: 'تجربة مجانية',
      freeTrialFallback: 'درس تجريبي مجاني',
      packageFallback: 'خطة {count} دروس',
      remaining: '{n} دروس متبقية',
      expireBy: 'صالحة حتى {date}',
      submitting: 'جارٍ الحجز...',
      confirm: 'تأكيد الحجز',
      noPackages: {
        title: 'لا توجد خطة نشطة',
        hint: 'أكمل اختبار 30 ثانية وسنوصي بخطة مناسبة.',
        cta: 'ابدأ اختبار المستوى'
      }
    },
    success: {
      title: 'تم تأكيد الحجز!',
      subtitle: 'لقد حجزنا لك هذا الموعد.',
      orderTitle: 'تفاصيل الطلب',
      teacher: 'المعلم',
      scheduledAt: 'الموعد',
      tzNote: 'بتوقيتك ({tz})',
      duration: 'المدة',
      minutes: '{n} دقيقة',
      package: 'الخطة المستخدمة',
      packageFallback: 'تم خصم درس واحد',
      status: 'الحالة',
      emailNote: 'سنرسل تذكيرًا قبل الدرس عبر البريد الإلكتروني. سيُفتح التقييم بعد انتهاء الدرس.',
      backHome: 'العودة للصفحة الرئيسية',
      viewOrders: 'عرض طلباتي'
    },
    status: {
      upcoming: 'قادم',
      cancelled: 'ملغي',
      finished: 'منتهي',
      refunding: 'قيد الاسترداد',
      refunded: 'تم الاسترداد',
      no_show_student: 'الطالب لم يحضر',
      no_show_teacher: 'المعلم لم يحضر',
      abnormal: 'غير طبيعي',
      to_review: 'بانتظار التقييم'
    },
    teacherBlocked: {
      title: 'تنبيه',
      message: 'لا يمكن لحسابات المعلمين شراء الدورات أو حجز الحصص. الرجاء التبديل إلى حساب طالب.',
      tooltip: 'حسابات المعلمين لا يمكنها الشراء. الرجاء التبديل إلى حساب طالب.',
      shortHint: 'المعلمون لا يمكنهم الشراء'
    }
  },
  myOrders: {
    title: 'طلباتي',
    subtitle: 'جميع دروسك المحجوزة',
    backHome: 'العودة للصفحة الرئيسية',
    loadFailed: 'فشل تحميل الطلبات، حاول مرة أخرى.',
    tabs: {
      all: 'الكل',
      upcoming: 'قادمة',
      toReview: 'بانتظار التقييم',
      finished: 'منتهية',
      cancelled: 'ملغاة',
      refunding: 'قيد الاسترداد'
    },
    empty: {
      all: 'ليس لديك طلبات بعد. ابدأ باختيار معلم.',
      upcoming: 'لا توجد دروس قادمة.',
      toReview: 'لا يوجد ما يحتاج إلى تقييم.',
      finished: 'لا توجد دروس منتهية بعد.',
      cancelled: 'لا توجد طلبات ملغاة.',
      refunding: 'لا توجد عمليات استرداد جارية.'
    },
    browseTeachers: 'تصفح المعلمين',
    card: {
      scheduledAt: 'الموعد',
      tzSuffix: 'بتوقيتك ({tz})',
      duration: '{n} دقيقة',
      package: 'الخطة المستخدمة',
      packageFallback: 'تم خصم درس واحد',
      cancelReason: 'سبب الإلغاء',
      refundedClass: 'تمت إعادة درس واحد إلى خطتك',
      notRefundedClass: 'لم يُعَد الدرس (أقل من 24 ساعة)',
      enterClass: 'دخول الفصل',
      enterClassNotYet: 'يُتاح الدخول قبل الدرس بـ 5 دقائق',
      cancel: 'إلغاء الحجز'
    },
    cancelDialog: {
      title: 'هل تريد إلغاء هذا الحجز؟',
      policyHeading: 'سياسة الإلغاء',
      policy24hPlus: 'قبل الدرس بأكثر من 24 ساعة — استرداد كامل لدرس واحد',
      policy24hWithin: 'خلال 24 ساعة من الدرس — يُفقد الدرس بدون استرداد',
      willRefund: 'تلغي قبل الدرس بـ {hours} ساعة. سيتم إعادة درس واحد إلى خطتك.',
      willNotRefund: 'تبقى أقل من 24 ساعة على الدرس. سيُفقد الدرس بدون استرداد.',
      alreadyStarted: 'انقضى موعد الدرس. يرجى التواصل مع الدعم.',
      reasonLabel: 'السبب (اختياري)',
      reasonPlaceholder: 'ملاحظة قصيرة تساعدنا على التحسين...',
      keepIt: 'إبقاء الحجز',
      confirm: 'نعم، إلغاء',
      submitting: 'جارٍ الإلغاء...',
      success: 'تم إلغاء الحجز',
      error: 'فشل الإلغاء. حاول مرة أخرى.'
    }
  },
  classroom: {
    title: 'الفصل',
    back: 'رجوع',
    backToOrders: 'العودة إلى طلباتي',
    loading: 'جارٍ الدخول إلى الفصل...',
    refresh: 'تحديث الفصل',
    subtitle: {
      student: 'المعلم {nickname} · {duration} دقيقة',
      teacher: 'الطالب {nickname} · {duration} دقيقة'
    },
    network: {
      online: 'متصل',
      offline: 'غير متصل',
      offlineTip: 'انقطع الاتصال، يرجى التحقق من WiFi أو 4G'
    },
    error: {
      loadFailed: 'تعذّر تحميل الفصل',
      retryHint: 'يرجى التحقق من الشبكة والمحاولة مجددًا، أو التواصل مع الدعم',
      contactSupport: `إذا استمرت المشكلة، تواصل معنا على hello{'@'}mandarly.com`,
      autoRetry: 'إعادة المحاولة #{n} خلال {seconds} ثانية...',
      manualRetry: 'إعادة الاتصال يدويًا',
      noJoinUrl: 'رمز الدخول غير متوفر. يرجى التواصل مع الدعم.'
    },
    leaveConfirm: {
      title: 'مغادرة الفصل',
      message: 'هل تريد المغادرة؟ الفصل لا يزال جاريًا، وقد تؤثر المغادرة المبكرة على سجل الدرس.',
      ok: 'مغادرة',
      cancel: 'البقاء'
    },
    fallback: {
      title: 'انتهى الدرس',
      message: 'تجاوز الدرس مدته المحددة بأكثر من 5 دقائق دون استلام إشارة الإنهاء. هل تريد التوجه للتقييم أو العودة للطلبات؟',
      toReview: 'كتابة تقييم',
      toOrders: 'العودة للطلبات',
      continue: 'البقاء في الفصل'
    },
    stub: {
      title: 'فصل تجريبي (تطوير محلي)',
      desc: 'صفحة بديلة تظهر قبل توفّر بيانات اعتماد LCIC. عند ضبط LCIC_MODE=real وإدخال البيانات الحقيقية سيُستبدل هذا بفصل LCIC الفعلي.',
      classId: 'معرّف الفصل',
      role: 'الدور',
      userId: 'معرّف المستخدم',
      token: 'الرمز',
      hint: 'تظهر هذه الصفحة فقط في البيئة المحلية / التطوير، ولا يصلها الإنتاج إطلاقًا.'
    }
  },
  auth: {
    login: {
      title: 'تسجيل الدخول إلى Mandarly',
      emailTab: 'البريد الإلكتروني',
      phoneTab: 'رقم الهاتف',
      socialTab: 'حساب اجتماعي',
      methodLabel: 'طريقة تسجيل الدخول',
      email: 'البريد الإلكتروني',
      phone: 'رقم الهاتف',
      password: 'كلمة المرور',
      code: 'رمز التحقق',
      submit: 'تسجيل الدخول',
      submitting: 'جارٍ تسجيل الدخول...',
      googleBtn: 'Google',
      appleBtn: 'Apple ID',
      forgotPassword: 'نسيت كلمة المرور؟',
      noAccount: 'ليس لديك حساب؟',
      register: 'إنشاء حساب',
      socialDivider: 'أو تابع باستخدام',
      processing: 'جارٍ المعالجة...',
      socialNotConfigured: 'طريقة الدخول هذه ستتوفر قريبًا',
      googleUnsupportedBrowserTitle: 'افتح الصفحة في المتصفح',
      googleUnsupportedBrowserMessage: 'أنت تستخدم حاليًا المتصفح المدمج في {browser}. سيتم حظر تسجيل الدخول عبر Google هناك. يرجى فتح هذه الصفحة في Safari / Chrome من القائمة العلوية، أو نسخ الرابط إلى Safari / Chrome ثم المحاولة مرة أخرى. يمكنك أيضًا تسجيل الدخول بالبريد الإلكتروني أو رقم الهاتف أولًا.'
    },
    phone: {
      countryCode: 'رمز الدولة/المنطقة',
      nationalPlaceholder: 'أدخل رقم الهاتف',
      required: 'أدخل رقم الهاتف',
      invalid: 'أدخل رقم هاتف دولي صالح'
    },
    validation: {
      emailRequired: 'أدخل بريدك الإلكتروني',
      emailInvalid: 'أدخل بريدًا إلكترونيًا صالحًا',
      codeRequired: 'أدخل رمز التحقق',
      codeLength: 'أدخل رمز التحقق المكوّن من 6 أرقام',
      termsRequired: 'يرجى قراءة الشروط والموافقة عليها',
      realNameRequired: 'يرجى إدخال اسمك الحقيقي'
    },
    register: {
      title: 'إنشاء حساب في Mandarly',
      methodLabel: 'طريقة التسجيل',
      socialDivider: 'أو سجّل باستخدام',
      iAmStudent: 'أنا طالب',
      iAmTeacher: 'أنا معلم',
      studentHint: 'أنشئ حساب طالب، واحصل على درس تجريبي، وابدأ حجز المعلمين.',
      teacherEntryText: 'هل تريد الانضمام إلى Mandarly كمعلم؟',
      teacherEntryAction: 'قدّم كمعلم',
      teacherReviewNotice: 'يتطلب تسجيل المعلمين مراجعة المؤهلات (عادة خلال 1-3 أيام عمل)، وهو مسار منفصل عن تسجيل الطلاب.',
      teacherIntro: {
        heading: 'انضم إلى فريق معلمي Mandarly',
        points: [
          'دروس محادثة صينية فردية لمتعلمي الصينية في هونغ كونغ وخارجها',
          'كسب الدخل بالدرس، حدد جدولك بنفسك',
          'بعد التسجيل، يجب تقديم المؤهلات للمراجعة (شهادة البوتونغهوا / الدرجة العلمية / خبرة التدريس)'
        ]
      },
      nickname: 'الاسم المستعار (اختياري)',
      realName: 'الاسم الحقيقي',
      realNamePlaceholder: 'أدخل اسمك الحقيقي',
      referralCode: 'رمز الإحالة (اختياري)',
      agreeTerms: 'لقد قرأت وأوافق على {terms} و {privacy}',
      termsLabel: 'شروط الخدمة',
      privacyLabel: 'سياسة الخصوصية',
      submit: 'تسجيل',
      haveAccount: 'لديك حساب بالفعل؟',
      goLogin: 'تسجيل الدخول'
    },
    resetPassword: {
      title: 'إعادة تعيين كلمة المرور',
      email: 'البريد الإلكتروني',
      code: 'رمز التحقق',
      newPassword: 'كلمة المرور الجديدة',
      confirmPassword: 'تأكيد كلمة المرور الجديدة',
      confirmPasswordRequired: 'أكّد كلمة المرور الجديدة',
      passwordMismatch: 'كلمتا المرور غير متطابقتين',
      sendCode: 'إرسال رمز التحقق',
      sentTo: 'تم إرسال رمز التحقق إلى {email}',
      next: 'التالي',
      success: 'تمت إعادة تعيين كلمة المرور. يرجى تسجيل الدخول مجددًا.',
      unavailable: 'هذه الميزة غير متاحة مؤقتًا',
      submit: 'إرسال',
      backToLogin: 'العودة لتسجيل الدخول'
    },
    code: {
      send: 'إرسال الرمز',
      resend: 'إعادة الإرسال ({n}ث)',
      sent: 'تم الإرسال',
      cooldown: 'يرجى الانتظار قبل إعادة المحاولة'
    },
    callback: {
      processing: 'جارٍ تسجيل دخولك، يرجى الانتظار...',
      failed: 'فشل تسجيل الدخول. يرجى المحاولة مرة أخرى.'
    },
    profile: {
      title: 'ملفي الشخصي',
      basicInfo: 'المعلومات الأساسية',
      nickname: 'الاسم المستعار',
      avatar: 'الصورة الشخصية',
      locale: 'اللغة',
      timezone: 'المنطقة الزمنية',
      bindings: 'الحسابات المرتبطة',
      bindEmail: 'ربط البريد الإلكتروني',
      bindPhone: 'ربط الهاتف',
      bindGoogle: 'ربط Google',
      bindApple: 'ربط Apple',
      unbind: 'فك الارتباط',
      boundAt: 'تاريخ الربط',
      save: 'حفظ',
      saved: 'تم الحفظ',
      referralCode: 'رمز الإحالة الخاص بي'
    },
    error: {
      invalidCredentials: 'البريد الإلكتروني أو كلمة المرور غير صحيحة',
      codeExpired: 'انتهت صلاحية رمز التحقق',
      codeInvalid: 'رمز التحقق غير صحيح',
      codeCooldown: 'طلبات كثيرة جدًا. يرجى الانتظار والمحاولة لاحقًا.',
      emailExists: 'هذا البريد الإلكتروني مسجل بالفعل',
      phoneExists: 'رقم الهاتف هذا مسجل بالفعل',
      teacherEmailExists: 'هذا البريد الإلكتروني مستخدم بالفعل. يرجى استخدام بريد إلكتروني آخر للتسجيل كمعلم.',
      teacherPhoneExists: 'رقم الهاتف هذا مستخدم بالفعل. يرجى استخدام رقم هاتف آخر للتسجيل كمعلم.',
      userNotFound: 'الحساب غير موجود',
      userFrozen: 'الحساب مجمّد. يرجى التواصل مع الدعم.',
      referralInvalid: 'رمز الإحالة غير صالح',
      socialNotConfigured: 'طريقة تسجيل الدخول هذه غير متاحة حاليًا',
      socialFetchFailed: 'الخدمة الاجتماعية غير متاحة مؤقتًا. يرجى استخدام طريقة تسجيل دخول أخرى.',
      tokenExpired: 'انتهت صلاحية جلستك',
      smtpNotConfigured: 'خدمة البريد الإلكتروني غير متاحة مؤقتًا',
      passwordWeak: 'يجب أن تحتوي كلمة المرور على 8 أحرف على الأقل، بأحرف وأرقام',
      rateLimitDaily: 'تم الوصول إلى الحد اليومي للإرسال',
      loginFailedLocked: 'محاولات فاشلة كثيرة جدًا. يرجى المحاولة بعد 15 دقيقة.',
      ipRegisterLimit: 'طلبات تسجيل كثيرة جدًا من هذا العنوان',
      socialAccountConflict: 'هذا الحساب مرتبط بالفعل بمستخدم آخر'
    },
    heroQuote: {
      login: 'أهلاً بعودتك · تابع رحلتك في تعلم الصينية',
      register: 'انضم إلى 200+ معلم أصلي حول العالم · ابدأ التحدث بثقة'
    }
  },
  authHero: {
    login: { title: 'تعلّم الصينية بدروس فردية مع معلّمين أصليين', subtitle: 'أكثر من 600 معلّم معتمد · طلاب من أكثر من 20 دولة' },
    register: { title: 'سجّل في 30 ثانية وابدأ رحلتك مع اللغة الصينية', subtitle: 'درس تجريبي مجاني عند التسجيل' },
    teacherRegister: { title: 'انضم إلى Mandarly وعلّم الصينية للعالم', subtitle: 'جدول مرن · طلاب من حول العالم · تسوية أسبوعية' },
    resetPassword: { title: 'لا تقلق — إعادة تعيين كلمة المرور سهلة', subtitle: '' },
    trust: {
      rating: '⭐ {rating} متوسط التقييم',
      teachers: 'أكثر من {count} معلّم معتمد',
      countries: 'طلاب من أكثر من {count} دولة'
    }
  },
  level_check: {
    q1: {
      text: 'كيف تصف مستواك الحالي في اللغة الصينية؟',
      opt: {
        complete_beginner: 'مبتدئ تمامًا — لم أدرس من قبل',
        some_basics: 'درست قليلاً — أعرف بعض الكلمات',
        simple_conversation: 'أستطيع إجراء محادثات بسيطة',
        fairly_fluent: 'طليق نسبيًا — أرغب في التحسين'
      }
    },
    q2: {
      text: 'ما هدفك الرئيسي من التعلم؟',
      opt: {
        career: 'العمل والوظيفة',
        business: 'التواصل التجاري',
        travel: 'السفر والحياة اليومية',
        hsk_exam: 'التحضير لاختبار HSK',
        for_kids: 'لطفلي',
        just_for_fun: 'مجرد هواية'
      }
    },
    q3: {
      text: 'كم درسًا تخطط في الأسبوع؟',
      opt: {
        one_per_week: 'درس واحد/أسبوع',
        two_per_week: 'درسان/أسبوع',
        intensive: 'مكثف (3+ /أسبوع)',
        not_sure: 'لست متأكدًا بعد'
      }
    },
    q4: {
      text: 'من هو المتعلم؟',
      opt: {
        myself: 'أنا',
        child: 'طفلي (دون 12)',
        teen: 'مراهقي (12-18)',
        multiple: 'عدة أشخاص'
      }
    }
  },
  review: {
    title: { write: 'كتابة تقييم لهذا الدرس', edit: 'تعديل التقييم' },
    subtitle: {
      write: 'شارك تجربتك لمساعدة الآخرين في اختيار معلم',
      edit: 'يمكنك التعديل خلال 24 ساعة من أول إرسال'
    },
    courseTime: 'وقت الدرس {time}',
    step1: { title: 'التقييم العام', guide: 'قيّم معلمك (يدعم نصف نجمة)' },
    step2: { title: 'ملاحظات محددة' },
    step3: { title: 'تأكيد وإرسال' },
    rating: { label: 'التقييم' },
    rate: {
      text: { 1: 'سيئ جدًا', 2: 'سيئ', 3: 'مقبول', 4: 'جيد', 5: 'ممتاز' }
    },
    label: { rating: 'التقييم', content: 'تقييمك', tags: 'العلامات' },
    placeholder: { content: 'سطر أو سطران عن الدرس... (اختياري)' },
    ratingDesc: {
      1: 'ضعيف', 2: 'دون المتوسط', 3: 'مقبول', 4: 'جيد', 5: 'ممتاز'
    },
    tags: {
      addCustom: 'علامة مخصصة',
      customLimit: 'حتى {n} علامات × {len} حرف',
      customLimitReached: 'تم بلوغ حد العلامات المخصصة ({n})',
      customPlaceholder: 'أدخل علامة مخصصة (≤{n} حرف)',
      maxHint: 'اختر حتى {n}',
      remove: 'إزالة {tag}'
    },
    text: { label: 'محتوى التقييم (اختياري)', placeholder: 'أخبرنا عن هذا الدرس...' },
    anonymous: { label: 'نشر بشكل مجهول', desc: 'تظهر في القائمة العامة كـ "طالب مجهول"' },
    editWindow: {
      expired: 'انتهت نافذة التعديل',
      remaining: 'متبقي {hours} ساعة للتعديل',
      urgent: 'متبقي {hours} دقيقة للتعديل'
    },
    action: { cancel: 'إلغاء', next: 'التالي', prev: 'السابق' },
    submit: 'إرسال التقييم',
    submitting: 'جارٍ الإرسال…',
    hint: {
      tagsLimit: 'حتى 3 علامات',
      editWindowLeft: 'متبقي {hours}h للتعديل',
      expired: 'انتهت نافذة التعديل'
    },
    btn: {
      write: 'كتابة تقييم', edit: 'تعديل التقييم',
      submit: 'إرسال', save: 'حفظ', back: 'رجوع'
    },
    error: {
      notFinished: 'يمكن تقييم الدروس المنتهية فقط',
      tooManyTags: 'حتى 3 علامات',
      editExpired: 'انتهت نافذة التعديل (24 ساعة من أول إرسال)',
      submitFailed: 'فشل الإرسال. أعد المحاولة.',
      loadFailed: 'فشل التحميل. أعد المحاولة.'
    },
    success: {
      submitted: 'تم إرسال التقييم',
      updated: 'تم تحديث التقييم',
      title: 'تم إرسال التقييم',
      subtitle: 'شكرًا على ملاحظاتك، يمكنك التعديل خلال 72 ساعة',
      redirect: 'العودة إلى دروسي',
      backToOrders: 'العودة إلى دروسي'
    },
    empty: { teacherList: 'لا توجد تقييمات بعد' },
    publicSection: {
      title: 'تقييمات الطلاب ({n})',
      loadMore: 'تحميل المزيد',
      end: '— نهاية القائمة —'
    },
    card: {
      starsAria: '{n} نجمة',
      submittedAt: 'تم التقييم في {time}',
      editedAt: 'تم التعديل في {time}',
      youWrote: 'تقييمك'
    },
    tag: {
      patient: 'صبور',
      native_accent: 'لكنة أصلية',
      good_pace: 'إيقاع جيد',
      well_prepared: 'محضر جيداً',
      audio_issue: 'مشكلة في الصوت',
      late: 'متأخر',
      interactive: 'تفاعلي',
      good_material: 'مواد جيدة',
      std_pronunciation: 'نطق صحيح',
      humorous: 'مرح',
      encouraging: 'مشجع',
      proper_difficulty: 'صعوبة مناسبة'
    }
  },
  myPackages: {
    title: 'باقاتي',
    subtitle: 'الباقات الحالية وسجل الشراء',
    tabs: { all: 'الكل', active: 'نشط', expired: 'منتهي', exhausted: 'مستهلك' },
    empty: {
      all: 'لا توجد باقات بعد — احجز درساً للبدء',
      active: 'لا توجد باقات نشطة',
      expired: 'لا توجد باقات منتهية',
      exhausted: 'لا توجد باقات مستهلكة'
    },
    loadFailed: 'فشل التحميل. أعد المحاولة.',
    browseTeachers: 'تصفح المعلمين',
    card: {
      remaining: 'متبقي {n}',
      total: 'الإجمالي {n}',
      weekly: '{n}/أسبوع',
      expireAt: 'تنتهي في {date}',
      purchasedAt: 'تم الحصول في {date}',
      free: 'تجربة مجانية',
      statusActive: 'نشط',
      statusExpired: 'منتهي',
      statusExhausted: 'مستهلك'
    },
    source: {
      purchase: 'مشترى',
      free_trial: 'تجربة مجانية',
      register_grant: 'تجربة مجانية',
      admin_grant: 'منحة',
      referral_reward: 'مكافأة الإحالة'
    }
  },
  profileTeacher: {
    stat: {
      title: 'إحصائيات تدريسي',
      avgRating: 'متوسط التقييم',
      reviewCount: 'التقييمات',
      orderCount: 'المنتهية',
      noReviews: '—',
      loadFailed: 'فشل تحميل الإحصائيات'
    },
    myReviews: {
      title: 'تقييمات الطلاب',
      empty: 'لا توجد تقييمات بعد',
      loadMore: 'تحميل المزيد',
      end: '— نهاية القائمة —'
    }
  },
  payment: {
    button: {
      checkout: 'اشتري الآن',
      retry: 'إعادة المحاولة'
    },
    processing: {
      title: 'جاري معالجة الدفع',
      tip: 'يرجى الانتظار، جاري تأكيد دفعتك...'
    },
    success: {
      title: 'تم الدفع بنجاح!',
      message: 'تم تفعيل الباقة. احجز درسك الأول الآن!'
    },
    cancel: {
      title: 'تم إلغاء الدفع',
      message: 'لم يتم خصم أي مبلغ.',
      retry: 'حاول مرة أخرى',
      back: 'باقاتي'
    },
    expired: {
      title: 'جارٍ معالجة الدفع',
      message: 'قد يستغرق تأكيد الدفع بضع ثوانٍ. إذا تم خصم المبلغ، ستظهر الباقة في باقاتي بعد التفعيل.'
    },
    amount: {
      usd: 'المبلغ (USD)'
    },
    discount: {
      applied: 'تم تطبيق خصم الإحالة'
    },
    status: {
      pending: 'في الانتظار',
      paid: 'مدفوع',
      failed: 'فشل الدفع',
      expired: 'منتهي الصلاحية',
      refunded: 'تم الاسترداد',
      partial_refunded: 'استرداد جزئي'
    },
    viewMyPackages: 'عرض باقاتي'
  },
  refund: {
    apply: {
      button: 'طلب استرداد',
      title: 'طلب استرداد',
      reason: {
        label: 'سبب الاسترداد',
        placeholder: 'الرجاء توضيح السبب (10 أحرف على الأقل)',
        min10: 'يجب أن يكون السبب 10 أحرف على الأقل'
      },
      pendingButton: 'طلب الاسترداد قيد المراجعة',
      success: 'تم تقديم طلب الاسترداد، في انتظار المراجعة',
      error: 'فشل التقديم، يرجى المحاولة لاحقاً'
    },
    myList: {
      title: 'طلبات الاسترداد الخاصة بي',
      empty: 'لا توجد طلبات استرداد'
    },
    status: {
      pending: 'قيد المراجعة',
      approved: 'تمت الموافقة',
      refunded: 'تم الاسترداد',
      rejected: 'مرفوض',
      failed: 'فشل الاسترداد'
    },
    auditNote: {
      label: 'ملاحظة المشرف'
    }
  },
  referral: {
    title: 'إحصائيات الإحالة',
    code: {
      your: 'رمز الإحالة الخاص بي',
      copy: 'نسخ',
      copied: 'تم نسخ الرمز'
    },
    stat: {
      invited: 'الأصدقاء المدعوون',
      reward: 'الدروس المجانية المكتسبة'
    },
    discount: {
      applied: 'تم تطبيق خصم الإحالة'
    },
    tip: {
      invite: 'ادع صديقاً لشراء باقة واحصل على درس مجاني',
      firstOrder: 'يحصل صديقك على خصم في طلبه الأول',
      howItWorks: 'يحصل كلاكما على مكافأة بعد أول عملية شراء'
    }
  },
  package: {
    title: 'اختر باقة',
    subtitle: 'اشترِ باقة دروس وابدأ رحلتك في تعلّم الماندرين',
    empty: 'لا توجد باقات متاحة، يرجى التواصل مع الدعم',
    buyNow: 'اشتري الآن',
    weeklyCount: 'حصص / أسبوع',
    validityWeeks: 'إجمالي الحصص',
    priceUsd: 'السعر',
    referralCode: {
      placeholder: 'رمز الإحالة (اختياري)'
    }
  },
  packages: {
    title: 'باقات الدروس',
    subtitle: 'اختر الإيقاع الذي يناسبك. سجّل الدخول لعرض الأسعار كاملة.',
    pricing: {
      eyebrow: 'خطط مرنة لكل متعلم',
      titlePrefix: 'اختر الخطة المناسبة لأهدافك في تعلم',
      titleHighlight: 'الصينية',
      titleSuffix: '',
      subtitle: '',
      lessonFormat: 'تشمل كل الخطط دروس ماندرين خاصة لمدة 30 دقيقة بنظام 1-1 مع معلمين ناطقين أصليين.',
      lessonFormatParts: {
        prefix: 'تشمل كل الخطط دروساً لمدة ',
        duration: '30 دقيقة',
        middle1: ' بنظام ',
        oneOnOne: '1-1',
        middle2: ' ',
        private: 'خاصة',
        suffix: ' في الماندرين مع معلمين ناطقين أصليين.'
      },
      pills: {
        tutors: 'معلمو ماندرين أصليون',
        scheduling: 'جدولة مرنة',
        personalized: 'تعلم مخصص',
        secure: 'آمن وموثوق'
      },
      badge: {
        mostPopular: 'الأكثر شيوعاً',
        bestValue: 'أفضل قيمة'
      },
      priceLocked: 'سجّل الدخول لعرض السعر',
      perLesson: '/ درس',
      lessonsCount: '{n} درس',
      durationMonths: '{n} شهر',
      durationDays: '{n} يوم',
      ctaLoggedOut: 'احجز تجربة مجانية',
      ctaLoggedIn: 'اختر الخطة',
      ctaLoggedInPlan: 'اختر {plan}',
      ctaDefaultPlan: 'الخطة',
      cards: {
        starter: {
          name: 'خطة البداية',
          ctaName: 'البداية',
          subtitle: 'مناسب للتعلم الخفيف',
          point1: 'درس واحد أسبوعياً',
          point2: 'حجز مرن',
          point3: 'معلمون أصليون',
          point4: 'مناسب للمبتدئين'
        },
        standard: {
          name: 'الخطة القياسية',
          ctaName: 'القياسية',
          subtitle: 'الأفضل للتقدم المنتظم',
          point1: 'درسان أسبوعياً',
          point2: 'تحسن أسرع',
          point3: 'أولوية في الحجز',
          point4: 'مسار تعلم مخصص'
        },
        annualLite: {
          name: 'الخطة السنوية',
          ctaName: 'السنوية',
          subtitle: 'للمتعلمين المنتظمين',
          point1: 'درس واحد أسبوعياً',
          point2: 'استمرارية طويلة المدى',
          point3: 'ترسيخ أفضل للتعلم',
          point4: 'وفر أكثر مع الخطة السنوية'
        },
        premiumAnnual: {
          name: 'الخطة السنوية المميزة',
          ctaName: 'المميزة',
          subtitle: 'أفضل قيمة للتعلم طويل المدى',
          point1: 'درسان أسبوعياً',
          point2: 'أفضل قيمة طويلة المدى',
          point3: 'دعم تعلم مخصص',
          point4: 'تقارير تقدم للوالدين'
        }
      },
      why: {
        titlePrefix: 'لماذا تختار',
        titleHighlight: 'Mandarly؟',
        native: {
          title: 'معلمون أصليون',
          desc: 'معلمو ماندرين مختارون ومعتمدون بعناية.'
        },
        schedule: {
          title: 'جدولة مرنة',
          desc: 'احجز الدروس في أي وقت وعبر كل المناطق الزمنية.'
        },
        personalized: {
          title: 'تعلم مخصص',
          desc: 'دروس مصممة حسب أهدافك وأسلوب تعلمك.'
        },
        progress: {
          title: 'تقدم منظم',
          desc: 'تابع التحسن وحقق أهدافك خطوة بخطوة.'
        }
      },
      testimonials: {
        title: 'محبوب من المتعلمين حول العالم',
        emily: {
          name: 'Emily, California',
          quote: 'ابنتي بدأت أخيراً تستمتع بتعلم الصينية!'
        },
        kevin: {
          name: 'Kevin, Singapore',
          quote: 'دروس مرنة واحترافية وجذابة جداً.'
        },
        sophie: {
          name: 'Sophie, London',
          quote: 'معلمون رائعون ومسار تعلم واضح. أوصي به بشدة!'
        }
      }
    },
    banner: {
      hero: 'خطط شهرية أو سنوية مرنة',
      freeTrial: {
        text: 'احصل على درس تجريبي مجاني (25 دقيقة) عند التسجيل',
        subtext: 'اختر أي معلم · لا حاجة لبطاقة ائتمان',
        cta: 'احصل عليه الآن',
        claimed: 'لقد حصلت بالفعل على الدرس التجريبي المجاني'
      }
    },
    teacherEntry: {
      eyebrow: 'متابعة الحجز',
      title: 'اختر باقة مناسبة لهذا المعلم',
      titleWithName: 'اختر باقة مناسبة لـ {name}',
      desc: 'بعد شراء الدروس، يمكنك الرجوع إلى صفحة المعلم وإكمال حجز درس الماندرين الفردي.',
      backToTeacher: 'العودة إلى المعلم'
    },
    currency: { label: 'العملة', hkd: 'HKD', usd: 'USD', cny: 'CNY' },
    rules: {
      title: 'قواعد الباقات',
      multiPackage: 'يمكنك الاحتفاظ بعدة باقات؛ يتم استهلاك الدروس الأقرب انتهاءً أولاً',
      expiry: 'تنتهي الدروس غير المستخدمة في نهاية فترة الصلاحية (تذكيرات قبل 7/3/1 أيام)',
      refund: 'الإلغاء قبل 24 ساعة أو أكثر من الدرس قابل للاسترداد بالكامل؛ راجع الشروط § 5',
      currency: 'يتم الدفع بعملة الباقة المعروضة، ويمكن للإدارة نشر أو إيقاف كل عملة بشكل مستقل'
    },
    compare: {
      title: 'مقارنة جميع الباقات', expand: 'توسيع المقارنة', collapse: 'طي',
      buy: 'شراء', loginToViewPrice: 'سجّل الدخول لعرض السعر', loginAction: 'تسجيل الدخول / إنشاء حساب', priceLockedNote: 'سجّل الدخول لعرض الأسعار الكاملة لكل باقة. ما زالت المزايا ظاهرة للمقارنة.', yes: '✓', no: '—', free: 'مجانًا',
      durationMonths: '{n} شهر', durationDays: '{n} يوم',
      col: { package: 'الباقة' },
      row: {
        pricePerLesson: 'السعر / الدرس', totalPrice: 'السعر الإجمالي', totalLessons: 'إجمالي الدروس',
        validity: 'الصلاحية', booking: 'حجز حر', refund: 'استرداد خلال 24 ساعة',
        upgrade: 'ترقية / تخفيض', discount: 'خصم', action: 'الإجراء'
      }
    },
    confirm: {
      title: 'تأكيد الطلب', summary: 'ملخص الطلب',
      tip: 'سيؤدي النقر على "المتابعة إلى الدفع" إلى تحويلك إلى صفحة Stripe الآمنة',
      refundTip: 'الإلغاء قبل 24 ساعة أو أكثر من الدرس قابل للاسترداد بالكامل. راجع',
      termsLink: 'الشروط § 5',
      cancel: 'إلغاء', confirm: 'المتابعة إلى الدفع', confirming: 'جارٍ المعالجة…',
      durationMonths: '{n} شهر من تاريخ الشراء', durationDays: '{n} يوم',
      sessionsCount: '~{n} دروس', sessionsCountWeekly: '~{n} دروس ({weekly} / أسبوع)',
      field: {
        package: 'الباقة', sessions: 'الدروس', duration: 'الصلاحية',
        currency: 'العملة', subtotal: 'المجموع الفرعي', discount: 'الخصم', total: 'الإجمالي'
      }
    },
    success: { message: 'تم تفعيل الباقة', returnTo: 'متابعة الحجز' },
    empty: {
      title: 'لا توجد باقات متاحة, يرجى التواصل مع الدعم',
      desc: 'قد تكون الباقات نفدت أو قيد الصيانة',
      currencyUnavailable: 'لا توجد باقات {currency} متاحة حالياً. يمكنك عرض باقات HKD بدلاً من ذلك',
      viewHkd: 'عرض باقات HKD'
    },
    error: { loadFailed: 'فشل تحميل الباقات, يرجى إعادة المحاولة', checkoutFailed: 'فشل بدء الدفع, يرجى إعادة المحاولة' }
  },
  seo: {
    home: {
      title: 'دروس صينية عبر الإنترنت للأطفال والبالغين | Mandarly',
      description: 'تقدم Mandarly دروس صينية مباشرة فردية عبر الإنترنت للأطفال واليافعين والبالغين مع معلمين صينيين أصليين ودودين.',
      keywords: 'دروس صينية عبر الإنترنت, محادثة صينية, ماندرين للأطفال, دروس ماندرين فردية, تعلم الماندرين'
    },
    teachers: {
      title: 'معلمو الماندرين · معلمون ناطقون أصليون · Mandarly',
      description: 'تصفّح معلمي Mandarly الناطقين بالماندرين أصلياً. تصنيفات: محادثة، تجارية، HSK، وأطفال. احجز دروساً فردية في الوقت المناسب لك.',
      keywords: 'معلم ماندرين, دروس صينية أونلاين, HSK, صينية تجارية'
    },
    packages: {
      title: 'الأسعار · خطط دروس ماندرين مرنة · Mandarly',
      description: 'اختر خطط دروس ماندرين مرنة للأطفال والمبتدئين والمتعلمين حول العالم. تعلم فردياً مع معلمين أصليين على Mandarly.',
      keywords: 'أسعار الماندرين, باقات دروس صينية, دروس ماندرين أونلاين, تعليم صيني فردي',
      catalogName: 'خطط دروس Mandarly'
    },
    teacherDetail: {
      titleTemplate: '{name} · معلّم/ـة ماندرين · Mandarly',
      titleFallback: 'معلّم ماندرين · Mandarly',
      descriptionTemplate: 'احجز دروس ماندرين فردية مع {name} على Mandarly. {intro}',
      descriptionFallback: 'احجز دروس ماندرين فردية مع هذا المعلم الناطق أصلياً على Mandarly.'
    },
    levelCheck: {
      title: 'اختبار مستوى الصينية المجاني · مطابقة في 30 ثانية · Mandarly',
      description: 'اختبار مستوى مجاني للغة الصينية على Mandarly. 30 ثانية، 4 أسئلة، توصية شخصية بمعلّم وخطة دروس. بدون تسجيل، بدون بطاقة ائتمان.',
      keywords: 'اختبار مستوى الصينية, اختبار HSK, اختبار ماندرين مجاني'
    },
    levelCheckResult: {
      title: 'نتيجة اختبار الصينية · Mandarly',
      description: 'اعرض نتيجة مستوى اللغة الصينية وتابع مسار التعلم بعد التسجيل.'
    },
    curriculum: {
      title: 'منهج دروس الصينية عبر الإنترنت | دورات محادثة ماندرين من Mandarly',
      description: 'استكشف منهج Mandarly لدروس الصينية عبر الإنترنت، من الصينية للمبتدئين إلى محادثة ماندرين متقدمة للأطفال واليافعين والبالغين.',
      keywords: 'منهج صيني عبر الإنترنت, دورات محادثة ماندرين, صينية للمبتدئين, HSK, صينية للأطفال'
    }
  },
  curriculum: {
    hero: {
      eyebrow: 'المنهج',
      title: 'منهج Mandarly للغة الصينية',
      subtitle: 'مسار واضح من الصينية للمبتدئين إلى تواصل واثق بالماندرين.',
      cta: 'احجز درسًا تجريبيًا'
    },
    overview: {
      eyebrow: 'نظرة عامة على المنهج',
      title: 'مسار منظم للتقدم الحقيقي في محادثة الماندرين',
      desc: 'صمم منهج Mandarly للمتعلمين خارج الصين. تساعد الدروس الفردية الطلاب على بناء الثقة خطوة بخطوة من النطق والمحادثة اليومية إلى التواصل المتقدم.'
    },
    labels: {
      learners: 'المتعلمون المناسبون',
      goal: 'الهدف',
      skills: 'المهارات'
    },
    stages: [
      {
        name: 'الصينية للمبتدئين',
        chineseName: 'النطق والبينيين من الصفر',
        learners: 'طلاب بدون خلفية في الصينية أو يعرفون كلمات قليلة فقط مثل 你好 و谢谢.',
        goal: 'بناء الاهتمام بالصينية وفهم النطق الأساسي والتحدث بتحيات وتعريفات بسيطة.',
        hsk: 'Pre-HSK / HSK 1',
        skills: ['أساسيات النطق', 'مقدمة البينيين', 'تدريب النغمات', 'التحيات والتعريف بالنفس', 'الأرقام والعائلة والألوان والطعام'],
        button: 'استكشف الصينية للمبتدئين'
      },
      {
        name: 'أساسيات الماندرين',
        chineseName: 'أساسيات المحادثة اليومية',
        learners: 'طلاب يعرفون البينيين والجمل البسيطة ويحتاجون إلى مفردات وثقة أكثر.',
        goal: 'بناء أساس قوي في الماندرين وتوسيع المفردات وأنماط الجمل والمحادثات اليومية.',
        hsk: 'HSK 1-2',
        skills: ['تحسين البينيين والنغمات', 'محادثة يومية', 'أنماط جمل أساسية', 'فهم الاستماع', 'مهام تحدث قصيرة'],
        button: 'استكشف أساسيات الماندرين'
      },
      {
        name: 'الصينية المتوسطة',
        chineseName: 'تعبير موضوعي متقدم',
        learners: 'طلاب لديهم قدرة تواصل أساسية ويريدون طلاقة وفهمًا وبنية أفضل.',
        goal: 'تحسين طلاقة الماندرين في موضوعات الحياة والمدرسة والسفر والثقافة والاهتمامات.',
        hsk: 'HSK 2-4',
        skills: ['تدريب الطلاقة', 'بناء جمل أطول', 'محادثة حسب الموضوع', 'رواية القصص', 'التعبير عن الرأي'],
        button: 'استكشف الصينية المتوسطة'
      },
      {
        name: 'ماندرين متقدم',
        chineseName: 'تواصل عميق ومتقدم',
        learners: 'طلاب لديهم أساس قوي ويريدون محادثة متقدمة وثقافة وعروضًا وكتابة أو تحضير HSK.',
        goal: 'الانتقال من القدرة على الكلام إلى التحدث بشكل طبيعي ومنطقي وواثق في موضوعات أعمق.',
        hsk: 'HSK 4-6+',
        skills: ['محادثة ماندرين متقدمة', 'نقاش وآراء', 'مهارات العرض بالصينية', 'أخبار وثقافة', 'دعم تحضير HSK'],
        button: 'استكشف الماندرين المتقدم'
      }
    ],
    bottomCta: {
      title: 'لست متأكدًا من المستوى المناسب؟',
      desc: 'احجز درسًا تجريبيًا وسيساعدك المعلم في اختيار أفضل مسار لتعلم الماندرين.',
      button: 'احجز درسًا تجريبيًا'
    }
  },
  comingSoon: {
    badge: 'قريبًا',
    cta: { backHome: 'العودة إلى الرئيسية' },
    textbook: {
      title: 'الكتب الدراسية قيد الإعداد',
      desc: 'يعمل فريق المناهج لدينا على تجهيز كتب دراسية مختارة بعناية لجميع المستويات، وسننشر هنا الملاحظات والتمارين قريبًا.'
    },
    resources: {
      title: 'مركز الموارد سيُطلق قريبًا',
      desc: 'أدلة الدراسة وبطاقات المفردات ومقالات ثقافية ومواد تدريب مجانية في الطريق إليكم.'
    }
  },
  header: {
    nav: {
      home: 'الرئيسية',
      curriculum: 'المنهج',
      teachers: 'المعلمون',
      packages: 'الأسعار',
      levelCheck: 'اختبار المستوى',
      myOrders: 'دروسي',
      // D26: روابط مرسى للصفحة الرئيسية (غير مسجّل)
      features: 'لماذا تختار',
      recommendedTeachers: 'المعلمون المقترحون',
      packagesSection: 'الباقات',
      stories: 'قصص المتعلمين',
      faq: 'الأسئلة الشائعة',
      // D27: مداخل المحتوى الخارجي
      textbook: 'الكتب الدراسية',
      resources: 'مركز الموارد',
      podcast: 'البودكاست',
      // D27: عناوين المجموعات في القائمة المنسدلة
      aboutGroup: 'تعرّف على Mandarly',
      resourcesGroup: 'الموارد'
    },
    action: {
      login: 'تسجيل الدخول',
      register: 'إنشاء حساب',
      becomeTeacher: 'كن معلّمًا'
    },
    menu: {
      profile: 'حسابي',
      profileTeacher: 'الملف الشخصي',
      myPackages: 'باقاتي',
      myOrdersTeacher: 'طلباتي',
      income: 'الدخل',
      withdrawal: 'سحب',
      myRefunds: 'استرداداتي',
      logout: 'تسجيل الخروج'
    }
  },
  tabbar: {
    home: 'الرئيسية',
    teachers: 'المعلمون',
    myOrders: 'دروسي',
    profile: 'حسابي'
  },
  footer: {
    section: {
      about: 'من نحن',
      service: 'الخدمات',
      legal: 'قانوني',
      contact: 'اتصل',
      payment: 'طرق الدفع المدعومة',
      follow: 'تابعنا'
    },
    about: {
      intro: 'من نحن',
      jobs: 'فرص العمل',
      teacherJoin: 'كن معلّمًا',
      contact: 'تواصل معنا'
    },
    legal: {
      privacy: 'سياسة الخصوصية',
      terms: 'شروط الخدمة'
    },
    social: {
      followOn: 'تابعنا على {name}',
      wechatScan: 'امسح الرمز للتواصل معنا على ويتشات',
      wechatZoomHint: 'انقر لتكبير رمز ويتشات'
    },
    copyright: '© 2026 ماندارلي تكنولوجي ليمتد · هونغ كونغ'
  },
  support: {
    open: 'فتح الدعم',
    close: 'إغلاق الدعم',
    title: 'دعم Mandarly',
    subtitle: 'WeChat · WhatsApp · Email',
    directHint: 'اختر قناة تواصل وسيتابع فريق الدعم معك في أقرب وقت ممكن.',
    greeting: 'مرحبًا! اكتب سؤالك وسنبحث لك عن الإجابة، أو تواصل مع الدعم مباشرة.',
    placeholder: 'اكتب سؤالك واضغط Enter للإرسال',
    send: 'إرسال',
    noMatch: 'لم نجد إجابة مناسبة الآن. يمكنك التواصل مع الدعم مباشرة.',
    wechatCopied: 'تم نسخ معرف WeChat',
    wechatCopyFailed: 'تعذر النسخ. يرجى نسخ معرف WeChat يدويًا: {id}',
    openFailed: 'تعذر فتح الرابط. يرجى المحاولة لاحقًا أو التقاط لقطة شاشة والتواصل مع فريقنا.',
    error: 'الدعم غير متاح مؤقتًا. حاول لاحقًا.',
    channel: {
      wechat: 'WeChat',
      whatsapp: 'WhatsApp',
      email: 'Email',
      other: 'قناة تواصل',
      pending: 'يجري تجهيز قناة التواصل هذه.'
    }
  },
  teacherDetail: {
    hero: {
      rating: 'التقييم',
      reviews: '{count} تقييم',
      todayAvailable: 'متاح اليوم {count} مواعيد',
      favorite: 'إضافة إلى المفضّلة'
    },
    video: { title: 'فيديو تعريفي', placeholder: 'اضغط للتشغيل' },
    intro: { title: 'نبذة عني', more: 'عرض المزيد', less: 'طي' },
    expertise: { title: 'التخصصات التعليمية' },
    book: {
      title: 'الحجز',
      priceUnit: '/ {n} دقيقة',
      selectSlot: 'اختر موعد الدرس',
      selectedSlot: 'تم الاختيار',
      bookNow: 'احجز الآن',
      buyPackageForTeacher: 'شراء باقة لهذا المعلم',
      viewOtherTeachers: 'عرض معلمين آخرين',
      emptyHint: 'يرجى اختيار موعد أولاً',
      packageCreditPricing: 'الحجز باستخدام رصيد الباقة',
      loginToViewPackages: 'سجّل الدخول لعرض الباقات',
      refundTip: 'الإلغاء قبل الدرس بأكثر من 24 ساعة قابل للاسترداد بالكامل'
    },
    review: {
      title: 'تقييمات الطلاب ({n})',
      empty: 'لا توجد تقييمات بعد · كن أول من يقيّم',
      lowVolume: 'كن أول طالب يترك تقييمًا',
      seeAll: 'تحميل المزيد ({count} متبقّ)'
    },
    related: {
      title: 'معلمون مرتبطون',
      viewAll: 'عرض الكل',
      emptyTitle: 'لا يوجد المزيد من المعلمين المرتبطين حاليًا',
      emptyDesc: 'ارجع إلى قائمة المعلمين وصفّ حسب الوقت أو اللهجة أو هدف التعلّم.',
      emptyAction: 'تصفّح كل المعلمين',
      loadFailed: 'تعذّر تحميل المعلمين المرتبطين حاليًا'
    },
    balance: {
      insufficient: 'رصيد الدروس في باقتك غير كافٍ، هل تريد شراء باقة جديدة؟',
      goPackages: 'الذهاب إلى الباقات'
    }
  },
  teacherCard: {
    badge: { todayAvailable: 'متاح اليوم', newTeacher: 'معلم جديد', introVideo: 'فيديو' },
    avatarAlt: 'صورة المعلم {name}',
    favorite: 'إضافة إلى المفضّلة',
    favoriteSoon: 'قريبًا',
    priceUnit: '/ 25 دقيقة',
    reviewCount: '({count} تقييم)',
    noReviews: 'لا توجد تقييمات بعد',
    finishedLessons: '{count} درس منتهٍ',
    newTeacherQualified: 'معلم جديد · مؤهلاته معتمدة',
    timezoneMeta: '{location} · {offset}'
  },
  packageCard: {
    cta: { buy: 'اشترِ الآن', claimTrial: 'احصل عليه الآن', loginToViewPrice: 'سجّل الدخول لعرض السعر', loginRegister: 'تسجيل الدخول / إنشاء حساب', loading: 'جارٍ التحميل…' },
    purchased: 'تم الشراء',
    perSessionUnit: '/ درس',
    sessions: '{n} درس',
    period: { months: 'صالحة لمدة {n} أشهر', days: 'صالحة لمدة {n} يومًا' }
  },
  profile: {
    title: 'حسابي',
    user: { guest: 'غير مسجّل الدخول' },
    menu: {
      account: 'الحساب',
      referral: 'الإحالات',
      teacherStats: 'إحصائيات التدريس',
      myPackages: 'باقاتي',
      myOrders: 'دروسي',
      myRefunds: 'استرداداتي',
      logout: 'تسجيل الخروج'
    },
    account: {
      saved: 'تم الحفظ',
      section: { basic: 'المعلومات الأساسية', bindings: 'الحسابات المرتبطة', password: 'تغيير كلمة المرور', danger: 'المنطقة الحرجة' },
      field: { nickname: 'الاسم المستعار', email: 'البريد الإلكتروني', locale: 'اللغة', timezone: 'المنطقة الزمنية', referralCode: 'رمز الإحالة الخاص بي' },
      action: { save: 'حفظ', changeAvatar: 'تغيير الصورة الرمزية' },
      avatar: {
        uploading: 'جارٍ الرفع...',
        uploadSuccess: 'تم تحديث الصورة الرمزية',
        uploadFailed: 'فشل رفع الصورة الرمزية، يرجى المحاولة مرة أخرى',
        invalidType: 'يرجى رفع ملف صورة (PNG / JPG / WebP / GIF)',
        tooLarge: 'يجب ألا تتجاوز الصورة {mb} ميغابايت'
      },
      bindings: {
        email: 'البريد الإلكتروني', phone: 'الهاتف', google: 'Google', apple: 'Apple',
        bound: 'مرتبط', notBound: 'غير مرتبط', bindNow: 'ربط الآن',
        bindEmail: 'ربط البريد الإلكتروني', bindPhone: 'ربط الهاتف', unbind: 'فك الارتباط',
        unbindConfirm: 'هل أنت متأكد من فك ارتباط طريقة تسجيل الدخول هذه؟', unbindSuccess: 'تم فك الارتباط',
        bindEmailSuccess: 'تم ربط البريد الإلكتروني', bindPhoneSuccess: 'تم ربط الهاتف',
        primaryEmail: 'البريد الإلكتروني الأساسي'
      },
      password: {
        current: 'كلمة المرور الحالية', new: 'كلمة المرور الجديدة', confirm: 'تأكيد كلمة المرور الجديدة', action: 'تغيير كلمة المرور',
        required: 'يرجى ملء كل حقول كلمة المرور', mismatch: 'كلمتا المرور الجديدتان غير متطابقتين',
        success: 'تم تغيير كلمة المرور', comingSoon: 'ميزة تغيير كلمة المرور قريبًا'
      },
      logout: {
        title: 'تسجيل الخروج',
        desc: 'تسجيل الخروج من الحساب الحالي. لن تتأثر الطلبات غير المدفوعة.',
        confirm: 'هل أنت متأكد من تسجيل الخروج؟ لن تتأثر الطلبات غير المدفوعة.',
        confirmBtn: 'تأكيد الخروج',
        success: 'تم تسجيل الخروج'
      }
    },
    referral: {
      title: 'الإحالات',
      codeLabel: 'رمز الإحالة', codeCopied: 'تم نسخ رمز الإحالة',
      linkLabel: 'رابط الدعوة الخاص بك', linkCopied: 'تم نسخ رابط الدعوة',
      copy: 'نسخ', copied: 'تم النسخ',
      shareLabel: 'مشاركة:',
      shareText: 'تعلّم الصينية معي على Mandarly واستفد من خصم عند التسجيل برابطي → {link}',
      recordsTitle: 'سجل الدعوات',
      stat: { invited: 'تمت الدعوة', rewarded: 'تمت المكافأة', totalReward: 'إجمالي المكافآت' },
      status: { rewarded: 'تمت المكافأة', pending: 'بانتظار الدفع' },
      empty: {
        title: 'لم تدعُ أي صديق بعد',
        desc: 'انسخ رابط الدعوة وشاركه مع أصدقائك. عند إتمام أول طلب لكليكما، يحصل كلاكما على مكافأة.',
        action: 'نسخ رابط الدعوة'
      }
    },
    teacherStats: {
      title: 'إحصائيات التدريس',
      stat: { avgRating: 'متوسط التقييم', lessonCount: 'الدروس المنتهية', reviewCount: 'عدد التقييمات' },
      monthlyTrend: 'اتجاه هذا الشهر',
      monthlyPlaceholder: 'سيتم إصدار ملخص بيانات هذا الشهر قريبًا، وسيعرض الدروس المنتهية والقادمة لهذا الشهر.'
    }
  },
  orders: {
    title: 'دروسي',
    loadFailed: 'فشل التحميل، يرجى المحاولة لاحقًا',
    tabs: {
      upcoming: 'قادمة', toReview: 'بانتظار التقييم', finished: 'منتهية',
      cancelled: 'ملغاة', refunding: 'قيد الاسترداد', all: 'الكل'
    },
    empty: {
      upcoming: { title: 'لا توجد دروس قادمة', desc: 'تصفّح المعلمين واحجز درسك الأول' },
      toReview: { title: 'لا يوجد ما يحتاج إلى تقييم', desc: 'ستظهر هنا الدروس المنتهية الجاهزة للتقييم' },
      finished: { title: 'لا توجد دروس منتهية بعد', desc: 'ستظهر هنا الدروس بعد انتهائها' },
      cancelled: { title: 'لا توجد طلبات ملغاة', desc: 'الحفاظ على الحضور يساعد على استمرارية التعلّم' },
      refunding: { title: 'لا توجد عمليات استرداد جارية', desc: 'سيظهر هنا تقدّم الاسترداد فور بدئه' },
      all: { title: 'لا توجد طلبات بعد', desc: 'تصفّح المعلمين واحجز درسك الأول' },
      browseTeachers: 'تصفّح المعلمين'
    },
    card: {
      scheduledAt: 'موعد الدرس', tzSuffix: 'بتوقيت {tz}', duration: '{n} دقيقة',
      package: 'الباقة', packageFallback: 'حجز فردي', freeTrialBadge: 'تجربة مجانية',
      cancelReason: 'سبب الإلغاء',
      refundedClass: '✓ تمت إعادة الدرس إلى خطتك', notRefundedClass: '✗ لم يُعَد الدرس',
      refundProgress: 'تقدّم الاسترداد', channelRefundId: 'رقم معاملة الاسترداد',
      status: {
        upcoming: 'قادم', finished: 'منتهٍ', finishedSettling: 'بانتظار التسوية',
        finishedReview: 'بانتظار التقييم', cancelled: 'ملغى', refunded: 'تم الاسترداد',
        refunding: 'قيد الاسترداد', no_show_student: 'لم يحضر الطالب', no_show_teacher: 'لم يحضر المعلم',
        abnormal: 'غير طبيعي', reviewExpired: 'انتهت نافذة التقييم'
      },
      action: {
        enterClass: 'دخول الفصل', returnClass: 'العودة إلى الفصل',
        review: '★ كتابة تقييم', editReview: 'تعديل التقييم',
        cancel: 'إلغاء الحجز', viewDetail: 'عرض التفاصيل'
      }
    },
    countdown: {
      daysHours: '{days} يوم و{hours} ساعة',
      hoursMinutes: '{hours} ساعة و{minutes} دقيقة',
      minutesSeconds: '{minutes} دقيقة و{seconds} ثانية',
      inProgress: 'جارٍ الآن', expired: 'انتهى'
    },
    refundWindow: {
      over24h: 'ⓘ الإلغاء قبل الدرس بأكثر من 24 ساعة يستردّ الدرس بالكامل',
      under24h: '⚠️ تبقّى أقل من 24 ساعة على الدرس، لن يُعَد الدرس عند الإلغاء'
    },
    refundProgress: { applied: 'تم التقديم', reviewing: 'قيد المراجعة', refunded: 'تم الاسترداد' },
    cancelDialog: {
      title: 'إلغاء الحجز',
      summary: { teacher: 'المعلم', time: 'موعد الدرس', distance: 'الوقت المتبقي' },
      countdown24Plus: '✓ تبقّى 24 ساعة أو أكثر على الدرس، سيتم إعادة الدرس بالكامل إلى خطتك',
      countdown24Minus: '⚠️ تبقّى أقل من 24 ساعة على الدرس، لن يُعَد الدرس',
      alreadyStarted: 'الدرس بدأ بالفعل، لا يمكن الإلغاء',
      reasonLabel: 'سبب الإلغاء (اختياري، يساعدنا على التحسين)',
      reasonPlaceholder: 'يرجى اختيار سبب الإلغاء',
      notePlaceholder: 'ملاحظات إضافية (اختياري)',
      reasonOther: 'أخرى',
      reason: {
        conflict: 'تعارض في المواعيد', illness: 'وعكة صحية', travel: 'سفر مفاجئ',
        noShow: 'لم يؤكد المعلم', changedMind: 'تغيّرت رغبتي'
      },
      cancel: 'الإبقاء على الحجز', confirm: 'تأكيد الإلغاء', confirming: 'جارٍ الإرسال…',
      success: 'تم الإلغاء', error: 'فشل الإلغاء، يرجى المحاولة لاحقًا'
    }
  },
  teacherCenter: {
    nav: {
      dashboard: 'لوحة العمل',
      profileEdit: 'ملفي الشخصي',
      qualification: 'المؤهلات',
      schedule: 'الجدول',
      orders: 'الطلبات',
      income: 'الدخل',
      withdrawal: 'سحب'
    },
    audit: {
      banner: {
        draft: 'لم يتم تقديم ملفك بعد. أكمل الملف والمؤهلات ثم أرسله للمراجعة.',
        pending: 'تم تقديم الملف للمراجعة، بانتظار موافقة المشرف.',
        rejected: 'لم تتمّ الموافقة: {reason}',
        draftAction: 'إكمال الملف',
        action: 'تحديث'
      }
    },
    dashboard: {
      title: 'مركز المعلم',
      welcome: 'مرحبًا، {name}',
      weeklyClassesLabel: 'دروس هذا الأسبوع',
      monthlyIncomeLabel: 'دخل هذا الشهر',
      pendingSettleLabel: 'بانتظار التسوية',
      ratingLabel: 'متوسط التقييم',
      quickActionsTitle: 'إجراءات سريعة',
      quickActionSchedule: 'إدارة الجدول',
      quickActionOrders: 'عرض الطلبات',
      quickActionIncome: 'عرض الدخل',
      quickActionWithdrawal: 'تقديم طلب سحب'
    },
    profile: {
      title: 'ملفي الشخصي',
      subtitle: 'أكمل سيرتك الذاتية ومعلومات التدريس. التعديلات المحفوظة ستخضع لمراجعة المشرف.',
      intro: {
        label: 'نبذة عن نفسك',
        placeholder: 'قدّم نبذة موجزة عن أسلوبك في التدريس وخلفيتك وأنواع الدروس التي تتقنها.',
        hint: 'يُفضّل 200 إلى 600 حرف. حافظ على لهجة ودودة ليتعرّف الطلاب عليك بسرعة.'
      },
      avatar: {
        label: 'صورة ملف المدرّس',
        upload: 'رفع صورة',
        uploading: 'جارٍ الرفع...',
        hint: 'ستظهر كالصورة الرئيسية في بطاقة المدرّس العامة وصفحة التفاصيل. استخدم صورة واضحة مربعة أو بنسبة 4:3، بحد أقصى {mb} ميغابايت.'
      },
      expertise: {
        label: 'محاور التدريس',
        hint: 'اختيار متعدد. ستظهر اختياراتك في فلاتر الطلاب.'
      },
      languages: {
        label: 'اللغات التي أتحدّثها',
        placeholder: 'اختر اللغات التي يمكنك التدريس أو التواصل بها.'
      },
      languageOptions: {
        zh: 'الصينية (الماندرين)',
        en: 'الإنجليزية',
        ar: 'العربية',
        yue: 'الكانتونية',
        ja: 'اليابانية',
        ko: 'الكورية'
      },
      accent: {
        label: 'اللهجة'
      },
      accentOptions: {
        mandarin_cn: 'الماندرين (الصين)',
        mandarin_tw: 'الماندرين (تايوان)',
        cantonese: 'الكانتونية',
        mixed: 'أخرى / مختلطة'
      },
      yearsExperience: {
        label: 'سنوات الخبرة',
        hint: 'بالسنوات الكاملة؛ أدخل 0 إذا لم تكن لديك خبرة سابقة.'
      },
      introVideo: {
        label: 'فيديو تعريفي عن نفسك',
        upload: 'رفع فيديو تعريفي',
        replace: 'استبدال الفيديو',
        remove: 'إزالة',
        hint: 'صيغة mp4 فقط. الحد الأقصى {mb} ميغابايت و{sec} ثانية لكل ملف.'
      },
      meta: {
        qualificationCount: 'المؤهلات المرفوعة',
        auditedAt: 'آخر مراجعة'
      },
      actions: {
        save: 'حفظ',
        saving: 'جارٍ الحفظ…',
        submitAudit: 'تقديم للمراجعة',
        submitAuditConfirm: 'تأكيد التقديم'
      },
      messages: {
        loadFailed: 'فشل تحميل الملف الشخصي. حاول مرة أخرى لاحقًا.',
        saveSuccess: 'تم حفظ الملف الشخصي',
        saveFailed: 'فشل الحفظ. حاول مرة أخرى لاحقًا.',
        submitAuditConfirm: 'يرجى إكمال جميع التعديلات قبل التقديم. بعد التقديم سيدخل الملف مرحلة المراجعة ولن تتمكّن من إعادة التقديم حتى انتهائها. هل تريد المتابعة؟',
        submitAuditSuccess: 'تم التقديم. ملفك الآن قيد المراجعة.',
        submitAuditFailed: 'فشل التقديم. حاول مرة أخرى لاحقًا.',
        videoFormatInvalid: 'يُدعم تنسيق mp4 فقط.',
        videoTooLarge: 'يجب ألا يتجاوز حجم الفيديو {mb} ميغابايت.',
        videoTooLong: 'يجب ألا تتجاوز مدة الفيديو {sec} ثانية.',
        videoUnreadable: 'تعذّر قراءة ملف الفيديو. يرجى تجربة ملف آخر.',
        videoUploadSuccess: 'تم رفع الفيديو بنجاح',
        videoUploadFailed: 'فشل رفع الفيديو. حاول مرة أخرى لاحقًا.',
        avatarFormatInvalid: 'يرجى رفع ملف صورة.',
        avatarTooLarge: 'يجب ألا يتجاوز حجم الصورة {mb} ميغابايت.',
        avatarUploadSuccess: 'تم تحديث صورة الملف الشخصي',
        avatarUploadFailed: 'فشل رفع الصورة. حاول مرة أخرى لاحقًا.'
      }
    },
    qualification: {
      title: 'المؤهلات',
      subtitle: 'ارفع مستند الهوية وشهادة المؤهل الدراسي. تخضع المستندات لمراجعة المشرف بعد التقديم.',
      requiredHint: 'إلزامي: مستند الهوية وشهادة المؤهل الدراسي. اختياري: شهادة تدريس، شهادة الإنجليزية CET-4/CET-6، وإثبات خبرة التدريس.',
      docType: {
        label: 'نوع المستند',
        placeholder: 'اختر نوع المستند',
        idCard: 'مستند الهوية',
        passport: 'جواز السفر',
        degreeCert: 'شهادة المؤهل الدراسي',
        teachingCert: 'شهادة تدريس',
        englishCert: 'شهادة الإنجليزية CET-4/CET-6',
        experienceProof: 'إثبات خبرة تدريس'
      },
      upload: {
        sectionTitle: 'رفع مستند',
        hint: 'يُدعم jpg / png / pdf. الحد الأقصى {mb} ميغابايت لكل ملف.',
        button: 'اختر الملف وارفعه',
        success: 'تم الرفع',
        failed: 'فشل الرفع. حاول مرة أخرى لاحقًا.',
        errorFormat: 'يُدعم jpg / png / pdf فقط.',
        errorSize: 'الملف يتجاوز {mb} ميغابايت. يرجى ضغطه وإعادة المحاولة.',
        noDocType: 'يرجى اختيار نوع المستند أولاً.'
      },
      list: {
        sectionTitle: 'المستندات المرفوعة',
        empty: 'لم يتم رفع أي مستندات مؤهلات بعد.',
        auditStatus: {
          pending: 'قيد المراجعة',
          approved: 'تمت الموافقة',
          rejected: 'لم تتمّ الموافقة'
        },
        rejectReasonPrefix: 'السبب: ',
        delete: 'حذف',
        deleteConfirm: 'هل تريد حذف هذا المستند؟ لا يمكن التراجع عن هذا الإجراء.',
        deleteSuccess: 'تم الحذف',
        deleteFailed: 'فشل الحذف. حاول مرة أخرى لاحقًا.'
      },
      messages: {
        loadFailed: 'فشل تحميل المؤهلات. حاول مرة أخرى لاحقًا.'
      }
    },
    schedule: {
      title: 'جدولي',
      subtitle: 'حدّد المواعيد المتاحة كل أسبوع؛ سيرى الطلاب الفترات المتاحة ويتمكّنون من الحجز',
      weekViewTitle: 'المواعيد المتاحة لهذا الأسبوع',
      weekRange: '{from} ~ {to}',
      prevWeek: 'الأسبوع السابق',
      nextWeek: 'الأسبوع التالي',
      currentWeek: 'هذا الأسبوع',
      timezoneLabel: 'المنطقة الزمنية: {tz}',
      modeLabel: 'وضع التحرير',
      modeTemplate: 'تعديل الأوقات الأسبوعية',
      modeTemplateDesc: 'ينطبق على كل الأسابيع القادمة؛ مناسب لمواعيدك الثابتة',
      modeException: 'تعديل هذا الأسبوع',
      modeExceptionDesc: 'ينطبق فقط على تواريخ هذا الأسبوع؛ مناسب للتغييرات المؤقتة',
      periodLabel: 'الفترة',
      periodAll: 'الكل 07:00-23:00',
      periodMorning: 'الصباح 07:00-12:00',
      periodAfternoon: 'بعد الظهر 12:00-18:00',
      periodEvening: 'المساء 18:00-23:00',
      applyToNextWeek: 'تطبيق القالب على الأسبوع التالي',
      applyToNextWeekConfirm: 'هل تريد تطبيق قالب هذا الأسبوع على الأسبوع التالي؟ لن تُستبدل استثناءات الأسبوع التالي التي حدّدتها سابقًا.',
      applyToNextWeekSuccess: 'تم التطبيق على الأسبوع التالي',
      addException: 'إضافة استثناء لهذا الأسبوع',
      slotAvailable: 'متاح',
      slotWeeklyAvailable: 'متاح أسبوعيًا',
      slotUnavailable: 'غير متاح',
      slotException: 'استثناء هذا الأسبوع',
      legendAvailable: 'متاح أسبوعيًا',
      legendException: 'استثناء هذا الأسبوع',
      legendWeeklyAvailable: 'متاح أسبوعيًا',
      legendExceptionClosed: 'مغلق هذا الأسبوع',
      legendExceptionExtra: 'مفتوح هذا الأسبوع',
      slotClosedThisWeek: 'مغلق هذا الأسبوع',
      slotOpenThisWeek: 'مفتوح هذا الأسبوع',
      bulkWeekday: 'تحديد أيام العمل',
      bulkColumn: 'تحديد العمود بالكامل',
      exceptionDrawerTitle: 'استثناءات هذا الأسبوع',
      exceptionEmpty: 'لا توجد استثناءات لهذا الأسبوع',
      exceptionDate: 'التاريخ',
      exceptionTime: 'الوقت',
      exceptionAction: 'الإجراء',
      exceptionActionMake: 'جعله متاحًا',
      exceptionActionBlock: 'جعله غير متاح',
      exceptionActionMakeOnce: 'فتحه لهذا الأسبوع فقط',
      exceptionActionBlockOnce: 'إغلاقه لهذا الأسبوع فقط',
      exceptionAddSuccess: 'تمت إضافة الاستثناء',
      exceptionRemoveSuccess: 'تم حذف الاستثناء',
      toggleSuccess: 'تم الحفظ',
      toggleFailed: 'فشل الحفظ، تم التراجع',
      loadFailed: 'فشل تحميل الجدول، يرجى المحاولة لاحقًا',
      mobileDayDot: '{day}',
      onboarding: {
        title: 'هل هذه أول مرة تحدد فيها جدولك؟',
        step1: '① انقر على أي خانة وقت أدناه لتحديدها كـ«متاحة» (انقر مرة أخرى للإلغاء)',
        step2: '② يتكرر القالب الأسبوعي تلقائياً؛ استخدم «استثناءات هذا الأسبوع» للتعديلات الفردية',
        step3: '③ يتم الحفظ فوراً، وسيرى الطلاب فترات توفرك على صفحتك ويتمكنون من الحجز',
        dismiss: 'فهمت، لنبدأ'
      }
    },
    orders: {
      title: 'طلباتي',
      subtitle: 'عرض جميع الدروس المحجوزة',
      loadFailed: 'فشل تحميل الطلبات، يرجى المحاولة لاحقًا',
      tabs: {
        all: 'الكل',
        upcoming: 'قادمة',
        finished: 'منتهية',
        cancelled: 'ملغاة'
      },
      empty: {
        all: 'لا توجد طلبات بعد',
        upcoming: 'لا توجد دروس قادمة',
        finished: 'لا توجد دروس منتهية',
        cancelled: 'لا توجد طلبات ملغاة'
      },
      card: {
        student: 'الطالب',
        scheduledAt: 'موعد الدرس',
        duration: 'المدة {n} دقيقة',
        package: 'الباقة المستخدمة',
        settleStatusLabel: 'حالة التسوية',
        settleFrozen: 'مجمّد (إفراج T+7)',
        settleAvailable: 'متاح للسحب',
        settleCancelled: 'ملغى — لا تسوية',
        feeLabel: 'أجر الحصة',
        enterClass: 'دخول الفصل',
        enterClassNotYet: 'يُتاح الدخول قبل الدرس بـ 5 دقائق',
        cancelledByStudent: 'ألغاه الطالب',
        viewDetail: 'عرض التفاصيل'
      }
    },
    income: {
      title: 'دخلي',
      subtitle: 'ملخّص الدخل المستحقّ والسحوبات',
      loadFailed: 'فشل تحميل الدخل، يرجى المحاولة لاحقًا',
      totalEarnedLabel: 'إجمالي الأرباح',
      availableLabel: 'الرصيد القابل للسحب',
      frozenLabel: 'مجمّد T+7',
      pendingWithdrawalLabel: 'محجوز قيد المعالجة',
      availableHint: 'الرصيد الذي تم الإفراج عنه ومتاح لتقديم طلب سحب',
      frozenHint: 'مستحقّات الدروس الحديثة، يتم الإفراج عنها تلقائيًا بعد T+7',
      pendingWithdrawalHint: 'يوجد طلب سحب قيد المراجعة، لا يمكن تقديم طلب آخر حاليًا',
      nextUnfreezeLabel: 'أقرب إفراج {date}',
      applyWithdrawalBtn: 'تقديم طلب سحب ←',
      detailTitle: 'تفاصيل الدخل',
      filterFrom: 'من تاريخ',
      filterTo: 'إلى تاريخ',
      filterType: 'نوع الدخل',
      filterTypeAll: 'الكل',
      filterTypeClass: 'دخل من الدروس',
      filterTypeAdjust: 'تعديل يدوي',
      exportCsv: 'تصدير CSV',
      column: {
        classTime: 'وقت الدرس',
        student: 'الطالب',
        type: 'النوع',
        fee: 'أجر الحصة (USD)',
        status: 'الحالة',
        unfreezeAt: 'وقت الإفراج'
      },
      statusFrozen: 'مجمّد',
      statusAvailable: 'قابل للسحب',
      statusWithdrawn: 'تم السحب',
      empty: 'لا توجد تفاصيل دخل بعد'
    },
    withdrawal: {
      apply: {
        title: 'تقديم طلب سحب',
        subtitle: 'املأ بيانات الاستلام؛ ستتم مراجعة الطلب يدويًا من قبل المشرف قبل الدفع',
        balanceLabel: 'الرصيد القابل للسحب',
        balanceUnit: 'USD',
        amountLabel: 'مبلغ السحب',
        amountUnit: 'USD',
        amountPlaceholder: 'أدخل مبلغ السحب',
        amountHint: 'الحد الأدنى للسحب {min} USD؛ تتحمّل رسوم التحويل على عاتق المعلم',
        amountAllInBtn: 'الكل',
        payeeMethodLabel: 'طريقة الاستلام',
        payeeMethodWechat: 'WeChat',
        payeeMethodAlipay: 'Alipay',
        payeeMethodPaypal: 'PayPal',
        payeeMethodBankCard: 'بطاقة مصرفية',
        payeeMethodOther: 'أخرى',
        payeeInfoLabel: 'بيانات الاستلام',
        payeeInfoPlaceholderWechat: 'أدخل معرّف WeChat',
        payeeInfoPlaceholderAlipay: 'أدخل حساب Alipay (هاتف أو بريد إلكتروني)',
        payeeInfoPlaceholderPaypal: 'أدخل البريد الإلكتروني المسجّل في PayPal',
        payeeInfoPlaceholderBankCard: 'اسم البنك / رقم الحساب / اسم صاحب الحساب',
        payeeInfoPlaceholderOther: 'يرجى تفصيل طريقة الاستلام والحساب',
        bankNameLabel: 'اسم البنك',
        bankCardNoLabel: 'رقم البطاقة',
        bankHolderNameLabel: 'اسم صاحب الحساب',
        bankNamePlaceholder: 'أدخل اسم البنك',
        bankCardNoPlaceholder: 'أدخل رقم البطاقة',
        bankHolderNamePlaceholder: 'أدخل اسم صاحب الحساب',
        payeeInfoHintWechat: 'يرجى إدخال معرّف WeChat أو معرّف يمكن مسحه ضوئيًا',
        payeeInfoHintAlipay: 'يرجى إدخال حساب Alipay (هاتف / بريد إلكتروني)',
        payeeInfoHintPaypal: 'يرجى إدخال بريد إلكتروني صالح مسجّل في PayPal؛ سيتم الدفع عبر PayPal',
        payeeInfoHintBankCard: 'يرجى إدخال اسم البنك ورقم الحساب واسم صاحب الحساب بشكل كامل؛ المعلومات الخاطئة قد تؤدي إلى فشل التحويل',
        payeeInfoHintOther: 'يرجى تفصيل طريقة الاستلام والحساب ليتمكّن المشرف من التحقّق',
        payeeInfoSecurityNote: 'لحماية بياناتك، لا يحفظ النظام بيانات استلام سابقة؛ يجب إدخالها في كل طلب',
        confirmCheckbox: 'أؤكّد صحة بيانات الاستلام أعلاه، وأدرك أنّ الدفع يتمّ يدويًا من قبل المشرف وأنّ المعلومات الخاطئة قد تؤدي إلى فشل التحويل',
        cancelBtn: 'إلغاء',
        submitBtn: 'تقديم الطلب',
        submitting: 'جارٍ التقديم...',
        submitSuccess: 'تم تقديم طلب السحب، بانتظار المراجعة',
        submitFailed: 'فشل التقديم، يرجى المحاولة لاحقًا'
      },
      list: {
        title: 'سجل السحوبات',
        subtitle: 'سجل طلبات السحب وحالة الدفع',
        applyBtn: 'تقديم طلب سحب',
        loadFailed: 'فشل تحميل السجل، يرجى المحاولة لاحقًا',
        empty: 'لا توجد سحوبات بعد',
        filterStatus: 'تصفية بالحالة',
        filterStatusAll: 'كل الحالات',
        column: {
          appliedAt: 'تاريخ التقديم',
          amount: 'المبلغ (USD)',
          method: 'طريقة الاستلام',
          payeeMasked: 'حساب الاستلام',
          status: 'الحالة',
          action: 'إجراء'
        },
        statusPending: 'قيد المراجعة',
        statusApproved: 'تمت الموافقة — بانتظار الدفع',
        statusPaid: 'تم الدفع',
        statusRejected: 'مرفوض',
        statusFailed: 'فشل الدفع',
        viewDetailBtn: 'التفاصيل',
        reapplyBtn: 'إعادة التقديم',
        detailTitle: 'تفاصيل السحب',
        timelineTitle: 'مسار الحالة',
        timelineApplied: 'تم التقديم',
        timelineApproved: 'تمت الموافقة',
        timelineRejected: 'تم الرفض',
        timelinePaid: 'تم الدفع',
        timelineFailed: 'فشل الدفع',
        rejectReasonLabel: 'سبب الرفض',
        failReasonLabel: 'سبب الفشل',
        paidRemarkLabel: 'ملاحظة الدفع',
        paidProofLabel: 'إثبات الدفع',
        payeeMaskedHint: 'يُعرض من حساب الاستلام آخر 4 أرقام فقط؛ البيانات الكاملة محفوظة بشكل مشفّر'
      }
    },
    errors: {
      balanceInsufficient: 'الرصيد غير كافٍ',
      withdrawalPending: 'لديك طلب سحب قيد المعالجة، يرجى الانتظار حتى اكتمال المراجعة قبل تقديم طلب جديد',
      withdrawalBelowMin: 'مبلغ السحب أقل من الحد الأدنى {min} USD',
      withdrawalAboveBalance: 'مبلغ السحب يتجاوز الرصيد القابل للسحب',
      amountInvalid: 'يرجى إدخال مبلغ سحب صالح',
      payeeRequired: 'يرجى ملء بيانات الاستلام',
      methodRequired: 'يرجى اختيار طريقة الاستلام',
      confirmRequired: 'يرجى تأكيد صحة بيانات الاستلام أولاً',
      permissionDenied: 'ليس لديك صلاحية الوصول إلى مركز المعلم',
      networkError: 'خطأ في الشبكة، يرجى المحاولة لاحقًا'
    }
  }
}
