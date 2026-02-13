package org.lorislab.p6.context;

public class ApplicationContext {

    private final String uuid;

    private ApplicationContext(String uuid) {
        this.uuid = uuid;
    }

    public String getUUID() {
        return uuid;
    }

    public static Builder builder(String uuid) {
        return new Builder().uuid(uuid);
    }

    public static class Builder {

        private String uuid;

        Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public ApplicationContext build() {
            return new ApplicationContext(uuid);
        }
    }
}
