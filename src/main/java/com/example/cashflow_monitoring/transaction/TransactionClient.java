package com.example.cashflow_monitoring.transaction;

import com.example.cashflow_monitoring.exception.BadRequestException;
import com.example.cashflow_monitoring.exception.InternalServerErrorException;
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
public class TransactionClient {

    private static final Logger log = LoggerFactory.getLogger(TransactionClient.class);

    private final WebClient webClient;

    public TransactionClient(WebClient.Builder webClientBuilder, @Value("${external.db.api.base-url}") String externalApiBaseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(externalApiBaseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(BYTE_COUNT))
                        .build())
                .build();
    }

    public <T extends TransactionDTO> Mono<List<T>> getTransactions(String url, Class<T[]> responseType) {
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
                .bodyToMono(responseType)
                .map(response -> response != null ? Arrays.asList(response) : Collections.<T>emptyList())
                .doOnSuccess(result -> log.info("Successfully fetched {} items from {}", result.size(), url))
                .doOnError(e -> log.error("Error fetching items from {}: {}", url, e.getMessage()));
    }
}
