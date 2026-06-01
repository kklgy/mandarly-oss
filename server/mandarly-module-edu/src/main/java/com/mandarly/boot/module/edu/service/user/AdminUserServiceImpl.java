package com.mandarly.boot.module.edu.service.user;

import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.user.vo.AdminUserFreezeReqVO;
import com.mandarly.boot.module.edu.controller.admin.user.vo.AdminUserPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.user.vo.AdminUserRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.EduUserOauthDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherProfileMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.UserOauthMapper;
import com.mandarly.boot.module.edu.enums.user.UserStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("eduAdminUserService")
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private EduUserMapper eduUserMapper;

    @Resource
    private TeacherProfileMapper teacherProfileMapper;

    @Resource
    private UserOauthMapper userOauthMapper;

    @Override
    public PageResult<AdminUserRespVO> getUserPage(AdminUserPageReqVO reqVO) {
        PageResult<UserDO> page = eduUserMapper.selectAdminUserPage(reqVO);
        return new PageResult<>(buildRespList(page.getList()), page.getTotal());
    }

    @Override
    public AdminUserRespVO getUser(Long userId) {
        UserDO user = eduUserMapper.selectById(userId);
        if (user == null) {
            throw ServiceExceptionUtil.exception0(404, "C 端用户不存在");
        }
        List<AdminUserRespVO> list = buildRespList(List.of(user));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void freezeUser(AdminUserFreezeReqVO reqVO, Long operatorAdminId) {
        UserDO user = eduUserMapper.selectById(reqVO.getUserId());
        if (user == null) {
            throw ServiceExceptionUtil.exception0(404, "C 端用户不存在");
        }

        boolean freeze = "freeze".equalsIgnoreCase(reqVO.getAction());
        boolean unfreeze = "unfreeze".equalsIgnoreCase(reqVO.getAction());
        if (!freeze && !unfreeze) {
            throw ServiceExceptionUtil.exception0(400, "action 必须是 freeze / unfreeze");
        }

        String newStatus = freeze ? UserStatusEnum.FROZEN.getCode() : UserStatusEnum.ACTIVE.getCode();
        if (newStatus.equals(user.getStatus())) {
            log.info("[freezeUser] userId={} 已是 {} 状态,幂等跳过", reqVO.getUserId(), newStatus);
            return;
        }

        user.setStatus(newStatus);
        eduUserMapper.updateById(user);

        log.info("[freezeUser] userId={} role={} action={} reason={} operator={}",
                reqVO.getUserId(), user.getRole(), reqVO.getAction(), reqVO.getReason(), operatorAdminId);
    }

    private List<AdminUserRespVO> buildRespList(List<UserDO> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> userIds = users.stream().map(UserDO::getId).toList();
        Map<Long, TeacherProfileDO> teacherProfileMap = buildTeacherProfileMap(userIds);
        Map<Long, List<EduUserOauthDO>> oauthMap = buildOauthMap(userIds);

        return users.stream().map(user -> {
            AdminUserRespVO vo = BeanUtils.toBean(user, AdminUserRespVO.class);
            TeacherProfileDO profile = teacherProfileMap.get(user.getId());
            if (profile != null) {
                vo.setTeacherAuditStatus(profile.getAuditStatus());
                vo.setTeacherAccent(profile.getAccent());
                vo.setTeacherYearsExperience(profile.getYearsExperience());
                vo.setTeacherExpertise(profile.getExpertise());
            }

            List<String> providers = oauthMap.getOrDefault(user.getId(), Collections.emptyList())
                    .stream()
                    .map(EduUserOauthDO::getProvider)
                    .distinct()
                    .toList();
            vo.setOauthProviders(providers);
            vo.setOauthSummary(providers.isEmpty() ? "未绑定" : String.join(" / ", providers));
            return vo;
        }).toList();
    }

    private Map<Long, TeacherProfileDO> buildTeacherProfileMap(List<Long> userIds) {
        Map<Long, TeacherProfileDO> map = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return map;
        }
        for (TeacherProfileDO profile : teacherProfileMapper.selectBatchIds(userIds)) {
            map.put(profile.getUserId(), profile);
        }
        return map;
    }

    private Map<Long, List<EduUserOauthDO>> buildOauthMap(List<Long> userIds) {
        Map<Long, List<EduUserOauthDO>> map = new HashMap<>();
        List<EduUserOauthDO> oauthList = userOauthMapper.selectActiveByUserIds(userIds);
        for (EduUserOauthDO oauth : oauthList) {
            map.computeIfAbsent(oauth.getUserId(), id -> new java.util.ArrayList<>()).add(oauth);
        }
        return map;
    }

}
