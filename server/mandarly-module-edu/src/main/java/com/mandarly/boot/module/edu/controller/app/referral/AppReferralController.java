package com.mandarly.boot.module.edu.controller.app.referral;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.module.edu.controller.app.referral.vo.AppReferralStatsRespVO;
import com.mandarly.boot.module.edu.service.referral.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;
import static com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * App 端 - 推荐码接口
 *
 * <p>对应 PRD-v4 §5.4:学生查我的推荐码 + 推荐战绩展示。
 */
@Tag(name = "用户端 - 推荐码")
@RestController
@RequestMapping("/edu/referral")
@Validated
public class AppReferralController {

    @Resource
    private ReferralService referralService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询我的推荐战绩(推荐码 + 已推荐人数 + 累计奖励)")
    public CommonResult<AppReferralStatsRespVO> getMyStats() {
        Long userId = getLoginUserId();
        return success(referralService.getMyStats(userId));
    }
}
