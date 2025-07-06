package service;

import agent.TaskExecution;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * WebSocket 消息服务
 * 用于推送任务状态更新
 */
@Service
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * 推送任务状态更新
     */
    public void pushTaskUpdate(TaskExecution task) {
        messagingTemplate.convertAndSend("/topic/task-update", task);
    }
    
    /**
     * 推送任务完成通知
     */
    public void pushTaskCompleted(TaskExecution task) {
        messagingTemplate.convertAndSend("/topic/task-completed", task);
    }
    
    /**
     * 推送任务失败通知
     */
    public void pushTaskFailed(TaskExecution task) {
        messagingTemplate.convertAndSend("/topic/task-failed", task);
    }
    
    /**
     * 推送系统消息
     */
    public void pushSystemMessage(String message) {
        messagingTemplate.convertAndSend("/topic/system", Map.of("message", message));
    }
} 