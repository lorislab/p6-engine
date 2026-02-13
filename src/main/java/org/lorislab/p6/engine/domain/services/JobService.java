package org.lorislab.p6.engine.domain.services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.engine.domain.store.JobRepository;
import org.lorislab.p6.engine.domain.store.model.Job;
import org.lorislab.p6.engine.domain.store.model.event.TokenEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;

@ApplicationScoped
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    @Inject
    StreamService streamService;

    @Inject
    ValueMapper valueMapper;

    @Inject
    Pool pool;

    @Inject
    JobRepository jobRepository;

    public Uni<Void> failJob(String key, String lockKey, Integer retries, String errorMessage, Long retryBackOff,
            Map<String, Object> variables) {
        return pool.withTransaction(conn -> jobRepository.findByIdAndLockKey(conn, key, lockKey)
                .onItem().ifNotNull().transformToUni(job -> valueMapper.writeVariables(variables)
                        .invoke(job::setOutput)
                        .invoke(() -> {
                            job.setLockKey(null);
                            job.setErrorMessage(errorMessage);
                            job.setStatus(Job.Status.PENDING);
                            job.setState(Job.State.CREATED);
                            job.setTakeFrom(LocalDateTime.now().plus(retryBackOff, ChronoUnit.MILLIS));
                            job.setRetryCount(retries);
                        })
                        .chain(() -> jobRepository.updateFailJob(conn, job))));
    }

    public Uni<Void> errorJob(String key, String lockKey, String errorCode, String errorMessage,
            Map<String, Object> variables) {
        return pool.withTransaction(conn -> jobRepository.findByIdAndLockKey(conn, key, lockKey)
                .onItem().ifNotNull().transformToUni(job -> valueMapper.writeVariables(variables)
                        .invoke(job::setOutput)
                        .invoke(() -> {
                            job.setErrorCode(errorCode);
                            job.setErrorMessage(errorMessage);
                            job.setStatus(Job.Status.DONE);
                            job.setState(Job.State.ERROR_THROWN);
                        })
                        .chain(() -> jobRepository.updateErrorJob(conn, job))));
    }

    public Uni<Void> completeJob(String key, String lockKey, Map<String, Object> variables) {
        return pool.withTransaction(conn -> jobRepository.findByIdAndLockKey(conn, key, lockKey)
                .onItem().ifNotNull().transformToUni(job -> {

                    job.setStatus(Job.Status.DONE);
                    job.setState(Job.State.COMPLETED);

                    return valueMapper.readVariables(job.getVariables())
                            .onItem().transformToUni(data -> {
                                data.putAll(variables);

                                if (TokenEvent.class.getSimpleName().equals(job.getTokenType())) {

                                    return valueMapper.read(job.getToken(), TokenEvent.class)
                                            .onItem().transformToUni(token -> {

                                                token.setVariables(data);
                                                token.setPhase(TokenEvent.Phase.CLOSE);

                                                return jobRepository.updateCompleteJob(conn, job)
                                                        .chain(() -> streamService.insert(conn, token));
                                            });
                                }

                                log.warn("Not supported event type {}", job.getTokenType());
                                return Uni.createFrom().voidItem();
                            });
                }));
    }
}
