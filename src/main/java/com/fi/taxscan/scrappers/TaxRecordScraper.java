package com.fi.taxscan.scrappers;

import com.fi.taxscan.entity.PropertyTaxRecord;

public interface TaxRecordScraper {
    PropertyTaxRecord scrape(String accountNumber) throws Exception;
}