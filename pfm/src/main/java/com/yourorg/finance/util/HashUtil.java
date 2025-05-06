package com.yourorg.finance.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class HashUtil {
    // hash a plain‑text password
    public static String hash(String plain) {
        // strength 10 is the default
        return BCrypt.withDefaults()
                .hashToString(12, plain.toCharArray());
    }

    // verify a plain‑text password against a stored bcrypt hash
    public static boolean check(String plain, String bcryptHash) {
        BCrypt.Result result = BCrypt.verifyer().verify(
                plain.toCharArray(),
                bcryptHash
        );
        return result.verified;
    }
}
