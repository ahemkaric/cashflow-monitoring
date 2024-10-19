package com.example.cashflow_monitoring.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                description = "Application to Monitor and Analyze Financial Transactions for Companies",
                title = "CashFlow Monitoring Application",
                version = "1.0.0"
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "http://localhost:8081"
                )
        }
)
public class OpenApiConfig {
}
