package com.mandarly.boot.module.edu.service.teacher;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.teacher.vo.TeacherProfileAuditReqVO;
import com.mandarly.boot.module.edu.controller.admin.teacher.vo.TeacherProfilePageReqVO;
import com.mandarly.boot.module.edu.controller.app.teacher.vo.AppTeacherListReqVO;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherProfileUpdateReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;

import java.util.List;

/**
 * 教师档案 Service 接口
 */
public interface TeacherProfileService {

    /**
     * Admin 后台 — 分页查询教师档案
     */
    PageResult<TeacherProfileDO> getTeacherProfilePage(TeacherProfilePageReqVO reqVO);

    /**
     * 注册时为教师建 draft 档案(幂等)。已存在不抛错。
     *
     * <p>D19 Phase A':教师注册三路径(邮箱 / 手机 / OAuth Google&Apple)收口,
     * 完成 user 创建后由上层显式调用此方法落 teacher_profile 行,后续 admin 审核 / 教师端
     * getMe 暴露 teacherAuditStatus 都依赖此行存在。
     *
     * @param userId 教师 user.id(同时也是 teacher_profile 主键)
     */
    void createPendingProfile(Long userId);

    /**
     * 获取单个教师档案
     */
    TeacherProfileDO getTeacherProfile(Long userId);

    /**
     * Admin 审核教师档案(approve / reject)
     */
    void auditTeacherProfile(TeacherProfileAuditReqVO reqVO, Long operatorAdminId);

    /**
     * App 端 — 获取已上线(approved + active)的教师列表(简化首版,不分页 / 不筛选)
     *
     * <p>过滤条件:user.role='teacher' AND user.status='active' AND user.deleted=0
     *           AND teacher_profile.audit_status='approved'
     */
    List<TeacherProfileDO> listVisibleTeachers();

    /**
     * App 端 — 按 filter 查询已上线教师(M5 Wave 4 Subagent E)
     *
     * <p>支持:keyword(模糊 nickname / intro)、accent / expertise / tags / priceBuckets / available /
     * minRating / sort / pageNo / pageSize。
     *
     * <p>过渡期实现:由于 teacher_profile 表 tags / recommend_weight / avg_rating / display_price /
     * today_available_count 字段尚未落库(M5 SQL patch 同步落),Service 在内存做 filter + sort:
     * keyword / accent / expertise 走 SQL,priceBuckets / available / minRating / tags 字段未就位
     * 时降级跳过(返当前已审核教师)。SQL patch 落地后改 Mapper 走索引(plan §11.12)。
     *
     * @param reqVO 查询参数
     * @return 命中的 TeacherProfileDO 列表(已分页)
     */
    List<TeacherProfileDO> listVisibleTeachers(AppTeacherListReqVO reqVO);

    /**
     * App 端 — 按 filter 查询已上线教师的总数(M5 Wave 4 Subagent E)
     *
     * <p>用于抽屉"应用筛选(N)"按钮的实时命中数。
     *
     * @param reqVO 查询参数(忽略 pageNo / pageSize / sort)
     * @return 命中数
     */
    long countVisibleTeachers(AppTeacherListReqVO reqVO);

    /**
     * 教师本人更新自己档案(D19 Phase B / B3)。
     *
     * <p>partial update 语义:null 字段不动,允许只改 intro 不动 expertise。
     *
     * <p>**不修改 audit_status** — 即使当前 approved,改字段也不会自动转 pending
     * (改重要字段触发回审是二期;一期需要回审让教师走 {@link #submitForAudit(Long)} 入口)。
     *
     * @param userId 教师 user.id(从 Security 上下文获取)
     * @param req    新字段值;null 字段不动
     */
    void updateOwnProfile(Long userId, AppTeacherProfileUpdateReqVO req);

    /**
     * 教师显式提交审核(rejected → pending 状态机)(D19 Phase B / B3)。
     *
     * <p>状态转换规则:
     * <ul>
     *   <li>rejected → pending,清空 rejectReason / auditedAt / auditedBy</li>
     *   <li>pending → 抛 TEACHER_PROFILE_INVALID_STATUS_FOR_SUBMIT(已在审核中)</li>
     *   <li>approved → 抛 TEACHER_PROFILE_INVALID_STATUS_FOR_SUBMIT(已上架,改字段先驳回)</li>
     * </ul>
     *
     * @param userId 教师 user.id(从 Security 上下文获取)
     */
    void submitForAudit(Long userId);

    /**
     * 把 teacher_profile.intro_video_url(DB 存的是 object key)签成 COS 临时下载 URL,
     * 供学生端 / 教师本人 / admin / 等级测试 4 条读路径直接 set 给前端 video src 播放。
     *
     * <p>对齐 {@link TeacherQualificationService#generatePresignedUrl(String)} 的资质材料签名,
     * 也复用 {@code TeacherQualificationServiceImpl#normalizeToObjectKey} 兜底历史脏数据
     * (DB 里若残留完整 signed URL,这里再 normalize 一遍)。
     *
     * @param url DB 字段值(预期 object key,兼容历史完整 signed URL)
     * @return 签名后的 COS https URL(15 分钟 TTL);null / blank 返 null
     */
    String presignIntroVideoUrl(String url);

}
