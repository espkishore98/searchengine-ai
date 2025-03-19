package com.searchengine.searchengine_ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    @Autowired
    private LlmService llmService;
    @Autowired
    private WebScraperService webScraperService;
    @GetMapping
    public String search(@RequestParam String query) {
        // 1️⃣ Fetch live web content
        ingestionPipeline.run(query);

        // 2️⃣ Pass it to Llama for processing
        return llmService.generateResponse(query);
    }

    @Autowired
    private IngestionPipeline ingestionPipeline;
}
