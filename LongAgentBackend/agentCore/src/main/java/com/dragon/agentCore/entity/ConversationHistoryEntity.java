package com.dragon.agentCore.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("conversation_history")
public class ConversationHistoryEntity extends BaseEntity {
    private String userId;
    private String sessionId;
    private String role;
    private String content;
}
