package com.mandarly.boot.module.edu.service.review;

import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.dal.dataobject.review.ReviewDO;

import java.util.List;
import java.util.Map;

/**
 * 课后评价 Service(M3 主线 / PRD §S7 + §T6 / Wave 5 第 11 轮 ReviewFormView 步骤化)。
 *
 * <p>口径(2026-05-10 第 11 轮设计稿调整):
 * <ul>
 *   <li>仅 course_order.status=finished 才能评(no_show 都不能评)</li>
 *   <li>修改窗口 72h(原 24h 升级),从首次 create_time 起算(每次 update 不重置窗口)</li>
 *   <li>预设 tag 上限 5 个(原 3 个),白名单与 platform_config.review.tag_dict 同步</li>
 *   <li>自定义 tag 上限 3 条 × 8 字符,存 review.custom_tags JSON</li>
 *   <li>匿名评价 review.is_anonymous,公开列表脱敏到"匿名学员"</li>
 *   <li>is_visible 默认 1;admin 端隐藏功能留运营压力上来再做</li>
 * </ul>
 */
public interface ReviewService {

    /**
     * 学生首次写评价。
     *
     * @param orderId    课程订单 id
     * @param studentId  学生 user.id(SecurityFrameworkUtils.getLoginUserId)
     * @param rating     1-5
     * @param content    评价正文,可空
     * @param tags       预设标签数组,白名单内 0-5 个
     * @param isAnonymous 匿名评价(可空,默认 false)
     * @param customTags 用户自定义标签,0-3 条 × 8 字符,可空
     * @return 评价主键(= orderId)
     */
    Long saveReview(Long orderId, Long studentId, Integer rating, String content,
                    List<String> tags, Boolean isAnonymous, List<String> customTags);

    /**
     * 学生在 72h 内修改评价。校验:本人 + 在窗口内。
     */
    void updateReview(Long orderId, Long studentId, Integer rating, String content,
                      List<String> tags, Boolean isAnonymous, List<String> customTags);

    /**
     * 取预设标签字典(GET /app-api/edu/review/tags)
     * 一期返 ReviewServiceImpl#TAG_WHITELIST 静态列表,二期改读 platform_config['review.tag_dict']
     *
     * @return tag code 列表(前端 i18n 解 review.tag.{code})
     */
    List<String> getPresetTagCodes();

    /**
     * 取一条评价。返回 null 表示该订单还没评。
     */
    ReviewDO getReviewByOrder(Long orderId);

    /**
     * 教师视角:我收到的评价分页(只看 is_visible=1)。
     */
    PageResult<ReviewDO> getReviewsByTeacher(Long teacherId, PageParam pageParam);

    /**
     * 学生视角:我写过的评价分页。
     */
    PageResult<ReviewDO> getReviewsByStudent(Long studentId, PageParam pageParam);

    /**
     * 教师评分聚合 + 完成订单数。
     *
     * @return Map: avgRating(BigDecimal), reviewCount(Long), finishedOrderCount(Long)
     */
    Map<String, Object> getTeacherStat(Long teacherId);

}
