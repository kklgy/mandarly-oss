package com.mandarly.boot.module.edu.controller.app.teacher_center;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherQualificationReqVO;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherQualificationRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherQualificationDO;
import com.mandarly.boot.module.edu.service.teacher.TeacherQualificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * App 端 - 教师中心:教师本人资质材料管理(D19 Phase B / B5)。
 *
 * <p>对应 PRD-v4 §4.2 T0 教师中心导航 + docs/database/01-users-auth.md §4.4。
 * 教师 onboarding 流程中通过本入口上传 / 删除 / 列举自己的资质材料,
 * 上传后 admin 在 D19-A 教师审核台逐条审核。
 *
 * <p>路径前缀 {@code /edu/teacher-center/qualification} 对齐 D16 教师中心其他
 * controller({@code /edu/teacher-center/dashboard|schedule|profile|...}),
 * 与 admin 端 {@code /edu/teacher-qualification/list} 不冲突。
 *
 * <p>权限走 {@code @ss.hasRole('teacher')}(M6 桥接);userId 由
 * {@link SecurityFrameworkUtils#getLoginUserId()} 兜底取,
 * <strong>不接收</strong>路径 / body 中的 userId 参数(防越权)。
 * 删除走 service {@code deleteByUserAndId(userId, id)} 双因子校验。
 */
@Tag(name = "用户端 - 教师中心 - 教师资质(本人)")
@RestController("appTeacherCenterQualificationController")
@RequestMapping("/edu/teacher-center/qualification")
@Validated
public class AppTeacherQualificationController {

    @Resource
    private TeacherQualificationService teacherQualificationService;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "教师 - 列本人资质材料(不暴露 docUrl,只展示文件名 + 审核状态)")
    public CommonResult<List<AppTeacherQualificationRespVO>> listMine() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<TeacherQualificationDO> rows = teacherQualificationService.listByUserId(userId);
        return success(BeanUtils.toBean(rows, AppTeacherQualificationRespVO.class));
    }

    @PostMapping("/upload")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "教师 - 记录已上传至 COS 的资质材料(前端先调 /infra/file/upload 拿 docUrl)")
    public CommonResult<Long> upload(@Valid @RequestBody AppTeacherQualificationReqVO req) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        Long id = teacherQualificationService.uploadQualification(
                userId, req.getDocType(), req.getDocUrl(), req.getDocFilename());
        return success(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "教师 - 删除本人的资质材料(owner check 严格)")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        teacherQualificationService.deleteByUserAndId(userId, id);
        return success(true);
    }

}
