package com.mandarly.boot.module.edu.service.teacher;

import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherQualificationDO;

import java.util.List;

/**
 * 教师资质材料 Service 接口(D19 Phase B)
 *
 * <p>对应 docs/database/01-users-auth.md §4.4 + DDL teacher_qualification。
 * 一个教师可上传多份资质(身份证 / 学历 / 普通话证书 / 教师资格证 / 其他),
 * 每份独立 audit_status,由 admin 在 D19-A 已完成的审核台中逐条审核(B11 复用)。
 *
 * <p>读取时通过 {@link #generatePresignedUrl(String)} 走 FileApi 签名,
 * 不直接返回 COS 原始 URL(隐私 + 防外链)。
 */
public interface TeacherQualificationService {

    /**
     * 教师上传一份资质材料(落 pending 行)。
     *
     * <p>文件本身已先通过 infra 模块 /admin-api/infra/file/upload 上传到 COS,
     * 本方法只做 DB 记录,docUrl 是 infra 返回的访问地址(可签名)。
     *
     * @param userId      教师 user.id(同 teacher_profile.user_id)
     * @param docType     文件类型(id_card / education / mandarin_cert / teacher_cert / other)
     * @param docUrl      COS 完整访问 URL(由 fileApi.createFile 返回)
     * @param docFilename 原始文件名(展示用)
     * @return 新增行 id
     */
    Long uploadQualification(Long userId, String docType, String docUrl, String docFilename);

    /**
     * 按 user.id 列出该教师所有资质材料(创建时间倒序)。
     */
    List<TeacherQualificationDO> listByUserId(Long userId);

    /**
     * 按 owner 删除一条资质材料(owner check 严格,他人禁删)。
     *
     * <p>不存在 → 抛 404;owner 不匹配 → 抛 403。
     *
     * @param userId 当前登录教师 user.id(从 SecurityFrameworkUtils 拿)
     * @param id     资质记录主键
     */
    void deleteByUserAndId(Long userId, Long id);

    /**
     * 为一条 COS docUrl 生成 15 分钟 TTL 的预签名只读 URL,前端 img / 下载用。
     *
     * <p>空/blank 输入返回 null(不抛错,方便上层 stream-map 处理)。
     *
     * @param docUrl COS 完整访问 URL(对应 DO.docUrl)
     * @return 预签名 URL(900s TTL),输入空时返回 null
     */
    String generatePresignedUrl(String docUrl);

}
