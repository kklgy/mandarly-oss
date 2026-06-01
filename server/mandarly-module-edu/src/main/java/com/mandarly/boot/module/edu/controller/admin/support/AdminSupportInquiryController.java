package com.mandarly.boot.module.edu.controller.admin.support;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportInquiryPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportInquiryRespVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportInquiryStatsRespVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportTopUnmatchedRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.support.SupportInquiryLogDO;
import com.mandarly.boot.module.edu.service.support.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 客服咨询日志")
@RestController
@RequestMapping("/edu/support/inquiry")
@Validated
public class AdminSupportInquiryController {

    @Resource
    private SupportService supportService;

    @GetMapping("/page")
    @Operation(summary = "咨询日志分页")
    @PreAuthorize("@ss.hasPermission('edu:support-inquiry:query')")
    public CommonResult<PageResult<SupportInquiryRespVO>> getPage(@Valid SupportInquiryPageReqVO reqVO) {
        PageResult<SupportInquiryLogDO> page = supportService.getInquiryPage(reqVO);
        return success(BeanUtils.toBean(page, SupportInquiryRespVO.class));
    }

    @GetMapping("/stats")
    @Operation(summary = "咨询日志指标")
    @PreAuthorize("@ss.hasPermission('edu:support-inquiry:query')")
    public CommonResult<SupportInquiryStatsRespVO> getStats() {
        return success(supportService.getInquiryStats());
    }

    @GetMapping("/top-unmatched")
    @Operation(summary = "未命中问题 Top N")
    @PreAuthorize("@ss.hasPermission('edu:support-inquiry:query')")
    public CommonResult<List<SupportTopUnmatchedRespVO>> getTopUnmatched(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return success(supportService.getTopUnmatched(limit));
    }
}
