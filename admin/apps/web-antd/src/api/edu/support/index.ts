import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace EduSupportApi {
  export type FaqStatus = 'active' | 'disabled';

  export interface Faq {
    id?: number;
    category: string;
    locale: string;
    question: string;
    answer: string;
    keywords?: string[];
    sort?: number;
    status: FaqStatus;
    viewCount?: number;
    matchCount?: number;
    createTime?: string;
    updateTime?: string;
  }

  export interface FaqPageReq extends PageParam {
    category?: string;
    locale?: string;
    status?: FaqStatus;
    question?: string;
  }

  export interface Contact {
    id?: number;
    market: string;
    channelType: string;
    displayText: string;
    linkUrl?: string;
    imageUrl?: string;
    sort?: number;
    isActive?: boolean;
    createTime?: string;
    updateTime?: string;
  }

  export interface ContactPageReq extends PageParam {
    market?: string;
    channelType?: string;
    displayText?: string;
    isActive?: boolean;
  }

  export interface Inquiry {
    id: number;
    userId?: number;
    sessionId: string;
    locale: string;
    market?: string;
    questionText: string;
    matchedFaqId?: number;
    score?: number | string;
    clickedToHuman?: boolean;
    clickedContactId?: number;
    ip?: string;
    userAgent?: string;
    createTime?: string;
  }

  export interface InquiryPageReq extends PageParam {
    userId?: number;
    sessionId?: string;
    locale?: string;
    market?: string;
    questionKeyword?: string;
    matched?: boolean;
    clickedToHuman?: boolean;
  }

  export interface InquiryStats {
    totalCount: number;
    askCount: number;
    matchedCount: number;
    unmatchedCount: number;
    clickedToHumanCount: number;
    directClickCount: number;
    matchRate: number | string;
    clickRate: number | string;
  }

  export interface TopUnmatched {
    questionText: string;
    count: number;
    lastAskedAt?: string;
  }
}

export function getSupportFaqPage(params: EduSupportApi.FaqPageReq) {
  return requestClient.get<PageResult<EduSupportApi.Faq>>(
    '/edu/support/faq/page',
    { params },
  );
}

export function getSupportFaq(id: number) {
  return requestClient.get<EduSupportApi.Faq>(`/edu/support/faq/get?id=${id}`);
}

export function createSupportFaq(data: EduSupportApi.Faq) {
  return requestClient.post<number>('/edu/support/faq/create', data);
}

export function updateSupportFaq(data: EduSupportApi.Faq) {
  return requestClient.put<boolean>('/edu/support/faq/update', data);
}

export function deleteSupportFaq(id: number) {
  return requestClient.delete<boolean>(`/edu/support/faq/delete?id=${id}`);
}

export function getSupportContactPage(params: EduSupportApi.ContactPageReq) {
  return requestClient.get<PageResult<EduSupportApi.Contact>>(
    '/edu/support/contact/page',
    { params },
  );
}

export function getSupportContact(id: number) {
  return requestClient.get<EduSupportApi.Contact>(
    `/edu/support/contact/get?id=${id}`,
  );
}

export function createSupportContact(data: EduSupportApi.Contact) {
  return requestClient.post<number>('/edu/support/contact/create', data);
}

export function updateSupportContact(data: EduSupportApi.Contact) {
  return requestClient.put<boolean>('/edu/support/contact/update', data);
}

export function deleteSupportContact(id: number) {
  return requestClient.delete<boolean>(`/edu/support/contact/delete?id=${id}`);
}

export function getSupportInquiryPage(params: EduSupportApi.InquiryPageReq) {
  return requestClient.get<PageResult<EduSupportApi.Inquiry>>(
    '/edu/support/inquiry/page',
    { params },
  );
}

export function getSupportInquiryStats() {
  return requestClient.get<EduSupportApi.InquiryStats>(
    '/edu/support/inquiry/stats',
  );
}

export function getSupportTopUnmatched(limit = 10) {
  return requestClient.get<EduSupportApi.TopUnmatched[]>(
    `/edu/support/inquiry/top-unmatched?limit=${limit}`,
  );
}
