package com.fi.taxscan.service;

import com.fi.taxscan.entity.PropertyTaxRecord;
import com.fi.taxscan.repository.PropertyTaxRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyTaxRecordService {

    @Autowired
    private PropertyTaxRecordRepository repository;

    public PropertyTaxRecord save(PropertyTaxRecord record) {
        if (record.getAccountNumber() == null || record.getAccountNumber().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty");
        }
        return repository.save(record);
    }

    public List<PropertyTaxRecord> findAll() {
        return repository.findAll();
    }

    public List<PropertyTaxRecord> findByCounty(String county) {
        return repository.findByCounty(county);
    }

    public Optional<PropertyTaxRecord> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<PropertyTaxRecord> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber);
    }
}