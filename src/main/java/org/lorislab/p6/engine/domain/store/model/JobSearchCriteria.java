package org.lorislab.p6.engine.domain.store.model;

import org.lorislab.quarkus.data.sql.PageRequest;

public class JobSearchCriteria {

    private String type;

    private String worker;

    private Job.Status status;

    private PageRequest pageRequest = PageRequest.ofSize(100);

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

    public Job.Status getStatus() {
        return status;
    }

    public void setStatus(Job.Status status) {
        this.status = status;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public void setPageRequest(PageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }

}
