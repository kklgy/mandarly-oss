package com.mandarly.boot.module.edu.controller.app.review;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.review.vo.AppReviewItemRespVO;
import com.mandarly.boot.module.edu.controller.app.review.vo.AppReviewSaveReqVO;
import com.mandarly.boot.module.edu.controller.app.review.vo.AppReviewUpdateReqVO;
import com.mandarly.boot.module.edu.controller.app.review.vo.AppTeacherReviewStatRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.review.ReviewDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.service.review.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * App 端 - 课后评价(M3 主线 / PRD §S7 + §T6)
 *
 * <p>路由 base:{@code /edu/review}(网关层补 /app-api 前缀)。
 * <p>口径:仅 finished 才能评、24h 改窗、tag 上限 3、白名单见 ReviewServiceImpl#TAG_WHITELIST。
 */
@Tag(name = "用户端 - 课后评价")
@RestController
@RequestMapping("/edu/review")
@Validated
public class AppReviewController {

    @Resource
    private ReviewService reviewService;

    @Resource
    private EduUserMapper eduUserMapper;

    // ==================== 学生端写入 ====================

    @PostMapping("/save")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "提交评价(首次,Wave 5 第 11 轮支持 isAnonymous + customTags)")
    public CommonResult<Long> saveReview(@Valid @RequestBody AppReviewSaveReqVO req) {
        Long studentId = SecurityFrameworkUtils.getLoginUserId();
        Long orderId = reviewService.saveReview(
                req.getOrderId(), studentId, req.getRating(), req.getContent(),
                req.getTags(), req.getIsAnonymous(), req.getCustomTags());
        return success(orderId);
    }

    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "修改评价(首次提交后 72h 内,Wave 5 第 11 轮)")
    public CommonResult<Boolean> updateReview(@Valid @RequestBody AppReviewUpdateReqVO req) {
        Long studentId = SecurityFrameworkUtils.getLoginUserId();
        reviewService.updateReview(
                req.getOrderId(), studentId, req.getRating(), req.getContent(),
                req.getTags(), req.getIsAnonymous(), req.getCustomTags());
        return success(true);
    }

    @GetMapping("/tags")
    @PermitAll
    @Operation(summary = "预设标签字典(Wave 5 第 11 轮 TagPicker;前端 i18n 解 review.tag.{code})")
    public CommonResult<List<String>> presetTagCodes() {
        return success(reviewService.getPresetTagCodes());
    }

    // ==================== 查询 ====================

    @GetMapping("/by-order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按订单查评价(MyOrders 写/改入口前置查)")
    @Parameter(name = "orderId", description = "course_order.id", required = true)
    public CommonResult<AppReviewItemRespVO> getByOrder(@PathVariable Long orderId) {
        ReviewDO review = reviewService.getReviewByOrder(orderId);
        if (review == null) {
            return success(null);
        }
        return success(toItemResp(review, /*publicListing=*/false));
    }

    @GetMapping("/my-page")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "学生:我写过的评价分页")
    public CommonResult<PageResult<AppReviewItemRespVO>> myReviewPage(@Valid PageParam pageParam) {
        Long studentId = SecurityFrameworkUtils.getLoginUserId();
        PageResult<ReviewDO> page = reviewService.getReviewsByStudent(studentId, pageParam);
        return success(toRespPage(page, /*publicListing=*/false));
    }

    // ==================== 公开端 ====================

    @GetMapping("/teacher/{teacherId}/list")
    @PermitAll
    @Operation(summary = "公开:教师评价分页(S2 教师详情)")
    @Parameter(name = "teacherId", description = "教师 user.id", required = true)
    public CommonResult<PageResult<AppReviewItemRespVO>> publicTeacherReviews(
            @PathVariable Long teacherId,
            @Valid PageParam pageParam) {
        if (!isLoggedIn()) {
            return success(new PageResult<>(List.of(), 0L));
        }
        PageResult<ReviewDO> page = reviewService.getReviewsByTeacher(teacherId, pageParam);
        return success(toRespPage(page, /*publicListing=*/true));
    }

    @GetMapping("/teacher/{teacherId}/stat")
    @PermitAll
    @Operation(summary = "公开:教师评分聚合(S2 教师详情顶部 / T6 自查)")
    public CommonResult<AppTeacherReviewStatRespVO> teacherStat(@PathVariable Long teacherId) {
        if (!isLoggedIn()) {
            AppTeacherReviewStatRespVO vo = new AppTeacherReviewStatRespVO();
            vo.setAvgRating(BigDecimal.ZERO);
            vo.setReviewCount(0L);
            vo.setFinishedOrderCount(0L);
            return success(vo);
        }
        Map<String, Object> stat = reviewService.getTeacherStat(teacherId);
        AppTeacherReviewStatRespVO vo = new AppTeacherReviewStatRespVO();
        vo.setAvgRating((BigDecimal) stat.getOrDefault("avgRating", BigDecimal.ZERO));
        vo.setReviewCount((Long) stat.getOrDefault("reviewCount", 0L));
        vo.setFinishedOrderCount((Long) stat.getOrDefault("finishedOrderCount", 0L));
        return success(vo);
    }

    private boolean isLoggedIn() {
        return SecurityFrameworkUtils.getLoginUserId() != null;
    }

    @GetMapping("/teacher/me/stat")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "T6:教师查自己的评分聚合(ProfileView 教学统计区块)")
    public CommonResult<AppTeacherReviewStatRespVO> myStat() {
        Long teacherId = SecurityFrameworkUtils.getLoginUserId();
        return teacherStat(teacherId);
    }

    // ==================== 私有 ====================

    private PageResult<AppReviewItemRespVO> toRespPage(PageResult<ReviewDO> page, boolean publicListing) {
        if (page.getList().isEmpty()) {
            return new PageResult<>(List.of(), page.getTotal());
        }
        Set<Long> studentIds = new HashSet<>();
        for (ReviewDO r : page.getList()) {
            studentIds.add(r.getStudentId());
        }
        Map<Long, String> nicknameById = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<UserDO> users = eduUserMapper.selectBatchIds(studentIds);
            for (UserDO u : users) {
                nicknameById.put(u.getId(), u.getNickname());
            }
        }

        List<AppReviewItemRespVO> list = page.getList().stream()
                .map(r -> {
                    AppReviewItemRespVO vo = toItemResp(r, publicListing);
                    String raw = nicknameById.get(r.getStudentId());
                    // 公开端:匿名评价直接返"匿名学员"占位 i18n key;非匿名走脱敏
                    // 自查端:不脱敏(学生看自己的)
                    if (publicListing) {
                        if (Boolean.TRUE.equals(r.getIsAnonymous())) {
                            vo.setStudentDisplayName("__ANONYMOUS__");  // 前端解 review.anonymous.displayName
                        } else {
                            vo.setStudentDisplayName(maskNickname(raw));
                        }
                    } else {
                        vo.setStudentDisplayName(raw);
                    }
                    return vo;
                })
                .toList();
        return new PageResult<>(list, page.getTotal());
    }

    private AppReviewItemRespVO toItemResp(ReviewDO r, boolean publicListing) {
        AppReviewItemRespVO vo = new AppReviewItemRespVO();
        vo.setOrderId(r.getOrderId());
        vo.setStudentId(r.getStudentId());
        vo.setTeacherId(r.getTeacherId());
        vo.setRating(r.getRating());
        vo.setContent(r.getContent());
        vo.setTags(r.getTags());
        vo.setCreateTime(r.getCreateTime());
        vo.setLastEditedAt(r.getLastEditedAt());
        vo.setIsVisible(r.getIsVisible());
        vo.setIsAnonymous(r.getIsAnonymous());
        vo.setEditableUntilAt(r.getEditableUntilAt());
        vo.setCustomTags(r.getCustomTags());
        return vo;
    }

    /**
     * 学生昵称脱敏(公开端,非匿名):
     * 长度 ≥ 2 → 首字符 + *** + 末字符;长度 1 → 首字符 + ***;空 → "Student".
     * 注:匿名评价(is_anonymous=true)不走本方法,直接置 __ANONYMOUS__,前端解 i18n key.
     */
    static String maskNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "Student";
        }
        String trimmed = nickname.trim();
        if (trimmed.length() == 1) {
            return trimmed + "***";
        }
        return trimmed.charAt(0) + "***" + trimmed.charAt(trimmed.length() - 1);
    }

}
