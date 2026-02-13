package org.lorislab.p6.engine.domain.store.model;

public class JobActiveCriteria {
    private String type;
    private String worker;
    private long timeout;
    private int maxJobsToActivate;
    private int maxRetries;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getMaxJobsToActivate() {
        return maxJobsToActivate;
    }

    public void setMaxJobsToActivate(int maxJobsToActivate) {
        this.maxJobsToActivate = maxJobsToActivate;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
}
