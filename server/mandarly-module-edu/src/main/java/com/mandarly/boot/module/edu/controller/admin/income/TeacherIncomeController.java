package com.mandarly.boot.module.edu.controller.admin.income;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.income.vo.TeacherBalanceRespVO;
import com.mandarly.boot.module.edu.controller.admin.income.vo.TeacherIncomePageReqVO;
import com.mandarly.boot.module.edu.controller.admin.income.vo.TeacherIncomeRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherBalanceDO;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherBalanceMapper;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherIncomeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 教师收入看板(只读)
 *
 * <p>对应 PRD-v4 §6.2 admin 教师收入结算看板。
 * Track 6C: 收入流水(JOIN 多表) + 余额详情。
 */
@Tag(name = "管理后台 - 教师收入")
@RestController
@RequestMapping("/edu/teacher-income")
@Validated
public class TeacherIncomeController {

    @Resource
    private TeacherIncomeMapper teacherIncomeMapper;

    @Resource
    private TeacherBalanceMapper teacherBalanceMapper;

    @GetMapping("/page")
    @Operation(summary = "教师收入流水分页(含 JOIN system_users/course_order/package)")
    @PreAuthorize("@ss.hasPermission('edu:teacher-income:query')")
    public CommonResult<PageResult<TeacherIncomeRespVO>> getPage(@Valid TeacherIncomePageReqVO reqVO) {
        PageResult<TeacherIncomeRespVO> page = teacherIncomeMapper.selectIncomePageWithJoin(reqVO);
        return success(page);
    }

    @GetMapping("/balance/{teacherId}")
    @Operation(summary = "查询单教师余额详情(frozen / available / totalEarned / totalWithdrawn)")
    @Parameter(name = "teacherId", description = "教师 user.id", required = true, example = "2048")
    @PreAuthorize("@ss.hasPermission('edu:teacher-income:query')")
    public CommonResult<TeacherBalanceRespVO> getBalance(@PathVariable("teacherId") Long teacherId) {
        TeacherBalanceDO balance = teacherBalanceMapper.selectById(teacherId);
        return success(BeanUtils.toBean(balance, TeacherBalanceRespVO.class));
    }
}
