package com.hotech.events.service.impl;

import com.hotech.events.service.ProgressTrackingService;
import com.hotech.events.service.SystemMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 进度跟踪服务实现类
 * 提供实时进度反馈和状态更新功能
 */
@Slf4j
@Service
public class ProgressTrackingServiceImpl implements ProgressTrackingService {

    @Autowired
    private SystemMonitoringService monitoringService;

    // 活跃任务跟踪
    private final ConcurrentHashMap<String, TaskProgress> activeTasks = new ConcurrentHashMap<>();

    // 任务历史记录
    private final ConcurrentHashMap<String, List<TaskProgress>> taskHistory = new ConcurrentHashMap<>();

    @Override
    public String startTracking(String taskId, String taskName, int totalSteps) {
        try {
            String sessionId = UUID.randomUUID().toString();

            TaskProgress progress = new TaskProgress();
            progress.setSessionId(sessionId);
            progress.setTaskId(taskId);
            progress.setTaskName(taskName);
            progress.setTotalSteps(totalSteps);
            progress.setCurrentStep(0);
            progress.setProgress(0);
            progress.setStatus(TaskStatus.STARTED);
            progress.setStartTime(LocalDateTime.now());
            progress.setLastUpdateTime(LocalDateTime.now());
            progress.setSteps(new ArrayList<>());

            // 初始化步骤列表
            for (int i = 1; i <= totalSteps; i++) {
                ProgressStep step = new ProgressStep();
                step.setStepNumber(i);
                step.setStepName("步骤 " + i);
                step.setProgress(0);
                step.setStatus(StepStatus.PENDING);
                progress.getSteps().add(step);
            }

            activeTasks.put(sessionId, progress);

            log.info("开始任务跟踪: sessionId={}, taskId={}, taskName={}, totalSteps={}",
                    sessionId, taskId, taskName, totalSteps);

            // 记录性能指标
            monitoringService.recordPerformanceMetrics("TASK_TRACKING_START",
                    0, getMemoryUsage(), getCpuUsage());

            return sessionId;

        } catch (Exception e) {
            log.error("开始任务跟踪失败: taskId={}, taskName={}", taskId, taskName, e);

            // 记录系统错误
            monitoringService.recordSystemError("TASK_TRACKING_START", "TRACKING_START_ERROR",
                    e.getMessage(), getStackTrace(e));

            throw new RuntimeException("开始任务跟踪失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateProgress(String sessionId, int currentStep, String stepName, int progress, String message) {
        try {
            TaskProgress taskProgress = activeTasks.get(sessionId);
            if (taskProgress == null) {
                log.warn("未找到跟踪会话: sessionId={}", sessionId);
                return;
            }

            // 更新任务进度
            taskProgress.setCurrentStep(currentStep);
            taskProgress.setStepName(stepName);
            taskProgress.setProgress(Math.max(0, Math.min(100, progress))); // 确保在0-100范围内
            taskProgress.setMessage(message);
            taskProgress.setStatus(TaskStatus.RUNNING);
            taskProgress.setLastUpdateTime(LocalDateTime.now());

            // 更新步骤进度
            if (currentStep > 0 && currentStep <= taskProgress.getSteps().size()) {
                ProgressStep step = taskProgress.getSteps().get(currentStep - 1);
                step.setStepName(stepName);
                step.setProgress(progress);
                step.setMessage(message);
                step.setStatus(StepStatus.RUNNING);
                step.setLastUpdateTime(LocalDateTime.now());

                if (step.getStartTime() == null) {
                    step.setStartTime(LocalDateTime.now());
                }
            }

            log.debug("更新任务进度: sessionId={}, step={}/{}, progress={}%, message={}",
                    sessionId, currentStep, taskProgress.getTotalSteps(), progress, message);

        } catch (Exception e) {
            log.error("更新任务进度失败: sessionId={}", sessionId, e);

            // 记录系统错误
            monitoringService.recordSystemError("TASK_TRACKING_UPDATE", "TRACKING_UPDATE_ERROR",
                    e.getMessage(), getStackTrace(e));
        }
    }

    @Override
    public void completeTracking(String sessionId, boolean success, String finalMessage) {
        try {
            TaskProgress taskProgress = activeTasks.get(sessionId);
            if (taskProgress == null) {
                log.warn("未找到跟踪会话: sessionId={}", sessionId);
                return;
            }

            // 更新任务状态
            taskProgress.setStatus(success ? TaskStatus.COMPLETED : TaskStatus.FAILED);
            taskProgress.setMessage(finalMessage);
            taskProgress.setEndTime(LocalDateTime.now());
            taskProgress.setLastUpdateTime(LocalDateTime.now());

            // 计算总耗时
            if (taskProgress.getStartTime() != null) {
                taskProgress.setDuration(ChronoUnit.MILLIS.between(
                        taskProgress.getStartTime(), taskProgress.getEndTime()));
            }

            // 完成当前步骤
            if (taskProgress.getCurrentStep() > 0 &&
                    taskProgress.getCurrentStep() <= taskProgress.getSteps().size()) {
                ProgressStep currentStep = taskProgress.getSteps().get(taskProgress.getCurrentStep() - 1);
                currentStep.setStatus(success ? StepStatus.COMPLETED : StepStatus.FAILED);
                currentStep.setEndTime(LocalDateTime.now());

                if (currentStep.getStartTime() != null) {
                    currentStep.setDuration(ChronoUnit.MILLIS.between(
                            currentStep.getStartTime(), currentStep.getEndTime()));
                }
            }

            // 如果成功完成，设置进度为100%
            if (success) {
                taskProgress.setProgress(100);
            }

            // 移动到历史记录
            moveToHistory(taskProgress);

            // 从活跃任务中移除
            activeTasks.remove(sessionId);

            log.info("完成任务跟踪: sessionId={}, taskId={}, success={}, duration={}ms, message={}",
                    sessionId, taskProgress.getTaskId(), success, taskProgress.getDuration(), finalMessage);

            // 记录性能指标
            monitoringService.recordPerformanceMetrics("TASK_TRACKING_COMPLETE",
                    taskProgress.getDuration(), getMemoryUsage(), getCpuUsage());

        } catch (Exception e) {
            log.error("完成任务跟踪失败: sessionId={}", sessionId, e);

            // 记录系统错误
            monitoringService.recordSystemError("TASK_TRACKING_COMPLETE", "TRACKING_COMPLETE_ERROR",
                    e.getMessage(), getStackTrace(e));
        }
    }

    @Override
    public TaskProgress getProgress(String sessionId) {
        try {
            TaskProgress progress = activeTasks.get(sessionId);
            if (progress != null) {
                return cloneTaskProgress(progress);
            }

            // 如果不在活跃任务中，查找历史记录
            for (List<TaskProgress> historyList : taskHistory.values()) {
                for (TaskProgress historyProgress : historyList) {
                    if (sessionId.equals(historyProgress.getSessionId())) {
                        return cloneTaskProgress(historyProgress);
                    }
                }
            }

            return null;

        } catch (Exception e) {
            log.error("获取任务进度失败: sessionId={}", sessionId, e);
            return null;
        }
    }

    @Override
    public List<TaskProgress> getTaskHistory(String taskId) {
        try {
            List<TaskProgress> history = taskHistory.get(taskId);
            if (history == null) {
                return new ArrayList<>();
            }

            return history.stream()
                    .map(this::cloneTaskProgress)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取任务历史记录失败: taskId={}", taskId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<TaskProgress> getActiveTasks() {
        try {
            return activeTasks.values().stream()
                    .map(this::cloneTaskProgress)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取活跃任务列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanupExpiredRecords() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7); // 保留7天的记录
            int removedCount = 0;

            // 清理过期的历史记录
            for (String taskId : taskHistory.keySet()) {
                List<TaskProgress> history = taskHistory.get(taskId);
                if (history != null) {
                    int originalSize = history.size();
                    history.removeIf(
                            progress -> progress.getEndTime() != null && progress.getEndTime().isBefore(cutoffTime));
                    removedCount += originalSize - history.size();

                    // 如果历史记录为空，移除整个条目
                    if (history.isEmpty()) {
                        taskHistory.remove(taskId);
                    }
                }
            }

            // 清理长时间未更新的活跃任务
            LocalDateTime activeTaskCutoff = LocalDateTime.now().minusHours(24); // 24小时未更新的任务
            List<String> expiredActiveTasks = new ArrayList<>();

            for (String sessionId : activeTasks.keySet()) {
                TaskProgress progress = activeTasks.get(sessionId);
                if (progress != null && progress.getLastUpdateTime().isBefore(activeTaskCutoff)) {
                    expiredActiveTasks.add(sessionId);
                }
            }

            for (String sessionId : expiredActiveTasks) {
                TaskProgress progress = activeTasks.remove(sessionId);
                if (progress != null) {
                    progress.setStatus(TaskStatus.CANCELLED);
                    progress.setMessage("任务超时自动取消");
                    progress.setEndTime(LocalDateTime.now());
                    moveToHistory(progress);
                    removedCount++;
                }
            }

            if (removedCount > 0) {
                log.info("清理过期跟踪记录: removedCount={}", removedCount);
            }

        } catch (Exception e) {
            log.error("清理过期跟踪记录失败", e);
        }
    }

    /**
     * 移动任务到历史记录
     */
    private void moveToHistory(TaskProgress taskProgress) {
        String taskId = taskProgress.getTaskId();
        taskHistory.computeIfAbsent(taskId, k -> new ArrayList<>()).add(cloneTaskProgress(taskProgress));

        // 限制每个任务的历史记录数量
        List<TaskProgress> history = taskHistory.get(taskId);
        if (history.size() > 100) { // 最多保留100条记录
            history.remove(0); // 移除最旧的记录
        }
    }

    /**
     * 克隆任务进度对象
     */
    private TaskProgress cloneTaskProgress(TaskProgress original) {
        TaskProgress clone = new TaskProgress();
        clone.setSessionId(original.getSessionId());
        clone.setTaskId(original.getTaskId());
        clone.setTaskName(original.getTaskName());
        clone.setTotalSteps(original.getTotalSteps());
        clone.setCurrentStep(original.getCurrentStep());
        clone.setStepName(original.getStepName());
        clone.setProgress(original.getProgress());
        clone.setMessage(original.getMessage());
        clone.setStatus(original.getStatus());
        clone.setStartTime(original.getStartTime());
        clone.setLastUpdateTime(original.getLastUpdateTime());
        clone.setEndTime(original.getEndTime());
        clone.setDuration(original.getDuration());

        // 克隆步骤列表
        if (original.getSteps() != null) {
            List<ProgressStep> clonedSteps = new ArrayList<>();
            for (ProgressStep step : original.getSteps()) {
                ProgressStep clonedStep = new ProgressStep();
                clonedStep.setStepNumber(step.getStepNumber());
                clonedStep.setStepName(step.getStepName());
                clonedStep.setProgress(step.getProgress());
                clonedStep.setMessage(step.getMessage());
                clonedStep.setStartTime(step.getStartTime());
                clonedStep.setLastUpdateTime(step.getLastUpdateTime());
                clonedStep.setEndTime(step.getEndTime());
                clonedStep.setDuration(step.getDuration());
                clonedStep.setStatus(step.getStatus());
                clonedSteps.add(clonedStep);
            }
            clone.setSteps(clonedSteps);
        }

        return clone;
    }

    /**
     * 获取内存使用量
     */
    private long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * 获取CPU使用率
     */
    private double getCpuUsage() {
        try {
            return ((com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory
                    .getOperatingSystemMXBean())
                    .getProcessCpuLoad() * 100;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取异常堆栈跟踪
     */
    private String getStackTrace(Throwable throwable) {
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "无法获取堆栈跟踪: " + e.getMessage();
        }
    }
}