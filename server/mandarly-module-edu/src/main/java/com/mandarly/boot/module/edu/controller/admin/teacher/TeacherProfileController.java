package com.mandarly.boot.module.edu.controller.admin.teacher;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.admin.teacher.vo.TeacherProfileAuditReqVO;
import com.mandarly.boot.module.edu.controller.admin.teacher.vo.TeacherProfilePageReqVO;
import com.mandarly.boot.module.edu.controller.admin.teacher.vo.TeacherProfileRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.service.teacher.TeacherProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 教师档案")
@RestController
@RequestMapping("/edu/teacher-profile")
@Validated
public class TeacherProfileController {

    @Resource
    private TeacherProfileService teacherProfileService;
    @Resource
    private EduUserMapper eduUserMapper;

    @GetMapping("/page")
    @Operation(summary = "教师档案分页查询")
    @PreAuthorize("@ss.hasPermission('edu:teacher-profile:query')")
    public CommonResult<PageResult<TeacherProfileRespVO>> getTeacherProfilePage(@Valid TeacherProfilePageReqVO reqVO) {
        PageResult<TeacherProfileDO> page = teacherProfileService.getTeacherProfilePage(reqVO);
        Map<Long, String> phoneMap = buildPhoneMap(page.getList());
        return success(BeanUtils.toBean(page, TeacherProfileRespVO.class,
                vo -> {
                    vo.setPhone(phoneMap.get(vo.getUserId()));
                    // D19 续: DB 存 object key, 出参签成 COS 15min URL 供 admin 审核详情视频预览
                    vo.setIntroVideoUrl(teacherProfileService.presignIntroVideoUrl(vo.getIntroVideoUrl()));
                }));
    }

    @GetMapping("/get")
    @Operation(summary = "获取教师档案详情")
    @Parameter(name = "userId", description = "教师 user.id", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:teacher-profile:query')")
    public CommonResult<TeacherProfileRespVO> getTeacherProfile(@RequestParam("userId") Long userId) {
        TeacherProfileDO profile = teacherProfileService.getTeacherProfile(userId);
        TeacherProfileRespVO vo = BeanUtils.toBean(profile, TeacherProfileRespVO.class);
        if (vo != null) {
            UserDO user = eduUserMapper.selectById(userId);
            vo.setPhone(user == null ? null : user.getPhone());
            // D19 续: DB 存 object key, 出参签成 COS 15min URL 供 admin 审核详情视频预览
            vo.setIntroVideoUrl(teacherProfileService.presignIntroVideoUrl(vo.getIntroVideoUrl()));
        }
        return success(vo);
    }

    @PutMapping("/audit")
    @Operation(summary = "审核教师档案(approve / reject)")
    @PreAuthorize("@ss.hasPermission('edu:teacher-profile:audit')")
    public CommonResult<Boolean> auditTeacherProfile(@Valid @RequestBody TeacherProfileAuditReqVO reqVO) {
        Long operatorAdminId = SecurityFrameworkUtils.getLoginUserId();
        teacherProfileService.auditTeacherProfile(reqVO, operatorAdminId);
        return success(true);
    }

    private Map<Long, String> buildPhoneMap(List<TeacherProfileDO> profiles) {
        Map<Long, String> phoneMap = new HashMap<>();
        if (profiles == null || profiles.isEmpty()) {
            return phoneMap;
        }
        List<Long> userIds = profiles.stream()
                .map(TeacherProfileDO::getUserId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return phoneMap;
        }
        for (UserDO user : eduUserMapper.selectBatchIds(userIds)) {
            phoneMap.put(user.getId(), user.getPhone());
        }
        return phoneMap;
    }

}
