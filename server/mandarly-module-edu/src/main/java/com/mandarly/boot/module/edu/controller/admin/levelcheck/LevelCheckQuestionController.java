package com.mandarly.boot.module.edu.controller.admin.levelcheck;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckQuestionPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckQuestionRespVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckQuestionSaveReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckQuestionDO;
import com.mandarly.boot.module.edu.service.levelcheck.LevelCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 水平测试题目")
@RestController
@RequestMapping("/edu/level-check/question")
@Validated
public class LevelCheckQuestionController {

    @Resource
    private LevelCheckService levelCheckService;

    @GetMapping("/page")
    @Operation(summary = "题目分页")
    @PreAuthorize("@ss.hasPermission('edu:level-check:query')")
    public CommonResult<PageResult<LevelCheckQuestionRespVO>> getPage(@Valid LevelCheckQuestionPageReqVO reqVO) {
        PageResult<LevelCheckQuestionDO> page = levelCheckService.getQuestionPage(reqVO);
        return success(BeanUtils.toBean(page, LevelCheckQuestionRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取题目详情(含选项)")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('edu:level-check:query')")
    public CommonResult<LevelCheckQuestionRespVO> get(@RequestParam("id") Long id) {
        return success(levelCheckService.getQuestionWithOptions(id));
    }

    @PostMapping("/create")
    @Operation(summary = "新建题目")
    @PreAuthorize("@ss.hasPermission('edu:level-check:create')")
    public CommonResult<Long> create(@Valid @RequestBody LevelCheckQuestionSaveReqVO reqVO) {
        return success(levelCheckService.createQuestion(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新题目")
    @PreAuthorize("@ss.hasPermission('edu:level-check:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody LevelCheckQuestionSaveReqVO reqVO) {
        levelCheckService.updateQuestion(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除题目")
    @PreAuthorize("@ss.hasPermission('edu:level-check:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        levelCheckService.deleteQuestion(id);
        return success(true);
    }

}
