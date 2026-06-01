package com.mandarly.boot.module.system.service.logger;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.mandarly.boot.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import com.mandarly.boot.module.system.dal.dataobject.logger.LoginLogDO;
import com.mandarly.boot.module.system.dal.mysql.logger.LoginLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 登录日志 Service 实现
 */
@Service
@Validated
public class LoginLogServiceImpl implements LoginLogService {

    @Resource
    private LoginLogMapper loginLogMapper;

    @Override
    public LoginLogDO getLoginLog(Long id) {
        return loginLogMapper.selectById(id);
    }

    @Override
    public PageResult<LoginLogDO> getLoginLogPage(LoginLogPageReqVO pageReqVO) {
        return loginLogMapper.selectPage(pageReqVO);
    }

    @Override
    public void createLoginLog(LoginLogCreateReqDTO reqDTO) {
        LoginLogDO loginLog = BeanUtils.toBean(reqDTO, LoginLogDO.class);
        loginLogMapper.insert(loginLog);
    }

}
