package org.lorislab.p6.engine.rs.v1.mappers;

import java.util.List;

import org.lorislab.p6.common.rs.OffsetDateTimeMapper;
import org.lorislab.p6.engine.domain.store.model.Job;
import org.lorislab.p6.engine.domain.store.model.JobActiveCriteria;
import org.lorislab.p6.engine.domain.store.model.JobSearchCriteria;
import org.lorislab.p6.engine.rs.v1.controllers.JobRestController;
import org.lorislab.quarkus.data.sql.Page;
import org.lorislab.quarkus.data.sql.PageRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.lorislab.p6.engine.rs.v1.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface JobMapper {

    @Mapping(target = "maxRetries", ignore = true)
    JobActiveCriteria createCriteria(ActivateJobRequestDTO dto);

    default ActivateJobResponseDTO createActiveJobs(List<JobRestController.JobWrapper> jobs) {
        return new ActivateJobResponseDTO().jobs(activeJobs(jobs));
    }

    List<ActivateJobItemDTO> activeJobs(List<JobRestController.JobWrapper> jobs);

    default ActivateJobItemDTO activeJob(JobRestController.JobWrapper wrapper) {
        if (wrapper == null) {
            return null;
        }
        return createActiveJob(wrapper.job()).variables(wrapper.variables()).customHeaders(wrapper.customHeaders());
    }

    @Mapping(target = "retries", source = "retryCount")
    @Mapping(target = "deadline", source = "lockTo")
    @Mapping(target = "removeCustomHeadersItem", ignore = true)
    @Mapping(target = "removeVariablesItem", ignore = true)
    @Mapping(target = "variables", ignore = true)
    @Mapping(target = "customHeaders", ignore = true)
    ActivateJobItemDTO createActiveJob(Job job);

    default SearchJobResponseDTO create(Page<JobRestController.JobWrapper> page) {
        var result = new SearchJobResponseDTO();
        result.setPage(createPage(page));
        result.setItems(create(page.content()));
        return result;
    }

    default PageDTO createPage(Page<?> page) {
        if (page == null) {
            return null;
        }
        var result = new PageDTO();
        result.setTotalElements(page.totalElements());
        result.setTotalPages(page.totalPages());
        result.setHasTotals(page.hasTotals());
        result.setNumberOfElements(page.numberOfElements());
        result.setHasNext(page.hasNext());
        result.setHasPrevious(page.hasPrevious());
        return result;
    }

    List<JobDTO> create(List<JobRestController.JobWrapper> items);

    default JobDTO create(JobRestController.JobWrapper job) {
        if (job == null) {
            return null;
        }
        return create(job.job()).variables(job.variables());
    }

    @Mapping(target = "removeVariablesItem", ignore = true)
    @Mapping(target = "variables", ignore = true)
    JobDTO create(Job job);

    default JobSearchCriteria criteria(SearchJobRequestDTO dto) {
        var result = map(dto.getCriteria());
        result.setPageRequest(map(dto.getPage()));
        return result;
    }

    @Mapping(target = "pageRequest", ignore = true)
    JobSearchCriteria map(SearchJobCriteriaDTO dto);

    default PageRequest map(SearchPageDTO dto) {
        return PageRequest.ofPage(dto.getFrom(), dto.getLimit(), true);
    }
}
