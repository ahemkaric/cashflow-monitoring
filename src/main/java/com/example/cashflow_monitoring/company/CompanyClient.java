package com.example.cashflow_monitoring.company;

import com.example.cashflow_monitoring.exception.BadRequestException;
import com.example.cashflow_monitoring.exception.InternalServerErrorException;
import com.example.cashflow_monitoring.exception.NotFoundException;
import com.example.cashflow_monitoring.util.UrlBuilderUtils;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.example.cashflow_monitoring.util.Constants.BYTE_COUNT;

@Service
public class CompanyClient {
    private static final Logger log = LoggerFactory.getLogger(CompanyClient.class);

    private final WebClient webClient;
    private final UrlBuilderUtils urlBuilderUtils;

    public CompanyClient(WebClient.Builder webClientBuilder, UrlBuilderUtils urlBuilderUtils, @Value("${external.db.api.base-url}") String externalApiBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(externalApiBaseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(BYTE_COUNT))
                        .build())
                .build();
        this.urlBuilderUtils = urlBuilderUtils;
    }

    public Mono<List<CompanyDTO>> getCompanies(@Nullable Integer limit, @Nullable Integer afterId) {
        String url = urlBuilderUtils.buildCompaniesUrl(limit, afterId);
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus.BAD_REQUEST::equals, response -> {
                    log.error("Client error while fetching companies from {}", url);
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new BadRequestException(body)));
                })
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, response -> {
                    log.error("Server error while fetching companies from {}", url);
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new InternalServerErrorException(body, null)));
                })
                .bodyToMono(CompanyDTO[].class)
                .map(response -> response != null ? Arrays.asList(response) : Collections.<CompanyDTO>emptyList())
                .doOnSuccess(result -> log.info("Successfully fetched {} companies from {}", result.size(), url))
                .doOnError(e -> log.error("Failed to fetch companies from {}", url, e));

    }

    public Mono<CompanyDTO> getCompanyById(Integer companyId) {
        String url = urlBuilderUtils.buildCompanyUrl(companyId);
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response -> {
                    log.error("Company with ID {} not found at {}", companyId, url);
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new NotFoundException("Company not found: " + companyId)));
                })
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, response -> {
                    log.error("Server error while fetching company with ID {} from {}", companyId, url);
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new InternalServerErrorException(body, null)));
                })
                .bodyToMono(CompanyDTO.class)
                .doOnSuccess(result -> log.info("Successfully fetched company with ID {} from {}", companyId, url))
                .doOnError(e -> log.error("Failed to fetch company with ID {} from {}", companyId, url, e));

    }

}