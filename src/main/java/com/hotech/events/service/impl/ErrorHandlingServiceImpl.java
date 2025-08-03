package com.hotech.events.service.impl;

import com.hotech.events.exception.TimelineEnhancementException;
import com.hotech.events.service.ErrorHandlingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 错误处理服务实现
 * 
 * @author Kiro
 * @since 2024-01-01
 */
@Slf4j
@Service
public class ErrorHandlingServiceImpl implements ErrorHandlingService {

    // 错误统计
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong retryableErrors = new AtomicLong(0);
    private final AtomicLong nonRetryableErrors = new AtomicLong(0);
    private final AtomicLong successfulRetries = new AtomicLong(0);
    private final AtomicLong failedRetries = new AtomicLong(0);
    private final AtomicLong fallbackExecutions = new AtomicLong(0);
    
    // 熔断器状态管理
    private final Map<String, CircuitBreakerState> circuitBreakers = new HashMap<>();
    private final Map<String, AtomicLong> errorCounts = new HashMap<>();
    private final Map<String, LocalDateTime> lastErrorTimes = new HashMap<>();
    
    // 熔断器配置
    private static final int DEFAULT_ERROR_THRESHOLD = 5; // 默认错误阈值
    private static final long DEFAULT_TIMEOUT_MINUTES = 5; // 默认超时时间（分钟）

    @Override
    public <T> T executeWithRetryAndFallback(Supplier<T> primaryOperation, Supplier<T> fallbackOperation,
            int maxRetries) {
        Exception lastException = null;
        int retryCount = 0; // 将retryCount声明移到for循环外部

        for (retryCount = 0; retryCount <= maxRetries; retryCount++) {
            try {
                if (retryCount > 0) {
                    // 计算重试延迟
                    long delay = calculateRetryDelay(retryCount);
                    log.info("第{}次重试，延迟{}ms", retryCount, delay);
                    Thread.sleep(delay);
                }

                T result = primaryOperation.get();

                if (retryCount > 0) {
                    successfulRetries.incrementAndGet();
                    log.info("重试成功，重试次数: {}", retryCount);
                }

                return result;

            } catch (Exception e) {
                lastException = e;
                totalErrors.incrementAndGet();

                if (shouldRetry(e, retryCount, maxRetries)) {
                    retryableErrors.incrementAndGet();
                    log.warn("操作失败，将进行重试。重试次数: {}/{}, 错误: {}",
                            retryCount + 1, maxRetries, e.getMessage());
                } else {
                    nonRetryableErrors.incrementAndGet();
                    log.error("操作失败，不可重试或已达到最大重试次数。错误: {}", e.getMessage());
                    break;
                }
            }
        }

        // 所有重试都失败了，执行降级操作
        if (retryCount > maxRetries) {
            failedRetries.incrementAndGet();
        }

        log.warn("主要操作失败，执行降级操作");
        final Exception finalException = lastException; // 创建final变量供lambda使用
        return executeWithFallback(() -> {
            throw new RuntimeException("主要操作失败", finalException);
        }, fallbackOperation);
    }

    @Override
    public <T> T executeWithFallback(Supplier<T> primaryOperation, Supplier<T> fallbackOperation) {
        try {
            return primaryOperation.get();
        } catch (Exception e) {
            log.warn("主要操作失败，执行降级操作。错误: {}", e.getMessage());
            fallbackExecutions.incrementAndGet();

            try {
                return fallbackOperation.get();
            } catch (Exception fallbackException) {
                log.error("降级操作也失败了", fallbackException);
                throw new RuntimeException("主要操作和降级操作都失败了", fallbackException);
            }
        }
    }

    @Override
    public void logError(String errorType, String errorMessage, Exception exception) {
        totalErrors.incrementAndGet();

        Map<String, Object> errorContext = new HashMap<>();
        errorContext.put("errorType", errorType);
        errorContext.put("errorMessage", errorMessage);
        errorContext.put("timestamp", LocalDateTime.now());
        errorContext.put("exceptionClass", exception.getClass().getSimpleName());
        errorContext.put("exceptionMessage", exception.getMessage());

        log.error("错误记录 - 类型: {}, 消息: {}, 异常: {}",
                errorType, errorMessage, exception.getMessage(), exception);

        // 这里可以添加更复杂的错误记录逻辑，比如发送到监控系统
    }

    @Override
    public String createUserFriendlyMessage(TimelineEnhancementException exception) {
        String errorCode = exception.getErrorCode();
        String category = exception.getCategory();

        // 根据错误代码和类别创建用户友好的消息
        switch (category) {
            case "VALIDATION":
                return "请求参数有误：" + exception.getMessage();
            case "API":
                return "外部服务暂时不可用，请稍后重试";
            case "DATABASE":
                return "数据访问出现问题，请稍后重试";
            case "NETWORK":
                return "网络连接异常，请检查网络连接后重试";
            case "TIMEOUT":
                return "操作超时，请稍后重试";
            case "RATE_LIMIT":
                return "请求过于频繁，请稍后重试";
            case "GENERATION":
                return "时间线生成失败，请稍后重试";
            default:
                return "系统暂时出现问题，请稍后重试";
        }
    }

    @Override
    public Map<String, Object> getErrorStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalErrors", totalErrors.get());
        stats.put("retryableErrors", retryableErrors.get());
        stats.put("nonRetryableErrors", nonRetryableErrors.get());
        stats.put("successfulRetries", successfulRetries.get());
        stats.put("failedRetries", failedRetries.get());
        stats.put("fallbackExecutions", fallbackExecutions.get());

        // 计算成功率
        long totalRetries = successfulRetries.get() + failedRetries.get();
        if (totalRetries > 0) {
            stats.put("retrySuccessRate", (double) successfulRetries.get() / totalRetries);
        } else {
            stats.put("retrySuccessRate", 0.0);
        }

        stats.put("lastUpdated", LocalDateTime.now());

        return stats;
    }

    @Override
    public void resetErrorStatistics() {
        totalErrors.set(0);
        retryableErrors.set(0);
        nonRetryableErrors.set(0);
        successfulRetries.set(0);
        failedRetries.set(0);
        fallbackExecutions.set(0);

        log.info("错误统计信息已重置");
    }

    @Override
    public boolean shouldRetry(Exception exception, int retryCount, int maxRetries) {
        // 如果已达到最大重试次数，不再重试
        if (retryCount >= maxRetries) {
            return false;
        }

        // 根据异常类型判断是否应该重试
        String exceptionMessage = exception.getMessage().toLowerCase();

        // 网络相关错误通常可以重试
        if (exceptionMessage.contains("timeout") ||
                exceptionMessage.contains("connection") ||
                exceptionMessage.contains("socket") ||
                exceptionMessage.contains("network")) {
            return true;
        }

        // API限流错误可以重试
        if (exceptionMessage.contains("rate limit") ||
                exceptionMessage.contains("quota") ||
                exceptionMessage.contains("throttle")) {
            return true;
        }

        // 服务器错误可以重试
        if (exceptionMessage.contains("server error") ||
                exceptionMessage.contains("internal error") ||
                exceptionMessage.contains("service unavailable")) {
            return true;
        }

        // 如果是TimelineEnhancementException，检查其类别
        if (exception instanceof TimelineEnhancementException) {
            TimelineEnhancementException tee = (TimelineEnhancementException) exception;
            String category = tee.getCategory();

            // 这些类别的错误通常可以重试
            return "NETWORK".equals(category) ||
                    "TIMEOUT".equals(category) ||
                    "API".equals(category) ||
                    "RATE_LIMIT".equals(category);
        }

        // 默认情况下，对于未知错误，进行有限的重试
        return retryCount < 2;
    }

    @Override
    public long calculateRetryDelay(int retryCount) {
        // 指数退避算法：基础延迟 * 2^重试次数
        long baseDelay = 1000; // 1秒基础延迟
        long maxDelay = 30000; // 最大30秒延迟

        long delay = baseDelay * (1L << retryCount);
        return Math.min(delay, maxDelay);
    }

    @Override
    public boolean shouldTriggerCircuitBreaker(String errorType) {
        if (errorType == null || errorType.trim().isEmpty()) {
            return false;
        }

        synchronized (circuitBreakers) {
            CircuitBreakerState state = circuitBreakers.get(errorType);
            
            // 如果熔断器已经打开，检查是否应该保持打开状态
            if (state == CircuitBreakerState.OPEN) {
                LocalDateTime lastErrorTime = lastErrorTimes.get(errorType);
                if (lastErrorTime != null && 
                    lastErrorTime.plusMinutes(DEFAULT_TIMEOUT_MINUTES).isAfter(LocalDateTime.now())) {
                    return true; // 仍在超时期内，保持熔断器打开
                } else {
                    // 超时期已过，将熔断器设置为半开状态
                    circuitBreakers.put(errorType, CircuitBreakerState.HALF_OPEN);
                    log.info("熔断器 {} 从打开状态转为半开状态", errorType);
                    return false;
                }
            }

            // 检查错误计数是否超过阈值
            AtomicLong errorCount = errorCounts.get(errorType);
            if (errorCount != null && errorCount.get() >= DEFAULT_ERROR_THRESHOLD) {
                circuitBreakers.put(errorType, CircuitBreakerState.OPEN);
                lastErrorTimes.put(errorType, LocalDateTime.now());
                recordCircuitBreakerTrigger(errorType, "错误次数超过阈值: " + errorCount.get());
                log.warn("熔断器 {} 被触发，错误次数: {}", errorType, errorCount.get());
                return true;
            }

            return false;
        }
    }

    @Override
    public void resetCircuitBreaker(String errorType) {
        if (errorType == null || errorType.trim().isEmpty()) {
            return;
        }

        synchronized (circuitBreakers) {
            circuitBreakers.put(errorType, CircuitBreakerState.CLOSED);
            errorCounts.put(errorType, new AtomicLong(0));
            lastErrorTimes.remove(errorType);
            
            log.info("熔断器 {} 已重置为关闭状态", errorType);
        }
    }

    @Override
    public Map<String, Object> getCircuitBreakerStatus(String errorType) {
        Map<String, Object> status = new HashMap<>();
        
        if (errorType == null || errorType.trim().isEmpty()) {
            status.put("error", "错误类型不能为空");
            return status;
        }

        synchronized (circuitBreakers) {
            CircuitBreakerState state = circuitBreakers.getOrDefault(errorType, CircuitBreakerState.CLOSED);
            AtomicLong errorCount = errorCounts.getOrDefault(errorType, new AtomicLong(0));
            LocalDateTime lastErrorTime = lastErrorTimes.get(errorType);

            status.put("errorType", errorType);
            status.put("state", state.name());
            status.put("stateDescription", state.getDescription());
            status.put("errorCount", errorCount.get());
            status.put("errorThreshold", DEFAULT_ERROR_THRESHOLD);
            status.put("lastErrorTime", lastErrorTime);
            status.put("timeoutMinutes", DEFAULT_TIMEOUT_MINUTES);
            status.put("isTriggered", state == CircuitBreakerState.OPEN);

            if (state == CircuitBreakerState.OPEN && lastErrorTime != null) {
                LocalDateTime resetTime = lastErrorTime.plusMinutes(DEFAULT_TIMEOUT_MINUTES);
                status.put("estimatedResetTime", resetTime);
                status.put("remainingTimeoutMinutes", 
                    java.time.Duration.between(LocalDateTime.now(), resetTime).toMinutes());
            }
        }

        return status;
    }

    @Override
    public void recordCircuitBreakerTrigger(String errorType, String reason) {
        if (errorType == null || errorType.trim().isEmpty()) {
            return;
        }

        // 增加错误计数
        synchronized (circuitBreakers) {
            errorCounts.computeIfAbsent(errorType, k -> new AtomicLong(0)).incrementAndGet();
            lastErrorTimes.put(errorType, LocalDateTime.now());
        }

        log.warn("记录熔断器触发事件 - 类型: {}, 原因: {}", errorType, reason);

        // 这里可以添加更复杂的记录逻辑，比如发送到监控系统
        Map<String, Object> triggerEvent = new HashMap<>();
        triggerEvent.put("errorType", errorType);
        triggerEvent.put("reason", reason);
        triggerEvent.put("timestamp", LocalDateTime.now());
        triggerEvent.put("errorCount", errorCounts.get(errorType).get());
    }

    /**
     * 熔断器状态枚举
     */
    private enum CircuitBreakerState {
        CLOSED("关闭", "正常状态，允许请求通过"),
        OPEN("打开", "熔断状态，拒绝请求"),
        HALF_OPEN("半开", "测试状态，允许少量请求通过以测试服务是否恢复");

        private final String name;
        private final String description;

        CircuitBreakerState(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}