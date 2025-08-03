package com.hotech.events.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 地区项目实体类
 * 用于存储地区包含的具体项目（国家、城市等）
 */
@Data
@TableName("region_item")
public class RegionItem {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联的地区ID
     */
    private Long regionId;
    
    /**
     * 字典项ID（关联dictionary表）
     */
    private Long dictionaryId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}