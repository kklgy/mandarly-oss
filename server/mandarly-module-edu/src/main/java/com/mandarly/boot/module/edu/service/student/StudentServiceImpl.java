package com.mandarly.boot.module.edu.service.student;

import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.student.vo.StudentFreezeReqVO;
import com.mandarly.boot.module.edu.controller.admin.student.vo.StudentPageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.enums.user.UserRoleEnum;
import com.mandarly.boot.module.edu.enums.user.UserStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StudentServiceImpl implements StudentService {

    @Resource
    private EduUserMapper eduUserMapper;

    @Override
    public PageResult<UserDO> getStudentPage(StudentPageReqVO reqVO) {
        return eduUserMapper.selectStudentPage(reqVO);
    }

    @Override
    public UserDO getStudent(Long userId) {
        UserDO user = eduUserMapper.selectById(userId);
        if (user == null) {
            throw ServiceExceptionUtil.exception0(404, "学生不存在");
        }
        if (!UserRoleEnum.STUDENT.getCode().equals(user.getRole())) {
            // 不是学生(可能是教师 user.id),按"学生不存在"语义返回,避免暴露身份
            throw ServiceExceptionUtil.exception0(404, "学生不存在");
        }
        return user;
    }

    @Override
    public void freezeStudent(StudentFreezeReqVO reqVO, Long operatorAdminId) {
        UserDO user = getStudent(reqVO.getUserId()); // 复用 role 校验

        boolean freeze = "freeze".equalsIgnoreCase(reqVO.getAction());
        boolean unfreeze = "unfreeze".equalsIgnoreCase(reqVO.getAction());
        if (!freeze && !unfreeze) {
            throw ServiceExceptionUtil.exception0(400, "action 必须是 freeze / unfreeze");
        }

        String newStatus = freeze ? UserStatusEnum.FROZEN.getCode() : UserStatusEnum.ACTIVE.getCode();
        if (newStatus.equals(user.getStatus())) {
            // 状态已经是目标状态,幂等通过
            log.info("[freezeStudent] userId={} 已是 {} 状态,幂等跳过", reqVO.getUserId(), newStatus);
            return;
        }

        user.setStatus(newStatus);
        eduUserMapper.updateById(user);

        log.info("[freezeStudent] userId={} action={} reason={} operator={}",
                reqVO.getUserId(), reqVO.getAction(), reqVO.getReason(), operatorAdminId);
    }
}
