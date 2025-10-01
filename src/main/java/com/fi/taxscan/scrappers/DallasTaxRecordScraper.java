package com.fi.taxscan.scrappers;

import com.fi.taxscan.entity.PropertyTaxRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class DallasTaxRecordScraper implements TaxRecordScraper {
    private static final String BASE_URL = "https://www.dallasact.com/act_webdev/dallas/showdetail2.jsp?can=";

    @Override
    public PropertyTaxRecord scrape(String accountNumber) throws Exception {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }
        String cleanedAccountNumber = accountNumber.trim();
        Document doc;
        try {
            doc = Jsoup.connect(BASE_URL + cleanedAccountNumber + "&ownerno=0")
                    .timeout(15000)
                    .get();
            Thread.sleep(1000); // Respectful delay
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to Dallas website for account number " + cleanedAccountNumber + ": " + e.getMessage(), e);
        }

        PropertyTaxRecord record = new PropertyTaxRecord();
        record.setAccountNumber(cleanedAccountNumber);

        // Parse Account Number
        Elements leftTd = doc.select("td[width=53%] h3");
        if (!leftTd.isEmpty()) {
            Element h3Element = leftTd.first();
            Element accountBold = h3Element.selectFirst("b:contains(Account Number)");
            String accountText = "";
            if (accountBold != null) {
                accountText = accountBold.text().replaceAll("Account Number\\s*:\\s*", "").trim();
            }
            System.out.println("Scraped account number: '" + accountText + "' for input: '" + cleanedAccountNumber + "'");
            if (!accountText.equals(cleanedAccountNumber)) {
                throw new IllegalStateException("Scraped account number '" + accountText + "' does not match input '" + cleanedAccountNumber + "'");
            }
        } else {
            System.err.println("Account Number element not found in page for " + cleanedAccountNumber);
            throw new IllegalStateException("Account Number element not found in page for " + cleanedAccountNumber);
        }

        // Parse Address (multi-line)
        Elements addressLabel = doc.select("td[width=53%] h3 b:contains(Address)");
        if (!addressLabel.isEmpty()) {
            Element bElement = addressLabel.first();
            StringBuilder addressBuilder = new StringBuilder();
            Node current = bElement.nextSibling();
            while (current != null) {
                if (current instanceof TextNode) {
                    String text = ((TextNode) current).text().trim();
                    if (!text.isEmpty()) {
                        addressBuilder.append(text).append(" ");
                    }
                } else if (current instanceof Element && !((Element) current).tagName().equals("b")) {
                    String text = ((Element) current).text().trim();
                    if (!text.isEmpty()) {
                        addressBuilder.append(text).append(" ");
                    }
                } else if (current instanceof Element && ((Element) current).tagName().equals("b")) {
                    break; // Stop at next bold tag
                }
                current = current.nextSibling();
            }
            record.setAddress(addressBuilder.toString().trim());
        }

        // Parse Property Site Address (multi-line)
        Elements propSiteLabel = doc.select("td[width=53%] h3 b:contains(Property Site Address)");
        if (!propSiteLabel.isEmpty()) {
            Element bElement = propSiteLabel.first();
            StringBuilder propSiteBuilder = new StringBuilder();
            Node current = bElement.nextSibling();
            while (current != null) {
                if (current instanceof TextNode) {
                    String text = ((TextNode) current).text().trim();
                    if (!text.isEmpty()) {
                        propSiteBuilder.append(text).append(" ");
                    }
                } else if (current instanceof Element && !((Element) current).tagName().equals("b")) {
                    String text = ((Element) current).text().trim();
                    if (!text.isEmpty()) {
                        propSiteBuilder.append(text).append(" ");
                    }
                } else if (current instanceof Element && ((Element) current).tagName().equals("b")) {
                    break;
                }
                current = current.nextSibling();
            }
            record.setPropertySiteAddress(propSiteBuilder.toString().trim());
        }

        // Parse Legal Description (multi-line)
        Elements legalDescLabel = doc.select("td[width=53%] h3 b:contains(Legal Description)");
        if (!legalDescLabel.isEmpty()) {
            Element bElement = legalDescLabel.first();
            StringBuilder legalDescBuilder = new StringBuilder();
            Node current = bElement.nextSibling();
            while (current != null) {
                if (current instanceof TextNode) {
                    String text = ((TextNode) current).text().trim();
                    if (!text.isEmpty()) {
                        legalDescBuilder.append(text).append(" ");
                    }
                } else if (current instanceof Element && !((Element) current).tagName().equals("b")) {
                    String text = ((Element) current).text().trim();
                    if (!text.isEmpty()) {
                        legalDescBuilder.append(text).append(" ");
                    }
                } else if (current instanceof Element && ((Element) current).tagName().equals("b")) {
                    break;
                }
                current = current.nextSibling();
            }
            record.setLegalDescription(legalDescBuilder.toString().trim());
        }

        // Helper method to parse single-line values
        Map<String, String> fieldMappings = new HashMap<>();
        fieldMappings.put("taxLevy2024", "Current Tax Levy:");
        fieldMappings.put("amountDue2024", "Current Amount Due:");
        fieldMappings.put("priorYearsDue", "Prior Year Amount Due:");
        fieldMappings.put("totalAmountDue", "Total Amount Due:");
        fieldMappings.put("totalMarketValue", "Market Value:");
        fieldMappings.put("landValue", "Land Value:");
        fieldMappings.put("improvementValue", "Improvement Value:");
        fieldMappings.put("cappedValue", "Capped Value:");
        fieldMappings.put("agriculturalValue", "Agricultural Value:");
        fieldMappings.put("exemptions", "Exemptions:");

        for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
            String field = entry.getKey();
            String label = entry.getValue();
            String selector = field.contains("totalMarketValue") || field.contains("landValue") ||
                    field.contains("improvementValue") || field.contains("cappedValue") ||
                    field.contains("agriculturalValue") || field.contains("exemptions")
                    ? "td[width=47%] h3 b:contains(" + label + ")"
                    : "td[width=53%] h3 b:contains(" + label + ")";
            Elements labelElements = doc.select(selector);
            if (!labelElements.isEmpty()) {
                Element bElement = labelElements.first();
                Node next = bElement.nextSibling();
                if (next instanceof TextNode) {
                    String text = ((TextNode) next).text().trim();
                    if (!text.isEmpty()) {
                        try {
                            if (field.equals("exemptions")) {
                                record.setExemptions(text);
                            } else {
                                String cleanText = text.replaceAll("[^0-9.]", "");
                                if (!cleanText.isEmpty()) {
                                    record.getClass()
                                            .getMethod("set" + field.substring(0, 1).toUpperCase() + field.substring(1), BigDecimal.class)
                                            .invoke(record, new BigDecimal(cleanText));
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error setting field " + field + " for account number " + cleanedAccountNumber + ": " + e.getMessage());
                        }
                    }
                }
            }
        }

        // Fields not present in Dallas data
        record.setHalfPaymentOption(BigDecimal.ZERO);
        record.setLastPaymentAmount(null);
        record.setLastPayer(null);
        record.setLastPaymentDate(null);
        record.setActiveLawsuits(null);
        record.setActiveJudgments(null);
        record.setPendingPayments(null);
        record.setJurisdictions(null);
        record.setDelinquentAfter(null);

        return record;
    }
}