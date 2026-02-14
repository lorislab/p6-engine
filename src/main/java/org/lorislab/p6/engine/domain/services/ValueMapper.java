package org.lorislab.p6.engine.domain.services;

import java.util.Collections;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.engine.domain.store.model.Stream;
import org.lorislab.p6.engine.domain.store.model.event.Event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

@ApplicationScoped
public class ValueMapper {

    @Inject
    ObjectMapper objectMapper;

    public <T extends Event> Uni<T> read(Stream event, Class<T> clazz) {
        if (event == null) {
            return Uni.createFrom().nullItem();
        }
        return read(event.getValue(), clazz);
    }

    public <T extends Event> Uni<T> read(byte[] data, Class<T> clazz) {
        if (data == null) {
            return Uni.createFrom().nullItem();
        }

        return Uni.createFrom().item(() -> {
            try {
                return objectMapper.readValue(data, clazz);
            } catch (Exception ex) {
                throw new ValueMapperException("Failed to parse stream event", ex);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    public Uni<byte[]> mergeVariables(byte[] current, Map<String, Object> variables) {
        if (current == null) {
            return writeVariables(variables);
        }
        if (variables == null) {
            return Uni.createFrom().item(current);
        }
        return readVariables(current)
                .onItem().transformToUni(input -> {
                    input.putAll(variables);
                    return writeVariables(input);
                });
    }

    public Uni<byte[]> mergeVariables(byte[] current, byte[] variables) {
        if (current == null) {
            return Uni.createFrom().item(variables);
        }
        if (variables == null) {
            return Uni.createFrom().item(current);
        }
        return readVariables(current)
                .onItem().transformToUni(
                        input -> readVariables(variables)
                                .onItem().transformToUni(tmp -> {
                                    input.putAll(tmp);
                                    return writeVariables(input);
                                }));
    }

    public Uni<byte[]> writeVariables(Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return Uni.createFrom().item(new byte[0]);
        }

        return Uni.createFrom().item(() -> {
            try {
                return objectMapper.writeValueAsBytes(variables);
            } catch (Exception e) {
                throw new ValueMapperException("Failed to write variables", e);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    public Uni<byte[]> write(Object data) {
        return Uni.createFrom().item(() -> {
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new ValueMapperException("Failed to write object", e);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    public Uni<Map<String, Object>> readVariables(byte[] data) {
        if (data == null || data.length == 0) {
            return Uni.createFrom().item(Collections.emptyMap());
        }

        return Uni.createFrom().item(() -> {
            try {
                return objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
                });
            } catch (Exception e) {
                throw new ValueMapperException("Failed to read variables", e);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    public Uni<Map<String, String>> readMap(byte[] data) {
        if (data == null || data.length == 0) {
            return Uni.createFrom().item(Collections.emptyMap());
        }

        return Uni.createFrom().item(() -> {
            try {
                return objectMapper.readValue(data, new TypeReference<Map<String, String>>() {
                });
            } catch (Exception e) {
                throw new ValueMapperException("Failed to read map", e);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    public static class ValueMapperException extends RuntimeException {
        public ValueMapperException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
