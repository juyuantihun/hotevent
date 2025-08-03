package com.hotech.events.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.ApiResponse;
import com.hotech.events.dto.RegionCreateRequest;
import com.hotech.events.dto.RegionItemRequest;
import com.hotech.events.dto.RegionUpdateRequest;
import com.hotech.events.entity.Region;
import com.hotech.events.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 地区控制器
 * 提供地区管理功能
 */
@Slf4j
@RestController
@RequestMapping("/api/regions")
@Tag(name = "地区管理", description = "地区管理功能")
public class RegionController {

    @Autowired
    private RegionService regionService;
    
    /**
     * 创建地区
     * @param request 创建地区请求
     * @return 创建的地区
     */
    @PostMapping
    @Operation(summary = "创建地区", description = "创建新的地区")
    public ResponseEntity<ApiResponse<Region>> createRegion(
            @Validated @RequestBody RegionCreateRequest request) {
        
        log.info("创建地区请求: {}", request);
        
        try {
            // 转换请求对象
            Region region = new Region();
            BeanUtils.copyProperties(request, region);
            
            // 调用服务创建地区
            Region createdRegion = regionService.createRegion(region, request.getDictionaryIds());
            
            log.info("地区创建成功: {}", createdRegion);
            return ResponseEntity.ok(ApiResponse.success(createdRegion));
        } catch (Exception e) {
            log.error("创建地区失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建地区失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新地区
     * @param id 地区ID
     * @param request 更新地区请求
     * @return 更新后的地区
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新地区", description = "更新指定地区的信息")
    public ResponseEntity<ApiResponse<Region>> updateRegion(
            @Parameter(description = "地区ID", required = true)
            @PathVariable Long id,
            @Validated @RequestBody RegionUpdateRequest request) {
        
        log.info("更新地区请求: id={}, request={}", id, request);
        
        // 确保ID一致
        if (!id.equals(request.getId())) {
            log.warn("路径ID与请求体ID不一致: pathId={}, bodyId={}", id, request.getId());
            return ResponseEntity.ok(ApiResponse.error("路径ID与请求体ID不一致"));
        }
        
        try {
            // 转换请求对象
            Region region = new Region();
            BeanUtils.copyProperties(request, region);
            
            // 调用服务更新地区
            Region updatedRegion = regionService.updateRegion(region, request.getDictionaryIds());
            
            log.info("地区更新成功: {}", updatedRegion);
            return ResponseEntity.ok(ApiResponse.success(updatedRegion));
        } catch (Exception e) {
            log.error("更新地区失败", e);
            return ResponseEntity.ok(ApiResponse.error("更新地区失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除地区
     * @param id 地区ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除地区", description = "删除指定地区")
    public ResponseEntity<ApiResponse<Boolean>> deleteRegion(
            @Parameter(description = "地区ID", required = true)
            @PathVariable Long id) {
        
        log.info("删除地区请求: id={}", id);
        
        try {
            // 检查地区是否被引用
            if (regionService.isRegionReferenced(id)) {
                log.warn("地区 {} 被引用，无法删除", id);
                return ResponseEntity.ok(ApiResponse.error("地区被时间线引用，无法删除"));
            }
            
            // 调用服务删除地区
            boolean result = regionService.deleteRegion(id);
            
            log.info("地区删除结果: id={}, result={}", id, result);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("删除地区失败", e);
            return ResponseEntity.ok(ApiResponse.error("删除地区失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取地区详情
     * @param id 地区ID
     * @return 地区详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取地区详情", description = "获取指定地区的详细信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRegionDetail(
            @Parameter(description = "地区ID", required = true)
            @PathVariable Long id) {
        
        log.info("获取地区详情请求: id={}", id);
        
        try {
            // 调用服务获取地区详情
            Map<String, Object> detail = regionService.getRegionDetail(id);
            
            if (detail == null) {
                log.warn("地区 {} 不存在", id);
                return ResponseEntity.ok(ApiResponse.error("地区不存在"));
            }
            
            log.info("获取地区详情成功: id={}", id);
            return ResponseEntity.ok(ApiResponse.success(detail));
        } catch (Exception e) {
            log.error("获取地区详情失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取地区详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 分页查询地区列表
     * @param page 页码
     * @param size 每页大小
     * @param name 地区名称（可选）
     * @param type 地区类型（可选）
     * @return 地区分页列表
     */
    @GetMapping
    @Operation(summary = "分页查询地区列表", description = "分页查询地区列表")
    public ResponseEntity<ApiResponse<IPage<Region>>> listRegions(
            @Parameter(description = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true)
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "地区名称")
            @RequestParam(required = false) String name,
            @Parameter(description = "地区类型")
            @RequestParam(required = false) String type) {
        
        log.info("分页查询地区列表请求: page={}, size={}, name={}, type={}", page, size, name, type);
        
        try {
            // 创建分页参数
            Page<Region> pageParam = new Page<>(page, size);
            
            // 调用服务查询地区列表
            IPage<Region> result = regionService.listRegions(pageParam, name, type);
            
            log.info("分页查询地区列表成功: total={}", result.getTotal());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("分页查询地区列表失败", e);
            return ResponseEntity.ok(ApiResponse.error("分页查询地区列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取地区包含的字典项
     * @param id 地区ID
     * @return 字典项列表
     */
    @GetMapping("/{id}/items")
    @Operation(summary = "获取地区包含的字典项", description = "获取指定地区包含的字典项")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRegionItems(
            @Parameter(description = "地区ID", required = true)
            @PathVariable Long id) {
        
        log.info("获取地区包含的字典项请求: id={}", id);
        
        try {
            // 调用服务获取地区包含的字典项
            List<Map<String, Object>> items = regionService.getRegionDictionaryItems(id);
            
            log.info("获取地区包含的字典项成功: id={}, count={}", id, items.size());
            return ResponseEntity.ok(ApiResponse.success(items));
        } catch (Exception e) {
            log.error("获取地区包含的字典项失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取地区包含的字典项失败: " + e.getMessage()));
        }
    }
    
    /**
     * 添加字典项到地区
     * @param id 地区ID
     * @param request 字典项请求
     * @return 添加结果
     */
    @PostMapping("/{id}/items")
    @Operation(summary = "添加字典项到地区", description = "添加字典项到指定地区")
    public ResponseEntity<ApiResponse<Boolean>> addRegionItem(
            @Parameter(description = "地区ID", required = true)
            @PathVariable Long id,
            @Validated @RequestBody RegionItemRequest request) {
        
        log.info("添加字典项到地区请求: id={}, request={}", id, request);
        
        try {
            // 调用服务添加字典项到地区
            boolean result = regionService.addDictionaryItem(id, request.getDictionaryId());
            
            log.info("添加字典项到地区结果: id={}, dictionaryId={}, result={}", 
                    id, request.getDictionaryId(), result);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("添加字典项到地区失败", e);
            return ResponseEntity.ok(ApiResponse.error("添加字典项到地区失败: " + e.getMessage()));
        }
    }
    
    /**
     * 从地区移除字典项
     * @param id 地区ID
     * @param itemId 字典项ID
     * @return 移除结果
     */
    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "从地区移除字典项", description = "从指定地区移除字典项")
    public ResponseEntity<ApiResponse<Boolean>> removeRegionItem(
            @Parameter(description = "地区ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "字典项ID", required = true)
            @PathVariable Long itemId) {
        
        log.info("从地区移除字典项请求: id={}, itemId={}", id, itemId);
        
        try {
            // 调用服务从地区移除字典项
            boolean result = regionService.removeDictionaryItem(id, itemId);
            
            log.info("从地区移除字典项结果: id={}, itemId={}, result={}", id, itemId, result);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("从地区移除字典项失败", e);
            return ResponseEntity.ok(ApiResponse.error("从地区移除字典项失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取地区树形结构
     * @return 地区树形结构
     */
    @GetMapping("/tree")
    @Operation(summary = "获取地区树形结构", description = "获取完整的地区树形结构")
    public ResponseEntity<ApiResponse<List<Region>>> getRegionTree() {
        log.info("获取地区树形结构请求");
        
        try {
            // 调用服务获取地区树形结构
            List<Region> regionTree = regionService.getRegionTree();
            
            log.info("获取地区树形结构成功: count={}", regionTree.size());
            return ResponseEntity.ok(ApiResponse.success(regionTree));
        } catch (Exception e) {
            log.error("获取地区树形结构失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取地区树形结构失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取地区信息
     * @param id 地区ID
     * @return 地区信息
     */
    @GetMapping("/by-id/{id}")
    @Operation(summary = "根据ID获取地区信息", description = "根据ID获取地区基本信息")
    public ResponseEntity<ApiResponse<Region>> getRegionById(
            @Parameter(description = "地区ID", required = true)
            @PathVariable Long id) {
        
        log.info("根据ID获取地区信息请求: id={}", id);
        
        try {
            // 调用服务获取地区信息
            Region region = regionService.getById(id);
            
            if (region == null) {
                log.warn("地区 {} 不存在", id);
                return ResponseEntity.ok(ApiResponse.error("地区不存在"));
            }
            
            log.info("根据ID获取地区信息成功: id={}, name={}", id, region.getName());
            return ResponseEntity.ok(ApiResponse.success(region));
        } catch (Exception e) {
            log.error("根据ID获取地区信息失败", e);
            return ResponseEntity.ok(ApiResponse.error("根据ID获取地区信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取地区的子地区
     * @param id 地区ID
     * @return 子地区列表
     */
    @GetMapping("/{id}/children")
    @Operation(summary = "获取地区的子地区", description = "获取指定地区的直接子地区")
    public ResponseEntity<ApiResponse<List<Region>>> getRegionChildren(
            @Parameter(description = "地区ID", required = true)
            @PathVariable Long id) {
        
        log.info("获取地区的子地区请求: id={}", id);
        
        try {
            // 调用服务获取子地区
            List<Region> children = regionService.getRegionChildren(id);
            
            log.info("获取地区的子地区成功: id={}, count={}", id, children.size());
            return ResponseEntity.ok(ApiResponse.success(children));
        } catch (Exception e) {
            log.error("获取地区的子地区失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取地区的子地区失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取地区的祖先地区
     * @param id 地区ID
     * @return 祖先地区列表（从根到父级）
     */
    @GetMapping("/{id}/ancestors")
    @Operation(summary = "获取地区的祖先地区", description = "获取指定地区的所有祖先地区")
    public ResponseEntity<ApiResponse<List<Region>>> getRegionAncestors(
            @Parameter(description = "地区ID", required = true)
            @PathVariable Long id) {
        
        log.info("获取地区的祖先地区请求: id={}", id);
        
        try {
            // 调用服务获取祖先地区
            List<Region> ancestors = regionService.getRegionAncestors(id);
            
            log.info("获取地区的祖先地区成功: id={}, count={}", id, ancestors.size());
            return ResponseEntity.ok(ApiResponse.success(ancestors));
        } catch (Exception e) {
            log.error("获取地区的祖先地区失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取地区的祖先地区失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索地区
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索地区", description = "根据关键词搜索地区")
    public ResponseEntity<ApiResponse<IPage<Region>>> searchRegions(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.info("搜索地区请求: keyword={}, page={}, size={}", keyword, page, size);
        
        try {
            // 创建分页参数
            Page<Region> pageParam = new Page<>(page + 1, size); // 前端传0开始，后端需要1开始
            
            // 调用服务搜索地区
            IPage<Region> result = regionService.searchRegions(pageParam, keyword);
            
            log.info("搜索地区成功: keyword={}, total={}", keyword, result.getTotal());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("搜索地区失败", e);
            return ResponseEntity.ok(ApiResponse.error("搜索地区失败: " + e.getMessage()));
        }
    }
}