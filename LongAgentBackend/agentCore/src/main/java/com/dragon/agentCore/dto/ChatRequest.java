package com.dragon.agentCore.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String userId;
    private String sessionId;
    private String message;
}
