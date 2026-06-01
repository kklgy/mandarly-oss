package com.mandarly.boot.module.edu.controller.admin.referral;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.referral.vo.ReferralRecordPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.referral.vo.ReferralRecordRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.referral.ReferralRecordDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.referral.ReferralRecordMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 推荐记录看板(只读)
 *
 * <p>对应 PRD-v4 §5.4 admin 推荐码管理。JOIN user 表展示 referrer/referee 邮箱。
 */
@Tag(name = "管理后台 - 推荐记录")
@RestController
@RequestMapping("/edu/referral")
@Validated
public class ReferralRecordController {

    @Resource
    private ReferralRecordMapper referralRecordMapper;

    @Resource
    private EduUserMapper eduUserMapper;

    @GetMapping("/page")
    @Operation(summary = "推荐记录分页查询(含 referrer/referee 邮箱)")
    @PreAuthorize("@ss.hasPermission('edu:referral:query')")
    public CommonResult<PageResult<ReferralRecordRespVO>> getPage(@Valid ReferralRecordPageReqVO reqVO) {
        // 1. 分页查 referral_record
        PageResult<ReferralRecordDO> page = referralRecordMapper.selectPage(reqVO,
                new LambdaQueryWrapperX<ReferralRecordDO>()
                        .eqIfPresent(ReferralRecordDO::getReferrerUserId, reqVO.getReferrerUserId())
                        .eqIfPresent(ReferralRecordDO::getRefereeUserId, reqVO.getRefereeUserId())
                        .eqIfPresent(ReferralRecordDO::getStatus, reqVO.getStatus())
                        .orderByDesc(ReferralRecordDO::getCreateTime));

        if (page.getList().isEmpty()) {
            return success(new PageResult<>(List.of(), page.getTotal()));
        }

        // 2. 批量查 user 邮箱(referrer + referee)
        Set<Long> userIds = page.getList().stream()
                .flatMap(r -> {
                    java.util.List<Long> ids = new java.util.ArrayList<>();
                    if (r.getReferrerUserId() != null) ids.add(r.getReferrerUserId());
                    if (r.getRefereeUserId() != null) ids.add(r.getRefereeUserId());
                    return ids.stream();
                })
                .collect(Collectors.toSet());
        Map<Long, String> emailById = eduUserMapper.selectList(UserDO::getId, userIds)
                .stream().collect(Collectors.toMap(UserDO::getId, u -> u.getEmail() != null ? u.getEmail() : ""));

        // 3. 组装 VO
        List<ReferralRecordRespVO> list = page.getList().stream().map(r -> toRespVO(r, emailById)).toList();
        return success(new PageResult<>(list, page.getTotal()));
    }

    // ======================== private ========================

    private ReferralRecordRespVO toRespVO(ReferralRecordDO r, Map<Long, String> emailById) {
        ReferralRecordRespVO vo = new ReferralRecordRespVO();
        vo.setId(r.getId());
        vo.setReferrerUserId(r.getReferrerUserId());
        vo.setReferrerEmail(emailById.getOrDefault(r.getReferrerUserId(), ""));
        vo.setRefereeUserId(r.getRefereeUserId());
        vo.setRefereeEmail(emailById.getOrDefault(r.getRefereeUserId(), ""));
        vo.setReferralCode(r.getReferralCode());
        vo.setPaymentId(r.getPaymentId());
        vo.setRefereeDiscountAmountUsd(r.getRefereeDiscountAmountUsd());
        vo.setReferrerRewardPackageId(r.getReferrerRewardPackageId());
        vo.setStatus(r.getStatus());
        vo.setBoundAt(r.getBoundAt());
        vo.setRewardedAt(r.getRewardedAt());
        vo.setCreateTime(r.getCreateTime());
        return vo;
    }
}
