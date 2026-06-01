package com.mandarly.boot.module.edu.controller.app.booking;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.BookingCancelReqVO;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.BookingCreateReqVO;
import com.mandarly.boot.module.edu.controller.app.booking.vo.AppBookingCountsRespVO;
import com.mandarly.boot.module.edu.controller.app.booking.vo.AppBookingCreateReqVO;
import com.mandarly.boot.module.edu.controller.app.booking.vo.AppCourseOrderRespVO;
import com.mandarly.boot.module.edu.controller.app.booking.vo.AppMyOrderPageReqVO;
import com.mandarly.boot.module.edu.controller.app.booking.vo.AppOrderCancelReqVO;
import com.mandarly.boot.module.edu.controller.app.booking.vo.AppStudentPackageRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.StudentPackageDO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.pkg.PackageMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.service.booking.BookingService;
import com.mandarly.boot.module.edu.service.booking.StudentPackageService;
import com.mandarly.boot.module.edu.service.i18n.I18nMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * App 端 - 课程预约接口
 *
 * <p>对应 PRD-v4 §4.3 S3 选时段 / S4 扣次预约。M2.5 已切真 token,
 *    studentId 从 SecurityFrameworkUtils.getLoginUserId() 取;访问需 @PreAuthorize("isAuthenticated()")。
 *
 * <p>实际下单逻辑(身份校验 / 时段冲突 / 扣次优先级 / 订单状态机 / meeting 占位)
 *    完全复用 {@link BookingService#createOrder(BookingCreateReqVO)},本控制器只做参数转发与 VO 翻译。
 */
@Tag(name = "用户端 - 预约下单")
@RestController
@RequestMapping("/edu/booking")
@Validated
public class AppBookingController {

    @Resource
    private BookingService bookingService;

    @Resource
    private StudentPackageService studentPackageService;

    @Resource
    private PackageMapper packageMapper;

    @Resource
    private EduUserMapper eduUserMapper;

    @Resource
    private I18nMessageService i18nMessageService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "创建预约订单(扣次,无支付)")
    public CommonResult<Long> createBooking(@Valid @RequestBody AppBookingCreateReqVO reqVO) {
        BookingCreateReqVO adminReq = new BookingCreateReqVO();
        adminReq.setStudentId(SecurityFrameworkUtils.getLoginUserId());
        adminReq.setTeacherId(reqVO.getTeacherId());
        adminReq.setScheduledAt(reqVO.getScheduledAt());
        adminReq.setStudentPackageId(reqVO.getStudentPackageId());
        Long orderId = bookingService.createOrder(adminReq);
        return success(orderId);
    }

    @GetMapping("/order/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查订单详情(成功页 / 我的订单)")
    @Parameter(name = "id", description = "course_order.id", required = true, example = "1")
    public CommonResult<AppCourseOrderRespVO> getOrder(
            @PathVariable("id") Long id,
            @RequestParam(value = "locale", required = false) String locale) {
        CourseOrderDO order = bookingService.getOrder(id);
        if (order == null) {
            throw ServiceExceptionUtil.exception0(404, "订单不存在");
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (!loginUserId.equals(order.getStudentId()) && !loginUserId.equals(order.getTeacherId())) {
            throw ServiceExceptionUtil.exception0(403, "无权查看该订单");
        }
        AppCourseOrderRespVO vo = toOrderResp(order, locale);
        return success(vo);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "我的订单分页(按登录用户过滤,可选状态 IN)")
    public CommonResult<PageResult<AppCourseOrderRespVO>> listMyOrders(
            @Valid AppMyOrderPageReqVO reqVO,
            @RequestParam(value = "locale", required = false) String locale) {
        Long studentId = SecurityFrameworkUtils.getLoginUserId();
        Set<String> statuses = parseStatuses(reqVO.getStatus());
        PageResult<CourseOrderDO> page = bookingService.getMyOrdersPage(
                studentId, statuses, reqVO);

        List<AppCourseOrderRespVO> list = page.getList().stream()
                .map(o -> toOrderResp(o, locale))
                .toList();
        PageResult<AppCourseOrderRespVO> resp = new PageResult<>(list, page.getTotal());
        return success(resp);
    }

    @PostMapping("/order/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "学生取消订单(24h+ 返还课次 / 24h 内不返还)")
    @Parameter(name = "id", description = "course_order.id", required = true, example = "1")
    public CommonResult<Boolean> cancelOrder(
            @PathVariable("id") Long id,
            @Valid @RequestBody(required = false) AppOrderCancelReqVO reqVO) {
        CourseOrderDO order = bookingService.getOrder(id);
        if (order == null) {
            throw ServiceExceptionUtil.exception0(404, "订单不存在");
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (!loginUserId.equals(order.getStudentId())) {
            throw ServiceExceptionUtil.exception0(403, "无权取消该订单");
        }
        BookingCancelReqVO adminReq = new BookingCancelReqVO();
        adminReq.setOrderId(id);
        adminReq.setCancelledBy("student");
        adminReq.setReason(reqVO != null ? reqVO.getReason() : null);
        bookingService.cancelOrder(adminReq);
        return success(true);
    }

    @GetMapping("/counts")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "我的订单角标计数(待上课 / 待评价 / 退款中)")
    public CommonResult<AppBookingCountsRespVO> getMyOrderCounts() {
        Long studentId = SecurityFrameworkUtils.getLoginUserId();
        return success(bookingService.getMyOrderCounts(studentId));
    }

    /**
     * Wave 4 P1(2026-05-10):免费体验领取状态。
     *
     * <p>判定:学生 student_package 表中是否有 source='free_trial' 的记录(无论 active/expired/exhausted,
     *   领过即视为已领,不可重复领取)。
     *
     * <p>降级说明:免费体验唯一性靠 source='free_trial' 标识,booking 表当前没有 is_trial 字段,
     *   走 student_package.source 判定即可,无需新增字段。
     */
    @GetMapping("/free-trial-status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "免费体验领取状态(claimed: 是否已领过免费体验套餐)")
    public CommonResult<Map<String, Boolean>> getFreeTrialStatus() {
        Long studentId = SecurityFrameworkUtils.getLoginUserId();
        boolean claimed = studentPackageService.hasClaimedFreeTrial(studentId);
        return success(Map.of("claimed", claimed));
    }

    private Set<String> parseStatuses(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    @GetMapping("/student-packages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "学生当前可用套餐(remaining > 0 AND expire_at > now,按到期升序)")
    public CommonResult<List<AppStudentPackageRespVO>> listMyPackages(
            @RequestParam(value = "locale", required = false) String locale) {
        Long studentId = SecurityFrameworkUtils.getLoginUserId();
        List<StudentPackageDO> active = studentPackageService.listActiveByStudent(studentId);
        return success(toPackageRespList(active, locale));
    }

    @GetMapping("/student-packages/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "S8 我的套餐:学生全部套餐(active + expired + exhausted,按购买时间倒序)")
    public CommonResult<List<AppStudentPackageRespVO>> listMyAllPackages(
            @RequestParam(value = "locale", required = false) String locale) {
        Long studentId = SecurityFrameworkUtils.getLoginUserId();
        List<StudentPackageDO> all = studentPackageService.listAllByStudent(studentId);
        return success(toPackageRespList(all, locale));
    }

    /**
     * student_package 列表 → VO 列表(批量取 package 定义 + i18n 翻译,计算 status)。
     */
    private List<AppStudentPackageRespVO> toPackageRespList(List<StudentPackageDO> sps, String locale) {
        if (sps == null || sps.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> packageIds = sps.stream().map(StudentPackageDO::getPackageId).collect(Collectors.toSet());
        Map<Long, PackageDO> packageById = packageMapper.selectList(
                Wrappers.<PackageDO>lambdaQuery().in(PackageDO::getId, packageIds))
                .stream().collect(Collectors.toMap(PackageDO::getId, p -> p));

        Set<String> codes = packageById.values().stream()
                .map(PackageDO::getNameI18nCode)
                .filter(c -> c != null && !c.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
        Map<String, String> nameByCode = i18nMessageService.translateMap(codes, locale);

        LocalDateTime now = LocalDateTime.now();
        return sps.stream().map(sp -> {
            AppStudentPackageRespVO vo = new AppStudentPackageRespVO();
            vo.setId(sp.getId());
            vo.setPackageId(sp.getPackageId());
            vo.setRemaining(sp.getRemaining());
            vo.setExpireAt(sp.getExpireAt());
            vo.setSource(sp.getSource());
            vo.setStatus(computeStatus(sp, now));
            vo.setPurchasedAt(sp.getCreateTime());
            vo.setPaymentId(sp.getPaymentId());
            PackageDO pkg = packageById.get(sp.getPackageId());
            if (pkg != null) {
                vo.setName(nameByCode.get(pkg.getNameI18nCode()));
                vo.setTotalCount(pkg.getTotalCount());
                vo.setWeeklyCount(pkg.getWeeklyCount());
                vo.setIsFreeTrial(pkg.getIsFreeTrial());
                vo.setPrice(pkg.getPrice());
                vo.setCurrency(pkg.getCurrency());
            }
            return vo;
        }).toList();
    }

    /**
     * 三态:active(可用)/ expired(过期)/ exhausted(用完)。
     * 优先级:用完 > 过期 > 可用。
     */
    static String computeStatus(StudentPackageDO sp, LocalDateTime now) {
        if (sp.getRemaining() == null || sp.getRemaining() <= 0) {
            return "exhausted";
        }
        if (sp.getExpireAt() != null && sp.getExpireAt().isBefore(now)) {
            return "expired";
        }
        return "active";
    }

    private AppCourseOrderRespVO toOrderResp(CourseOrderDO order, String locale) {
        AppCourseOrderRespVO vo = new AppCourseOrderRespVO();
        vo.setId(order.getId());
        vo.setStudentId(order.getStudentId());
        vo.setTeacherId(order.getTeacherId());
        vo.setStudentPackageId(order.getStudentPackageId());
        vo.setScheduledAt(order.getScheduledAt());
        vo.setDuration(order.getDuration());
        vo.setPriceDisplay(order.getPriceDisplay());
        vo.setCurrency(order.getCurrency());
        vo.setStatus(order.getStatus());
        vo.setIsFreeTrial(order.getIsFreeTrial());
        vo.setCreateTime(order.getCreateTime());

        UserDO teacher = eduUserMapper.selectById(order.getTeacherId());
        if (teacher != null) {
            vo.setTeacherNickname(teacher.getNickname());
        }

        if (order.getStudentPackageId() != null) {
            StudentPackageDO sp = studentPackageService.getStudentPackage(order.getStudentPackageId());
            if (sp != null) {
                PackageDO pkg = packageMapper.selectById(sp.getPackageId());
                if (pkg != null && pkg.getNameI18nCode() != null) {
                    vo.setPackageName(i18nMessageService.translate(pkg.getNameI18nCode(), locale));
                }
            }
        }
        return vo;
    }

}
