package com.mandarly.boot.module.edu.dal.mysql.user;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.mybatis.core.mapper.BaseMapperX;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.user.vo.AdminUserPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.student.vo.StudentPageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.enums.user.UserRoleEnum;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.util.StringUtils;

@Mapper
public interface EduUserMapper extends BaseMapperX<UserDO> {

    default UserDO selectByEmail(String email) {
        return selectOne(UserDO::getEmail, email);
    }

    default UserDO selectByPhone(String phone) {
        return selectOne(UserDO::getPhone, phone);
    }

    default UserDO selectByReferralCode(String referralCode) {
        return selectOne(UserDO::getReferralCode, referralCode);
    }

    /**
     * Admin 后台 — 学生分页查询(role 强制 = student)
     */
    default PageResult<UserDO> selectStudentPage(StudentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserDO>()
                .eq(UserDO::getRole, UserRoleEnum.STUDENT.getCode())
                .likeIfPresent(UserDO::getNickname, reqVO.getNickname())
                .likeIfPresent(UserDO::getEmail, reqVO.getEmail())
                .likeIfPresent(UserDO::getPhone, reqVO.getPhone())
                .eqIfPresent(UserDO::getStatus, reqVO.getStatus())
                .eqIfPresent(UserDO::getLocale, reqVO.getLocale())
                .orderByDesc(UserDO::getCreateTime));
    }

    /**
     * Admin 后台 — C 端用户分页查询(role 可选:student / teacher)。
     */
    default PageResult<UserDO> selectAdminUserPage(AdminUserPageReqVO reqVO) {
        LambdaQueryWrapperX<UserDO> query = new LambdaQueryWrapperX<UserDO>()
                .eqIfPresent(UserDO::getRole, reqVO.getRole())
                .eqIfPresent(UserDO::getStatus, reqVO.getStatus())
                .eqIfPresent(UserDO::getLocale, reqVO.getLocale())
                .likeIfPresent(UserDO::getNickname, reqVO.getNickname())
                .likeIfPresent(UserDO::getEmail, reqVO.getEmail())
                .likeIfPresent(UserDO::getPhone, reqVO.getPhone());
        if (StringUtils.hasText(reqVO.getKeyword())) {
            String keyword = reqVO.getKeyword().trim();
            query.and(q -> q.like(UserDO::getNickname, keyword)
                    .or().like(UserDO::getEmail, keyword)
                    .or().like(UserDO::getPhone, keyword)
                    .or().like(UserDO::getReferralCode, keyword));
        }
        return selectPage(reqVO, query.orderByDesc(UserDO::getCreateTime));
    }

}
