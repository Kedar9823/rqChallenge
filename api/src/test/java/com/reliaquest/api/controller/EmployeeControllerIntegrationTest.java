package com.reliaquest.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.reliaquest.api.ApiApplication;
import com.reliaquest.api.WireMockInitializer;
import com.reliaquest.api.model.Employee;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

/**
 *
 * @author Kedar10
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ApiApplication.class)
@ContextConfiguration(initializers = {WireMockInitializer.class})
@AutoConfigureWebTestClient(timeout = "PT2M")
public class EmployeeControllerIntegrationTest {

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private WebTestClient webTestClient;

    private String createEmployeeRequest;

    private String createEmployeeInvalidRequest;

    private String employeeDeletionRequest;

    private String employeeResponse;

    private String employeeRegisterResponse;

    private String employeeSearchByIdResponse;

    private String employeeDeletionResponse;

    private static final String EMPLOYEE_URL = "/api/v1/employee";

    private static final String EMPLOYEE_URL_ID_PARAM = "/api/v1/employee/[a-z0-9\\-]+";

    private static final String EMPLOYEE_ID = "9a55c532-7457-4fe3-a8f4-6ea8a957bdb3";

    private static final String EMPLOYEE_NAME = "Winfred";

    private static final String EMPLOYEE_NAME_CHAR = "H";

    private static final String EMP_LIST_EMPTY_ERROR = "Employee list should not be empty or null";

    private static final String EMP_RESP_NULL_ERROR = "Employee response should not be null";

    private static final String EMP_NULL_ERROR = "Employee should not be null";

    private static final String EMP_HIGH_SALARY_NULL_ERROR = "Highest salary among all employees should not be null";

    private static final String EMP_LIST_EMPTY_FOR_HIGH_SALARY_ERROR =
            "Employee list should not be empty or null for determining the highest salary";

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String UTF_8 = "UTF-8";

    @BeforeEach
    public void setUp() throws IOException {

        // Request
        createEmployeeRequest = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/request/CreateEmployeeRequest.json"),
                Charset.forName(UTF_8));

        createEmployeeInvalidRequest = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/request/CreateEmployeeInvalidRequest.json"),
                Charset.forName(UTF_8));

        employeeDeletionRequest = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/request/DeleteEmployeeRequest.json"),
                Charset.forName("UTF-8"));

        // Response
        employeeResponse = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/response/EmployeeResponse.json"),
                Charset.forName(UTF_8));

        employeeRegisterResponse = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/response/EmployeeRegisterResponse.json"),
                Charset.forName(UTF_8));

        employeeSearchByIdResponse = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/response/EmployeeSearchByIdResponse.json"),
                Charset.forName(UTF_8));

        employeeDeletionResponse = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/response/EmployeeDeletionResponse.json"),
                Charset.forName(UTF_8));
    }

    @Test
    public void testGetEmployees() {

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeResponse)));

        webTestClient
                .get()
                .uri("/api/v2/employees")
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(new ParameterizedTypeReference<List<Employee>>() {})
                .consumeWith(response -> {
                    List<Employee> employeeResult = response.getResponseBody();

                    assertThat(employeeResult)
                            .withFailMessage(EMP_LIST_EMPTY_ERROR)
                            .isNotEmpty();

                    assertEquals(50, employeeResult.size());
                });
    }

    @Test
    public void testGetEmployeesWhenRateLimitThrownShouldReturn429() {

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse().withStatus(429)));

        webTestClient
                .get()
                .uri("/api/v2/employees")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectBody()
                .jsonPath("$.error")
                .isEqualTo("Your request limit has been reached. Please try again in some time.")
                .jsonPath("$.status")
                .isEqualTo("429")
                .jsonPath("$.timestamp")
                .isNotEmpty();
    }

    @Test
    public void testGetEmployeesWhenClientErrorThrownShouldReturn400() {

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse().withStatus(400)));

        webTestClient
                .get()
                .uri("/api/v2/employees")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody()
                .jsonPath("$.error")
                .isEqualTo("Client error occurred: 400")
                .jsonPath("$.status")
                .isEqualTo("400")
                .jsonPath("$.timestamp")
                .isNotEmpty();
    }

    @Test
    public void testGetEmployeesWhenServerErrorThrownShouldReturn500() {

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse().withStatus(500)));

        webTestClient
                .get()
                .uri("/api/v2/employees")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("$.error")
                .isEqualTo("Server error occurred: 500")
                .jsonPath("$.status")
                .isEqualTo("500")
                .jsonPath("$.timestamp")
                .isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_NAME})
    public void testGetEmployeesByNameSearch(String searchString) {

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeResponse)));

        webTestClient
                .get()
                .uri("api/v2/employees/search/{searchString}", searchString)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(new ParameterizedTypeReference<List<Employee>>() {})
                .consumeWith(response -> {
                    List<Employee> employeeResult = response.getResponseBody();

                    assertEquals("Winfred Kautzer", employeeResult.get(0).getEmployeeName());
                    assertEquals(68, employeeResult.get(0).getEmployeeAge());
                    assertEquals("konklab@company.com", employeeResult.get(0).getEmployeeEmail());
                    assertEquals(166709, employeeResult.get(0).getEmployeeSalary());
                    assertEquals("Regional Producer", employeeResult.get(0).getEmployeeTitle());
                    assertEquals(1, employeeResult.size(), EMP_NULL_ERROR);
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_NAME_CHAR})
    public void testGetEmployeesByCharSearch(String searchString) {

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeResponse)));

        webTestClient
                .get()
                .uri("api/v2/employees/search/{searchString}", searchString)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(new ParameterizedTypeReference<List<Employee>>() {})
                .consumeWith(response -> {
                    if (response.getStatus().value() == 200) {
                        List<Employee> employeeResult = response.getResponseBody();
                        assertNotNull(employeeResult.size(), EMP_LIST_EMPTY_ERROR);
                    }
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_ID})
    public void testGetEmployeeById(String id) {

        wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching(EMPLOYEE_URL_ID_PARAM))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeSearchByIdResponse)));

        webTestClient
                .get()
                .uri("/api/v2/employees/{id}", id)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(Employee.class)
                .consumeWith(response -> {
                    Employee employeeResult = response.getResponseBody();

                    assertThat(employeeResult).withFailMessage(EMP_NULL_ERROR).isNotNull();
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_ID})
    public void testGetEmployeeByIdWithNotFoundStatus(String id) {

        wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching(EMPLOYEE_URL_ID_PARAM))
                .willReturn(WireMock.aResponse()
                        .withStatus(404)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        webTestClient
                .get()
                .uri("/api/v2/employees/{id}", id)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody()
                .jsonPath("$.error")
                .isEqualTo("Employee with id: 9a55c532-7457-4fe3-a8f4-6ea8a957bdb3 not found")
                .jsonPath("$.status")
                .isEqualTo("404")
                .jsonPath("$.timestamp")
                .isNotEmpty();
    }

    @Test
    public void testGetHighestSalaryOfEmployees() {

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeResponse)));

        webTestClient
                .get()
                .uri("/api/v2/employees/highestSalary")
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(Integer.class)
                .consumeWith(response -> {
                    Integer highestSalary = response.getResponseBody();
                    assertThat(highestSalary)
                            .withFailMessage(EMP_HIGH_SALARY_NULL_ERROR)
                            .isNotNull();
                    assertThat(highestSalary).isEqualTo(499137);
                });
    }

    @Test
    public void testGetTopTenHighestEarningEmployeeNames() {

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeResponse)));

        webTestClient
                .get()
                .uri("/api/v2/employees/topTenHighestEarningEmployeeNames")
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(new ParameterizedTypeReference<List<String>>() {})
                .consumeWith(response -> {
                    List<String> responseBody = response.getResponseBody();

                    assertEquals(10, responseBody.size());
                    assertThat(response)
                            .withFailMessage(EMP_LIST_EMPTY_FOR_HIGH_SALARY_ERROR)
                            .isNotNull();
                });
    }

    @Test
    public void testCreateEmployee() {

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeRegisterResponse)));

        webTestClient
                .post()
                .uri("/api/v2/employees")
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(createEmployeeRequest))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(Employee.class)
                .consumeWith(response -> {
                    Employee employeeResponse = response.getResponseBody();
                    assertThat(employeeResponse)
                            .withFailMessage(EMP_RESP_NULL_ERROR)
                            .isNotNull();
                });
    }

    @Test
    public void testCreateEmployeeValidationError() {

        webTestClient
                .post()
                .uri("/api/v2/employees")
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(createEmployeeInvalidRequest))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody()
                .jsonPath("$.errors")
                .isArray()
                .jsonPath("$.errors.length()")
                .isEqualTo(4);
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_ID})
    public void testDeleteEmployeeById(String id) {

        wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching(EMPLOYEE_URL_ID_PARAM))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeSearchByIdResponse)));

        wireMockServer.stubFor(WireMock.delete(WireMock.urlPathMatching(EMPLOYEE_URL))
                .withRequestBody(WireMock.equalToJson(employeeDeletionRequest))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeDeletionResponse)));

        webTestClient
                .delete()
                .uri("/api/v2/employees/{id}", id)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String employeeName = response.getResponseBody();
                    assertEquals("Dr. Lindsy Anderson", employeeName);
                });
    }
}
