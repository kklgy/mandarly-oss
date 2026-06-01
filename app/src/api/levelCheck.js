/**
 * 水平测试 API(对应 /app-api/edu/level-check/*,均 @PermitAll)
 *
 * PRD: §4.3 S0 + docs/product/level-check-recommendation-v1.md
 */

import request from './request'

export function listQuestions() {
  return request.get('/edu/level-check/questions')
}

export function submitAnswers({ sessionId, locale, email, answers }) {
  return request.post('/edu/level-check/submit', {
    sessionId,
    locale,
    email,
    answers
  })
}

export function getResult(submissionId) {
  return request.get('/edu/level-check/result', { params: { submissionId } })
}
