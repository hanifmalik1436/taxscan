package com.fi.taxscan.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "property_tax_records")
@Data
public class PropertyTaxRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "county")
    private String county;

    @NotBlank
    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @NotBlank
    @Column(name = "address")
    private String address;

    @NotBlank
    @Column(name = "property_site_address")
    private String propertySiteAddress;

    @NotBlank
    @Column(name = "legal_description")
    private String legalDescription;

    @NotNull
    @Column(name = "tax_levy_2024")
    private BigDecimal taxLevy2024;

    @NotNull
    @Column(name = "amount_due_2024")
    private BigDecimal amountDue2024;

    @NotNull
    @Column(name = "half_payment_option")
    private BigDecimal halfPaymentOption;

    @NotNull
    @Column(name = "prior_years_due")
    private BigDecimal priorYearsDue;

    @NotNull
    @Column(name = "total_amount_due")
    private BigDecimal totalAmountDue;

    @Column(name = "last_payment_amount")
    private BigDecimal lastPaymentAmount;

    @Column(name = "last_payer")
    private String lastPayer;

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;

    @Column(name = "active_lawsuits")
    private String activeLawsuits;

    @Column(name = "active_judgments")
    private String activeJudgments;

    @Column(name = "pending_payments")
    private String pendingPayments;

    @NotNull
    @Column(name = "total_market_value")
    private BigDecimal totalMarketValue;

    @NotNull
    @Column(name = "land_value")
    private BigDecimal landValue;

    @NotNull
    @Column(name = "improvement_value")
    private BigDecimal improvementValue;

    @NotNull
    @Column(name = "capped_value")
    private BigDecimal cappedValue;

    @NotNull
    @Column(name = "agricultural_value")
    private BigDecimal agriculturalValue;

    @Column(name = "exemptions")
    private String exemptions;

    @Column(name = "jurisdictions")
    private String jurisdictions;

    @Column(name = "delinquent_after")
    private LocalDate delinquentAfter;
}
