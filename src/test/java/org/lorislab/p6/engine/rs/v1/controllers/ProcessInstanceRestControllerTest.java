package org.lorislab.p6.engine.rs.v1.controllers;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.lorislab.p6.AbstractTest;

import gen.org.lorislab.p6.engine.rs.v1.model.CreateProcessInstanceRequestDTO;
import gen.org.lorislab.p6.engine.rs.v1.model.CreateProcessInstanceResponseDTO;
import gen.org.lorislab.p6.engine.rs.v1.model.ProcessInstanceDTO;
import gen.org.lorislab.p6.engine.rs.v1.model.ProcessInstanceStatusDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestHTTPEndpoint(ProcessInstanceRestController.class)
class ProcessInstanceRestControllerTest extends AbstractTest {

    @Test
    void pi100StartEndProcessTest() throws Exception {

        var tmp = deployResource("rs/v1/pi-100-start-end.bpmn");

        var request = new CreateProcessInstanceRequestDTO()
                .processId(tmp.getProcessId())
                .processVersion(tmp.getProcessVersion())
                .variables(Map.of("param1", 1, "param2", "text", "param3", true));

        var createResponse = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(CreateProcessInstanceResponseDTO.class);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var item = given()
                            .contentType(ContentType.JSON)
                            .when()
                            .get(createResponse.getProcessInstanceId())
                            .then()
                            .statusCode(Response.Status.OK.getStatusCode())
                            .extract().as(ProcessInstanceDTO.class);
                    assertThat(item).isNotNull();
                    assertThat(item.getProcessDefinitionId()).isNotNull().isEqualTo(tmp.getId());
                    assertThat(item.getStatus()).isEqualTo(ProcessInstanceStatusDTO.COMPLETED);
                });

    }
}
