-- 创建提示词模板表
CREATE TABLE IF NOT EXISTS `prompt_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_type` VARCHAR(50) NOT NULL COMMENT '模板类型：event_fetch(事件检索), event_validation(事件验证), timeline_organize(时间线编制)',
    `template_content` TEXT NOT NULL COMMENT '模板内容',
    `version` VARCHAR(20) NOT NULL DEFAULT '1.0' COMMENT '版本号',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否激活：1-激活，0-未激活',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_name_type` (`template_name`, `template_type`),
    INDEX `idx_template_type` (`template_type`),
    INDEX `idx_is_active` (`is_active`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词模板表';

-- 插入默认模板数据
INSERT INTO `prompt_template` (`template_name`, `template_type`, `template_content`, `version`, `is_active`) VALUES
('默认事件检索模板', 'event_fetch', 
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
5. 每个事件都要包含可靠的来源信息
6. 按照时间顺序排列事件

返回格式：{responseFormat}', 
'1.0', 1),

('默认事件验证模板', 'event_validation',
'请验证以下事件的真实性和准确性：

{eventsList}

验证标准：
1. 事件是否真实发生
2. 时间、地点信息是否准确
3. 事件描述是否客观
4. 是否有可靠来源支持
5. 事件逻辑是否合理
6. 是否存在明显的偏见或错误信息

对于每个事件，请提供：
- 真实性评估（true/false）
- 可信度评分（0-1之间）
- 发现的问题列表
- 改进建议

返回格式：{responseFormat}',
'1.0', 1),

('默认时间线编制模板', 'timeline_organize',
'请将以下已验证的事件组织成连贯的时间线：

时间线主题：{timelineName}

事件列表：
{eventsList}

已知事件关系：
{relationsList}

组织原则：
1. 按时间顺序排列事件
2. 识别和建立因果关系
3. 突出关键转折点和里程碑事件
4. 保持逻辑连贯性
5. 标注事件的重要程度
6. 识别并行发生的事件

请分析事件间的关系，包括：
- 因果关系（cause）
- 时间先后关系（sequence）
- 并行关系（parallel）
- 影响关系（influence）

返回格式：{responseFormat}',
'1.0', 1);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS `idx_template_type_active` ON `prompt_template` (`template_type`, `is_active`);
CREATE INDEX IF NOT EXISTS `idx_version` ON `prompt_template` (`version`);