package com.fi.taxscan.controller;

import com.fi.taxscan.entity.PropertyTaxRecord;
import com.fi.taxscan.service.PropertyTaxRecordService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import jakarta.validation.Valid;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/tax-records")
public class PropertyTaxRecordController {

    @Autowired
    private PropertyTaxRecordService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyTaxRecord> create(@Valid @RequestBody PropertyTaxRecord record) {
        try {
            PropertyTaxRecord saved = service.save(record);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadRecords(
            @RequestParam("file") MultipartFile file,
            @RequestParam("county") String county) {
        Map<String, Object> response = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty or missing"));
            }

            String fileName = file.getOriginalFilename().toLowerCase();
            List<PropertyTaxRecord> records = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                if (fileName.endsWith(".txt")) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String accountNumber = line.trim();
                        if (!accountNumber.isEmpty()) {
                            PropertyTaxRecord record = fetchRecordByAccountNumber(accountNumber);
                            if (record != null) {
                                record.setCounty(county);
                                records.add(record);
                            } else {
                                errors.add("No data found for account number: " + accountNumber);
                            }
                        }
                    }
                } else if (fileName.endsWith(".csv")) {
                    HeaderColumnNameMappingStrategy<PropertyTaxRecord> strategy = new HeaderColumnNameMappingStrategy<>();
                    strategy.setType(PropertyTaxRecord.class);

                    CsvToBean<PropertyTaxRecord> csvToBean = new CsvToBeanBuilder<PropertyTaxRecord>(reader)
                            .withType(PropertyTaxRecord.class)
                            .withMappingStrategy(strategy)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build();
                    records = csvToBean.parse();
                    records.forEach(record -> record.setCounty(county));
                } else {
                    return ResponseEntity.badRequest().body(Map.of("error", "Unsupported file type: " + fileName));
                }
            }

            for (PropertyTaxRecord record : records) {
                try {
                    service.save(record);
                    successCount++;
                } catch (IllegalArgumentException e) {
                    errors.add("Failed to save record with account number " + record.getAccountNumber() + ": " + e.getMessage());
                } catch (Exception e) {
                    errors.add("Unexpected error for account number " + record.getAccountNumber() + ": " + e.getMessage());
                }
            }

            response.put("successCount", successCount);
            response.put("errors", errors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to process file: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PropertyTaxRecord>> getAll(@RequestParam(required = false) String county) {
        List<PropertyTaxRecord> records = county != null ? service.findByCounty(county) : service.findAll();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyTaxRecord> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/account/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyTaxRecord> getByAccountNumber(@PathVariable String accountNumber) {
        return service.findByAccountNumber(accountNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private PropertyTaxRecord fetchRecordByAccountNumber(String accountNumber) {
        try {
            Document doc = Jsoup.connect("https://bexar.acttax.com/act_webdev/bexar/showdetail2.jsp?can=" + accountNumber)
                    .timeout(10000)
                    .get();
            Thread.sleep(1000);

            Elements tableDivs = doc.select("td.responsive-table div");

            Map<String, String> labelValueMap = tableDivs.stream()
                    .map(div -> div.text().trim())
                    .filter(text -> !text.isEmpty() && text.contains(":"))
                    .map(text -> text.split(":", 2))
                    .filter(parts -> parts.length == 2)
                    .collect(LinkedHashMap::new,
                            (map, parts) -> map.put(parts[0].trim(), parts[1].trim()),
                            Map::putAll);

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
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data for account number: " + accountNumber, e);
        }
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