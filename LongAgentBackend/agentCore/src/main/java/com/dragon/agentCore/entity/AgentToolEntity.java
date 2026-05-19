package com.dragon.agentCore.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("agent_tool")
public class AgentToolEntity extends BaseEntity {
    private Long agentId;
    private String toolName;
}
