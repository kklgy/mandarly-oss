package com.mandarly.boot.module.edu.service.review;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.dataobject.review.ReviewDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.CourseOrderMapper;
import com.mandarly.boot.module.edu.dal.mysql.review.ReviewMapper;
import com.mandarly.boot.module.edu.enums.booking.OrderStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    /** 修改窗口:从首次 create_time 起算 72h(Wave 5 第 11 轮:24h→72h) */
    static final Duration EDIT_WINDOW = Duration.ofHours(72);

    /** 单条评价最多 5 个预设 tag(Wave 5:3→5) */
    static final int MAX_TAGS = 5;

    /** 单条评价最多 3 条用户自定义 tag,每条 ≤ 8 字符 */
    static final int MAX_CUSTOM_TAGS = 3;
    static final int CUSTOM_TAG_MAX_LEN = 8;

    /**
     * 预设 tag 白名单 — 与 platform_config.review.tag_dict 同步(mandarly.sql:499 + Wave 5 patch)。
     * Wave 5 在 M3 6 条基础上追加 6 条,共 12 条预设。
     *
     * <p>⚠️ 三源同步(2026-05-10 self-review P1-1 文档化):
     * <ol>
     *   <li>本 WHITELIST(Java 硬编码,validateTags 用)— 当前文件</li>
     *   <li>platform_config.review.tag_dict(admin 后台展示 + i18n 映射元数据)
     *       — patch/20260510_140000_M5_ux_overhaul.sql L101-113 JSON_MERGE_PATCH</li>
     *   <li>i18n_message review.tag.* × 4 语言(用户端 TagPicker 文案)
     *       — patch/20260510_140100_M5_review_tag_i18n_append.sql(独立 patch)</li>
     * </ol>
     * 增改 tag 时三处必须同步,否则 admin 后台或 TagPicker 显示空白 / "未知 tag"。
     * 一期硬编码;后期改为 PlatformConfigService.getReviewTagDict() 动态拉,消除三源。
     */
    static final Set<String> TAG_WHITELIST = Set.of(
            // M3 已落 6 条
            "patient",
            "native_accent",
            "good_pace",
            "well_prepared",
            "audio_issue",
            "late",
            // Wave 5 第 11 轮新增 6 条 positive
            "interactive",
            "good_material",
            "std_pronunciation",
            "humorous",
            "encouraging",
            "proper_difficulty"
    );

    /** 预设 tag 列表(保持顺序,GET /tags 直接用) */
    static final List<String> PRESET_TAG_CODES = List.of(
            "patient", "native_accent", "good_pace", "well_prepared",
            "interactive", "good_material", "std_pronunciation", "humorous",
            "encouraging", "proper_difficulty",
            // 负向放最后(运营展示时通常折叠)
            "audio_issue", "late"
    );

    @Resource
    private ReviewMapper reviewMapper;

    @Resource
    private CourseOrderMapper courseOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveReview(Long orderId, Long studentId, Integer rating, String content,
                           List<String> tags, Boolean isAnonymous, List<String> customTags) {
        CourseOrderDO order = loadAndCheckOrder(orderId, studentId);
        if (!OrderStatusEnum.FINISHED.getCode().equals(order.getStatus())) {
            throw ServiceExceptionUtil.exception0(409,
                    "仅已完成的课程可评价,当前状态 " + order.getStatus());
        }
        if (reviewMapper.selectById(orderId) != null) {
            throw ServiceExceptionUtil.exception0(409, "该订单已评价,请使用更新接口");
        }
        validateRating(rating);
        validateTags(tags);
        validateCustomTags(customTags);

        LocalDateTime now = LocalDateTime.now();
        ReviewDO review = new ReviewDO();
        review.setOrderId(orderId);
        review.setStudentId(studentId);
        review.setTeacherId(order.getTeacherId());
        review.setRating(rating);
        review.setContent(content);
        review.setTags(tags);
        review.setCustomTags(customTags);
        review.setIsAnonymous(Boolean.TRUE.equals(isAnonymous));
        review.setLastEditedAt(now);
        review.setEditableUntilAt(now.plus(EDIT_WINDOW));
        review.setIsVisible(true);
        reviewMapper.insert(review);

        log.info("[saveReview] orderId={} studentId={} teacherId={} rating={} tags={} customTags={} anon={}",
                orderId, studentId, order.getTeacherId(), rating, tags, customTags, isAnonymous);
        return orderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReview(Long orderId, Long studentId, Integer rating, String content,
                             List<String> tags, Boolean isAnonymous, List<String> customTags) {
        ReviewDO existing = reviewMapper.selectById(orderId);
        if (existing == null) {
            throw ServiceExceptionUtil.exception0(404, "评价不存在,请先提交");
        }
        if (!existing.getStudentId().equals(studentId)) {
            throw ServiceExceptionUtil.exception0(403, "只能修改自己的评价");
        }
        // 72h 窗口从首次 create_time 起算(每次 update 不重置)
        Duration elapsed = Duration.between(existing.getCreateTime(), LocalDateTime.now());
        if (elapsed.compareTo(EDIT_WINDOW) > 0) {
            throw ServiceExceptionUtil.exception0(409,
                    "评价修改窗口已过期(限首次提交后 " + EDIT_WINDOW.toHours() + "h 内)");
        }
        validateRating(rating);
        validateTags(tags);
        validateCustomTags(customTags);

        existing.setRating(rating);
        existing.setContent(content);
        existing.setTags(tags);
        existing.setCustomTags(customTags);
        if (isAnonymous != null) {
            existing.setIsAnonymous(isAnonymous);
        }
        existing.setLastEditedAt(LocalDateTime.now());
        reviewMapper.updateById(existing);

        log.info("[updateReview] orderId={} studentId={} elapsedHours={} rating={}",
                orderId, studentId, elapsed.toHours(), rating);
    }

    @Override
    public List<String> getPresetTagCodes() {
        return PRESET_TAG_CODES;
    }

    @Override
    public ReviewDO getReviewByOrder(Long orderId) {
        return reviewMapper.selectById(orderId);
    }

    @Override
    public PageResult<ReviewDO> getReviewsByTeacher(Long teacherId, PageParam pageParam) {
        return reviewMapper.selectVisiblePageByTeacher(teacherId, pageParam);
    }

    @Override
    public PageResult<ReviewDO> getReviewsByStudent(Long studentId, PageParam pageParam) {
        return reviewMapper.selectPageByStudent(studentId, pageParam);
    }

    @Override
    public Map<String, Object> getTeacherStat(Long teacherId) {
        Map<String, Object> raw = reviewMapper.selectStatByTeacher(teacherId);
        Long finishedOrderCount = courseOrderMapper.selectCount(
                Wrappers.<CourseOrderDO>lambdaQuery()
                        .eq(CourseOrderDO::getTeacherId, teacherId)
                        .eq(CourseOrderDO::getStatus, OrderStatusEnum.FINISHED.getCode()));

        BigDecimal avgRating = BigDecimal.ZERO;
        Long reviewCount = 0L;
        if (raw != null) {
            Object avg = raw.get("avg_rating");
            Object count = raw.get("review_count");
            if (avg instanceof Number n) {
                avgRating = BigDecimal.valueOf(n.doubleValue()).setScale(2, RoundingMode.HALF_UP);
            } else if (avg != null) {
                avgRating = new BigDecimal(avg.toString()).setScale(2, RoundingMode.HALF_UP);
            }
            if (count instanceof Number n) {
                reviewCount = n.longValue();
            } else if (count != null) {
                reviewCount = Long.parseLong(count.toString());
            }
        }

        Map<String, Object> result = new HashMap<>(3);
        result.put("avgRating", avgRating);
        result.put("reviewCount", reviewCount);
        result.put("finishedOrderCount", finishedOrderCount);
        return result;
    }

    // ==================== 私有校验 ====================

    private CourseOrderDO loadAndCheckOrder(Long orderId, Long studentId) {
        CourseOrderDO order = courseOrderMapper.selectById(orderId);
        if (order == null) {
            throw ServiceExceptionUtil.exception0(404, "订单不存在");
        }
        if (!order.getStudentId().equals(studentId)) {
            throw ServiceExceptionUtil.exception0(403, "只能评价自己的订单");
        }
        return order;
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw ServiceExceptionUtil.exception0(400, "评分必须为 1-5");
        }
    }

    private void validateTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        if (tags.size() > MAX_TAGS) {
            throw ServiceExceptionUtil.exception0(400, "最多选 " + MAX_TAGS + " 个标签");
        }
        for (String tag : tags) {
            if (!TAG_WHITELIST.contains(tag)) {
                throw ServiceExceptionUtil.exception0(400, "无效的评价标签:" + tag);
            }
        }
    }

    /**
     * 自定义 tag 校验:数量 ≤ 3,每条 1-8 字符,trim 后非空,无控制字符
     */
    private void validateCustomTags(List<String> customTags) {
        if (customTags == null || customTags.isEmpty()) {
            return;
        }
        if (customTags.size() > MAX_CUSTOM_TAGS) {
            throw ServiceExceptionUtil.exception0(400, "最多 " + MAX_CUSTOM_TAGS + " 条自定义标签");
        }
        for (String tag : customTags) {
            if (tag == null) {
                throw ServiceExceptionUtil.exception0(400, "自定义标签不能为空");
            }
            String trimmed = tag.trim();
            if (trimmed.isEmpty() || trimmed.length() > CUSTOM_TAG_MAX_LEN) {
                throw ServiceExceptionUtil.exception0(400,
                        "自定义标签长度需为 1-" + CUSTOM_TAG_MAX_LEN + " 字符");
            }
        }
    }

}
