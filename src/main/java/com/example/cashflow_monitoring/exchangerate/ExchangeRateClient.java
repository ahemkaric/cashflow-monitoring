package com.example.cashflow_monitoring.exchangerate;

import com.example.cashflow_monitoring.exception.InternalServerErrorException;
import com.example.cashflow_monitoring.util.UrlBuilderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class ExchangeRateClient {
    private static final Logger log = LoggerFactory.getLogger(ExchangeRateClient.class);

    private final WebClient webClient;
    private final UrlBuilderUtils urlBuilderUtils;

    public ExchangeRateClient(WebClient.Builder webClientBuilder, UrlBuilderUtils urlBuilderUtils, @Value("${external.db.api.base-url}") String externalApiBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(externalApiBaseUrl).build();
        this.urlBuilderUtils = urlBuilderUtils;
    }

    public Mono<List<ExchangeRateDTO>> getExchangeRates() {
        String url = urlBuilderUtils.buildExchangeRateUrl();
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, response -> {
                    log.error("Server error while fetching exchange rates from {}", url);
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new InternalServerErrorException(body, null)));
                })
                .bodyToMono(ExchangeRateDTO[].class)
                .map(response -> response != null ? Arrays.asList(response) : Collections.<ExchangeRateDTO>emptyList())
                .doOnSuccess(result -> log.info("Successfully fetched {} exchange rates from {}", result.size(), url))
                .doOnError(e -> log.error("Failed to fetch exchange rates", e));
    }
}
