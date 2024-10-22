package com.example.cashflow_monitoring.util;

import com.example.cashflow_monitoring.company.CompanyDTO;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestDataGenerator {

    public static List<CompanyDTO> generateCompanyDTOs(int count) {
        List<CompanyDTO> companyDTOList = new ArrayList<>();
        var random = new Random();
        for (int i = 0; i < count; i++) {
            var randomInt = random.nextInt(1000);
            var companyDTO = new CompanyDTO(randomInt, RandomStringUtils.random(5),
                    RandomStringUtils.random(5), RandomStringUtils.random(5));
            companyDTOList.add(companyDTO);
        }
        return companyDTOList;
    }

}
