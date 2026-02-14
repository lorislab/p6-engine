package org.lorislab.p6.engine.rs.v1.controllers;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;
import org.lorislab.p6.AbstractTest;

import gen.org.lorislab.p6.engine.rs.v1.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestHTTPEndpoint(JobRestController.class)
class JobRestControllerTest extends AbstractTest {

    @Test
    void job300Job1Test() {

        var tmp = deployResource("rs/v1/job/300_job_1.bpmn");

        var createResponse = createProcessInstance(tmp, Map.of("param1", 1, "param2", "text", "param3", true));

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var item = getProcessInstance(createResponse.getProcessInstanceId());
                    assertThat(item).isNotNull();
                    assertThat(item.getProcessDefinitionId()).isNotNull().isEqualTo(tmp.getId());
                    assertThat(item.getStatus()).isEqualTo(ProcessInstanceStatusDTO.ACTIVE);
                });

        var jobType = "300_job_1";
        var activeCriteria = new ActivateJobRequestDTO().type(jobType).worker("test").maxJobsToActivate(1).timeout(1000L);

        var activeJobs = given()
                .contentType(ContentType.JSON)
                .body(activeCriteria)
                .when()
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(ActivateJobResponseDTO.class);

        var job = activeJobs.getJobs().getFirst();
        var variables = job.getVariables();
        variables.put("new_variable", "123");

        var completeRequest = new CompleteJobRequestDTO().variables(variables).lockKey(job.getLockKey());

        given()
                .contentType(ContentType.JSON)
                .body(completeRequest)
                .pathParam("id", job.getId())
                .when()
                .post("/{id}/complete")
                .then()
                .statusCode(RestResponse.Status.NO_CONTENT.getStatusCode());

        var result = given()
                .contentType(ContentType.JSON)
                .body(new SearchJobRequestDTO().page(new SearchPageDTO()).criteria(new SearchJobCriteriaDTO().type(jobType)))
                .when()
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(SearchJobResponseDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        var item = result.getItems().getFirst();
        assertThat(item).isNotNull();
        assertThat(item.getType()).isEqualTo(jobType);
        assertThat(item.getStatus()).isEqualTo(JobStatusDTO.DONE);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var pi = getProcessInstance(createResponse.getProcessInstanceId());
                    assertThat(pi).isNotNull();
                    assertThat(pi.getProcessDefinitionId()).isNotNull().isEqualTo(tmp.getId());
                    assertThat(pi.getStatus()).isEqualTo(ProcessInstanceStatusDTO.COMPLETED);
                });
    }
}
