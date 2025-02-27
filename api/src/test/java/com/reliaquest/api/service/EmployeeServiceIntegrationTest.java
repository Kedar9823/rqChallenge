package com.reliaquest.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.reliaquest.api.ApiApplication;
import com.reliaquest.api.WireMockInitializer;
import com.reliaquest.api.exception.EmployeeAPIClientException;
import com.reliaquest.api.exception.EmployeeAPIServerException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeRateLimitException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeRegister;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 *
 * @author Kedar10
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ApiApplication.class)
@ContextConfiguration(initializers = {WireMockInitializer.class})
@AutoConfigureWebTestClient(timeout = "PT2M")
class EmployeeServiceIntegrationTest {

    @InjectMocks
    private static EmployeeService employeeService;

    @Autowired
    private WireMockServer wireMockServer;

    private String employeeDeletionRequest;

    private String employeeResponse;

    private String employeeRegisterResponse;

    private String employeeSearchByIdResponse;

    private String employeeDeletionResponse;

    private String errorResponse;

    private EmployeeRegister employeeRegister;

    private ObjectMapper objectMapper;

    private static final String EMPLOYEE_URL = "/api/v1/employee";

    private static final String EMPLOYEE_URL_ID_PARAM = "/api/v1/employee/[a-z0-9\\-]+";

    private static final String EMPLOYEE_ID = "9a55c532-7457-4fe3-a8f4-6ea8a957bdb3";

    private static final String EMPLOYEE_NAME = "Winfred";

    private static final String EMPLOYEE_NAME_CHAR = "H";

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String UTF_8 = "UTF-8";

    @BeforeEach
    void setUp() throws Exception {

        employeeService = new EmployeeService(buildWebClient());

        objectMapper = new ObjectMapper();

        // Request
        employeeRegister = objectMapper.readValue(
                new File("src/test/resources/com/reliaquest/api/request/CreateEmployeeRequest.json"),
                EmployeeRegister.class);

        employeeDeletionRequest = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/request/DeleteEmployeeRequest.json"),
                Charset.forName(UTF_8));

        // Response
        employeeResponse = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/response/EmployeeResponse.json"),
                Charset.forName(UTF_8));

        employeeRegisterResponse = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/response/EmployeeRegisterResponse.json"),
                Charset.forName(UTF_8));

        errorResponse = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/response/ErrorResponse.json"), Charset.forName(UTF_8));

        employeeSearchByIdResponse = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/response/EmployeeSearchByIdResponse.json"),
                Charset.forName(UTF_8));

        employeeDeletionResponse = FileUtils.readFileToString(
                new File("src/test/resources/com/reliaquest/api/response/EmployeeDeletionResponse.json"),
                Charset.forName(UTF_8));
    }

    // Successful Response
    @Test
    public void givenEmployees_whenGetEmployeeInfo_thenStatus200() {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeResponse)));

        List<Employee> employees = employeeService.getEmployeeInfo();

        // Then
        assertNotNull(employees);
        assertEquals(50, employees.size());
    }

    // Rate Limit Exception
    @Test
    public void givenEmployeeRateLimitException_whenGetEmployeeInfo_thenStatus429() {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse().withStatus(429)));

        EmployeeRateLimitException rateLimitException = assertThrows(EmployeeRateLimitException.class, () -> {
            employeeService.getEmployeeInfo();
        });

        // Then
        assertTrue(rateLimitException.getMessage().contains("Received 429 : Too Many Request"));
    }

    // API Client Exception
    @Test
    public void givenEmployeeAPIClientException_whenGetEmployeeInfo_thenStatus400() {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse().withStatus(400)));

        EmployeeAPIClientException clientException = assertThrows(EmployeeAPIClientException.class, () -> {
            employeeService.getEmployeeInfo();
        });

        // Then
        assertTrue(clientException.getMessage().contains("Client error occurred"));
    }

    // API Server Exception with Error Response
    @Test
    public void givenEmployeeAPIServerException_whenGetEmployeeInfo_thenStatus500WithErrorResponse() {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(errorResponse)));

        EmployeeAPIServerException serverException = assertThrows(EmployeeAPIServerException.class, () -> {
            employeeService.getEmployeeInfo();
        });

        // Then
        assertTrue(
                serverException.getMessage().contains("Exception occured while processing request, mock server api."));
    }

    // API Server Exception
    @Test
    public void givenEmployeeAPIServerException_whenGetEmployeeInfo_thenStatus500() {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse().withStatus(500)));

        EmployeeAPIServerException serverException = assertThrows(EmployeeAPIServerException.class, () -> {
            employeeService.getEmployeeInfo();
        });

        // Then
        assertTrue(serverException.getMessage().contains("Server error occurred"));
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_NAME})
    public void givenEmployees_whenGetEmployeesByNameSearchByName_thenStatus200(String name) {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeResponse)));

        List<Employee> employees = employeeService.getEmployeesByNameSearch(name);

        // Then
        assertNotNull(employees);
        assertEquals(1, employees.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_NAME_CHAR})
    public void givenEmployees_whenGetEmployeesByNameSearchByChar_thenStatus200(String name) {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeResponse)));

        List<Employee> employees = employeeService.getEmployeesByNameSearch(name);

        // Then
        assertNotNull(employees);
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_NAME})
    public void givenEmployeeRateLimitException_whenGetEmployeesByNameSearch_thenStatus429(String name) {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse().withStatus(429)));

        EmployeeRateLimitException rateLimitException = assertThrows(EmployeeRateLimitException.class, () -> {
            employeeService.getEmployeesByNameSearch(name);
        });

        // Then
        assertTrue(rateLimitException.getMessage().contains("Received 429 : Too Many Request"));
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_NAME})
    public void givenEmployeeAPIClientException_whenGetEmployeesByNameSearch_thenStatus400(String name) {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse().withStatus(400)));

        EmployeeAPIClientException clientException = assertThrows(EmployeeAPIClientException.class, () -> {
            employeeService.getEmployeesByNameSearch(name);
        });

        // Then
        assertTrue(clientException.getMessage().contains("Client error occurred"));
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_NAME})
    public void givenEmployeeAPIServerException_whenGetEmployeesByNameSearch_thenStatus500WithErrorResponse(
            String name) {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(errorResponse)));

        EmployeeAPIServerException serverException = assertThrows(EmployeeAPIServerException.class, () -> {
            employeeService.getEmployeesByNameSearch(name);
        });

        // Then
        assertTrue(
                serverException.getMessage().contains("Exception occured while processing request, mock server api."));
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_NAME})
    public void givenEmployeeAPIServerException_whenGetEmployeesByNameSearch_thenStatus500(String name) {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse().withStatus(500)));

        EmployeeAPIServerException serverException = assertThrows(EmployeeAPIServerException.class, () -> {
            employeeService.getEmployeesByNameSearch(name);
        });

        // Then
        assertTrue(serverException.getMessage().contains("Server error occurred"));
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_ID})
    public void givenEmployees_whenGetEmployeeInfoById_thenStatus200(String id) {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching(EMPLOYEE_URL_ID_PARAM))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeSearchByIdResponse)));

        Mono<Employee> employeeInfoById = employeeService.getEmployeeInfoById(id);

        // Then
        StepVerifier.create(employeeInfoById)
                .consumeNextWith(response -> {
                    assertThat(response).isInstanceOf(Employee.class);
                    assertThat(response.getUuid()).isEqualTo(UUID.fromString(EMPLOYEE_ID));
                    assertThat(response.getEmployeeName()).isEqualTo("Dr. Lindsy Anderson");
                    assertThat(response.getEmployeeSalary()).isEqualTo(59249);
                    assertThat(response.getEmployeeAge()).isEqualTo(49);
                    assertThat(response.getEmployeeEmail()).isEqualTo("bytecard@company.com");
                    assertThat(response.getEmployeeTitle()).isEqualTo("National IT Orchestrator");
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPLOYEE_ID})
    public void givenEmployees_whenGetEmployeeInfoById_thenStatus404(String id) {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching(EMPLOYEE_URL_ID_PARAM))
                .willReturn(WireMock.aResponse().withStatus(404).withBody(employeeResponse)));

        Mono<Employee> employeeInfoById = employeeService.getEmployeeInfoById(id);

        // Then
        StepVerifier.create(employeeInfoById)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(EmployeeNotFoundException.class);
                    assertThat(error.getMessage())
                            .contains("Employee with id: 9a55c532-7457-4fe3-a8f4-6ea8a957bdb3 not found");
                })
                .verify();
    }

    @Test
    public void givenEmployees_whenGetHighestSalaryOfEmployees_thenStatus200() {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeResponse)));

        Integer highestSalary = employeeService.getHighestSalaryOfEmployees();

        // Then
        assertNotNull(highestSalary);
        assertEquals(Integer.compare(highestSalary, 0), 1);
    }

    @Test
    public void givenEmployees_whenGetTop10HighestEarningEmployeeNames_thenStatus200() {

        // When
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeResponse)));

        List<String> topTenHighestEarningEmployeeNames = employeeService.getTopTenHighestEarningEmployeeNames();

        // Then
        assertNotNull(topTenHighestEarningEmployeeNames);
        assertEquals(10, topTenHighestEarningEmployeeNames.size());
    }

    @Test
    public void givenEmployees_whenCreateEmployee_thenStatus200() {

        // When
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeRegisterResponse)));

        Mono<Employee> employeeResponse = employeeService.createEmployee(employeeRegister);

        // Then
        StepVerifier.create(employeeResponse)
                .consumeNextWith(response -> {
                    assertThat(response).isInstanceOf(Employee.class);
                    assertThat(response.getUuid()).isEqualTo(UUID.fromString("5d755f24-be1a-4e40-aa9d-d65062d7f019"));
                    assertThat(response.getEmployeeName()).isEqualTo("John Doe");
                    assertThat(response.getEmployeeSalary()).isEqualTo(10000);
                    assertThat(response.getEmployeeAge()).isEqualTo(27);
                    assertThat(response.getEmployeeEmail()).isEqualTo("holdlamis@company.com");
                    assertThat(response.getEmployeeTitle()).isEqualTo("Senior Software Engineer");
                })
                .verifyComplete();
    }

    @Test
    public void givenEmployees_whenCreateEmployee_thenStatus400() {

        // When
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse().withStatus(400)));

        Mono<Employee> employeeResponse = employeeService.createEmployee(employeeRegister);

        // Then
        StepVerifier.create(employeeResponse)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(EmployeeAPIClientException.class);
                    assertThat(error.getMessage()).contains("Client error occurred");
                })
                .verify();
    }

    @Test
    public void givenEmployees_whenCreateEmployee_thenStatus429() {

        // When
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse().withStatus(429)));

        Mono<Employee> employeeResponse = employeeService.createEmployee(employeeRegister);

        // Then
        StepVerifier.create(employeeResponse)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(EmployeeRateLimitException.class);
                    assertThat(error.getMessage()).contains("Received 429 : Too Many Request");
                })
                .verify();
    }

    @Test
    public void givenEmployees_whenCreateEmployee_thenStatus500WithErrorResponse() {

        // When
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(errorResponse)));

        Mono<Employee> employeeResponse = employeeService.createEmployee(employeeRegister);

        // Then
        StepVerifier.create(employeeResponse)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(EmployeeAPIServerException.class);
                    assertThat(error.getMessage())
                            .contains("Exception occured while processing request, mock server api.");
                })
                .verify();
    }

    @Test
    public void givenEmployees_whenCreateEmployee_thenStatus500() {

        // When
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(EMPLOYEE_URL))
                .willReturn(WireMock.aResponse().withStatus(500).withBody(errorResponse)));

        Mono<Employee> employeeResponse = employeeService.createEmployee(employeeRegister);

        // Then
        StepVerifier.create(employeeResponse)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(EmployeeAPIServerException.class);
                    assertThat(error.getMessage()).contains("Server error occurred: 500");
                })
                .verify();
    }

    @Test
    public void givenEmployees_whenDeleteEmployeeById_thenStatus200() {

        // When
        Employee employee = new Employee();
        employee.setEmployeeName("Dr. Lindsy Anderson");

        wireMockServer.stubFor(WireMock.delete(WireMock.urlPathMatching(EMPLOYEE_URL))
                .withRequestBody(WireMock.equalToJson(employeeDeletionRequest))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(employeeDeletionResponse)));

        Mono<Boolean> employeeResponse = employeeService.deleteEmployee(employee);

        // Then
        StepVerifier.create(employeeResponse)
                .consumeNextWith(response -> {
                    assertThat(response).isInstanceOf(Boolean.class);
                    assertTrue(response);
                })
                .verifyComplete();
    }

    @Test
    public void givenEmployees_whenDeleteEmployeeById_thenStatus400() {

        // When
        Employee employee = new Employee();
        employee.setEmployeeName("Dr. Lindsy Anderson");

        wireMockServer.stubFor(WireMock.delete(WireMock.urlPathMatching(EMPLOYEE_URL))
                .withRequestBody(WireMock.equalToJson(employeeDeletionRequest))
                .willReturn(WireMock.aResponse().withStatus(400)));

        Mono<Boolean> employeeResponse = employeeService.deleteEmployee(employee);

        // Then
        StepVerifier.create(employeeResponse)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(EmployeeAPIClientException.class);
                    assertThat(error.getMessage()).contains("Client error occurred: 400");
                })
                .verify();
    }

    @Test
    public void givenEmployees_whenDeleteEmployeeById_thenStatus429() {

        // When
        Employee employee = new Employee();
        employee.setEmployeeName("Dr. Lindsy Anderson");

        wireMockServer.stubFor(WireMock.delete(WireMock.urlPathMatching(EMPLOYEE_URL))
                .withRequestBody(WireMock.equalToJson(employeeDeletionRequest))
                .willReturn(WireMock.aResponse().withStatus(429)));

        Mono<Boolean> employeeResponse = employeeService.deleteEmployee(employee);

        // Then
        StepVerifier.create(employeeResponse)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(EmployeeRateLimitException.class);
                    assertThat(error.getMessage()).contains("Received 429 : Too Many Request");
                })
                .verify();
    }

    @Test
    public void givenEmployees_whenDeleteEmployeeById_thenStatus500WithErrorResponse() {

        // When
        Employee employee = new Employee();
        employee.setEmployeeName("Dr. Lindsy Anderson");

        wireMockServer.stubFor(WireMock.delete(WireMock.urlEqualTo(EMPLOYEE_URL))
                .withRequestBody(WireMock.equalToJson(employeeDeletionRequest))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(errorResponse)));

        Mono<Boolean> employeeResponse = employeeService.deleteEmployee(employee);

        // Then
        StepVerifier.create(employeeResponse)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(EmployeeAPIServerException.class);
                    assertThat(error.getMessage())
                            .contains("Exception occured while processing request, mock server api.");
                })
                .verify();
    }

    @Test
    public void givenEmployees_whenDeleteEmployeeById_thenStatus500() {

        // When
        Employee employee = new Employee();
        employee.setEmployeeName("Dr. Lindsy Anderson");

        wireMockServer.stubFor(WireMock.delete(WireMock.urlPathMatching(EMPLOYEE_URL))
                .withRequestBody(WireMock.equalToJson(employeeDeletionRequest))
                .willReturn(WireMock.aResponse().withStatus(500)));

        Mono<Boolean> employeeResponse = employeeService.deleteEmployee(employee);

        // Then
        StepVerifier.create(employeeResponse)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(EmployeeAPIServerException.class);
                    assertThat(error.getMessage()).contains("Server error occurred: 500");
                })
                .verify();
    }

    private WebClient buildWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:" + wireMockServer.port() + "/api/v1")
                .build();
    }
}
