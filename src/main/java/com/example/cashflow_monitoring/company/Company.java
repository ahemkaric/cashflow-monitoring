package com.example.cashflow_monitoring.company;

import java.util.List;

public record Company(
        Integer id,
        List<String> ibans,
        String name,
        String address
) {
}
