package com.mandarly.boot.module.edu.controller.admin.levelcheck;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckSubmissionPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckSubmissionRespVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckSubmissionStatsRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckSubmissionDO;
import com.mandarly.boot.module.edu.service.levelcheck.LevelCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 水平测试答卷数据")
@RestController
@RequestMapping("/edu/level-check/submission")
@Validated
public class LevelCheckSubmissionController {

    @Resource
    private LevelCheckService levelCheckService;

    @GetMapping("/page")
    @Operation(summary = "答卷分页(运营看转化漏斗)")
    @PreAuthorize("@ss.hasPermission('edu:level-check:query')")
    public CommonResult<PageResult<LevelCheckSubmissionRespVO>> getPage(@Valid LevelCheckSubmissionPageReqVO reqVO) {
        PageResult<LevelCheckSubmissionDO> page = levelCheckService.getSubmissionPage(reqVO);
        return success(BeanUtils.toBean(page, LevelCheckSubmissionRespVO.class));
    }

    @GetMapping("/stats")
    @Operation(summary = "答卷指标看板")
    @PreAuthorize("@ss.hasPermission('edu:level-check:query')")
    public CommonResult<LevelCheckSubmissionStatsRespVO> getStats() {
        return success(levelCheckService.getSubmissionStats());
    }

}
