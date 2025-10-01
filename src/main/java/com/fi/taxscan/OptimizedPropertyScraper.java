package com.fi.taxscan;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class OptimizedPropertyScraper {
    private static final String BASE_URL = "https://bexar.acttax.com/act_webdev/bexar/showdetail2.jsp?can=";
    private static final int MAX_THREADS = 10; // Tune based on server limits/hardware (e.g., 10-50)
    private static final int BATCH_SIZE = 1000; // Process in batches to avoid memory issues
    private static final long DELAY_MS = 200; // Delay per request to avoid rate limits
    private static final int RETRIES = 3; // Retries for failed requests

    public static void main(String[] args) throws IOException, InterruptedException {
        // Load list of CANs (replace with your source: e.g., read from DB or file)
        List<String> cans = loadCansFromFile("bexar.txt"); // Assume file with one CAN per line

        // Output CSV file
        String outputFile = "property_data.csv";
        Set<String> allKeys = new LinkedHashSet<>(); // Track all unique keys for CSV header

        // Executor for parallel requests
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

        // Process in batches
        AtomicInteger processed = new AtomicInteger(0);
        for (int i = 0; i < cans.size(); i += BATCH_SIZE) {
            List<String> batch = cans.subList(i, Math.min(i + BATCH_SIZE, cans.size()));
            List<CompletableFuture<Map<String, String>>> futures = new ArrayList<>();

            for (String can : batch) {
                CompletableFuture<Map<String, String>> future = CompletableFuture.supplyAsync(() -> scrapeAndParse(can), executor)
                        .exceptionally(ex -> {
                            System.err.println("Error processing CAN " + can + ": " + ex.getMessage());
                            return Collections.emptyMap(); // Skip on error
                        });
                futures.add(future);
            }

            // Wait for batch to complete
            List<Map<String, String>> batchResults = futures.stream().map(CompletableFuture::join).toList();

            // Update allKeys with unique keys from this batch
            batchResults.forEach(map -> allKeys.addAll(map.keySet()));

            // Write batch to CSV (append mode)
            writeBatchToCsv(outputFile, batchResults, allKeys, i == 0); // Write header only for first batch

            processed.addAndGet(batch.size());
            System.out.println("Processed " + processed.get() + "/" + cans.size() + " records");

            // Optional: Batch delay to avoid bans
            Thread.sleep(1000);
        }

        executor.shutdown();
        System.out.println("Scraping complete. Data saved to " + outputFile);
    }

    private static Map<String, String> scrapeAndParse(String can) {
        for (int attempt = 1; attempt <= RETRIES; attempt++) {
            try {
                // Add delay to respect rate limits
                Thread.sleep(DELAY_MS);

                Document doc = Jsoup.connect(BASE_URL + can).get();
                Elements tableDivs = doc.select("td.responsive-table div");

                // Use Stream API for parsing (efficient for small per-page data)
                return tableDivs.stream()
                        .map(div -> div.text().trim())
                        .filter(text -> !text.isEmpty() && text.contains(":"))
                        .map(text -> text.split(":", 2))
                        .filter(parts -> parts.length == 2)
                        .collect(Collectors.toMap(
                                parts -> parts[0].trim(),
                                parts -> parts[1].trim(),
                                (v1, v2) -> v2, // Keep last if duplicate
                                LinkedHashMap::new // Preserve order per page
                        ));
            } catch (IOException | InterruptedException e) {
                if (attempt == RETRIES) {
                    System.err.println("Failed to scrape CAN " + can + " after " + RETRIES + " attempts: " + e.getMessage());
                }
            }
        }
        return Collections.emptyMap();
    }

    private static void writeBatchToCsv(String file, List<Map<String, String>> batchResults, Set<String> allKeys, boolean writeHeader) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) { // Append mode
            if (writeHeader) {
                writer.write("CAN," + String.join(",", allKeys));
                writer.newLine();
            }

            for (Map<String, String> map : batchResults) {
                String can = map.getOrDefault("Property ID", "UNKNOWN"); // Assume 'Property ID' is CAN, or adjust
                List<String> row = new ArrayList<>();
                row.add(can);
                for (String key : allKeys) {
                    row.add(map.getOrDefault(key, "")); // Empty if missing
                }
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }
    }

    private static List<String> loadCansFromFile(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }
}
