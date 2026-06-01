package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户端 - 教师中心:教师本人上传一份资质材料 Request VO(D19 Phase B / B5)。
 *
 * <p>前端先调 {@code /admin-api/infra/file/upload} 把文件传到 COS 拿 docUrl,
 * 再调本接口落 DB 记录;不在本接口直接处理 multipart。
 *
 * <p>docType 取值对齐
 * {@link com.mandarly.boot.module.edu.enums.teacher.TeacherQualificationDocTypeEnum}
 * ({@code id_card / passport / degree_cert / teaching_cert / english_cert / experience_proof}),
 * 用正则白名单防御非法值。
 */
@Schema(description = "用户端 - 教师中心 - 教师本人上传资质材料 Request VO")
@Data
public class AppTeacherQualificationReqVO {

    @Schema(description = "文件类型", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"id_card", "passport", "degree_cert", "teaching_cert", "english_cert", "experience_proof"},
            example = "degree_cert")
    @NotBlank(message = "文件类型不能为空")
    @Pattern(regexp = "^(id_card|passport|degree_cert|teaching_cert|english_cert|experience_proof)$",
            message = "文件类型必须为 id_card / passport / degree_cert / teaching_cert / english_cert / experience_proof")
    private String docType;

    @Schema(description = "COS 完整访问 URL(由 /infra/file/upload 返回)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://mandarly-private.cos.ap-hongkong.myqcloud.com/teacher/qual/123.pdf")
    @NotBlank(message = "文件 URL 不能为空")
    @Size(max = 512, message = "docUrl 长度不能超过 512")
    private String docUrl;

    @Schema(description = "原始文件名(展示用)", example = "teaching-cert.pdf")
    @Size(max = 128, message = "docFilename 长度不能超过 128")
    private String docFilename;

}
