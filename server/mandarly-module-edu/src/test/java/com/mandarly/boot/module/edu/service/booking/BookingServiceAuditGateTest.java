package com.mandarly.boot.module.edu.service.booking;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.BookingCreateReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.StudentPackageDO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.CourseOrderMapper;
import com.mandarly.boot.module.edu.dal.mysql.booking.StudentPackageMapper;
import com.mandarly.boot.module.edu.dal.mysql.pkg.PackageMapper;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherProfileMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum;
import com.mandarly.boot.module.edu.enums.user.UserRoleEnum;
import com.mandarly.boot.module.edu.enums.user.UserStatusEnum;
import com.mandarly.boot.module.edu.service.classroom.ClassroomService;
import com.mandarly.boot.module.edu.service.notification.NotificationService;
import com.mandarly.boot.module.infra.api.config.ConfigApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.TEACHER_NOT_APPROVED;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.BOOKING_SLOT_LOCK_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BookingServiceImpl#createOrder audit_status 闭合 backdoor 单元测试(D19 A4)
 *
 * <p>覆盖:仅 audit_status=approved 教师可被预约。
 * <ol>
 *   <li>teacher_profile 缺失 → 抛 TEACHER_NOT_APPROVED</li>
 *   <li>teacher_profile audit_status=pending → 抛 TEACHER_NOT_APPROVED</li>
 *   <li>teacher_profile audit_status=rejected → 抛 TEACHER_NOT_APPROVED</li>
 *   <li>teacher_profile audit_status=approved → 不再因审核态报错(后续无套餐校验会抛其它错,不在本测试范围)</li>
 * </ol>
 *
 * <p>注:仅覆盖前置教师审核闸口,其余下游(扣次/创建订单/LCIC)mock 到能跑过 teacher 校验即可。
 */
class BookingServiceAuditGateTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private EduUserMapper eduUserMapper;

    @Mock
    private TeacherProfileMapper teacherProfileMapper;

    @Mock
    private CourseOrderMapper courseOrderMapper;

    @Mock
    private StudentPackageMapper studentPackageMapper;

    @Mock
    private PackageMapper packageMapper;

    @Mock
    private ClassroomService classroomService;

    @Mock
    private ConfigApi configApi;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RedissonClient redissonClient;

    private static final Long STUDENT_ID = 1001L;
    private static final Long TEACHER_ID = 2002L;

    private BookingCreateReqVO buildReq() {
        BookingCreateReqVO vo = new BookingCreateReqVO();
        vo.setStudentId(STUDENT_ID);
        vo.setTeacherId(TEACHER_ID);
        vo.setScheduledAt(LocalDateTime.now().plusDays(1));
        return vo;
    }

    /** 让 student + teacher 的 role+status 校验通过(进入审核闸口) */
    private void stubStudentAndTeacherActive() {
        UserDO student = new UserDO();
        student.setId(STUDENT_ID);
        student.setRole(UserRoleEnum.STUDENT.getCode());
        student.setStatus(UserStatusEnum.ACTIVE.getCode());
        when(eduUserMapper.selectById(STUDENT_ID)).thenReturn(student);

        UserDO teacher = new UserDO();
        teacher.setId(TEACHER_ID);
        teacher.setRole(UserRoleEnum.TEACHER.getCode());
        teacher.setStatus(UserStatusEnum.ACTIVE.getCode());
        when(eduUserMapper.selectById(TEACHER_ID)).thenReturn(teacher);
    }

    private RLock stubBookingSlotLock(LocalDateTime scheduledAt, boolean acquired) throws InterruptedException {
        RLock lock = mock(RLock.class);
        lenient().when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(acquired);
        lenient().when(lock.isHeldByCurrentThread()).thenReturn(acquired);
        when(redissonClient.getLock(eq("booking:teacher-slot:" + TEACHER_ID + ":" + scheduledAt))).thenReturn(lock);
        return lock;
    }

    /** case 1: teacher_profile 缺失 → TEACHER_NOT_APPROVED */
    @Test
    void createOrder_teacherProfileMissing_throwsNotApproved() {
        stubStudentAndTeacherActive();
        when(teacherProfileMapper.selectById(TEACHER_ID)).thenReturn(null);

        assertThatThrownBy(() -> bookingService.createOrder(buildReq()))
                .isInstanceOf(ServiceException.class)
                .matches(e -> ((ServiceException) e).getCode() == TEACHER_NOT_APPROVED.getCode());

        // 没继续走到扣次环节
        verify(courseOrderMapper, never()).insert(org.mockito.ArgumentMatchers.any(CourseOrderDO.class));
        verify(studentPackageMapper, never()).updateById(org.mockito.ArgumentMatchers.any(StudentPackageDO.class));
    }

    /** case 2: teacher_profile audit_status=pending → TEACHER_NOT_APPROVED */
    @Test
    void createOrder_teacherProfilePending_throwsNotApproved() {
        stubStudentAndTeacherActive();
        TeacherProfileDO p = new TeacherProfileDO();
        p.setUserId(TEACHER_ID);
        p.setAuditStatus(TeacherAuditStatusEnum.PENDING.getCode());
        when(teacherProfileMapper.selectById(TEACHER_ID)).thenReturn(p);

        assertThatThrownBy(() -> bookingService.createOrder(buildReq()))
                .isInstanceOf(ServiceException.class)
                .matches(e -> ((ServiceException) e).getCode() == TEACHER_NOT_APPROVED.getCode());

        verify(courseOrderMapper, never()).insert(org.mockito.ArgumentMatchers.any(CourseOrderDO.class));
    }

    /** case 3: teacher_profile audit_status=rejected → TEACHER_NOT_APPROVED */
    @Test
    void createOrder_teacherProfileRejected_throwsNotApproved() {
        stubStudentAndTeacherActive();
        TeacherProfileDO p = new TeacherProfileDO();
        p.setUserId(TEACHER_ID);
        p.setAuditStatus(TeacherAuditStatusEnum.REJECTED.getCode());
        when(teacherProfileMapper.selectById(TEACHER_ID)).thenReturn(p);

        assertThatThrownBy(() -> bookingService.createOrder(buildReq()))
                .isInstanceOf(ServiceException.class)
                .matches(e -> ((ServiceException) e).getCode() == TEACHER_NOT_APPROVED.getCode());

        verify(courseOrderMapper, never()).insert(org.mockito.ArgumentMatchers.any(CourseOrderDO.class));
    }

    /**
     * case 4: teacher_profile audit_status=approved → 不再抛 TEACHER_NOT_APPROVED
     * (下游 chooseStudentPackage 因无可用套餐会抛其它错,这里只校验「不是审核错」即视为通过)
     */
    @Test
    void createOrder_teacherProfileApproved_doesNotThrowForAuditReason() {
        stubStudentAndTeacherActive();
        TeacherProfileDO p = new TeacherProfileDO();
        p.setUserId(TEACHER_ID);
        p.setAuditStatus(TeacherAuditStatusEnum.APPROVED.getCode());
        when(teacherProfileMapper.selectById(TEACHER_ID)).thenReturn(p);

        // 不 stub 后续 mapper,让它在「教师无冲突 → 选套餐 → 套餐为空」处抛 409,
        // 但应当不是 TEACHER_NOT_APPROVED。
        try {
            bookingService.createOrder(buildReq());
        } catch (ServiceException e) {
            assertThat(e.getCode()).isNotEqualTo(TEACHER_NOT_APPROVED.getCode());
            return;
        } catch (Throwable t) {
            // 任何其它异常(NPE 等)也视为已越过审核闸口
            return;
        }
        // 真正成功也行(不太可能,因为没 stub 套餐数据)
    }

    /** case 5:预约成功后触发教师预约邮件通知 */
    @Test
    void createOrder_success_sendsBookingCreatedNotification() {
        stubStudentAndTeacherActive();
        TeacherProfileDO p = new TeacherProfileDO();
        p.setUserId(TEACHER_ID);
        p.setAuditStatus(TeacherAuditStatusEnum.APPROVED.getCode());
        when(teacherProfileMapper.selectById(TEACHER_ID)).thenReturn(p);

        BookingCreateReqVO req = buildReq();
        req.setStudentPackageId(3003L);
        try {
            stubBookingSlotLock(req.getScheduledAt(), true);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
        when(courseOrderMapper.selectByTeacherAndScheduledAt(TEACHER_ID, req.getScheduledAt(), "upcoming"))
                .thenReturn(List.of());

        StudentPackageDO sp = new StudentPackageDO();
        sp.setId(3003L);
        sp.setStudentId(STUDENT_ID);
        sp.setPackageId(4004L);
        sp.setRemaining(2);
        sp.setExpireAt(LocalDateTime.now().plusDays(30));
        when(studentPackageMapper.selectById(3003L)).thenReturn(sp);

        PackageDO pkg = new PackageDO();
        pkg.setId(4004L);
        pkg.setPrice(new BigDecimal("100.00"));
        pkg.setTotalCount(10);
        pkg.setCurrency("HKD");
        pkg.setIsFreeTrial(false);
        when(packageMapper.selectById(4004L)).thenReturn(pkg);
        when(configApi.getConfigValueByKey("mandarly.income.teacher_lesson_fee_usd")).thenReturn("3");

        doAnswer(invocation -> {
            CourseOrderDO order = invocation.getArgument(0);
            order.setId(5005L);
            return 1;
        }).when(courseOrderMapper).insert(any(CourseOrderDO.class));

        Long orderId = bookingService.createOrder(req);

        assertThat(orderId).isEqualTo(5005L);
        ArgumentCaptor<CourseOrderDO> orderCaptor = ArgumentCaptor.forClass(CourseOrderDO.class);
        verify(courseOrderMapper).insert(orderCaptor.capture());
        CourseOrderDO createdOrder = orderCaptor.getValue();
        assertThat(createdOrder.getTeacherAmount()).isEqualByComparingTo("3.00");
        assertThat(createdOrder.getTeacherAmountCurrency()).isEqualTo("USD");
        verify(classroomService).openRoomForOrder(5005L);
        verify(notificationService).sendForBookingCreated(5005L);
    }

    /** case 6:同一教师同一时间 slot 锁拿不到 → 快速返回并发冲突,不扣次不插单 */
    @Test
    void createOrder_bookingSlotLockTimeout_throwsConcurrencyConflict() throws Exception {
        stubStudentAndTeacherActive();
        TeacherProfileDO p = new TeacherProfileDO();
        p.setUserId(TEACHER_ID);
        p.setAuditStatus(TeacherAuditStatusEnum.APPROVED.getCode());
        when(teacherProfileMapper.selectById(TEACHER_ID)).thenReturn(p);

        BookingCreateReqVO req = buildReq();
        stubBookingSlotLock(req.getScheduledAt(), false);

        assertThatThrownBy(() -> bookingService.createOrder(req))
                .isInstanceOf(ServiceException.class)
                .matches(e -> ((ServiceException) e).getCode() == BOOKING_SLOT_LOCK_TIMEOUT.getCode());

        verify(courseOrderMapper, never()).selectByTeacherAndScheduledAt(any(), any(), any());
        verify(studentPackageMapper, never()).updateById(any(StudentPackageDO.class));
        verify(courseOrderMapper, never()).insert(any(CourseOrderDO.class));
    }

    /** case 7:拿到 slot 锁后仍重查冲突;发现已有 upcoming 时不扣次不插单 */
    @Test
    void createOrder_conflictInsideSlotLock_throwsAndDoesNotDeductPackage() throws Exception {
        stubStudentAndTeacherActive();
        TeacherProfileDO p = new TeacherProfileDO();
        p.setUserId(TEACHER_ID);
        p.setAuditStatus(TeacherAuditStatusEnum.APPROVED.getCode());
        when(teacherProfileMapper.selectById(TEACHER_ID)).thenReturn(p);

        BookingCreateReqVO req = buildReq();
        stubBookingSlotLock(req.getScheduledAt(), true);
        CourseOrderDO existing = new CourseOrderDO();
        existing.setId(9001L);
        when(courseOrderMapper.selectByTeacherAndScheduledAt(TEACHER_ID, req.getScheduledAt(), "upcoming"))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> bookingService.createOrder(req))
                .isInstanceOf(ServiceException.class)
                .matches(e -> ((ServiceException) e).getCode() == 409);

        verify(studentPackageMapper, never()).updateById(any(StudentPackageDO.class));
        verify(courseOrderMapper, never()).insert(any(CourseOrderDO.class));
    }
}
