package com.mandarly.boot.module.system.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "App 端 - 更新资料 Request VO")
@Data
public class AppUserUpdateReqVO {

    @Schema(description = "昵称")
    @Size(max = 50)
    private String nickname;

    @Schema(description = "头像 URL")
    @Size(max = 500)
    private String avatarUrl;

    @Schema(description = "语言")
    @Size(max = 16)
    private String locale;

    @Schema(description = "时区")
    @Size(max = 64)
    private String timezone;

    @Schema(description = "学习目标")
    @Size(max = 200)
    private String learningGoal;
}
