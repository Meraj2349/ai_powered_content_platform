package com.example.skillmateai.user.utilities;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

@Component
public class GenerateAndValidateStringUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    public static String generateUniqueString() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateOtp(int length) {
        char[] otp = new char[length];
        for (int i = 0; i < length; i++) {
            otp[i] = ALPHANUMERIC[RANDOM.nextInt(ALPHANUMERIC.length)];
        }
        return new String(otp);
    }
}
