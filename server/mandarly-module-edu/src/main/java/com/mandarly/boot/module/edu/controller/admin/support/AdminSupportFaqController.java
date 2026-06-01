package com.mandarly.boot.module.edu.controller.admin.support;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportFaqPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportFaqRespVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportFaqSaveReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.support.FaqDO;
import com.mandarly.boot.module.edu.service.support.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 客服 FAQ")
@RestController
@RequestMapping("/edu/support/faq")
@Validated
public class AdminSupportFaqController {

    @Resource
    private SupportService supportService;

    @GetMapping("/page")
    @Operation(summary = "FAQ 分页")
    @PreAuthorize("@ss.hasPermission('edu:support-faq:query')")
    public CommonResult<PageResult<SupportFaqRespVO>> getPage(@Valid SupportFaqPageReqVO reqVO) {
        PageResult<FaqDO> page = supportService.getFaqPage(reqVO);
        return success(BeanUtils.toBean(page, SupportFaqRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "FAQ 详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('edu:support-faq:query')")
    public CommonResult<SupportFaqRespVO> get(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(supportService.getFaq(id), SupportFaqRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "新建 FAQ")
    @PreAuthorize("@ss.hasPermission('edu:support-faq:create')")
    public CommonResult<Long> create(@Valid @RequestBody SupportFaqSaveReqVO reqVO) {
        return success(supportService.createFaq(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 FAQ")
    @PreAuthorize("@ss.hasPermission('edu:support-faq:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SupportFaqSaveReqVO reqVO) {
        supportService.updateFaq(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 FAQ")
    @PreAuthorize("@ss.hasPermission('edu:support-faq:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        supportService.deleteFaq(id);
        return success(true);
    }
}
