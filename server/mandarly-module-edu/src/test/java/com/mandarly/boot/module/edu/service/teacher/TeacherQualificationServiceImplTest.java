package com.mandarly.boot.module.edu.service.teacher;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherQualificationDO;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherQualificationMapper;
import com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum;
import com.mandarly.boot.module.infra.api.file.FileApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * TeacherQualificationServiceImpl 单元测试(D19 Phase B Task B2)
 *
 * <p>覆盖 7 个 case:
 * <ol>
 *   <li>uploadQualification 显式落 pending 状态</li>
 *   <li>listByUserId 透传给 Mapper</li>
 *   <li>deleteByUserAndId 不存在 → 404</li>
 *   <li>deleteByUserAndId 他人 → 403</li>
 *   <li>deleteByUserAndId owner → 删除成功</li>
 *   <li>generatePresignedUrl 转 fileApi 带 900s TTL</li>
 *   <li>generatePresignedUrl blank 输入 → 返 null,fileApi 不调用</li>
 * </ol>
 */
class TeacherQualificationServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TeacherQualificationServiceImpl service;

    @Mock
    private TeacherQualificationMapper qualificationMapper;

    @Mock
    private FileApi fileApi;

    // ======================== uploadQualification ========================

    @Test
    void uploadQualification_persistsRowWithPendingStatus() {
        ArgumentCaptor<TeacherQualificationDO> captor =
                ArgumentCaptor.forClass(TeacherQualificationDO.class);

        // prod 2026-05-18:/infra/file/upload 返完整 signed URL,uploadQualification 入口
        // normalize 成 object key 防 admin 再签时 Hutool UrlBuilder 翻车
        service.uploadQualification(99001L, "id_card",
                "https://bucket.cos.region.com/20260518/id.jpg?X-Amz-Algorithm=AWS4&X-Amz-Signature=abc",
                "id.jpg");

        verify(qualificationMapper).insert(captor.capture());
        TeacherQualificationDO row = captor.getValue();
        assertEquals(99001L, row.getUserId());
        assertEquals("id_card", row.getDocType());
        // 关键回归:DB 存的是 object key,不是 signed URL
        assertEquals("20260518/id.jpg", row.getDocUrl());
        assertEquals("id.jpg", row.getDocFilename());
        assertEquals(TeacherAuditStatusEnum.PENDING.getCode(), row.getAuditStatus());
    }

    @Test
    void normalizeToObjectKey_variousFormats() {
        // signed URL with query → key
        assertEquals("20260518/cert.jpg",
                TeacherQualificationServiceImpl.normalizeToObjectKey(
                        "https://bucket.cos.region.com/20260518/cert.jpg?X-Amz-Algorithm=AWS4&X-Amz-Signature=abc"));
        // bare URL (no query) → key
        assertEquals("path/to/file.pdf",
                TeacherQualificationServiceImpl.normalizeToObjectKey(
                        "https://bucket.cos.region.com/path/to/file.pdf"));
        // already a key → unchanged
        assertEquals("20260518/cert.jpg",
                TeacherQualificationServiceImpl.normalizeToObjectKey("20260518/cert.jpg"));
        // null / blank
        assertNull(TeacherQualificationServiceImpl.normalizeToObjectKey(null));
        assertEquals("", TeacherQualificationServiceImpl.normalizeToObjectKey(""));
    }

    // ======================== listByUserId ========================

    @Test
    void listByUserId_delegatesToMapper() {
        TeacherQualificationDO row = new TeacherQualificationDO();
        when(qualificationMapper.selectListByUserId(99001L))
                .thenReturn(Collections.singletonList(row));

        List<TeacherQualificationDO> result = service.listByUserId(99001L);

        assertEquals(1, result.size());
        verify(qualificationMapper).selectListByUserId(99001L);
    }

    // ======================== deleteByUserAndId ========================

    @Test
    void deleteByUserAndId_notFound_throws404() {
        when(qualificationMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.deleteByUserAndId(99001L, 1L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("资质材料不存在");

        verify(qualificationMapper, never()).deleteById(any());
    }

    @Test
    void deleteByUserAndId_otherUser_throws403() {
        TeacherQualificationDO row = new TeacherQualificationDO();
        row.setId(1L);
        row.setUserId(99002L); // 别人的资质
        when(qualificationMapper.selectById(1L)).thenReturn(row);

        assertThatThrownBy(() -> service.deleteByUserAndId(99001L, 1L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("无权限");

        verify(qualificationMapper, never()).deleteById(any());
    }

    @Test
    void deleteByUserAndId_owner_deletes() {
        TeacherQualificationDO row = new TeacherQualificationDO();
        row.setId(1L);
        row.setUserId(99001L);
        when(qualificationMapper.selectById(1L)).thenReturn(row);

        service.deleteByUserAndId(99001L, 1L);

        verify(qualificationMapper).deleteById(1L);
    }

    // ======================== generatePresignedUrl ========================

    @Test
    void generatePresignedUrl_delegatesToFileApiWith900SecondsTtl() {
        // new data already normalized to object key — pass through
        when(fileApi.presignGetUrl("20260518/cert.jpg", 900))
                .thenReturn("https://signed/20260518/cert.jpg?sig=abc");

        String result = service.generatePresignedUrl("20260518/cert.jpg");

        assertEquals("https://signed/20260518/cert.jpg?sig=abc", result);
        verify(fileApi).presignGetUrl("20260518/cert.jpg", 900);
    }

    @Test
    void generatePresignedUrl_legacyDirtyUrl_normalizedBeforeSign() {
        // 历史脏数据兼容:DB 里残留完整 signed URL,generatePresignedUrl 入口 normalize 后再签
        when(fileApi.presignGetUrl("20260518/cert.jpg", 900))
                .thenReturn("https://signed/20260518/cert.jpg?sig=fresh");

        String result = service.generatePresignedUrl(
                "https://bucket.cos.region.com/20260518/cert.jpg?X-Amz-Signature=stale");

        assertEquals("https://signed/20260518/cert.jpg?sig=fresh", result);
        verify(fileApi).presignGetUrl("20260518/cert.jpg", 900);
    }

    @Test
    void generatePresignedUrl_blankInput_returnsNull() {
        assertNull(service.generatePresignedUrl(null));
        assertNull(service.generatePresignedUrl(""));
        assertNull(service.generatePresignedUrl("  "));
        verifyNoInteractions(fileApi);
    }

    // 顺便 sanity check:Service 通过 BaseMockitoUnitTest 注入完整
    @Test
    void sanity_serviceInstantiated() {
        assertThat(service).isNotNull();
    }
}
