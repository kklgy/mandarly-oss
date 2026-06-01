package com.mandarly.boot.module.edu.service.user;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.user.vo.AdminUserFreezeReqVO;
import com.mandarly.boot.module.edu.controller.admin.user.vo.AdminUserPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.user.vo.AdminUserRespVO;

public interface AdminUserService {

    PageResult<AdminUserRespVO> getUserPage(AdminUserPageReqVO reqVO);

    AdminUserRespVO getUser(Long userId);

    void freezeUser(AdminUserFreezeReqVO reqVO, Long operatorAdminId);

}
