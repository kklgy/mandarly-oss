package com.mandarly.boot.module.system.enums.sms;

import cn.hutool.core.util.ArrayUtil;
import com.mandarly.boot.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用户短信验证码发送场景的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum SmsSceneEnum implements ArrayValuable<Integer> {

    MEMBER_LOGIN(1, "user-sms-login", "会员用户 - 手机号登陆"),
    MEMBER_UPDATE_MOBILE(2, "user-update-mobile", "会员用户 - 修改手机"),
    MEMBER_UPDATE_PASSWORD(3, "user-update-password", "会员用户 - 修改密码"),
    MEMBER_RESET_PASSWORD(4, "user-reset-password", "会员用户 - 忘记密码"),
    MEMBER_REGISTER(5, "user-sms-register", "会员用户 - 手机号注册"),
    MEMBER_LOGIN_EN(11, "user-sms-login-en", "会员用户 - 手机号登陆 EN"),
    MEMBER_REGISTER_EN(15, "user-sms-register-en", "会员用户 - 手机号注册 EN"),
    MEMBER_LOGIN_CN(31, "user-sms-login-cn", "会员用户 - 手机号登陆 CN(中国大陆国内通道)"),
    MEMBER_UPDATE_MOBILE_CN(32, "user-update-mobile-cn", "会员用户 - 修改手机 CN(中国大陆国内通道)"),
    MEMBER_RESET_PASSWORD_CN(34, "user-reset-password-cn", "会员用户 - 忘记密码 CN(中国大陆国内通道)"),
    MEMBER_REGISTER_CN(35, "user-sms-register-cn", "会员用户 - 手机号注册 CN(中国大陆国内通道)"),

    ADMIN_MEMBER_LOGIN(21, "admin-sms-login", "后台用户 - 手机号登录"),
    ADMIN_MEMBER_REGISTER(22, "admin-sms-register", "后台用户 - 手机号注册"),
    ADMIN_MEMBER_RESET_PASSWORD(23, "admin-reset-password", "后台用户 - 忘记密码");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SmsSceneEnum::getScene).toArray(Integer[]::new);

    /**
     * 验证场景的编号
     */
    private final Integer scene;
    /**
     * 模版编码
     */
    private final String templateCode;
    /**
     * 描述
     */
    private final String description;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static SmsSceneEnum getCodeByScene(Integer scene) {
        return ArrayUtil.firstMatch(sceneEnum -> sceneEnum.getScene().equals(scene),
                values());
    }

}
