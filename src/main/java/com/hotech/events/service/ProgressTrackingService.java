package com.hotech.events.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 进度跟踪服务接口
 * 提供实时进度反馈和状态更新功能
 */
public interface ProgressTrackingService {
    
    /**
     * 开始任务跟踪
     * 
     * @param taskId 任务ID
     * @param taskName 任务名称
     * @param totalSteps 总步骤数
     * @return 跟踪会话ID
     */
    String startTracking(String taskId, String taskName, int totalSteps);
    
    /**
     * 更新任务进度
     * 
     * @param sessionId 跟踪会话ID
     * @param currentStep 当前步骤
     * @param stepName 步骤名称
     * @param progress 进度百分比 (0-100)
     * @param message 进度消息
     */
    void updateProgress(String sessionId, int currentStep, String stepName, int progress, String message);
    
    /**
     * 完成任务跟踪
     * 
     * @param sessionId 跟踪会话ID
     * @param success 是否成功
     * @param finalMessage 最终消息
     */
    void completeTracking(String sessionId, boolean success, String finalMessage);
    
    /**
     * 获取任务进度
     * 
     * @param sessionId 跟踪会话ID
     * @return 任务进度信息
     */
    TaskProgress getProgress(String sessionId);
    
    /**
     * 获取任务历史记录
     * 
     * @param taskId 任务ID
     * @return 任务历史记录列表
     */
    List<TaskProgress> getTaskHistory(String taskId);
    
    /**
     * 获取活跃任务列表
     * 
     * @return 活跃任务列表
     */
    List<TaskProgress> getActiveTasks();
    
    /**
     * 清理过期的跟踪记录
     */
    void cleanupExpiredRecords();
    
    /**
     * 任务进度信息
     */
    class TaskProgress {
        private String sessionId;
        private String taskId;
        private String taskName;
        private int totalSteps;
        private int currentStep;
        private String stepName;
        private int progress; // 0-100
        private String message;
        private TaskStatus status;
        private LocalDateTime startTime;
        private LocalDateTime lastUpdateTime;
        private LocalDateTime endTime;
        private long duration; // 毫秒
        private List<ProgressStep> steps;
        
        // Getters and Setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        
        public int getTotalSteps() { return totalSteps; }
        public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }
        
        public int getCurrentStep() { return currentStep; }
        public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }
        
        public String getStepName() { return stepName; }
        public void setStepName(String stepName) { this.stepName = stepName; }
        
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public TaskStatus getStatus() { return status; }
        public void setStatus(TaskStatus status) { this.status = status; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public List<ProgressStep> getSteps() { return steps; }
        public void setSteps(List<ProgressStep> steps) { this.steps = steps; }
    }
    
    /**
     * 进度步骤信息
     */
    class ProgressStep {
        private int stepNumber;
        private String stepName;
        private int progress;
        private String message;
        private LocalDateTime startTime;
        private LocalDateTime lastUpdateTime;
        private LocalDateTime endTime;
        private long duration;
        private StepStatus status;
        
        // Getters and Setters
        public int getStepNumber() { return stepNumber; }
        public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }
        
        public String getStepName() { return stepName; }
        public void setStepName(String stepName) { this.stepName = stepName; }
        
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public StepStatus getStatus() { return status; }
        public void setStatus(StepStatus status) { this.status = status; }
    }
    
    /**
     * 任务状态枚举
     */
    enum TaskStatus {
        STARTED,    // 已开始
        RUNNING,    // 运行中
        COMPLETED,  // 已完成
        FAILED,     // 失败
        CANCELLED   // 已取消
    }
    
    /**
     * 步骤状态枚举
     */
    enum StepStatus {
        PENDING,    // 待执行
        RUNNING,    // 执行中
        COMPLETED,  // 已完成
        FAILED,     // 失败
        SKIPPED     // 已跳过
    }
}