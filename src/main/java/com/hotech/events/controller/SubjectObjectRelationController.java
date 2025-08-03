package com.hotech.events.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.ApiResponse;
import com.hotech.events.dto.SubjectObjectRelationDTO;
import com.hotech.events.dto.SubjectObjectRelationQueryDTO;
import com.hotech.events.service.SubjectObjectRelationService;
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
 * 主体客体关系控制器
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/subject-object-relation")
@Tag(name = "主体客体关系管理", description = "主体客体关系相关的API接口")
public class SubjectObjectRelationController {

    @Autowired
    private SubjectObjectRelationService relationService;

    /**
     * 分页查询主体客体关系列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询主体客体关系列表", description = "分页查询主体客体关系列表")
    public ResponseEntity<ApiResponse<Page<SubjectObjectRelationDTO>>> getRelationPage(SubjectObjectRelationQueryDTO queryDTO) {
        try {
            log.info("分页查询主体客体关系列表请求，查询条件：{}", queryDTO);
            
            Page<SubjectObjectRelationDTO> result = relationService.getRelationPage(queryDTO);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("分页查询主体客体关系列表失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 获取主体客体关系详情
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取主体客体关系详情", description = "根据ID获取主体客体关系详情")
    public ResponseEntity<ApiResponse<SubjectObjectRelationDTO>> getRelationDetail(
            @Parameter(description = "关系ID") @PathVariable Long id) {
        try {
            log.info("获取主体客体关系详情请求，ID：{}", id);
            
            SubjectObjectRelationDTO result = relationService.getRelationDetail(id);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("获取主体客体关系详情失败，ID：{}", id, e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 创建主体客体关系
     */
    @PostMapping("/create")
    @Operation(summary = "创建主体客体关系", description = "创建新的主体客体关系")
    public ResponseEntity<ApiResponse<SubjectObjectRelationDTO>> createRelation(
            @Valid @RequestBody SubjectObjectRelationDTO relationDTO) {
        try {
            log.info("创建主体客体关系请求，关系信息：{}", relationDTO);
            
            SubjectObjectRelationDTO result = relationService.createRelation(relationDTO);
            
            return ResponseEntity.ok(ApiResponse.success("创建成功", result));
        } catch (Exception e) {
            log.error("创建主体客体关系失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建失败：" + e.getMessage()));
        }
    }

    /**
     * 更新主体客体关系
     */
    @PutMapping("/update")
    @Operation(summary = "更新主体客体关系", description = "更新主体客体关系信息")
    public ResponseEntity<ApiResponse<SubjectObjectRelationDTO>> updateRelation(
            @Valid @RequestBody SubjectObjectRelationDTO relationDTO) {
        try {
            log.info("更新主体客体关系请求，关系信息：{}", relationDTO);
            
            SubjectObjectRelationDTO result = relationService.updateRelation(relationDTO);
            
            return ResponseEntity.ok(ApiResponse.success("更新成功", result));
        } catch (Exception e) {
            log.error("更新主体客体关系失败", e);
            return ResponseEntity.ok(ApiResponse.error("更新失败：" + e.getMessage()));
        }
    }

    /**
     * 删除主体客体关系
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除主体客体关系", description = "根据ID删除主体客体关系")
    public ResponseEntity<ApiResponse<String>> deleteRelation(
            @Parameter(description = "关系ID") @PathVariable Long id) {
        try {
            log.info("删除主体客体关系请求，ID：{}", id);
            
            Boolean result = relationService.deleteRelation(id);
            
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("删除成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("删除失败"));
            }
        } catch (Exception e) {
            log.error("删除主体客体关系失败，ID：{}", id, e);
            return ResponseEntity.ok(ApiResponse.error("删除失败：" + e.getMessage()));
        }
    }

    /**
     * 批量删除主体客体关系
     */
    @DeleteMapping("/batch-delete")
    @Operation(summary = "批量删除主体客体关系", description = "批量删除多个主体客体关系")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteRelationsBatch(
            @RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> ids = request.get("ids");
            log.info("批量删除主体客体关系请求，IDs：{}", ids);
            
            Integer successCount = relationService.deleteRelationsBatch(ids);
            
            Map<String, Object> result = Map.of(
                "total", ids.size(),
                "success", successCount,
                "failed", ids.size() - successCount
            );
            
            return ResponseEntity.ok(ApiResponse.success("批量删除完成", result));
        } catch (Exception e) {
            log.error("批量删除主体客体关系失败", e);
            return ResponseEntity.ok(ApiResponse.error("批量删除失败：" + e.getMessage()));
        }
    }

    /**
     * 根据主体编码查询关系
     */
    @GetMapping("/by-subject/{subjectCode}")
    @Operation(summary = "根据主体编码查询关系", description = "根据主体编码查询关系列表")
    public ResponseEntity<ApiResponse<List<SubjectObjectRelationDTO>>> getRelationsBySubject(
            @Parameter(description = "主体编码") @PathVariable String subjectCode) {
        try {
            log.info("根据主体编码查询关系请求，主体编码：{}", subjectCode);
            
            List<SubjectObjectRelationDTO> result = relationService.getRelationsBySubject(subjectCode);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("根据主体编码查询关系失败，主体编码：{}", subjectCode, e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 根据客体编码查询关系
     */
    @GetMapping("/by-object/{objectCode}")
    @Operation(summary = "根据客体编码查询关系", description = "根据客体编码查询关系列表")
    public ResponseEntity<ApiResponse<List<SubjectObjectRelationDTO>>> getRelationsByObject(
            @Parameter(description = "客体编码") @PathVariable String objectCode) {
        try {
            log.info("根据客体编码查询关系请求，客体编码：{}", objectCode);
            
            List<SubjectObjectRelationDTO> result = relationService.getRelationsByObject(objectCode);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("根据客体编码查询关系失败，客体编码：{}", objectCode, e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 根据关系类型查询关系
     */
    @GetMapping("/by-type/{relationType}")
    @Operation(summary = "根据关系类型查询关系", description = "根据关系类型查询关系列表")
    public ResponseEntity<ApiResponse<List<SubjectObjectRelationDTO>>> getRelationsByType(
            @Parameter(description = "关系类型") @PathVariable String relationType) {
        try {
            log.info("根据关系类型查询关系请求，关系类型：{}", relationType);
            
            List<SubjectObjectRelationDTO> result = relationService.getRelationsByType(relationType);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("根据关系类型查询关系失败，关系类型：{}", relationType, e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 根据主体和客体查询关系
     */
    @GetMapping("/by-subject-object")
    @Operation(summary = "根据主体和客体查询关系", description = "根据主体和客体查询关系列表")
    public ResponseEntity<ApiResponse<List<SubjectObjectRelationDTO>>> getRelationsBySubjectAndObject(
            @Parameter(description = "主体编码") @RequestParam String subjectCode,
            @Parameter(description = "客体编码") @RequestParam String objectCode) {
        try {
            log.info("根据主体和客体查询关系请求，主体编码：{}，客体编码：{}", subjectCode, objectCode);
            
            List<SubjectObjectRelationDTO> result = relationService.getRelationsBySubjectAndObject(subjectCode, objectCode);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("根据主体和客体查询关系失败，主体编码：{}，客体编码：{}", subjectCode, objectCode, e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 检查是否存在特定关系
     */
    @GetMapping("/exists")
    @Operation(summary = "检查是否存在特定关系", description = "检查是否存在特定关系")
    public ResponseEntity<ApiResponse<Boolean>> existsRelation(
            @Parameter(description = "主体编码") @RequestParam String subjectCode,
            @Parameter(description = "客体编码") @RequestParam String objectCode,
            @Parameter(description = "关系类型") @RequestParam String relationType) {
        try {
            log.info("检查是否存在特定关系请求，主体编码：{}，客体编码：{}，关系类型：{}", subjectCode, objectCode, relationType);
            
            Boolean result = relationService.existsRelation(subjectCode, objectCode, relationType);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", result));
        } catch (Exception e) {
            log.error("检查是否存在特定关系失败，主体编码：{}，客体编码：{}，关系类型：{}", subjectCode, objectCode, relationType, e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }
} 