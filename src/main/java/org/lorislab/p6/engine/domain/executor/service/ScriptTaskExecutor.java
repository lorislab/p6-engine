package org.lorislab.p6.engine.domain.executor.service;

import jakarta.enterprise.context.ApplicationScoped;

import org.lorislab.p6.bpmn2.model.ScriptTask;
import org.lorislab.p6.engine.domain.models.TokenEventContext;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ScriptTaskExecutor {

    public Uni<Void> execute(TokenEventContext<ScriptTask> ctx) {
        // TODO: embedded quarkus qute script engine or register external script worker
        return Uni.createFrom().voidItem();
    }
}
