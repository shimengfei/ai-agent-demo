package agent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务执行状态
 * 用于跟踪任务执行进度和状态
 */
public class TaskExecution {
    private final String taskId;
    private final String userInput;
    private final LocalDateTime createdAt;
    private String status; // PENDING, ANALYZING, EXECUTING, COMPLETED, FAILED
    private String statusMessage;
    private String result;
    private LocalDateTime updatedAt;
    private final List<String> logs;
    
    public TaskExecution(String taskId, String userInput) {
        this.taskId = taskId;
        this.userInput = userInput;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "PENDING";
        this.statusMessage = "任务已提交，等待执行";
        this.logs = new ArrayList<>();
        this.logs.add("任务创建: " + taskId);
    }
    
    public void updateStatus(String status, String statusMessage) {
        this.status = status;
        this.statusMessage = statusMessage;
        this.updatedAt = LocalDateTime.now();
        this.logs.add(String.format("[%s] %s: %s", 
            updatedAt.toString(), status, statusMessage));
    }
    
    public void addLog(String message) {
        this.updatedAt = LocalDateTime.now();
        this.logs.add(String.format("[%s] %s", updatedAt.toString(), message));
    }
    
    public void setResult(String result) {
        this.result = result;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public String getTaskId() { return taskId; }
    public String getUserInput() { return userInput; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public String getStatusMessage() { return statusMessage; }
    public String getResult() { return result; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<String> getLogs() { return new ArrayList<>(logs); }
    
    @Override
    public String toString() {
        return String.format("TaskExecution{taskId='%s', status='%s', userInput='%s'}", 
            taskId, status, userInput);
    }
} 