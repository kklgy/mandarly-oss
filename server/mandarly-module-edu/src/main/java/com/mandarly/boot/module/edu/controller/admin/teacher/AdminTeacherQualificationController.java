package com.mandarly.boot.module.edu.controller.admin.teacher;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.teacher.vo.AdminTeacherQualificationRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherQualificationDO;
import com.mandarly.boot.module.edu.service.teacher.TeacherQualificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 教师资质材料(D19 Phase B / B5)。
 *
 * <p>对应 PRD-v4 §7.2 教师审核台 — admin 在教师详情页查看该教师上传的全部资质,
 * 并在线预览证件(预签名 URL 15min TTL)。
 *
 * <p>路径 {@code /edu/teacher-qualification} 与 D19-A 已有的 admin 教师审核台
 * ({@code /edu/teacher-profile/page|get|audit})平行;权限点
 * {@code edu:teacher-qualification:query} 在 D19 SQL patch
 * {@code 20260518_130000_add_teacher_qualification_admin_menu.sql} 中作为按钮挂在
 * 「教师审核」(menu id 3002)下。
 *
 * <p>B11 admin detail.vue 走本接口拉取资质列表;后续审核单条资质的接口在 D19-A 已落地
 * (qualification 审核功能在本期作为 admin 详情查看,审核单条资质属 D19-A 范畴)。
 */
@Tag(name = "管理后台 - 教师资质")
@RestController
@RequestMapping("/edu/teacher-qualification")
@Validated
public class AdminTeacherQualificationController {

    @Resource
    private TeacherQualificationService teacherQualificationService;

    @GetMapping("/list")
    @Operation(summary = "查指定教师的资质材料(docUrl 已替换为 15min 预签名 URL)")
    @Parameter(name = "userId", description = "教师 user.id", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:teacher-qualification:query')")
    public CommonResult<List<AdminTeacherQualificationRespVO>> list(@RequestParam("userId") Long userId) {
        List<TeacherQualificationDO> rows = teacherQualificationService.listByUserId(userId);
        if (rows == null || rows.isEmpty()) {
            return success(new ArrayList<>());
        }
        List<AdminTeacherQualificationRespVO> result = BeanUtils.toBean(rows, AdminTeacherQualificationRespVO.class);
        // docUrl 私有 bucket 直链不可访问,统一替换为 15min 预签名 URL
        for (int i = 0; i < rows.size(); i++) {
            String signed = teacherQualificationService.generatePresignedUrl(rows.get(i).getDocUrl());
            result.get(i).setDocUrl(signed);
        }
        return success(result);
    }

}
