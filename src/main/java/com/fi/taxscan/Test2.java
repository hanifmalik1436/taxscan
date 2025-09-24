package com.fi.taxscan;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Test2 {
    public static void main(String[] args) {
        try {

            List<String> cans = loadCansFromFile("cans.txt");
            cans.stream().forEach(can -> {
                Document doc = null;
                try {
                    doc = Jsoup.connect("https://bexar.acttax.com/act_webdev/bexar/showdetail2.jsp?can="+can).get();
                    Thread.sleep(1000);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Elements tableDivs = doc.select("td.responsive-table div");

                // Use Stream API to collect into LinkedHashMap
                Map<String, String> labelValueMap = tableDivs.stream()
                        .map(div -> div.text().trim()) // Get and trim text
                        .filter(text -> !text.isEmpty() && text.contains(":")) // Skip empty or no colon
                        .map(text -> text.split(":", 2)) // Split on first colon
                        .filter(parts -> parts.length == 2) // Ensure valid split
                        .collect(LinkedHashMap::new, // Use LinkedHashMap for order
                                (map, parts) -> map.put(parts[0].trim(), parts[1].trim()), // Put trimmed key-value
                                Map::putAll); // Combiner (not used in single-threaded stream)

                // Print the ordered map
                System.out.println("Collected Label-Value Pairs:");
                //labelValueMap.forEach((key, value) -> System.out.println(key + ": " + value));
                labelValueMap.forEach((key, value) -> {
                    System.out.println(key + ": " + value);

                });


            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> loadCansFromFile(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }
}
