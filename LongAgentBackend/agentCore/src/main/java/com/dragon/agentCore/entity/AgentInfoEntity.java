package com.dragon.agentCore.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 智能体信息表
 *
 * @author dlzhang13
 * @create 2026/4/29 17:14
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("agent_info")
public class AgentInfoEntity extends BaseEntity {
    /**
     * 智能体名称
     */
    private String name;

    /**
     * 智能体描述
     */
    private String description;

    /**
     * 智能体图标
     */
    private String icon;

    /**
     * 系统提示词
     */
    private String systemPrompt;
}
