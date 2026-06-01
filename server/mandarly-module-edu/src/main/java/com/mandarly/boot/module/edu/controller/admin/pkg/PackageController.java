package com.mandarly.boot.module.edu.controller.admin.pkg;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.pkg.vo.PackagePageReqVO;
import com.mandarly.boot.module.edu.controller.admin.pkg.vo.PackageRespVO;
import com.mandarly.boot.module.edu.controller.admin.pkg.vo.PackageSaveReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import com.mandarly.boot.module.edu.service.pkg.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 套餐定义")
@RestController
@RequestMapping("/edu/package")
@Validated
public class PackageController {

    @Resource
    private PackageService packageService;

    @GetMapping("/page")
    @Operation(summary = "套餐分页查询")
    @PreAuthorize("@ss.hasPermission('edu:package:query')")
    public CommonResult<PageResult<PackageRespVO>> getPackagePage(@Valid PackagePageReqVO reqVO) {
        PageResult<PackageDO> page = packageService.getPackagePage(reqVO);
        return success(BeanUtils.toBean(page, PackageRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取套餐详情")
    @Parameter(name = "id", description = "套餐 id", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('edu:package:query')")
    public CommonResult<PackageRespVO> getPackage(@RequestParam("id") Long id) {
        PackageDO entity = packageService.getPackage(id);
        return success(BeanUtils.toBean(entity, PackageRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建套餐")
    @PreAuthorize("@ss.hasPermission('edu:package:create')")
    public CommonResult<Long> createPackage(@Valid @RequestBody PackageSaveReqVO reqVO) {
        return success(packageService.createPackage(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新套餐")
    @PreAuthorize("@ss.hasPermission('edu:package:update')")
    public CommonResult<Boolean> updatePackage(@Valid @RequestBody PackageSaveReqVO reqVO) {
        packageService.updatePackage(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除套餐(逻辑删除)")
    @Parameter(name = "id", description = "套餐 id", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('edu:package:delete')")
    public CommonResult<Boolean> deletePackage(@RequestParam("id") Long id) {
        packageService.deletePackage(id);
        return success(true);
    }

}
