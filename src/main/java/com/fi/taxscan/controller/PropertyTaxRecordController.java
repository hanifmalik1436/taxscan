package com.fi.taxscan.controller;

import com.fi.taxscan.entity.PropertyTaxRecord;
import com.fi.taxscan.mappers.TaxRecordMapper;
import com.fi.taxscan.parsers.CsvFileParser;
import com.fi.taxscan.parsers.FileParser;
import com.fi.taxscan.parsers.TextFileParser;
import com.fi.taxscan.scrappers.ScraperFactory;
import com.fi.taxscan.scrappers.TaxRecordScraper;
import com.fi.taxscan.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tax-records")
public class PropertyTaxRecordController {

    private final PropertyTaxRecordService service;
    private final ScraperFactory scraperFactory;
    private final TaxRecordMapper mapper;
    private final FileParser csvFileParser;

    @Autowired
    public PropertyTaxRecordController(
            PropertyTaxRecordService service,
            ScraperFactory scraperFactory,
            TaxRecordMapper mapper) {
        this.service = service;
        this.scraperFactory = scraperFactory;
        this.mapper = mapper;
        this.csvFileParser = new CsvFileParser();
    }

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
            List<PropertyTaxRecord> records;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                if (fileName.endsWith(".txt")) {
                    TaxRecordScraper scraper = scraperFactory.getScraper(county);
                    records = new TextFileParser().parse(reader, county, scraper);
                } else if (fileName.endsWith(".csv")) {
                    records = csvFileParser.parse(reader, county);
                    TaxRecordScraper scraper = scraperFactory.getScraper(county);
                    List<PropertyTaxRecord> enhancedRecords = new ArrayList<>();
                    for (PropertyTaxRecord record : records) {
                        if (record.getAccountNumber() != null && !record.getAccountNumber().trim().isEmpty()) {
                            try {
                                PropertyTaxRecord fetched = scraper.scrape(record.getAccountNumber());
                                mapper.mergeRecords(record, fetched);
                                enhancedRecords.add(record);
                            } catch (Exception e) {
                                String errorMsg = "Failed to fetch web data for account number " + record.getAccountNumber() + ": " + e.getMessage();
                                System.err.println(errorMsg);
                                e.printStackTrace();
                                errors.add(errorMsg);
                                enhancedRecords.add(record);
                            }
                        } else {
                            errors.add("Missing account number in CSV row");
                            enhancedRecords.add(record);
                        }
                    }
                    records = enhancedRecords;
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
                    String errorMsg = "Unexpected error for account number " + record.getAccountNumber() + ": " + e.getMessage();
                    System.err.println(errorMsg);
                    e.printStackTrace();
                    errors.add(errorMsg);
                }
            }

            response.put("successCount", successCount);
            response.put("errors", errors);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            String errorMsg = "Failed to process file: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", errorMsg));
        } catch (Exception e) {
            String errorMsg = "Failed to process file: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", errorMsg));
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
}