import legalPrivacy from './legal-privacy/en.js'
import legalTerms from './legal-terms/en.js'

export default {
  legal: {
    ...legalPrivacy.legal,
    ...legalTerms.legal,
    toc: { title: 'Table of contents', close: 'Close' },
    print: 'Print',
    progressLabel: 'Reading progress',
    privacy: {
      ...legalPrivacy.legal.privacy,
      lastUpdatedLabel: 'Last updated: {date}'
    },
    terms: {
      ...legalTerms.legal.terms,
      lastUpdatedLabel: 'Last updated: {date}'
    }
  },
  app: {
    name: 'Mandarly',
    tagline: 'Learn Mandarin 1-on-1 with native teachers'
  },
  common: {
    next: 'Next',
    back: 'Back',
    submit: 'Submit',
    skip: 'Skip',
    retry: 'Retry',
    loading: 'Loading...',
    error: 'Something went wrong',
    cancel: 'Cancel',
    confirm: 'Confirm',
    delete: 'Delete',
    close: 'Close',
    networkError: {
      title: 'Loading failed',
      desc: 'Please check your network and try again'
    },
    price: {
      free: 'Free',
      loginToView: 'Log in to view price'
    }
  },
  home: {
    hero: {
      eyebrow: 'Live one-on-one Mandarin speaking lessons',
      title: 'Online Chinese Classes for Kids & Adults',
      tagline: 'Learn Chinese 1-on-1 with native teachers',
      subtitle: 'Learn Mandarin with friendly native Chinese teachers through live one-on-one speaking classes.',
      cta: {
        freeTrial: 'Book a Free Trial',
        curriculum: 'View Curriculum',
        browseTeachers: 'Browse Tutors',
        levelCheck: 'Take 30-sec Test'
      },
      trust: 'Native Chinese Teachers | One-on-One Live Classes | Speaking-Focused Learning',
      video: {
        title: 'See How Mandarly Classes Work',
        caption: 'Live one-on-one Mandarin speaking lessons with native Chinese teachers.',
        play: 'Play with sound',
        soundOn: 'Sound on'
      }
    },
    feature: {
      title: 'Why Mandarly',
      native: { title: 'Native 1-on-1 Teachers', desc: 'Globally certified native Chinese teachers, customized 1-on-1 lessons' },
      levelCheck: { title: 'Zero-Beginner Friendly', desc: '30-sec level test, smart-matched teachers and beginner courses' },
      flexible: { title: 'Flexible Booking', desc: 'Half-year / full-year plans, book at your own pace, cancel or reschedule' },
      // D25: 4th card (aligns with reference site)
      professional: { title: 'Certified Professional Teachers', desc: '100% certified, rigorously screened and trained, rich international Chinese teaching experience' },
      // D25: alternating layout CTA
      learnMore: 'Learn More'
    },
    recommend: {
      title: 'Recommended Teachers',
      sub: 'Hand-picked native teachers rated 4.8+, with newly certified teachers featured',
      viewAll: 'View All',
      viewDetail: 'View Profile',
      empty: 'More teachers coming soon',
      prev: 'Previous',
      next: 'Next'
    },
    package: {
      title: 'Choose Your Learning Pace',
      sub: 'Free trial + half-year / full-year plans. Log in to see full pricing.',
      viewAll: 'View Full Comparison',
      recommended: 'Recommended',
      discount: 'Save {amount} (10% off)',
      freeTrial: { name: 'Free Trial' },
      halfYear: { name: 'Half-Year Plan (1 lesson/week)' },
      fullYear: { name: 'Full-Year Plan (2 lessons/week)' }
    },
    curriculumPath: {
      eyebrow: '4-stage learning path',
      title: 'A Curriculum That Grows With You',
      subtitle: 'From beginner Chinese to advanced Mandarin communication, each stage builds speaking confidence step by step.',
      cta: 'View Full Curriculum',
      stages: [
        { label: 'Beginner', name: 'Beginner Chinese', desc: 'Start with pronunciation, pinyin, tones, and simple greetings.' },
        { label: 'Foundation', name: 'Foundation Mandarin', desc: 'Build daily vocabulary, sentence patterns, and basic conversations.' },
        { label: 'Intermediate', name: 'Intermediate Chinese', desc: 'Improve fluency through topics, storytelling, culture, and longer conversations.' },
        { label: 'Advanced', name: 'Advanced Mandarin', desc: 'Develop deeper discussion, presentation, writing support, and HSK preparation.' }
      ]
    },
    trustBar: {
      title: 'Trusted by 2,300+ learners worldwide',
      items: {
        lesson: { title: '30-Minute Lessons', desc: 'Focused and effective 1-on-1 learning' },
        native: { title: 'Native-speaking Tutors', desc: 'Professional, certified and experienced' },
        schedule: { title: 'Flexible Scheduling', desc: 'Book lessons at your convenience' },
        progress: { title: 'Track Your Progress', desc: 'Personalized reports and learning support' }
      }
    },
    how: {
      title: 'How Mandarly Works',
      subtitle: 'Start your Mandarin learning journey in 4 simple steps',
      cta: 'Start Free Trial',
      steps: [
        { title: 'Free Level Check', desc: 'Take our free placement test to find your current level.' },
        { title: 'Match with Tutor', desc: 'We match you with the perfect tutor for your goals.' },
        { title: 'Book Your Lesson', desc: 'Choose a time that works best for your schedule.' },
        { title: 'Start Learning', desc: 'Enjoy personalized lessons and track your progress.' }
      ]
    },
    faq: {
      title: 'Frequently Asked Questions',
      q1: { q: 'How does pricing work?', a: 'We offer half-year / full-year plans by weekly cadence. Sign up or log in to see full pricing and currency options. New users get 1 free trial lesson.' },
      q2: { q: 'Are all teachers native Chinese speakers? What credentials do they have?', a: 'All teachers are native Chinese speakers (Mandarin/Cantonese) with teaching certifications and ≥ 1 year of Chinese teaching experience. Every teacher is manually vetted by the Mandarly team (identity / credential / trial lesson).' },
      q3: { q: 'How do classes work? Do I need to install software?', a: 'Classes run directly in your browser — no install needed (powered by Tencent Cloud LCIC). Works on PC / phone / tablet with HD video. Join 30 min early to test your devices.' },
      q4: { q: 'Can I get a free trial lesson?', a: 'Yes. Sign up to claim 1 free trial lesson (25 minutes). Pick any teacher, no credit card required.' },
      q5: { q: 'What is the refund policy?', a: 'Cancel ≥ 24h before class for a full refund (lesson credited back). For dissatisfaction after class starts, you may request a refund — see Terms § 5 for details.' },
      q6: { q: 'Can complete beginners and kids take classes?', a: 'Yes — every teacher has experience with absolute beginners. Learners aged 6-12 can pick teachers tagged "kid-friendly" inside any plan.' }
    },
    cta: {
      title: 'Still on the fence? Take a quick free Chinese level test',
      sub: '30 seconds, 4 questions, smart match to the right teacher and plan',
      btn: 'Take 30-sec Test',
      teacherJoin: 'Become a Mandarly tutor'
    },

    // D25 new: stats bar
    stats: {
      activeLearners: { value: '2,300+', label: 'Active Learners' },
      professionalTutors: { value: '150+', label: 'Professional Tutors' },
      lessonsCompleted: { value: '50,000+', label: 'Lessons Completed' },
      parentSatisfaction: { value: '98%', label: 'Parent Satisfaction' }
    },

    // D25 new: testimonials (sample content — 3 voices: parent / neutral / adult)
    testimonial: {
      title: 'Learner Stories',
      subtitle: 'Hear what our learners around the world say',
      items: [
        { name: 'Emma', country: 'USA · 6 months', quote: 'My daughter now asks for lessons every day. Her pronunciation keeps improving.' },
        { name: 'Lucas', country: 'UK · 1 year', quote: 'My teacher is so patient. I can chat with my friends in Chinese now.' },
        { name: 'Sofia', country: 'France · 8 months self-study', quote: '1-on-1 lessons really work. My progress exceeded what I expected.' }
      ]
    },

    // D25 new: AppHeader neutral tagline
    appTagline: 'For all ages, all levels'
  },
  s0: {
    kicker: 'Free level check',
    title: 'Find your tutor in 30 seconds',
    subtitle: 'Answer 4 quick questions, get matched with the right teacher and plan.',
    flowLabel: 'Level check steps',
    cardLabel: 'Level check question',
    proof: {
      questions: { value: '4 questions', label: 'No long placement test' },
      login: { value: 'No login', label: 'Register only when you are ready' },
      match: { value: 'Tutor + plan', label: 'Go straight into the main path' }
    },
    flow: {
      level: 'Current level',
      goal: 'Goal',
      pace: 'Pace',
      learner: 'Learner'
    },
    progress: 'Question {current} of {total}',
    optionalEmail: {
      label: 'Email (optional, for follow-up)',
      placeholder: 'Your email address'
    },
    error: {
      loadFailed: 'Failed to load questions. Please retry.',
      submitFailed: 'Submission failed. Please retry.'
    },
    result: {
      subtitle: 'Based on your answers, here is a lightweight study path. You can book a tutor now or ask support to confirm the fit.',
      subtitleGuest: 'Based on your answers, here is a lightweight study path. Sign up to save your result and continue.',
      proof: {
        level: 'Estimated level',
        teachers: 'Tutor picks',
        plan: 'Study plan',
        planReady: 'Ready',
        planLater: 'Ask us',
        nextStep: 'Next step',
        nextReady: 'Ready'
      },
      teachersEyebrow: 'Tutor match',
      heading: {
        beginner: 'You\'re just getting started — perfect time to begin',
        intermediate: 'You\'ve got the basics — let\'s build fluency',
        advanced: 'You\'re fluent — time to refine and specialize'
      },
      level: {
        beginner: 'Beginner',
        intermediate: 'Intermediate',
        advanced: 'Advanced'
      },
      teachersTitle: 'Top 3 tutors picked for you',
      packageEyebrow: 'Study rhythm',
      packageTitle: 'Recommended plan',
      packageDesc: 'Suggested from your learning pace. You can still compare every plan before buying.',
      packageDefaultName: 'Selected plan',
      yearsExperience: '{n} yrs experience',
      hasIntroVideo: 'Intro video',
      bookNow: 'Book a class',
      viewPackages: 'Compare all plans',
      viewMoreTeachers: 'See more tutors',
      registerToContinue: 'Register to continue',
      contactWhatsapp: 'Chat with us on WhatsApp',
      contactSupport: 'Contact support',
      empty: 'No matching tutors yet — please contact support.',
      freePackage: 'Free trial',
      perWeek: '{n} class/week',
      perWeekPlural: '{n} classes/week',
      total: '{n} classes total',
      validity: 'Valid {days} days'
    }
  },
  teachers: {
    title: 'Browse our tutors',
    subtitle: 'Pick a tutor that fits your goals.',
    intro: {
      eyebrow: 'Tutor marketplace',
      description: 'Not sure who fits? Start with one free trial class or take the 30-second level check.',
      descriptionNoTrial: 'Not sure who fits? Take the 30-second level check and we will recommend a tutor.',
      freeTrial: 'Free trial class',
      levelCheck: '30-second level check'
    },
    filterAll: 'All',
    loadFailed: 'Failed to load tutors. Please retry.',
    yearsExperience: '{n} yrs experience',
    introVideo: 'Intro video',
    bookNow: 'Book a class',
    search: { placeholder: 'Search by tutor name / tag', clear: 'Clear' },
    filter: {
      title: 'Filters',
      reset: 'Reset',
      apply: 'Apply ({count})',
      applyEmpty: 'No tutors match',
      accent: { title: 'Accent', mandarin_cn: 'Mandarin (CN)', mandarin_tw: 'Mandarin (TW)', cantonese: 'Cantonese' },
      priceBuckets: { title: 'Price', lt200: '< HK$200', '200_500': 'HK$200 - 500', '500_1000': 'HK$500 - 1000', gt1000: '> HK$1000' },
      available: 'Available today',
      minRating: 'Min rating',
      expertise: { title: 'Expertise' },
      tags: { title: 'Tags', beginner: 'Beginner-friendly', kids: 'For kids', hasVideo: 'Has intro video' }
    },
    sort: { label: 'Sort', recommend: 'Recommended', rating: 'Rating ↓', priceAsc: 'Price ↑', priceDesc: 'Price ↓', reviewCount: 'Reviews ↓' },
    list: { summary: '{total} total · {loaded} loaded', loadMore: 'Load more ({remaining})', loadingMore: 'Loading...', allLoaded: 'All loaded' },
    empty: {
      noResult: {
        title: 'No tutors found',
        desc: 'Try relaxing your filters or browse all',
        clearFilters: 'Clear filters',
        browseAll: 'Browse all',
        levelCheck: 'Take level check'
      }
    },
    stats: {
      teachers: '{n} tutors',
      languages: '{n} languages',
      avgRating: 'Avg rating {r}'
    }
  },
  teacher: {
    backToList: 'Back to list',
    about: 'About me',
    expertise: {
      title: 'Specialties',
      business: 'Business Chinese',
      daily: 'Daily conversation',
      kids: 'Kids',
      beginner: 'Beginner Mandarin',
      HSK: 'HSK prep',
      hsk: 'HSK prep',
      speaking: 'Speaking'
    },
    accent: {
      title: 'Accent',
      mainland: 'Mandarin',
      mandarin_cn: 'Mandarin (CN)',
      taiwan: 'Taiwanese Mandarin',
      mandarin_tw: 'Mandarin (TW)',
      cantonese: 'Cantonese'
    },
    languages: 'Languages',
    introVideo: 'Intro video',
    yearsExperience: '{n} years of teaching experience',
    scheduleTitle: 'Available time slots',
    notFound: 'Tutor not found.'
  },
  booking: {
    bookNow: 'Book this slot',
    pickSlotFirst: 'Pick a time slot first',
    picker: {
      tzHint: 'Times shown in your timezone ({tz})',
      today: 'Today',
      empty: 'No available slots in the next 7 days.',
      weekday: {
        0: 'Sun',
        1: 'Mon',
        2: 'Tue',
        3: 'Wed',
        4: 'Thu',
        5: 'Fri',
        6: 'Sat'
      }
    },
    dialog: {
      title: 'Confirm booking',
      teacher: 'Tutor',
      when: 'When',
      choosePackage: 'Choose a plan to deduct from',
      freeTrialBadge: 'Free trial',
      freeTrialFallback: 'Free trial class',
      packageFallback: '{count}-class plan',
      remaining: '{n} classes left',
      expireBy: 'Valid until {date}',
      submitting: 'Booking...',
      confirm: 'Confirm booking',
      noPackages: {
        title: 'You have no active plan yet',
        hint: 'Take the 30-second level check and we\'ll suggest the right plan.',
        cta: 'Take level check'
      }
    },
    success: {
      title: 'Booking confirmed!',
      subtitle: 'We\'ve held this slot for you.',
      orderTitle: 'Order details',
      teacher: 'Tutor',
      scheduledAt: 'When',
      tzNote: 'Shown in your timezone ({tz})',
      duration: 'Duration',
      minutes: '{n} minutes',
      package: 'Plan used',
      packageFallback: '1 class deducted',
      status: 'Status',
      emailNote: 'We\'ll send a reminder before class. Review opens when class ends.',
      backHome: 'Back to home',
      viewOrders: 'View my orders'
    },
    status: {
      upcoming: 'Upcoming',
      cancelled: 'Cancelled',
      finished: 'Finished',
      refunding: 'Refunding',
      refunded: 'Refunded',
      no_show_student: 'Student no-show',
      no_show_teacher: 'Teacher no-show',
      abnormal: 'Abnormal',
      to_review: 'To review'
    },
    teacherBlocked: {
      title: 'Notice',
      message: 'Teacher accounts cannot purchase courses or book classes. Please switch to a student account.',
      tooltip: 'Teacher accounts cannot purchase. Please switch to a student account.',
      shortHint: 'Teachers cannot purchase'
    }
  },
  myOrders: {
    title: 'My orders',
    subtitle: 'All your booked classes',
    backHome: 'Back to home',
    loadFailed: 'Failed to load orders. Please retry.',
    tabs: {
      all: 'All',
      upcoming: 'Upcoming',
      toReview: 'To review',
      finished: 'Finished',
      cancelled: 'Cancelled',
      refunding: 'Refunding'
    },
    empty: {
      all: 'You don\'t have any orders yet. Pick a tutor to get started.',
      upcoming: 'No upcoming classes.',
      toReview: 'Nothing to review yet.',
      finished: 'No finished classes yet.',
      cancelled: 'No cancelled orders.',
      refunding: 'No refunds in progress.'
    },
    browseTeachers: 'Browse tutors',
    card: {
      scheduledAt: 'When',
      tzSuffix: 'Your timezone ({tz})',
      duration: '{n} minutes',
      package: 'Plan used',
      packageFallback: '1 class deducted',
      cancelReason: 'Reason',
      refundedClass: '1 class refunded to your plan',
      notRefundedClass: 'Class not refunded (within 24h)',
      enterClass: 'Enter classroom',
      enterClassNotYet: 'Opens 5 minutes before class',
      cancel: 'Cancel booking'
    },
    cancelDialog: {
      title: 'Cancel this booking?',
      policyHeading: 'Cancellation policy',
      policy24hPlus: '24h+ before class — full refund of 1 class to your plan',
      policy24hWithin: 'Within 24h of class — class is forfeited, no refund',
      willRefund: 'You\'re cancelling {hours}h before class. 1 class will be refunded to your plan.',
      willNotRefund: 'It\'s less than 24h to class. The class will be forfeited.',
      alreadyStarted: 'Class time has passed. Please contact support to handle this order.',
      reasonLabel: 'Reason (optional)',
      reasonPlaceholder: 'A short note helps us improve...',
      keepIt: 'Keep booking',
      confirm: 'Yes, cancel',
      submitting: 'Cancelling...',
      success: 'Booking cancelled',
      error: 'Cancel failed. Please retry.'
    }
  },
  classroom: {
    title: 'Classroom',
    back: 'Back',
    backToOrders: 'Back to my orders',
    loading: 'Joining the classroom...',
    refresh: 'Refresh classroom',
    subtitle: {
      student: 'Tutor {nickname} · {duration} min',
      teacher: 'Student {nickname} · {duration} min'
    },
    network: {
      online: 'Connected',
      offline: 'Offline',
      offlineTip: 'You are offline. Please check your WiFi or 4G.'
    },
    error: {
      loadFailed: 'Failed to load classroom',
      retryHint: 'Please check your network and retry, or contact support',
      contactSupport: `If the issue persists, contact us at hello{'@'}mandarly.com`,
      autoRetry: 'Retry #{n} in {seconds}s...',
      manualRetry: 'Retry now',
      noJoinUrl: 'Classroom token missing. Please contact support.'
    },
    leaveConfirm: {
      title: 'Leave classroom',
      message: 'Are you sure you want to leave? The class is still in progress and leaving early may affect your lesson record.',
      ok: 'Leave',
      cancel: 'Stay'
    },
    fallback: {
      title: 'Class ended',
      message: 'The class is over 5 minutes past its scheduled duration without an end signal. Go to review or back to orders?',
      toReview: 'Write review',
      toOrders: 'Back to orders',
      continue: 'Stay in class'
    },
    stub: {
      title: 'Mock classroom (local dev)',
      desc: 'Placeholder shown while LCIC credentials are not yet provisioned. Once LCIC_MODE=real with real keys, this will be replaced by the Tencent LCIC live classroom.',
      classId: 'Class ID',
      role: 'Role',
      userId: 'User ID',
      token: 'Token',
      hint: 'This page only renders in local / dev. Production traffic never reaches it.'
    }
  },
  auth: {
    login: {
      title: 'Sign in to Mandarly',
      emailTab: 'Email',
      phoneTab: 'Phone',
      socialTab: 'Social',
      methodLabel: 'Sign-in method',
      email: 'Email',
      phone: 'Phone number',
      password: 'Password',
      code: 'Verification code',
      submit: 'Sign in',
      submitting: 'Signing in...',
      googleBtn: 'Google',
      appleBtn: 'Apple ID',
      forgotPassword: 'Forgot password?',
      noAccount: "Don't have an account?",
      register: 'Sign up',
      socialDivider: 'Or continue with',
      processing: 'Processing...',
      socialNotConfigured: 'This login method is coming soon',
      googleUnsupportedBrowserTitle: 'Open in a browser',
      googleUnsupportedBrowserMessage: 'You are currently using the in-app browser in {browser}. Google sign-in is blocked there. Please use the top-right menu to open this page in Safari / Chrome, or copy the link into Safari / Chrome and try Google again. You can also sign in with email or phone first.'
    },
    phone: {
      countryCode: 'Country/region code',
      nationalPlaceholder: 'Enter phone number',
      required: 'Enter your phone number',
      invalid: 'Enter a valid international phone number'
    },
    validation: {
      emailRequired: 'Enter your email',
      emailInvalid: 'Enter a valid email address',
      codeRequired: 'Enter the verification code',
      codeLength: 'Enter the 6-digit verification code',
      termsRequired: 'Please read and agree to the terms',
      realNameRequired: 'Please enter your real name'
    },
    register: {
      title: 'Create your Mandarly account',
      methodLabel: 'Sign-up method',
      socialDivider: 'Or sign up with',
      iAmStudent: "I'm a student",
      iAmTeacher: "I'm a teacher",
      studentHint: 'Create a learner account, claim your trial lesson, and start booking tutors.',
      teacherEntryText: 'Want to join Mandarly as a tutor?',
      teacherEntryAction: 'Apply as a tutor',
      teacherReviewNotice: 'Teacher registration requires qualification review (usually 1–3 business days) and is separate from learner sign-up.',
      teacherIntro: {
        heading: 'Join the Mandarly teacher team',
        points: [
          '1-on-1 Mandarin speaking classes for Chinese learners in Hong Kong and overseas',
          'Earn per class, set your own schedule',
          'After registration, submit qualifications for review (Putonghua cert / degree / teaching experience)'
        ]
      },
      nickname: 'Nickname (optional)',
      realName: 'Real name',
      realNamePlaceholder: 'Enter your real name',
      referralCode: 'Referral code (optional)',
      agreeTerms: 'I have read and agree to the {terms} and {privacy}',
      termsLabel: 'Terms of Service',
      privacyLabel: 'Privacy Policy',
      submit: 'Sign up',
      haveAccount: 'Already have an account?',
      goLogin: 'Sign in'
    },
    resetPassword: {
      title: 'Reset password',
      email: 'Email',
      code: 'Verification code',
      newPassword: 'New password',
      confirmPassword: 'Confirm new password',
      confirmPasswordRequired: 'Confirm your new password',
      passwordMismatch: 'Passwords do not match',
      sendCode: 'Send code',
      sentTo: 'Verification code sent to {email}',
      next: 'Next',
      success: 'Password reset. Please sign in again.',
      unavailable: 'This feature is temporarily unavailable',
      submit: 'Submit',
      backToLogin: 'Back to sign in'
    },
    code: {
      send: 'Send code',
      resend: 'Resend ({n}s)',
      sent: 'Sent',
      cooldown: 'Please wait before retrying'
    },
    callback: {
      processing: 'Signing you in, please wait...',
      failed: 'Sign in failed. Please try again.'
    },
    profile: {
      title: 'My profile',
      basicInfo: 'Basic info',
      nickname: 'Nickname',
      avatar: 'Avatar',
      locale: 'Language',
      timezone: 'Timezone',
      bindings: 'Connected accounts',
      bindEmail: 'Connect email',
      bindPhone: 'Connect phone',
      bindGoogle: 'Connect Google',
      bindApple: 'Connect Apple',
      unbind: 'Disconnect',
      boundAt: 'Connected on',
      save: 'Save',
      saved: 'Saved',
      referralCode: 'My referral code'
    },
    error: {
      invalidCredentials: 'Email or password is incorrect',
      codeExpired: 'Verification code has expired',
      codeInvalid: 'Verification code is incorrect',
      codeCooldown: 'Too many requests. Please wait and retry.',
      emailExists: 'This email is already registered',
      phoneExists: 'This phone number is already registered',
      teacherEmailExists: 'This email is already in use. Please use a different email to register as a teacher.',
      teacherPhoneExists: 'This phone number is already in use. Please use a different phone number to register as a teacher.',
      userNotFound: 'Account not found',
      userFrozen: 'Account is frozen. Please contact support.',
      referralInvalid: 'Invalid referral code',
      socialNotConfigured: 'This sign-in method is currently unavailable',
      socialFetchFailed: 'Social service temporarily unavailable. Please use another sign-in method.',
      tokenExpired: 'Your session has expired',
      smtpNotConfigured: 'Email service is temporarily unavailable',
      passwordWeak: 'Password must be at least 8 characters with letters and numbers',
      rateLimitDaily: 'Daily send limit reached',
      loginFailedLocked: 'Too many failed attempts. Please try again in 15 minutes.',
      ipRegisterLimit: 'Too many registration requests from this IP',
      socialAccountConflict: 'This account is already linked to another user'
    },
    heroQuote: {
      login: 'Welcome back · Continue your Chinese journey',
      register: 'Join 200+ native teachers worldwide · Start speaking confidently'
    }
  },
  authHero: {
    login: { title: 'Learn Chinese 1-on-1 with native teachers', subtitle: '600+ certified tutors · learners from 20+ countries' },
    register: { title: 'Sign up in 30 seconds, start your Chinese journey', subtitle: '1 free trial lesson on us' },
    teacherRegister: { title: 'Join Mandarly — share your Chinese with the world', subtitle: 'Flexible schedule · global learners · weekly payouts' },
    resetPassword: { title: 'No worries — resetting your password is easy', subtitle: '' },
    trust: {
      rating: '⭐ {rating} average rating',
      teachers: '{count}+ certified tutors',
      countries: 'Learners from {count}+ countries'
    }
  },
  level_check: {
    q1: {
      text: 'How would you describe your current Chinese level?',
      opt: {
        complete_beginner: 'Complete beginner — never studied',
        some_basics: 'Studied a bit — know some words',
        simple_conversation: 'Can hold simple conversations',
        fairly_fluent: 'Fairly fluent — want to refine'
      }
    },
    q2: {
      text: 'What\'s your main learning goal?',
      opt: {
        career: 'Career & job',
        business: 'Business communication',
        travel: 'Travel & daily life',
        hsk_exam: 'HSK exam preparation',
        for_kids: 'For my child',
        just_for_fun: 'Just for fun'
      }
    },
    q3: {
      text: 'How many classes per week do you plan?',
      opt: {
        one_per_week: '1 class/week',
        two_per_week: '2 classes/week',
        intensive: 'Intensive (3+/week)',
        not_sure: 'Not sure yet'
      }
    },
    q4: {
      text: 'Who is the learner?',
      opt: {
        myself: 'Myself',
        child: 'My child (under 12)',
        teen: 'My teenager (12-18)',
        multiple: 'Multiple people'
      }
    }
  },
  review: {
    title: { write: 'Review this class', edit: 'Edit Review' },
    subtitle: {
      write: 'Share your class experience to help others pick a tutor',
      edit: 'You can update within 24h after first submitting'
    },
    courseTime: 'Class time {time}',
    step1: { title: 'Overall rating', guide: 'Rate your tutor (half-star supported)' },
    step2: { title: 'Specific feedback' },
    step3: { title: 'Confirm & submit' },
    rating: { label: 'Rating' },
    rate: {
      text: { 1: 'Poor', 2: 'Bad', 3: 'OK', 4: 'Good', 5: 'Excellent' }
    },
    label: { rating: 'Rating', content: 'Your review', tags: 'Highlights' },
    placeholder: { content: 'A line or two about the class... (optional)' },
    ratingDesc: {
      1: 'Poor', 2: 'Below average', 3: 'OK', 4: 'Good', 5: 'Excellent'
    },
    tags: {
      addCustom: 'Custom tag',
      customLimit: 'Up to {n} tags × {len} chars each',
      customLimitReached: 'Custom tag limit reached ({n})',
      customPlaceholder: 'Enter custom tag (≤{n} chars)',
      maxHint: 'Pick up to {n}',
      remove: 'Remove {tag}'
    },
    text: { label: 'Review (optional)', placeholder: 'Tell us about this class...' },
    anonymous: { label: 'Post anonymously', desc: 'Public list shows you as "Anonymous student"' },
    editWindow: {
      expired: 'Edit window closed',
      remaining: '{hours}h left to edit',
      urgent: '{hours} min left to edit'
    },
    action: { cancel: 'Cancel', next: 'Next', prev: 'Back' },
    submit: 'Submit review',
    submitting: 'Submitting…',
    hint: {
      tagsLimit: 'Pick up to 3',
      editWindowLeft: '{hours}h left to edit',
      expired: 'Edit window expired'
    },
    btn: {
      write: 'Write Review', edit: 'Edit Review',
      submit: 'Submit', save: 'Save', back: 'Back'
    },
    error: {
      notFinished: 'Only finished classes can be reviewed',
      tooManyTags: 'Pick up to 3 tags',
      editExpired: 'Edit window expired (24h after first submission)',
      submitFailed: 'Failed to submit. Please retry.',
      loadFailed: 'Load failed. Please retry.'
    },
    success: {
      submitted: 'Review submitted',
      updated: 'Review updated',
      title: 'Review submitted',
      subtitle: 'Thanks for your feedback. You can edit within 72h.',
      redirect: 'Returning to my lessons',
      backToOrders: 'Back to my lessons'
    },
    empty: { teacherList: 'No reviews yet' },
    publicSection: {
      title: 'Student Reviews ({n})',
      loadMore: 'Load more',
      end: '— end of list —'
    },
    card: {
      starsAria: '{n} star(s)',
      submittedAt: 'Reviewed on {time}',
      editedAt: 'Edited on {time}',
      youWrote: 'Your review'
    },
    tag: {
      patient: 'Patient',
      native_accent: 'Native accent',
      good_pace: 'Good pace',
      well_prepared: 'Well prepared',
      audio_issue: 'Audio issue',
      late: 'Late',
      interactive: 'Interactive',
      good_material: 'Great materials',
      std_pronunciation: 'Standard pronunciation',
      humorous: 'Humorous',
      encouraging: 'Encouraging',
      proper_difficulty: 'Right difficulty'
    }
  },
  myPackages: {
    title: 'My Packages',
    subtitle: 'Active packages and purchase history',
    tabs: { all: 'All', active: 'Active', expired: 'Expired', exhausted: 'Used up' },
    empty: {
      all: 'No packages yet — book a class to get started',
      active: 'No active packages',
      expired: 'No expired packages',
      exhausted: 'No used-up packages'
    },
    loadFailed: 'Load failed. Please retry.',
    browseTeachers: 'Browse Teachers',
    card: {
      remaining: '{n} left',
      total: '{n} total',
      weekly: '{n}/week',
      expireAt: 'Expires {date}',
      purchasedAt: 'Acquired {date}',
      free: 'Free trial',
      statusActive: 'Active',
      statusExpired: 'Expired',
      statusExhausted: 'Used up'
    },
    source: {
      purchase: 'Purchased',
      free_trial: 'Free trial',
      register_grant: 'Free trial',
      admin_grant: 'Granted',
      referral_reward: 'Referral reward'
    }
  },
  profileTeacher: {
    stat: {
      title: 'My Teaching Stats',
      avgRating: 'Avg Rating',
      reviewCount: 'Reviews',
      orderCount: 'Finished',
      noReviews: '—',
      loadFailed: 'Failed to load stats'
    },
    myReviews: {
      title: 'Reviews from Students',
      empty: 'No reviews yet',
      loadMore: 'Load more',
      end: '— end of list —'
    }
  },
  payment: {
    button: {
      checkout: 'Buy Now',
      retry: 'Retry'
    },
    processing: {
      title: 'Processing payment',
      tip: 'Please wait, confirming your payment...'
    },
    success: {
      title: 'Payment successful!',
      message: 'Your package is ready. Book your first class now!'
    },
    cancel: {
      title: 'Payment cancelled',
      message: 'You cancelled the payment. No charge was made.',
      retry: 'Try again',
      back: 'My packages'
    },
    expired: {
      title: 'Payment is being processed',
      message: 'Payment confirmation can take a few seconds. If you were charged, the package will appear in My packages after it is activated.'
    },
    amount: {
      usd: 'Amount (USD)'
    },
    discount: {
      applied: 'Referral discount applied'
    },
    status: {
      pending: 'Pending',
      paid: 'Paid',
      failed: 'Failed',
      expired: 'Expired',
      refunded: 'Refunded',
      partial_refunded: 'Partially refunded'
    },
    viewMyPackages: 'View my packages'
  },
  refund: {
    apply: {
      button: 'Request refund',
      title: 'Request a refund',
      reason: {
        label: 'Reason for refund',
        placeholder: 'Please explain your reason (min 10 chars)',
        min10: 'Reason must be at least 10 characters'
      },
      pendingButton: 'Refund in review',
      success: 'Refund request submitted, pending review',
      error: 'Submission failed, please try again'
    },
    myList: {
      title: 'My refund requests',
      empty: 'No refund requests yet'
    },
    status: {
      pending: 'Pending',
      approved: 'Approved',
      refunded: 'Refunded',
      rejected: 'Rejected',
      failed: 'Failed'
    },
    auditNote: {
      label: 'Admin note'
    }
  },
  referral: {
    title: 'My referral stats',
    code: {
      your: 'My referral code',
      copy: 'Copy',
      copied: 'Code copied!'
    },
    stat: {
      invited: 'Friends invited',
      reward: 'Free classes earned'
    },
    discount: {
      applied: 'Referral discount applied'
    },
    tip: {
      invite: 'Invite a friend to buy a package and earn a free class',
      firstOrder: 'Your friend gets a discount on their first order',
      howItWorks: 'Both you and your friend are rewarded after first purchase'
    }
  },
  package: {
    title: 'Choose a package',
    subtitle: 'Purchase a lesson package and start your Mandarin journey',
    empty: 'No packages available, please contact support',
    buyNow: 'Buy now',
    weeklyCount: 'Classes / week',
    validityWeeks: 'Total classes',
    priceUsd: 'Price',
    referralCode: {
      placeholder: 'Referral code (optional)'
    }
  },
  packages: {
    title: 'Lesson Packages',
    subtitle: 'Pick a pace that suits you. Log in to see full pricing.',
    pricing: {
      eyebrow: 'Flexible Plans for Every Learner',
      titlePrefix: 'Choose the Right Plan for Your',
      titleHighlight: 'Chinese',
      titleSuffix: 'Learning Goals',
      subtitle: '',
      lessonFormat: 'All plans include 30-minute 1-on-1 private Mandarin lessons with native-speaking tutors.',
      lessonFormatParts: {
        prefix: 'All plans include ',
        duration: '30-minute',
        middle1: ' ',
        oneOnOne: '1-on-1',
        middle2: ' ',
        private: 'private',
        suffix: ' Mandarin lessons with native-speaking tutors.'
      },
      pills: {
        tutors: 'Native-speaking tutors',
        scheduling: 'Flexible Scheduling',
        personalized: 'Personalized Learning',
        secure: 'Safe, secure & trusted'
      },
      badge: {
        mostPopular: 'Most Popular',
        bestValue: 'Best Value'
      },
      priceLocked: 'Log in to view price',
      perLesson: '/ lesson',
      lessonsCount: '{n} lessons',
      durationMonths: '{n} months',
      durationDays: '{n} days',
      ctaLoggedOut: 'Book Free Trial',
      ctaLoggedIn: 'Choose Plan',
      ctaLoggedInPlan: 'Choose {plan}',
      ctaDefaultPlan: 'Plan',
      cards: {
        starter: {
          name: 'Starter Plan',
          ctaName: 'Starter',
          subtitle: 'Great for casual learners',
          point1: '1 lesson per week',
          point2: 'Flexible booking',
          point3: 'Native teachers',
          point4: 'Beginner friendly'
        },
        standard: {
          name: 'Standard Plan',
          ctaName: 'Standard',
          subtitle: 'Best for steady progress',
          point1: '2 lessons per week',
          point2: 'Faster improvement',
          point3: 'Priority booking',
          point4: 'Personalized learning path'
        },
        annualLite: {
          name: 'Annual Plan',
          ctaName: 'Annual',
          subtitle: 'For consistent learners',
          point1: '1 lesson per week',
          point2: 'Long-term consistency',
          point3: 'Better learning retention',
          point4: 'Save more with annual plan'
        },
        premiumAnnual: {
          name: 'Premium Annual',
          ctaName: 'Premium',
          subtitle: 'Best value for long-term learners',
          point1: '2 lessons per week',
          point2: 'Best long-term value',
          point3: 'Dedicated learning support',
          point4: 'Parent progress reports'
        }
      },
      why: {
        titlePrefix: 'Why Choose',
        titleHighlight: 'Mandarly?',
        native: {
          title: 'Native Tutors',
          desc: 'Carefully selected and certified Mandarin teachers.'
        },
        schedule: {
          title: 'Flexible Scheduling',
          desc: 'Book lessons anytime, across all time zones.'
        },
        personalized: {
          title: 'Personalized Learning',
          desc: 'Lessons tailored to your goals and learning style.'
        },
        progress: {
          title: 'Structured Progress',
          desc: 'Track improvement and achieve your goals step by step.'
        }
      },
      testimonials: {
        title: 'Loved by Learners Worldwide',
        emily: {
          name: 'Emily, California',
          quote: 'My daughter finally enjoys learning Chinese!'
        },
        kevin: {
          name: 'Kevin, Singapore',
          quote: 'Flexible, professional, and very engaging lessons.'
        },
        sophie: {
          name: 'Sophie, London',
          quote: 'Great teachers and a clear learning path. Highly recommend!'
        }
      }
    },
    banner: {
      hero: 'Flexible monthly or yearly plans',
      freeTrial: {
        text: 'Get 1 free trial lesson (25 min) on signup',
        subtext: 'Choose any tutor · No credit card required',
        cta: 'Claim now',
        claimed: 'You have already claimed your free trial'
      }
    },
    teacherEntry: {
      eyebrow: 'Continue booking',
      title: 'Choose a package for this tutor',
      titleWithName: 'Choose a package for {name}',
      desc: 'After buying lessons, return to the tutor detail page and finish the 1-on-1 Mandarin booking you started.',
      backToTeacher: 'Back to tutor'
    },
    currency: { label: 'Currency', hkd: 'HKD', usd: 'USD', cny: 'CNY' },
    rules: {
      title: 'Package rules',
      multiPackage: 'You may hold multiple packages; bookings consume the earliest-expiring lessons first',
      expiry: 'Unused lessons expire at the end of the validity period (reminders sent 7/3/1 days before)',
      refund: 'Cancellations 24h+ before lesson are fully refunded; see Terms § 5',
      currency: 'Packages are charged in their listed currency. Admins can publish or unpublish each currency independently'
    },
    compare: {
      title: 'Compare all packages', expand: 'Expand comparison', collapse: 'Collapse',
      buy: 'Buy', loginToViewPrice: 'Log in to view price', loginAction: 'Log in / Sign up', priceLockedNote: 'Log in to view full package prices. Benefits are still visible for comparison.', yes: '✓', no: '—', free: 'Free',
      durationMonths: '{n} months', durationDays: '{n} days',
      col: { package: 'Package' },
      row: {
        pricePerLesson: 'Price / lesson', totalPrice: 'Total price', totalLessons: 'Total lessons',
        validity: 'Validity', booking: 'Free booking', refund: '24h refund',
        upgrade: 'Upgrade / downgrade', discount: 'Discount', action: 'Action'
      }
    },
    confirm: {
      title: 'Confirm Order', summary: 'Order summary',
      tip: 'Clicking "Proceed to payment" will redirect you to Stripe secure checkout',
      refundTip: 'Cancellations 24h+ before lesson are fully refunded. See',
      termsLink: 'Terms § 5',
      cancel: 'Cancel', confirm: 'Proceed to payment', confirming: 'Processing…',
      durationMonths: '{n} months from purchase', durationDays: '{n} days',
      sessionsCount: '~{n} lessons', sessionsCountWeekly: '~{n} lessons ({weekly} / week)',
      field: {
        package: 'Package', sessions: 'Lessons', duration: 'Validity',
        currency: 'Currency', subtotal: 'Subtotal', discount: 'Discount', total: 'Total'
      }
    },
    success: { message: 'Package activated', returnTo: 'Continue booking' },
    empty: {
      title: 'No packages available, please contact support',
      desc: 'Packages may be sold out or under maintenance',
      currencyUnavailable: 'No {currency} packages are currently available. You can view HKD packages instead',
      viewHkd: 'View HKD packages'
    },
    error: { loadFailed: 'Failed to load packages, please retry', checkoutFailed: 'Failed to start payment, please retry' }
  },
  seo: {
    home: {
      title: 'Online Chinese Classes for Kids & Adults | Mandarly',
      description: 'Mandarly offers live one-on-one online Chinese classes for kids, teens, and adults. Learn Mandarin speaking with friendly native Chinese teachers.',
      keywords: 'online Chinese classes, Chinese speaking classes, Mandarin classes for kids, one-on-one Mandarin lessons, learn Mandarin online, native Chinese teachers'
    },
    teachers: {
      title: 'Mandarin Tutors · Native Chinese Teachers · Mandarly',
      description: 'Browse Mandarly\'s vetted native Mandarin teachers. Filter by speaking, business Chinese, HSK or kids tracks. Book 1-on-1 lessons in your own time slot.',
      keywords: 'Mandarin tutor, Chinese teacher, online Chinese class, HSK tutor, business Chinese teacher'
    },
    packages: {
      title: 'Pricing · Flexible Mandarin Lesson Plans · Mandarly',
      description: 'Choose flexible Mandarin lesson plans for kids, beginners, and global learners. Learn 1-on-1 with native tutors on Mandarly.',
      keywords: 'Mandarin pricing, Chinese lesson packages, online Mandarin lessons, 1-on-1 Chinese tutoring',
      catalogName: 'Mandarly lesson plans'
    },
    teacherDetail: {
      titleTemplate: '{name} · Mandarin Tutor · Mandarly',
      titleFallback: 'Mandarin Tutor · Mandarly',
      descriptionTemplate: 'Book 1-on-1 Mandarin lessons with {name} on Mandarly. {intro}',
      descriptionFallback: 'Book 1-on-1 Mandarin lessons with this native Chinese teacher on Mandarly.'
    },
    levelCheck: {
      title: 'Free Chinese Level Test · 30-Second Match · Mandarly',
      description: 'Take a free 30-second Chinese level test on Mandarly. Get a personalized Mandarin tutor and lesson plan. No credit card, no signup needed.',
      keywords: 'Chinese level test, Mandarin placement test, free Chinese test, HSK level check'
    },
    levelCheckResult: {
      title: 'Your Chinese Level Result · Mandarly',
      description: 'View your Mandarin level result and continue your study path after sign-up.'
    },
    curriculum: {
      title: 'Online Chinese Classes Curriculum | Mandarly Mandarin Speaking Courses',
      description: 'Explore Mandarly’s online Chinese classes curriculum, from beginner Chinese to advanced Mandarin speaking. One-on-one Mandarin lessons for kids, teens, and adults.',
      keywords: 'online Chinese curriculum, Mandarin speaking courses, beginner Chinese, HSK Mandarin lessons, Chinese classes for kids'
    }
  },
  curriculum: {
    hero: {
      eyebrow: 'Curriculum',
      title: 'Mandarly Chinese Curriculum',
      subtitle: 'A clear learning path from beginner Chinese to confident Mandarin communication.',
      cta: 'Book a Trial Class'
    },
    overview: {
      eyebrow: 'Curriculum Overview',
      title: 'A structured path for real Mandarin speaking progress',
      desc: 'Mandarly offers a structured online Chinese curriculum designed for overseas learners. Our one-on-one Mandarin speaking classes help students build confidence step by step, from basic pronunciation and daily conversation to advanced Chinese communication.'
    },
    labels: {
      learners: 'Suitable learners',
      goal: 'Goal',
      skills: 'Skills overview'
    },
    stages: [
      {
        name: 'Beginner Chinese',
        chineseName: '零基础中文入门',
        learners: 'Students with no Chinese background, or learners who only know a few simple words such as “你好” and “谢谢”.',
        goal: 'Build interest in Chinese, understand basic Mandarin pronunciation, and speak simple greetings and self-introductions.',
        hsk: 'Pre-HSK / HSK 1',
        skills: ['Chinese pronunciation basics', 'Pinyin introduction', 'Mandarin tones practice', 'Greetings and self-introduction', 'Numbers, age, family, colors, food'],
        button: 'Explore Beginner Chinese'
      },
      {
        name: 'Foundation Mandarin',
        chineseName: '中文基础提升',
        learners: 'Students who know basic pinyin and can speak simple sentences, but need more vocabulary and confidence.',
        goal: 'Build a strong Mandarin foundation, expand daily vocabulary, learn sentence patterns, and communicate in simple daily conversations.',
        hsk: 'HSK 1-2',
        skills: ['Pinyin and tones improvement', 'Daily conversation practice', 'Basic sentence patterns', 'Listening comprehension', 'Short speaking tasks'],
        button: 'Explore Foundation Mandarin'
      },
      {
        name: 'Intermediate Chinese',
        chineseName: '中级中文表达',
        learners: 'Students with basic Chinese communication skills who want better fluency, listening, structure, and natural speaking.',
        goal: 'Improve Mandarin speaking fluency across daily life, school, travel, culture, and personal interests.',
        hsk: 'HSK 2-4',
        skills: ['Mandarin fluency speaking practice', 'Longer sentence building', 'Topic-based conversation', 'Storytelling practice', 'Opinion expression'],
        button: 'Explore Intermediate Chinese'
      },
      {
        name: 'Advanced Mandarin',
        chineseName: '高级中文沟通',
        learners: 'Students with a strong Chinese foundation who want advanced speaking, culture, presentation, writing support, or HSK preparation.',
        goal: 'Move from being able to speak Chinese to speaking Mandarin naturally, logically, and confidently in deeper conversations.',
        hsk: 'HSK 4-6+',
        skills: ['Advanced Mandarin conversation', 'Debate and opinion discussion', 'Chinese presentation skills', 'News and culture topics', 'HSK preparation support'],
        button: 'Explore Advanced Mandarin'
      }
    ],
    bottomCta: {
      title: 'Not sure which level is right for you?',
      desc: 'Book a trial class and our teacher will help you choose the best Mandarin learning path.',
      button: 'Book a Trial Class'
    }
  },
  comingSoon: {
    badge: 'Coming Soon',
    cta: { backHome: 'Back to home' },
    textbook: {
      title: 'Textbook is in the works',
      desc: 'Our curriculum team is preparing handpicked Mandarin textbooks across levels. Lesson notes and exercises will land here soon.'
    },
    resources: {
      title: 'Resource center launching soon',
      desc: 'Study guides, vocabulary flashcards, culture reads and free practice materials are on the way.'
    }
  },
  header: {
    nav: {
      home: 'Home',
      curriculum: 'Curriculum',
      teachers: 'Tutors',
      packages: 'Pricing',
      levelCheck: 'Level Check',
      myOrders: 'My Lessons',
      // D26: anonymous home anchor nav
      features: 'Why Us',
      recommendedTeachers: 'Tutors',
      packagesSection: 'Plans',
      stories: 'Stories',
      faq: 'FAQ',
      // D27: external content entries
      textbook: 'Textbook',
      resources: 'Resources',
      podcast: 'Podcast',
      // D27: dropdown group labels
      aboutGroup: 'About',
      resourcesGroup: 'Resource'
    },
    action: {
      login: 'Sign in',
      register: 'Sign up',
      becomeTeacher: 'Become a tutor'
    },
    menu: {
      profile: 'My Account',
      profileTeacher: 'Profile',
      myPackages: 'My Packages',
      myOrdersTeacher: 'My Orders',
      income: 'Earnings',
      withdrawal: 'Withdraw',
      myRefunds: 'My Refunds',
      logout: 'Sign out'
    }
  },
  tabbar: {
    home: 'Home',
    teachers: 'Tutors',
    myOrders: 'My Lessons',
    profile: 'Me'
  },
  footer: {
    section: {
      about: 'About Us',
      service: 'Service',
      legal: 'Legal',
      contact: 'Contact',
      payment: 'Supported Payments',
      follow: 'Follow Us'
    },
    about: {
      intro: 'About',
      jobs: 'Careers',
      teacherJoin: 'Become a tutor',
      contact: 'Contact us'
    },
    legal: {
      privacy: 'Privacy Policy',
      terms: 'Terms of Service'
    },
    social: {
      followOn: 'Follow us on {name}',
      wechatScan: 'Scan to contact us on WeChat',
      wechatZoomHint: 'Click to enlarge WeChat QR'
    },
    copyright: '© 2026 MANDARLY TECHNOLOGY LIMITED · Hong Kong'
  },
  support: {
    open: 'Open support',
    close: 'Close support',
    title: 'Mandarly Support',
    subtitle: 'WeChat · WhatsApp · Email',
    directHint: 'Choose a channel and our team will follow up as soon as possible.',
    greeting: 'Hi! Ask a question and we will look for an answer, or contact support directly.',
    placeholder: 'Type a question, press Enter to send',
    send: 'Send',
    noMatch: 'We could not find a matching answer yet. You can contact support directly.',
    wechatCopied: 'WeChat ID copied',
    wechatCopyFailed: 'Copy failed. Please copy the WeChat ID manually: {id}',
    openFailed: 'Could not open the link. Please try again later or take a screenshot to reach our team.',
    error: 'Support is temporarily unavailable. Please try again later.',
    channel: {
      wechat: 'WeChat',
      whatsapp: 'WhatsApp',
      email: 'Email',
      other: 'Contact channel',
      pending: 'This contact channel is being prepared.'
    }
  },
  teacherDetail: {
    hero: {
      rating: 'Rating',
      reviews: '{count} reviews',
      todayAvailable: '{count} slots today',
      favorite: 'Favorite'
    },
    video: { title: 'Trial Lesson Video', placeholder: 'Click to play' },
    intro: { title: 'About Me', more: 'Show more', less: 'Show less' },
    expertise: { title: 'Teaching Expertise' },
    book: {
      title: 'Book',
      priceUnit: '/ {n} min',
      selectSlot: 'Select a time slot',
      selectedSlot: 'Selected',
      bookNow: 'Book Now',
      buyPackageForTeacher: 'Buy package for this tutor',
      viewOtherTeachers: 'View other tutors',
      emptyHint: 'Please pick a slot first',
      packageCreditPricing: 'Booked with package credits',
      loginToViewPackages: 'Log in to view packages',
      refundTip: 'Full refund if cancelled 24h before class'
    },
    review: {
      title: 'Student Reviews ({n})',
      empty: 'No reviews yet · Be the first to leave one',
      lowVolume: 'Be the first student to leave a review',
      seeAll: 'Load more ({count} remaining)'
    },
    related: {
      title: 'Related Tutors',
      viewAll: 'View all',
      emptyTitle: 'No more related tutors yet',
      emptyDesc: 'Go back to the tutor list and filter by time, accent, or learning goal.',
      emptyAction: 'Browse all tutors',
      loadFailed: 'Related tutors failed to load'
    },
    balance: {
      insufficient: 'You don’t have enough lessons left. Buy a package?',
      goPackages: 'Buy Package'
    }
  },
  teacherCard: {
    badge: { todayAvailable: 'Available Today', newTeacher: 'New Teacher', introVideo: 'Video' },
    avatarAlt: 'Tutor {name} avatar',
    favorite: 'Favorite',
    favoriteSoon: 'Coming soon',
    priceUnit: '/ 25 min',
    reviewCount: '({count} reviews)',
    noReviews: 'No reviews yet',
    finishedLessons: '{count} completed lessons',
    newTeacherQualified: 'New tutor · verified',
    timezoneMeta: '{location} · {offset}'
  },
  packageCard: {
    cta: { buy: 'Buy Now', claimTrial: 'Claim Free Trial', loginToViewPrice: 'Log in to view price', loginRegister: 'Log in / Sign up', loading: 'Loading…' },
    purchased: 'Owned',
    perSessionUnit: '/ lesson',
    sessions: '{n} lessons',
    period: { months: '{n} months', days: '{n} days' }
  },
  profile: {
    title: 'Profile',
    user: { guest: 'Guest' },
    menu: {
      account: 'Account',
      referral: 'Referrals',
      teacherStats: 'Teaching stats',
      myPackages: 'My packages',
      myOrders: 'My orders',
      myRefunds: 'My refunds',
      logout: 'Log out'
    },
    account: {
      saved: 'Saved',
      section: { basic: 'Basic info', bindings: 'Account bindings', password: 'Change password', danger: 'Danger zone' },
      field: { nickname: 'Nickname', email: 'Email', locale: 'Language', timezone: 'Timezone', referralCode: 'My referral code' },
      action: { save: 'Save', changeAvatar: 'Change avatar' },
      avatar: {
        uploading: 'Uploading...',
        uploadSuccess: 'Avatar updated',
        uploadFailed: 'Avatar upload failed, please try again',
        invalidType: 'Please upload an image file (PNG / JPG / WebP / GIF)',
        tooLarge: 'Image must be under {mb}MB'
      },
      bindings: {
        email: 'Email', phone: 'Phone', google: 'Google', apple: 'Apple',
        bound: 'Bound', notBound: 'Not bound', bindNow: 'Bind now',
        bindEmail: 'Bind email', bindPhone: 'Bind phone', unbind: 'Unbind',
        unbindConfirm: 'Are you sure you want to unbind this login method?',
        unbindSuccess: 'Unbound successfully',
        bindEmailSuccess: 'Email bound successfully',
        bindPhoneSuccess: 'Phone bound successfully',
        primaryEmail: 'Primary email'
      },
      password: {
        current: 'Current password', new: 'New password', confirm: 'Confirm new password',
        action: 'Change password', required: 'Please fill all password fields',
        mismatch: 'New passwords do not match', success: 'Password changed',
        comingSoon: 'Change password coming soon'
      },
      logout: {
        title: 'Log out',
        desc: 'Log out of your account. Pending payments will not be affected.',
        confirm: 'Log out of your account? Pending payments will not be affected.',
        confirmBtn: 'Log out',
        success: 'Logged out'
      }
    },
    referral: {
      title: 'Referrals',
      codeLabel: 'Referral code', codeCopied: 'Referral code copied',
      linkLabel: 'Your invite link', linkCopied: 'Invite link copied',
      copy: 'Copy', copied: 'Copied',
      shareLabel: 'Share:',
      shareText: 'Learn Chinese with me on Mandarly — use my link to sign up and get a bonus → {link}',
      recordsTitle: 'Invite records',
      stat: { invited: 'Invited', rewarded: 'Rewarded', totalReward: 'Total reward' },
      status: { rewarded: 'Rewarded', pending: 'Pending' },
      empty: {
        title: 'You haven’t invited any friends yet',
        desc: 'Copy your invite link and share it with friends — both sides earn rewards on their first order',
        action: 'Copy invite link'
      }
    },
    teacherStats: {
      title: 'Teaching stats',
      stat: { avgRating: 'Average rating', lessonCount: 'Lessons completed', reviewCount: 'Reviews' },
      monthlyTrend: 'This month',
      monthlyPlaceholder: 'Monthly summary coming soon — will show completed lessons and upcoming sessions for the current month.'
    }
  },
  orders: {
    title: 'My lessons',
    loadFailed: 'Load failed, please try again',
    tabs: {
      upcoming: 'Upcoming', toReview: 'To review', finished: 'Finished',
      cancelled: 'Cancelled', refunding: 'Refunding', all: 'All'
    },
    empty: {
      upcoming: { title: 'No upcoming lessons', desc: 'Browse teachers and book your first class' },
      toReview: { title: 'No lessons to review', desc: 'Finished lessons appear here' },
      finished: { title: 'No finished lessons yet', desc: 'Finished lessons appear here' },
      cancelled: { title: 'No cancelled orders', desc: 'Steady attendance helps your progress' },
      refunding: { title: 'No refunding orders', desc: 'Refund progress shows here in real time' },
      all: { title: 'No orders yet', desc: 'Browse teachers and book your first class' },
      browseTeachers: 'Browse teachers'
    },
    card: {
      scheduledAt: 'Scheduled', tzSuffix: '{tz} timezone', duration: '{n} min',
      package: 'Package', packageFallback: 'Single lesson', freeTrialBadge: 'Free trial',
      cancelReason: 'Cancel reason',
      refundedClass: '✓ Lesson refunded', notRefundedClass: '✗ Lesson not refunded',
      refundProgress: 'Refund progress', channelRefundId: 'Refund ID',
      status: {
        upcoming: 'Upcoming', finished: 'Finished', finishedSettling: 'Waiting to settle',
        finishedReview: 'To review', cancelled: 'Cancelled', refunded: 'Refunded',
        refunding: 'Refunding', no_show_student: 'No-show', no_show_teacher: 'Teacher absent',
        abnormal: 'Abnormal', reviewExpired: 'Edit window closed'
      },
      action: {
        enterClass: 'Join class', returnClass: 'Return to class',
        review: '★ Write review', editReview: 'Edit review',
        cancel: 'Cancel booking', viewDetail: 'View details'
      }
    },
    countdown: {
      daysHours: '{days}d {hours}h',
      hoursMinutes: '{hours}h {minutes}m',
      minutesSeconds: '{minutes}:{seconds}',
      inProgress: 'In class', expired: 'Ended'
    },
    refundWindow: {
      over24h: 'ⓘ Cancel 24h+ ahead for full refund',
      under24h: '⚠️ Less than 24h to start, lesson will not be refunded'
    },
    refundProgress: { applied: 'Applied', reviewing: 'Reviewing', refunded: 'Refunded' },
    cancelDialog: {
      title: 'Cancel booking',
      summary: { teacher: 'Teacher', time: 'Scheduled', distance: 'Time to start' },
      countdown24Plus: '✓ Over 24h to start — lesson will be refunded in full',
      countdown24Minus: '⚠️ Less than 24h to start — lesson will not be refunded',
      alreadyStarted: 'Class already started, cannot cancel',
      reasonLabel: 'Cancel reason (optional, helps us improve)',
      reasonPlaceholder: 'Choose a reason',
      notePlaceholder: 'Additional notes (optional)',
      reasonOther: 'Other',
      reason: {
        conflict: 'Schedule conflict', illness: 'Feeling unwell', travel: 'Travel',
        noShow: 'Teacher not confirming', changedMind: 'Changed my mind'
      },
      cancel: 'Keep it', confirm: 'Confirm cancel', confirming: 'Submitting…',
      success: 'Cancelled', error: 'Cancel failed, please try again'
    }
  },
  teacherCenter: {
    nav: {
      dashboard: 'Dashboard',
      profileEdit: 'My Profile',
      qualification: 'Qualifications',
      schedule: 'Schedule',
      orders: 'Orders',
      income: 'Income',
      withdrawal: 'Withdrawal'
    },
    audit: {
      banner: {
        draft: 'Your profile has not been submitted yet. Complete your profile and qualifications, then submit for review.',
        pending: 'Submitted for review. Awaiting admin approval.',
        rejected: 'Review not passed: {reason}',
        draftAction: 'Complete profile',
        action: 'Update'
      }
    },
    dashboard: {
      title: 'Teacher Center',
      welcome: 'Hi, {name}',
      weeklyClassesLabel: 'This week',
      monthlyIncomeLabel: 'This month',
      pendingSettleLabel: 'Pending',
      ratingLabel: 'Rating',
      quickActionsTitle: 'Quick actions',
      quickActionSchedule: 'Manage schedule',
      quickActionOrders: 'View orders',
      quickActionIncome: 'View income',
      quickActionWithdrawal: 'Request withdrawal'
    },
    profile: {
      title: 'My Profile',
      subtitle: 'Complete your bio and teaching info. Saved changes will be reviewed by an admin.',
      intro: {
        label: 'Self introduction',
        placeholder: 'Briefly introduce your teaching style, background and the kinds of lessons you do best.',
        hint: 'Suggested 200–600 characters. Keep it friendly so students get a quick sense of you.'
      },
      avatar: {
        label: 'Tutor profile photo',
        upload: 'Upload photo',
        uploading: 'Uploading...',
        hint: 'Shown as the main image on your public tutor card and detail page. Use a clear square or 4:3 headshot, max {mb} MB.'
      },
      expertise: {
        label: 'Teaching focus',
        hint: 'Multi-select. Your picks will appear in student filters.'
      },
      languages: {
        label: 'Languages I speak',
        placeholder: 'Select the languages you can teach or communicate in.'
      },
      languageOptions: {
        zh: 'Chinese (Mandarin)',
        en: 'English',
        ar: 'Arabic',
        yue: 'Cantonese',
        ja: 'Japanese',
        ko: 'Korean'
      },
      accent: {
        label: 'Accent'
      },
      accentOptions: {
        mandarin_cn: 'Mandarin (Mainland)',
        mandarin_tw: 'Mandarin (Taiwan)',
        cantonese: 'Cantonese',
        mixed: 'Other / Mixed'
      },
      yearsExperience: {
        label: 'Years of experience',
        hint: 'Whole years; enter 0 if you have no prior experience.'
      },
      introVideo: {
        label: 'Self-introduction video',
        upload: 'Upload intro video',
        replace: 'Replace video',
        remove: 'Remove',
        hint: 'mp4 only. Max {mb} MB and {sec} seconds per file.'
      },
      meta: {
        qualificationCount: 'Qualifications uploaded',
        auditedAt: 'Last reviewed'
      },
      actions: {
        save: 'Save',
        saving: 'Saving…',
        submitAudit: 'Submit for review',
        submitAuditConfirm: 'Submit'
      },
      messages: {
        loadFailed: 'Failed to load profile. Please try again later.',
        saveSuccess: 'Profile saved',
        saveFailed: 'Failed to save. Please try again later.',
        submitAuditConfirm: 'Finish your edits before submitting. Once submitted, your profile will enter review and cannot be resubmitted until done. Continue?',
        submitAuditSuccess: 'Submitted. Your profile is now under review.',
        submitAuditFailed: 'Submit failed. Please try again later.',
        videoFormatInvalid: 'Only mp4 videos are supported.',
        videoTooLarge: 'Video must be smaller than {mb} MB.',
        videoTooLong: 'Video must be shorter than {sec} seconds.',
        videoUnreadable: 'Could not read the video file. Please try another file.',
        videoUploadSuccess: 'Video uploaded successfully',
        videoUploadFailed: 'Video upload failed. Please try again later.',
        avatarFormatInvalid: 'Please upload an image file.',
        avatarTooLarge: 'Image must be smaller than {mb} MB.',
        avatarUploadSuccess: 'Profile photo updated',
        avatarUploadFailed: 'Photo upload failed. Please try again later.'
      }
    },
    qualification: {
      title: 'Qualifications',
      subtitle: 'Upload identity and education documents. Documents will be reviewed by an admin after submission.',
      requiredHint: 'Required: identity document and degree certificate. Optional: teaching certificate, CET-4/CET-6 English certificate, proof of teaching experience.',
      docType: {
        label: 'Document type',
        placeholder: 'Select a document type',
        idCard: 'Identity document',
        passport: 'Passport',
        degreeCert: 'Degree certificate',
        teachingCert: 'Teaching certificate',
        englishCert: 'CET-4 / CET-6 English certificate',
        experienceProof: 'Proof of teaching experience'
      },
      upload: {
        sectionTitle: 'Upload a document',
        hint: 'jpg / png / pdf supported. Max {mb} MB per file.',
        button: 'Choose file and upload',
        success: 'Uploaded',
        failed: 'Upload failed. Please try again later.',
        errorFormat: 'Only jpg / png / pdf formats are supported.',
        errorSize: 'File exceeds {mb} MB. Please compress and retry.',
        noDocType: 'Please choose a document type first.'
      },
      list: {
        sectionTitle: 'Uploaded documents',
        empty: 'No qualification documents uploaded yet.',
        auditStatus: {
          pending: 'Pending',
          approved: 'Approved',
          rejected: 'Not approved'
        },
        rejectReasonPrefix: 'Reason: ',
        delete: 'Delete',
        deleteConfirm: 'Delete this qualification document? This cannot be undone.',
        deleteSuccess: 'Deleted',
        deleteFailed: 'Delete failed. Please try again later.'
      },
      messages: {
        loadFailed: 'Failed to load qualifications. Please try again later.'
      }
    },
    schedule: {
      title: 'My schedule',
      subtitle: 'Set your weekly availability; students will book the open slots',
      weekViewTitle: 'Weekly availability',
      weekRange: '{from} ~ {to}',
      prevWeek: 'Previous week',
      nextWeek: 'Next week',
      currentWeek: 'This week',
      timezoneLabel: 'Time zone: {tz}',
      modeLabel: 'Edit mode',
      modeTemplate: 'Edit weekly hours',
      modeTemplateDesc: 'Applies to every future week; use this for your regular teaching hours',
      modeException: 'Adjust this week',
      modeExceptionDesc: 'Applies only to specific dates this week; use this for temporary changes',
      periodLabel: 'Time range',
      periodAll: 'All 07:00-23:00',
      periodMorning: 'Morning 07:00-12:00',
      periodAfternoon: 'Afternoon 12:00-18:00',
      periodEvening: 'Evening 18:00-23:00',
      applyToNextWeek: 'Apply template to next week',
      applyToNextWeekConfirm: 'Apply this week\'s template to next week? Existing next-week exceptions will not be overwritten.',
      applyToNextWeekSuccess: 'Applied to next week',
      addException: 'Add exception',
      slotAvailable: 'Available',
      slotWeeklyAvailable: 'Weekly available',
      slotUnavailable: 'Unavailable',
      slotException: 'Exception',
      legendAvailable: 'Weekly available',
      legendException: 'This week exception',
      legendWeeklyAvailable: 'Weekly availability',
      legendExceptionClosed: 'Closed this week',
      legendExceptionExtra: 'Open this week',
      slotClosedThisWeek: 'Closed this week',
      slotOpenThisWeek: 'Open this week',
      bulkWeekday: 'Weekdays bulk',
      bulkColumn: 'Select column',
      exceptionDrawerTitle: 'Exceptions this week',
      exceptionEmpty: 'No exceptions this week',
      exceptionDate: 'Date',
      exceptionTime: 'Time',
      exceptionAction: 'Action',
      exceptionActionMake: 'Mark available',
      exceptionActionBlock: 'Mark unavailable',
      exceptionActionMakeOnce: 'Open only this week',
      exceptionActionBlockOnce: 'Close only this week',
      exceptionAddSuccess: 'Exception added',
      exceptionRemoveSuccess: 'Exception removed',
      toggleSuccess: 'Saved',
      toggleFailed: 'Save failed, rolled back',
      loadFailed: 'Failed to load schedule, please retry',
      mobileDayDot: '{day}',
      onboarding: {
        title: 'First time setting up your schedule?',
        step1: '① Tap any time slot below to mark it Available (tap again to clear)',
        step2: '② The weekly template recurs automatically; use "This week\'s exceptions" for one-off changes',
        step3: '③ Saved instantly — students see your open slots on your teacher page and can book',
        dismiss: 'Got it, let me set it up'
      }
    },
    orders: {
      title: 'My orders',
      subtitle: 'See all your booked classes',
      loadFailed: 'Failed to load orders, please retry',
      tabs: {
        all: 'All',
        upcoming: 'Upcoming',
        finished: 'Finished',
        cancelled: 'Cancelled'
      },
      empty: {
        all: 'No orders yet',
        upcoming: 'No upcoming classes',
        finished: 'No finished classes',
        cancelled: 'No cancelled classes'
      },
      card: {
        student: 'Student',
        scheduledAt: 'Scheduled',
        duration: 'Duration: {n} min',
        package: 'Package',
        settleStatusLabel: 'Settlement',
        settleFrozen: 'Frozen (T+7 unfreeze)',
        settleAvailable: 'Settled, withdrawable',
        settleCancelled: 'Cancelled, not settled',
        feeLabel: 'Fee',
        enterClass: 'Enter class',
        enterClassNotYet: 'Opens 5 min before class',
        cancelledByStudent: 'Cancelled by student',
        viewDetail: 'Detail'
      }
    },
    income: {
      title: 'My income',
      subtitle: 'Earnings and withdrawals overview',
      loadFailed: 'Failed to load income, please retry',
      totalEarnedLabel: 'Total earned',
      availableLabel: 'Available balance',
      frozenLabel: 'In transit (T+7)',
      pendingWithdrawalLabel: 'Withdrawal in review',
      availableHint: 'Unfrozen balance available for withdrawal',
      frozenHint: 'Recent class earnings; auto-unfreeze after T+7',
      pendingWithdrawalHint: 'A withdrawal is under review; cannot apply again',
      nextUnfreezeLabel: 'Next unfreeze {date}',
      applyWithdrawalBtn: 'Request withdrawal →',
      detailTitle: 'Income details',
      filterFrom: 'From',
      filterTo: 'To',
      filterType: 'Type',
      filterTypeAll: 'All',
      filterTypeClass: 'Class income',
      filterTypeAdjust: 'Manual adjustment',
      exportCsv: 'Export CSV',
      column: {
        classTime: 'Class time',
        student: 'Student',
        type: 'Type',
        fee: 'Fee (USD)',
        status: 'Status',
        unfreezeAt: 'Unfreeze at'
      },
      statusFrozen: 'Frozen',
      statusAvailable: 'Available',
      statusWithdrawn: 'Withdrawn',
      empty: 'No income records yet'
    },
    withdrawal: {
      apply: {
        title: 'Request withdrawal',
        subtitle: 'Fill in payee info; an admin will manually review and pay out',
        balanceLabel: 'Available balance',
        balanceUnit: 'USD',
        amountLabel: 'Amount',
        amountUnit: 'USD',
        amountPlaceholder: 'Enter withdrawal amount',
        amountHint: 'Minimum {min} USD; transfer fee is borne by teacher',
        amountAllInBtn: 'All',
        payeeMethodLabel: 'Payee method',
        payeeMethodWechat: 'WeChat',
        payeeMethodAlipay: 'Alipay',
        payeeMethodPaypal: 'PayPal',
        payeeMethodBankCard: 'Bank card',
        payeeMethodOther: 'Other',
        payeeInfoLabel: 'Payee info',
        payeeInfoPlaceholderWechat: 'Enter WeChat ID',
        payeeInfoPlaceholderAlipay: 'Enter Alipay account (phone or email)',
        payeeInfoPlaceholderPaypal: 'Enter PayPal registered email',
        payeeInfoPlaceholderBankCard: 'Bank / Card no. / Holder name',
        payeeInfoPlaceholderOther: 'Describe payee method and account in detail',
        bankNameLabel: 'Bank',
        bankCardNoLabel: 'Card number',
        bankHolderNameLabel: 'Holder name',
        bankNamePlaceholder: 'Enter bank name',
        bankCardNoPlaceholder: 'Enter card number',
        bankHolderNamePlaceholder: 'Enter holder name',
        payeeInfoHintWechat: 'Provide your WeChat ID or a scannable identifier',
        payeeInfoHintAlipay: 'Provide your Alipay account (phone or email)',
        payeeInfoHintPaypal: 'Provide a valid PayPal registered email; payout will be sent via PayPal',
        payeeInfoHintBankCard: 'Provide complete bank, card number and holder name; wrong info may cause payout failure',
        payeeInfoHintOther: 'Describe in detail so admin can verify',
        payeeInfoSecurityNote: 'For security, payee info is not retained; please fill in each time',
        confirmCheckbox: 'I confirm the payee info is correct and understand that incorrect info may cause payout failure',
        cancelBtn: 'Cancel',
        submitBtn: 'Submit',
        submitting: 'Submitting...',
        submitSuccess: 'Withdrawal request submitted, awaiting review',
        submitFailed: 'Submit failed, please retry'
      },
      list: {
        title: 'Withdrawal history',
        subtitle: 'Past withdrawal requests and payout status',
        applyBtn: 'Request withdrawal',
        loadFailed: 'Failed to load, please retry',
        empty: 'No withdrawal records yet',
        filterStatus: 'Status',
        filterStatusAll: 'All statuses',
        column: {
          appliedAt: 'Applied at',
          amount: 'Amount (USD)',
          method: 'Payee method',
          payeeMasked: 'Payee',
          status: 'Status',
          action: 'Action'
        },
        statusPending: 'Pending',
        statusApproved: 'Approved, awaiting payout',
        statusPaid: 'Paid',
        statusRejected: 'Rejected',
        statusFailed: 'Payout failed',
        viewDetailBtn: 'Detail',
        reapplyBtn: 'Re-apply',
        detailTitle: 'Withdrawal detail',
        timelineTitle: 'Status timeline',
        timelineApplied: 'Submitted',
        timelineApproved: 'Approved',
        timelineRejected: 'Rejected',
        timelinePaid: 'Paid',
        timelineFailed: 'Failed',
        rejectReasonLabel: 'Reject reason',
        failReasonLabel: 'Failure reason',
        paidRemarkLabel: 'Payout note',
        paidProofLabel: 'Proof',
        payeeMaskedHint: 'Only the last 4 chars are shown; full info is encrypted'
      }
    },
    errors: {
      balanceInsufficient: 'Insufficient balance',
      withdrawalPending: 'You have a pending withdrawal request; please wait for review',
      withdrawalBelowMin: 'Amount is below the minimum {min} USD',
      withdrawalAboveBalance: 'Amount exceeds your available balance',
      amountInvalid: 'Please enter a valid amount',
      payeeRequired: 'Please fill in payee info',
      methodRequired: 'Please select a payee method',
      confirmRequired: 'Please confirm payee info is correct',
      permissionDenied: 'You do not have permission to access Teacher Center',
      networkError: 'Network error, please retry'
    }
  }
}
