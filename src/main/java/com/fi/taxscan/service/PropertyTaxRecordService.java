package com.fi.taxscan.service;

import com.fi.taxscan.entity.PropertyTaxRecord;
import com.fi.taxscan.repository.PropertyTaxRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyTaxRecordService {
    private final PropertyTaxRecordRepository repository;

    @Autowired
    public PropertyTaxRecordService(PropertyTaxRecordRepository repository) {
        this.repository = repository;
    }

    public PropertyTaxRecord save(PropertyTaxRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Record cannot be null");
        }
        String accountNumber = record.getAccountNumber();
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }
        // Trim and validate account number
        accountNumber = accountNumber.trim();
        if (accountNumber.length() > 20) {
            throw new IllegalArgumentException("Account number exceeds maximum length of 20 characters: " + accountNumber);
        }
        try {
            Optional<PropertyTaxRecord> existing = repository.findByAccountNumber(accountNumber);
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Record with account number " + accountNumber + " already exists");
            }
            record.setAccountNumber(accountNumber); // Ensure trimmed value is set
            return repository.save(record);
        } catch (Exception e) {
            System.err.println("Error saving record for account number: " + accountNumber);
            e.printStackTrace();
            throw new RuntimeException("Failed to save record for account number " + accountNumber + ": " + e.getMessage(), e);
        }
    }

    public List<PropertyTaxRecord> findAll() {
        return repository.findAll();
    }

    public List<PropertyTaxRecord> findByCounty(String county) {
        return repository.findByCountyIgnoreCase(county);
    }

    public Optional<PropertyTaxRecord> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<PropertyTaxRecord> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber);
    }
}