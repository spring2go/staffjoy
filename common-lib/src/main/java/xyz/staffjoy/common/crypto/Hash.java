package xyz.staffjoy.common.crypto;


import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Hash {
    public static String encode(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return Hex.toHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
    }
}
