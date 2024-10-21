package com.example.cashflow_monitoring.companyinfo;

import com.example.cashflow_monitoring.countrydetail.CountryDetail;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Document(collection = "company_info")
public class CompanyInfo implements Serializable {
    @Id
    private String id;

    @Indexed(unique = true)
    private Integer companyId;

    private BigDecimal balanceEur = BigDecimal.ZERO;
    private UUID lastSepaTransactionId;
    private OffsetDateTime lastSepaTransactionTimestamp;
    private UUID lastSwiftTransactionId;
    private OffsetDateTime lastSwiftTransactionTimestamp;
    private List<CountryDetail> countryDetails = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public BigDecimal getBalanceEur() {
        return balanceEur;
    }

    public void setBalanceEur(BigDecimal balanceEur) {
        this.balanceEur = balanceEur;
    }

    public UUID getLastSepaTransactionId() {
        return lastSepaTransactionId;
    }

    public void setLastSepaTransactionId(UUID lastSepaTransactionId) {
        this.lastSepaTransactionId = lastSepaTransactionId;
    }

    public OffsetDateTime getLastSepaTransactionTimestamp() {
        return lastSepaTransactionTimestamp;
    }

    public void setLastSepaTransactionTimestamp(OffsetDateTime lastSepaTransactionTimestamp) {
        this.lastSepaTransactionTimestamp = lastSepaTransactionTimestamp;
    }

    public UUID getLastSwiftTransactionId() {
        return lastSwiftTransactionId;
    }

    public void setLastSwiftTransactionId(UUID lastSwiftTransactionId) {
        this.lastSwiftTransactionId = lastSwiftTransactionId;
    }

    public OffsetDateTime getLastSwiftTransactionTimestamp() {
        return lastSwiftTransactionTimestamp;
    }

    public void setLastSwiftTransactionTimestamp(OffsetDateTime lastSwiftTransactionTimestamp) {
        this.lastSwiftTransactionTimestamp = lastSwiftTransactionTimestamp;
    }

    public List<CountryDetail> getCountryDetails() {
        return countryDetails;
    }

    public void setCountryDetails(List<CountryDetail> countryDetails) {
        this.countryDetails = countryDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyInfo that = (CompanyInfo) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getCompanyId(), that.getCompanyId()) &&
                Objects.equals(getBalanceEur(), that.getBalanceEur()) &&
                Objects.equals(getLastSepaTransactionId(), that.getLastSepaTransactionId()) &&
                Objects.equals(getLastSepaTransactionTimestamp(), that.getLastSepaTransactionTimestamp()) &&
                Objects.equals(getLastSwiftTransactionId(), that.getLastSwiftTransactionId()) &&
                Objects.equals(getLastSwiftTransactionTimestamp(), that.getLastSwiftTransactionTimestamp()) &&
                Objects.equals(getCountryDetails(), that.getCountryDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCompanyId(), getBalanceEur(), getLastSepaTransactionId(), getLastSepaTransactionTimestamp(),
                getLastSwiftTransactionId(), getLastSwiftTransactionTimestamp(), getCountryDetails());
    }

    @Override
    public String toString() {
        return "CompanyInfo{" +
                "id='" + id + '\'' +
                ", companyId=" + companyId +
                ", balanceEur=" + balanceEur +
                ", lastSepaTransactionId=" + lastSepaTransactionId +
                ", lastSepaTransactionTimestamp=" + lastSepaTransactionTimestamp +
                ", lastSwiftTransactionId=" + lastSwiftTransactionId +
                ", lastSwiftTransactionTimestamp=" + lastSwiftTransactionTimestamp +
                ", countryDetails=" + countryDetails +
                '}';
    }
}
