# 热点事件管理系统数据库设计文档

## 1. 系统概述

### 1.1 项目简介
热点事件管理系统（HotEvents）是一个智能化的国际热点事件采集、分析和管理平台。系统通过DeepSeek AI技术自动获取全球热点事件，支持人工录入，并提供事件关联分析、实体关系管理等功能。

### 1.2 技术架构
- **数据库引擎**: MySQL 5.7+
- **字符集**: UTF8MB4
- **存储引擎**: InnoDB
- **数据规模**: 支持百万级事件数据存储

### 1.3 核心功能
- 国际热点事件自动采集与人工录入
- 事件关联关系分析
- 实体（国家、组织、人物）管理
- 字典数据管理
- 系统配置管理
- 用户权限管理

## 2. 数据库架构设计

### 2.1 整体架构

```
                    热点事件管理系统
    ┌──────────────────────────────────────────────────────────┐
    │                                             		       │
    │   ┌─────────────┐  ┌───────────────┐   ┌─────────────┐   │
    │   │ 事件管理模块  │  │ 实体管理模块     │   │ 系统管理模块  │	│
    │   │             │  │               │   │             │   │
    │   │ • event     │  │ • country     │   │ • user      │   │
    │   │ • event_    │  │ • organization│   │ • system_   │   │
    │   │   keyword   │  │ • person      │   │   config    │   │
    │   │ • event_    │  │ • entity_     │   │ • dictionary│   │
    │   │   relation  │  │   relationship│   │             │   │
    │   └─────────────┘  └───────────────┘   └─────────────┘   │
    │                                                          │
    └──────────────────────────────────────────────────────────┘
```

### 2.2 数据分层
- **核心业务层**: 事件数据、实体数据
- **关系映射层**: 事件关联、实体关系
- **字典配置层**: 数据字典、系统配置
- **权限管理层**: 用户管理、权限控制

## 3. 表结构详细设计

### 3.1 事件管理模块

#### 3.1.1 event（事件表）
| 字段名 | 数据类型 | 长度 | 非空 | 主键 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | bigint | 20 | Y | Y | - | 事件ID |
| event_code | varchar | 100 | Y | - | - | 事件编码 |
| event_time | datetime | - | Y | - | - | 事件发生时间 |
| event_location | varchar | 500 | N | - | - | 事件地点 |
| event_type | varchar | 100 | N | - | - | 事件类型 |
| event_description | text | - | N | - | - | 事件描述 |
| subject | varchar | 200 | N | - | - | 事件主体 |
| object | varchar | 200 | N | - | - | 事件客体 |
| longitude | decimal | 10,7 | N | - | - | 经度 |
| latitude | decimal | 10,7 | N | - | - | 纬度 |
| source_type | tinyint | 1 | Y | - | 1 | 来源类型：1-自动获取，2-手动录入 |
| status | tinyint | 1 | Y | - | 1 | 状态：0-禁用，1-启用 |
| created_at | datetime | - | Y | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | - | Y | - | CURRENT_TIMESTAMP | 更新时间 |
| created_by | varchar | 100 | N | - | - | 创建人 |
| updated_by | varchar | 100 | N | - | - | 更新人 |

**索引设计**:
- 唯一索引: `uk_event_code`(event_code)
- 普通索引: `idx_event_time`(event_time), `idx_event_type`(event_type)
- 复合索引: `idx_location`(longitude, latitude)

#### 3.1.2 event_keyword（事件关键词表）
| 字段名 | 数据类型 | 长度 | 非空 | 主键 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | bigint | 20 | Y | Y | - | ID |
| event_id | bigint | 20 | Y | - | - | 事件ID |
| keyword | varchar | 100 | Y | - | - | 关键词 |
| created_at | datetime | - | Y | - | CURRENT_TIMESTAMP | 创建时间 |

**索引设计**:
- 普通索引: `idx_event_id`(event_id), `idx_keyword`(keyword)

#### 3.1.3 event_relation（事件关联关系表）
| 字段名 | 数据类型 | 长度 | 非空 | 主键 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | bigint | 20 | Y | Y | - | 关联ID |
| source_event_id | bigint | 20 | Y | - | - | 源事件ID |
| target_event_id | bigint | 20 | Y | - | - | 目标事件ID |
| relation_type | varchar | 50 | Y | - | - | 关联类型 |
| relation_name | varchar | 100 | Y | - | - | 关系名称 |
| intensity_level | int | 11 | N | - | - | 强度级别(1-10) |
| relation_description | text | - | N | - | - | 关联描述 |
| confidence | decimal | 3,2 | N | - | 1.00 | 置信度（0-1） |
| status | tinyint | 1 | Y | - | 1 | 状态：0-禁用，1-启用 |
| created_at | datetime | - | Y | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | - | Y | - | CURRENT_TIMESTAMP | 更新时间 |
| created_by | varchar | 100 | N | - | - | 创建人 |
| updated_by | varchar | 100 | N | - | - | 更新人 |

**索引设计**:
- 普通索引: `idx_source_event`(source_event_id), `idx_target_event`(target_event_id)
- 普通索引: `idx_relation_type`(relation_type)

### 3.2 实体管理模块

#### 3.2.1 country（国家表）
| 字段名 | 数据类型 | 长度 | 非空 | 主键 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | bigint | 20 | Y | Y | - | 主键ID |
| name | varchar | 64 | Y | - | - | 国家名称 |
| short_name | varchar | 32 | N | - | - | 简称 |
| population | bigint | 20 | N | - | - | 人口 |
| area | double | - | N | - | - | 面积（平方公里） |
| capital | varchar | 64 | N | - | - | 首都 |
| language | varchar | 64 | N | - | - | 官方语言 |
| currency | varchar | 32 | N | - | - | 货币 |
| created_at | datetime | - | N | - | - | 创建时间 |
| updated_at | datetime | - | N | - | - | 更新时间 |

#### 3.2.2 organization（组织表）
| 字段名 | 数据类型 | 长度 | 非空 | 主键 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | bigint | 20 | Y | Y | - | 主键ID |
| name | varchar | 100 | Y | - | - | 组织名称 |
| short_name | varchar | 50 | N | - | - | 组织简称 |
| type | varchar | 50 | N | - | - | 组织类型 |
| country_id | bigint | 20 | N | - | - | 所属国家ID |
| description | varchar | 255 | N | - | - | 组织描述 |
| status | tinyint | 4 | N | - | 1 | 状态：0-禁用，1-启用 |
| created_at | datetime | - | N | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | - | N | - | CURRENT_TIMESTAMP | 更新时间 |
| created_by | varchar | 50 | N | - | - | 创建人 |
| updated_by | varchar | 50 | N | - | - | 更新人 |

#### 3.2.3 person（人物表）
| 字段名 | 数据类型 | 长度 | 非空 | 主键 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | bigint | 20 | Y | Y | - | 主键ID |
| name | varchar | 50 | Y | - | - | 姓名 |
| gender | varchar | 10 | N | - | - | 性别 |
| birth_date | date | - | N | - | - | 出生日期 |
| country_id | bigint | 20 | N | - | - | 国籍ID |
| organization_id | bigint | 20 | N | - | - | 所属组织ID |
| description | varchar | 255 | N | - | - | 人物描述 |
| status | tinyint | 4 | N | - | 1 | 状态：0-禁用，1-启用 |
| created_at | datetime | - | N | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | - | N | - | CURRENT_TIMESTAMP | 更新时间 |
| created_by | varchar | 50 | N | - | - | 创建人 |
| updated_by | varchar | 50 | N | - | - | 更新人 |

#### 3.2.4 entity_relationship（实体关系表）
| 字段名 | 数据类型 | 长度 | 非空 | 主键 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | bigint | 20 | Y | Y | - | 关系ID |
| source_entity_type | varchar | 32 | Y | - | - | 源实体类型 |
| source_entity_id | bigint | 20 | Y | - | - | 源实体ID |
| target_entity_type | varchar | 32 | Y | - | - | 目标实体类型 |
| target_entity_id | bigint | 20 | Y | - | - | 目标实体ID |
| relationship_type | varchar | 50 | Y | - | - | 关系类型 |
| relationship_description | text | - | N | - | - | 关系描述 |
| status | tinyint | 4 | N | - | 1 | 状态：0-禁用，1-启用 |
| created_at | datetime | - | N | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | - | N | - | CURRENT_TIMESTAMP | 更新时间 |
| created_by | varchar | 50 | N | - | - | 创建人 |
| updated_by | varchar | 50 | N | - | - | 更新人 |

**索引设计**:
- 复合索引: `idx_source`(source_entity_type, source_entity_id)
- 复合索引: `idx_target`(target_entity_type, target_entity_id)

### 3.3 系统管理模块

#### 3.3.1 dictionary（字典表）
| 字段名 | 数据类型 | 长度 | 非空 | 主键 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | bigint | 20 | Y | Y | - | 字典ID |
| dict_type | varchar | 50 | Y | - | - | 字典类型 |
| dict_code | varchar | 100 | Y | - | - | 字典编码 |
| dict_name | varchar | 200 | Y | - | - | 字典名称 |
| dict_description | text | - | N | - | - | 字典描述 |
| parent_id | bigint | 20 | N | - | 0 | 父级ID |
| sort_order | int | 11 | N | - | 0 | 排序 |
| status | tinyint | 1 | Y | - | 1 | 状态：0-禁用，1-启用 |
| is_auto_added | tinyint | 1 | Y | - | 0 | 是否自动添加 |
| entity_type | varchar | 32 | N | - | - | 实体类型 |
| entity_id | bigint | 20 | N | - | - | 实体ID |
| created_at | datetime | - | Y | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | - | Y | - | CURRENT_TIMESTAMP | 更新时间 |
| created_by | varchar | 100 | N | - | - | 创建人 |
| updated_by | varchar | 100 | N | - | - | 更新人 |

**索引设计**:
- 唯一索引: `uk_dict_type_code`(dict_type, dict_code)
- 普通索引: `idx_dict_type`(dict_type)

#### 3.3.2 system_config（系统配置表）
| 字段名 | 数据类型 | 长度 | 非空 | 主键 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | bigint | 20 | Y | Y | - | 配置ID |
| config_key | varchar | 100 | Y | - | - | 配置键 |
| config_value | text | - | N | - | - | 配置值 |
| config_description | varchar | 500 | N | - | - | 配置描述 |
| config_type | varchar | 50 | Y | - | STRING | 配置类型 |
| status | tinyint | 1 | Y | - | 1 | 状态：0-禁用，1-启用 |
| created_at | datetime | - | Y | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | - | Y | - | CURRENT_TIMESTAMP | 更新时间 |

**索引设计**:
- 唯一索引: `uk_config_key`(config_key)

#### 3.3.3 user（用户表）
| 字段名 | 数据类型 | 长度 | 非空 | 主键 | 默认值 | 注释 |
|--------|----------|------|------|------|--------|------|
| id | bigint | 20 | Y | Y | - | 用户ID |
| username | varchar | 50 | Y | - | - | 用户名 |
| password | varchar | 255 | Y | - | - | 密码 |
| real_name | varchar | 100 | N | - | - | 真实姓名 |
| email | varchar | 100 | N | - | - | 邮箱 |
| phone | varchar | 20 | N | - | - | 手机号 |
| status | tinyint | 1 | Y | - | 1 | 状态：0-禁用，1-启用 |
| created_at | datetime | - | Y | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | datetime | - | Y | - | CURRENT_TIMESTAMP | 更新时间 |

**索引设计**:
- 唯一索引: `uk_username`(username)

## 4. 数据关系设计

### 4.1 实体关系图（ERD）

```
       实体关系模型
    ┌─────────────────────────────────────────┐
    │                                         │
    │  ┌─────────────┐      ┌─────────────┐   │
    │  │   country   │      │organization │   │
    │  │             │      │             │   │
    │  │ id (PK)     │ 1:N  │ id (PK)     │   │
    │  │ name        │────→ │ name        │   │
    │  │ short_name  │      │ country_id  │   │
    │  │ population  │      │ type        │   │
    │  │ ...         │      │ ...         │   │
    │  └─────────────┘      └─────────────┘   │
    │         │                     │         │
    │         │ 1:N            1:N  │         │
    │         │                     │         │
    │         ▼                     ▼         │
    │  ┌─────────────┐      ┌─────────────┐   │
    │  │   person    │      │entity_relat-│   │
    │  │             │      │ionship      │   │
    │  │ id (PK)     │ M:N  │ id (PK)     │   │
    │  │ name        │◄───► │ source_*    │   │
    │  │ country_id  │      │ target_*    │   │
    │  │ org_id      │      │ relation_*  │   │
    │  │ ...         │      │ ...         │   │
    │  └─────────────┘      └─────────────┘   │
    │                                         │
    └─────────────────────────────────────────┘

       事件关系模型
    ┌─────────────────────────────────────────┐
    │                                         │
    │  ┌─────────────┐      ┌─────────────┐   │
    │  │    event    │      │event_keyword│   │
    │  │             │      │             │   │
    │  │ id (PK)     │ 1:N  │ id (PK)     │   │
    │  │ event_code  │────→ │ event_id    │   │
    │  │ event_time  │      │ keyword     │   │
    │  │ description │      │ ...         │   │
    │  │ ...         │      └─────────────┘   │
    │  └─────────────┘                        │
    │         │                               │
    │         │ M:N                           │
    │         │                               │
    │         ▼                               │
    │  ┌─────────────┐                        │
    │  │event_relat- │                        │
    │  │ion          │                        │
    │  │ id (PK)     │                        │
    │  │ source_id   │                        │
    │  │ target_id   │                        │
    │  │ relation_*  │                        │
    │  │ ...         │                        │
    │  └─────────────┘                        │
    │                                         │
    └─────────────────────────────────────────┘
```

### 4.2 关系说明

#### 4.2.1 一对多关系
- **country** → **organization**: 一个国家包含多个组织
- **country** → **person**: 一个国家包含多个人物
- **organization** → **person**: 一个组织包含多个人物
- **event** → **event_keyword**: 一个事件包含多个关键词
- **event** → **event_relation**: 一个事件可以与多个事件建立关联

#### 4.2.2 多对多关系
- **event** ↔ **event**: 通过event_relation表建立多对多关系
- **entity** ↔ **entity**: 通过entity_relationship表建立多对多关系

#### 4.2.3 字典关联
- **dictionary** 与各实体表通过entity_type和entity_id建立关联
- 支持灵活的字典数据管理

## 5. 数据约束与规则

### 5.1 主键约束
- 所有表都使用bigint类型的自增主键
- 主键名称统一为`id`

### 5.2 唯一约束
- event.event_code: 事件编码唯一
- dictionary(dict_type, dict_code): 字典类型和编码组合唯一
- system_config.config_key: 配置键唯一
- user.username: 用户名唯一

### 5.3 外键约束
- 系统设计时考虑了外键关系，但实际实现中未使用外键约束
- 采用应用层控制数据一致性

### 5.4 检查约束
- status字段统一使用0/1表示禁用/启用
- source_type字段使用1/2表示不同来源类型
- intensity_level字段范围为1-10

## 6. 性能优化建议

### 6.1 索引策略
- **单列索引**: 对频繁查询的字段建立单列索引
- **复合索引**: 对多字段联合查询建立复合索引
- **唯一索引**: 保证数据唯一性的同时提升查询性能

### 6.2 分区策略
- 可考虑对event表按时间分区
- 对历史数据进行归档处理

### 6.3 缓存策略
- 字典数据适合缓存
- 系统配置数据适合缓存
- 热点事件数据可考虑缓存

### 6.4 查询优化
- 避免全表扫描
- 合理使用limit分页
- 优化复杂关联查询

## 7. 数据安全与备份

### 7.1 数据安全
- 用户密码采用加密存储
- 敏感配置信息加密存储
- 实施访问控制和权限管理

### 7.2 备份策略
- 定期全量备份
- 实时增量备份
- 异地备份保证数据安全

### 7.3 恢复机制
- 支持点在时间恢复
- 提供数据回滚机制
- 建立灾备恢复流程

## 8. 扩展性设计

### 8.1 水平扩展
- 支持读写分离
- 支持分库分表
- 支持集群部署

### 8.2 垂直扩展
- 表结构支持字段扩展
- 字典表支持动态扩展
- 配置表支持参数扩展

### 8.3 版本兼容
- 支持数据库版本升级
- 提供迁移脚本
- 保持向后兼容性

## 9. 版本信息

| 版本 | 日期 | 修改内容 | 修改人 |
|------|------|----------|--------|
| 1.0 | 2025-01-11 | 初始版本 | 邵石磊 |

---

**文档说明**: 本文档基于MySQL 5.7数据库设计，采用UTF8MB4字符集，支持完整的Unicode字符存储。所有表均使用InnoDB存储引擎，保证事务一致性和并发性能。 