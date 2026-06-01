import { requestClient } from '#/api/request';

export namespace EduReportApi {
  export interface PeriodStat {
    beginAt?: string;
    courseOrders: number;
    label: string;
    newStudents: number;
    newTeachers: number;
    paidAmountUsd?: number | string;
    paidPaymentOrders: number;
    refundedAmountUsd?: number | string;
    refundedOrders: number;
  }

  export interface Overview {
    abnormalCourseOrders: number;
    approvedTeachers: number;
    lastUpdatedAt?: string;
    paidAmountUsd?: number | string;
    paidPaymentOrders: number;
    pendingRefunds: number;
    pendingTeacherAudits: number;
    periods: PeriodStat[];
    refundedAmountUsd?: number | string;
    totalCourseOrders: number;
    totalStudents: number;
    totalTeachers: number;
    upcomingCourseOrders: number;
  }
}

export function getEduReportOverview() {
  return requestClient.get<EduReportApi.Overview>('/edu/report/overview');
}
