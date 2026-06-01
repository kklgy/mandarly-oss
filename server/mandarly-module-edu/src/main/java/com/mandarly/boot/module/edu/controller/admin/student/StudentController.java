package com.mandarly.boot.module.edu.controller.admin.student;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.admin.student.vo.StudentFreezeReqVO;
import com.mandarly.boot.module.edu.controller.admin.student.vo.StudentPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.student.vo.StudentRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.service.student.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 学生")
@RestController
@RequestMapping("/edu/student")
@Validated
public class StudentController {

    @Resource
    private StudentService studentService;

    @GetMapping("/page")
    @Operation(summary = "学生分页查询")
    @PreAuthorize("@ss.hasPermission('edu:student:query')")
    public CommonResult<PageResult<StudentRespVO>> getStudentPage(@Valid StudentPageReqVO reqVO) {
        PageResult<UserDO> page = studentService.getStudentPage(reqVO);
        return success(BeanUtils.toBean(page, StudentRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取学生详情")
    @Parameter(name = "userId", description = "学生 user.id", required = true, example = "2001")
    @PreAuthorize("@ss.hasPermission('edu:student:query')")
    public CommonResult<StudentRespVO> getStudent(@RequestParam("userId") Long userId) {
        UserDO student = studentService.getStudent(userId);
        return success(BeanUtils.toBean(student, StudentRespVO.class));
    }

    @PutMapping("/freeze")
    @Operation(summary = "冻结 / 解冻学生(action=freeze/unfreeze)")
    @PreAuthorize("@ss.hasPermission('edu:student:freeze')")
    public CommonResult<Boolean> freezeStudent(@Valid @RequestBody StudentFreezeReqVO reqVO) {
        Long operatorAdminId = SecurityFrameworkUtils.getLoginUserId();
        studentService.freezeStudent(reqVO, operatorAdminId);
        return success(true);
    }
}
