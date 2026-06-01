package com.mandarly.boot.module.edu.controller.admin.student.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 学生详情 Response VO")
@Data
public class StudentRespVO {

    @Schema(description = "学生 user.id", example = "2001")
    private Long id;

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

    @Schema(description = "语言偏好")
    private String locale;

    @Schema(description = "时区")
    private String timezone;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "推荐码(自己的)")
    private String referralCode;

    @Schema(description = "被谁推荐 user.id")
    private Long referredBy;

    @Schema(description = "学习目标")
    private String learningGoal;

    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "最近登录 IP")
    private String lastLoginIp;

    @Schema(description = "注册时间")
    private LocalDateTime createTime;
}
