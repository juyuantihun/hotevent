package com.hotech.events.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotech.events.entity.Timeline;
import com.hotech.events.entity.TimelineCreationCache;
import com.hotech.events.entity.TimelineEvent;
import com.hotech.events.entity.TimelineRegion;
import com.hotech.events.mapper.TimelineEventMapper;
import com.hotech.events.mapper.TimelineMapper;
import com.hotech.events.mapper.TimelineRegionMapper;
import com.hotech.events.service.TimelineService;
import com.hotech.events.service.TimelineDuplicationDetectionService;
import com.hotech.events.task.TimelineGenerationTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 时间线服务实现类
 */
@Slf4j
@Service
public class TimelineServiceImpl implements TimelineService {

    private final TimelineMapper timelineMapper;
    private final TimelineRegionMapper timelineRegionMapper;
    private final TimelineEventMapper timelineEventMapper;
    private final TimelineGenerationTask timelineGenerationTask;
    private final com.hotech.events.repository.Neo4jEventRepository neo4jEventRepository;
    
    @Autowired(required = false)
    private TimelineDuplicationDetectionService duplicationDetectionService;
    
    @Autowired
    public TimelineServiceImpl(
            TimelineMapper timelineMapper,
            TimelineRegionMapper timelineRegionMapper,
            TimelineEventMapper timelineEventMapper,
            @Autowired(required = false) TimelineGenerationTask timelineGenerationTask,
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            com.hotech.events.repository.Neo4jEventRepository neo4jEventRepository) {
        this.timelineMapper = timelineMapper;
        this.timelineRegionMapper = timelineRegionMapper;
        this.timelineEventMapper = timelineEventMapper;
        this.timelineGenerationTask = timelineGenerationTask;
        this.neo4jEventRepository = neo4jEventRepository;
    }
    
    // 存储时间线生成进度的缓存
    private static final Map<Long, Map<String, Object>> GENERATION_PROGRESS_CACHE = new ConcurrentHashMap<>();
    
    @Override
    @Transactional
    public Timeline createTimeline(Timeline timeline, List<Long> regionIds) {
        log.info("创建时间线: {}, 地区: {}", timeline, regionIds);
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        timeline.setCreatedAt(now);
        timeline.setUpdatedAt(now);
        
        // 设置初始状态
        if (timeline.getStatus() == null) {
            timeline.setStatus("COMPLETED");
        }
        
        // 设置初始事件数量和关系数量
        if (timeline.getEventCount() == null) {
            timeline.setEventCount(0);
        }
        
        if (timeline.getRelationCount() == null) {
            timeline.setRelationCount(0);
        }
        
        // 保存时间线信息
        timelineMapper.insert(timeline);
        
        // 保存时间线地区关联
        if (regionIds != null && !regionIds.isEmpty()) {
            for (Long regionId : regionIds) {
                TimelineRegion item = new TimelineRegion();
                item.setTimelineId(timeline.getId());
                item.setRegionId(regionId);
                item.setCreatedAt(now);
                timelineRegionMapper.insert(item);
            }
        }
        
        return timeline;
    }
    
    @Override
    @Transactional
    public Timeline updateTimeline(Timeline timeline, List<Long> regionIds) {
        log.info("更新时间线: {}, 地区: {}", timeline, regionIds);
        
        // 设置更新时间
        timeline.setUpdatedAt(LocalDateTime.now());
        
        // 更新时间线信息
        timelineMapper.updateById(timeline);
        
        // 更新时间线地区关联
        if (regionIds != null) {
            // 获取当前时间线的地区ID列表
            List<Long> currentRegionIds = timelineRegionMapper.findRegionIdsByTimelineId(timeline.getId());
            
            // 需要添加的地区
            for (Long regionId : regionIds) {
                if (!currentRegionIds.contains(regionId)) {
                    TimelineRegion item = new TimelineRegion();
                    item.setTimelineId(timeline.getId());
                    item.setRegionId(regionId);
                    item.setCreatedAt(LocalDateTime.now());
                    timelineRegionMapper.insert(item);
                }
            }
            
            // 需要删除的地区
            for (Long regionId : currentRegionIds) {
                if (!regionIds.contains(regionId)) {
                    LambdaQueryWrapper<TimelineRegion> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(TimelineRegion::getTimelineId, timeline.getId())
                            .eq(TimelineRegion::getRegionId, regionId);
                    timelineRegionMapper.delete(wrapper);
                }
            }
        }
        
        return timeline;
    }
    
    @Override
    @Transactional
    public boolean deleteTimeline(Long id) {
        log.info("删除时间线: {}", id);
        
        // 删除时间线地区关联
        LambdaQueryWrapper<TimelineRegion> regionWrapper = new LambdaQueryWrapper<>();
        regionWrapper.eq(TimelineRegion::getTimelineId, id);
        timelineRegionMapper.delete(regionWrapper);
        
        // 删除时间线事件关联
        LambdaQueryWrapper<TimelineEvent> eventWrapper = new LambdaQueryWrapper<>();
        eventWrapper.eq(TimelineEvent::getTimelineId, id);
        timelineEventMapper.delete(eventWrapper);
        
        // 删除时间线
        int result = timelineMapper.deleteById(id);
        
        // 清除进度缓存
        GENERATION_PROGRESS_CACHE.remove(id);
        
        return result > 0;
    }
    
    @Override
    public Map<String, Object> getTimelineDetail(Long id) {
        log.info("获取时间线详情: {}", id);
        
        // 获取时间线基本信息
        Map<String, Object> timelineDetail = timelineMapper.findTimelineDetail(id);
        if (timelineDetail == null) {
            return null;
        }
        
        // 获取时间线包含的地区
        List<Map<String, Object>> regions = timelineRegionMapper.findRegionsByTimelineId(id);
        
        // 组装结果
        Map<String, Object> result = new HashMap<>();
        result.put("timeline", timelineDetail);
        result.put("regions", regions);
        
        return result;
    }
    
    @Override
    public IPage<Timeline> listTimelines(Page<Timeline> page, String name, String status, Long regionId) {
        log.info("分页查询时间线列表: page={}, name={}, status={}, regionId={}", page, name, status, regionId);
        
        // 如果指定了地区ID，则需要联表查询
        if (regionId != null) {
            // 这里需要自定义SQL查询，暂时简单实现
            List<Timeline> timelines = timelineMapper.findByRegionId(regionId);
            
            // 手动分页
            int start = (int) ((page.getCurrent() - 1) * page.getSize());
            int end = Math.min(start + (int) page.getSize(), timelines.size());
            
            List<Timeline> pageRecords = timelines.subList(start, end);
            page.setRecords(pageRecords);
            page.setTotal(timelines.size());
            
            return page;
        }
        
        // 否则直接查询时间线表
        LambdaQueryWrapper<Timeline> wrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(name)) {
            wrapper.like(Timeline::getName, name);
        }
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(Timeline::getStatus, status);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Timeline::getCreatedAt);
        
        return timelineMapper.selectPage(page, wrapper);
    }
    
    @Override
    @Transactional
    public Long generateTimelineAsync(String name, String description, List<Long> regionIds, 
                                     LocalDateTime startTime, LocalDateTime endTime) {
        log.info("异步生成时间线: name={}, regionIds={}, startTime={}, endTime={}", 
                name, regionIds, startTime, endTime);
        
        // 防重复创建检查1：检查是否已存在相同名称且状态为GENERATING的时间线
        LambdaQueryWrapper<Timeline> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Timeline::getName, name)
                   .eq(Timeline::getStatus, "GENERATING");
        
        Timeline existingTimeline = timelineMapper.selectOne(checkWrapper);
        if (existingTimeline != null) {
            log.warn("检测到重复的时间线生成请求，返回已存在的时间线: id={}, name={}", 
                    existingTimeline.getId(), name);
            return existingTimeline.getId();
        }
        
        // 防重复创建检查2：检查最近5分钟内是否已创建相同名称的时间线
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        LambdaQueryWrapper<Timeline> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.eq(Timeline::getName, name)
                    .ge(Timeline::getCreatedAt, fiveMinutesAgo);
        
        Timeline recentTimeline = timelineMapper.selectOne(recentWrapper);
        if (recentTimeline != null) {
            log.warn("检测到5分钟内的重复时间线创建请求，返回最近的时间线: id={}, name={}, createdAt={}", 
                    recentTimeline.getId(), name, recentTimeline.getCreatedAt());
            return recentTimeline.getId();
        }
        
        // 防重复创建检查3：检查相同名称、时间范围的时间线
        if (startTime != null && endTime != null) {
            LambdaQueryWrapper<Timeline> timeRangeWrapper = new LambdaQueryWrapper<>();
            timeRangeWrapper.eq(Timeline::getName, name)
                           .eq(Timeline::getStartTime, startTime)
                           .eq(Timeline::getEndTime, endTime);
            
            Timeline timeRangeTimeline = timelineMapper.selectOne(timeRangeWrapper);
            if (timeRangeTimeline != null) {
                log.warn("检测到相同名称和时间范围的时间线，返回已存在的时间线: id={}, name={}, startTime={}, endTime={}", 
                        timeRangeTimeline.getId(), name, startTime, endTime);
                return timeRangeTimeline.getId();
            }
        }
        
        // 创建时间线记录
        Timeline timeline = new Timeline();
        timeline.setName(name);
        timeline.setDescription(description);
        timeline.setStartTime(startTime);
        timeline.setEndTime(endTime);
        timeline.setStatus("GENERATING");
        timeline.setEventCount(0);
        timeline.setRelationCount(0);
        
        // 保存时间线
        timeline = createTimeline(timeline, regionIds);
        
        // 初始化进度信息
        Map<String, Object> progress = new HashMap<>();
        progress.put("id", timeline.getId());
        progress.put("status", "GENERATING");
        progress.put("percentage", 0);
        progress.put("eventCount", 0);
        progress.put("relationCount", 0);
        progress.put("currentStep", "准备生成时间线...");
        
        // 缓存进度信息
        GENERATION_PROGRESS_CACHE.put(timeline.getId(), progress);
        
        // 异步执行时间线生成任务
        startGenerationTask(timeline.getId(), regionIds, startTime, endTime);
        
        return timeline.getId();
    }
    
    @Async
    protected void startGenerationTask(Long timelineId, List<Long> regionIds, 
                                     LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 执行时间线生成任务
            if (timelineGenerationTask != null) {
                timelineGenerationTask.generateTimeline(timelineId, regionIds, startTime, endTime);
            } else {
                log.warn("TimelineGenerationTask未配置，跳过时间线生成");
                // 直接标记为完成
                completeGeneration(timelineId, 0, 0);
            }
        } catch (Exception e) {
            log.error("时间线生成任务执行失败", e);
            
            // 更新时间线状态为失败
            timelineMapper.updateStatus(timelineId, "FAILED");
            
            // 更新进度信息
            Map<String, Object> progress = GENERATION_PROGRESS_CACHE.getOrDefault(timelineId, new HashMap<>());
            progress.put("status", "FAILED");
            progress.put("errorMessage", e.getMessage());
            GENERATION_PROGRESS_CACHE.put(timelineId, progress);
        }
    }
    
    @Override
    public Map<String, Object> getGenerationProgress(Long id) {
        log.info("获取时间线生成进度: {}", id);
        
        // 从缓存中获取进度信息
        Map<String, Object> progress = GENERATION_PROGRESS_CACHE.get(id);
        
        if (progress == null) {
            // 如果缓存中没有进度信息，则查询数据库
            Timeline timeline = timelineMapper.selectById(id);
            
            if (timeline == null) {
                return null;
            }
            
            // 创建进度信息
            progress = new HashMap<>();
            progress.put("id", timeline.getId());
            progress.put("status", timeline.getStatus());
            progress.put("percentage", "COMPLETED".equals(timeline.getStatus()) ? 100 : 0);
            progress.put("eventCount", timeline.getEventCount());
            progress.put("relationCount", timeline.getRelationCount());
            
            if ("GENERATING".equals(timeline.getStatus())) {
                progress.put("currentStep", "正在生成时间线...");
            } else if ("COMPLETED".equals(timeline.getStatus())) {
                progress.put("currentStep", "时间线生成完成");
            } else if ("FAILED".equals(timeline.getStatus())) {
                progress.put("currentStep", "时间线生成失败");
                progress.put("errorMessage", "未知错误");
            }
        }
        
        return progress;
    }
    
    @Override
    public boolean cancelGeneration(Long id) {
        log.info("取消时间线生成: {}", id);
        
        // 更新时间线状态
        int result = timelineMapper.updateStatus(id, "FAILED");
        
        // 更新进度信息
        Map<String, Object> progress = GENERATION_PROGRESS_CACHE.getOrDefault(id, new HashMap<>());
        progress.put("status", "FAILED");
        progress.put("errorMessage", "用户取消生成");
        GENERATION_PROGRESS_CACHE.put(id, progress);
        
        return result > 0;
    }
    
    @Override
    public List<Map<String, Object>> getTimelineRegions(Long timelineId) {
        log.info("获取时间线包含的地区: {}", timelineId);
        return timelineRegionMapper.findRegionsByTimelineId(timelineId);
    }
    
    @Override
    public List<Map<String, Object>> getTimelineEvents(Long timelineId) {
        log.info("获取时间线包含的事件: {}", timelineId);
        return timelineEventMapper.findEventsByTimelineId(timelineId);
    }
    
    @Override
    public IPage<Map<String, Object>> getTimelineEventsWithPagination(Long timelineId, Page<Map<String, Object>> page, 
            Boolean includeDetails, String keyword, String nodeType, String sortBy, String sortOrder) {
        log.info("分页获取时间线包含的事件: timelineId={}, page={}, size={}, includeDetails={}, keyword={}, nodeType={}, sortBy={}, sortOrder={}", 
                timelineId, page.getCurrent(), page.getSize(), includeDetails, keyword, nodeType, sortBy, sortOrder);
        
        try {
            // 调用Mapper的分页查询方法（带搜索和排序）
            IPage<Map<String, Object>> result = timelineEventMapper.findEventsByTimelineIdWithPagination(
                timelineId, page, includeDetails, keyword, nodeType, sortBy, sortOrder);
            
            log.info("分页获取时间线事件成功: timelineId={}, total={}, currentPage={}, totalPages={}", 
                    timelineId, result.getTotal(), result.getCurrent(), result.getPages());
            
            return result;
        } catch (Exception e) {
            log.error("分页获取时间线事件失败: timelineId={}", timelineId, e);
            throw new RuntimeException("分页获取时间线事件失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> getTimelineGraph(Long timelineId) {
        log.info("获取时间线图形数据: {}", timelineId);
        
        try {
            // 检查Neo4j是否可用
            if (neo4jEventRepository != null) {
                // 从Neo4j中获取时间线图形数据
                return neo4jEventRepository.getTimelineGraph(timelineId);
            } else {
                log.warn("Neo4j不可用，将从MySQL中获取基本数据");
                throw new RuntimeException("Neo4j不可用");
            }
        } catch (Exception e) {
            log.error("从Neo4j获取时间线图形数据失败", e);
            
            // 如果Neo4j查询失败，则从MySQL中获取基本数据
            List<Map<String, Object>> events = timelineEventMapper.findEventsByTimelineId(timelineId);
            
            // 构建简单的图形数据
            Map<String, Object> result = new HashMap<>();
            result.put("nodes", events);
            result.put("links", new ArrayList<>());
            
            return result;
        }
    }
    
    /**
     * 更新时间线生成进度
     * @param timelineId 时间线ID
     * @param percentage 进度百分比
     * @param eventCount 事件数量
     * @param relationCount 关系数量
     * @param currentStep 当前步骤
     */
    public void updateGenerationProgress(Long timelineId, int percentage, int eventCount, 
                                        int relationCount, String currentStep) {
        log.info("更新时间线生成进度: id={}, percentage={}, eventCount={}, relationCount={}, currentStep={}",
                timelineId, percentage, eventCount, relationCount, currentStep);
        
        // 更新进度信息
        Map<String, Object> progress = GENERATION_PROGRESS_CACHE.getOrDefault(timelineId, new HashMap<>());
        progress.put("id", timelineId);
        progress.put("status", "GENERATING");
        progress.put("percentage", percentage);
        progress.put("eventCount", eventCount);
        progress.put("relationCount", relationCount);
        progress.put("currentStep", currentStep);
        
        GENERATION_PROGRESS_CACHE.put(timelineId, progress);
        
        // 更新数据库中的事件数量和关系数量
        timelineMapper.updateProgress(timelineId, eventCount, relationCount);
    }
    
    /**
     * 完成时间线生成
     * @param timelineId 时间线ID
     * @param eventCount 事件数量
     * @param relationCount 关系数量
     */
    public void completeGeneration(Long timelineId, int eventCount, int relationCount) {
        log.info("完成时间线生成: id={}, eventCount={}, relationCount={}",
                timelineId, eventCount, relationCount);
        
        // 更新时间线状态
        timelineMapper.updateStatus(timelineId, "COMPLETED");
        
        // 更新事件数量和关系数量
        timelineMapper.updateProgress(timelineId, eventCount, relationCount);
        
        // 更新缓存记录状态
        try {
            if (duplicationDetectionService != null) {
                duplicationDetectionService.updateCacheStatus(
                    null, // 这里需要通过timelineId查找cacheId
                    TimelineCreationCache.Status.COMPLETED,
                    timelineId
                );
            }
        } catch (Exception e) {
            log.warn("更新缓存记录状态失败", e);
        }
        
        // 更新进度信息
        Map<String, Object> progress = GENERATION_PROGRESS_CACHE.getOrDefault(timelineId, new HashMap<>());
        progress.put("id", timelineId);
        progress.put("status", "COMPLETED");
        progress.put("percentage", 100);
        progress.put("eventCount", eventCount);
        progress.put("relationCount", relationCount);
        progress.put("currentStep", "时间线生成完成");
        
        GENERATION_PROGRESS_CACHE.put(timelineId, progress);
    }
    
    /**
     * 失败时间线生成
     * @param timelineId 时间线ID
     * @param errorMessage 错误信息
     */
    public void failGeneration(Long timelineId, String errorMessage) {
        log.info("时间线生成失败: id={}, errorMessage={}", timelineId, errorMessage);
        
        // 更新时间线状态
        timelineMapper.updateStatus(timelineId, "FAILED");
        
        // 更新缓存记录状态
        try {
            if (duplicationDetectionService != null) {
                duplicationDetectionService.updateCacheStatus(
                    null, // 这里需要通过timelineId查找cacheId
                    TimelineCreationCache.Status.FAILED,
                    timelineId
                );
            }
        } catch (Exception e) {
            log.warn("更新缓存记录状态失败", e);
        }
        
        // 更新进度信息
        Map<String, Object> progress = GENERATION_PROGRESS_CACHE.getOrDefault(timelineId, new HashMap<>());
        progress.put("id", timelineId);
        progress.put("status", "FAILED");
        progress.put("errorMessage", errorMessage);
        
        GENERATION_PROGRESS_CACHE.put(timelineId, progress);
    }
    
    @Override
    @Transactional
    public void associateRegions(Long timelineId, List<Long> regionIds) {
        log.info("关联地区到时间线: timelineId={}, regionIds={}", timelineId, regionIds);
        
        if (regionIds == null || regionIds.isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        for (Long regionId : regionIds) {
            // 检查是否已经存在关联
            LambdaQueryWrapper<TimelineRegion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TimelineRegion::getTimelineId, timelineId)
                    .eq(TimelineRegion::getRegionId, regionId);
            
            TimelineRegion existing = timelineRegionMapper.selectOne(wrapper);
            if (existing == null) {
                TimelineRegion item = new TimelineRegion();
                item.setTimelineId(timelineId);
                item.setRegionId(regionId);
                item.setCreatedAt(now);
                timelineRegionMapper.insert(item);
            }
        }
    }
    
    @Override
    @Transactional
    public void associateEvents(Long timelineId, List<Long> eventIds) {
        log.info("关联事件到时间线: timelineId={}, eventIds={}", timelineId, eventIds);
        
        if (eventIds == null || eventIds.isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        for (Long eventId : eventIds) {
            // 检查是否已经存在关联
            LambdaQueryWrapper<TimelineEvent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TimelineEvent::getTimelineId, timelineId)
                    .eq(TimelineEvent::getEventId, eventId);
            
            TimelineEvent existing = timelineEventMapper.selectOne(wrapper);
            if (existing == null) {
                TimelineEvent item = new TimelineEvent();
                item.setTimelineId(timelineId);
                item.setEventId(eventId);
                item.setCreatedAt(now);
                timelineEventMapper.insert(item);
            }
        }
    }
    
    @Override
    @Transactional
    public boolean addEventToTimeline(Long timelineId, Long eventId) {
        log.info("添加事件到时间线: timelineId={}, eventId={}", timelineId, eventId);
        
        try {
            // 检查时间线是否存在
            Timeline timeline = timelineMapper.selectById(timelineId);
            if (timeline == null) {
                log.warn("时间线不存在: timelineId={}", timelineId);
                return false;
            }
            
            // 检查是否已经存在关联
            LambdaQueryWrapper<TimelineEvent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TimelineEvent::getTimelineId, timelineId)
                    .eq(TimelineEvent::getEventId, eventId);
            
            TimelineEvent existing = timelineEventMapper.selectOne(wrapper);
            if (existing != null) {
                log.info("事件已经关联到时间线: timelineId={}, eventId={}", timelineId, eventId);
                return true;
            }
            
            // 创建新的关联
            TimelineEvent item = new TimelineEvent();
            item.setTimelineId(timelineId);
            item.setEventId(eventId);
            item.setCreatedAt(LocalDateTime.now());
            
            int result = timelineEventMapper.insert(item);
            
            if (result > 0) {
                // 更新时间线的事件数量
                timeline.setEventCount((timeline.getEventCount() != null ? timeline.getEventCount() : 0) + 1);
                timeline.setUpdatedAt(LocalDateTime.now());
                timelineMapper.updateById(timeline);
                
                log.info("成功添加事件到时间线: timelineId={}, eventId={}", timelineId, eventId);
                return true;
            } else {
                log.warn("添加事件到时间线失败: timelineId={}, eventId={}", timelineId, eventId);
                return false;
            }
        } catch (Exception e) {
            log.error("添加事件到时间线异常: timelineId={}, eventId={}", timelineId, eventId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean removeEventFromTimeline(Long timelineId, Long eventId) {
        log.info("从时间线中移除事件: timelineId={}, eventId={}", timelineId, eventId);
        
        try {
            // 检查时间线是否存在
            Timeline timeline = timelineMapper.selectById(timelineId);
            if (timeline == null) {
                log.warn("时间线不存在: timelineId={}", timelineId);
                return false;
            }
            
            // 查找并删除关联记录
            LambdaQueryWrapper<TimelineEvent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TimelineEvent::getTimelineId, timelineId)
                    .eq(TimelineEvent::getEventId, eventId);
            
            TimelineEvent existing = timelineEventMapper.selectOne(wrapper);
            if (existing == null) {
                log.info("事件未关联到时间线: timelineId={}, eventId={}", timelineId, eventId);
                return true; // 已经不存在关联，视为成功
            }
            
            // 删除关联记录
            int result = timelineEventMapper.delete(wrapper);
            
            if (result > 0) {
                // 更新时间线的事件数量
                int currentCount = timeline.getEventCount() != null ? timeline.getEventCount() : 0;
                timeline.setEventCount(Math.max(0, currentCount - 1));
                timeline.setUpdatedAt(LocalDateTime.now());
                timelineMapper.updateById(timeline);
                
                log.info("成功从时间线中移除事件: timelineId={}, eventId={}", timelineId, eventId);
                return true;
            } else {
                log.warn("从时间线中移除事件失败: timelineId={}, eventId={}", timelineId, eventId);
                return false;
            }
        } catch (Exception e) {
            log.error("从时间线中移除事件异常: timelineId={}, eventId={}", timelineId, eventId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public Timeline save(Timeline timeline) {
        log.info("保存时间线: {}", timeline);
        
        if (timeline.getId() == null) {
            // 新增
            LocalDateTime now = LocalDateTime.now();
            timeline.setCreatedAt(now);
            timeline.setUpdatedAt(now);
            timelineMapper.insert(timeline);
        } else {
            // 更新
            timeline.setUpdatedAt(LocalDateTime.now());
            timelineMapper.updateById(timeline);
        }
        
        return timeline;
    }
    
    @Override
    public Timeline getById(Long id) {
        log.info("根据ID获取时间线: {}", id);
        return timelineMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public boolean updateById(Timeline timeline) {
        log.info("根据ID更新时间线: {}", timeline);
        
        timeline.setUpdatedAt(LocalDateTime.now());
        int result = timelineMapper.updateById(timeline);
        return result > 0;
    }
    
    @Override
    public IPage<Map<String, Object>> getAvailableEvents(Long timelineId, Page<Map<String, Object>> page,
            String eventType, String subject, String object, Integer sourceType, 
            LocalDateTime startTime, LocalDateTime endTime) {
        
        log.info("获取未关联到时间线的事件: timelineId={}, page={}, size={}, eventType={}, subject={}, object={}, sourceType={}, startTime={}, endTime={}",
                timelineId, page.getCurrent(), page.getSize(), eventType, subject, object, sourceType, startTime, endTime);
        
        // 添加详细的参数调试信息
        log.info("Service层参数调试: eventType=[{}], subject=[{}], object=[{}], sourceType=[{}], startTime=[{}], endTime=[{}]",
                eventType, subject, object, sourceType, startTime, endTime);
        log.info("Service层参数是否为空: eventType={}, subject={}, object={}, sourceType={}, startTime={}, endTime={}",
                eventType == null || eventType.trim().isEmpty(),
                subject == null || subject.trim().isEmpty(),
                object == null || object.trim().isEmpty(),
                sourceType == null,
                startTime == null,
                endTime == null);
        
        try {
            // 先检查时间线是否存在
            Timeline timeline = timelineMapper.selectById(timelineId);
            if (timeline == null) {
                log.warn("时间线不存在: timelineId={}", timelineId);
                throw new RuntimeException("时间线不存在: " + timelineId);
            }
            
            // 添加调试信息
            int totalEvents = timelineEventMapper.countAllEvents();
            int associatedEvents = timelineEventMapper.countAssociatedEvents(timelineId);
            log.info("调试信息 - 总事件数: {}, 已关联事件数: {}, 预期未关联事件数: {}", 
                    totalEvents, associatedEvents, totalEvents - associatedEvents);
            
            // 调用mapper方法获取未关联的事件
            IPage<Map<String, Object>> result = timelineEventMapper.selectAvailableEvents(
                    page, timelineId, eventType, subject, object, sourceType, startTime, endTime);
            
            log.info("获取未关联到时间线的事件成功: timelineId={}, total={}, records={}", 
                    timelineId, result.getTotal(), result.getRecords().size());
            
            // 打印前几条记录用于调试
            if (result.getRecords().size() > 0) {
                log.debug("第一条记录: {}", result.getRecords().get(0));
            } else {
                log.warn("没有找到未关联的事件，可能的原因：1.所有事件都已关联 2.SQL查询有问题 3.数据库连接问题");
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取未关联到时间线的事件失败: timelineId={}", timelineId, e);
            throw new RuntimeException("获取未关联到时间线的事件失败", e);
        }
    }
    
    @Override
    public int countAllEvents() {
        return timelineEventMapper.countAllEvents();
    }
    
    @Override
    public int countAssociatedEvents(Long timelineId) {
        return timelineEventMapper.countAssociatedEvents(timelineId);
    }
    
    @Override
    public IPage<Map<String, Object>> debugAllEvents(Long timelineId, Page<Map<String, Object>> page,
            String eventType, String subject, String object, Integer sourceType, 
            LocalDateTime startTime, LocalDateTime endTime) {
        
        log.info("调试获取所有事件: timelineId={}, page={}, size={}, eventType={}, subject={}, object={}, sourceType={}, startTime={}, endTime={}",
                timelineId, page.getCurrent(), page.getSize(), eventType, subject, object, sourceType, startTime, endTime);
        
        try {
            // 先检查时间线是否存在
            Timeline timeline = timelineMapper.selectById(timelineId);
            if (timeline == null) {
                log.warn("时间线不存在: timelineId={}", timelineId);
                throw new RuntimeException("时间线不存在: " + timelineId);
            }
            
            // 调用mapper方法获取所有事件（包含关联状态）
            IPage<Map<String, Object>> result = timelineEventMapper.selectAllEventsForDebug(
                    page, timelineId, eventType, subject, object, sourceType, startTime, endTime);
            
            log.info("调试获取所有事件成功: timelineId={}, total={}, records={}", 
                    timelineId, result.getTotal(), result.getRecords().size());
            
            return result;
        } catch (Exception e) {
            log.error("调试获取所有事件失败: timelineId={}", timelineId, e);
            throw new RuntimeException("调试获取所有事件失败: " + e.getMessage(), e);
        }
    }
}