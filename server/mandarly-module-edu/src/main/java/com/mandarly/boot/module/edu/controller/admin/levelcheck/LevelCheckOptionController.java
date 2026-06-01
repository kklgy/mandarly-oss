package com.mandarly.boot.module.edu.controller.admin.levelcheck;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckOptionRespVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckOptionSaveReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckOptionDO;
import com.mandarly.boot.module.edu.service.levelcheck.LevelCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 水平测试选项")
@RestController
@RequestMapping("/edu/level-check/option")
@Validated
public class LevelCheckOptionController {

    @Resource
    private LevelCheckService levelCheckService;

    @GetMapping("/list")
    @Operation(summary = "按题目列出选项")
    @PreAuthorize("@ss.hasPermission('edu:level-check:query')")
    public CommonResult<List<LevelCheckOptionRespVO>> list(@RequestParam("questionId") Long questionId) {
        List<LevelCheckOptionDO> list = levelCheckService.listOptionsByQuestion(questionId);
        return success(BeanUtils.toBean(list, LevelCheckOptionRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "新建选项")
    @PreAuthorize("@ss.hasPermission('edu:level-check:create')")
    public CommonResult<Long> create(@Valid @RequestBody LevelCheckOptionSaveReqVO reqVO) {
        return success(levelCheckService.createOption(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新选项")
    @PreAuthorize("@ss.hasPermission('edu:level-check:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody LevelCheckOptionSaveReqVO reqVO) {
        levelCheckService.updateOption(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除选项")
    @PreAuthorize("@ss.hasPermission('edu:level-check:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        levelCheckService.deleteOption(id);
        return success(true);
    }

}
