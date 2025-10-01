package com.fi.taxscan.parsers;

import com.fi.taxscan.entity.PropertyTaxRecord;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import java.io.BufferedReader;
import java.util.List;

public class CsvFileParser implements FileParser {
    @Override
    public List<PropertyTaxRecord> parse(BufferedReader reader, String county) throws Exception {
        HeaderColumnNameMappingStrategy<PropertyTaxRecord> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(PropertyTaxRecord.class);

        CsvToBean<PropertyTaxRecord> csvToBean = new CsvToBeanBuilder<PropertyTaxRecord>(reader)
                .withType(PropertyTaxRecord.class)
                .withMappingStrategy(strategy)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        List<PropertyTaxRecord> records = csvToBean.parse();
        records.forEach(record -> record.setCounty(county.toUpperCase()));
        return records;
    }
}