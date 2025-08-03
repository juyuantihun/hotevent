package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 地区实体类
 * 用于存储地区信息，一个地区可以包含多个国家、城市等
 */
@Data
@TableName("region")
public class Region {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 地区名称
     */
    private String name;
    
    /**
     * 地区类型：CUSTOM(自定义), CONTINENT(洲), COUNTRY(国家), PROVINCE(省份), CITY(城市)
     */
    private String type;
    
    /**
     * 父地区ID
     */
    private Long parentId;
    
    /**
     * 子地区列表（不存储在数据库中，用于构建树形结构）
     */
    @TableField(exist = false)
    private List<Region> children;
    
    // 注释掉不存在的字段
    // /**
    //  * 地区描述
    //  */
    // private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}