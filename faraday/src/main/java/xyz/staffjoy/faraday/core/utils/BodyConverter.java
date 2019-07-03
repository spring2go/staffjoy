package xyz.staffjoy.faraday.core.utils;

import static java.nio.charset.Charset.forName;

public class BodyConverter {
    public static String convertBodyToString(byte[] body) {
        if (body == null) {
            return null;
        }
        return new String(body, forName("UTF-8"));
    }

    public static byte[] convertStringToBody(String body) {
        if (body == null) {
            return null;
        }
        return body.getBytes(forName("UTF-8"));
    }
}
