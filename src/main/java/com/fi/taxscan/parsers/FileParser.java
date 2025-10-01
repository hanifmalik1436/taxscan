package com.fi.taxscan.parsers;

import com.fi.taxscan.entity.PropertyTaxRecord;

import java.io.BufferedReader;
import java.util.List;

public interface FileParser {
    List<PropertyTaxRecord> parse(BufferedReader reader, String county) throws Exception;
}