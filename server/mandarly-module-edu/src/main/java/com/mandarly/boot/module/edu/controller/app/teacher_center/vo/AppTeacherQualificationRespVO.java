package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户端 - 教师中心:教师本人资质材料列表项 Response VO(D19 Phase B / B5)。
 *
 * <p><strong>故意不暴露 docUrl</strong>:私有 bucket 教师本人浏览自己的资质也需要
 * 签名 URL,但前端 QualificationView 设计上只需展示文件名 + 审核状态 + 删除按钮,
 * 不做在线预览。简化以避免私有 bucket 签名 URL 复杂度,也降低 URL 误泄风险。
 *
 * <p>如后续需要教师本人复看证件,新增 {@code GET .../qualification/{id}/preview}
 * 走签名 URL 即可,本列表 VO 保持极简。
 */
@Schema(description = "用户端 - 教师中心 - 教师本人资质材料列表项 Response VO")
@Data
public class AppTeacherQualificationRespVO {

    @Schema(description = "资质记录主键", example = "1")
    private Long id;

    @Schema(description = "文件类型 id_card / passport / teaching_cert / experience_proof",
            example = "teaching_cert")
    private String docType;

    @Schema(description = "原始文件名(教师识别用)", example = "teaching-cert.pdf")
    private String docFilename;

    @Schema(description = "审核状态 pending / approved / rejected", example = "pending")
    private String auditStatus;

    @Schema(description = "驳回原因(audit_status=rejected 时填充)", example = "证件照不清晰,请重新上传")
    private String rejectReason;

    @Schema(description = "上传时间")
    private LocalDateTime createTime;

}
