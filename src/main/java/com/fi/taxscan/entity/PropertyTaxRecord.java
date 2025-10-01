package com.fi.taxscan.entity;

import jakarta.persistence.*;
import com.opencsv.bean.CsvBindByName;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "property_tax_records")
public class PropertyTaxRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", length = 20, nullable = false, unique = true)
    @CsvBindByName(column = "accountNumber")
    private String accountNumber;

    @Column(length = 50)
    @CsvBindByName(column = "county")
    private String county;

    @Column(columnDefinition = "TEXT")
    @CsvBindByName(column = "address")
    private String address;

    @Column(columnDefinition = "TEXT")
    @CsvBindByName(column = "propertySiteAddress")
    private String propertySiteAddress;

    @Column(columnDefinition = "TEXT")
    @CsvBindByName(column = "legalDescription")
    private String legalDescription;

    @Column(name = "tax_levy_2024")
    @CsvBindByName(column = "taxLevy2024")
    private BigDecimal taxLevy2024;

    @Column(name = "amount_due_2024")
    @CsvBindByName(column = "amountDue2024")
    private BigDecimal amountDue2024;

    @Column
    @CsvBindByName(column = "halfPaymentOption")
    private BigDecimal halfPaymentOption;

    @Column
    @CsvBindByName(column = "priorYearsDue")
    private BigDecimal priorYearsDue;

    @Column
    @CsvBindByName(column = "totalAmountDue")
    private BigDecimal totalAmountDue;

    @Column
    @CsvBindByName(column = "lastPaymentAmount")
    private BigDecimal lastPaymentAmount;

    @Column(length = 255)
    @CsvBindByName(column = "lastPayer")
    private String lastPayer;

    @Column
    @CsvBindByName(column = "lastPaymentDate")
    private LocalDate lastPaymentDate;

    @Column(columnDefinition = "TEXT")
    @CsvBindByName(column = "activeLawsuits")
    private String activeLawsuits;

    @Column(columnDefinition = "TEXT")
    @CsvBindByName(column = "activeJudgments")
    private String activeJudgments;

    @Column(columnDefinition = "TEXT")
    @CsvBindByName(column = "pendingPayments")
    private String pendingPayments;

    @Column
    @CsvBindByName(column = "totalMarketValue")
    private BigDecimal totalMarketValue;

    @Column
    @CsvBindByName(column = "landValue")
    private BigDecimal landValue;

    @Column
    @CsvBindByName(column = "improvementValue")
    private BigDecimal improvementValue;

    @Column
    @CsvBindByName(column = "cappedValue")
    private BigDecimal cappedValue;

    @Column
    @CsvBindByName(column = "agriculturalValue")
    private BigDecimal agriculturalValue;

    @Column(columnDefinition = "TEXT")
    @CsvBindByName(column = "exemptions")
    private String exemptions;

    @Column(columnDefinition = "TEXT")
    @CsvBindByName(column = "jurisdictions")
    private String jurisdictions;

    @Column
    @CsvBindByName(column = "delinquentAfter")
    private LocalDate delinquentAfter;

    // Getters and setters (unchanged)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPropertySiteAddress() { return propertySiteAddress; }
    public void setPropertySiteAddress(String propertySiteAddress) { this.propertySiteAddress = propertySiteAddress; }
    public String getLegalDescription() { return legalDescription; }
    public void setLegalDescription(String legalDescription) { this.legalDescription = legalDescription; }
    public BigDecimal getTaxLevy2024() { return taxLevy2024; }
    public void setTaxLevy2024(BigDecimal taxLevy2024) { this.taxLevy2024 = taxLevy2024; }
    public BigDecimal getAmountDue2024() { return amountDue2024; }
    public void setAmountDue2024(BigDecimal amountDue2024) { this.amountDue2024 = amountDue2024; }
    public BigDecimal getHalfPaymentOption() { return halfPaymentOption; }
    public void setHalfPaymentOption(BigDecimal halfPaymentOption) { this.halfPaymentOption = halfPaymentOption; }
    public BigDecimal getPriorYearsDue() { return priorYearsDue; }
    public void setPriorYearsDue(BigDecimal priorYearsDue) { this.priorYearsDue = priorYearsDue; }
    public BigDecimal getTotalAmountDue() { return totalAmountDue; }
    public void setTotalAmountDue(BigDecimal totalAmountDue) { this.totalAmountDue = totalAmountDue; }
    public BigDecimal getLastPaymentAmount() { return lastPaymentAmount; }
    public void setLastPaymentAmount(BigDecimal lastPaymentAmount) { this.lastPaymentAmount = lastPaymentAmount; }
    public String getLastPayer() { return lastPayer; }
    public void setLastPayer(String lastPayer) { this.lastPayer = lastPayer; }
    public LocalDate getLastPaymentDate() { return lastPaymentDate; }
    public void setLastPaymentDate(LocalDate lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }
    public String getActiveLawsuits() { return activeLawsuits; }
    public void setActiveLawsuits(String activeLawsuits) { this.activeLawsuits = activeLawsuits; }
    public String getActiveJudgments() { return activeJudgments; }
    public void setActiveJudgments(String activeJudgments) { this.activeJudgments = activeJudgments; }
    public String getPendingPayments() { return pendingPayments; }
    public void setPendingPayments(String pendingPayments) { this.pendingPayments = pendingPayments; }
    public BigDecimal getTotalMarketValue() { return totalMarketValue; }
    public void setTotalMarketValue(BigDecimal totalMarketValue) { this.totalMarketValue = totalMarketValue; }
    public BigDecimal getLandValue() { return landValue; }
    public void setLandValue(BigDecimal landValue) { this.landValue = landValue; }
    public BigDecimal getImprovementValue() { return improvementValue; }
    public void setImprovementValue(BigDecimal improvementValue) { this.improvementValue = improvementValue; }
    public BigDecimal getCappedValue() { return cappedValue; }
    public void setCappedValue(BigDecimal cappedValue) { this.cappedValue = cappedValue; }
    public BigDecimal getAgriculturalValue() { return agriculturalValue; }
    public void setAgriculturalValue(BigDecimal agriculturalValue) { this.agriculturalValue = agriculturalValue; }
    public String getExemptions() { return exemptions; }
    public void setExemptions(String exemptions) { this.exemptions = exemptions; }
    public String getJurisdictions() { return jurisdictions; }
    public void setJurisdictions(String jurisdictions) { this.jurisdictions = jurisdictions; }
    public LocalDate getDelinquentAfter() { return delinquentAfter; }
    public void setDelinquentAfter(LocalDate delinquentAfter) { this.delinquentAfter = delinquentAfter; }

    @Override
    public String toString() {
        return "PropertyTaxRecord{accountNumber='" + accountNumber + "', county='" + county + "'}";
    }
}