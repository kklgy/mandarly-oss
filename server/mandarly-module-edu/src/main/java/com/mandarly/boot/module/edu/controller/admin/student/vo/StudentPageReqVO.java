package com.mandarly.boot.module.edu.controller.admin.student.vo;

import com.mandarly.boot.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 学生分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentPageReqVO extends PageParam {

    @Schema(description = "昵称模糊", example = "Tom")
    private String nickname;

    @Schema(description = "邮箱模糊", example = "@mandarly.test")
    private String email;

    @Schema(description = "手机号模糊(E.164)", example = "+85291")
    private String phone;

    @Schema(description = "状态:active / pending_verification / frozen", example = "active")
    private String status;

    @Schema(description = "语言偏好:en/zh-CN/zh-TW/ar", example = "en")
    private String locale;
}
