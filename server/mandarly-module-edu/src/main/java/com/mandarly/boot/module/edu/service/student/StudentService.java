package com.mandarly.boot.module.edu.service.student;

import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.student.vo.StudentFreezeReqVO;
import com.mandarly.boot.module.edu.controller.admin.student.vo.StudentPageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;

/**
 * 学生 Service 接口
 *
 * <p>学生数据复用 user 表(role='student'),不另起表。
 */
public interface StudentService {

    /**
     * Admin 后台 — 学生分页查询
     */
    PageResult<UserDO> getStudentPage(StudentPageReqVO reqVO);

    /**
     * 获取单个学生详情(role 必须为 student,否则报错)
     */
    UserDO getStudent(Long userId);

    /**
     * Admin 冻结 / 解冻学生
     */
    void freezeStudent(StudentFreezeReqVO reqVO, Long operatorAdminId);
}
