package org.lorislab.p6.common.rs;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RestLogContext {

    private String method;

    private String path;

    private String durationString;

    private String logger = RestLogContext.class.getName();

    private final long startTime;

    RestLogContext() {
        this.startTime = System.currentTimeMillis();
    }

    public void close() {
        var durationMillis = (System.currentTimeMillis() - startTime);
        var durationSec = BigDecimal.valueOf(durationMillis / 1000f).setScale(3, RoundingMode.HALF_DOWN).doubleValue();
        durationString = String.format("%.3f", durationSec);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getDurationString() {
        return durationString;
    }

    public String getLogger() {
        return logger;
    }
}
