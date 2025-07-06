package config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Agent 统一配置类
 * 负责管理所有 Agent 相关的配置和模型交互逻辑
 */
@Component
public class AgentConfig {
    
    // 模型配置
    @Value("${app.agent.model.name:qwen-plus}")
    private String modelName;
    
    @Value("${app.agent.model.temperature:0.7}")
    private double temperature;
    
    @Value("${app.agent.model.max-tokens:1000}")
    private int maxTokens;
    
    @Value("${app.agent.model.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;
    
    // 并发配置
    @Value("${app.agent.max-concurrent-tasks:10}")
    private int maxConcurrentTasks;
    
    @Value("${app.agent.task-timeout-seconds:300}")
    private int taskTimeoutSeconds;
    
    // 聊天记忆配置
    @Value("${app.agent.max-messages:10}")
    private int maxMessages;
    
    // 工具调用配置
    @Value("${app.agent.max-tool-calls-per-request:5}")
    private int maxToolCallsPerRequest;
    
    // 缓存配置
    private OpenAiChatModel cachedModel;
    private ExecutorService cachedExecutorService;
    
    /**
     * 获取或创建 OpenAI 模型实例
     */
    public OpenAiChatModel getModel() {
        if (cachedModel == null) {
            cachedModel = createModel();
        }
        return cachedModel;
    }
    
    /**
     * 创建新的 OpenAI 模型实例
     */
    public OpenAiChatModel createModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(getApiKey())
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }
    
    /**
     * 创建自定义的 OpenAI 模型实例
     */
    public OpenAiChatModel createCustomModel(String customModelName, double customTemperature, int customMaxTokens) {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(getApiKey())
                .modelName(customModelName)
                .temperature(customTemperature)
                .maxTokens(customMaxTokens)
                .build();
    }
    
    /**
     * 获取线程池执行器
     */
    public ExecutorService getExecutorService() {
        if (cachedExecutorService == null || cachedExecutorService.isShutdown()) {
            cachedExecutorService = Executors.newFixedThreadPool(maxConcurrentTasks);
        }
        return cachedExecutorService;
    }
    

    
    /**
     * 获取 API Key
     */
    private String getApiKey() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY 环境变量未设置");
        }
        return apiKey;
    }
    
    /**
     * 验证环境配置
     */
    public boolean validateEnvironment() {
        try {
            getApiKey();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取配置信息
     */
    public String getConfigurationInfo() {
        StringBuilder info = new StringBuilder();
        info.append("🔧 Agent 配置信息:\n");
        info.append("  - 模型名称: ").append(modelName).append("\n");
        info.append("  - 基础URL: ").append(baseUrl).append("\n");
        info.append("  - 温度: ").append(temperature).append("\n");
        info.append("  - 最大令牌数: ").append(maxTokens).append("\n");
        info.append("  - 最大并发任务: ").append(maxConcurrentTasks).append("\n");
        info.append("  - 任务超时时间: ").append(taskTimeoutSeconds).append("秒\n");
        info.append("  - 最大消息数: ").append(maxMessages).append("\n");
        info.append("  - 最大工具调用数: ").append(maxToolCallsPerRequest).append("\n");
        info.append("  - API Key: ").append(validateEnvironment() ? "✅ 已配置" : "❌ 未配置").append("\n");
        return info.toString();
    }
    
    // Getter 方法
    public String getModelName() { return modelName; }
    public double getTemperature() { return temperature; }
    public int getMaxTokens() { return maxTokens; }
    public String getBaseUrl() { return baseUrl; }
    public int getMaxConcurrentTasks() { return maxConcurrentTasks; }
    public int getTaskTimeoutSeconds() { return taskTimeoutSeconds; }
    public int getMaxMessages() { return maxMessages; }
    public int getMaxToolCallsPerRequest() { return maxToolCallsPerRequest; }
    
    /**
     * 关闭资源
     */
    public void shutdown() {
        if (cachedExecutorService != null && !cachedExecutorService.isShutdown()) {
            cachedExecutorService.shutdown();
        }
    }
} 