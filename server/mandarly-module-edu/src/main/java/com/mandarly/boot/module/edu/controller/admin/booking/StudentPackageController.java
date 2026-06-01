package com.mandarly.boot.module.edu.controller.admin.booking;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.StudentPackageGrantReqVO;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.StudentPackagePageReqVO;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.StudentPackageRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.StudentPackageDO;
import com.mandarly.boot.module.edu.service.booking.StudentPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 学生持有套餐")
@RestController
@RequestMapping("/edu/student-package")
@Validated
public class StudentPackageController {

    @Resource
    private StudentPackageService studentPackageService;

    @GetMapping("/page")
    @Operation(summary = "学生持有套餐分页")
    @PreAuthorize("@ss.hasPermission('edu:student-package:query')")
    public CommonResult<PageResult<StudentPackageRespVO>> getPage(@Valid StudentPackagePageReqVO reqVO) {
        PageResult<StudentPackageDO> page = studentPackageService.getStudentPackagePage(reqVO);
        return success(BeanUtils.toBean(page, StudentPackageRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取学生持有套餐详情")
    @Parameter(name = "id", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('edu:student-package:query')")
    public CommonResult<StudentPackageRespVO> get(@RequestParam("id") Long id) {
        StudentPackageDO sp = studentPackageService.getStudentPackage(id);
        return success(BeanUtils.toBean(sp, StudentPackageRespVO.class));
    }

    @GetMapping("/list-active-by-student")
    @Operation(summary = "列出学生当前可用套餐(remaining>0 且未过期)")
    @Parameter(name = "studentId", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:student-package:query')")
    public CommonResult<List<StudentPackageRespVO>> listActive(@RequestParam("studentId") Long studentId) {
        List<StudentPackageDO> list = studentPackageService.listActiveByStudent(studentId);
        return success(BeanUtils.toBean(list, StudentPackageRespVO.class));
    }

    @PostMapping("/grant")
    @Operation(summary = "管理员手动发放套餐(source=admin_grant)")
    @PreAuthorize("@ss.hasPermission('edu:student-package:grant')")
    public CommonResult<Long> grant(@Valid @RequestBody StudentPackageGrantReqVO reqVO) {
        Long operator = SecurityFrameworkUtils.getLoginUserId();
        return success(studentPackageService.grantPackage(reqVO, operator));
    }

}
