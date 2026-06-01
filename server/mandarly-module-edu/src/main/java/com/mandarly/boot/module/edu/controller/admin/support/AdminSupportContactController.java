package com.mandarly.boot.module.edu.controller.admin.support;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportContactPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportContactRespVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportContactSaveReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.support.SupportContactDO;
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

@Tag(name = "管理后台 - 客服联系方式")
@RestController
@RequestMapping("/edu/support/contact")
@Validated
public class AdminSupportContactController {

    @Resource
    private SupportService supportService;

    @GetMapping("/page")
    @Operation(summary = "客服联系方式分页")
    @PreAuthorize("@ss.hasPermission('edu:support-contact:query')")
    public CommonResult<PageResult<SupportContactRespVO>> getPage(@Valid SupportContactPageReqVO reqVO) {
        PageResult<SupportContactDO> page = supportService.getContactPage(reqVO);
        return success(BeanUtils.toBean(page, SupportContactRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "客服联系方式详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('edu:support-contact:query')")
    public CommonResult<SupportContactRespVO> get(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(supportService.getContact(id), SupportContactRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "新建客服联系方式")
    @PreAuthorize("@ss.hasPermission('edu:support-contact:create')")
    public CommonResult<Long> create(@Valid @RequestBody SupportContactSaveReqVO reqVO) {
        return success(supportService.createContact(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客服联系方式")
    @PreAuthorize("@ss.hasPermission('edu:support-contact:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SupportContactSaveReqVO reqVO) {
        supportService.updateContact(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客服联系方式")
    @PreAuthorize("@ss.hasPermission('edu:support-contact:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        supportService.deleteContact(id);
        return success(true);
    }
}
