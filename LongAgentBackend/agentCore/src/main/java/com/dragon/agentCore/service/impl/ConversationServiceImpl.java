package com.dragon.agentCore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dragon.agentCore.entity.ConversationHistoryEntity;
import com.dragon.agentCore.mapper.ConversationHistoryMapper;
import com.dragon.agentCore.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationHistoryMapper conversationHistoryMapper;

    @Override
    public void saveMessage(String userId, String sessionId, String role, String content) {
        ConversationHistoryEntity entity = new ConversationHistoryEntity();
        entity.setUserId(userId);
        entity.setSessionId(sessionId);
        entity.setRole(role);
        entity.setContent(content);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        conversationHistoryMapper.insert(entity);
    }

    @Override
    public List<ConversationHistoryEntity> getHistory(String userId, String sessionId, int limit) {
        QueryWrapper<ConversationHistoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("session_id", sessionId)
               .orderByAsc("create_time")
               .last("LIMIT " + limit);
        return conversationHistoryMapper.selectList(wrapper);
    }
}
