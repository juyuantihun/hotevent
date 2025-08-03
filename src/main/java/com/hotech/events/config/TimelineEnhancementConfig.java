package com.hotech.events.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 时间线增强功能配置类
 * 负责管理时间段分割、地理信息处理、API调用等相关配置
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "timeline.enhancement")
public class TimelineEnhancementConfig {
    
    /**
     * 时间段分割配置
     */
    private SegmentationConfig segmentation = new SegmentationConfig();
    
    /**
     * 地理信息处理配置
     */
    private GeographicConfig geographic = new GeographicConfig();
    
    /**
     * API调用增强配置
     */
    private ApiConfig api = new ApiConfig();
    
    /**
     * 性能监控配置
     */
    private MonitoringConfig monitoring = new MonitoringConfig();
    
    /**
     * 缓存配置
     */
    private CacheConfig cache = new CacheConfig();
    
    /**
     * 前端UI配置
     */
    private FrontendConfig frontend = new FrontendConfig();
    
    /**
     * 数据验证配置
     */
    private ValidationConfig validation = new ValidationConfig();
    
    /**
     * 错误处理配置
     */
    private ErrorHandlingConfig errorHandling = new ErrorHandlingConfig();
    
    /**
     * 时间段分割配置
     */
    @Data
    public static class SegmentationConfig {
        /**
         * 最大时间跨度（天）
         */
        private int maxSpanDays = 7;
        
        /**
         * 每个时间段最小事件数量
         */
        private int minEventsPerSegment = 5;
        
        /**
         * 最大分割段数
         */
        private int maxSegments = 10;
        
        /**
         * 是否启用并行处理
         */
        private boolean parallelProcessing = true;
        
        /**
         * 智能分割阈值
         */
        private int intelligentThreshold = 15;
        
        /**
         * 每天预期事件数量
         */
        private int expectedEventsPerDay = 3;
        
        /**
         * 每个时间段最大事件数量
         */
        private int maxEventsPerSegment = 20;
        
        /**
         * 分割算法类型：FIXED_DAYS, INTELLIGENT, ADAPTIVE
         */
        private String algorithmType = "INTELLIGENT";
    }
    
    /**
     * 地理信息处理配置
     */
    @Data
    public static class GeographicConfig {
        /**
         * 坐标缓存TTL（秒）
         */
        private int coordinateCacheTtl = 3600;
        
        /**
         * 默认坐标配置
         */
        private DefaultCoordinatesConfig defaultCoordinates = new DefaultCoordinatesConfig();
        
        /**
         * 坐标解析配置
         */
        private ParsingConfig parsing = new ParsingConfig();
        
        /**
         * 缓存配置
         */
        private GeographicCacheConfig cache = new GeographicCacheConfig();
        
        @Data
        public static class DefaultCoordinatesConfig {
            /**
             * 是否启用默认坐标
             */
            private boolean enabled = true;
            
            /**
             * 国家级别回退到首都
             */
            private boolean fallbackToCapital = true;
            
            /**
             * 地区级别回退到首府
             */
            private boolean fallbackToRegionCenter = true;
            
            /**
             * 城市级别回退到地区中心
             */
            private boolean fallbackToCityCenter = true;
        }
        
        @Data
        public static class ParsingConfig {
            /**
             * 解析超时时间（毫秒）
             */
            private int timeout = 5000;
            
            /**
             * 最大重试次数
             */
            private int maxRetries = 3;
            
            /**
             * 批量处理大小
             */
            private int batchSize = 50;
            
            /**
             * 是否启用智能识别
             */
            private boolean enableSmartRecognition = true;
        }
        
        @Data
        public static class GeographicCacheConfig {
            /**
             * 最大缓存条目数
             */
            private int maxEntries = 10000;
            
            /**
             * 缓存过期时间（秒）
             */
            private int expireAfterWrite = 7200;
            
            /**
             * 缓存刷新时间（秒）
             */
            private int refreshAfterWrite = 3600;
            
            /**
             * 是否启用统计
             */
            private boolean enableStats = true;
        }
    }
    
    /**
     * API调用增强配置
     */
    @Data
    public static class ApiConfig {
        /**
         * 火山引擎API配置
         */
        private VolcengineConfig volcengine = new VolcengineConfig();
        
        /**
         * DeepSeek API配置
         */
        private DeepSeekConfig deepseek = new DeepSeekConfig();
        
        /**
         * 通用API配置
         */
        private CommonConfig common = new CommonConfig();
        
        @Data
        public static class VolcengineConfig {
            /**
             * 最大token数
             */
            private int maxTokens = 4000;
            
            /**
             * 超时时间（毫秒）
             */
            private int timeout = 30000;
            
            /**
             * 重试次数
             */
            private int retryCount = 3;
            
            /**
             * 批量处理大小
             */
            private int batchSize = 5;
            
            /**
             * 并发线程数
             */
            private int concurrentThreads = 3;
            
            /**
             * 请求间隔（毫秒）
             */
            private int requestInterval = 1000;
        }
        
        @Data
        public static class DeepSeekConfig {
            /**
             * 最大token数
             */
            private int maxTokens = 2000;
            
            /**
             * 超时时间（毫秒）
             */
            private int timeout = 20000;
            
            /**
             * 重试次数
             */
            private int retryCount = 2;
            
            /**
             * 批量处理大小
             */
            private int batchSize = 3;
            
            /**
             * 并发线程数
             */
            private int concurrentThreads = 2;
            
            /**
             * 请求间隔（毫秒）
             */
            private int requestInterval = 1500;
        }
        
        @Data
        public static class CommonConfig {
            /**
             * 响应完整性检查
             */
            private CompletenessCheckConfig completenessCheck = new CompletenessCheckConfig();
            
            /**
             * 重试策略
             */
            private RetryStrategyConfig retryStrategy = new RetryStrategyConfig();
            
            @Data
            public static class CompletenessCheckConfig {
                /**
                 * 是否启用
                 */
                private boolean enabled = true;
                
                /**
                 * 最小事件数量阈值
                 */
                private int minEventThreshold = 3;
                
                /**
                 * 响应长度阈值
                 */
                private int minResponseLength = 100;
                
                /**
                 * 关键字检查
                 */
                private List<String> requiredKeywords = List.of("事件", "时间", "地点");
            }
            
            @Data
            public static class RetryStrategyConfig {
                /**
                 * 重试算法：FIXED, EXPONENTIAL, LINEAR
                 */
                private String algorithm = "EXPONENTIAL";
                
                /**
                 * 初始延迟（毫秒）
                 */
                private int initialDelay = 1000;
                
                /**
                 * 最大延迟（毫秒）
                 */
                private int maxDelay = 10000;
                
                /**
                 * 延迟倍数
                 */
                private double multiplier = 2.0;
            }
        }
    }
    
    /**
     * 性能监控配置
     */
    @Data
    public static class MonitoringConfig {
        /**
         * 是否启用性能监控
         */
        private boolean enabled = true;
        
        /**
         * 监控指标收集间隔（秒）
         */
        private int metricsInterval = 30;
        
        /**
         * 历史数据保留时间（小时）
         */
        private int historyRetentionHours = 24;
        
        /**
         * 性能阈值配置
         */
        private ThresholdsConfig thresholds = new ThresholdsConfig();
        
        /**
         * 告警配置
         */
        private AlertsConfig alerts = new AlertsConfig();
        
        @Data
        public static class ThresholdsConfig {
            /**
             * 时间段分割处理时间阈值（毫秒）
             */
            private int segmentationTime = 5000;
            
            /**
             * 地理信息处理时间阈值（毫秒）
             */
            private int geographicProcessingTime = 3000;
            
            /**
             * API调用时间阈值（毫秒）
             */
            private int apiCallTime = 15000;
            
            /**
             * 内存使用阈值（MB）
             */
            private int memoryUsage = 512;
            
            /**
             * CPU使用率阈值（%）
             */
            private double cpuUsage = 80.0;
        }
        
        @Data
        public static class AlertsConfig {
            /**
             * 是否启用告警
             */
            private boolean enabled = true;
            
            /**
             * 告警阈值触发次数
             */
            private int thresholdTriggerCount = 3;
            
            /**
             * 告警冷却时间（秒）
             */
            private int cooldownPeriod = 300;
            
            /**
             * 告警通知方式：LOG, EMAIL, WEBHOOK
             */
            private List<String> notificationMethods = List.of("LOG");
        }
    }
    
    /**
     * 缓存配置
     */
    @Data
    public static class CacheConfig {
        /**
         * 时间段分割结果缓存
         */
        private SegmentationCacheConfig segmentation = new SegmentationCacheConfig();
        
        /**
         * 地理信息缓存
         */
        private GeographicCacheConfig geographic = new GeographicCacheConfig();
        
        /**
         * API响应缓存
         */
        private ApiResponseCacheConfig apiResponse = new ApiResponseCacheConfig();
        
        @Data
        public static class SegmentationCacheConfig {
            /**
             * 是否启用
             */
            private boolean enabled = true;
            
            /**
             * 最大条目数
             */
            private int maxEntries = 1000;
            
            /**
             * 过期时间（秒）
             */
            private int expireAfterWrite = 1800;
            
            /**
             * 刷新时间（秒）
             */
            private int refreshAfterWrite = 900;
        }
        
        @Data
        public static class GeographicCacheConfig {
            /**
             * 是否启用
             */
            private boolean enabled = true;
            
            /**
             * 最大条目数
             */
            private int maxEntries = 5000;
            
            /**
             * 过期时间（秒）
             */
            private int expireAfterWrite = 7200;
            
            /**
             * 刷新时间（秒）
             */
            private int refreshAfterWrite = 3600;
        }
        
        @Data
        public static class ApiResponseCacheConfig {
            /**
             * 是否启用
             */
            private boolean enabled = true;
            
            /**
             * 最大条目数
             */
            private int maxEntries = 2000;
            
            /**
             * 过期时间（秒）
             */
            private int expireAfterWrite = 3600;
            
            /**
             * 刷新时间（秒）
             */
            private int refreshAfterWrite = 1800;
        }
    }
    
    /**
     * 前端UI配置
     */
    @Data
    public static class FrontendConfig {
        /**
         * 时间线配置
         */
        private TimelineConfig timeline = new TimelineConfig();
        
        @Data
        public static class TimelineConfig {
            /**
             * 隐藏中间时间标签
             */
            private boolean hideMiddleTimeLabels = true;
            
            /**
             * 显示事件坐标
             */
            private boolean showEventCoordinates = true;
            
            /**
             * 启用平滑滚动
             */
            private boolean enableSmoothScrolling = true;
            
            /**
             * 卡片动画持续时间（毫秒）
             */
            private int cardAnimationDuration = 300;
            
            /**
             * 时间线指示器样式
             */
            private String indicatorStyle = "minimal";
            
            /**
             * 事件卡片最大宽度（像素）
             */
            private int maxCardWidth = 400;
            
            /**
             * 时间线容器最大高度（像素）
             */
            private int maxContainerHeight = 800;
        }
    }
    
    /**
     * 数据验证配置
     */
    @Data
    public static class ValidationConfig {
        /**
         * 时间段验证
         */
        private TimeSegmentConfig timeSegment = new TimeSegmentConfig();
        
        /**
         * 地理信息验证
         */
        private GeographicValidationConfig geographic = new GeographicValidationConfig();
        
        /**
         * 事件数据验证
         */
        private EventDataConfig eventData = new EventDataConfig();
        
        @Data
        public static class TimeSegmentConfig {
            /**
             * 最小时间跨度（小时）
             */
            private int minSpanHours = 1;
            
            /**
             * 最大时间跨度（天）
             */
            private int maxSpanDays = 365;
            
            /**
             * 是否允许未来时间
             */
            private boolean allowFutureTime = false;
        }
        
        @Data
        public static class GeographicValidationConfig {
            /**
             * 纬度范围
             */
            private double[] latitudeRange = {-90.0, 90.0};
            
            /**
             * 经度范围
             */
            private double[] longitudeRange = {-180.0, 180.0};
            
            /**
             * 坐标精度（小数位数）
             */
            private int coordinatePrecision = 6;
            
            /**
             * 是否验证坐标有效性
             */
            private boolean validateCoordinates = true;
        }
        
        @Data
        public static class EventDataConfig {
            /**
             * 事件标题最小长度
             */
            private int minTitleLength = 5;
            
            /**
             * 事件标题最大长度
             */
            private int maxTitleLength = 200;
            
            /**
             * 事件描述最大长度
             */
            private int maxDescriptionLength = 2000;
            
            /**
             * 必需字段
             */
            private List<String> requiredFields = List.of("title", "eventTime", "location");
        }
    }
    
    /**
     * 错误处理配置
     */
    @Data
    public static class ErrorHandlingConfig {
        /**
         * 时间段分割错误处理
         */
        private SegmentationErrorConfig segmentation = new SegmentationErrorConfig();
        
        /**
         * 地理信息处理错误处理
         */
        private GeographicErrorConfig geographic = new GeographicErrorConfig();
        
        /**
         * API调用错误处理
         */
        private ApiErrorConfig api = new ApiErrorConfig();
        
        @Data
        public static class SegmentationErrorConfig {
            /**
             * 分割失败时是否回退到单次调用
             */
            private boolean fallbackToSingleCall = true;
            
            /**
             * 部分失败时是否继续处理
             */
            private boolean continueOnPartialFailure = true;
            
            /**
             * 最大失败容忍度（百分比）
             */
            private double maxFailureTolerance = 30.0;
        }
        
        @Data
        public static class GeographicErrorConfig {
            /**
             * 解析失败时是否使用默认坐标
             */
            private boolean useDefaultOnFailure = true;
            
            /**
             * 是否跳过无效坐标
             */
            private boolean skipInvalidCoordinates = true;
            
            /**
             * 错误重试间隔（毫秒）
             */
            private int retryInterval = 2000;
        }
        
        @Data
        public static class ApiErrorConfig {
            /**
             * 调用失败时是否切换API
             */
            private boolean switchApiOnFailure = true;
            
            /**
             * 降级处理策略：SKIP, RETRY, FALLBACK
             */
            private String degradationStrategy = "FALLBACK";
            
            /**
             * 熔断器配置
             */
            private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();
            
            @Data
            public static class CircuitBreakerConfig {
                /**
                 * 失败阈值
                 */
                private int failureThreshold = 5;
                
                /**
                 * 超时时间（毫秒）
                 */
                private int timeout = 60000;
                
                /**
                 * 半开状态最大调用次数
                 */
                private int halfOpenMaxCalls = 3;
            }
        }
    }
}