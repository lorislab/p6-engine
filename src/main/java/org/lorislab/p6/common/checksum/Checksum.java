package org.lorislab.p6.common.checksum;

import java.util.zip.CRC32;

public final class Checksum {

    public static long checksum(byte[] data) {
        var c = new CRC32();
        c.update(data);
        return c.getValue();
    }

    private Checksum() {
    }
}
