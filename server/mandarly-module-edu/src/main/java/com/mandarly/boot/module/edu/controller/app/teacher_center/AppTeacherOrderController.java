package com.mandarly.boot.module.edu.controller.app.teacher_center;

import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherOrderPageReqVO;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherOrderRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.service.booking.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * App 端 - 教师中心:订单(M6 §3.7 / §5.2 T2)
 *
 * <p>透传 {@link BookingService#getTeacherOrdersPage}(M3 era,A6 新增) 与
 * {@link BookingService#getOrder};权限走 `@ss.hasRole('teacher')`,teacherId 由
 * {@link SecurityFrameworkUtils#getLoginUserId()} 兜底取(防越权)。
 */
@Tag(name = "用户端 - 教师中心 - 订单")
@RestController("appTeacherCenterOrderController")
@RequestMapping("/edu/teacher-center/orders")
@Validated
public class AppTeacherOrderController {

    @Resource
    private BookingService bookingService;

    @Resource
    private EduUserMapper eduUserMapper;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "教师本人订单分页(可选 status 过滤,逗号分隔)")
    public CommonResult<PageResult<AppTeacherOrderRespVO>> list(@Valid AppTeacherOrderPageReqVO reqVO) {
        Long teacherId = SecurityFrameworkUtils.getLoginUserId();
        Set<String> statuses = parseStatuses(reqVO.getStatus());
        PageResult<CourseOrderDO> page = bookingService.getTeacherOrdersPage(teacherId, statuses, reqVO);

        List<AppTeacherOrderRespVO> list = page.getList().stream()
                .map(this::toResp)
                .toList();
        return success(new PageResult<>(list, page.getTotal()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "订单详情(教师视角)")
    @Parameter(name = "id", description = "course_order.id", required = true, example = "10001")
    public CommonResult<AppTeacherOrderRespVO> get(@PathVariable("id") @Positive Long id) {
        CourseOrderDO order = bookingService.getOrder(id);
        if (order == null) {
            throw ServiceExceptionUtil.exception0(404, "订单不存在");
        }
        Long teacherId = SecurityFrameworkUtils.getLoginUserId();
        if (!teacherId.equals(order.getTeacherId())) {
            throw ServiceExceptionUtil.exception0(403, "无权查看该订单");
        }
        return success(toResp(order));
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

    private AppTeacherOrderRespVO toResp(CourseOrderDO order) {
        AppTeacherOrderRespVO vo = new AppTeacherOrderRespVO();
        vo.setId(order.getId());
        vo.setStudentId(order.getStudentId());
        vo.setScheduledAt(order.getScheduledAt());
        vo.setDuration(order.getDuration());
        vo.setStatus(order.getStatus());
        vo.setTeacherAmount(order.getTeacherAmount());
        vo.setTeacherSettleStatus(order.getTeacherSettleStatus());
        vo.setIsFreeTrial(order.getIsFreeTrial());
        vo.setFinishedAt(order.getFinishedAt());
        vo.setCreateTime(order.getCreateTime());

        UserDO student = eduUserMapper.selectById(order.getStudentId());
        if (student != null) {
            vo.setStudentNickname(student.getNickname());
            vo.setStudentAvatar(student.getAvatarUrl());
        }
        return vo;
    }
}
