package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端 - 教师中心:教师本人档案 Response VO(D19 Phase B / B4)。
 *
 * <p>对应 GET /edu/teacher-center/profile/me — 一次返档案字段 + 审核状态 + 资质数量,
 * 让前端 ProfileEditView / AuditStatusBanner 一次组件刷新即可拿全。
 *
 * <p>字段语义:
 * <ul>
 *   <li>{@code userId} 始终回填(从 Security 上下文取),即使 profile 行不存在也兜底
 *       (A2 注册路径已容错建 draft profile,profile=null 是 defensive 兜底)</li>
 *   <li>{@code auditStatus} 为 null 时前端展示"档案待初始化"提示</li>
 *   <li>{@code rejectReason} 仅在 auditStatus=rejected 时有值</li>
 *   <li>{@code qualificationCount} 由 {@link com.mandarly.boot.module.edu.service.teacher.TeacherQualificationService#listByUserId} 统计</li>
 * </ul>
 */
@Schema(description = "用户端 - 教师中心 - 教师本人档案(含审核状态 + 资质 summary)Response VO")
@Data
public class AppTeacherProfileMeRespVO {

    @Schema(description = "教师 user.id", example = "1024")
    private Long userId;

    @Schema(description = "文字自我介绍", example = "我是来自香港的中文老师...")
    private String intro;

    @Schema(description = "教学方向数组", example = "[\"business\", \"kids\"]")
    private List<String> expertise;

    @Schema(description = "口音 mainland / taiwan / hk / mixed", example = "hk")
    private String accent;

    @Schema(description = "教师会的语言列表", example = "[\"zh\", \"en\"]")
    private List<String> languages;

    @Schema(description = "教学年限", example = "5")
    private Integer yearsExperience;

    @Schema(description = "教师自我介绍视频(COS 路径,选填)", example = "teacher/intro/123.mp4")
    private String introVideoUrl;

    @Schema(description = "审核状态 draft / pending / approved / rejected", example = "draft")
    private String auditStatus;

    @Schema(description = "驳回原因(audit_status=rejected 时填充)", example = "证件照不清晰,请重新上传")
    private String rejectReason;

    @Schema(description = "最近一次审核时间")
    private LocalDateTime auditedAt;

    @Schema(description = "已上传资质材料数量", example = "3")
    private Integer qualificationCount;

}
