package com.mandarly.boot.module.edu.controller.admin.teacher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 教师资质材料 Response VO(D19 Phase B / B5)。
 *
 * <p>对应 GET /admin-api/edu/teacher-qualification/list?userId=...
 * 让 admin 在教师审核详情页查看教师的全部资质材料并在线预览证件。
 *
 * <p><strong>docUrl 字段已替换为 15 分钟 TTL 的预签名 URL</strong>(
 * {@link com.mandarly.boot.module.edu.service.teacher.TeacherQualificationService#generatePresignedUrl(String)}),
 * admin 点击直接在浏览器加载;无需在 nginx / oss 层做权限。
 */
@Schema(description = "管理后台 - 教师资质材料 Response VO")
@Data
public class AdminTeacherQualificationRespVO {

    @Schema(description = "资质记录主键", example = "1")
    private Long id;

    @Schema(description = "教师 user.id", example = "1024")
    private Long userId;

    @Schema(description = "文件类型 id_card / passport / teaching_cert / experience_proof",
            example = "teaching_cert")
    private String docType;

    @Schema(description = "预签名 URL(15min TTL,admin 在线预览证件)",
            example = "https://mandarly-private.cos.ap-hongkong.myqcloud.com/teacher/qual/123.pdf?sign=...")
    private String docUrl;

    @Schema(description = "原始文件名", example = "teaching-cert.pdf")
    private String docFilename;

    @Schema(description = "审核状态 pending / approved / rejected", example = "pending")
    private String auditStatus;

    @Schema(description = "驳回原因(audit_status=rejected 时填充)", example = "证件照不清晰,请重新上传")
    private String rejectReason;

    @Schema(description = "最近一次审核时间")
    private LocalDateTime auditedAt;

    @Schema(description = "审核管理员 system_users.id", example = "1")
    private Long auditedBy;

    @Schema(description = "上传时间")
    private LocalDateTime createTime;

}
