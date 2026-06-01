package com.mandarly.boot.module.edu.service.teacher;

import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherQualificationDO;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherQualificationMapper;
import com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum;
import com.mandarly.boot.module.infra.api.file.FileApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 教师资质材料 Service 实现(D19 Phase B Task B2)
 *
 * <p>跨模块依赖:通过 {@link FileApi}(infra 模块对外暴露的 facade)调用
 * presignGetUrl 生成签名 URL。沿用 edu 模块既有 infra.api.* 引用模式
 * (参见 ReferralServiceImpl / PaymentServiceImpl)。
 */
@Service
@Slf4j
public class TeacherQualificationServiceImpl implements TeacherQualificationService {

    /**
     * 预签名 URL TTL(秒)— 15 分钟,审核台一次性查看 / 下载够用
     */
    private static final int PRESIGNED_URL_TTL_SECONDS = 900;

    @Resource
    private TeacherQualificationMapper qualificationMapper;

    @Resource
    private FileApi fileApi;

    @Override
    public Long uploadQualification(Long userId, String docType, String docUrl, String docFilename) {
        TeacherQualificationDO row = new TeacherQualificationDO();
        row.setUserId(userId);
        row.setDocType(docType);
        // prod 2026-05-18 实战:/infra/file/upload 返完整签名 URL(含 ?X-Amz-...),
        // 教师端直接存 → admin 再签时 Hutool UrlBuilder 把 path-only 当 file URL 补 cwd /app/ → 403。
        // 入口 normalize 成 object key 防脏数据;DB 存 key,GET 时统一走 fileApi.presignGetUrl 重新签。
        row.setDocUrl(normalizeToObjectKey(docUrl));
        row.setDocFilename(docFilename);
        // 显式落 pending(虽 DDL 已 DEFAULT,但显式更稳,防 JPA / MP 默认值漂移)
        row.setAuditStatus(TeacherAuditStatusEnum.PENDING.getCode());
        qualificationMapper.insert(row);
        log.info("[uploadQualification] userId={} docType={} id={} key={}",
                userId, docType, row.getId(), row.getDocUrl());
        return row.getId();
    }

    /**
     * 把上传 API 返的 signed URL 规范化成 COS object key:剥 scheme://host[:port] + query string。
     *
     * <p>例:
     * <ul>
     *   <li>{@code https://bucket.cos.region.com/20260518/cert.jpg?X-Amz-...} → {@code 20260518/cert.jpg}</li>
     *   <li>{@code 20260518/cert.jpg} → 不变(已是 object key)</li>
     * </ul>
     */
    static String normalizeToObjectKey(String url) {
        if (url == null || url.isBlank()) return url;
        if (!url.contains("://")) return url;
        int schemeEnd = url.indexOf("://");
        int pathStart = url.indexOf('/', schemeEnd + 3);
        if (pathStart < 0) return url;
        String afterDomain = url.substring(pathStart + 1);
        int q = afterDomain.indexOf('?');
        return q >= 0 ? afterDomain.substring(0, q) : afterDomain;
    }

    @Override
    public List<TeacherQualificationDO> listByUserId(Long userId) {
        return qualificationMapper.selectListByUserId(userId);
    }

    @Override
    public void deleteByUserAndId(Long userId, Long id) {
        TeacherQualificationDO row = qualificationMapper.selectById(id);
        if (row == null) {
            // 显式 404 而非 silently return:admin / educator workflow 希望明确感知 "已被另一会话删了 / id 拼错了"
            throw ServiceExceptionUtil.exception0(404, "资质材料不存在");
        }
        if (!userId.equals(row.getUserId())) {
            // owner check 严格用 equals(Long 装箱不能 == 比较)
            throw ServiceExceptionUtil.exception0(403, "无权限删除他人的资质材料");
        }
        qualificationMapper.deleteById(id);
        log.info("[deleteByUserAndId] userId={} qualificationId={} deleted", userId, id);
    }

    @Override
    public String generatePresignedUrl(String docUrl) {
        if (docUrl == null || docUrl.isBlank()) {
            return null;
        }
        // 兼容历史脏数据(DB 里存了完整 signed URL 而非 object key):入口再 normalize 一遍。
        // 新数据已在 uploadQualification 入口 normalize,这里 no-op。
        return fileApi.presignGetUrl(normalizeToObjectKey(docUrl), PRESIGNED_URL_TTL_SECONDS);
    }

}
