package com.mandarly.boot.module.edu.dal.mysql.report;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface EduReportMapper {

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM `user`
            WHERE deleted = b'0'
              <if test="role != null and role != ''">
              AND role = #{role}
              </if>
              <if test="beginAt != null">
              AND create_time &gt;= #{beginAt}
              </if>
              <if test="endAt != null">
              AND create_time &lt; #{endAt}
              </if>
            </script>
            """)
    Long countUsers(@Param("role") String role,
                    @Param("beginAt") LocalDateTime beginAt,
                    @Param("endAt") LocalDateTime endAt);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM teacher_profile
            WHERE deleted = b'0'
              <if test="auditStatus != null and auditStatus != ''">
              AND audit_status = #{auditStatus}
              </if>
              <if test="beginAt != null">
              AND create_time &gt;= #{beginAt}
              </if>
              <if test="endAt != null">
              AND create_time &lt; #{endAt}
              </if>
            </script>
            """)
    Long countTeacherProfiles(@Param("auditStatus") String auditStatus,
                              @Param("beginAt") LocalDateTime beginAt,
                              @Param("endAt") LocalDateTime endAt);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM course_order
            WHERE deleted = b'0'
              <if test="status != null and status != ''">
              AND status = #{status}
              </if>
              <if test="beginAt != null">
              AND create_time &gt;= #{beginAt}
              </if>
              <if test="endAt != null">
              AND create_time &lt; #{endAt}
              </if>
            </script>
            """)
    Long countCourseOrders(@Param("status") String status,
                           @Param("beginAt") LocalDateTime beginAt,
                           @Param("endAt") LocalDateTime endAt);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM payment
            WHERE deleted = b'0'
              AND status IN ('paid', 'partial_refunded', 'refunded')
              <if test="beginAt != null">
              AND paid_at &gt;= #{beginAt}
              </if>
              <if test="endAt != null">
              AND paid_at &lt; #{endAt}
              </if>
            </script>
            """)
    Long countPaidPayments(@Param("beginAt") LocalDateTime beginAt,
                           @Param("endAt") LocalDateTime endAt);

    @Select("""
            <script>
            SELECT IFNULL(SUM(amount_settled_usd), 0)
            FROM payment
            WHERE deleted = b'0'
              AND status IN ('paid', 'partial_refunded', 'refunded')
              <if test="beginAt != null">
              AND paid_at &gt;= #{beginAt}
              </if>
              <if test="endAt != null">
              AND paid_at &lt; #{endAt}
              </if>
            </script>
            """)
    BigDecimal sumPaidAmountUsd(@Param("beginAt") LocalDateTime beginAt,
                                @Param("endAt") LocalDateTime endAt);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM refund
            WHERE deleted = b'0'
              <if test="status != null and status != ''">
              AND status = #{status}
              </if>
              <if test="beginAt != null">
              AND refunded_at &gt;= #{beginAt}
              </if>
              <if test="endAt != null">
              AND refunded_at &lt; #{endAt}
              </if>
            </script>
            """)
    Long countRefunds(@Param("status") String status,
                      @Param("beginAt") LocalDateTime beginAt,
                      @Param("endAt") LocalDateTime endAt);

    @Select("""
            <script>
            SELECT IFNULL(SUM(final_amount_usd), 0)
            FROM refund
            WHERE deleted = b'0'
              AND status = 'refunded'
              <if test="beginAt != null">
              AND refunded_at &gt;= #{beginAt}
              </if>
              <if test="endAt != null">
              AND refunded_at &lt; #{endAt}
              </if>
            </script>
            """)
    BigDecimal sumRefundedAmountUsd(@Param("beginAt") LocalDateTime beginAt,
                                    @Param("endAt") LocalDateTime endAt);
}
