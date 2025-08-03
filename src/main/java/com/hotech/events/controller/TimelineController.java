package com.hotech.events.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.dto.ApiResponse;
import com.hotech.events.dto.TimelineGenerateRequest;
import com.hotech.events.entity.Timeline;
import com.hotech.events.entity.TimelineCreationCache;
import com.hotech.events.service.TimelineService;
import com.hotech.events.service.TimelineDuplicationDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 时间线控制器
 * 提供时间线生成和管理功能
 */
@Slf4j
@RestController
@RequestMapping("/api/timelines")
@Tag(name = "时间线管理", description = "时间线生成和管理功能")
public class TimelineController {

    @Autowired
    private TimelineService timelineService;

    @Autowired(required = false)
    private TimelineDuplicationDetectionService duplicationDetectionService;

    /**
     * 分页查询时间线列表
     * 
     * @param page     页码
     * @param size     每页大小
     * @param name     时间线名称（可选）
     * @param status   时间线状态（可选）
     * @param regionId 地区ID（可选）
     * @return 时间线分页列表
     */
    @GetMapping
    @Operation(summary = "分页查询时间线列表", description = "分页查询时间线列表")
    public ResponseEntity<ApiResponse<IPage<Timeline>>> listTimelines(
            @Parameter(description = "页码", required = true) @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = true) @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "时间线名称") @RequestParam(required = false) String name,
            @Parameter(description = "时间线状态") @RequestParam(required = false) String status,
            @Parameter(description = "地区ID") @RequestParam(required = false) Long regionId) {

        log.info("分页查询时间线列表: page={}, size={}, name={}, status={}, regionId={}",
                page, size, name, status, regionId);

        try {
            // 创建分页参数
            Page<Timeline> pageParam = new Page<>(page, size);

            // 调用服务查询时间线列表
            IPage<Timeline> result = timelineService.listTimelines(pageParam, name, status, regionId);

            log.info("分页查询时间线列表成功: total={}", result.getTotal());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("分页查询时间线列表失败", e);
            return ResponseEntity.ok(ApiResponse.error("分页查询时间线列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取时间线详情
     * 
     * @param id 时间线ID
     * @return 时间线详情
     */
    @GetMapping("/{id}")
    @Operation(summary = " ", description = "获取指定时间线的详细信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTimelineDetail(
            @Parameter(description = "时间线ID", required = true) @PathVariable Long id) {

        log.info("获取时间线详情: id={}", id);

        try {
            // 调用服务获取时间线详情
            Map<String, Object> detail = timelineService.getTimelineDetail(id);

            if (detail == null) {
                log.warn("时间线 {} 不存在", id);
                return ResponseEntity.ok(ApiResponse.error("时间线不存在"));
            }

            log.info("获取时间线详情成功: id={}", id);
            return ResponseEntity.ok(ApiResponse.success(detail));
        } catch (Exception e) {
            log.error("获取时间线详情失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取时间线详情失败: " + e.getMessage()));
        }
    }

    /**
     * 从时间线中移除事件
     * 
     * @param timelineId 时间线ID
     * @param eventId 事件ID
     * @return 移除结果
     */
    @DeleteMapping("/{timelineId}/events/{eventId}")
    @Operation(summary = "从时间线中移除事件", description = "移除事件与时间线的关联关系，不删除事件本身")
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeEventFromTimeline(
            @Parameter(description = "时间线ID", required = true) @PathVariable Long timelineId,
            @Parameter(description = "事件ID", required = true) @PathVariable Long eventId) {

        log.info("从时间线中移除事件: timelineId={}, eventId={}", timelineId, eventId);

        try {
            // 调用服务移除事件与时间线的关联
            boolean result = timelineService.removeEventFromTimeline(timelineId, eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("timelineId", timelineId);
            response.put("eventId", eventId);
            response.put("message", result ? "事件已从时间线中移除" : "移除事件失败");

            log.info("移除事件结果: timelineId={}, eventId={}, result={}", timelineId, eventId, result);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("移除事件失败: timelineId={}, eventId={}", timelineId, eventId, e);
            return ResponseEntity.ok(ApiResponse.error("移除事件失败: " + e.getMessage()));
        }
    }

    /**
     * 删除时间线
     * 
     * @param id 时间线ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除时间线", description = "删除指定时间线")
    public ResponseEntity<ApiResponse<Boolean>> deleteTimeline(
            @Parameter(description = "时间线ID", required = true) @PathVariable Long id) {

        log.info("删除时间线: id={}", id);

        try {
            // 调用服务删除时间线
            boolean result = timelineService.deleteTimeline(id);

            log.info("删除时间线结果: id={}, result={}", id, result);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("删除时间线失败", e);
            return ResponseEntity.ok(ApiResponse.error("删除时间线失败: " + e.getMessage()));
        }
    }

    /**
     * 获取时间线包含的地区
     * 
     * @param id 时间线ID
     * @return 地区列表
     */
    @GetMapping("/{id}/regions")
    @Operation(summary = "获取时间线包含的地区", description = "获取指定时间线包含的地区")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTimelineRegions(
            @Parameter(description = "时间线ID", required = true) @PathVariable Long id) {

        log.info("获取时间线包含的地区: id={}", id);

        try {
            // 调用服务获取时间线包含的地区
            List<Map<String, Object>> regions = timelineService.getTimelineRegions(id);

            log.info("获取时间线包含的地区成功: id={}, count={}", id, regions.size());
            return ResponseEntity.ok(ApiResponse.success(regions));
        } catch (Exception e) {
            log.error("获取时间线包含的地区失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取时间线包含的地区失败: " + e.getMessage()));
        }
    }

    /**
     * 获取时间线包含的事件（支持分页和搜索）
     * 
     * @param id 时间线ID
     * @param page 页码（可选，默认为1）
     * @param pageSize 每页大小（可选，默认为50）
     * @param includeDetails 是否包含详细信息（可选，默认为false）
     * @param keyword 搜索关键词（可选）
     * @param nodeType 事件类型（可选）
     * @param sortBy 排序字段（可选，默认为eventTime）
     * @param sortOrder 排序方向（可选，默认为asc）
     * @return 事件列表
     */
    @GetMapping("/{id}/events")
    @Operation(summary = "获取时间线包含的事件", description = "获取指定时间线包含的事件，支持分页和搜索")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTimelineEvents(
            @Parameter(description = "时间线ID", required = true) @PathVariable Long id,
            @Parameter(description = "页码", required = false) @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", required = false) @RequestParam(defaultValue = "50") Integer pageSize,
            @Parameter(description = "是否包含详细信息", required = false) @RequestParam(defaultValue = "false") Boolean includeDetails,
            @Parameter(description = "搜索关键词", required = false) @RequestParam(required = false) String keyword,
            @Parameter(description = "事件类型", required = false) @RequestParam(required = false) String nodeType,
            @Parameter(description = "排序字段", required = false) @RequestParam(defaultValue = "eventTime") String sortBy,
            @Parameter(description = "排序方向", required = false) @RequestParam(defaultValue = "asc") String sortOrder) {

        log.info("获取时间线包含的事件: id={}, page={}, pageSize={}, includeDetails={}, keyword={}, nodeType={}, sortBy={}, sortOrder={}", 
                id, page, pageSize, includeDetails, keyword, nodeType, sortBy, sortOrder);

        try {
            // 创建分页参数
            Page<Map<String, Object>> pageParam = new Page<>(page, pageSize);
            
            // 调用服务获取时间线包含的事件（分页和搜索）
            IPage<Map<String, Object>> eventsPage = timelineService.getTimelineEventsWithPagination(
                id, pageParam, includeDetails, keyword, nodeType, sortBy, sortOrder);

            // 构建响应数据
            Map<String, Object> result = new HashMap<>();
            result.put("events", eventsPage.getRecords());
            result.put("total", eventsPage.getTotal());
            result.put("totalPages", eventsPage.getPages());
            result.put("currentPage", eventsPage.getCurrent());
            result.put("pageSize", eventsPage.getSize());
            result.put("hasNext", eventsPage.getCurrent() < eventsPage.getPages());
            result.put("hasPrevious", eventsPage.getCurrent() > 1);

            log.info("获取时间线包含的事件成功: id={}, total={}, currentPage={}, totalPages={}", 
                    id, eventsPage.getTotal(), eventsPage.getCurrent(), eventsPage.getPages());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("获取时间线包含的事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取时间线包含的事件失败: " + e.getMessage()));
        }
    }

    /**
     * 获取时间线包含的所有事件（不分页，保持向后兼容）
     * 
     * @param id 时间线ID
     * @return 事件列表
     */
    @GetMapping("/{id}/events/all")
    @Operation(summary = "获取时间线包含的所有事件", description = "获取指定时间线包含的所有事件（不分页）")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllTimelineEvents(
            @Parameter(description = "时间线ID", required = true) @PathVariable Long id) {

        log.info("获取时间线包含的所有事件: id={}", id);

        try {
            // 调用服务获取时间线包含的所有事件
            List<Map<String, Object>> events = timelineService.getTimelineEvents(id);

            log.info("获取时间线包含的所有事件成功: id={}, count={}", id, events.size());
            return ResponseEntity.ok(ApiResponse.success(events));
        } catch (Exception e) {
            log.error("获取时间线包含的所有事件失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取时间线包含的所有事件失败: " + e.getMessage()));
        }
    }

    /**
     * 获取时间线图形数据
     * 
     * @param id 时间线ID
     * @return 图形数据
     */
    @GetMapping("/{id}/graph")
    @Operation(summary = "获取时间线图形数据", description = "获取指定时间线的图形数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTimelineGraph(
            @Parameter(description = "时间线ID", required = true) @PathVariable Long id) {

        log.info("获取时间线图形数据: id={}", id);

        try {
            // 调用服务获取时间线图形数据
            Map<String, Object> graph = timelineService.getTimelineGraph(id);

            log.info("获取时间线图形数据成功: id={}", id);
            return ResponseEntity.ok(ApiResponse.success(graph));
        } catch (Exception e) {
            log.error("获取时间线图形数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取时间线图形数据失败: " + e.getMessage()));
        }
    }

    /**
     * 异步生成时间线
     * 
     * @param request 生成时间线请求
     * @return 生成任务信息
     */
    @PostMapping("/generate/async")
    @Operation(summary = "异步生成时间线", description = "异步生成时间线")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateTimelineAsync(
            @Validated @RequestBody TimelineGenerateRequest request) {

        log.info("接收到异步生成时间线请求: {}", request);

        try {
            // 获取用户ID（这里简化处理，实际应该从认证上下文获取）
            String userId = "default_user"; // TODO: 从认证上下文获取真实用户ID

            // 执行重复检测
            TimelineDuplicationDetectionService.DuplicationCheckResult checkResult = null;
            if (duplicationDetectionService != null) {
                checkResult = duplicationDetectionService.checkDuplication(
                        request.getName(),
                        request.getDescription(),
                        request.getRegionIds(),
                        request.getStartTime(),
                        request.getEndTime(),
                        userId);
            }

            // 如果发现重复，返回已存在的时间线
            if (checkResult != null && checkResult.isDuplicate()) {
                Timeline existingTimeline = checkResult.getExistingTimeline();

                log.info("检测到重复的时间线创建请求: reason={}, existingTimelineId={}",
                        checkResult.getReason(), existingTimeline.getId());

                // 创建响应，返回已存在的时间线信息
                Map<String, Object> result = new HashMap<>();
                result.put("id", existingTimeline.getId());
                result.put("name", existingTimeline.getName());
                result.put("description", existingTimeline.getDescription());
                result.put("regionIds", request.getRegionIds());
                result.put("startTime", existingTimeline.getStartTime());
                result.put("endTime", existingTimeline.getEndTime());
                result.put("status", existingTimeline.getStatus());
                result.put("isDuplicate", true);
                result.put("duplicateReason", checkResult.getReason());
                result.put("message", "检测到重复请求，返回已存在的时间线: " + checkResult.getReason());

                log.info("返回已存在的时间线: {}", result);
                return ResponseEntity.ok(ApiResponse.success(result));
            }

            // 没有重复，继续创建新的时间线
            Long timelineId = timelineService.generateTimelineAsync(
                    request.getName(),
                    request.getDescription(),
                    request.getRegionIds(),
                    request.getStartTime(),
                    request.getEndTime());

            // 更新缓存记录状态
            if (checkResult != null && checkResult.getCacheRecord() != null && duplicationDetectionService != null) {
                duplicationDetectionService.updateCacheStatus(
                        checkResult.getCacheRecord().getId(),
                        TimelineCreationCache.Status.CREATING,
                        timelineId);
            }

            // 创建响应
            Map<String, Object> result = new HashMap<>();
            result.put("id", timelineId);
            result.put("name", request.getName());
            result.put("description", request.getDescription());
            result.put("regionIds", request.getRegionIds());
            result.put("startTime", request.getStartTime());
            result.put("endTime", request.getEndTime());
            result.put("status", "GENERATING");
            result.put("isDuplicate", false);
            result.put("message", "时间线生成任务已提交，正在处理中...");

            log.info("成功创建时间线生成任务: {}", result);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("生成时间线失败", e);
            return ResponseEntity.ok(ApiResponse.error("生成时间线失败: " + e.getMessage()));
        }
    }

    /**
     * 根据地区异步生成时间线
     * 
     * @param regionId    地区ID
     * @param name        时间线名称
     * @param description 时间线描述（可选）
     * @return 生成任务信息
     */
    @PostMapping("/generate/region/{regionId}/async")
    @Operation(summary = "根据地区异步生成时间线", description = "根据指定地区异步生成时间线")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateTimelineByRegionAsync(
            @Parameter(description = "地区ID", required = true) @PathVariable Long regionId,
            @Parameter(description = "时间线名称", required = true) @RequestParam String name,
            @Parameter(description = "时间线描述") @RequestParam(required = false) String description) {

        log.info("接收到根据地区异步生成时间线请求: 地区ID={}, 名称={}, 描述={}", regionId, name, description);

        try {
            // 获取用户ID（这里简化处理，实际应该从认证上下文获取）
            String userId = "default_user"; // TODO: 从认证上下文获取真实用户ID

            LocalDateTime startTime = LocalDateTime.now().minusMonths(1);
            LocalDateTime endTime = LocalDateTime.now();
            List<Long> regionIds = Arrays.asList(regionId);

            // 执行重复检测
            TimelineDuplicationDetectionService.DuplicationCheckResult checkResult = null;
            if (duplicationDetectionService != null) {
                checkResult = duplicationDetectionService.checkDuplication(
                        name,
                        description,
                        regionIds,
                        startTime,
                        endTime,
                        userId);
            }

            // 如果发现重复，返回已存在的时间线
            if (checkResult != null && checkResult.isDuplicate()) {
                Timeline existingTimeline = checkResult.getExistingTimeline();

                log.info("检测到重复的时间线创建请求: reason={}, existingTimelineId={}",
                        checkResult.getReason(), existingTimeline.getId());

                // 创建响应，返回已存在的时间线信息
                Map<String, Object> result = new HashMap<>();
                result.put("id", existingTimeline.getId());
                result.put("regionId", regionId);
                result.put("name", existingTimeline.getName());
                result.put("description", existingTimeline.getDescription());
                result.put("status", existingTimeline.getStatus());
                result.put("isDuplicate", true);
                result.put("duplicateReason", checkResult.getReason());
                result.put("message", "检测到重复请求，返回已存在的时间线: " + checkResult.getReason());

                log.info("返回已存在的时间线: {}", result);
                return ResponseEntity.ok(ApiResponse.success(result));
            }

            // 没有重复，继续创建新的时间线
            Long timelineId = timelineService.generateTimelineAsync(
                    name,
                    description,
                    regionIds,
                    startTime,
                    endTime);

            // 更新缓存记录状态
            if (checkResult != null && checkResult.getCacheRecord() != null && duplicationDetectionService != null) {
                duplicationDetectionService.updateCacheStatus(
                        checkResult.getCacheRecord().getId(),
                        TimelineCreationCache.Status.CREATING,
                        timelineId);
            }

            // 创建响应
            Map<String, Object> result = new HashMap<>();
            result.put("id", timelineId);
            result.put("regionId", regionId);
            result.put("name", name);
            result.put("description", description);
            result.put("status", "GENERATING");
            result.put("isDuplicate", false);
            result.put("message", "时间线生成任务已提交，正在处理中...");

            log.info("成功创建时间线生成任务: {}", result);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("生成时间线失败", e);
            return ResponseEntity.ok(ApiResponse.error("生成时间线失败: " + e.getMessage()));
        }
    }

    /**
     * 获取时间线生成进度
     * 
     * @param id 时间线ID
     * @return 生成进度信息
     */
    @GetMapping("/{id}/generation-progress")
    @Operation(summary = "获取时间线生成进度", description = "获取指定时间线的生成进度")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGenerationProgress(
            @Parameter(description = "时间线ID", required = true) @PathVariable Long id) {

        log.info("获取时间线生成进度: id={}", id);

        try {
            // 调用服务获取时间线生成进度
            Map<String, Object> progress = timelineService.getGenerationProgress(id);

            if (progress == null) {
                log.warn("时间线 {} 不存在或未在生成中", id);

                // 检查时间线是否存在
                Map<String, Object> timelineDetail = timelineService.getTimelineDetail(id);
                if (timelineDetail == null) {
                    return ResponseEntity.ok(ApiResponse.error("时间线不存在"));
                }

                // 时间线存在但没有生成进度，返回默认状态
                Map<String, Object> defaultProgress = new HashMap<>();
                defaultProgress.put("progress", 100);
                defaultProgress.put("status", "COMPLETED");
                defaultProgress.put("currentStep", "已完成");
                defaultProgress.put("message", "时间线已生成完成");
                defaultProgress.put("eventCount", timelineDetail.get("eventCount"));
                defaultProgress.put("relationCount", timelineDetail.get("relationCount"));

                return ResponseEntity.ok(ApiResponse.success(defaultProgress));
            }

            log.debug("返回时间线生成进度: {}", progress);
            return ResponseEntity.ok(ApiResponse.success(progress));
        } catch (Exception e) {
            log.error("获取时间线生成进度失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取时间线生成进度失败: " + e.getMessage()));
        }
    }

    /**
     * 取消时间线生成
     * 
     * @param id 时间线ID
     * @return 取消结果
     */
    @PostMapping("/{id}/cancel-generation")
    @Operation(summary = "取消时间线生成", description = "取消指定时间线的生成任务")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelGeneration(
            @Parameter(description = "时间线ID", required = true) @PathVariable Long id) {

        log.info("取消时间线生成: id={}", id);

        try {
            // 调用服务取消时间线生成
            boolean result = timelineService.cancelGeneration(id);

            Map<String, Object> response = new HashMap<>();
            response.put("cancelled", result);
            response.put("timelineId", id);
            response.put("message", result ? "时间线生成已取消" : "取消失败或任务已完成");

            log.info("时间线生成取消结果: id={}, result={}", id, result);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("取消时间线生成失败", e);
            return ResponseEntity.ok(ApiResponse.error("取消时间线生成失败: " + e.getMessage()));
        }
    }

    /**
     * 添加事件到时间线
     * 
     * @param timelineId 时间线ID
     * @param eventId 事件ID
     * @return 添加结果
     */
    @PostMapping("/{timelineId}/events/{eventId}")
    @Operation(summary = "添加事件到时间线", description = "将指定事件添加到时间线中")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addEventToTimeline(
            @Parameter(description = "时间线ID", required = true) @PathVariable Long timelineId,
            @Parameter(description = "事件ID", required = true) @PathVariable Long eventId) {

        log.info("添加事件到时间线: timelineId={}, eventId={}", timelineId, eventId);

        try {
            // 调用服务添加事件到时间线
            boolean result = timelineService.addEventToTimeline(timelineId, eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("timelineId", timelineId);
            response.put("eventId", eventId);
            response.put("message", result ? "事件已成功添加到时间线" : "添加事件到时间线失败");

            log.info("添加事件到时间线结果: timelineId={}, eventId={}, result={}", timelineId, eventId, result);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("添加事件到时间线失败: timelineId={}, eventId={}", timelineId, eventId, e);
            return ResponseEntity.ok(ApiResponse.error("添加事件到时间线失败: " + e.getMessage()));
        }
    }

    /**
     * 创建时间线
     * 
     * @param requestBody 请求体，包含时间线信息和地区ID列表
     * @return 创建后的时间线
     */
    @PostMapping
    @Operation(summary = "创建时间线", description = "创建新的时间线")
    public ResponseEntity<ApiResponse<Timeline>> createTimeline(@RequestBody Map<String, Object> requestBody) {
        log.info("创建时间线请求: {}", requestBody);

        try {
            // 从请求体中提取时间线信息
            Timeline timeline = new Timeline();
            if (requestBody.containsKey("name")) {
                timeline.setName((String) requestBody.get("name"));
            }
            if (requestBody.containsKey("description")) {
                timeline.setDescription((String) requestBody.get("description"));
            }
            if (requestBody.containsKey("status")) {
                timeline.setStatus((String) requestBody.get("status"));
            }

            // 设置时间范围
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            if (requestBody.containsKey("startTime")) {
                Object startTimeObj = requestBody.get("startTime");
                if (startTimeObj instanceof String) {
                    try {
                        // 尝试使用自定义格式解析
                        timeline.setStartTime(LocalDateTime.parse((String) startTimeObj, formatter));
                    } catch (DateTimeParseException e) {
                        try {
                            // 如果失败，尝试使用ISO格式解析
                            timeline.setStartTime(LocalDateTime.parse((String) startTimeObj));
                        } catch (DateTimeParseException e2) {
                            log.warn("无法解析开始时间: {}", startTimeObj, e2);
                        }
                    }
                }
            }

            if (requestBody.containsKey("endTime")) {
                Object endTimeObj = requestBody.get("endTime");
                if (endTimeObj instanceof String) {
                    try {
                        // 尝试使用自定义格式解析
                        timeline.setEndTime(LocalDateTime.parse((String) endTimeObj, formatter));
                    } catch (DateTimeParseException e) {
                        try {
                            // 如果失败，尝试使用ISO格式解析
                            timeline.setEndTime(LocalDateTime.parse((String) endTimeObj));
                        } catch (DateTimeParseException e2) {
                            log.warn("无法解析结束时间: {}", endTimeObj, e2);
                        }
                    }
                }
            }

            // 提取地区ID列表
            List<Long> regionIds = new ArrayList<>();
            if (requestBody.containsKey("regionIds") && requestBody.get("regionIds") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> regionIdObjects = (List<Object>) requestBody.get("regionIds");
                for (Object regionIdObj : regionIdObjects) {
                    if (regionIdObj instanceof Number) {
                        regionIds.add(((Number) regionIdObj).longValue());
                    } else if (regionIdObj instanceof String) {
                        try {
                            regionIds.add(Long.parseLong((String) regionIdObj));
                        } catch (NumberFormatException e) {
                            log.warn("无法解析地区ID: {}", regionIdObj);
                        }
                    }
                }
            }

            // 调用服务创建时间线
            Timeline createdTimeline = timelineService.createTimeline(timeline, regionIds);

            log.info("成功创建时间线: id={}", createdTimeline.getId());
            return ResponseEntity.ok(ApiResponse.success(createdTimeline));
        } catch (Exception e) {
            log.error("创建时间线失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建时间线失败: " + e.getMessage()));
        }
    }
}