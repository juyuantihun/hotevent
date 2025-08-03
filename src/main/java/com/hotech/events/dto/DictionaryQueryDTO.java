package com.hotech.events.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 字典查询DTO
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典名称（模糊查询）
     */
    private String dictName;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 是否自动添加：0-否，1-是
     */
    private Integer isAutoAdded;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
} 