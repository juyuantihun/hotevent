package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.ConfigChangeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 配置变更历史Mapper接口
 * 
 * @author system
 * @since 2025-01-24
 */
@Mapper
public interface ConfigChangeLogMapper extends BaseMapper<ConfigChangeLog> {

    /**
     * 根据配置键查询变更历史
     * 
     * @param configKey 配置键
     * @return 配置变更历史列表
     */
    @Select("SELECT * FROM config_change_log WHERE config_key = #{configKey} ORDER BY changed_at DESC")
    List<ConfigChangeLog> selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据时间范围查询变更历史
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 配置变更历史列表
     */
    @Select("SELECT * FROM config_change_log WHERE changed_at BETWEEN #{startTime} AND #{endTime} ORDER BY changed_at DESC")
    List<ConfigChangeLog> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据变更人查询变更历史
     * 
     * @param changedBy 变更人
     * @return 配置变更历史列表
     */
    @Select("SELECT * FROM config_change_log WHERE changed_by = #{changedBy} ORDER BY changed_at DESC")
    List<ConfigChangeLog> selectByChangedBy(@Param("changedBy") String changedBy);
}