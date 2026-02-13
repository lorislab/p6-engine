package org.lorislab.p6.engine.domain.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "p6.engine")
public interface EngineConfig {

    /**
     * Job common configuration.
     */
    @WithName("job")
    JobConfig job();

    /**
     * Job common configuration.
     */
    interface JobConfig {

        /**
         * Job maximum retries.
         */
        @WithName("max-retries")
        @WithDefault("10")
        Integer maxRetries();
    }
}
