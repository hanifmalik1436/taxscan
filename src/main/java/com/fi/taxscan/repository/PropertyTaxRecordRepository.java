package com.fi.taxscan.repository;

import com.fi.taxscan.entity.PropertyTaxRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PropertyTaxRecordRepository extends JpaRepository<PropertyTaxRecord, Long> {
    List<PropertyTaxRecord> findByCounty(String county);
    Optional<PropertyTaxRecord> findByAccountNumber(String accountNumber);
}