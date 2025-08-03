package com.hotech.events.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.ApiResponse;
import com.hotech.events.dto.EntityRelationshipDTO;
import com.hotech.events.service.EntityRelationshipService;
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
 * 实体关系控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/entity-relationship")
@Tag(name = "实体关系管理", description = "实体关系相关的API接口")
public class EntityRelationshipController {

    @Autowired(required = false)
    private EntityRelationshipService entityRelationshipService;

    /**
     * 创建实体关系
     */
    @PostMapping("/create")
    @Operation(summary = "创建实体关系", description = "创建新的实体关系")
    public ResponseEntity<ApiResponse<EntityRelationshipDTO>> createRelationship(
            @Valid @RequestBody EntityRelationshipDTO dto) {
        try {
            log.info("创建实体关系请求：{}", dto);
            
            if (entityRelationshipService == null) {
                return ResponseEntity.ok(ApiResponse.error("实体关系服务暂不可用"));
            }
            
            EntityRelationshipDTO result = entityRelationshipService.createRelationship(dto);
            
            return ResponseEntity.ok(ApiResponse.success("创建成功", result));
        } catch (Exception e) {
            log.error("创建实体关系失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建失败：" + e.getMessage()));
        }
    }

    /**
     * 更新实体关系
     */
    @PutMapping("/update")
    @Operation(summary = "更新实体关系", description = "更新实体关系信息")
    public ResponseEntity<ApiResponse<EntityRelationshipDTO>> updateRelationship(
            @Valid @RequestBody EntityRelationshipDTO dto) {
        try {
            log.info("更新实体关系请求：{}", dto);
            
            if (entityRelationshipService == null) {
                return ResponseEntity.ok(ApiResponse.error("实体关系服务暂不可用"));
            }
            
            EntityRelationshipDTO result = entityRelationshipService.updateRelationship(dto);
            
            return ResponseEntity.ok(ApiResponse.success("更新成功", result));
        } catch (Exception e) {
            log.error("更新实体关系失败", e);
            return ResponseEntity.ok(ApiResponse.error("更新失败：" + e.getMessage()));
        }
    }

    /**
     * 删除实体关系
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除实体关系", description = "根据ID删除实体关系")
    public ResponseEntity<ApiResponse<String>> deleteRelationship(
            @Parameter(description = "关系ID") @PathVariable Long id) {
        try {
            log.info("删除实体关系请求，ID：{}", id);
            
            if (entityRelationshipService == null) {
                return ResponseEntity.ok(ApiResponse.error("实体关系服务暂不可用"));
            }
            
            Boolean result = entityRelationshipService.deleteRelationship(id);
            
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("删除成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("删除失败"));
            }
        } catch (Exception e) {
            log.error("删除实体关系失败，ID：{}", id, e);
            return ResponseEntity.ok(ApiResponse.error("删除失败：" + e.getMessage()));
        }
    }

    /**
     * 获取实体关系详情
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取实体关系详情", description = "根据ID获取实体关系详情")
    public ResponseEntity<ApiResponse<EntityRelationshipDTO>> getRelationship(
            @Parameter(description = "关系ID") @PathVariable Long id) {
        try {
            log.info("获取实体关系详情请求，ID：{}", id);
            
            if (entityRelationshipService == null) {
                return ResponseEntity.ok(ApiResponse.error("实体关系服务暂不可用"));
            }
            
            EntityRelationshipDTO result = entityRelationshipService.getRelationship(id);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取实体关系详情失败，ID：{}", id, e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 分页查询实体关系
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询实体关系", description = "分页查询实体关系列表")
    public ResponseEntity<ApiResponse<Page<EntityRelationshipDTO>>> getRelationshipPage(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "关系类型") @RequestParam(required = false) String relationshipType,
            @Parameter(description = "源实体类型") @RequestParam(required = false) String sourceEntityType,
            @Parameter(description = "目标实体类型") @RequestParam(required = false) String targetEntityType) {
        try {
            log.info("分页查询实体关系请求，current：{}，size：{}，relationshipType：{}，sourceEntityType：{}，targetEntityType：{}", 
                    current, size, relationshipType, sourceEntityType, targetEntityType);
            
            if (entityRelationshipService == null) {
                return ResponseEntity.ok(ApiResponse.success("查询成功", new Page<>(current, size)));
            }
            
            Page<EntityRelationshipDTO> result = entityRelationshipService.getRelationshipPage(
                    current, size, relationshipType, sourceEntityType, targetEntityType);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("分页查询实体关系失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 根据实体查询关系
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "根据实体查询关系", description = "查询实体的所有关系")
    public ResponseEntity<ApiResponse<List<EntityRelationshipDTO>>> getRelationshipsByEntity(
            @Parameter(description = "实体类型") @PathVariable String entityType,
            @Parameter(description = "实体ID") @PathVariable Long entityId) {
        try {
            log.info("根据实体查询关系请求，entityType：{}，entityId：{}", entityType, entityId);
            
            if (entityRelationshipService == null) {
                return ResponseEntity.ok(ApiResponse.success("查询成功", List.of()));
            }
            
            List<EntityRelationshipDTO> result = entityRelationshipService.getRelationshipsByEntity(entityType, entityId);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("根据实体查询关系失败，entityType：{}，entityId：{}", entityType, entityId, e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 检查关系是否存在
     */
    @GetMapping("/exists")
    @Operation(summary = "检查关系是否存在", description = "检查两个实体之间是否存在特定关系")
    public ResponseEntity<ApiResponse<Boolean>> existsRelationship(
            @Parameter(description = "源实体类型") @RequestParam String sourceType,
            @Parameter(description = "源实体ID") @RequestParam Long sourceId,
            @Parameter(description = "目标实体类型") @RequestParam String targetType,
            @Parameter(description = "目标实体ID") @RequestParam Long targetId,
            @Parameter(description = "关系类型") @RequestParam String relationshipType) {
        try {
            log.info("检查关系是否存在请求，sourceType：{}，sourceId：{}，targetType：{}，targetId：{}，relationshipType：{}", 
                    sourceType, sourceId, targetType, targetId, relationshipType);
            
            if (entityRelationshipService == null) {
                return ResponseEntity.ok(ApiResponse.success("查询成功", false));
            }
            
            Boolean result = entityRelationshipService.existsRelationship(sourceType, sourceId, targetType, targetId, relationshipType);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("检查关系是否存在失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 获取关系图数据
     */
    @GetMapping("/graph")
    @Operation(summary = "获取关系图数据", description = "获取实体关系图数据")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRelationshipGraphData() {
        try {
            log.info("获取关系图数据请求");
            
            if (entityRelationshipService == null) {
                return ResponseEntity.ok(ApiResponse.success("查询成功", List.of()));
            }
            
            List<Map<String, Object>> result = entityRelationshipService.getRelationshipGraphData();
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取关系图数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 根据关系类型查询
     */
    @GetMapping("/type/{relationshipType}")
    @Operation(summary = "根据关系类型查询", description = "根据关系类型查询关系列表")
    public ResponseEntity<ApiResponse<List<EntityRelationshipDTO>>> getRelationshipsByType(
            @Parameter(description = "关系类型") @PathVariable String relationshipType) {
        try {
            log.info("根据关系类型查询请求，relationshipType：{}", relationshipType);
            
            if (entityRelationshipService == null) {
                return ResponseEntity.ok(ApiResponse.success("查询成功", List.of()));
            }
            
            List<EntityRelationshipDTO> result = entityRelationshipService.getRelationshipsByType(relationshipType);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("根据关系类型查询失败，relationshipType：{}", relationshipType, e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }
} 