package com.fi.taxscan.mappers;

import com.fi.taxscan.entity.PropertyTaxRecord;

public interface TaxRecordMapper {
    void mergeRecords(PropertyTaxRecord original, PropertyTaxRecord fetched);
}