package com.mandarly.boot.module.edu.controller.app.teacher;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.teacher.vo.AppTeacherListReqVO;
import com.mandarly.boot.module.edu.controller.app.teacher.vo.AppTeacherSimpleRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.service.teacher.TeacherProfileService;
import com.mandarly.boot.module.system.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "用户端 - 教师")
@RestController
@RequestMapping("/edu/teacher")
@Validated
public class AppTeacherController {

    @Resource
    private TeacherProfileService teacherProfileService;

    @Resource
    private EduUserMapper eduUserMapper;

    @Resource
    private UserService userService;

    @GetMapping("/list")
    @Operation(summary = "已上线教师列表(支持 keyword / accent / priceBuckets / available / minRating / "
            + "expertise / tags / sort / pageNo / pageSize)")
    @PermitAll
    public CommonResult<List<AppTeacherSimpleRespVO>> listVisibleTeachers(AppTeacherListReqVO reqVO) {
        // D27-C:教师信息是公开数据(对齐 D25 §11.2=B + Cambly/Italki marketplace 公开范式),
        // 解除未登录闸门;VO 中无价格/敏感字段(price 在 package 维度,已 D27-C 同步脱敏)
        // M5 Wave 4 Subagent E:支持 7 query 参数 + 分页
        List<TeacherProfileDO> teachers = teacherProfileService.listVisibleTeachers(reqVO);
        Map<Long, UserDO> users = loadUsers(
                teachers.stream().map(TeacherProfileDO::getUserId).toList());
        List<AppTeacherSimpleRespVO> result = teachers.stream().map(p -> {
            AppTeacherSimpleRespVO vo = BeanUtils.toBean(p, AppTeacherSimpleRespVO.class);
            enrichTeacherUserFields(vo, p, users.get(p.getUserId()));
            vo.setProfileVisible(true);
            vo.setIntroVideoUrl(teacherProfileService.presignIntroVideoUrl(vo.getIntroVideoUrl()));
            return vo;
        }).toList();
        return success(result);
    }

    @GetMapping("/count")
    @Operation(summary = "已上线教师匹配数(用于抽屉应用筛选按钮文案)")
    @PermitAll
    public CommonResult<Map<String, Long>> countVisibleTeachers(AppTeacherListReqVO reqVO) {
        // D27-C:跟 listVisibleTeachers 对齐,匿名也能拿筛选总数
        long total = teacherProfileService.countVisibleTeachers(reqVO);
        Map<String, Long> body = new HashMap<>();
        body.put("total", total);
        return success(body);
    }

    @GetMapping("/get")
    @Operation(summary = "教师详情(脱敏)")
    @Parameter(name = "userId", description = "教师 user.id", required = true, example = "1024")
    @PermitAll
    public CommonResult<AppTeacherSimpleRespVO> getTeacher(@RequestParam("userId") Long userId) {
        // D27-C:教师详情公开,匿名访问 /teacher/:userId 也能看(预约按钮触发登录)
        TeacherProfileDO profile = teacherProfileService.getTeacherProfile(userId);
        if (profile == null) {
            return success(null);
        }
        AppTeacherSimpleRespVO vo = BeanUtils.toBean(profile, AppTeacherSimpleRespVO.class);
        enrichTeacherUserFields(vo, profile, loadUsers(List.of(profile.getUserId())).get(profile.getUserId()));
        vo.setProfileVisible(true);
        vo.setIntroVideoUrl(teacherProfileService.presignIntroVideoUrl(vo.getIntroVideoUrl()));
        return success(vo);
    }

    private boolean isLoggedIn() {
        return SecurityFrameworkUtils.getLoginUserId() != null;
    }

    private void enrichTeacherUserFields(AppTeacherSimpleRespVO vo, TeacherProfileDO profile, UserDO user) {
        if (user != null) {
            vo.setNickname(user.getNickname() == null ? "" : user.getNickname());
            vo.setAvatar(userService.presignAvatarUrl(user.getAvatarUrl()));
            vo.setTeacherTimezone(user.getTimezone());
        }
        vo.setHasIntroVideo(profile.getIntroVideoUrl() != null && !profile.getIntroVideoUrl().isBlank());
    }

    private Map<Long, UserDO> loadUsers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyMap();
        return eduUserMapper.selectBatchIds(userIds)
                .stream().collect(Collectors.toMap(UserDO::getId, u -> u, (a, b) -> a));
    }

}
