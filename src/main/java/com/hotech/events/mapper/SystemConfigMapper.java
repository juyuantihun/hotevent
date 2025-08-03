package com.hotech.events.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotech.events.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统配置Mapper接口
 * 
 * @author system
 * @since 2025-01-24
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键查询配置
     * 
     * @param configKey 配置键
     * @return 系统配置
     */
    @Select("SELECT * FROM system_config WHERE config_key = #{configKey}")
    SystemConfig selectByKey(@Param("configKey") String configKey);

    /**
     * 根据配置分组查询配置列表
     * 
     * @param configGroup 配置分组
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_config WHERE config_group = #{configGroup} ORDER BY config_key")
    List<SystemConfig> selectByGroup(@Param("configGroup") String configGroup);

    /**
     * 查询所有必需的配置
     * 
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_config WHERE is_required = 1 ORDER BY config_group, config_key")
    List<SystemConfig> selectAllRequired();

    /**
     * 查询所有加密的配置
     * 
     * @return 系统配置列表
     */
    @Select("SELECT * FROM system_config WHERE is_encrypted = 1 ORDER BY config_group, config_key")
    List<SystemConfig> selectAllEncrypted();
}