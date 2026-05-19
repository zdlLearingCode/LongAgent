package com.dragon.agentCore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatStreamResponse {
    private String sessionId;
    private String messageType;
    private String content;

    public static ChatStreamResponse text(String sessionId, String content) {
        return ChatStreamResponse.builder()
                .sessionId(sessionId)
                .messageType("TEXT")
                .content(content)
                .build();
    }

    public static ChatStreamResponse done(String sessionId) {
        return ChatStreamResponse.builder()
                .sessionId(sessionId)
                .messageType("DONE")
                .content("")
                .build();
    }
}
