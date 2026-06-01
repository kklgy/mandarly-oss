package com.mandarly.boot.module.edu.service.teacher;

import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.infra.api.file.FileApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TeacherProfileServiceImpl#presignIntroVideoUrl 单元测试(D19 续 introVideoUrl 出参签名 fix)。
 *
 * <p>背景:D19 P0 #2 fix 90950d2 只修了写入侧 normalize,读出侧 5 处出参点
 * (学生端列表/详情、教师本人 GET /me、admin 审核 page+get、等级测试结果)都把 DB object key
 * 直接当 URL 返,前端 video src 拼到 localhost:3001 域名 → 404 不能播放。
 *
 * <p>本测试覆盖:
 * <ol>
 *   <li>null / blank 输入 → 返 null,不调 fileApi</li>
 *   <li>object key 输入 → 走 fileApi.presignGetUrl,TTL 900,key 原样传</li>
 *   <li>历史脏数据(DB 残留完整 signed URL)→ 入口 normalize 剥 scheme+host+query 后再签</li>
 * </ol>
 */
class TeacherProfileServiceImplPresignVideoTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TeacherProfileServiceImpl teacherProfileService;

    @Mock
    private FileApi fileApi;

    /** case 1: null 输入直接返 null,不打扰 fileApi */
    @Test
    void presignIntroVideoUrl_nullInput_returnsNullWithoutCallingFileApi() {
        String result = teacherProfileService.presignIntroVideoUrl(null);

        assertThat(result).isNull();
        verify(fileApi, never()).presignGetUrl(anyString(), anyInt());
    }

    /** case 2: 空白字符串同样跳过 fileApi */
    @Test
    void presignIntroVideoUrl_blankInput_returnsNullWithoutCallingFileApi() {
        String result = teacherProfileService.presignIntroVideoUrl("   ");

        assertThat(result).isNull();
        verify(fileApi, never()).presignGetUrl(anyString(), anyInt());
    }

    /** case 3: 干净 object key → 透传给 fileApi 签名,TTL 900s */
    @Test
    void presignIntroVideoUrl_objectKey_callsPresignWithTtl900() {
        String key = "teacher/20260519/c520c22249f9e16a6adc6ec8a7a28b80.mp4";
        String signed = "https://mandarly-demo-bucket.cos.ap-hongkong.myqcloud.com/"
                + key + "?X-Amz-Signature=fakesig";
        when(fileApi.presignGetUrl(key, 900)).thenReturn(signed);

        String result = teacherProfileService.presignIntroVideoUrl(key);

        assertThat(result).isEqualTo(signed);
        ArgumentCaptor<Integer> ttlCap = ArgumentCaptor.forClass(Integer.class);
        verify(fileApi).presignGetUrl(org.mockito.ArgumentMatchers.eq(key), ttlCap.capture());
        assertThat(ttlCap.getValue()).isEqualTo(900);
    }

    /** case 4: 历史脏数据 — DB 残留完整 signed URL,入口再 normalize 一遍剥成 key 再签 */
    @Test
    void presignIntroVideoUrl_dirtySignedUrl_normalizesBeforeSigning() {
        String dirty = "https://mandarly-demo-bucket.cos.ap-hongkong.myqcloud.com/"
                + "teacher/20260515/abc.mp4?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20260515T120000Z";
        String cleanKey = "teacher/20260515/abc.mp4";
        String freshSigned = "https://mandarly-demo-bucket.cos.ap-hongkong.myqcloud.com/"
                + cleanKey + "?X-Amz-Signature=newsig";
        when(fileApi.presignGetUrl(cleanKey, 900)).thenReturn(freshSigned);

        String result = teacherProfileService.presignIntroVideoUrl(dirty);

        assertThat(result).isEqualTo(freshSigned);
        // 关键断言:fileApi 收到的是 normalize 后的 key,不是带 ? query 的 dirty 字符串
        verify(fileApi).presignGetUrl(cleanKey, 900);
    }
}
