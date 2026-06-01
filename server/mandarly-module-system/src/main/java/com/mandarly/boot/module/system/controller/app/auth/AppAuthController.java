package com.mandarly.boot.module.system.controller.app.auth;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.framework.common.util.servlet.ServletUtils;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import com.mandarly.boot.module.system.controller.app.auth.vo.*;
import com.mandarly.boot.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.mandarly.boot.module.system.dal.dataobject.user.AppUserDO;
import com.mandarly.boot.module.system.enums.sms.SmsSceneEnum;
import com.mandarly.boot.module.system.service.auth.AppAuthService;
import com.mandarly.boot.module.system.service.auth.MailCodeService;
import com.mandarly.boot.module.system.service.auth.SocialClient;
import com.mandarly.boot.module.system.service.auth.SocialUserInfo;
import com.mandarly.boot.module.system.service.oauth2.OAuth2TokenService;
import com.mandarly.boot.module.system.service.sms.SmsCodeService;
import com.mandarly.boot.module.system.service.user.UserOAuthService;
import com.mandarly.boot.module.system.service.user.UserService;
import com.mandarly.boot.module.system.dal.dataobject.user.UserOAuthDO;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.system.enums.ErrorCodeConstants.AUTH_SOCIAL_ACCOUNT_CONFLICT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;
import static com.mandarly.boot.module.system.service.auth.AppAuthServiceImpl.checkSmsPhoneSupported;
import static com.mandarly.boot.module.system.service.auth.AppAuthServiceImpl.isMainlandMobile;

@Tag(name = "App 端 - 认证")
@RestController
@RequestMapping("/system/auth")
@Validated
public class AppAuthController {

    @Resource
    private AppAuthService appAuthService;
    @Resource
    private MailCodeService mailCodeService;
    @Resource
    private SmsCodeService smsCodeService;
    @Resource
    private SocialClient socialClient;
    @Resource
    private UserService userService;
    @Resource
    private UserOAuthService userOAuthService;
    @Resource
    private OAuth2TokenService oauth2TokenService;

    // ==================== 公开接口(无需认证) ====================

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "邮箱密码登录")
    public CommonResult<AppAuthLoginRespVO> login(@Valid @RequestBody AppAuthLoginReqVO req,
                                                  HttpServletRequest httpReq) {
        OAuth2AccessTokenDO token = appAuthService.loginByEmail(
                req.getEmail(), req.getPassword(), ServletUtils.getClientIP(httpReq));
        return success(toLoginRespVO(token));
    }

    @PostMapping("/sms-login")
    @PermitAll
    @Operation(summary = "手机验证码登录")
    public CommonResult<AppAuthLoginRespVO> smsLogin(@Valid @RequestBody AppAuthSmsLoginReqVO req,
                                                     HttpServletRequest httpReq) {
        OAuth2AccessTokenDO token = appAuthService.loginByPhone(
                req.getPhone(), req.getCode(), req.getLocale(), ServletUtils.getClientIP(httpReq));
        return success(toLoginRespVO(token));
    }

    @PostMapping("/social-login")
    @PermitAll
    @Operation(summary = "三方账号登录")
    public CommonResult<AppAuthLoginRespVO> socialLogin(@Valid @RequestBody AppAuthSocialLoginReqVO req,
                                                        HttpServletRequest httpReq) {
        OAuth2AccessTokenDO token = appAuthService.loginBySocial(
                req.getType(), req.getCode(), req.getState(),
                ServletUtils.getClientIP(httpReq), req.getRole());
        return success(toLoginRespVO(token));
    }

    @PostMapping("/register")
    @PermitAll
    @Operation(summary = "邮箱注册")
    public CommonResult<AppAuthLoginRespVO> register(@Valid @RequestBody AppAuthRegisterReqVO req,
                                                     HttpServletRequest httpReq) {
        OAuth2AccessTokenDO token = appAuthService.registerByEmail(
                req.getRole(), req.getEmail(), req.getPassword(), req.getCode(),
                req.getNickname(), req.getLocale(), req.getTz(), req.getRefCode(),
                ServletUtils.getClientIP(httpReq));
        return success(toLoginRespVO(token));
    }

    @PostMapping("/sms-register")
    @PermitAll
    @Operation(summary = "手机注册")
    public CommonResult<AppAuthLoginRespVO> smsRegister(@Valid @RequestBody AppAuthSmsRegisterReqVO req,
                                                        HttpServletRequest httpReq) {
        OAuth2AccessTokenDO token = appAuthService.registerByPhone(
                req.getRole(), req.getPhone(), req.getCode(),
                req.getNickname(), req.getLocale(), req.getTz(), req.getRefCode(),
                ServletUtils.getClientIP(httpReq));
        return success(toLoginRespVO(token));
    }

    @PostMapping("/send-email-code")
    @PermitAll
    @Operation(summary = "发送邮箱验证码")
    public CommonResult<Boolean> sendEmailCode(@Valid @RequestBody AppAuthSendEmailCodeReqVO req) {
        mailCodeService.sendCode(req.getEmail(), req.getScene(), req.getLocale());
        return success(true);
    }

    @PostMapping("/send-sms-code")
    @PermitAll
    @Operation(summary = "发送手机验证码")
    public CommonResult<Boolean> sendSmsCode(@Valid @RequestBody AppAuthSendSmsCodeReqVO req,
                                             HttpServletRequest httpReq) {
        checkSmsPhoneSupported(req.getPhone());
        SmsCodeSendReqDTO dto = new SmsCodeSendReqDTO();
        dto.setMobile(req.getPhone());
        dto.setScene(mapSmsScene(req.getScene(), req.getLocale(), req.getPhone()));
        dto.setCreateIp(ServletUtils.getClientIP(httpReq));
        smsCodeService.sendSmsCode(dto);
        return success(true);
    }

    @PostMapping("/refresh-token")
    @PermitAll
    @Operation(summary = "刷新 token")
    public CommonResult<AppAuthLoginRespVO> refreshToken(@Valid @RequestBody AppAuthRefreshTokenReqVO req) {
        OAuth2AccessTokenDO token = oauth2TokenService.refreshAccessToken(req.getRefreshToken(), "app-client");
        return success(toLoginRespVO(token));
    }

    @PostMapping("/reset-password")
    @PermitAll
    @Operation(summary = "重置密码")
    public CommonResult<Boolean> resetPassword(@Valid @RequestBody AppAuthResetPasswordReqVO req) {
        // 1. 校验邮箱验证码(scene=reset 一次性)
        mailCodeService.useCode(req.getEmail(), req.getCode(), "reset");
        // 2. 校验密码强度 + BCrypt 加密更新(由 UserService 内部完成)
        userService.updatePasswordByEmail(req.getEmail(), req.getNewPassword());
        return success(true);
    }

    @GetMapping("/social-redirect-url")
    @PermitAll
    @Operation(summary = "获取三方登录跳转 URL")
    public CommonResult<AppAuthSocialRedirectRespVO> socialRedirectUrl(@RequestParam("type") String type) {
        String url = socialClient.getRedirectUrl(type);
        return success(new AppAuthSocialRedirectRespVO(url));
    }

    @GetMapping("/channels")
    @PermitAll
    @Operation(summary = "4 渠道凭证就位状态(LoginView/RegisterView 据此 disable 不可用渠道)")
    public CommonResult<AppAuthChannelsRespVO> channels() {
        return success(appAuthService.getAvailableChannels());
    }

    // ==================== 需认证接口 ====================

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "登出")
    public CommonResult<Boolean> logout(HttpServletRequest httpReq) {
        String token = SecurityFrameworkUtils.obtainAuthorization(httpReq, "Authorization", "access_token");
        appAuthService.logout(token);
        return success(true);
    }

    @PostMapping("/bind-social")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "已登录用户绑定三方账号")
    public CommonResult<Boolean> bindSocial(@Valid @RequestBody AppAuthBindSocialReqVO req) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 1. 走三方平台拿用户信息(凭证未就位时 SocialClient 内部 throw AUTH_SOCIAL_NOT_CONFIGURED)
        SocialUserInfo info = socialClient.getUserInfo(req.getType(), req.getCode(), req.getState());
        // 2. conflict 检查:同 (provider, oauthUid) 已存在 active 绑定
        UserOAuthDO active = userOAuthService.findActive(req.getType(), info.getOauthUid());
        if (active != null) {
            // 已绑给自己 → 幂等成功;绑给别人 → conflict
            if (loginUserId.equals(active.getUserId())) {
                return success(true);
            }
            throw exception(AUTH_SOCIAL_ACCOUNT_CONFLICT);
        }
        // 3. 真绑
        userOAuthService.bind(loginUserId, req.getType(), info.getOauthUid(),
                info.getOauthEmail(), info.getRawAttributes());
        return success(true);
    }

    @PostMapping("/unbind-social")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "解绑三方账号")
    public CommonResult<Boolean> unbindSocial(@Valid @RequestBody AppAuthUnbindSocialReqVO req) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 按 (userId, provider) 查 active 绑定再解 — 同 user 同 provider 最多一条 active,
        // 不需要前端传 oauthUid(getMe 返回的 binding VO 本就不暴露 oauthUid)
        userOAuthService.unbindByUserAndProvider(loginUserId, req.getProvider());
        return success(true);
    }

    // ==================== 私有工具方法 ====================

    /**
     * 将 OAuth2AccessTokenDO 转换为登录响应 VO,并填充用户角色
     */
    private AppAuthLoginRespVO toLoginRespVO(OAuth2AccessTokenDO token) {
        AppAuthLoginRespVO vo = BeanUtils.toBean(token, AppAuthLoginRespVO.class);
        AppUserDO user = userService.getById(token.getUserId());
        if (user != null) {
            vo.setRole(user.getRole());
        }
        return vo;
    }

    /**
     * 将字符串场景映射到 SmsSceneEnum 对应的 scene 值。
     * 路由优先级:+86 → 国内短信模板;其它号码按 locale zh-* → ZH(海外 ISMS 中文)> EN(海外 ISMS 英文)。
     */
    public static Integer mapSmsScene(String scene, String locale, String phone) {
        boolean mainland = isMainlandMobile(phone);
        boolean zh = locale != null && locale.toLowerCase().startsWith("zh");
        return switch (scene == null ? "" : scene) {
            case "register" -> (mainland
                    ? SmsSceneEnum.MEMBER_REGISTER_CN
                    : (zh ? SmsSceneEnum.MEMBER_REGISTER : SmsSceneEnum.MEMBER_REGISTER_EN)).getScene();
            case "reset" -> (mainland
                    ? SmsSceneEnum.MEMBER_RESET_PASSWORD_CN
                    : SmsSceneEnum.MEMBER_RESET_PASSWORD).getScene();
            case "bind" -> (mainland
                    ? SmsSceneEnum.MEMBER_UPDATE_MOBILE_CN
                    : SmsSceneEnum.MEMBER_UPDATE_MOBILE).getScene();
            default -> (mainland
                    ? SmsSceneEnum.MEMBER_LOGIN_CN
                    : (zh ? SmsSceneEnum.MEMBER_LOGIN : SmsSceneEnum.MEMBER_LOGIN_EN)).getScene();
        };
    }

}
