package com.mandarly.boot.module.edu.service.teacher;

import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.teacher.vo.TeacherProfileAuditReqVO;
import com.mandarly.boot.module.edu.controller.admin.teacher.vo.TeacherProfilePageReqVO;
import com.mandarly.boot.module.edu.controller.app.teacher.vo.AppTeacherListReqVO;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherProfileUpdateReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherQualificationDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherProfileMapper;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherQualificationMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum;
import com.mandarly.boot.module.edu.enums.teacher.TeacherQualificationDocTypeEnum;
import com.mandarly.boot.module.edu.enums.user.UserRoleEnum;
import com.mandarly.boot.module.edu.enums.user.UserStatusEnum;
import com.mandarly.boot.module.edu.service.notification.NotificationService;
import com.mandarly.boot.module.infra.api.file.FileApi;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.TEACHER_PROFILE_INVALID_STATUS_FOR_SUBMIT;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.TEACHER_REQUIRED_QUALIFICATION_MISSING;

/**
 * 教师档案 Service 实现
 */
@Service
@Slf4j
public class TeacherProfileServiceImpl implements TeacherProfileService {

    /**
     * intro_video_url 预签名 TTL(秒)— 15 分钟,与资质材料 TTL 对齐
     */
    private static final int INTRO_VIDEO_PRESIGN_TTL_SECONDS = 900;

    @Resource
    private TeacherProfileMapper teacherProfileMapper;

    @Resource
    private TeacherQualificationMapper teacherQualificationMapper;

    @Resource
    private EduUserMapper eduUserMapper;

    @Resource
    private NotificationService notificationService;

    @Resource
    private FileApi fileApi;

    @Override
    public PageResult<TeacherProfileDO> getTeacherProfilePage(TeacherProfilePageReqVO reqVO) {
        return teacherProfileMapper.selectPage(reqVO);
    }

    @Override
    public void createPendingProfile(Long userId) {
        if (teacherProfileMapper.selectById(userId) != null) {
            log.debug("[createPendingProfile] userId={} already has profile, skip", userId);
            return; // 幂等:已存在档案不重复落行
        }
        TeacherProfileDO profile = new TeacherProfileDO();
        profile.setUserId(userId);
        profile.setAuditStatus(TeacherAuditStatusEnum.DRAFT.getCode());
        // 自注册场景 SecurityFrameworkUtils 还没 login user,MP auto-fill 拿不到 creator/updater
        // 显式填 userId 自己作为 creator(memory local_dev_workflow:@PermitAll 路径必须兜底)
        String userIdStr = String.valueOf(userId);
        profile.setCreator(userIdStr);
        profile.setUpdater(userIdStr);
        teacherProfileMapper.insert(profile);
        log.info("[createPendingProfile] userId={} -> draft profile created", userId);
    }

    @Override
    public TeacherProfileDO getTeacherProfile(Long userId) {
        return teacherProfileMapper.selectById(userId);
    }

    @Override
    public void auditTeacherProfile(TeacherProfileAuditReqVO reqVO, Long operatorAdminId) {
        TeacherProfileDO profile = teacherProfileMapper.selectById(reqVO.getUserId());
        if (profile == null) {
            throw ServiceExceptionUtil.exception0(404, "教师档案不存在");
        }

        boolean approve = "approve".equalsIgnoreCase(reqVO.getAction());
        boolean reject = "reject".equalsIgnoreCase(reqVO.getAction());
        if (!approve && !reject) {
            throw ServiceExceptionUtil.exception0(400, "action 必须是 approve / reject");
        }
        if (reject && !StringUtils.hasText(reqVO.getRejectReason())) {
            throw ServiceExceptionUtil.exception0(400, "驳回时必须填写 rejectReason");
        }

        profile.setAuditStatus(approve
                ? TeacherAuditStatusEnum.APPROVED.getCode()
                : TeacherAuditStatusEnum.REJECTED.getCode());
        profile.setRejectReason(approve ? null : reqVO.getRejectReason());
        profile.setAuditedAt(LocalDateTime.now());
        profile.setAuditedBy(operatorAdminId);
        teacherProfileMapper.updateById(profile);

        // D19-A6: 触发审核结果通知(A5 三通道 facade — 一期 mail only,sms+inbox TODO)
        // 放在 updateById 之后:DB 落定后再通知;A5 内部 @Async 并自带 try/catch,不会回滚主流程。
        String event = approve ? "approved" : "rejected";
        notificationService.sendForTeacherAuditEvent(event, reqVO.getUserId(),
                approve ? null : reqVO.getRejectReason());

        log.info("[auditTeacherProfile] userId={} action={} operator={}",
                reqVO.getUserId(), reqVO.getAction(), operatorAdminId);
    }

    @Override
    public List<TeacherProfileDO> listVisibleTeachers() {
        // 1. 拿到所有 active 教师 user.id
        List<UserDO> activeTeachers = eduUserMapper.selectList(Wrappers.<UserDO>lambdaQuery()
                .eq(UserDO::getRole, UserRoleEnum.TEACHER.getCode())
                .eq(UserDO::getStatus, UserStatusEnum.ACTIVE.getCode()));
        if (activeTeachers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> teacherIds = activeTeachers.stream().map(UserDO::getId).toList();

        // 2. 在档案表中筛 approved
        return teacherProfileMapper.selectList(Wrappers.<TeacherProfileDO>lambdaQuery()
                .in(TeacherProfileDO::getUserId, teacherIds)
                .eq(TeacherProfileDO::getAuditStatus, TeacherAuditStatusEnum.APPROVED.getCode()));
    }

    // =========================================================================
    // M5 Wave 4 Subagent E — TeacherList 搜索 / 筛选 / 排序 / 分页
    //
    // 过渡期实现:teacher_profile 字段 tags / recommend_weight / avg_rating /
    // display_price / today_available_count 尚未落库;此处 service 内存 filter:
    //   - 实施:keyword / accent(String 单值过渡)/ expertise(JSON 字段已有)
    //   - 降级:priceBuckets / available / minRating / tags(字段缺失,跳过 filter)
    //   - sort:recommend(createTime DESC fallback)/ rating_desc(降级 createTime)/
    //          price_*  (降级 createTime)/ review_count_desc(降级 createTime)
    //
    // SQL patch 落地后,把过滤 / 排序下放到 Mapper 走 JSON_CONTAINS + 索引(plan §11.12)。
    // =========================================================================
    @Override
    public List<TeacherProfileDO> listVisibleTeachers(AppTeacherListReqVO reqVO) {
        List<TeacherProfileDO> all = listVisibleTeachersFiltered(reqVO);
        // 排序
        Comparator<TeacherProfileDO> cmp = buildComparator(reqVO == null ? null : reqVO.getSort());
        all.sort(cmp);
        // 分页
        int pageNo = (reqVO != null && reqVO.getPageNo() != null && reqVO.getPageNo() > 0)
                ? reqVO.getPageNo() : 1;
        int pageSize = (reqVO != null && reqVO.getPageSize() != null && reqVO.getPageSize() > 0)
                ? reqVO.getPageSize() : 24;
        int from = Math.min((pageNo - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        return new ArrayList<>(all.subList(from, to));
    }

    @Override
    public long countVisibleTeachers(AppTeacherListReqVO reqVO) {
        return listVisibleTeachersFiltered(reqVO).size();
    }

    /**
     * 共用的"取已审核教师 + 应用 filter"内部方法(不分页 / 不排序)。
     */
    private List<TeacherProfileDO> listVisibleTeachersFiltered(AppTeacherListReqVO reqVO) {
        // 1. 拿到所有 active 教师 + 昵称(keyword 模糊用)
        List<UserDO> activeTeachers = eduUserMapper.selectList(Wrappers.<UserDO>lambdaQuery()
                .eq(UserDO::getRole, UserRoleEnum.TEACHER.getCode())
                .eq(UserDO::getStatus, UserStatusEnum.ACTIVE.getCode()));
        if (activeTeachers.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, String> nicknameMap = new HashMap<>();
        for (UserDO u : activeTeachers) {
            nicknameMap.put(u.getId(), u.getNickname() == null ? "" : u.getNickname());
        }
        List<Long> teacherIds = activeTeachers.stream().map(UserDO::getId).toList();

        // 2. 在档案表中筛 approved
        List<TeacherProfileDO> profiles = teacherProfileMapper.selectList(
                Wrappers.<TeacherProfileDO>lambdaQuery()
                        .in(TeacherProfileDO::getUserId, teacherIds)
                        .eq(TeacherProfileDO::getAuditStatus,
                                TeacherAuditStatusEnum.APPROVED.getCode()));

        if (reqVO == null) {
            return profiles;
        }

        // 3. keyword 模糊(nickname / intro)
        String keyword = StringUtils.hasText(reqVO.getKeyword()) ? reqVO.getKeyword().trim() : null;
        Set<String> accentSet = parseCsvSet(reqVO.getAccent());
        Set<String> expertiseSet = parseCsvSet(reqVO.getExpertise());
        // priceBuckets / minRating / available / tags:字段未落,过渡期跳过。
        // 上线 SQL patch 后改 Mapper 走 JSON_CONTAINS,本块删除。
        boolean filterPriceBuckets = StringUtils.hasText(reqVO.getPriceBuckets());
        boolean filterAvailable = Boolean.TRUE.equals(reqVO.getAvailable());
        boolean filterMinRating = reqVO.getMinRating() != null
                && reqVO.getMinRating().signum() > 0;
        Set<String> tagSet = parseCsvSet(reqVO.getTags());

        if (filterPriceBuckets || filterAvailable || filterMinRating || !tagSet.isEmpty()) {
            log.warn("[listVisibleTeachers] M5 SQL patch 未落,priceBuckets/available/minRating/tags filter 跳过"
                    + " (priceBuckets={}, available={}, minRating={}, tags={})",
                    reqVO.getPriceBuckets(), reqVO.getAvailable(), reqVO.getMinRating(), reqVO.getTags());
        }

        List<TeacherProfileDO> result = new ArrayList<>(profiles.size());
        for (TeacherProfileDO p : profiles) {
            // keyword
            if (keyword != null) {
                String nickname = nicknameMap.getOrDefault(p.getUserId(), "");
                String intro = p.getIntro() == null ? "" : p.getIntro();
                String kw = keyword.toLowerCase();
                if (!nickname.toLowerCase().contains(kw) && !intro.toLowerCase().contains(kw)) {
                    continue;
                }
            }
            // accent(过渡期 String 单值,落库后是 List<String>;两种都兼容)
            if (!accentSet.isEmpty()) {
                String accent = p.getAccent();
                if (accent == null || !accentSet.contains(accent)) {
                    continue;
                }
            }
            // expertise(JSON List 字段)— OR 语义,任一命中即可
            if (!expertiseSet.isEmpty()) {
                List<String> ex = p.getExpertise();
                if (ex == null || ex.isEmpty()) continue;
                Set<String> have = new HashSet<>(ex);
                have.retainAll(expertiseSet);
                if (have.isEmpty()) continue;
            }
            result.add(p);
        }
        return result;
    }

    private Set<String> parseCsvSet(String csv) {
        if (!StringUtils.hasText(csv)) return Collections.emptySet();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * 排序映射;字段未落地时统一降级到 createTime DESC(plan §6 / DESIGN §D)。
     */
    private Comparator<TeacherProfileDO> buildComparator(String sort) {
        // createTime DESC 兜底
        Comparator<TeacherProfileDO> byCreateDesc = (a, b) -> {
            LocalDateTime ta = a.getCreateTime();
            LocalDateTime tb = b.getCreateTime();
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        };
        if (sort == null) return byCreateDesc;
        switch (sort) {
            case "recommend":
                // recommend_weight DESC, avg_rating DESC, finished_order_count DESC(字段未落,降 createTime DESC)
                return byCreateDesc;
            case "rating_desc":
                // avg_rating DESC(字段未落,降 createTime DESC)
                return byCreateDesc;
            case "price_asc":
            case "price_desc":
                // display_price ASC/DESC(字段未落,降 createTime DESC)
                return byCreateDesc;
            case "review_count_desc":
                // review_count DESC(字段未落,降 createTime DESC)
                return byCreateDesc;
            default:
                return byCreateDesc;
        }
    }

    // =========================================================================
    // D19 Phase B / B3 — 教师本人改自己档案 + 显式重提审核
    // =========================================================================

    @Override
    public void updateOwnProfile(Long userId, AppTeacherProfileUpdateReqVO req) {
        TeacherProfileDO profile = teacherProfileMapper.selectById(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception0(404, "教师档案不存在");
        }
        // partial update:null 字段不动,允许只改 intro 不动 expertise
        // 不修改 audit_status:即使 approved 改字段也不自动转 pending(回审走 submitForAudit)
        if (req.getIntro() != null) {
            profile.setIntro(req.getIntro());
        }
        if (req.getExpertise() != null) {
            profile.setExpertise(req.getExpertise());
        }
        if (req.getAccent() != null) {
            profile.setAccent(req.getAccent());
        }
        if (req.getLanguages() != null) {
            profile.setLanguages(req.getLanguages());
        }
        if (req.getYearsExperience() != null) {
            profile.setYearsExperience(req.getYearsExperience());
        }
        if (req.getIntroVideoUrl() != null) {
            // prod 2026-05-18:/infra/file/upload 返完整 signed URL,客户端直接当 URL 存
            // 会触发 Hutool UrlBuilder 把 path-only 解析为 file URL 的 bug(详见
            // TeacherQualificationServiceImpl.normalizeToObjectKey)。入口 normalize 存 key。
            profile.setIntroVideoUrl(TeacherQualificationServiceImpl.normalizeToObjectKey(req.getIntroVideoUrl()));
        }
        teacherProfileMapper.updateById(profile);
        log.info("[updateOwnProfile] userId={} updated own profile", userId);
    }

    @Override
    public void submitForAudit(Long userId) {
        TeacherProfileDO profile = teacherProfileMapper.selectById(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception0(404, "教师档案不存在");
        }
        // 严格状态机:只接受 draft / rejected → pending;pending / approved 都拒绝
        if (!TeacherAuditStatusEnum.DRAFT.getCode().equals(profile.getAuditStatus())
                && !TeacherAuditStatusEnum.REJECTED.getCode().equals(profile.getAuditStatus())) {
            throw exception(TEACHER_PROFILE_INVALID_STATUS_FOR_SUBMIT);
        }
        validateRequiredQualifications(userId);
        profile.setAuditStatus(TeacherAuditStatusEnum.PENDING.getCode());
        profile.setRejectReason(null);
        profile.setAuditedAt(null);
        profile.setAuditedBy(null);
        teacherProfileMapper.updateById(profile);
        log.info("[submitForAudit] userId={} submitted for audit", userId);
    }

    private void validateRequiredQualifications(Long userId) {
        List<TeacherQualificationDO> rows = teacherQualificationMapper.selectListByUserId(userId);
        Set<String> docTypes = rows == null
                ? Collections.emptySet()
                : rows.stream()
                        .map(TeacherQualificationDO::getDocType)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
        if (!docTypes.contains(TeacherQualificationDocTypeEnum.ID_CARD.getCode())
                || !docTypes.contains(TeacherQualificationDocTypeEnum.DEGREE_CERT.getCode())) {
            throw exception(TEACHER_REQUIRED_QUALIFICATION_MISSING);
        }
    }

    @Override
    public String presignIntroVideoUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        // D19 续: 写入侧已 normalize(updateOwnProfile L315), 这里再 normalize 兜底历史脏数据
        // (prod 2026-05-15/16 cutover 前 2 个教师档案 introVideoUrl 字段曾以完整 signed URL 入库)
        return fileApi.presignGetUrl(
                TeacherQualificationServiceImpl.normalizeToObjectKey(url),
                INTRO_VIDEO_PRESIGN_TTL_SECONDS);
    }

}
