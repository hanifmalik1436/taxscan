package com.fi.taxscan.parsers;

import com.fi.taxscan.entity.PropertyTaxRecord;
import com.fi.taxscan.scrappers.TaxRecordScraper;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class TextFileParser implements FileParser {
    @Override
    public List<PropertyTaxRecord> parse(BufferedReader reader, String county) throws Exception {
        throw new IllegalStateException("TextFileParser requires a TaxRecordScraper; use constructor with scraper");
    }

    public List<PropertyTaxRecord> parse(BufferedReader reader, String county, TaxRecordScraper scraper) throws Exception {
        List<PropertyTaxRecord> records = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String accountNumber = line.trim();
            if (!accountNumber.isEmpty()) {
                PropertyTaxRecord record = scraper.scrape(accountNumber);
                if (record != null) {
                    record.setCounty(county.toUpperCase());
                    records.add(record);
                }
            }
        }
        return records;
    }
}