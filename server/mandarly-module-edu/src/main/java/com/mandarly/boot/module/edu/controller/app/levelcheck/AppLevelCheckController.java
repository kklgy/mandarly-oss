package com.mandarly.boot.module.edu.controller.app.levelcheck;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckQuestionRespVO;
import com.mandarly.boot.module.edu.controller.app.levelcheck.vo.AppLevelCheckResultRespVO;
import com.mandarly.boot.module.edu.controller.app.levelcheck.vo.AppLevelCheckSubmitReqVO;
import com.mandarly.boot.module.edu.service.levelcheck.LevelCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * App 端 - 水平测试公开接口(无需登录,@PermitAll)
 *
 * <p>详见 docs/product/level-check-recommendation-v1.md §六
 */
@Tag(name = "用户端 - 水平测试(公开)")
@RestController
@RequestMapping("/edu/level-check")
@Validated
public class AppLevelCheckController {

    @Resource
    private LevelCheckService levelCheckService;

    @GetMapping("/questions")
    @PermitAll
    @Operation(summary = "取活跃题库(题目 + 选项嵌套,按 sort)")
    public CommonResult<List<LevelCheckQuestionRespVO>> listQuestions() {
        return success(levelCheckService.listActiveQuestionsWithOptions());
    }

    @PostMapping("/submit")
    @PermitAll
    @Operation(summary = "提交答卷,返回推断等级 + 推荐教师 + 推荐套餐")
    public CommonResult<AppLevelCheckResultRespVO> submit(@Valid @RequestBody AppLevelCheckSubmitReqVO reqVO) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        return success(maskIfAnonymous(levelCheckService.submitAnswers(reqVO, loginUserId), loginUserId));
    }

    @GetMapping("/result")
    @PermitAll
    @Operation(summary = "按 submissionId 查历史推荐结果(用户重看 / 邮件回链)")
    public CommonResult<AppLevelCheckResultRespVO> getResult(@RequestParam("submissionId") Long submissionId) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        return success(maskIfAnonymous(levelCheckService.getResult(submissionId), loginUserId));
    }

    private AppLevelCheckResultRespVO maskIfAnonymous(AppLevelCheckResultRespVO result, Long loginUserId) {
        if (loginUserId != null || result == null) {
            return result;
        }
        result.setRecommendedTeachers(Collections.emptyList());
        result.setRecommendedPackage(null);
        return result;
    }

}
