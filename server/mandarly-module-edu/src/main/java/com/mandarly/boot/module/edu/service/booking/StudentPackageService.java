package com.mandarly.boot.module.edu.service.booking;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.StudentPackageGrantReqVO;
import com.mandarly.boot.module.edu.controller.admin.booking.vo.StudentPackagePageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.booking.StudentPackageDO;

import java.util.List;

/**
 * 学生持有的套餐 Service
 *
 * <p>对应 docs/database/02-packages-orders.md §1.2 / §1.5(扣次优先级)
 */
public interface StudentPackageService {

    PageResult<StudentPackageDO> getStudentPackagePage(StudentPackagePageReqVO reqVO);

    StudentPackageDO getStudentPackage(Long id);

    /**
     * 列出学生**仍可用**(remaining > 0 AND expire_at > now)的套餐,按到期升序
     */
    List<StudentPackageDO> listActiveByStudent(Long studentId);

    /**
     * 列出学生**全部**套餐(active + expired + exhausted),按 create_time 倒序;
     * S8 我的套餐(包含历史)用。
     */
    List<StudentPackageDO> listAllByStudent(Long studentId);

    /**
     * Admin 后台手动发放套餐(source=admin_grant);
     * 自动从 package 定义取 totalCount + validityDays,可被 reqVO 字段覆盖
     */
    Long grantPackage(StudentPackageGrantReqVO reqVO, Long operatorAdminId);

    /**
     * 注册时自动发放免费体验套餐(M2.5)。
     * source = 'register_grant';免费体验套餐 id 取 mandarly.app-auth.referral.free-trial-package-id
     * 幂等:如该 user 已有 source=register_grant 记录,跳过
     *
     * @return student_package.id;已发放过则返回既有记录 id;未能发放则返回 null
     */
    Long grantFreeTrialPackage(Long userId);

    /**
     * 推荐奖励发放套餐(source=referral_reward)。
     *
     * @return 新发放的 student_package.id;未能发放则返回 null
     */
    Long grantReferralRewardPackage(Long userId, Long packageId);

    /**
     * Wave 4 P1(2026-05-10):学生是否已领取过免费体验套餐。
     *
     * <p>判定:student_package 中存在 source IN ('free_trial', 'register_grant') 的记录(不区分是否过期 / 用完)。
     *   register_grant 是注册自动发放, free_trial 是手动领取入口, 两者均视为"已领过"。
     */
    boolean hasClaimedFreeTrial(Long studentId);

}
