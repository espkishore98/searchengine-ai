package com.searchengine.searchengine_ai;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Component
class IngestionPipeline {
    @Value("classpath:DummyFile.txt")
    private Resource resourceFile;
    @Value("${document.file.path:src/main/resources/DummyFile.txt}")
    private String filePath; // Define a dynamic file path
    private static final Logger logger = LoggerFactory.getLogger(IngestionPipeline.class);

    private final VectorStore vectorStore;

    @Autowired
    WebScraperService webScraperService;

    IngestionPipeline(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    void run(String query) {
//        List<String> value = webScraperService.scrapeGoogleResults(query);
//        List<Document> documents = new ArrayList<>(value.stream().map(Document::new).toList());
//
//        var textReader1 = new TextReader(resourceFile);
//        textReader1.setCharset(Charset.defaultCharset());
//        List<Document> fileDocuments = new ArrayList<>(textReader1.get());
//        documents.addAll(fileDocuments);
//        logger.info("Creating and storing Embeddings from Documents");
//        vectorStore.add(new TokenTextSplitter().split(documents));

        List<String> value = webScraperService.scrapeGoogleResults(query);

        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : value) {
                writer.write(line);
                writer.newLine();
            }
            logger.info("Scraped data written to file: " + file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error writing to file", e);
            return; // Exit if file writing fails
        }

        var textReader = new TextReader(String.valueOf(file.toPath().toUri()));
        textReader.setCharset(Charset.defaultCharset());
        List<Document> documents = new ArrayList<>(textReader.get());

        // Step 4: Store embeddings in vectorStore
        logger.info("Creating and storing Embeddings from Documents");
        vectorStore.add(new TokenTextSplitter().split(documents));
    }

}