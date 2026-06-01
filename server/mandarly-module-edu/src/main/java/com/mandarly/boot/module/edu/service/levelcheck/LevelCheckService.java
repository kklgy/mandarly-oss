package com.mandarly.boot.module.edu.service.levelcheck;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckOptionSaveReqVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckQuestionPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckQuestionRespVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckQuestionSaveReqVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckSubmissionPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckSubmissionStatsRespVO;
import com.mandarly.boot.module.edu.controller.app.levelcheck.vo.AppLevelCheckResultRespVO;
import com.mandarly.boot.module.edu.controller.app.levelcheck.vo.AppLevelCheckSubmitReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckOptionDO;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckQuestionDO;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckSubmissionDO;

import java.util.List;

/**
 * 水平测试 Service
 *
 * <p>对应 docs/product/level-check-recommendation-v1.md
 */
public interface LevelCheckService {

    // ========== 题库(admin + app 共用查询)==========

    /**
     * App 端 - 取活跃题库 + 选项嵌套(按 sort)
     */
    List<LevelCheckQuestionRespVO> listActiveQuestionsWithOptions();

    PageResult<LevelCheckQuestionDO> getQuestionPage(LevelCheckQuestionPageReqVO reqVO);

    LevelCheckQuestionRespVO getQuestionWithOptions(Long id);

    Long createQuestion(LevelCheckQuestionSaveReqVO reqVO);

    void updateQuestion(LevelCheckQuestionSaveReqVO reqVO);

    void deleteQuestion(Long id);

    // ========== 选项 ==========

    List<LevelCheckOptionDO> listOptionsByQuestion(Long questionId);

    Long createOption(LevelCheckOptionSaveReqVO reqVO);

    void updateOption(LevelCheckOptionSaveReqVO reqVO);

    void deleteOption(Long id);

    // ========== 答卷与推荐 ==========

    /**
     * App 端 - 提交答卷,触发推荐算法,落库 + 返回结果
     */
    AppLevelCheckResultRespVO submitAnswers(AppLevelCheckSubmitReqVO reqVO, Long userIdIfLogin);

    /**
     * App 端 - 按 submissionId 查历史结果
     */
    AppLevelCheckResultRespVO getResult(Long submissionId);

    PageResult<LevelCheckSubmissionDO> getSubmissionPage(LevelCheckSubmissionPageReqVO reqVO);

    LevelCheckSubmissionStatsRespVO getSubmissionStats();

}
