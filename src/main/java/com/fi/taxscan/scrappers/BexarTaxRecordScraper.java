package com.fi.taxscan.scrappers;

import com.fi.taxscan.entity.PropertyTaxRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BexarTaxRecordScraper implements TaxRecordScraper {
    private static final String BASE_URL = "https://bexar.acttax.com/act_webdev/bexar/showdetail2.jsp?can=";

    @Override
    public PropertyTaxRecord scrape(String accountNumber) throws Exception {
        Document doc = Jsoup.connect(BASE_URL + accountNumber)
                .timeout(10000)
                .get();
        Thread.sleep(1000);

        Elements tableDivs = doc.select("td.responsive-table div");

        Map<String, String> labelValueMap = tableDivs.stream()
                .map(div -> div.text().trim())
                .filter(text -> !text.isEmpty() && text.contains(":"))
                .map(text -> text.split(":", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> parts[0].trim(),
                        parts -> parts[1].trim(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        PropertyTaxRecord record = new PropertyTaxRecord();
        record.setAccountNumber(accountNumber);
        record.setAddress(getStringValue(labelValueMap, "Address", ""));
        record.setPropertySiteAddress(getStringValue(labelValueMap, "Property Site Address", ""));
        record.setLegalDescription(getStringValue(labelValueMap, "Legal Description", ""));
        record.setTaxLevy2024(getBigDecimalValue(labelValueMap, "2024 Year Tax Levy", "0.00"));
        record.setAmountDue2024(getBigDecimalValue(labelValueMap, "2024 Year Amount Due", "0.00"));
        record.setHalfPaymentOption(getBigDecimalValue(labelValueMap, "Half Payment Option Amount (1/2 of Current Tax Levy)", "0.00"));
        record.setPriorYearsDue(getBigDecimalValue(labelValueMap, "Prior Year(s) Amount Due", "0.00"));
        record.setTotalAmountDue(getBigDecimalValue(labelValueMap, "Total Amount Due", "0.00"));
        record.setLastPaymentAmount(getBigDecimalValue(labelValueMap, "Last Payment Amount Received", null));
        record.setLastPayer(getStringValue(labelValueMap, "Last Payer", null));
        record.setLastPaymentDate(getLocalDateValue(labelValueMap, "Last Payment Date", null));
        record.setActiveLawsuits(getStringValue(labelValueMap, "Active Lawsuits", null));
        record.setActiveJudgments(getStringValue(labelValueMap, "Active Judgments", null));
        record.setPendingPayments(getStringValue(labelValueMap, "Pending Credit Card or eCheck Payments", null));
        record.setTotalMarketValue(getBigDecimalValue(labelValueMap, "Total Market Value", "0.00"));
        record.setLandValue(getBigDecimalValue(labelValueMap, "Land Value", "0.00"));
        record.setImprovementValue(getBigDecimalValue(labelValueMap, "Improvement Value", "0.00"));
        record.setCappedValue(getBigDecimalValue(labelValueMap, "Capped Value", "0.00"));
        record.setAgriculturalValue(getBigDecimalValue(labelValueMap, "Agricultural Value", "0.00"));
        record.setExemptions(getStringValue(labelValueMap, "Exemptions (current year only)", null));
        record.setJurisdictions(getStringValue(labelValueMap, "Jurisdictions (current year only)", null));
        record.setDelinquentAfter(getLocalDateValue(labelValueMap, "Delinquent After", null));
        return record;
    }

    private String getStringValue(Map<String, String> map, String key, String defaultValue) {
        String value = map.get(key);
        return value != null ? value : defaultValue;
    }

    private BigDecimal getBigDecimalValue(Map<String, String> map, String key, String defaultValue) {
        String value = map.get(key);
        if (value == null || value.isEmpty()) {
            return defaultValue != null ? new BigDecimal(defaultValue) : null;
        }
        value = value.replaceAll("[^0-9.]", "");
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return defaultValue != null ? new BigDecimal(defaultValue) : null;
        }
    }

    private LocalDate getLocalDateValue(Map<String, String> map, String key, String defaultValue) {
        String value = map.get(key);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            return LocalDate.parse(value, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}