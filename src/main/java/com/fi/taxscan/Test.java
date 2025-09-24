package com.fi.taxscan;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Test {

    public static void main(String args[]){
        try{
            Document doc = Jsoup.connect("https://bexar.acttax.com/act_webdev/bexar/showdetail2.jsp?can=045391030170").get();

            Elements tableDivs = doc.select("td.responsive-table div");
            for(Element div:tableDivs) {
                System.out.println("Div = "+div.text());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
