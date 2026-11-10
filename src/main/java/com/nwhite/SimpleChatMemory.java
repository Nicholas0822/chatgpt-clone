package com.nwhite;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimpleChatMemory implements ChatMemory {
    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();
    private final int maxMessagesPerConversation = 40;

    @Override
    public void add(String conversationId, Message message) {
        if (conversationId == null || message == null) {
            return;
        }

        List<Message> messages = conversations.computeIfAbsent(
                conversationId,
                k -> new CopyOnWriteArrayList<>()
        );

        messages.add(message);

        while (messages.size() > maxMessagesPerConversation) {
            messages.remove(0);
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null) {
            return;
        }

        for (Message message : messages) {
            add(conversationId, message);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> messages = conversations.get(conversationId);

        if (messages == null) {
            return List.of();
        }

        return List.copyOf(messages);
    }

    public List<Message> get(String conversationId, int lastN) {
        List<Message> all = get(conversationId);

        if (lastN <= 0 || all.size() <= lastN) {
            return all;
        }

        int from = all.size() - lastN;
        return List.copyOf(all.subList(from, all.size()));
    }

    @Override
    public void clear(String conversationId) {
        if (conversationId != null) {
            conversations.remove(conversationId);
        }
    }
}
