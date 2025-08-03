package com.hotech.events.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.ApiResponse;
import com.hotech.events.dto.DictionaryDTO;
import com.hotech.events.dto.DictionaryQueryDTO;
import com.hotech.events.dto.OrganizationDTO;
import com.hotech.events.dto.PersonDTO;
import com.hotech.events.service.DictionaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 字典控制器
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/dictionary")
@Tag(name = "字典管理", description = "字典相关的API接口")
public class DictionaryController {

    @Autowired
    private DictionaryService dictionaryService;

    /**
     * 获取字典列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取字典列表", description = "分页查询字典列表")
    public ResponseEntity<ApiResponse<Page<DictionaryDTO>>> getDictionaryList(DictionaryQueryDTO queryDTO) {
        try {
            log.info("获取字典列表请求，查询条件：{}", queryDTO);
            
            Page<DictionaryDTO> result = dictionaryService.getDictionaryList(queryDTO);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取字典列表失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 获取字典树形结构
     */
    @GetMapping("/tree")
    @Operation(summary = "获取字典树形结构", description = "获取字典的树形结构数据")
    public ResponseEntity<ApiResponse<List<DictionaryDTO>>> getDictionaryTree(
            @Parameter(description = "字典类型") @RequestParam(required = false) String dictType) {
        try {
            log.info("获取字典树形结构请求，字典类型：{}", dictType);
            
            List<DictionaryDTO> result = dictionaryService.getDictionaryTree(dictType);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取字典树形结构失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 根据类型获取字典项
     */
    @GetMapping("/type/{dictType}")
    @Operation(summary = "根据类型获取字典项", description = "根据字典类型获取字典项列表")
    public ResponseEntity<ApiResponse<List<DictionaryDTO>>> getDictionaryByType(
            @Parameter(description = "字典类型") @PathVariable String dictType) {
        try {
            log.info("根据类型获取字典项请求，字典类型：{}", dictType);
            
            List<DictionaryDTO> result = dictionaryService.getDictionaryByType(dictType);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("根据类型获取字典项失败，字典类型：{}", dictType, e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 获取字典项或关联实体详情
     * @param id 字典ID
     * @return ApiResponse
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<Object>> getDetail(@PathVariable Long id) {
        try {
            DictionaryDTO dict = dictionaryService.getDictionaryDetail(id);
            if (dict == null) {
                return ResponseEntity.ok(ApiResponse.error("字典项不存在"));
            }
            if (dict.getEntityType() != null && dict.getEntityId() != null) {
                Object detail = dictionaryService.getEntityDetail(dict.getEntityType(), dict.getEntityId());
                return ResponseEntity.ok(ApiResponse.success("查询成功", new EntityDetailWrapper(dict.getEntityType(), detail)));
            } else {
                return ResponseEntity.ok(ApiResponse.success("查询成功", new EntityDetailWrapper("dictionary", dict)));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取详情失败: " + e.getMessage()));
        }
    }

    /**
     * 实体详情包装类，便于前端区分类型
     */
    public static class EntityDetailWrapper {
        private String entityType;
        private Object data;
        public EntityDetailWrapper(String entityType, Object data) {
            this.entityType = entityType;
            this.data = data;
        }
        public String getEntityType() { return entityType; }
        public Object getData() { return data; }
    }

    /**
     * 创建字典项
     */
    @PostMapping("/create")
    @Operation(summary = "创建字典项", description = "创建新的字典项")
    public ResponseEntity<ApiResponse<DictionaryDTO>> createDictionary(
            @Valid @RequestBody DictionaryDTO dictionaryDTO) {
        try {
            log.info("创建字典项请求，字典信息：{}", dictionaryDTO);
            
            DictionaryDTO result = dictionaryService.createDictionary(dictionaryDTO);
            
            return ResponseEntity.ok(ApiResponse.success("创建成功", result));
        } catch (Exception e) {
            log.error("创建字典项失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建失败：" + e.getMessage()));
        }
    }

    /**
     * 更新字典项
     */
    @PutMapping("/update")
    @Operation(summary = "更新字典项", description = "更新字典项信息")
    public ResponseEntity<ApiResponse<DictionaryDTO>> updateDictionary(
            @Valid @RequestBody DictionaryDTO dictionaryDTO) {
        try {
            log.info("更新字典项请求，字典信息：{}", dictionaryDTO);
            
            DictionaryDTO result = dictionaryService.updateDictionary(dictionaryDTO);
            
            return ResponseEntity.ok(ApiResponse.success("更新成功", result));
        } catch (Exception e) {
            log.error("更新字典项失败", e);
            return ResponseEntity.ok(ApiResponse.error("更新失败：" + e.getMessage()));
        }
    }

    /**
     * 删除字典项
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除字典项", description = "根据ID删除字典项")
    public ResponseEntity<ApiResponse<String>> deleteDictionary(
            @Parameter(description = "字典ID") @PathVariable Long id) {
        try {
            log.info("删除字典项请求，ID：{}", id);
            
            Boolean result = dictionaryService.deleteDictionary(id);
            
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("删除成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("删除失败"));
            }
        } catch (Exception e) {
            log.error("删除字典项失败，ID：{}", id, e);
            return ResponseEntity.ok(ApiResponse.error("删除失败：" + e.getMessage()));
        }
    }

    /**
     * 批量删除字典项
     */
    @DeleteMapping("/batch-delete")
    @Operation(summary = "批量删除字典项", description = "批量删除多个字典项")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteDictionariesBatch(
            @RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> ids = request.get("ids");
            log.info("批量删除字典项请求，IDs：{}", ids);
            
            Integer successCount = dictionaryService.deleteDictionariesBatch(ids);
            
            Map<String, Object> result = Map.of(
                "total", ids.size(),
                "success", successCount,
                "failed", ids.size() - successCount
            );
            
            return ResponseEntity.ok(ApiResponse.success("批量删除完成", result));
        } catch (Exception e) {
            log.error("批量删除字典项失败", e);
            return ResponseEntity.ok(ApiResponse.error("批量删除失败：" + e.getMessage()));
        }
    }

    /**
     * 获取字典类型列表
     */
    @GetMapping("/types")
    @Operation(summary = "获取字典类型列表", description = "获取所有字典类型列表")
    public ResponseEntity<ApiResponse<List<String>>> getDictionaryTypes() {
        try {
            log.info("获取字典类型列表请求");
            
            List<String> result = dictionaryService.getDictionaryTypes();
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取字典类型列表失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }
} 