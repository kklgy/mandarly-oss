package com.mandarly.boot.module.edu.controller.app.classroom;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.classroom.vo.AppClassroomJoinInfoRespVO;
import com.mandarly.boot.module.edu.service.classroom.ClassroomService;
import com.mandarly.boot.module.edu.service.classroom.dto.LcicJoinInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * App 端 - 课堂(进入课堂 join 信息)。
 *
 * <p>对应 PRD-v4 §4.6 视频课堂。M2.5 已切真 token,userId 从 SecurityFrameworkUtils.getLoginUserId() 取。
 *
 * <p>角色判定 + 状态校验都在 {@link ClassroomService#getJoinInfo(Long, Long)} 内部:
 *    userId 必须等于 order.studentId / order.teacherId,否则 403;order 必须 upcoming。
 */
@Tag(name = "用户端 - 视频课堂")
@RestController
@RequestMapping("/edu/classroom")
@Validated
public class AppClassroomController {

    @Resource
    private ClassroomService classroomService;

    @GetMapping("/{orderId}/join-info")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取进入课堂的 join URL + token(实时签发)")
    @Parameter(name = "orderId", description = "course_order.id", required = true, example = "1")
    public CommonResult<AppClassroomJoinInfoRespVO> getJoinInfo(@PathVariable("orderId") Long orderId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        LcicJoinInfo info = classroomService.getJoinInfo(orderId, userId);
        AppClassroomJoinInfoRespVO vo = new AppClassroomJoinInfoRespVO();
        vo.setJoinUrl(info.getJoinUrl());
        vo.setToken(info.getToken());
        vo.setExpiresAt(info.getExpiresAt());
        vo.setRole(info.getRole());
        return success(vo);
    }

}
