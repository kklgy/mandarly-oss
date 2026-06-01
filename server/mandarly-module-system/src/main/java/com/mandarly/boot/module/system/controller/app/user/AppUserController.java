package com.mandarly.boot.module.system.controller.app.user;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.framework.common.util.servlet.ServletUtils;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.mandarly.boot.module.system.controller.app.user.vo.*;
import com.mandarly.boot.module.system.dal.dataobject.user.AppUserDO;
import com.mandarly.boot.module.system.dal.dataobject.user.UserOAuthDO;
import com.mandarly.boot.module.system.enums.sms.SmsSceneEnum;
import com.mandarly.boot.module.system.service.auth.MailCodeService;
import com.mandarly.boot.module.system.service.sms.SmsCodeService;
import com.mandarly.boot.module.system.service.user.UserOAuthService;
import com.mandarly.boot.module.system.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;
import static com.mandarly.boot.module.system.service.auth.AppAuthServiceImpl.isMainlandMobile;

@Tag(name = "App 端 - 用户")
@RestController
@RequestMapping("/system/user")
@Validated
@PreAuthorize("isAuthenticated()")
@Slf4j
public class AppUserController {

    @Resource
    private UserService userService;

    @Resource
    private UserOAuthService userOAuthService;

    @Resource
    private MailCodeService mailCodeService;

    @Resource
    private SmsCodeService smsCodeService;

    @Resource
    private ApplicationContext applicationContext;

    @GetMapping("/me")
    @Operation(summary = "我的资料 + 三方绑定列表")
    public CommonResult<AppUserProfileRespVO> getMe() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        AppUserDO user = userService.getById(userId);
        AppUserProfileRespVO vo = BeanUtils.toBean(user, AppUserProfileRespVO.class);
        // D20 头像上线: DB 存 object key, 出参签 COS 15min URL 供顶部 AppHeader / AccountView 渲染
        vo.setAvatarUrl(userService.presignAvatarUrl(vo.getAvatarUrl()));
        List<UserOAuthDO> bindings = userOAuthService.listActiveByUser(userId);
        vo.setOauthBindings(bindings.stream()
                .map(b -> new AppOAuthBindingItemVO(b.getProvider(), b.getOauthEmail(), b.getBoundAt()))
                .collect(Collectors.toList()));
        // D19-A7: role=teacher 时反射读 edu 模块 TeacherProfile,暴露审核状态供前端横幅
        enrichTeacherAuditStatus(vo, user);
        return success(vo);
    }

    /**
     * D19-A7: role=teacher 时反射读 edu 模块 TeacherProfileServiceImpl.getTeacherProfile(userId),
     * 填充 teacherAuditStatus / teacherRejectReason 字段。
     *
     * <p>反射方式跨模块调用,避免 system 模块强依赖 edu;edu bean 缺失或抛错时优雅降级,
     * 不影响 getMe 主流程返回。
     */
    // package-private 便于单元测试直接调用,绕过 SecurityFrameworkUtils.getLoginUserId() 的 static 依赖
    void enrichTeacherAuditStatus(AppUserProfileRespVO vo, AppUserDO user) {
        if (user == null || !"teacher".equals(user.getRole())) {
            return;
        }
        try {
            Object bean = applicationContext.getBean("teacherProfileServiceImpl");
            Object profile = bean.getClass()
                    .getMethod("getTeacherProfile", Long.class)
                    .invoke(bean, user.getId());
            if (profile != null) {
                String auditStatus = (String) profile.getClass()
                        .getMethod("getAuditStatus").invoke(profile);
                String rejectReason = (String) profile.getClass()
                        .getMethod("getRejectReason").invoke(profile);
                vo.setTeacherAuditStatus(auditStatus);
                vo.setTeacherRejectReason(rejectReason);
            }
        } catch (Exception e) {
            log.warn("[D19-A7] getMe 读教师档案失败 userId={}", user.getId(), e);
        }
    }

    @PutMapping("/me")
    @Operation(summary = "更新资料")
    public CommonResult<Boolean> updateMe(@Valid @RequestBody AppUserUpdateReqVO req) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        userService.updateProfile(userId,
                req.getNickname(), req.getAvatarUrl(), req.getLocale(),
                req.getTimezone(), req.getLearningGoal());
        return success(true);
    }

    @PostMapping("/bind-email")
    @Operation(summary = "绑定邮箱")
    public CommonResult<Boolean> bindEmail(@Valid @RequestBody AppUserBindEmailReqVO req) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        mailCodeService.useCode(req.getEmail(), req.getCode(), "bind");
        userService.bindEmail(userId, req.getEmail());
        return success(true);
    }

    @PostMapping("/bind-phone")
    @Operation(summary = "绑定手机")
    public CommonResult<Boolean> bindPhone(@Valid @RequestBody AppUserBindPhoneReqVO req) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        SmsCodeUseReqDTO dto = new SmsCodeUseReqDTO();
        dto.setMobile(req.getPhone());
        dto.setCode(req.getCode());
        dto.setScene(isMainlandMobile(req.getPhone())
                ? SmsSceneEnum.MEMBER_UPDATE_MOBILE_CN.getScene()
                : SmsSceneEnum.MEMBER_UPDATE_MOBILE.getScene());
        dto.setUsedIp(ServletUtils.getClientIP());
        smsCodeService.useSmsCode(dto);
        userService.bindPhone(userId, req.getPhone());
        return success(true);
    }
}
