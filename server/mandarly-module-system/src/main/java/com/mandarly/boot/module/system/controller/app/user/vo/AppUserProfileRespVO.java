package com.mandarly.boot.module.system.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "App 端 - 用户资料 Response VO")
@Data
public class AppUserProfileRespVO {

    @Schema(description = "用户 ID")
    private Long id;

    @Schema(description = "角色", example = "student")
    private String role;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "邮箱验证时间")
    private LocalDateTime emailVerifiedAt;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "手机号验证时间")
    private LocalDateTime phoneVerifiedAt;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像 URL")
    private String avatarUrl;

    @Schema(description = "语言", example = "zh-HK")
    private String locale;

    @Schema(description = "时区", example = "Asia/Hong_Kong")
    private String timezone;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "推荐码")
    private String referralCode;

    @Schema(description = "学习目标")
    private String learningGoal;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "三方绑定列表")
    private List<AppOAuthBindingItemVO> oauthBindings;

    @Schema(description = "教师审核状态(role=teacher 才有,draft/pending/approved/rejected)")
    private String teacherAuditStatus;

    @Schema(description = "教师驳回原因(audit_status=rejected 时返回)")
    private String teacherRejectReason;
}
