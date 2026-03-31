package com.qfedu.smartcampusseckill.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * MD5 工具类（用于管理员登录密码校验）。
 */
public class Md5Util {

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String s = Integer.toHexString(b & 0xff);
                if (s.length() == 1) {
                    sb.append('0');
                }
                sb.append(s);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 计算失败", e);
        }
    }
}

