package com.example.cashflow_monitoring.cashflow;

import com.example.cashflow_monitoring.company.CompanyDTO;
import com.example.cashflow_monitoring.company.CompanyMapper;
import com.example.cashflow_monitoring.company.CompanyService;
import com.example.cashflow_monitoring.companyinfo.CompanyInfo;
import com.example.cashflow_monitoring.companyinfo.CompanyInfoService;
import com.example.cashflow_monitoring.countrydetail.CountryDetail;
import com.example.cashflow_monitoring.countrydetail.CountryDetailService;
import com.example.cashflow_monitoring.exception.NotFoundException;
import com.example.cashflow_monitoring.transaction.TransactionProcessingService;
import com.example.cashflow_monitoring.transaction.TransactionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;

import java.math.BigDecimal;
import java.util.List;

import static com.example.cashflow_monitoring.util.Constants.*;

@RestController
@RequestMapping("/api/v1/companies")
@Tag(name = "Cashflow Management")
public class CashflowController {
    private static final Logger log = LoggerFactory.getLogger(CashflowController.class);
    private final CompanyService companyService;
    private final CompanyInfoService companyInfoService;
    private final CountryDetailService countryDetailService;
    private final TransactionProcessingService transactionProcessingService;
    private final CompanyMapper companyMapper;

    public CashflowController(CompanyService companyService, CompanyInfoService companyInfoService,
                              CountryDetailService countryDetailService, TransactionProcessingService transactionProcessingService,
                              CompanyMapper companyMapper) {
        this.companyService = companyService;
        this.companyInfoService = companyInfoService;
        this.countryDetailService = countryDetailService;
        this.transactionProcessingService = transactionProcessingService;
        this.companyMapper = companyMapper;
    }

    @Operation(summary = "get all companies")
    @GetMapping
    public Mono<ResponseEntity<List<CompanyDTO>>> getAllCompanies(@Parameter(description = "Maximum number of items to retrieve in a single batch")
                                                                  @RequestParam(name = LIMIT, required = false) Integer limit,
                                                                  @Parameter(description = "The ID after which the next batch of companies should start")
                                                                  @RequestParam(name = AFTER_ID, required = false) Integer afterId) {
        return companyService.getAllCompaniesPaginated(limit, afterId)
                .map(companyMapper::toDTOList)
                .map(this::toResponseEntity)
                .onErrorResume(this::handleCompaniesError);
    }

    @Operation(summary = "get current balance of a company in EUR")
    @GetMapping("/{id}/balance")
    public Mono<ResponseEntity<BigDecimal>> getCompanyBalanceByCompanyId(
            @Parameter(description = "Unique identifier of the company to be retrieved")
            @PathVariable Integer id) {
        return companyInfoService.getCompanyBalanceByCompanyId(id)
                .map(this::toResponseEntity)
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
                .onErrorResume(this::handleCompanyBalanceError);
    }


    @Operation(summary = "get country code with number of transactions")
    @GetMapping("/{id}/country-details")
    public Mono<ResponseEntity<List<CountryDetail>>> getCountryDetailsByCompanyId(
            @Parameter(description = "Unique identifier of the company to be retrieved")
            @PathVariable Integer id) {
        return countryDetailService.getCountryDetailsById(id)
                .map(this::toResponseEntity)
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
                .onErrorResume(this::handleCountryDetailError);
    }

    @Operation(summary = "get transactions of a company within time range")
    @GetMapping("/{id}/transactions")
    public Mono<ResponseEntity<TransactionResponseDTO>> getTransactions(
            @Parameter(description = "Unique identifier of the company to be retrieved") @PathVariable Integer id,
            @Parameter(description = "Maximum number of items to retrieve in a single batch") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Filter for transactions occurring after this starting Timestamp (ISO8601)")
            @RequestParam(name = AFTER_TIMESTAMP, required = false) String afterTimestamp,
            @Parameter(description = "Filter for transactions occurring before this ending Timestamp (ISO8601)")
            @RequestParam(name = BEFORE_TIMESTAMP, required = false) String beforeTimestamp) {
        return transactionProcessingService.getTransactionsWithinTimeRange(id, limit, afterTimestamp, beforeTimestamp)
                .map(this::toResponseEntity)
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
                .onErrorResume(this::handleTransactionError);
    }

    @Operation(summary = "update the database by fetching new companies")
    @PostMapping("/update")
    public Mono<ResponseEntity<List<CompanyInfo>>> updateNewCompaniesToDb(@RequestParam(required = false) Integer limit) {
        return companyInfoService.getAndSaveNewCompanies(limit)
                .map(this::toResponseEntity);
    }

    @Operation(summary = "update the database by processing new transactions")
    @PostMapping("/update/transactions")
    public Mono<ResponseEntity<Tuple4<String, String, String, String>>>
    processNewTransactions(@Parameter(description = "Maximum number of times to get new list of transactions")
                           @RequestParam(required = false) Integer limit) {
        return transactionProcessingService.processNewTransactions(limit)
                .map(this::toResponseEntity);
    }

    private Mono<ResponseEntity<BigDecimal>> handleCompanyBalanceError(Throwable e) {
        log.error("Error fetching balance for company: ", e);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    private <T> ResponseEntity<T> toResponseEntity(T body) {
        return body != null ? ResponseEntity.ok(body) : ResponseEntity.noContent().build();
    }

    private Mono<ResponseEntity<List<CompanyDTO>>> handleCompaniesError(Throwable e) {
        log.error("Error retrieving companies", e);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    private Mono<ResponseEntity<List<CountryDetail>>> handleCountryDetailError(Throwable e) {
        log.error("Error fetching country details: ", e);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    private Mono<ResponseEntity<TransactionResponseDTO>> handleTransactionError(Throwable e) {
        log.error("Error fetching transactions: ", e);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

}
