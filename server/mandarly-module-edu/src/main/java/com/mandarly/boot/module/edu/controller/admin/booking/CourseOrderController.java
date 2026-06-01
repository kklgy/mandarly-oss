package com.mandarly.boot.module.edu.controller.admin.booking;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.BookingCancelReqVO;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.BookingCreateReqVO;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.CourseOrderPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.CourseOrderRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.service.booking.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 课程订单 / 预约")
@RestController
@RequestMapping("/edu/course-order")
@Validated
public class CourseOrderController {

    @Resource
    private BookingService bookingService;

    @GetMapping("/page")
    @Operation(summary = "订单分页")
    @PreAuthorize("@ss.hasPermission('edu:course-order:query')")
    public CommonResult<PageResult<CourseOrderRespVO>> getPage(@Valid CourseOrderPageReqVO reqVO) {
        PageResult<CourseOrderDO> page = bookingService.getOrderPage(reqVO);
        return success(BeanUtils.toBean(page, CourseOrderRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取订单详情")
    @Parameter(name = "id", required = true, example = "100")
    @PreAuthorize("@ss.hasPermission('edu:course-order:query')")
    public CommonResult<CourseOrderRespVO> get(@RequestParam("id") Long id) {
        CourseOrderDO order = bookingService.getOrder(id);
        return success(BeanUtils.toBean(order, CourseOrderRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "管理员代下订单(状态机起点 → upcoming)")
    @PreAuthorize("@ss.hasPermission('edu:course-order:create')")
    public CommonResult<Long> create(@Valid @RequestBody BookingCreateReqVO reqVO) {
        return success(bookingService.createOrder(reqVO));
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消订单(24h+ 返还课次 / 24h 内不返还)")
    @PreAuthorize("@ss.hasPermission('edu:course-order:cancel')")
    public CommonResult<Boolean> cancel(@Valid @RequestBody BookingCancelReqVO reqVO) {
        bookingService.cancelOrder(reqVO);
        return success(true);
    }

}
