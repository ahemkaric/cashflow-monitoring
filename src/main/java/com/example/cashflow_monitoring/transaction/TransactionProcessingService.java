package com.example.cashflow_monitoring.transaction;

import com.example.cashflow_monitoring.company.CompanyService;
import com.example.cashflow_monitoring.companyinfo.CompanyInfo;
import com.example.cashflow_monitoring.companyinfo.CompanyInfoService;
import com.example.cashflow_monitoring.companyinfo.IbanToCompanyIdMapCache;
import com.example.cashflow_monitoring.exception.NotFoundException;
import com.example.cashflow_monitoring.exchangerate.ExchangeRate;
import com.example.cashflow_monitoring.exchangerate.ExchangeRateService;
import com.example.cashflow_monitoring.transaction.sepa.SepaTransaction;
import com.example.cashflow_monitoring.transaction.sepa.SepaTransactionParams;
import com.example.cashflow_monitoring.transaction.swift.SwiftTransaction;
import com.example.cashflow_monitoring.transaction.swift.SwiftTransactionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.cashflow_monitoring.util.Constants.DEFAULT_LIMIT_FOR_REQUESTS;

@Service
public class TransactionProcessingService {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessingService.class);
    private final TransactionService<SepaTransactionParams, SepaTransaction> sepaTransactionService;
    private final TransactionService<SwiftTransactionParams, SwiftTransaction> swiftTransactionService;
    private final CompanyService companyService;
    private final IbanToCompanyIdMapCache ibanToCompanyIdMapCache;
    private final ExchangeRateService exchangeRateService;
    private final CompanyInfoService companyInfoService;

    public TransactionProcessingService(
            TransactionService<SepaTransactionParams, SepaTransaction> sepaTransactionService,
            TransactionService<SwiftTransactionParams, SwiftTransaction> swiftTransactionService, CompanyService companyService, IbanToCompanyIdMapCache ibanToCompanyIdMapCache, ExchangeRateService exchangeRateService, CompanyInfoService companyInfoService) {
        this.sepaTransactionService = sepaTransactionService;
        this.swiftTransactionService = swiftTransactionService;
        this.companyService = companyService;
        this.ibanToCompanyIdMapCache = ibanToCompanyIdMapCache;
        this.exchangeRateService = exchangeRateService;
        this.companyInfoService = companyInfoService;
    }

    public Mono<TransactionResponseDTO> getTransactionsWithinTimeRange(Integer companyId, Integer limit,
                                                                       String afterTimestamp, String beforeTimestamp) {
        return companyService.getCompanyById(companyId)
                .switchIfEmpty(Mono.error(new NotFoundException("Could not find companyInfo for this CompanyId")))
                .flatMap(company -> getTransactionsFromIbans(company.ibans(), limit, afterTimestamp, beforeTimestamp, companyId));
    }

    private Mono<TransactionResponseDTO> getTransactionsFromIbans(List<String> ibans, Integer limit, String afterTimestamp,
                                                                  String beforeTimestamp, Integer companyId) {
        log.info("processing ibans: {} for companyId: {}", ibans, companyId);
        return Flux.fromIterable(ibans)
                .flatMap(iban -> getAllTransactionForIban(iban, afterTimestamp, beforeTimestamp, limit))
                .collectList()
                .flatMap(this::aggregateTransactionResponses);
    }

    private Mono<TransactionResponseDTO> aggregateTransactionResponses(List<Tuple4<List<SepaTransaction>, List<SepaTransaction>,
            List<SwiftTransaction>, List<SwiftTransaction>>> tuples) {
        List<SepaTransaction> allPayerTransactions = new ArrayList<>();
        List<SepaTransaction> allReceiverTransactions = new ArrayList<>();
        List<SwiftTransaction> allSenderTransactions = new ArrayList<>();
        List<SwiftTransaction> allBeneficiaryTransactions = new ArrayList<>();

        for (Tuple4<List<SepaTransaction>, List<SepaTransaction>, List<SwiftTransaction>, List<SwiftTransaction>> tuple : tuples) {
            allPayerTransactions.addAll(tuple.getT1());
            allReceiverTransactions.addAll(tuple.getT2());
            allSenderTransactions.addAll(tuple.getT3());
            allBeneficiaryTransactions.addAll(tuple.getT4());
        }
        allPayerTransactions.addAll(allReceiverTransactions);
        allSenderTransactions.addAll(allBeneficiaryTransactions);
        TransactionResponseDTO transactionResponseDTO = new TransactionResponseDTO(
                allPayerTransactions, allSenderTransactions
        );
        return Mono.just(transactionResponseDTO);
    }

    private Mono<Tuple4<List<SepaTransaction>, List<SepaTransaction>, List<SwiftTransaction>,
            List<SwiftTransaction>>> getAllTransactionForIban(String iban, String afterTimestamp,
                                                              String beforeTimestamp, Integer limit) {
        return Mono.zip(
                handleGetTransactionsPaginated(
                        sepaTransactionService,
                        getSepaTransactionParamsForPayer(iban, afterTimestamp, beforeTimestamp, limit)
                ),
                handleGetTransactionsPaginated(
                        sepaTransactionService,
                        getSepaTransactionParamsForReceiver(iban, afterTimestamp, beforeTimestamp, limit)
                ),
                handleGetTransactionsPaginated(
                        swiftTransactionService,
                        getSwiftTransactionParamsForSender(iban, afterTimestamp, beforeTimestamp, limit)
                ),
                handleGetTransactionsPaginated(
                        swiftTransactionService,
                        getSwiftTransactionParamsForBeneficiary(iban, afterTimestamp, beforeTimestamp, limit)
                )
        );
    }

    private <P extends TransactionParams, R extends Transaction> Mono<List<R>>
    handleGetTransactionsPaginated(TransactionService<P, R> service, P params) {
        return service.getPaginatedTransactions(params);
    }

    private Mono<List<SepaTransaction>> getSepaTransactionsBatch(String sepaAfterTimestamp, String sepaAfterUuid) {
        return handleGetTransactionsPaginated(
                sepaTransactionService,
                new SepaTransactionParams(DEFAULT_LIMIT_FOR_REQUESTS, sepaAfterTimestamp, sepaAfterUuid, null, null, null)
        );
    }

    private Mono<List<SwiftTransaction>> getSwiftTransactionsBatch(String swiftAfterTimestamp, String swiftAfterUuid) {
        return handleGetTransactionsPaginated(
                swiftTransactionService,
                new SwiftTransactionParams(DEFAULT_LIMIT_FOR_REQUESTS, swiftAfterTimestamp, swiftAfterUuid, null, null, null)
        );
    }

    private Mono<Tuple2<List<SepaTransaction>, List<SwiftTransaction>>> getTransactionsBatch(
            String sepaAfterTimestamp, String sepaAfterUuid, String swiftAfterTimestamp, String swiftAfterUuid) {
        return Mono.zip(getSepaTransactionsBatch(sepaAfterTimestamp, sepaAfterUuid),
                getSwiftTransactionsBatch(swiftAfterTimestamp, swiftAfterUuid));
    }

    private <P extends TransactionParams, R extends Transaction> Mono<Void>
    processTransactions(List<R> transactions,
                        TransactionService<P, R> transactionService, Mono<List<ExchangeRate>> exchangeRates,
                        Mono<Map<String, Integer>> ibanToCompanyIdMap) {

        return Flux.fromIterable(transactions)
                .flatMap(transaction -> transactionService.processTransaction(transaction, exchangeRates, ibanToCompanyIdMap))
                .then();
    }

    private Mono<Tuple4<String, String, String, String>> processTransactionsBatch(
            String sepaAfterTimestamp, String sepaAfterUuid, String swiftAfterTimestamp, String swiftAfterUuid) {
        Mono<List<ExchangeRate>> exchangeRates = exchangeRateService.getExchangeRates().cache(Duration.ofHours(1));
        log.info("Processing all transactions");
        return getTransactionsBatch(sepaAfterTimestamp, sepaAfterUuid, swiftAfterTimestamp, swiftAfterUuid)
                .flatMap(tuple -> {
                    List<SepaTransaction> sepaTransactions = tuple.getT1();
                    List<SwiftTransaction> swiftTransactions = tuple.getT2();
                    log.info("Processing {} sepaTransactions", sepaTransactions.size());
                    log.info("Processing {} swiftTransactions", swiftTransactions.size());

                    Mono<Map<String, Integer>> ibanToCompanyIdMap = ibanToCompanyIdMapCache.getIbanToCompanyIdMap();

                    Mono<Void> sepaProcessing = processTransactions(sepaTransactions, sepaTransactionService,
                            exchangeRates, ibanToCompanyIdMap);
                    Mono<Void> swiftProcessing = processTransactions(swiftTransactions, swiftTransactionService,
                            exchangeRates, ibanToCompanyIdMap);

                    return Mono.when(sepaProcessing, swiftProcessing)
                            .then(Mono.defer(() -> {
                                String newSepaAfterTimestamp = sepaTransactions.isEmpty() ? sepaAfterTimestamp
                                        : String.valueOf(sepaTransactions.get(sepaTransactions.size() - 1).timestamp());
                                String newSepaAfterUuid = sepaTransactions.isEmpty() ? sepaAfterUuid
                                        : String.valueOf(sepaTransactions.get(sepaTransactions.size() - 1).id());
                                String newSwiftAfterTimestamp = swiftTransactions.isEmpty() ? swiftAfterTimestamp
                                        : String.valueOf(swiftTransactions.get(swiftTransactions.size() - 1).timestamp());
                                String newSwiftAfterUuid = swiftTransactions.isEmpty() ? swiftAfterUuid
                                        : String.valueOf(swiftTransactions.get(swiftTransactions.size() - 1).id());

                                return Mono.just(Tuples.of(newSepaAfterTimestamp, newSepaAfterUuid,
                                        newSwiftAfterTimestamp, newSwiftAfterUuid));
                            }));
                })
                .doOnError(e -> log.error("Error processing transactions", e));
    }

    public Mono<Tuple4<String, String, String, String>> processNewTransactions(Integer limit) {
        Integer finalLimit = limit == null || limit > 5 ? 5 : limit;
        return Mono.zip(
                        companyInfoService.findTopByOrderByLastSepaTransactionTimestampDesc(),
                        companyInfoService.findTopByOrderByLastSwiftTransactionTimestampDesc()
                )
                .flatMap(tuple -> {
                    CompanyInfo sepa = tuple.getT1();
                    CompanyInfo swift = tuple.getT2();
                    String sepaAfterTimestamp = sepa.getLastSepaTransactionTimestamp() != null ?
                            String.valueOf(sepa.getLastSepaTransactionTimestamp()) : "2000-01-01T00:00:00.000000Z";
                    String sepaAfterUuid = sepa.getLastSepaTransactionId() != null ?
                            String.valueOf(sepa.getLastSepaTransactionId()) : "00000000-0000-0000-0000-000000000000";
                    String swiftAfterTimestamp = swift.getLastSwiftTransactionId() != null ?
                            String.valueOf(swift.getLastSwiftTransactionTimestamp()) : "2000-01-01T00:00:00.000000Z";
                    String swiftAfterUuid = swift.getLastSwiftTransactionId() != null ?
                            String.valueOf(swift.getLastSwiftTransactionId()) : "00000000-0000-0000-0000-000000000000";
                    AtomicInteger attempt = new AtomicInteger();
                    return processAllTransactions(sepaAfterTimestamp, sepaAfterUuid, swiftAfterTimestamp, swiftAfterUuid, finalLimit, attempt);
                });
    }

    private Mono<Tuple4<String, String, String, String>> processAllTransactions(
            String sepaAfterTimestamp, String sepaAfterUuid,
            String swiftAfterTimestamp, String swiftAfterUuid, Integer limit, AtomicInteger attempt) {
        return processTransactionsBatch(sepaAfterTimestamp, sepaAfterUuid, swiftAfterTimestamp, swiftAfterUuid)
                .flatMap(result -> {
                    String newSepaAfterTimestamp = result.getT1();
                    String newSepaAfterUuid = result.getT2();
                    String newSwiftAfterTimestamp = result.getT3();
                    String newSwiftAfterUuid = result.getT4();

                    if (newSepaAfterTimestamp.equals(sepaAfterTimestamp) &&
                            newSepaAfterUuid.equals(sepaAfterUuid) &&
                            newSwiftAfterTimestamp.equals(swiftAfterTimestamp) &&
                            newSwiftAfterUuid.equals(swiftAfterUuid)) {
                        return Mono.just(result);
                    }

                    if (attempt.incrementAndGet() < limit) {
                        return processAllTransactions(newSepaAfterTimestamp, newSepaAfterUuid,
                                newSwiftAfterTimestamp, newSwiftAfterUuid, limit, attempt);
                    }
                    return Mono.just(result);
                });
    }

    private SepaTransactionParams getSepaTransactionParamsForPayer(String iban, String afterTimestamp, String beforeTimestamp, Integer limit) {
        return new SepaTransactionParams(
                limit == null ? DEFAULT_LIMIT_FOR_REQUESTS : limit,
                afterTimestamp,
                beforeTimestamp,
                null,
                iban,
                null
        );
    }

    private SepaTransactionParams getSepaTransactionParamsForReceiver(String iban, String afterTimestamp, String beforeTimestamp, Integer limit) {
        return new SepaTransactionParams(
                limit == null ? DEFAULT_LIMIT_FOR_REQUESTS : limit,
                afterTimestamp,
                beforeTimestamp,
                null,
                null,
                iban
        );
    }

    private SwiftTransactionParams getSwiftTransactionParamsForSender(String iban, String afterTimestamp, String beforeTimestamp, Integer limit) {
        return new SwiftTransactionParams(
                limit == null ? DEFAULT_LIMIT_FOR_REQUESTS : limit,
                afterTimestamp,
                beforeTimestamp,
                null,
                iban,
                null
        );
    }

    private SwiftTransactionParams getSwiftTransactionParamsForBeneficiary(String iban, String afterTimestamp, String beforeTimestamp, Integer limit) {
        return new SwiftTransactionParams(
                limit == null ? DEFAULT_LIMIT_FOR_REQUESTS : limit,
                afterTimestamp,
                beforeTimestamp,
                null,
                null,
                iban
        );
    }


}
