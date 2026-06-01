package com.mandarly.boot.framework.security.typehandler;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/**
 * AES-256-GCM 字段加密 TypeHandler，用于 PII 字段（如 teacher_withdrawal.payee_info）透明加解密。
 * <p>
 * 落库格式：{@code v1:{ivBase64}:{ctWithTagBase64}}，{@code v1:} 前缀为密钥版本号，为将来轮换预留。
 * <p>
 * 密钥来源：Spring 配置项 {@code mandarly.security.pii-aes-key}（32 字节 Base64，对应 AES-256）。
 */
public class AesEncryptTypeHandler extends BaseTypeHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(AesEncryptTypeHandler.class);

    private static final String KEY_PROPERTY_NAME = "mandarly.security.pii-aes-key";
    private static final String VERSION_PREFIX = "v1:";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;          // GCM 标准 IV 长度
    private static final int TAG_LENGTH_BITS = 128;   // GCM auth tag 长度

    private static final SecureRandom RANDOM = new SecureRandom();

    private static volatile SecretKeySpec keySpec;

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }

    public static String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            Base64.Encoder b64 = Base64.getEncoder();
            return VERSION_PREFIX + b64.encodeToString(iv) + ":" + b64.encodeToString(ct);
        } catch (Exception e) {
            throw new RuntimeException("AES encrypt failed", e);
        }
    }

    public static String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        try {
            if (!ciphertext.startsWith(VERSION_PREFIX)) {
                throw new IllegalArgumentException("unsupported ciphertext version");
            }
            String body = ciphertext.substring(VERSION_PREFIX.length());
            int sep = body.indexOf(':');
            if (sep < 0) {
                throw new IllegalArgumentException("malformed ciphertext");
            }
            Base64.Decoder b64 = Base64.getDecoder();
            byte[] iv = b64.decode(body.substring(0, sep));
            byte[] ct = b64.decode(body.substring(sep + 1));
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e) {
            String preview = ciphertext.length() > 16 ? ciphertext.substring(0, 16) : ciphertext;
            log.error("AES decrypt failed, ciphertext prefix={}", preview);
            throw new RuntimeException("AES decrypt failed", e);
        }
    }

    private static SecretKeySpec getKey() {
        SecretKeySpec local = keySpec;
        if (local != null) {
            return local;
        }
        String base64Key = SpringUtil.getProperty(KEY_PROPERTY_NAME);
        Assert.notEmpty(base64Key, "配置项({}) 不能为空", KEY_PROPERTY_NAME);
        byte[] raw = Base64.getDecoder().decode(base64Key);
        local = new SecretKeySpec(raw, "AES");
        keySpec = local;
        return local;
    }

}
