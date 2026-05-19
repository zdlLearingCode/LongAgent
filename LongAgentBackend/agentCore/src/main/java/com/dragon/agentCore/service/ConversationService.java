package com.dragon.agentCore.service;

import com.dragon.agentCore.entity.ConversationHistoryEntity;

import java.util.List;

public interface ConversationService {

    void saveMessage(String userId, String sessionId, String role, String content);

    List<ConversationHistoryEntity> getHistory(String userId, String sessionId, int limit);
}
