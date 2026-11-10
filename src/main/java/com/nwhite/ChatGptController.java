package com.nwhite;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@CrossOrigin
public class ChatGptController {
    private static final Logger log = LoggerFactory.getLogger(ChatGptController.class);
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    // TODO: upgrade to 1.0.0-M1 and use InMemoryChatMemory
    public ChatGptController(ChatClient.Builder builder) {
        this.chatMemory = new SimpleChatMemory();
        this.chatClient = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(this.chatMemory).build()
                )
                .build();
    }

    @GetMapping("")
    public String home() {
        return "index";
    }

    @HxRequest
    @PostMapping("/api/chat")
    public HtmxResponse generate(@RequestParam String message, HttpSession session, Model model) {
        log.info("User Message: {}", message);
        String conversationId = getOrCreateConversationId(session);
        String response = chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        model.addAttribute("response",response);
        model.addAttribute("message",message);
        model.addAttribute("conversationId", conversationId);

        return HtmxResponse.builder()
                .view("response::responseFragment")
                .view("recent-message-list::messageFragment")
                .build();
    }

    private String getOrCreateConversationId(HttpSession session) {
        String conversationId = (String) session.getAttribute("conversationId");

        if (conversationId == null) {
            conversationId = UUID.randomUUID().toString();
            session.setAttribute("conversationId", conversationId);
        }

        return conversationId;
    }
}
