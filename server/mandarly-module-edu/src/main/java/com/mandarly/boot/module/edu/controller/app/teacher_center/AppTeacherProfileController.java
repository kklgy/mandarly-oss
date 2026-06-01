package com.mandarly.boot.module.edu.controller.app.teacher_center;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherProfileMeRespVO;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherProfileUpdateReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherQualificationDO;
import com.mandarly.boot.module.edu.service.teacher.TeacherProfileService;
import com.mandarly.boot.module.edu.service.teacher.TeacherQualificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * App 端 - 教师中心:教师本人档案(D19 Phase B / B4)。
 *
 * <p>对应 PRD-v4 §4.2 T0 教师中心导航 + docs/database/01-users-auth.md §4.3。
 * 教师注册后通过本入口看 / 改自我档案 + 提交审核(rejected → pending 复审)。
 *
 * <p>路径前缀 {@code /edu/teacher-center/profile} 对齐 D16 教师中心其他 controller
 * ({@code /edu/teacher-center/dashboard|schedule|...}),与 admin 端
 * {@code /edu/teacher-profile/page|get|audit} 不冲突。
 *
 * <p>权限走 {@code @ss.hasRole('teacher')}(M6 桥接);userId 由
 * {@link SecurityFrameworkUtils#getLoginUserId()} 兜底取,<strong>不接收</strong>
 * 路径 / body 中的 userId 参数(防越权)。
 */
@Tag(name = "用户端 - 教师中心 - 教师档案(本人)")
@RestController("appTeacherCenterProfileController")
@RequestMapping("/edu/teacher-center/profile")
@Validated
public class AppTeacherProfileController {

    @Resource
    private TeacherProfileService teacherProfileService;
    @Resource
    private TeacherQualificationService teacherQualificationService;

    @GetMapping("/me")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "教师 - 获取本人档案 + 审核状态 + 资质 summary")
    public CommonResult<AppTeacherProfileMeRespVO> getMyProfile() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        TeacherProfileDO profile = teacherProfileService.getTeacherProfile(userId);

        // BeanUtils.toBean 对 null 输入安全返 null;profile 不存在时(A2 容错失败的 edge case)
        // 仍要回 userId 让前端展示"档案待初始化"引导
        AppTeacherProfileMeRespVO vo = BeanUtils.toBean(profile, AppTeacherProfileMeRespVO.class);
        if (vo == null) {
            vo = new AppTeacherProfileMeRespVO();
        }
        vo.setUserId(userId);
        // D19 续: DB 存 object key, 出参签成 COS 15min URL 供 ProfileEditView 视频预览播放
        vo.setIntroVideoUrl(teacherProfileService.presignIntroVideoUrl(vo.getIntroVideoUrl()));

        List<TeacherQualificationDO> quals = teacherQualificationService.listByUserId(userId);
        vo.setQualificationCount(quals == null ? 0 : quals.size());

        return success(vo);
    }

    @PutMapping("/me")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "教师 - 更新本人档案(partial update,不修改 audit_status)")
    public CommonResult<Boolean> updateMyProfile(@Valid @RequestBody AppTeacherProfileUpdateReqVO req) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        teacherProfileService.updateOwnProfile(userId, req);
        return success(true);
    }

    @PostMapping("/submit-audit")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "教师 - 提交审核(rejected → pending 状态机)")
    public CommonResult<Boolean> submitForAudit() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        teacherProfileService.submitForAudit(userId);
        return success(true);
    }

}
