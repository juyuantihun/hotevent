package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.EventRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 事件关系Mapper接口
 */
@Mapper
public interface EventRelationMapper extends BaseMapper<EventRelation> {
    
    /**
     * 根据事件ID列表查询关联关系
     * @param eventIds 事件ID列表
     * @return 关联关系列表
     */
    @Select({
        "<script>",
        "SELECT er.* FROM event_relation er",
        "<where>",
        "   <if test='eventIds != null and eventIds.size() > 0'>",
        "       AND (",
        "           er.source_event_id IN",
        "           <foreach collection='eventIds' item='id' open='(' separator=',' close=')'>",
        "               #{id}",
        "           </foreach>",
        "           OR er.target_event_id IN",
        "           <foreach collection='eventIds' item='id' open='(' separator=',' close=')'>",
        "               #{id}",
        "           </foreach>",
        "       )",
        "   </if>",
        "   AND er.status = 1",
        "</where>",
        "ORDER BY er.created_at DESC",
        "</script>"
    })
    List<Map<String, Object>> findRelationsByEventIds(@Param("eventIds") List<Long> eventIds);
    
    /**
     * 查询所有事件关系
     * @return 关系列表
     */
    @Select("SELECT * FROM event_relation WHERE status = 1 ORDER BY created_at DESC LIMIT 50")
    List<Map<String, Object>> findAllRelations();
}