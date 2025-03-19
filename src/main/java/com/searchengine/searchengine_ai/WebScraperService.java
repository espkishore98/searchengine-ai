package com.searchengine.searchengine_ai;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebScraperService {
    @Autowired
    GoogleSeleniumScraper googleSeleniumScraper;
    private final ChatClient ollamaClient;
    @Autowired
    public WebScraperService(ChatClient.Builder ollamaClient) {
        this.ollamaClient = ollamaClient.build();
    }

    public List<String> scrapeGoogleResults(String query) {

        List<ScrapeLink> links = googleSeleniumScraper.getLinks(query);
        List<String> values= new ArrayList<>();
        for(int i=0; i<1; i++){
            if (links.get(i) != null) {
                values.add(scrapeContent(links.get(i).getLink(), query));
            }
        }
        return values;
    }

    public String scrapeContent(String url, String query) {
        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();
            //Url is"+ url + "and its content is" +doc.text()
           ChatResponse chatResponse = ollamaClient.prompt("summarize the content in 100 words :"+ doc.text()).advisors().call().chatResponse();
            return query +" and its url is : "+ url + " and  "+ "content in the url is " + chatResponse.getResult().getOutput().getText();
        } catch (IOException e) {
            return "Failed to fetch content from " + url;
        }
    }
}
