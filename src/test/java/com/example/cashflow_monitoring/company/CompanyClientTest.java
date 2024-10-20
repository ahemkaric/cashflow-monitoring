package com.example.cashflow_monitoring.company;

import com.example.cashflow_monitoring.util.UrlBuilderUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompanyClientTest {
    private static CompanyClient companyClient;

    public static MockWebServer mockWebServer;

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void initialize() {
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
    void getCompanies() throws InterruptedException, JsonProcessingException {
        var companyDTO = new CompanyDTO(1, "xyz", "abc", "pqr");

        mockWebServer
                .enqueue(new MockResponse()
                        .setBody(objectMapper.writeValueAsString(Collections.singletonList(companyDTO)))
                        .addHeader("Content-Type", "application/json"));
        var limit = 10;
        var result = companyClient.getCompanies(limit, null);
        StepVerifier
                .create(result)
                .expectNextMatches(companies -> companies.size() == 1 && companies.get(0).equals(companyDTO))
                .verifyComplete();

        var recordedRequest = mockWebServer.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/companies?limit=10", recordedRequest.getPath());
    }

    @Test
    void getCompanyById() {
    }
}