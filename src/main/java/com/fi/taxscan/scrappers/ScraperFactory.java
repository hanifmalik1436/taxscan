package com.fi.taxscan.scrappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ScraperFactory {
    private final Map<String, TaxRecordScraper> scrapers;

    @Autowired
    public ScraperFactory(BexarTaxRecordScraper bexarScraper, DallasTaxRecordScraper dallasScraper) {
        scrapers = new HashMap<>();
        scrapers.put("BEXAR", bexarScraper);
        scrapers.put("DALLAS", dallasScraper);
        scrapers.put("EL PASO", dallasScraper);
    }

    public TaxRecordScraper getScraper(String county) {
        TaxRecordScraper scraper = scrapers.get(county.toUpperCase());
        if (scraper == null) {
            throw new IllegalArgumentException("Unsupported county: " + county);
        }
        return scraper;
    }
}