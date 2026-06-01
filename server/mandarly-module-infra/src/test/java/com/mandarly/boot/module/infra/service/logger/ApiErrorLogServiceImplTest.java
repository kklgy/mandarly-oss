package com.mandarly.boot.module.infra.service.logger;

import com.mandarly.boot.framework.common.enums.UserTypeEnum;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.test.core.ut.BaseDbUnitTest;
import com.mandarly.boot.framework.common.biz.infra.logger.dto.ApiErrorLogCreateReqDTO;
import com.mandarly.boot.module.infra.controller.admin.logger.vo.apierrorlog.ApiErrorLogPageReqVO;
import com.mandarly.boot.module.infra.dal.dataobject.logger.ApiErrorLogDO;
import com.mandarly.boot.module.infra.dal.mysql.logger.ApiErrorLogMapper;
import com.mandarly.boot.module.infra.enums.logger.ApiErrorLogProcessStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static com.mandarly.boot.framework.common.util.date.LocalDateTimeUtils.*;
import static com.mandarly.boot.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static com.mandarly.boot.framework.test.core.util.AssertUtils.assertPojoEquals;
import static com.mandarly.boot.framework.test.core.util.AssertUtils.assertServiceException;
import static com.mandarly.boot.framework.test.core.util.RandomUtils.randomLongId;
import static com.mandarly.boot.framework.test.core.util.RandomUtils.randomPojo;
import static com.mandarly.boot.module.infra.enums.ErrorCodeConstants.API_ERROR_LOG_NOT_FOUND;
import static com.mandarly.boot.module.infra.enums.ErrorCodeConstants.API_ERROR_LOG_PROCESSED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(ApiErrorLogServiceImpl.class)
public class ApiErrorLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ApiErrorLogServiceImpl apiErrorLogService;

    @Resource
    private ApiErrorLogMapper apiErrorLogMapper;

    @Test
    public void testGetApiErrorLogPage() {
        // mock 数据
        ApiErrorLogDO apiErrorLogDO = randomPojo(ApiErrorLogDO.class, o -> {
            o.setUserId(2233L);
            o.setUserType(UserTypeEnum.ADMIN.getValue());
            o.setApplicationName("mandarly-test");
            o.setRequestUrl("foo");
            o.setExceptionTime(buildTime(2021, 3, 13));
            o.setProcessStatus(ApiErrorLogProcessStatusEnum.INIT.getStatus());
        });
        apiErrorLogMapper.insert(apiErrorLogDO);
        // 测试 userId 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, o -> o.setUserId(3344L)));
        // 测试 userType 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, o -> o.setUserType(UserTypeEnum.MEMBER.getValue())));
        // 测试 applicationName 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, o -> o.setApplicationName("test")));
        // 测试 requestUrl 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, o -> o.setRequestUrl("bar")));
        // 测试 exceptionTime 不匹配：构造一个早期时间 2021-02-06 00:00:00
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, o -> o.setExceptionTime(buildTime(2021, 2, 6))));
        // 测试 progressStatus 不匹配
        apiErrorLogMapper.insert(cloneIgnoreId(apiErrorLogDO, logDO -> logDO.setProcessStatus(ApiErrorLogProcessStatusEnum.DONE.getStatus())));
        // 准备参数
        ApiErrorLogPageReqVO reqVO = new ApiErrorLogPageReqVO();
        reqVO.setUserId(2233L);
        reqVO.setUserType(UserTypeEnum.ADMIN.getValue());
        reqVO.setApplicationName("mandarly-test");
        reqVO.setRequestUrl("foo");
        reqVO.setExceptionTime(buildBetweenTime(2021, 3, 1, 2021, 3, 31));
        reqVO.setProcessStatus(ApiErrorLogProcessStatusEnum.INIT.getStatus());

        // 调用
        PageResult<ApiErrorLogDO> pageResult = apiErrorLogService.getApiErrorLogPage(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(apiErrorLogDO, pageResult.getList().get(0));
    }

    @Test
    public void testCreateApiErrorLog() {
        // 准备参数
        ApiErrorLogCreateReqDTO createDTO = randomPojo(ApiErrorLogCreateReqDTO.class);

        // 调用
        apiErrorLogService.createApiErrorLog(createDTO);
        // 断言
        ApiErrorLogDO apiErrorLogDO = apiErrorLogMapper.selectOne(null);
        assertPojoEquals(createDTO, apiErrorLogDO);
        assertEquals(ApiErrorLogProcessStatusEnum.INIT.getStatus(), apiErrorLogDO.getProcessStatus());
    }

    @Test
    public void testCreateApiErrorLog_defaultsMissingExceptionLocation() {
        // 准备参数:ServiceException 等短链路可能缺少异常定位字段,DB 应兜底而不是让日志记录失败
        ApiErrorLogCreateReqDTO createDTO = randomPojo(ApiErrorLogCreateReqDTO.class, o -> {
            o.setExceptionClassName(null);
            o.setExceptionFileName(null);
            o.setExceptionMethodName(null);
            o.setExceptionLineNumber(null);
        });

        // 调用
        apiErrorLogService.createApiErrorLog(createDTO);

        // 断言
        ApiErrorLogDO apiErrorLogDO = apiErrorLogMapper.selectOne(null);
        assertEquals("", apiErrorLogDO.getExceptionClassName());
        assertEquals("", apiErrorLogDO.getExceptionFileName());
        assertEquals("", apiErrorLogDO.getExceptionMethodName());
        assertEquals(0, apiErrorLogDO.getExceptionLineNumber());
        assertEquals(ApiErrorLogProcessStatusEnum.INIT.getStatus(), apiErrorLogDO.getProcessStatus());
    }

    @Test
    public void testUpdateApiErrorLogProcess_success() {
        // 准备参数
        ApiErrorLogDO apiErrorLogDO = randomPojo(ApiErrorLogDO.class,
                o -> o.setProcessStatus(ApiErrorLogProcessStatusEnum.INIT.getStatus()));
        apiErrorLogMapper.insert(apiErrorLogDO);
        // 准备参数
        Long id = apiErrorLogDO.getId();
        Integer processStatus = randomEle(ApiErrorLogProcessStatusEnum.values()).getStatus();
        Long processUserId = randomLongId();

        // 调用
        apiErrorLogService.updateApiErrorLogProcess(id, processStatus, processUserId);
        // 断言
        ApiErrorLogDO dbApiErrorLogDO = apiErrorLogMapper.selectById(apiErrorLogDO.getId());
        assertEquals(processStatus, dbApiErrorLogDO.getProcessStatus());
        assertEquals(processUserId, dbApiErrorLogDO.getProcessUserId());
        assertNotNull(dbApiErrorLogDO.getProcessTime());
    }

    @Test
    public void testUpdateApiErrorLogProcess_processed() {
        // 准备参数
        ApiErrorLogDO apiErrorLogDO = randomPojo(ApiErrorLogDO.class,
                o -> o.setProcessStatus(ApiErrorLogProcessStatusEnum.DONE.getStatus()));
        apiErrorLogMapper.insert(apiErrorLogDO);
        // 准备参数
        Long id = apiErrorLogDO.getId();
        Integer processStatus = randomEle(ApiErrorLogProcessStatusEnum.values()).getStatus();
        Long processUserId = randomLongId();

        // 调用，并断言异常
        assertServiceException(() ->
                apiErrorLogService.updateApiErrorLogProcess(id, processStatus, processUserId),
                API_ERROR_LOG_PROCESSED);
    }

    @Test
    public void testUpdateApiErrorLogProcess_notFound() {
        // 准备参数
        Long id = randomLongId();
        Integer processStatus = randomEle(ApiErrorLogProcessStatusEnum.values()).getStatus();
        Long processUserId = randomLongId();

        // 调用，并断言异常
        assertServiceException(() ->
                apiErrorLogService.updateApiErrorLogProcess(id, processStatus, processUserId),
                API_ERROR_LOG_NOT_FOUND);
    }

    @Test
    public void testCleanJobLog() {
        // mock 数据
        ApiErrorLogDO log01 = randomPojo(ApiErrorLogDO.class, o -> o.setCreateTime(addTime(Duration.ofDays(-3))));
        apiErrorLogMapper.insert(log01);
        ApiErrorLogDO log02 = randomPojo(ApiErrorLogDO.class, o -> o.setCreateTime(addTime(Duration.ofDays(-1))));
        apiErrorLogMapper.insert(log02);
        // 准备参数
        Integer exceedDay = 2;
        Integer deleteLimit = 1;

        // 调用
        Integer count = apiErrorLogService.cleanErrorLog(exceedDay, deleteLimit);
        // 断言
        assertEquals(1, count);
        List<ApiErrorLogDO> logs = apiErrorLogMapper.selectList();
        assertEquals(1, logs.size());
        // TODO @芋艿：createTime updateTime 被屏蔽，仅 win11 会复现，建议后续修复。
        assertPojoEquals(log02, logs.get(0), "createTime", "updateTime");
    }

}
