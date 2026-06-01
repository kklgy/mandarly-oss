package com.mandarly.boot.framework.security.typehandler;

import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

/**
 * {@link AesEncryptTypeHandler} 的单元测试
 */
public class AesEncryptTypeHandlerTest {

    // 固定测试密钥 A：32 字节随机数据的 Base64（仅用于测试，非生产凭据）
    private static final String TEST_KEY_A = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    // 固定测试密钥 B：与 A 不同，用于 wrong_key_throws 用例
    private static final String TEST_KEY_B = "ZmVkY2JhOTg3NjU0MzIxMGZlZGNiYTk4NzY1NDMyMTA=";

    private static final String PROPERTY_NAME = "mandarly.security.pii-aes-key";

    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() throws Exception {
        // 每个测试前重置 handler 静态缓存，避免跨用例污染
        resetKeyCache();
        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getProperty(PROPERTY_NAME)).thenReturn(TEST_KEY_A);
    }

    @AfterEach
    void tearDown() throws Exception {
        springUtilMock.close();
        resetKeyCache();
    }

    /**
     * 通过反射重置 AesEncryptTypeHandler 的静态 keySpec 缓存，
     * 使测试可在用例间切换密钥。
     */
    private void resetKeyCache() throws Exception {
        java.lang.reflect.Field field = AesEncryptTypeHandler.class.getDeclaredField("keySpec");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    void encrypt_then_decrypt_roundtrip() {
        // case 1: 普通 ASCII + 中文
        String plain1 = "微信号:wxid_abc123";
        String enc1 = AesEncryptTypeHandler.encrypt(plain1);
        assertEquals(plain1, AesEncryptTypeHandler.decrypt(enc1));

        // case 2: emoji
        String plain2 = "支付宝 💰 alipay@example.com 🎉";
        String enc2 = AesEncryptTypeHandler.encrypt(plain2);
        assertEquals(plain2, AesEncryptTypeHandler.decrypt(enc2));

        // case 3: 长文本（~2000 字符）
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("银行卡 6225 1234 5678 9012 户名张三 ");
        }
        String plain3 = sb.toString();
        String enc3 = AesEncryptTypeHandler.encrypt(plain3);
        assertEquals(plain3, AesEncryptTypeHandler.decrypt(enc3));
    }

    @Test
    void format_is_v1_iv_ct_base64() {
        String enc = AesEncryptTypeHandler.encrypt("微信号:wxid_abc123");
        assertTrue(enc.matches("^v1:[A-Za-z0-9+/=]+:[A-Za-z0-9+/=]+$"),
                "ciphertext format expected v1:{ivBase64}:{ctBase64}, got: " + enc);
    }

    @Test
    void iv_is_random_per_call() {
        String plain = "微信号:wxid_abc123";
        String enc1 = AesEncryptTypeHandler.encrypt(plain);
        String enc2 = AesEncryptTypeHandler.encrypt(plain);
        assertNotEquals(enc1, enc2, "two encryptions of same plaintext must differ (random IV)");
    }

    @Test
    void wrong_key_throws() throws Exception {
        // 用 key A 加密
        String enc = AesEncryptTypeHandler.encrypt("微信号:wxid_abc123");

        // 切换到 key B，重新解密应该失败
        resetKeyCache();
        springUtilMock.when(() -> SpringUtil.getProperty(PROPERTY_NAME)).thenReturn(TEST_KEY_B);

        assertThrows(RuntimeException.class, () -> AesEncryptTypeHandler.decrypt(enc));
    }

}
