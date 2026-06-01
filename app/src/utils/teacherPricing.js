export function hasTeacherSessionPrice(pricePerSession) {
  return pricePerSession !== null && pricePerSession !== undefined && pricePerSession !== ''
}

export function getTeacherPricingMode({ isLoggedIn, isTeacher, pricePerSession }) {
  if (hasTeacherSessionPrice(pricePerSession)) {
    return isLoggedIn && !isTeacher ? 'price' : 'locked'
  }
  return isLoggedIn && !isTeacher ? 'package-credit' : 'login-package'
}
