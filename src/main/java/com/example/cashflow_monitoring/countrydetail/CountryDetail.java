package com.example.cashflow_monitoring.countrydetail;

import java.io.Serializable;
import java.util.Objects;

public class CountryDetail implements Serializable {
    private String countryCode;
    private int numberOfTransactions;

    public CountryDetail(String countryCode, int numberOfTransactions) {
        this.countryCode = countryCode;
        this.numberOfTransactions = numberOfTransactions;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(int numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountryDetail that = (CountryDetail) o;
        return getNumberOfTransactions() == that.getNumberOfTransactions() && Objects.equals(getCountryCode(), that.getCountryCode());
    }

    @Override
    public String toString() {
        return "CountryDetail{" +
                "countryCode='" + countryCode + '\'' +
                ", numberOfTransactions=" + numberOfTransactions +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCountryCode(), getNumberOfTransactions());
    }
}
