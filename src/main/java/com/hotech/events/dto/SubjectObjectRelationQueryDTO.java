package com.hotech.events.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 主体客体关系查询DTO
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectObjectRelationQueryDTO implements Serializable {

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
     * 主体编码
     */
    private String subjectCode;

    /**
     * 客体编码
     */
    private String objectCode;

    /**
     * 关系类型
     */
    private String relationType;

    /**
     * 关系名称（模糊搜索）
     */
    private String relationName;

    /**
     * 强度级别
     */
    private Integer intensityLevel;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;
} 