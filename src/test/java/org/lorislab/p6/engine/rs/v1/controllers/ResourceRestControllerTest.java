package org.lorislab.p6.engine.rs.v1.controllers;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.lorislab.p6.AbstractTest;

import gen.org.lorislab.p6.engine.rs.v1.model.DeploymentResponseDTO;
import gen.org.lorislab.p6.engine.rs.v1.model.ResourceDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestHTTPEndpoint(ResourceRestController.class)
class ResourceRestControllerTest extends AbstractTest {

    @Test
    void changesTest() {
        var response = deployResources("changes/1/process-change.bpmn");
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotBlank();
        assertThat(response.getDeployments()).isNotNull();
        var deployments = response.getDeployments();
        assertThat(deployments).isNotNull().isNotEmpty().hasSize(1);
        var deployment = deployments.getFirst();
        assertThat(deployment).isNotNull();
        var deploymentResource = deployment.getResource();
        assertThat(deploymentResource).isNotNull();
        assertThat(deploymentResource.getVersion()).isEqualTo(1);

        var resource = given()
                .pathParam("id", deploymentResource.getId())
                .get("/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(ResourceDTO.class);

        assertThat(resource).isNotNull();
        assertThat(resource.getVersion()).isEqualTo(1);
        assertThat(resource.getId()).isEqualTo(deploymentResource.getId());

        deployResources("changes/2/process-change.bpmn");

        response = deployResources("changes/3/process-change.bpmn");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotBlank();
        assertThat(response.getDeployments()).isNotNull();
        deployments = response.getDeployments();
        assertThat(deployments).isNotNull().isNotEmpty().hasSize(1);
        deployment = deployments.getFirst();
        assertThat(deployment).isNotNull();
        deploymentResource = deployment.getResource();
        assertThat(deploymentResource).isNotNull();
        assertThat(deploymentResource.getVersion()).isEqualTo(2);

        resource = given()
                .pathParam("id", deploymentResource.getId())
                .get("/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(ResourceDTO.class);

        assertThat(resource).isNotNull();
        assertThat(resource.getVersion()).isEqualTo(2);
        assertThat(resource.getId()).isEqualTo(deploymentResource.getId());
    }

    @Test
    void deploymentTest() {
        var response = deployResources("100-start-end.bpmn", "100-start-end-drools.bpmn");
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotBlank();
        assertThat(response.getDeployments()).isNotNull().hasSize(2);
    }

    @Test
    void deploymentCustomResourceNameTest() {
        var response = given()
                .multiPart("process", getBpmn2File("100-start-end-custom-resource.bpmn"))
                .when()
                .post("/deployments")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(DeploymentResponseDTO.class);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotBlank();
        assertThat(response.getDeployments()).isNotNull().hasSize(1);
    }

    @Test
    void deploymentNoResourcesTest() {
        given()
                .contentType(ContentType.MULTIPART)
                .when()
                .post("/deployments")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

}
