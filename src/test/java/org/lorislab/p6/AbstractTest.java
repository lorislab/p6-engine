package org.lorislab.p6;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.core.Response;

import gen.org.lorislab.p6.engine.rs.v1.model.*;
import io.restassured.http.ContentType;

public abstract class AbstractTest {

    protected static File getBpmn2File(String name) {
        return Paths.get("src/test/resources/bpmn2/" + name).toFile();
    }

    protected DeploymentProcessDefinitionResultDTO deployResource(String file) {
        var name = Paths.get(file).getFileName().toString();
        var item = map(deployResources(file)).get(name);
        assertThat(item).isNotNull();
        return item;
    }

    protected Map<String, DeploymentProcessDefinitionResultDTO> map(DeploymentResponseDTO dto) {

        var deployments = dto.getDeployments();
        if (deployments == null) {
            return Map.of();
        }

        Map<String, DeploymentProcessDefinitionResultDTO> result = new HashMap<>();
        for (var deployment : dto.getDeployments()) {
            result.put(deployment.getResource().getName(), deployment.getProcessDefinition());
        }

        return result;
    }

    protected ProcessDefinitionDTO getProcessDefinition(String id) {
        var tmp = given().basePath("/v1/processDefinition/" + id);
        return tmp
                .get()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(ProcessDefinitionDTO.class);
    }

    protected DeploymentResponseDTO deployResources(String... files) {

        var tmp = given().basePath("/v1/resource/deployments");

        for (String file : files) {
            tmp = tmp.multiPart("resources", getBpmn2File(file));
        }

        var response = tmp.when()
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(DeploymentResponseDTO.class);

        assertThat(response).isNotNull();

        return response;
    }

    protected CreateProcessInstanceResponseDTO createProcessInstance(DeploymentProcessDefinitionResultDTO dto,
            Map<String, Object> variables) {
        return createProcessInstance(dto.getProcessId(), dto.getProcessVersion(), variables);
    }

    protected CreateProcessInstanceResponseDTO createProcessInstance(String processId, String processVersion,
            Map<String, Object> variables) {

        var request = new CreateProcessInstanceRequestDTO()
                .processId(processId)
                .processVersion(processVersion)
                .variables(variables);

        return given().basePath("/v1/processInstances")
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(CreateProcessInstanceResponseDTO.class);
    }

    public ProcessInstanceDTO getProcessInstance(String processInstanceId) {
        return given().basePath("/v1/processInstances/" + processInstanceId)
                .contentType(ContentType.JSON)
                .when()
                .get()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(ProcessInstanceDTO.class);
    }
}
