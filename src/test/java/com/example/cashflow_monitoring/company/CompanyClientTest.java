package com.example.cashflow_monitoring.company;

import com.example.cashflow_monitoring.exception.InternalServerErrorException;
import com.example.cashflow_monitoring.exception.NotFoundException;
import com.example.cashflow_monitoring.util.UrlBuilderUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.example.cashflow_monitoring.util.TestDataGenerator.generateCompanyDTOs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompanyClientTest {
    private static CompanyClient companyClient;

    public static MockWebServer mockWebServer;

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        objectMapper = new ObjectMapper();
        var baseUrl = mockWebServer.url("/").toString();
        var webClient = WebClient.builder().baseUrl(baseUrl);
        var urlBuilderUtils = new UrlBuilderUtils(baseUrl);
        companyClient = new CompanyClient(webClient, urlBuilderUtils, baseUrl);

    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getCompanies_happyPathTest() throws InterruptedException, JsonProcessingException {
        var random = new Random();
        var limit = random.nextInt(10);
        var mockCompanyDTOS = generateCompanyDTOs(limit);
        mockWebServer
                .enqueue(new MockResponse()
                        .setBody(objectMapper.writeValueAsString(mockCompanyDTOS))
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        var result = companyClient.getCompanies(limit, null);
        StepVerifier
                .create(result)
                .expectNextMatches(responseCompanyDTOS ->
                        responseCompanyDTOS.size() == limit && responseCompanyDTOS.equals(mockCompanyDTOS))
                .verifyComplete();
        var recordedRequest = mockWebServer.takeRequest();
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
        assertEquals("/companies?limit=" + limit, recordedRequest.getPath());
    }

    @Test
    void getCompanyById_happyPathTest() throws JsonProcessingException, InterruptedException {
        var companyDTO = generateCompanyDTOs(1).getFirst();
        var id = companyDTO.id();
        mockWebServer
                .enqueue(new MockResponse()
                        .setBody(objectMapper.writeValueAsString(companyDTO))
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        var result = companyClient.getCompanyById(id);
        StepVerifier
                .create(result)
                .expectNextMatches(responseCompanyDTO ->
                        responseCompanyDTO.id().equals(id) && responseCompanyDTO.equals(companyDTO))
                .verifyComplete();
        var recordedRequest = mockWebServer.takeRequest();
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
        assertEquals("/companies/" + id, recordedRequest.getPath());
    }

    @Test
    void getCompanyById_notFoundTest() throws InterruptedException {
        var companyDTO = generateCompanyDTOs(1).getFirst();
        var id = companyDTO.id();
        mockWebServer
                .enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));
        var result = companyClient.getCompanyById(id);
        StepVerifier
                .create(result)
                .expectError(NotFoundException.class)
                .verify();
        var recordedRequest = mockWebServer.takeRequest();
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
        assertEquals("/companies/" + id, recordedRequest.getPath());
    }

    @Test
    void getCompanyById_internalServerErrorTest() throws InterruptedException {
        var companyDTO = generateCompanyDTOs(1).getFirst();
        var id = companyDTO.id();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setBody("Internal server error"));
        var result = companyClient.getCompanyById(id);
        StepVerifier
                .create(result)
                .expectError(InternalServerErrorException.class)
                .verify();
        var recordedRequest = mockWebServer.takeRequest();
        assertEquals(HttpMethod.GET.name(), recordedRequest.getMethod());
        assertEquals("/companies/" + id, recordedRequest.getPath());
    }

    @Test
    void getCompanyById_invalidIdTest() {
        Integer invalidId = null;
        assertThrows(IllegalArgumentException.class, () -> {
            companyClient.getCompanyById(invalidId).block();
        });
    }

    @Test
    void getCompanyById_timeoutTest() throws JsonProcessingException {
        var companyDTO = generateCompanyDTOs(1).getFirst();
        var id = companyDTO.id();
        mockWebServer.enqueue(new MockResponse()
                .setBodyDelay(5, TimeUnit.SECONDS)
                .setResponseCode(HttpStatus.OK.value())
                .setBody(objectMapper.writeValueAsString(companyDTO)));
        var result = companyClient.getCompanyById(id);
        StepVerifier.create(result)
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    void getCompanyById_unexpectedStatusCodeTest() {
        var companyDTO = generateCompanyDTOs(1).getFirst();
        var id = companyDTO.id();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.FORBIDDEN.value()));
        var result = companyClient.getCompanyById(id);
        StepVerifier.create(result)
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    void getCompanyById_networkIssueTest() {
        var companyDTO = generateCompanyDTOs(1).getFirst();
        var id = companyDTO.id();

        // Here we need to simulate a network issue.
        StepVerifier.create(companyClient.getCompanyById(id))
                .expectError(WebClientRequestException.class)
                .verify();
    }

    @Test
    void getCompanyById_loggingTest() throws JsonProcessingException {
        var companyDTO = generateCompanyDTOs(1).getFirst();
        var id = companyDTO.id();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(objectMapper.writeValueAsString(companyDTO)));
        companyClient.getCompanyById(id).block();
    }

    @Test
    void getCompanyById_emptyResponseBodyTest() {
        var companyDTO = generateCompanyDTOs(1).getFirst();
        var id = companyDTO.id();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(""));
        var result = companyClient.getCompanyById(id);
        StepVerifier.create(result)
                .expectError()
                .verify();
    }

}