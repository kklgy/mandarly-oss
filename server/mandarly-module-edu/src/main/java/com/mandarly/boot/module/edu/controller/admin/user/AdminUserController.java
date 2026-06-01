package com.mandarly.boot.module.edu.controller.admin.user;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.admin.user.vo.AdminUserFreezeReqVO;
import com.mandarly.boot.module.edu.controller.admin.user.vo.AdminUserPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.user.vo.AdminUserRespVO;
import com.mandarly.boot.module.edu.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - C 端用户")
@RestController
@RequestMapping("/edu/user")
@Validated
public class AdminUserController {

    @Resource(name = "eduAdminUserService")
    private AdminUserService adminUserService;

    @GetMapping("/page")
    @Operation(summary = "C 端用户分页查询")
    @PreAuthorize("@ss.hasPermission('edu:user:query')")
    public CommonResult<PageResult<AdminUserRespVO>> getUserPage(@Valid AdminUserPageReqVO reqVO) {
        return success(adminUserService.getUserPage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获取 C 端用户详情")
    @Parameter(name = "userId", description = "C 端 user.id", required = true, example = "2001")
    @PreAuthorize("@ss.hasPermission('edu:user:query')")
    public CommonResult<AdminUserRespVO> getUser(@RequestParam("userId") Long userId) {
        return success(adminUserService.getUser(userId));
    }

    @PutMapping("/freeze")
    @Operation(summary = "冻结 / 解冻 C 端用户(action=freeze/unfreeze)")
    @PreAuthorize("@ss.hasPermission('edu:user:freeze')")
    public CommonResult<Boolean> freezeUser(@Valid @RequestBody AdminUserFreezeReqVO reqVO) {
        Long operatorAdminId = SecurityFrameworkUtils.getLoginUserId();
        adminUserService.freezeUser(reqVO, operatorAdminId);
        return success(true);
    }

}
