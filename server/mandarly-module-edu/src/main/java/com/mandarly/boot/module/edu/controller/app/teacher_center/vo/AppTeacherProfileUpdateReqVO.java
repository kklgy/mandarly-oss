package com.mandarly.boot.module.edu.controller.app.teacher_center.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户端 - 教师中心:教师本人更新自我档案 Request VO(D19 Phase B / B3)
 *
 * <p>语义:
 * <ul>
 *   <li>partial update — null 字段不动,允许只改 intro 不动 expertise</li>
 *   <li>不修改 audit_status — 即使当前 approved,改字段也不会自动转 pending
 *       (改重要字段触发回审是二期;一期需要回审让教师走 submitForAudit 入口)</li>
 * </ul>
 */
@Schema(description = "用户端 - 教师中心 - 教师本人更新自我档案 Request VO")
@Data
public class AppTeacherProfileUpdateReqVO {

    @Schema(description = "文字自我介绍", example = "我是来自香港的中文老师...")
    @Size(max = 2000, message = "intro 长度不能超过 2000")
    private String intro;

    @Schema(description = "教学方向数组", example = "[\"business\", \"kids\"]")
    private List<String> expertise;

    @Schema(description = "口音 mainland / taiwan / hk / mixed", example = "hk")
    @Size(max = 32, message = "accent 长度不能超过 32")
    private String accent;

    @Schema(description = "教师会的语言列表", example = "[\"zh\", \"en\"]")
    private List<String> languages;

    @Schema(description = "教学年限", example = "5")
    @Min(value = 0, message = "yearsExperience 不能为负")
    @Max(value = 99, message = "yearsExperience 不合理")
    private Integer yearsExperience;

    @Schema(description = "教师自我介绍视频(COS 路径,选填)", example = "teacher/intro/123.mp4")
    @Size(max = 512, message = "introVideoUrl 长度不能超过 512")
    private String introVideoUrl;
}
