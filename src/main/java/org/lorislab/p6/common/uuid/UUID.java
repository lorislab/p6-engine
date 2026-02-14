package org.lorislab.p6.common.uuid;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class UUID {

    private static final SecureRandom GENERATOR = new SecureRandom();

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    private static byte[] random() {
        byte[] value = new byte[16];
        GENERATOR.nextBytes(value);
        var timestamp = System.currentTimeMillis();
        value[5] = (byte) timestamp;
        value[4] = (byte) (timestamp >> 8);
        value[3] = (byte) (timestamp >> 16);
        value[2] = (byte) (timestamp >> 24);
        value[1] = (byte) (timestamp >> 32);
        value[0] = (byte) (timestamp >> 40);

        value[6] = (byte) ((value[6] & 0x0F) | 0x70);
        value[8] = (byte) ((value[8] & 0x3F) | 0x80);
        return value;
    }

    private static String string(byte[] value) {
        byte[] h = new byte[36];
        toHex(value, 0, 4, h, 0);
        h[8] = '-';
        toHex(value, 4, 6, h, 9);
        h[13] = '-';
        toHex(value, 6, 8, h, 14);
        h[18] = '-';
        toHex(value, 8, 10, h, 19);
        h[23] = '-';
        toHex(value, 10, 16, h, 24);
        return new String(h, StandardCharsets.UTF_8);
    }

    private static void toHex(byte[] in, int fi, int ti, byte[] out, int to) {
        for (int i = fi, j = 0; i < ti; i++, j++) {
            var v = in[i] & 0xFF;
            out[to + j * 2] = HEX_ARRAY[v >>> 4];
            out[to + j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
    }

    public static String create() {
        return string(random());
    }

    private UUID() {
    }
}
