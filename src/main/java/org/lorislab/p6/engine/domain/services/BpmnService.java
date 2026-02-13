package org.lorislab.p6.engine.domain.services;

import jakarta.enterprise.context.ApplicationScoped;

import org.lorislab.p6.bpmn2.model.Definitions;
import org.lorislab.p6.bpmn2.reader.ModelReader;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

@ApplicationScoped
public class BpmnService {

    public Uni<Definitions> loadDefinitions(byte[] data) {
        if (data == null || data.length == 0) {
            return Uni.createFrom().nullItem();
        }
        return Uni.createFrom().item(() -> {
            try {
                return ModelReader.read(data);
            } catch (Exception ex) {
                throw new BpmReaderException("Failed to read BPMN data.", ex);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    public static class BpmReaderException extends RuntimeException {
        public BpmReaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
