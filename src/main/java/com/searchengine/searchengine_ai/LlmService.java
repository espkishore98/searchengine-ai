package com.searchengine.searchengine_ai;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LlmService {

    private final ChatClient ollamaClient;
    private VectorStore vectorStore;

    @Autowired
    public LlmService(ChatClient.Builder ollamaClient,
                      VectorStore vectorStore
                      ) {

        this.ollamaClient = ollamaClient.build();
        this.vectorStore = vectorStore;
    }

    public String generateResponse(String query) {
        SearchRequest searchRequest = SearchRequest.builder().topK(1).build();
        List<Document> retrievedDocs = vectorStore.similaritySearch(searchRequest);

        String bestMatch = retrievedDocs.isEmpty() ? "" : retrievedDocs.get(0).getText();
        System.out.println(" Best Retrieved Match: " + bestMatch);

        String prompt = "You are an AI assistant with access to external knowledge.\n" +
                "Here is the most relevant retrieved information:\n" +
                bestMatch + "\n\n" +
                "Answer the following question based on the retrieved data:\n" +
                query;
        ChatResponse response =  ollamaClient.prompt(prompt)
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().topK(10).build()))
                .call().chatResponse();
        String llmAnswer = response.getResult().getOutput().getText();
        System.out.println("🤖 LLM Response: " + llmAnswer);

        // 🔹 Step 4: Compare Retrieved vs. LLM Answer
        if (!bestMatch.isEmpty()) {
            return "✅ Best Match from Retrieval:\n" + bestMatch;
        } else {
            return "🤖 LLM's Best Answer:\n" + llmAnswer;
        }
    }
}
