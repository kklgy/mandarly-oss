package com.mandarly.boot.module.edu.controller.admin.teacher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 教师档案 Response VO")
@Data
public class TeacherProfileRespVO {

    @Schema(description = "教师 user.id", example = "1024")
    private Long userId;

    @Schema(description = "教师手机号(E.164)", example = "+85298765432")
    private String phone;

    @Schema(description = "自我介绍")
    private String intro;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "审核时间")
    private LocalDateTime auditedAt;

    @Schema(description = "审核管理员 id")
    private Long auditedBy;

    @Schema(description = "教师等级(一期 NULL,二期预留 Level1/2/3)")
    private String level;

    @Schema(description = "教学方向数组")
    private List<String> expertise;

    @Schema(description = "口音")
    private String accent;

    @Schema(description = "会的语言列表")
    private List<String> languages;

    @Schema(description = "教学年限")
    private Integer yearsExperience;

    @Schema(description = "自我介绍视频 URL")
    private String introVideoUrl;

    @Schema(description = "视频文件大小 byte")
    private Long introVideoSize;

    @Schema(description = "视频上传时间")
    private LocalDateTime introVideoUploadedAt;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;
}
