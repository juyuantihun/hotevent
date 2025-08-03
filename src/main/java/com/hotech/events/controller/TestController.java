package com.hotech.events.controller;

import com.hotech.events.dto.ApiResponse;
import com.hotech.events.util.SqlExecutor;
import com.hotech.events.entity.Dictionary;
import com.hotech.events.entity.Organization;
import com.hotech.events.entity.Person;
import com.hotech.events.mapper.DictionaryMapper;
import com.hotech.events.mapper.OrganizationMapper;
import com.hotech.events.mapper.PersonMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试控制器
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@Tag(name = "测试接口", description = "测试相关的API接口")
public class TestController {

    @Autowired
    private SqlExecutor sqlExecutor;
    
    @Autowired
    private DictionaryMapper dictionaryMapper;
    @Autowired
    private OrganizationMapper organizationMapper;
    @Autowired
    private PersonMapper personMapper;

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务状态")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        try {
            log.info("健康检查请求");
            return ResponseEntity.ok(ApiResponse.success("服务正常运行", "OK"));
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return ResponseEntity.ok(ApiResponse.error("服务异常：" + e.getMessage()));
        }
    }

    /**
     * 执行测试数据SQL脚本
     */
    @PostMapping("/execute-test-data")
    @Operation(summary = "执行测试数据SQL脚本", description = "执行test_data.sql文件")
    public ResponseEntity<ApiResponse<String>> executeTestData() {
        try {
            log.info("执行测试数据SQL脚本");
            
            String filePath = "test_data.sql";
            boolean success = sqlExecutor.executeSqlFile(filePath);
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("测试数据执行成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("测试数据执行失败"));
            }
        } catch (Exception e) {
            log.error("执行测试数据SQL脚本失败", e);
            return ResponseEntity.ok(ApiResponse.error("执行失败：" + e.getMessage()));
        }
    }
    
    /**
     * 执行中文字典数据SQL脚本
     */
    @PostMapping("/execute-chinese-data")
    @Operation(summary = "执行中文字典数据SQL脚本", description = "执行clean_and_insert_chinese_data.sql文件")
    public ResponseEntity<ApiResponse<String>> executeChineseData() {
        try {
            log.info("执行中文字典数据SQL脚本");
            
            String filePath = "clean_and_insert_chinese_data.sql";
            boolean success = sqlExecutor.executeSqlFile(filePath);
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("中文字典数据执行成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("中文字典数据执行失败"));
            }
        } catch (Exception e) {
            log.error("执行中文字典数据SQL脚本失败", e);
            return ResponseEntity.ok(ApiResponse.error("执行失败：" + e.getMessage()));
        }
    }
    
    /**
     * 执行UTF-8编码的中文字典数据SQL脚本
     */
    @PostMapping("/execute-utf8-chinese-data")
    @Operation(summary = "执行UTF-8编码的中文字典数据SQL脚本", description = "执行insert_chinese_data_utf8.sql文件")
    public ResponseEntity<ApiResponse<String>> executeUtf8ChineseData() {
        try {
            log.info("执行UTF-8编码的中文字典数据SQL脚本");
            
            String filePath = "insert_chinese_data_utf8.sql";
            boolean success = sqlExecutor.executeSqlFile(filePath);
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("UTF-8中文字典数据执行成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("UTF-8中文字典数据执行失败"));
            }
        } catch (Exception e) {
            log.error("执行UTF-8编码的中文字典数据SQL脚本失败", e);
            return ResponseEntity.ok(ApiResponse.error("执行失败：" + e.getMessage()));
        }
    }
    
    /**
     * 通过Java代码插入中文字典数据
     */
    @PostMapping("/insert-chinese-data-java")
    @Operation(summary = "通过Java代码插入中文字典数据", description = "避免SQL文件编码问题，直接通过Java代码插入")
    @Transactional
    public ResponseEntity<ApiResponse<String>> insertChineseDataByJava() {
        try {
            log.info("通过Java代码插入中文字典数据");
            
            // 清空现有数据
            dictionaryMapper.delete(null);
            
            List<Dictionary> dictionaries = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            
            // 国家数据
            dictionaries.add(createDictionary("国家", "CN", "中国", "中华人民共和国", 0L, 1, now));
            dictionaries.add(createDictionary("国家", "US", "美国", "美利坚合众国", 0L, 2, now));
            dictionaries.add(createDictionary("国家", "RU", "俄罗斯", "俄罗斯联邦", 0L, 3, now));
            dictionaries.add(createDictionary("国家", "JP", "日本", "日本国", 0L, 4, now));
            dictionaries.add(createDictionary("国家", "KR", "韩国", "大韩民国", 0L, 5, now));
            dictionaries.add(createDictionary("国家", "IL", "以色列", "以色列国", 0L, 6, now));
            dictionaries.add(createDictionary("国家", "IR", "伊朗", "伊朗伊斯兰共和国", 0L, 7, now));
            dictionaries.add(createDictionary("国家", "LB", "黎巴嫩", "黎巴嫩共和国", 0L, 8, now));
            
            // 地区数据
            dictionaries.add(createDictionary("地区", "ASIA", "亚洲", "亚洲地区", 0L, 1, now));
            dictionaries.add(createDictionary("地区", "EUROPE", "欧洲", "欧洲地区", 0L, 2, now));
            dictionaries.add(createDictionary("地区", "NORTH_AMERICA", "北美洲", "北美洲地区", 0L, 3, now));
            dictionaries.add(createDictionary("地区", "MIDDLE_EAST", "中东", "中东地区", 0L, 4, now));
            
            // 城市数据
            dictionaries.add(createDictionary("城市", "BEIJING", "北京", "中国首都", 0L, 1, now));
            dictionaries.add(createDictionary("城市", "WASHINGTON", "华盛顿", "美国首都", 0L, 2, now));
            dictionaries.add(createDictionary("城市", "MOSCOW", "莫斯科", "俄罗斯首都", 0L, 3, now));
            dictionaries.add(createDictionary("城市", "TOKYO", "东京", "日本首都", 0L, 4, now));
            
            // 事件类型数据
            dictionaries.add(createDictionary("事件类型", "MILITARY_ATTACK", "军事攻击", "军事攻击事件", 0L, 1, now));
            dictionaries.add(createDictionary("事件类型", "POLITICAL_NEGOTIATE", "政治谈判", "政治谈判事件", 0L, 2, now));
            dictionaries.add(createDictionary("事件类型", "ECONOMIC_SANCTION", "经济制裁", "经济制裁事件", 0L, 3, now));
            dictionaries.add(createDictionary("事件类型", "TERRORISM", "恐怖主义", "恐怖主义事件", 0L, 4, now));
            
            // 事件主体数据
            dictionaries.add(createDictionary("事件主体", "GOVERNMENT", "政府", "政府机构", 0L, 1, now));
            dictionaries.add(createDictionary("事件主体", "MILITARY", "军队", "军事组织", 0L, 2, now));
            dictionaries.add(createDictionary("事件主体", "TERRORIST_GROUP", "恐怖组织", "恐怖主义组织", 0L, 3, now));
            dictionaries.add(createDictionary("事件主体", "INTERNATIONAL_ORG", "国际组织", "国际组织", 0L, 4, now));
            
            // 事件客体数据
            dictionaries.add(createDictionary("事件客体", "CIVILIAN_TARGET", "平民目标", "平民目标", 0L, 1, now));
            dictionaries.add(createDictionary("事件客体", "MILITARY_FACILITY", "军事设施", "军事设施目标", 0L, 2, now));
            dictionaries.add(createDictionary("事件客体", "INFRASTRUCTURE", "基础设施", "基础设施目标", 0L, 3, now));
            dictionaries.add(createDictionary("事件客体", "GOVERNMENT_BUILDING", "政府建筑", "政府建筑目标", 0L, 4, now));
            
            // 关联关系类型数据
            dictionaries.add(createDictionary("关联关系类型", "CAUSE", "导致", "因果关系", 0L, 1, now));
            dictionaries.add(createDictionary("关联关系类型", "TRIGGER", "触发", "触发关系", 0L, 2, now));
            dictionaries.add(createDictionary("关联关系类型", "RETALIATE", "报复", "报复关系", 0L, 3, now));
            dictionaries.add(createDictionary("关联关系类型", "RESPOND", "回应", "回应关系", 0L, 4, now));
            
            // 来源类型数据
            dictionaries.add(createDictionary("来源类型", "AUTO_FETCH", "自动获取", "自动获取数据", 0L, 1, now));
            dictionaries.add(createDictionary("来源类型", "MANUAL_INPUT", "手动输入", "手动输入数据", 0L, 2, now));
            dictionaries.add(createDictionary("来源类型", "IMPORT", "导入", "批量导入数据", 0L, 3, now));
            dictionaries.add(createDictionary("来源类型", "THIRD_PARTY", "第三方API", "第三方API数据", 0L, 4, now));
            
            // 事件状态数据
            dictionaries.add(createDictionary("事件状态", "DISABLED", "禁用", "禁用状态", 0L, 1, now));
            dictionaries.add(createDictionary("事件状态", "ENABLED", "启用", "启用状态", 0L, 2, now));
            dictionaries.add(createDictionary("事件状态", "DRAFT", "草稿", "草稿状态", 0L, 3, now));
            dictionaries.add(createDictionary("事件状态", "REVIEWING", "审核中", "审核中状态", 0L, 4, now));
            
            // 批量插入
            for (Dictionary dictionary : dictionaries) {
                dictionaryMapper.insert(dictionary);
            }
            
            log.info("通过Java代码插入中文字典数据完成，共插入{}条记录", dictionaries.size());
            return ResponseEntity.ok(ApiResponse.success("Java代码插入中文字典数据成功，共插入" + dictionaries.size() + "条记录"));
            
        } catch (Exception e) {
            log.error("通过Java代码插入中文字典数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("执行失败：" + e.getMessage()));
        }
    }
    
    /**
     * 一次性迁移组织/人物字典数据到实体表
     */
    @GetMapping("/migrate-org-person")
    @Transactional
    public ResponseEntity<ApiResponse<String>> migrateOrgPerson() {
        try {
            // 迁移组织
            List<Dictionary> orgDicts = dictionaryMapper.selectList(
                new QueryWrapper<Dictionary>().eq("dict_type", "组织").isNull("entity_id")
            );
            int orgCount = 0;
            for (Dictionary dict : orgDicts) {
                Organization org = new Organization();
                org.setName(dict.getDictName());
                org.setShortName(dict.getDictCode());
                org.setType("未知");
                org.setStatus(1);
                org.setCreatedAt(dict.getCreatedAt());
                org.setUpdatedAt(dict.getUpdatedAt());
                org.setCreatedBy(dict.getCreatedBy());
                org.setUpdatedBy(dict.getUpdatedBy());
                organizationMapper.insert(org);
                Organization saved = org;
                dict.setEntityType("organization");
                dict.setEntityId(saved.getId());
                dictionaryMapper.updateById(dict);
                orgCount++;
                log.info("组织字典[{}]已迁移为实体[{}]", dict.getDictName(), saved.getId());
            }
            // 迁移人物
            List<Dictionary> personDicts = dictionaryMapper.selectList(
                new QueryWrapper<Dictionary>().eq("dict_type", "人物").isNull("entity_id")
            );
            int personCount = 0;
            for (Dictionary dict : personDicts) {
                Person person = new Person();
                person.setName(dict.getDictName());
                person.setStatus(1);
                person.setCreatedAt(dict.getCreatedAt());
                person.setUpdatedAt(dict.getUpdatedAt());
                person.setCreatedBy(dict.getCreatedBy());
                person.setUpdatedBy(dict.getUpdatedBy());
                personMapper.insert(person);
                Person saved = person;
                dict.setEntityType("person");
                dict.setEntityId(saved.getId());
                dictionaryMapper.updateById(dict);
                personCount++;
                log.info("人物字典[{}]已迁移为实体[{}]", dict.getDictName(), saved.getId());
            }
            return ResponseEntity.ok(ApiResponse.success("迁移完成，组织：" + orgCount + "，人物：" + personCount));
        } catch (Exception e) {
            log.error("组织/人物字典迁移失败", e);
            return ResponseEntity.ok(ApiResponse.error("迁移失败：" + e.getMessage()));
        }
    }
    
    /**
     * 创建字典对象的辅助方法
     */
    private Dictionary createDictionary(String dictType, String dictCode, String dictName, String dictDescription, Long parentId, Integer sortOrder, LocalDateTime now) {
        Dictionary dictionary = new Dictionary();
        dictionary.setDictType(dictType);
        dictionary.setDictCode(dictCode);
        dictionary.setDictName(dictName);
        dictionary.setDictDescription(dictDescription);
        dictionary.setParentId(parentId);
        dictionary.setSortOrder(sortOrder);
        dictionary.setIsAutoAdded(0);
        dictionary.setStatus(1);
        dictionary.setCreatedAt(now);
        dictionary.setUpdatedAt(now);
        return dictionary;
    }
    
    /**
     * 执行自定义SQL语句
     */
    @PostMapping("/execute-sql")
    @Operation(summary = "执行自定义SQL语句", description = "执行指定的SQL语句")
    public ResponseEntity<ApiResponse<String>> executeSql(@RequestBody String sql) {
        try {
            log.info("执行自定义SQL语句: {}", sql);
            
            boolean success = sqlExecutor.executeSql(sql);
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("SQL执行成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("SQL执行失败"));
            }
        } catch (Exception e) {
            log.error("执行自定义SQL语句失败", e);
            return ResponseEntity.ok(ApiResponse.error("执行失败：" + e.getMessage()));
        }
    }
} 