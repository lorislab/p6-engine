package org.lorislab.p6.engine.rs.v1.controllers;

import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.resteasy.reactive.RestResponse;
import org.lorislab.p6.engine.domain.config.EngineConfig;
import org.lorislab.p6.engine.domain.services.JobService;
import org.lorislab.p6.engine.domain.services.ValueMapper;
import org.lorislab.p6.engine.domain.store.JobRepository;
import org.lorislab.p6.engine.domain.store.model.Job;
import org.lorislab.p6.engine.rs.v1.mappers.JobMapper;
import org.lorislab.quarkus.data.sql.Page;

import gen.org.lorislab.p6.engine.rs.v1.JobApiService;
import gen.org.lorislab.p6.engine.rs.v1.model.*;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class JobRestController implements JobApiService {

    @Inject
    JobMapper jobMapper;

    @Inject
    JobService jobService;

    @Inject
    EngineConfig engineConfig;

    @Inject
    JobRepository jobRepository;

    @Inject
    Pool pool;

    @Inject
    ValueMapper valueMapper;

    @Override
    public Uni<RestResponse<ActivateJobResponseDTO>> activateJob(ActivateJobRequestDTO activateJobRequestDTO) {
        var criteria = jobMapper.createCriteria(activateJobRequestDTO);
        criteria.setMaxRetries(engineConfig.job().maxRetries());

        return pool.withTransaction(conn -> jobRepository.findNextJobs(conn, criteria)
                .onItem().ifNotNull().transformToUni(items -> convertActiveJob(items)
                        .map(x -> jobMapper.createActiveJobs(x))))
                .onItem().transform(response -> {
                    if (response == null) {
                        return RestResponse.notFound();
                    }
                    return RestResponse.ok(response);
                });
    }

    private Uni<List<JobWrapper>> convertActiveJob(List<Job> jobs) {
        List<Uni<JobWrapper>> unis = jobs.stream()
                .map(job -> Uni.combine().all()
                        .unis(
                                valueMapper.readVariables(job.getVariables()),
                                valueMapper.readMap(job.getCustomHeaders()))
                        .asTuple()
                        .map(tuple -> new JobWrapper(job, tuple.getItem1(), tuple.getItem2())))
                .toList();
        return Uni.combine().all().unis(unis).with(x -> x.stream().map(o -> (JobWrapper) o).toList());
    }

    @Override
    public Uni<RestResponse<Void>> completeJob(String id, CompleteJobRequestDTO completeJobRequestDTO) {
        return jobService.completeJob(id, completeJobRequestDTO.getLockKey(), completeJobRequestDTO.getVariables())
                .onItem().transform(x -> RestResponse.noContent());
    }

    @Override
    public Uni<RestResponse<Void>> errorJob(String id, ErrorJobRequestDTO errorJobRequestDTO) {
        return jobService.errorJob(id, errorJobRequestDTO.getLockKey(), errorJobRequestDTO.getErrorCode(),
                errorJobRequestDTO.getErrorMessage(),
                errorJobRequestDTO.getVariables())
                .onItem().transform(x -> RestResponse.noContent());
    }

    @Override
    public Uni<RestResponse<Void>> failJob(String id, FailJobRequestDTO failJobRequestDTO) {
        return jobService.failJob(id, failJobRequestDTO.getLockKey(), failJobRequestDTO.getRetries(),
                failJobRequestDTO.getErrorMessage(),
                failJobRequestDTO.getRetryBackOff(), failJobRequestDTO.getVariables())
                .onItem().transform(x -> RestResponse.noContent());
    }

    @Override
    public Uni<RestResponse<SearchJobResponseDTO>> searchJobs(SearchJobRequestDTO searchJobRequestDTO) {
        var criteria = jobMapper.criteria(searchJobRequestDTO);
        return jobRepository.findByCriteria(criteria)
                .onItem().ifNotNull().transformToUni(page -> convertJob(page.content())
                        .map(x -> Page.ofPage(page, x))
                        .map(p -> jobMapper.create(p)))
                .onItem().transform(response -> {
                    if (response == null) {
                        return RestResponse.notFound();
                    }
                    return RestResponse.ok(response);
                });
    }

    private Uni<List<JobWrapper>> convertJob(List<Job> jobs) {
        List<Uni<JobWrapper>> unis = jobs.stream()
                .map(job -> valueMapper.readVariables(job.getVariables())
                        .map(x -> new JobWrapper(job, x, null)))
                .toList();

        return Uni.combine().all().unis(unis).with(x -> x.stream().map(o -> (JobWrapper) o).toList());
    }

    public record JobWrapper(Job job, Map<String, Object> variables, Map<String, String> customHeaders) {
    }
}
