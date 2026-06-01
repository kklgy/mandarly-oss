import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace EduLevelCheckApi {
  export type InferredLevel = 'advanced' | 'beginner' | 'intermediate';

  export interface Question {
    id?: number;
    questionCode: string;
    questionI18nCode: string;
    questionType?: string;
    sort?: number;
    isActive?: boolean;
    options?: Option[];
  }

  export interface QuestionPageReq extends PageParam {
    questionCode?: string;
    isActive?: boolean;
  }

  export interface Option {
    id?: number;
    questionId: number;
    optionCode: string;
    optionI18nCode: string;
    matchExpertise?: string[];
    scoreRules?: Array<Record<string, any>>;
    inferredLevel?: InferredLevel;
    recommendedWeeklyCount?: number;
    sort?: number;
  }

  export interface Submission {
    id: number;
    userId?: number;
    sessionId?: string;
    email?: string;
    locale: string;
    market?: string;
    answers?: Array<Record<string, string>>;
    inferredLevel: InferredLevel;
    recommendedTeacherIds?: number[];
    recommendedPackageId?: number;
    isConverted?: boolean;
    convertedOrderId?: number;
    createTime?: string;
  }

  export interface SubmissionPageReq extends PageParam {
    locale?: string;
    inferredLevel?: InferredLevel;
    isConverted?: boolean;
    createTimeFrom?: string;
    createTimeTo?: string;
  }

  export interface LevelStats {
    inferredLevel: string;
    count: number;
    rate: number | string;
  }

  export interface SubmissionStats {
    totalCount: number;
    convertedCount: number;
    unconvertedCount: number;
    conversionRate: number | string;
    levelStats: LevelStats[];
  }
}

export function getLevelCheckQuestionPage(
  params: EduLevelCheckApi.QuestionPageReq,
) {
  return requestClient.get<PageResult<EduLevelCheckApi.Question>>(
    '/edu/level-check/question/page',
    { params },
  );
}

export function getLevelCheckQuestion(id: number) {
  return requestClient.get<EduLevelCheckApi.Question>(
    `/edu/level-check/question/get?id=${id}`,
  );
}

export function createLevelCheckQuestion(data: EduLevelCheckApi.Question) {
  return requestClient.post<number>('/edu/level-check/question/create', data);
}

export function updateLevelCheckQuestion(data: EduLevelCheckApi.Question) {
  return requestClient.put<boolean>('/edu/level-check/question/update', data);
}

export function deleteLevelCheckQuestion(id: number) {
  return requestClient.delete<boolean>(
    `/edu/level-check/question/delete?id=${id}`,
  );
}

export function getLevelCheckOptionList(questionId: number) {
  return requestClient.get<EduLevelCheckApi.Option[]>(
    `/edu/level-check/option/list?questionId=${questionId}`,
  );
}

export function createLevelCheckOption(data: EduLevelCheckApi.Option) {
  return requestClient.post<number>('/edu/level-check/option/create', data);
}

export function updateLevelCheckOption(data: EduLevelCheckApi.Option) {
  return requestClient.put<boolean>('/edu/level-check/option/update', data);
}

export function deleteLevelCheckOption(id: number) {
  return requestClient.delete<boolean>(`/edu/level-check/option/delete?id=${id}`);
}

export function getLevelCheckSubmissionPage(
  params: EduLevelCheckApi.SubmissionPageReq,
) {
  return requestClient.get<PageResult<EduLevelCheckApi.Submission>>(
    '/edu/level-check/submission/page',
    { params },
  );
}

export function getLevelCheckSubmissionStats() {
  return requestClient.get<EduLevelCheckApi.SubmissionStats>(
    '/edu/level-check/submission/stats',
  );
}
