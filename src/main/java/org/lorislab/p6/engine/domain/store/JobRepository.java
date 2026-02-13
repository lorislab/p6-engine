package org.lorislab.p6.engine.domain.store;

import static org.lorislab.quarkus.data.sql.Op.equal;
import static org.lorislab.quarkus.data.sql.Order.asc;
import static org.lorislab.quarkus.data.sql.Select.select;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.lorislab.p6.engine.domain.store.mapper.JobDataMapper;
import org.lorislab.p6.engine.domain.store.model.Job;
import org.lorislab.p6.engine.domain.store.model.JobActiveCriteria;
import org.lorislab.p6.engine.domain.store.model.JobSearchCriteria;
import org.lorislab.p6.engine.domain.store.model.Job_;
import org.lorislab.quarkus.data.sql.*;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.SqlClient;

@ApplicationScoped
public class JobRepository extends AbstractRepository {

    @Inject
    JobDataMapper mapper;

    //                    AND TAKE_FROM <= now()
    private static final String SQL_FIND_NEXT_JOB = """
            WITH next_job AS (
                SELECT id
                FROM JOB
                WHERE
                    RETRY_COUNT < $1
                    AND TYPE = $2
                    AND TAKE_FROM <= CURRENT_TIMESTAMP
                    AND (
                        STATUS = 'PENDING'
                        OR (STATUS = 'IN_PROGRESS' AND LOCK_TO <= CURRENT_TIMESTAMP)
                    )
                ORDER BY CREATED_AT
                LIMIT $3
                FOR UPDATE SKIP LOCKED
            )
            UPDATE JOB
            SET STATUS = 'IN_PROGRESS',
                LOCK_KEY = gen_random_uuid(),
                WORKER = $4,
                LOCK = LOCK + 1,
                UPDATED_AT = CURRENT_TIMESTAMP,
                LOCK_TO = CURRENT_TIMESTAMP + ($5 * interval '1 millisecond'),
                RETRY_COUNT = RETRY_COUNT + 1
            FROM next_job
            WHERE job.id = next_job.id
            RETURNING job.*;
            """;

    public Uni<List<Job>> findNextJobs(SqlClient client, JobActiveCriteria criteria) {
        return client.preparedQuery(SQL_FIND_NEXT_JOB)
                .mapping(mapper::map)
                .execute(tuple(criteria.getMaxRetries(), criteria.getType(), criteria.getMaxJobsToActivate(),
                        criteria.getWorker(), criteria.getTimeout()))
                .map(rowSet -> rowSet.stream().toList());
    }

    public Uni<Job> findByIdAndLockKey(SqlClient client, String id, String lockKey) {
        return client.preparedQuery(select().from(Job_.TABLE_).where(equal(Job_.ID_), equal(Job_.LOCK_KEY)).build())
                .mapping(mapper::map)
                .execute(tuple(id, lockKey))
                .map(rowSet -> rowSet.stream().findFirst().orElse(null));
    }

    public Uni<Page<Job>> findByCriteria(JobSearchCriteria searchCriteria) {

        var criteria = new Criteria();
        criteria.addNotNull(equal(Job_.TYPE), searchCriteria.getType());
        criteria.addNotNull(equal(Job_.WORKER), searchCriteria.getWorker());
        criteria.addNotNull(equal(Job_.STATUS), searchCriteria.getStatus());

        var pageRequest = searchCriteria.getPageRequest();

        var select = select()
                .from(Job_.TABLE_)
                .where(criteria.ops())
                .limit(pageRequest.size())
                .offset((int) (pageRequest.page() - 1) * pageRequest.size())
                .orderBy(asc(Job_.ID_));

        return (pageRequest.requestTotal() ? selectCount(pool, select, criteria, Job_.ID) : Uni.createFrom().item(-1L))
                .flatMap(totalResults -> pool.preparedQuery(select.build())
                        .mapping(mapper::map)
                        .execute(tuple(criteria.values()))
                        .map(results -> PageRecord.of(pageRequest, results.stream().toList(), totalResults)));
    }

    private static final String SQL_INSERT = Insert.insert()
            .into(Job_.TABLE_)
            .columns(Job_.ID_, Job_.LOCK, Job_.TYPE, Job_.STATUS, Job_.RETRY_COUNT,
                    Job_.STATE, Job_.ERROR_CODE, Job_.ERROR_MESSAGE, Job_.ELEMENT_ID,
                    Job_.PROCESS_ID, Job_.PROCESS_VERSION, Job_.PROCESS_INSTANCE_ID,
                    Job_.PROCESS_DEFINITION_ID,
                    Job_.PROCESS_DEFINITION_VERSION, Job_.CUSTOM_HEADERS, Job_.OUTPUT, Job_.VARIABLES, Job_.TOKEN,
                    Job_.TOKEN_TYPE)
            .build();

    public Uni<Void> insert(SqlClient client, Job job) {
        return client.preparedQuery(SQL_INSERT)
                .execute(tuple(
                        job.getId(), job.getLock(), job.getType(), job.getStatus(),
                        //                        job.getCreatedAt(), job.getUpdatedAt(), job.getTakeFrom(),
                        job.getRetryCount(),
                        job.getState(), job.getErrorCode(), job.getErrorMessage(), job.getElementId(),
                        job.getProcessId(), job.getProcessVersion(), job.getProcessInstanceId(),
                        job.getProcessDefinitionId(),
                        job.getProcessDefinitionVersion(), job.getCustomHeaders(), job.getOutput(),
                        job.getVariables(), job.getToken(), job.getTokenType()))
                .replaceWithVoid();
    }

    private static final String SQL_UPDATE_COMPLETE_JOB = Update.update(Job_.TABLE_)
            .set(Job_.STATUS, Job_.STATE, Job_.OUTPUT)
            .where(equal(Job_.ID))
            .build();

    public Uni<Void> updateCompleteJob(SqlClient client, Job job) {
        return client.preparedQuery(SQL_UPDATE_COMPLETE_JOB)
                .execute(tuple(job.getStatus(), job.getState(), job.getOutput(), job.getId()))
                .replaceWithVoid();
    }

    private static final String SQL_UPDATE_ERROR_JOB = Update.update(Job_.TABLE_)
            .set(Job_.STATUS, Job_.STATE, Job_.ERROR_CODE, Job_.ERROR_MESSAGE)
            .where(equal(Job_.ID))
            .build();

    public Uni<Void> updateErrorJob(SqlClient client, Job job) {
        return client.preparedQuery(SQL_UPDATE_ERROR_JOB)
                .execute(tuple(job.getStatus(), job.getState(), job.getErrorCode(), job.getErrorMessage()))
                .replaceWithVoid();
    }

    private static final String SQL_UPDATE_FAIL_JOB = Update.update(Job_.TABLE_)
            .set(Job_.STATUS, Job_.STATE, Job_.ERROR_MESSAGE, Job_.LOCK_KEY, Job_.TAKE_FROM, Job_.RETRY_COUNT)
            .where(equal(Job_.ID))
            .build();

    public Uni<Void> updateFailJob(SqlClient client, Job job) {
        return client.preparedQuery(SQL_UPDATE_FAIL_JOB)
                .execute(tuple(job.getStatus(), job.getState(), job.getErrorMessage(), job.getLockKey(), job.getTakeFrom(),
                        job.getRetryCount()))
                .replaceWithVoid();
    }
}
