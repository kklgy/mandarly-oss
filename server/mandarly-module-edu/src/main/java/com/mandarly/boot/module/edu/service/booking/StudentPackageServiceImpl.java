package com.mandarly.boot.module.edu.service.booking;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.StudentPackageGrantReqVO;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.StudentPackagePageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.StudentPackageDO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import com.mandarly.boot.module.edu.dal.mysql.booking.StudentPackageMapper;
import com.mandarly.boot.module.edu.dal.mysql.pkg.PackageMapper;
import com.mandarly.boot.module.edu.enums.booking.StudentPackageSourceEnum;
import com.mandarly.boot.module.edu.service.pkg.PackageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class StudentPackageServiceImpl implements StudentPackageService {

    @Resource
    private StudentPackageMapper studentPackageMapper;

    @Resource
    private PackageMapper packageMapper;

    @Resource
    private PackageService packageService;

    @Value("${mandarly.app-auth.referral.free-trial-package-id:1}")
    private Long freeTrialPackageId;

    @Override
    public PageResult<StudentPackageDO> getStudentPackagePage(StudentPackagePageReqVO reqVO) {
        return studentPackageMapper.selectPage(reqVO);
    }

    @Override
    public StudentPackageDO getStudentPackage(Long id) {
        return studentPackageMapper.selectById(id);
    }

    @Override
    public List<StudentPackageDO> listActiveByStudent(Long studentId) {
        return studentPackageMapper.selectActiveByStudent(studentId, LocalDateTime.now());
    }

    @Override
    public List<StudentPackageDO> listAllByStudent(Long studentId) {
        return studentPackageMapper.selectAllByStudent(studentId);
    }

    @Override
    public Long grantPackage(StudentPackageGrantReqVO reqVO, Long operatorAdminId) {
        PackageDO pkg = packageMapper.selectById(reqVO.getPackageId());
        if (pkg == null) {
            throw ServiceExceptionUtil.exception0(404, "套餐定义不存在");
        }
        if (!Boolean.TRUE.equals(pkg.getIsActive())) {
            log.warn("[grantPackage] 套餐已下架但仍发放,packageId={}", pkg.getId());
        }

        Integer remaining = reqVO.getRemainingOverride() != null
                ? reqVO.getRemainingOverride()
                : pkg.getTotalCount();
        int validityDays = reqVO.getValidityDaysOverride() != null
                ? reqVO.getValidityDaysOverride()
                : pkg.getValidityDays();

        StudentPackageDO entity = new StudentPackageDO();
        entity.setStudentId(reqVO.getStudentId());
        entity.setPackageId(reqVO.getPackageId());
        entity.setRemaining(remaining);
        entity.setExpireAt(LocalDateTime.now().plusDays(validityDays));
        entity.setSource(StudentPackageSourceEnum.ADMIN_GRANT.getCode());
        entity.setGrantedBy(operatorAdminId);
        entity.setGrantedReason(reqVO.getGrantedReason());
        studentPackageMapper.insert(entity);

        log.info("[grantPackage] studentId={} packageId={} remaining={} expire={} operator={}",
                entity.getStudentId(), entity.getPackageId(), entity.getRemaining(),
                entity.getExpireAt(), operatorAdminId);
        return entity.getId();
    }

    /**
     * 注册路径自动发放免费体验套餐(source=register_grant)。
     *
     * <p>幂等保护(2026-05-10 self-review P0-2 加固,c 方案):
     * <ol>
     *   <li>快路径:selectOne 重查 — 已有 register_grant 直接 return(覆盖 99% 顺序调用)</li>
     *   <li>慢路径兜底:catch DataIntegrityViolationException — 极端并发(多 tab 同时注册回调)
     *       两次 insert 同时穿过 selectCount,DB unique index 拒第二条,catch 后视同已发放</li>
     * </ol>
     * 配套 DB unique:patch/20260510_150000_M5_student_package_freetrial_unique.sql
     *   functional unique on (CASE WHEN source IN ('register_grant','free_trial') THEN student_id END, source)
     * 该 patch 为可选项,未启用时仍依赖快路径(并发缝极小、白嫖损失可控)。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long grantFreeTrialPackage(Long userId) {
        StudentPackageDO existing = studentPackageMapper.selectOne(
                Wrappers.<StudentPackageDO>lambdaQuery()
                        .eq(StudentPackageDO::getStudentId, userId)
                        .eq(StudentPackageDO::getSource, StudentPackageSourceEnum.REGISTER_GRANT.getCode())
                        .last("LIMIT 1"));
        if (existing != null) {
            log.info("[grantFreeTrialPackage] 已有 register_grant 记录,跳过 userId={}", userId);
            return existing.getId();
        }

        PackageDO pkg = packageService.getPackage(freeTrialPackageId);
        if (pkg == null || !Boolean.TRUE.equals(pkg.getIsFreeTrial())) {
            log.warn("[grantFreeTrialPackage] 免费体验套餐 id={} 不存在或非 isFreeTrial,跳过 grant userId={}",
                    freeTrialPackageId, userId);
            return null;
        }

        StudentPackageDO entity = buildPackageGrant(userId, pkg, StudentPackageSourceEnum.REGISTER_GRANT);
        // 注册路径无登录上下文,手动兜底 creator/updater 让 NOT NULL 通过
        entity.setCreator("system");
        entity.setUpdater("system");

        try {
            studentPackageMapper.insert(entity);
        } catch (DataIntegrityViolationException ex) {
            // 慢路径兜底:并发竞态被 DB unique index 拒(需 patch 20260510_150000 启用)
            log.warn("[grantFreeTrialPackage] DB unique 拒重复 grant,视同已发放 userId={} cause={}",
                    userId, ex.getMostSpecificCause().getMessage());
            StudentPackageDO current = studentPackageMapper.selectOne(
                    Wrappers.<StudentPackageDO>lambdaQuery()
                            .eq(StudentPackageDO::getStudentId, userId)
                            .eq(StudentPackageDO::getSource, StudentPackageSourceEnum.REGISTER_GRANT.getCode())
                            .last("LIMIT 1"));
            return current == null ? null : current.getId();
        }

        log.info("[grantFreeTrialPackage] grant 免费体验 userId={} packageId={} remaining={} expireAt={}",
                userId, freeTrialPackageId, entity.getRemaining(), entity.getExpireAt());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long grantReferralRewardPackage(Long userId, Long packageId) {
        if (packageId == null) {
            log.warn("[grantReferralRewardPackage] packageId 为空,跳过 userId={}", userId);
            return null;
        }
        PackageDO pkg = packageService.getPackage(packageId);
        if (pkg == null || !Boolean.TRUE.equals(pkg.getIsActive())) {
            log.warn("[grantReferralRewardPackage] 奖励套餐 id={} 不存在或已下架,跳过 userId={}",
                    packageId, userId);
            return null;
        }

        StudentPackageDO entity = buildPackageGrant(userId, pkg, StudentPackageSourceEnum.REFERRAL_REWARD);
        entity.setCreator("system");
        entity.setUpdater("system");
        studentPackageMapper.insert(entity);
        log.info("[grantReferralRewardPackage] grant 推荐奖励 userId={} packageId={} studentPackageId={}",
                userId, packageId, entity.getId());
        return entity.getId();
    }

    private StudentPackageDO buildPackageGrant(Long userId, PackageDO pkg, StudentPackageSourceEnum source) {
        StudentPackageDO entity = new StudentPackageDO();
        entity.setStudentId(userId);
        entity.setPackageId(pkg.getId());
        entity.setRemaining(pkg.getTotalCount());
        entity.setExpireAt(LocalDateTime.now().plusDays(pkg.getValidityDays()));
        entity.setSource(source.getCode());
        return entity;
    }

    @Override
    public boolean hasClaimedFreeTrial(Long studentId) {
        if (studentId == null) {
            return false;
        }
        Long count = studentPackageMapper.selectCount(
                Wrappers.<StudentPackageDO>lambdaQuery()
                        .eq(StudentPackageDO::getStudentId, studentId)
                        .in(StudentPackageDO::getSource,
                                StudentPackageSourceEnum.FREE_TRIAL.getCode(),
                                StudentPackageSourceEnum.REGISTER_GRANT.getCode()));
        return count != null && count > 0;
    }

}
