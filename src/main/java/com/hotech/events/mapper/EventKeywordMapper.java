package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.EventKeyword;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 事件关键词Mapper接口
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Repository
public interface EventKeywordMapper extends BaseMapper<EventKeyword> {

    /**
     * 根据事件ID查询关键词
     * 
     * @param eventId 事件ID
     * @return 关键词列表
     */
    List<EventKeyword> selectByEventId(@Param("eventId") Long eventId);

    /**
     * 根据关键词查询事件
     * 
     * @param keyword 关键词
     * @return 事件关键词列表
     */
    List<EventKeyword> selectByKeyword(@Param("keyword") String keyword);

    /**
     * 批量插入关键词
     * 
     * @param eventId 事件ID
     * @param keywords 关键词列表
     * @return 插入数量
     */
    int batchInsert(@Param("eventId") Long eventId, @Param("keywords") List<String> keywords);

    /**
     * 删除事件的所有关键词
     * 
     * @param eventId 事件ID
     * @return 删除数量
     */
    int deleteByEventId(@Param("eventId") Long eventId);

    /**
     * 统计热门关键词
     * 
     * @param limit 限制数量
     * @return 热门关键词统计
     */
    List<java.util.Map<String, Object>> countHotKeywords(@Param("limit") Integer limit);
} 