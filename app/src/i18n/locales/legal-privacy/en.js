// TODO 法务审核 (legal review pending)
// Privacy Policy - English version
// Source of truth for company entity: docs/compliance/company-entity.md
// First draft; framework is complete and covers Mandarly's actual business surface
// (social login, Stripe payments, Tencent LCIC classroom, cross-border data).
// A Hong Kong solicitor should review before publication.

export default {
  legal: {
    privacy: {
      title: 'Privacy Policy',
      seoTitle: 'Mandarly Privacy Policy · How We Collect and Protect Your Data',
      seoDescription: 'Mandarly Privacy Policy explains how we collect, use, share and protect your personal information when you use our online 1-on-1 Mandarin tutoring service, and the rights you have over your data.',
      seoKeywords: 'Mandarly privacy policy, data protection, online Mandarin tutoring, learn Chinese, personal information',
      lastUpdated: 'Last updated: 2026-05-09',
      lastUpdatedDate: '2026-05-09',
      backToHome: 'Back to home',
      companyName: 'MANDARLY TECHNOLOGY LIMITED',
      companyNameEn: 'MANDARLY TECHNOLOGY LIMITED',
      companyAddress: 'RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
      contactEmail: 'support{\'@\'}mandarly.com',
      sections: {
        intro: {
          heading: 'Introduction',
          body: 'Welcome to Mandarly. Mandarly is an online 1-on-1 Mandarin tutoring platform operated by {company}, primarily serving learners in Hong Kong and the global Chinese-speaking diaspora. We take the privacy and security of your personal information seriously. This Privacy Policy explains what information we collect when you visit mandarly.com, sign up for an account, book lessons or make payments; how we use and share that information; and the rights you have. By continuing to use Mandarly you confirm that you have read and understood this Policy.'
        },
        collect: {
          heading: 'Information We Collect',
          body: 'We collect only the information necessary to deliver and improve our service. The main categories are: (1) Account registration data — email, mobile number, password (stored hashed), display name, region and preferred language; (2) Social login data — when you sign in with Google or Apple, we receive the basic profile information you authorize (avatar, name, email); we never receive your password on the third-party platform; (3) Lesson and interaction data — booking records, lesson duration, attendance, post-lesson reviews and in-app messages with your tutor; (4) Payment data — order amount, currency, order status and refund records; full credit card numbers are handled directly by Stripe — Mandarly does not store your card number or CVV; (5) Device and log data — browser type, operating system, IP address, access timestamps and error logs, used for security audit and service optimization; (6) Cookies and local storage — login token, session id, locale preference, timezone and similar session data.'
        },
        use: {
          heading: 'How We Use Information',
          body: 'We use the information we collect for the following purposes: (1) To provide the core service — creating your account, matching tutors, scheduling lessons, connecting you to the Tencent Cloud LCIC online classroom and processing payments and refunds; (2) To improve the product — analyzing usage patterns to refine tutor recommendations, time-slot scheduling and interface design; (3) To communicate with you — account verification, lesson reminders, payment receipts and policy change notices; (4) For security and fraud prevention — detecting unusual logins, preventing account takeover and stopping abusive booking patterns; (5) For legal compliance — meeting the laws of Hong Kong, your region of residence and our partner jurisdictions (such as the United States where Stripe is located), and responding to lawful requests from regulators or judicial authorities. We will not use your personal information for purposes beyond those listed above without first notifying you and, where required, obtaining your consent.'
        },
        share: {
          heading: 'How We Share Information',
          body: 'We share your personal information only in the situations below, and only to the minimum extent each recipient needs to perform its work: (1) Tutors — your display name, region, learning goals and level-test results are shown to the tutor you book, so they can prepare and deliver your lesson; (2) Payment processor Stripe — order amount, currency, order id and email; Stripe collects your card details directly and is independently responsible for PCI DSS compliance; (3) Classroom provider Tencent Cloud LCIC — room id, user id and display name, used to establish the classroom session; (4) Notification providers — emails are sent through Aliyun enterprise mailbox SMTP and SMS through Tencent Cloud ISMS; only the recipient address and template variables are passed; (5) Legal requirements — when we receive a valid court order or regulatory request, or when disclosure is necessary to protect users or the public; (6) Corporate changes — in case of merger, acquisition or asset sale, the successor will be bound by protections at least equivalent to this Policy and you will be notified in advance. We do not sell your personal information to any third party.'
        },
        cookies: {
          heading: 'Cookies and Local Storage',
          body: 'We use only the cookies and browser local storage required to deliver the service, including: login token, session id, locale preference, timezone and CSRF protection token. We do not run third-party advertising cookies or join cross-site tracking ad networks. We have not integrated Google Analytics, Facebook Pixel or similar third-party analytics in this first release; should we add any in the future we will update this Policy and provide an opt-out mechanism. You may clear or disable cookies in your browser settings, but doing so may interfere with basic functions such as keeping you signed in.'
        },
        rights: {
          heading: 'Your Rights',
          body: 'Subject to applicable law, you have the following rights over your personal information: (1) Right of access — to see what data we hold about you; (2) Right of correction — to ask us to correct inaccurate or incomplete data; (3) Right of erasure — to request deletion of your account and related data, except where retention is required by law; (4) Right to restrict processing — to ask us to suspend certain uses in defined situations; (5) Right of data portability — to obtain the data you provided in a structured, commonly used, machine-readable format and have it transmitted to another service; (6) Right to withdraw consent — for processing based on your consent you may withdraw at any time, without affecting the lawfulness of processing already done; (7) Right to lodge a complaint — with the Hong Kong Office of the Privacy Commissioner for Personal Data (PCPD) or the data-protection authority in your region. To exercise any of these rights, email {email}; we will respond within 30 days.'
        },
        children: {
          heading: "Children's Privacy",
          body: 'Mandarly does not knowingly target children under 13. If we discover we have inadvertently collected personal information from a child under 13 we will delete it promptly. Users between 13 and 18 should review this Policy with a parent or legal guardian, and the guardian should consent on their behalf or together with them to the collection and use of data. If a guardian discovers that a minor has registered with Mandarly without their consent, please contact us at {email} and we will help close the account and delete the related data.'
        },
        crossBorder: {
          heading: 'Cross-Border Data Transfers',
          body: 'Mandarly servers are located in Hong Kong and core data (accounts, orders, lesson records) is primarily stored there. Because the service is international, some data is transferred across borders in the following ways: (1) Stripe payment processing involves data centers in the United States and Europe; (2) The Tencent Cloud LCIC classroom service operates from mainland China; (3) Email is sent through Aliyun enterprise mailbox; (4) SMS is sent through the Tencent Cloud ISMS international gateway to local carriers. For each cross-border transfer we rely on contractual safeguards, transport-layer encryption and the principle of minimum necessity, so that the level of protection during transfer is no lower than the commitments in this Policy.'
        },
        retention: {
          heading: 'Data Retention',
          body: 'We retain your personal information only as long as necessary for the purposes set out in this Policy: (1) Basic account data — kept while your account is active and cleared or anonymized within 30 days after account closure; (2) Order and payment records — kept for at least 7 years to meet Hong Kong tax and accounting law; (3) Lesson and review records — kept for 2 years after the lesson, for dispute handling and quality review; (4) Log data — kept for 90 days for security audit; (5) Where the law requires longer retention we follow the law. You can request account closure from the user center or by emailing {email}.'
        },
        changes: {
          heading: 'Changes to this Policy',
          body: 'We may update this Policy from time to time to reflect product changes, legal developments or security improvements. For any material change — such as adding a new category of collected data, adjusting who we share with or extending retention — we will notify you at least 7 days in advance through in-app messages, email or a website notice. Continued use of Mandarly after a change indicates your acceptance of the updated Policy; if you disagree, please stop using the service and you may request account closure. We recommend you check this page periodically to stay informed.'
        },
        contact: {
          heading: 'Contact Us',
          body: 'For any questions, comments or complaints about this Privacy Policy or our data-handling practices, please reach us at: Company: {company}; Registered address: {address}; Contact email: {email}. We commit to responding within 30 days. If you are not satisfied with our response, you may also lodge a complaint with the Hong Kong Office of the Privacy Commissioner for Personal Data (PCPD) or the data-protection regulator in your region.'
        }
      }
    }
  }
}
