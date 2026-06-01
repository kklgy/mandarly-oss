package com.mandarly.boot.module.edu.service.review;

import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.dataobject.review.ReviewDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.CourseOrderMapper;
import com.mandarly.boot.module.edu.dal.mysql.review.ReviewMapper;
import com.mandarly.boot.module.edu.enums.booking.OrderStatusEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * M3 ReviewServiceImpl 校验逻辑单元测试。
 *
 * <p>覆盖:finished 才能评 / 本人订单 / 唯一约束 / tag 白名单 / tag 上限 3 / rating 1-5 / 24h 修改窗口。
 */
class ReviewServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private CourseOrderMapper courseOrderMapper;

    private static final Long ORDER_ID = 100L;
    private static final Long STUDENT_ID = 1L;
    private static final Long TEACHER_ID = 2L;
    private static final Long OTHER_STUDENT_ID = 99L;

    private CourseOrderDO finishedOrder() {
        CourseOrderDO o = new CourseOrderDO();
        o.setId(ORDER_ID);
        o.setStudentId(STUDENT_ID);
        o.setTeacherId(TEACHER_ID);
        o.setStatus(OrderStatusEnum.FINISHED.getCode());
        return o;
    }

    // ==================== save:正路 ====================

    @Test
    void save_finished_validTags_succeeds() {
        when(courseOrderMapper.selectById(ORDER_ID)).thenReturn(finishedOrder());
        when(reviewMapper.selectById(ORDER_ID)).thenReturn(null);

        Long result = reviewService.saveReview(ORDER_ID, STUDENT_ID, 5, "great",
                List.of("patient", "good_pace"), false, List.of());

        assertThat(result).isEqualTo(ORDER_ID);
        verify(reviewMapper, times(1)).insert(any(ReviewDO.class));
    }

    @Test
    void save_emptyTags_emptyContent_succeeds() {
        when(courseOrderMapper.selectById(ORDER_ID)).thenReturn(finishedOrder());
        when(reviewMapper.selectById(ORDER_ID)).thenReturn(null);

        Long result = reviewService.saveReview(ORDER_ID, STUDENT_ID, 4, null, List.of(), false, List.of());

        assertThat(result).isEqualTo(ORDER_ID);
        verify(reviewMapper).insert(any(ReviewDO.class));
    }

    // ==================== save:拒路 ====================

    @Test
    void save_orderNotFinished_rejects() {
        CourseOrderDO order = finishedOrder();
        order.setStatus(OrderStatusEnum.UPCOMING.getCode());
        when(courseOrderMapper.selectById(ORDER_ID)).thenReturn(order);

        assertThatThrownBy(() -> reviewService.saveReview(
                ORDER_ID, STUDENT_ID, 5, "x", List.of("patient"), false, List.of()))
                .hasMessageContaining("仅已完成的课程可评价");
        verify(reviewMapper, never()).insert(any(ReviewDO.class));
    }

    @Test
    void save_notOwnOrder_rejects() {
        when(courseOrderMapper.selectById(ORDER_ID)).thenReturn(finishedOrder());

        assertThatThrownBy(() -> reviewService.saveReview(
                ORDER_ID, OTHER_STUDENT_ID, 5, "x", List.of(), false, List.of()))
                .hasMessageContaining("只能评价自己的订单");
        verify(reviewMapper, never()).insert(any(ReviewDO.class));
    }

    @Test
    void save_alreadyReviewed_rejectsAndHintsUpdate() {
        when(courseOrderMapper.selectById(ORDER_ID)).thenReturn(finishedOrder());
        ReviewDO existing = new ReviewDO();
        existing.setOrderId(ORDER_ID);
        when(reviewMapper.selectById(ORDER_ID)).thenReturn(existing);

        assertThatThrownBy(() -> reviewService.saveReview(
                ORDER_ID, STUDENT_ID, 5, "x", List.of(), false, List.of()))
                .hasMessageContaining("使用更新接口");
        verify(reviewMapper, never()).insert(any(ReviewDO.class));
    }

    @Test
    void save_invalidTag_rejects() {
        when(courseOrderMapper.selectById(ORDER_ID)).thenReturn(finishedOrder());
        when(reviewMapper.selectById(ORDER_ID)).thenReturn(null);

        assertThatThrownBy(() -> reviewService.saveReview(
                ORDER_ID, STUDENT_ID, 5, "x", List.of("not_in_dict"), false, List.of()))
                .hasMessageContaining("无效的评价标签");
        verify(reviewMapper, never()).insert(any(ReviewDO.class));
    }

    @Test
    void save_tooManyTags_rejects() {
        when(courseOrderMapper.selectById(ORDER_ID)).thenReturn(finishedOrder());
        when(reviewMapper.selectById(ORDER_ID)).thenReturn(null);

        assertThatThrownBy(() -> reviewService.saveReview(
                ORDER_ID, STUDENT_ID, 5, "x",
                List.of("patient", "good_pace", "well_prepared", "native_accent",
                        "interactive", "humorous"),
                false, List.of()))
                .hasMessageContaining("最多选");
        verify(reviewMapper, never()).insert(any(ReviewDO.class));
    }

    @Test
    void save_invalidRating_rejects() {
        when(courseOrderMapper.selectById(ORDER_ID)).thenReturn(finishedOrder());
        when(reviewMapper.selectById(ORDER_ID)).thenReturn(null);

        assertThatThrownBy(() -> reviewService.saveReview(
                ORDER_ID, STUDENT_ID, 6, "x", List.of(), false, List.of()))
                .hasMessageContaining("评分必须为 1-5");
        verify(reviewMapper, never()).insert(any(ReviewDO.class));
    }

    // ==================== update ====================

    @Test
    void update_withinWindow_succeeds() {
        ReviewDO existing = new ReviewDO();
        existing.setOrderId(ORDER_ID);
        existing.setStudentId(STUDENT_ID);
        existing.setCreateTime(LocalDateTime.now().minusHours(10));  // 10h 前提交
        when(reviewMapper.selectById(ORDER_ID)).thenReturn(existing);

        reviewService.updateReview(ORDER_ID, STUDENT_ID, 4, "updated", List.of("patient"), false, List.of());

        verify(reviewMapper).updateById(eq(existing));
        assertThat(existing.getRating()).isEqualTo(4);
        assertThat(existing.getContent()).isEqualTo("updated");
        assertThat(existing.getTags()).containsExactly("patient");
    }

    @Test
    void update_pastWindow_rejects() {
        ReviewDO existing = new ReviewDO();
        existing.setOrderId(ORDER_ID);
        existing.setStudentId(STUDENT_ID);
        existing.setCreateTime(LocalDateTime.now().minusHours(73));  // 73h 前提交(超过 72h EDIT_WINDOW)
        when(reviewMapper.selectById(ORDER_ID)).thenReturn(existing);

        assertThatThrownBy(() -> reviewService.updateReview(
                ORDER_ID, STUDENT_ID, 3, "y", List.of(), false, List.of()))
                .hasMessageContaining("修改窗口已过期");
        verify(reviewMapper, never()).updateById(any(ReviewDO.class));
    }

    @Test
    void update_notOwnReview_rejects() {
        ReviewDO existing = new ReviewDO();
        existing.setOrderId(ORDER_ID);
        existing.setStudentId(STUDENT_ID);
        existing.setCreateTime(LocalDateTime.now().minusHours(1));
        when(reviewMapper.selectById(ORDER_ID)).thenReturn(existing);

        assertThatThrownBy(() -> reviewService.updateReview(
                ORDER_ID, OTHER_STUDENT_ID, 4, "x", List.of(), false, List.of()))
                .hasMessageContaining("只能修改自己的评价");
        verify(reviewMapper, never()).updateById(any(ReviewDO.class));
    }

    @Test
    void update_nonExistent_rejects() {
        when(reviewMapper.selectById(ORDER_ID)).thenReturn(null);

        assertThatThrownBy(() -> reviewService.updateReview(
                ORDER_ID, STUDENT_ID, 5, "x", List.of(), false, List.of()))
                .hasMessageContaining("评价不存在");
    }
}
