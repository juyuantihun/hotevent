-- 提示词模板配置表
CREATE TABLE `prompt_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_type` VARCHAR(50) NOT NULL COMMENT '模板类型：event_fetch-事件检索，event_validation-事件验证，timeline_organize-时间线编制',
    `template_content` TEXT NOT NULL COMMENT '模板内容',
    `response_format` TEXT COMMENT '响应格式模板',
    `version` VARCHAR(20) NOT NULL DEFAULT '1.0' COMMENT '版本号',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    `description` TEXT COMMENT '模板描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(100) DEFAULT 'system' COMMENT '创建人',
    `updated_by` VARCHAR(100) DEFAULT 'system' COMMENT '更新人',
    UNIQUE KEY `uk_name_type` (`template_name`, `template_type`),
    INDEX `idx_template_type` (`template_type`),
    INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词模板配置表';

-- 插入默认提示词模板
INSERT INTO `prompt_template` (`template_name`, `template_type`, `template_content`, `response_format`, `description`) VALUES
('default_event_fetch', 'event_fetch', 
'你是一个专业的国际事件分析师。请根据以下条件，从可靠的新闻源中检索真实的国际事件：

时间线名称：{timelineName}
时间线描述：{timelineDescription}
目标地区：{regions}
时间范围：{startTime} 至 {endTime}

请严格按照以下要求：
1. 只返回真实发生的事件
2. 事件必须与指定地区和时间范围相关
3. 优先选择具有重大影响的事件
4. 确保事件信息的准确性和完整性

返回格式：{responseFormat}',
'{
  "events": [
    {
      "title": "事件标题",
      "description": "详细描述",
      "eventTime": "2024-01-01T12:00:00",
      "location": "具体地点",
      "subject": "事件主体",
      "object": "事件客体", 
      "eventType": "事件类型",
      "keywords": ["关键词1", "关键词2"],
      "sources": ["来源1", "来源2"],
      "credibilityScore": 0.95
    }
  ]
}',
'默认事件检索提示词模板'),

('default_event_validation', 'event_validation',
'请验证以下事件的真实性和准确性：

{eventsList}

验证标准：
1. 事件是否真实发生
2. 时间、地点信息是否准确
3. 事件描述是否客观
4. 是否有可靠来源支持

返回格式：{responseFormat}',
'{
  "validationResults": [
    {
      "eventId": "事件ID",
      "isValid": true,
      "credibilityScore": 0.95,
      "issues": [],
      "suggestions": []
    }
  ]
}',
'默认事件验证提示词模板'),

('default_timeline_organize', 'timeline_organize',
'请将以下已验证的事件组织成连贯的时间线：

时间线主题：{timelineName}
事件列表：{eventsList}
事件关系：{relationsList}

组织原则：
1. 按时间顺序排列
2. 识别因果关系
3. 突出关键节点
4. 保持逻辑连贯

返回格式：{responseFormat}',
'{
  "timeline": {
    "name": "时间线名称",
    "events": [
      {
        "eventId": "事件ID",
        "order": 1,
        "importance": "high",
        "relations": ["相关事件ID"]
      }
    ],
    "relations": [
      {
        "sourceEventId": "源事件ID",
        "targetEventId": "目标事件ID",
        "relationType": "cause",
        "description": "关系描述"
      }
    ]
  }
}',
'默认时间线编制提示词模板');

-- 环境变量配置表
CREATE TABLE `system_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(50) NOT NULL DEFAULT 'STRING' COMMENT '配置类型：STRING-字符串，NUMBER-数字，BOOLEAN-布尔值，JSON-JSON对象',
    `config_group` VARCHAR(50) NOT NULL COMMENT '配置分组：deepseek-DeepSeek配置，validation-验证配置，storage-存储配置',
    `description` TEXT COMMENT '配置描述',
    `is_encrypted` TINYINT(1) DEFAULT 0 COMMENT '是否加密：0-否，1-是',
    `is_required` TINYINT(1) DEFAULT 0 COMMENT '是否必需：0-否，1-是',
    `default_value` TEXT COMMENT '默认值',
    `validation_rule` TEXT COMMENT '验证规则（正则表达式或JSON Schema）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(100) DEFAULT 'system' COMMENT '创建人',
    `updated_by` VARCHAR(100) DEFAULT 'system' COMMENT '更新人',
    UNIQUE KEY `uk_config_key` (`config_key`),
    INDEX `idx_config_group` (`config_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `config_group`, `description`, `is_required`, `default_value`) VALUES
('deepseek.api.key', '${DEEPSEEK_API_KEY}', 'STRING', 'deepseek', 'DeepSeek API密钥', 1, ''),
('deepseek.api.url', 'https://api.deepseek.com/v1/chat/completions', 'STRING', 'deepseek', 'DeepSeek API地址', 1, 'https://api.deepseek.com/v1/chat/completions'),
('deepseek.api.model', 'deepseek-chat', 'STRING', 'deepseek', 'DeepSeek模型名称', 1, 'deepseek-chat'),
('deepseek.api.timeout', '60000', 'NUMBER', 'deepseek', 'API超时时间（毫秒）', 1, '60000'),
('deepseek.api.max-retries', '3', 'NUMBER', 'deepseek', '最大重试次数', 1, '3'),
('deepseek.cache.ttl', '300000', 'NUMBER', 'deepseek', '缓存TTL（毫秒）', 0, '300000'),
('deepseek.rate-limit', '60', 'NUMBER', 'deepseek', '每分钟请求限制', 0, '60'),
('validation.credibility-threshold', '0.7', 'NUMBER', 'validation', '可信度阈值', 0, '0.7'),
('validation.strict-mode', 'false', 'BOOLEAN', 'validation', '是否启用严格验证模式', 0, 'false'),
('storage.batch-size', '100', 'NUMBER', 'storage', '批量存储大小', 0, '100'),
('storage.enable-deduplication', 'true', 'BOOLEAN', 'storage', '是否启用去重', 0, 'true'),
('prompt.template.cache.refresh-interval', '300', 'NUMBER', 'prompt', '提示词模板缓存刷新间隔（秒）', 0, '300'),
('prompt.template.watch.enabled', 'true', 'BOOLEAN', 'prompt', '是否启用配置文件监听', 0, 'true');

-- 配置变更历史表
CREATE TABLE `config_change_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `old_value` TEXT COMMENT '旧值',
    `new_value` TEXT COMMENT '新值',
    `change_type` VARCHAR(20) NOT NULL COMMENT '变更类型：CREATE-创建，UPDATE-更新，DELETE-删除',
    `change_reason` TEXT COMMENT '变更原因',
    `changed_by` VARCHAR(100) COMMENT '变更人',
    `changed_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    INDEX `idx_config_key` (`config_key`),
    INDEX `idx_changed_at` (`changed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置变更历史表';