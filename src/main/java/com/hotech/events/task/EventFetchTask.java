package com.hotech.events.task;

import com.hotech.events.dto.event.EventDTO;
import com.hotech.events.service.DeepSeekService;
import com.hotech.events.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 事件抓取定时任务
 * 定时从DeepSeek获取国际热点事件
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
//@Component
//@ConditionalOnProperty(prefix = "app.task.fetch", name = "enabled", havingValue = "true", matchIfMissing = false)
public class EventFetchTask {

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private EventService eventService;

    @Value("${app.task.fetch.batch-size:5}")
    private Integer batchSize;

    /**
     * 定时抓取最新事件
     * 每小时执行一次
     */
    //@Scheduled(fixedRateString = "${app.task.fetch.interval:3600000}")
    public void fetchLatestEvents() {
        log.info("开始执行定时事件抓取任务，批次大小：{}", batchSize);
        
        try {
            // 检查DeepSeek连接状态
            boolean connected = deepSeekService.checkConnection();
            if (!connected) {
                log.info("DeepSeek API不可用，使用模拟数据进行抓取");
            }
            
            // 获取最新事件（API不可用时会自动返回模拟数据）
            List<EventDTO> events = deepSeekService.fetchLatestEvents(batchSize);
            
            if (events.isEmpty()) {
                log.info("本次抓取未获取到新事件");
                return;
            }
            
            // 批量保存事件
            int successCount = 0;
            for (EventDTO event : events) {
                try {
                    // 设置事件编码
                    if (event.getEventCode() == null || event.getEventCode().isEmpty()) {
                        event.setEventCode("DEEPSEEK_" + System.currentTimeMillis() + "_" + successCount);
                    }
                    
                    // 设置创建人
                    event.setCreatedBy("deepseek_task");
                    event.setUpdatedBy("deepseek_task");
                    
                    // 保存事件
                    eventService.createEvent(event);
                    successCount++;
                    
                    log.debug("成功保存事件：{}", event.getEventDescription());
                    
                } catch (Exception e) {
                    log.error("保存事件失败：{}，错误：{}", event.getEventDescription(), e.getMessage());
                }
            }
            
            log.info("定时事件抓取任务完成，共获取{}个事件，成功保存{}个", events.size(), successCount);
            
        } catch (Exception e) {
            log.error("定时事件抓取任务执行失败", e);
        }
    }

    /**
     * 每日凌晨执行关键词搜索抓取
     * 抓取特定关键词相关的事件
     */
    //@Scheduled(cron = "0 0 2 * * ?")
    public void fetchKeywordEvents() {
        log.info("开始执行关键词事件抓取任务");
        
        try {
            // 定义关键词列表
            List<String> keywordGroups = List.of(
                "中美关系,贸易战",
                "俄乌冲突,乌克兰",
                "中东冲突,以色列,巴勒斯坦",
                "朝鲜,核武器",
                "台海,两岸关系",
                "伊朗,核协议",
                "欧盟,制裁"
            );
            
            int totalSuccess = 0;
            
            for (String keywordGroup : keywordGroups) {
                try {
                    List<String> keywords = List.of(keywordGroup.split(","));
                    List<EventDTO> events = deepSeekService.fetchEventsByKeywords(keywords, 3);
                    
                    for (EventDTO event : events) {
                        try {
                            // 设置事件编码
                            if (event.getEventCode() == null || event.getEventCode().isEmpty()) {
                                event.setEventCode("KEYWORD_" + System.currentTimeMillis() + "_" + totalSuccess);
                            }
                            
                            // 设置创建人
                            event.setCreatedBy("keyword_task");
                            event.setUpdatedBy("keyword_task");
                            
                            // 保存事件
                            eventService.createEvent(event);
                            totalSuccess++;
                            
                        } catch (Exception e) {
                            log.error("保存关键词事件失败：{}，错误：{}", event.getEventDescription(), e.getMessage());
                        }
                    }
                    
                    // 避免频繁调用API，添加延时
                    Thread.sleep(2000);
                    
                } catch (Exception e) {
                    log.error("处理关键词组失败：{}，错误：{}", keywordGroup, e.getMessage());
                }
            }
            
            log.info("关键词事件抓取任务完成，成功保存{}个事件", totalSuccess);
            
        } catch (Exception e) {
            log.error("关键词事件抓取任务执行失败", e);
        }
    }

    /**
     * 手动触发事件抓取（用于测试）
     */
    public void manualFetch() {
        log.info("手动触发事件抓取任务");
        fetchLatestEvents();
    }
} 