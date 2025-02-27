package com.reliaquest.api.service;

import com.reliaquest.api.config.CachingConfig;
import com.reliaquest.api.exception.EmployeeAPIClientException;
import com.reliaquest.api.exception.EmployeeAPIServerException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeRateLimitException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeDeletion;
import com.reliaquest.api.model.EmployeeRegister;
import com.reliaquest.api.model.EmployeeResponse;
import com.reliaquest.api.model.Status;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Service
public class EmployeeService {

    private final WebClient webClient;

    @Autowired
    public EmployeeService(WebClient webClient) {
        this.webClient = webClient;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeService.class);

    @Cacheable(value = CachingConfig.EMP_CACHE)
    public List<Employee> getEmployeeInfo() {

        LOGGER.info("Inside getEmployeeInfo method : EmployeeService");

        return getEmployeeResponse()
                .cache(
                        response -> response == null ? Duration.ZERO : Duration.ofMillis(Long.MAX_VALUE),
                        throwable -> Duration.ZERO,
                        () -> Duration.ZERO)
                .block();
    }

    private Mono<List<Employee>> getEmployeeResponse() {

        LOGGER.info("Inside getEmployeeResponse method : EmployeeService");

        return webClient
                .get()
                .uri("/employee")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                .bodyToMono(new ParameterizedTypeReference<EmployeeResponse<List<Employee>>>() {})
                .retryWhen(performRetryBackOffSpec())
                .onErrorResume(WebClientException.class, e -> {
                    LOGGER.info("WebClientException occurred: {}", e.getMessage());
                    return Mono.error(e);
                })
                .map(this::processResponse);
    }

    public List<Employee> getEmployeesByNameSearch(String searchString) {

        LOGGER.info("Inside getEmployeesByNameSearch method : EmployeeService");

        return getEmployeeInfo().stream()
                .filter(employee -> employee.getEmployeeName().toLowerCase().contains(searchString.toLowerCase())
                        || employee.getEmployeeName().toLowerCase().matches(searchString.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Mono<Employee> getEmployeeInfoById(String id) {

        LOGGER.info("Inside getEmployeeInfoById method : EmployeeService");

        return webClient
                .get()
                .uri("/employee/{id}", id)
                .retrieve()
                .onStatus(status -> HttpStatus.NOT_FOUND == status, response -> {
                    LOGGER.info(
                            "Received http status code: {}, from Mock Employee API",
                            response.statusCode().value());
                    return Mono.error(new EmployeeNotFoundException("Employee with id: " + id + " not found"));
                })
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                .bodyToMono(new ParameterizedTypeReference<EmployeeResponse<Employee>>() {})
                .retryWhen(performRetryBackOffSpec())
                .onErrorResume(WebClientException.class, e -> {
                    LOGGER.info("WebClientException occurred: {}", e.getMessage());
                    return Mono.error(e);
                })
                .map(this::processResponse);
    }

    public Integer getHighestSalaryOfEmployees() {

        LOGGER.info("Inside getHighestSalaryOfEmployees method : EmployeeService");

        return getEmployeeInfo().stream()
                .mapToInt(Employee::getEmployeeSalary)
                .max()
                .orElseThrow(NoSuchElementException::new);
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {

        LOGGER.info("Inside getTopTenHighestEarningEmployeeNames method : EmployeeService");

        return getEmployeeInfo().stream()
                .sorted(Comparator.comparingInt(Employee::getEmployeeSalary).reversed())
                .limit(10)
                .map(Employee::getEmployeeName)
                .collect(Collectors.toList());
    }

    public Mono<Employee> createEmployee(EmployeeRegister employeeInput) {

        LOGGER.info("Inside createEmployee method : EmployeeService");

        return webClient
                .post()
                .uri("/employee")
                .body(BodyInserters.fromValue(employeeInput))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                .bodyToMono(new ParameterizedTypeReference<EmployeeResponse<Employee>>() {})
                .retryWhen(performRetryBackOffSpec())
                .onErrorResume(WebClientException.class, e -> {
                    LOGGER.info("WebClientException occurred: {}", e.getMessage());
                    return Mono.error(e);
                })
                .map(this::processResponse);
    }

    public Mono<Boolean> deleteEmployee(Employee employee) {

        LOGGER.info("Inside deleteEmployee method : EmployeeService");

        EmployeeDeletion employeeDeletion = new EmployeeDeletion(employee.getEmployeeName());

        return webClient
                .method(HttpMethod.DELETE)
                .uri("/employee")
                .body(BodyInserters.fromValue(employeeDeletion))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                .bodyToMono(new ParameterizedTypeReference<EmployeeResponse<Boolean>>() {})
                .retryWhen(performRetryBackOffSpec())
                .onErrorResume(WebClientException.class, e -> {
                    LOGGER.info("WebClientException occurred: {}", e.getMessage());
                    return Mono.error(e);
                })
                .map(this::processResponse);
    }

    private Mono<? extends Throwable> handleClientError(ClientResponse response) {

        HttpStatusCode statusCode = response.statusCode();

        LOGGER.info("Received http status code: {}, from Mock Employee API", statusCode.value());

        if (statusCode == HttpStatus.TOO_MANY_REQUESTS) {

            return Mono.error(new EmployeeRateLimitException(
                    "Your request limit has been reached. Please try again in some time."));

        } else {

            return Mono.error(new EmployeeAPIClientException(
                    "Client error occurred: " + response.statusCode().value()));
        }
    }

    private Mono<? extends Throwable> handleServerError(ClientResponse response) {

        LOGGER.info(
                "Received http status code: {}, from Mock Employee API",
                response.statusCode().value());

        return Mono.error(new EmployeeAPIServerException(
                "Server error occurred: " + response.statusCode().value()));
    }

    private static RetryBackoffSpec performRetryBackOffSpec() {
        // https://www.couchbase.com/blog/spring-webclient-429-ratelimit-errors/
        return Retry.backoff(3, Duration.ofSeconds(5))
                .jitter(0.5)
                .filter(throwable -> throwable instanceof EmployeeRateLimitException)
                .doAfterRetry(retry -> {
                    LOGGER.info("External call to Mock Employee API failed, retry {}", retry.totalRetries() + 1);
                })
                .onRetryExhaustedThrow((retryspec, retry) ->
                        new EmployeeRateLimitException("Received 429 : Too Many Request", retry.failure()));
    }

    private <T> T processResponse(EmployeeResponse<T> response) {

        LOGGER.info(
                "Response status: {}, Message: {}",
                response.getStatus(),
                response.getStatus().getValue());

        if (response != null && response.getStatus() == Status.HANDLED) {

            Object obj = response.getData();

            if ((obj instanceof List<?>) || (obj instanceof Employee) || (obj instanceof Boolean)) {

                return response.getData();
            }
        } else if (response != null && response.getStatus() == Status.ERROR) {
            String error = response.getError();
            throw new EmployeeAPIServerException(error);
        }

        return null;
    }

    @CacheEvict(
            allEntries = true,
            cacheNames = {CachingConfig.EMP_CACHE})
    @Scheduled(fixedDelay = CachingConfig.EMP_CACHE_TTL)
    public void cacheEvict() {
        LOGGER.info("Clearing caches");
    }
}
