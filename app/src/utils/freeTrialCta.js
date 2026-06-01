export function shouldShowFreeTrialCta({ isLoggedIn, claimed }) {
  return !isLoggedIn || !claimed
}

export function getFreeTrialCtaTarget({ isLoggedIn }) {
  return isLoggedIn ? '/booking/free-trial' : '/register?redirect=/booking/free-trial'
}
