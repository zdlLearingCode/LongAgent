package com.dragon.agentCore.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

/**
 * 数据库基础审计信息字段
 *
 * @author dlzhang13
 * @create 2026/4/29 17:18
 */
@Data
public class BaseEntity {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新人
     */
    private String updateUser;

    /**
     * 是否已删除
     */
    @TableLogic
    private Boolean delFlag;
}
