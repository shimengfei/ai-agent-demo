package controller;

import agent.EnhancedAgentOrchestrator;
import agent.TaskExecution;
import config.AgentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent REST API 控制器
 * 提供任务提交、状态查询等接口
 * 使用统一的 AgentConfig 进行配置管理
 */
@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://127.0.0.1:8080"}, allowCredentials = "false")
public class AgentController {
    
    @Autowired
    private EnhancedAgentOrchestrator orchestrator;
    
    @Autowired
    private AgentConfig agentConfig;
    
    /**
     * 提交任务
     */
    @PostMapping("/task")
    public ResponseEntity<TaskExecution> submitTask(@RequestBody TaskRequest request) {
        try {
            TaskExecution task = orchestrator.submitTask(request.getUserInput());
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取任务状态
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<TaskExecution> getTaskStatus(@PathVariable String taskId) {
        TaskExecution task = orchestrator.getTaskStatus(taskId);
        if (task != null) {
            return ResponseEntity.ok(task);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取所有活跃任务
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskExecution>> getAllTasks() {
        List<TaskExecution> tasks = orchestrator.getAllActiveTasks();
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * 获取可用Agent列表
     */
    @GetMapping("/agents")
    public ResponseEntity<Map<String, String>> getAvailableAgents() {
        Map<String, String> agents = orchestrator.getAvailableAgents();
        return ResponseEntity.ok(agents);
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "timestamp", System.currentTimeMillis(),
            "agents", orchestrator.getAvailableAgents().size(),
            "config", agentConfig.getConfigurationInfo()
        );
        return ResponseEntity.ok(health);
    }
    
    /**
     * 获取配置信息
     */
    @GetMapping("/config")
    public ResponseEntity<String> getConfig() {
        return ResponseEntity.ok(agentConfig.getConfigurationInfo());
    }
    
    /**
     * 验证配置
     */
    @GetMapping("/config/validate")
    public ResponseEntity<Map<String, Object>> validateConfig() {
        Map<String, Object> validation = new HashMap<>();
        validation.put("apiKeyValid", agentConfig.validateEnvironment());
        validation.put("modelName", agentConfig.getModelName());
        validation.put("temperature", agentConfig.getTemperature());
        validation.put("maxTokens", agentConfig.getMaxTokens());
        return ResponseEntity.ok(validation);
    }
    
    /**
     * 任务请求对象
     */
    public static class TaskRequest {
        private String userInput;
        
        public String getUserInput() { return userInput; }
        public void setUserInput(String userInput) { this.userInput = userInput; }
    }
} 