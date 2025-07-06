package agent;

import config.AgentConfig;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import service.WebSocketService;
import tools.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 增强的 Agent 编排器
 * 支持复杂任务编排、工作流管理、实时状态跟踪
 * 使用统一的 AgentConfig 进行配置管理
 */
@Component
public class EnhancedAgentOrchestrator {
    
    private final ExecutorService executorService;
    private final Map<String, SpecializedAgent> agents;
    private final OpenAiChatModel model;
    private final Map<String, TaskExecution> activeTasks;
    private final AtomicInteger taskCounter;
    private final AgentConfig agentConfig;
    private final WebSocketService webSocketService;
    
    @Autowired
    public EnhancedAgentOrchestrator(AgentConfig agentConfig, WebSocketService webSocketService) {
        this.agentConfig = agentConfig;
        this.executorService = agentConfig.getExecutorService();
        this.model = agentConfig.getModel();
        
        this.agents = new ConcurrentHashMap<>();
        this.activeTasks = new ConcurrentHashMap<>();
        this.taskCounter = new AtomicInteger(0);
        this.webSocketService = webSocketService;
        
        initializeAgents();
    }
    
    private void initializeAgents() {
        // 数学计算 Agent
        agents.put("calculator", new SpecializedAgent(
            "calculator",
            "数学计算专家，专门处理各种数学运算和计算问题",
            AiServices.builder(CalculatorAgent.class)
                .chatModel(model)
                .tools(new CalculatorTool())
                .build()
        ));
        
        // 天气查询 Agent
        agents.put("weather", new SpecializedAgent(
            "weather",
            "天气信息专家，提供全球各地的天气、温度、湿度等信息",
            AiServices.builder(WeatherAgent.class)
                .chatModel(model)
                .tools(new WeatherTool())
                .build()
        ));
        
        // 时间管理 Agent
        agents.put("time", new SpecializedAgent(
            "time",
            "时间管理专家，处理时间查询、时区转换、时间计算等",
            AiServices.builder(TimeAgent.class)
                .chatModel(model)
                .tools(new TimeTool())
                .build()
        ));
        
        // 搜索 Agent
        agents.put("search", new SpecializedAgent(
            "search",
            "信息搜索专家，提供知识查询和搜索服务",
            AiServices.builder(SearchAgent.class)
                .chatModel(model)
                .tools(new SearchTool())
                .build()
        ));
        
        // 翻译 Agent
        agents.put("translator", new SpecializedAgent(
            "translator",
            "语言翻译专家，支持多语言翻译和语言处理",
            AiServices.builder(TranslationAgent.class)
                .chatModel(model)
                .tools(new TranslationTool())
                .build()
        ));
        
        // 文件操作 Agent
        agents.put("file", new SpecializedAgent(
            "file",
            "文件操作专家，处理文件读写、目录管理、文件搜索等",
            AiServices.builder(FileAgent.class)
                .chatModel(model)
                .tools(new FileTool())
                .build()
        ));
    }
    
    /**
     * 提交复杂任务
     */
    public TaskExecution submitTask(String userInput) {
        String taskId = "task-" + taskCounter.incrementAndGet();
        TaskExecution task = new TaskExecution(taskId, userInput);
        activeTasks.put(taskId, task);
        
        // 异步执行任务
        CompletableFuture.runAsync(() -> {
            try {
                executeTask(task);
            } catch (Exception e) {
                task.updateStatus("FAILED", "任务执行失败: " + e.getMessage());
            }
        }, executorService);
        
        return task;
    }
    
    /**
     * 执行任务
     */
    private void executeTask(TaskExecution task) {
        task.updateStatus("ANALYZING", "正在分析任务...");
        webSocketService.pushTaskUpdate(task);
        
        // 分析任务类型
        TaskAnalysis analysis = analyzeTask(task.getUserInput());
        task.addLog("任务分析完成: " + analysis.getDescription());
        
        if (analysis.getRequiredAgents().isEmpty()) {
            task.updateStatus("FAILED", "无法识别任务类型");
            webSocketService.pushTaskFailed(task);
            return;
        }
        
        task.updateStatus("EXECUTING", "正在执行任务...");
        webSocketService.pushTaskUpdate(task);
        
        // 执行任务
        String result;
        if (analysis.getRequiredAgents().size() == 1) {
            result = executeSingleAgentTask(analysis.getRequiredAgents().get(0), task.getUserInput(), task);
        } else {
            result = executeMultiAgentTask(analysis, task.getUserInput(), task);
        }
        
        task.setResult(result);
        task.updateStatus("COMPLETED", "任务执行完成");
        webSocketService.pushTaskCompleted(task);
    }
    
    /**
     * 分析任务类型和需要的 Agent
     */
    private TaskAnalysis analyzeTask(String userInput) {
        String analysisPrompt = String.format("""
            分析以下用户输入，确定需要哪些专业Agent来处理：
            用户输入: %s
            
            可用的Agent类型：
            - calculator: 数学计算
            - weather: 天气查询
            - time: 时间管理
            - search: 信息搜索
            - translator: 语言翻译
            - file: 文件操作
            
            请返回JSON格式：
            {
                "description": "任务描述",
                "requiredAgents": ["agent1", "agent2"],
                "executionOrder": ["agent1", "agent2"],
                "collaborationType": "sequential|parallel"
            }
            """, userInput);
        
        try {
            String response = model.chat(UserMessage.from(analysisPrompt)).aiMessage().text();
            return parseTaskAnalysis(response, userInput);
        } catch (Exception e) {
            return new TaskAnalysis("任务分析失败", Arrays.asList("search"), "sequential");
        }
    }
    
    private TaskAnalysis parseTaskAnalysis(String response, String userInput) {
        String lowerInput = userInput.toLowerCase();
        
        if (lowerInput.contains("计算") || lowerInput.contains("数学") || lowerInput.contains("+") || lowerInput.contains("-") || lowerInput.contains("*") || lowerInput.contains("/")) {
            return new TaskAnalysis("数学计算任务", Arrays.asList("calculator"), "sequential");
        } else if (lowerInput.contains("天气") || lowerInput.contains("温度") || lowerInput.contains("下雨")) {
            return new TaskAnalysis("天气查询任务", Arrays.asList("weather"), "sequential");
        } else if (lowerInput.contains("时间") || lowerInput.contains("几点") || lowerInput.contains("时区")) {
            return new TaskAnalysis("时间管理任务", Arrays.asList("time"), "sequential");
        } else if (lowerInput.contains("翻译") || lowerInput.contains("英文") || lowerInput.contains("中文")) {
            return new TaskAnalysis("翻译任务", Arrays.asList("translator"), "sequential");
        } else if (lowerInput.contains("文件") || lowerInput.contains("读取") || lowerInput.contains("写入") || lowerInput.contains("目录")) {
            return new TaskAnalysis("文件操作任务", Arrays.asList("file"), "sequential");
        } else if (lowerInput.contains("搜索") || lowerInput.contains("查询") || lowerInput.contains("信息")) {
            return new TaskAnalysis("信息搜索任务", Arrays.asList("search"), "sequential");
        } else {
            // 复杂任务，可能需要多个Agent协作
            List<String> requiredAgents = new ArrayList<>();
            if (lowerInput.contains("计算") || lowerInput.contains("数学")) requiredAgents.add("calculator");
            if (lowerInput.contains("天气")) requiredAgents.add("weather");
            if (lowerInput.contains("时间")) requiredAgents.add("time");
            if (lowerInput.contains("翻译")) requiredAgents.add("translator");
            if (lowerInput.contains("文件")) requiredAgents.add("file");
            if (lowerInput.contains("搜索") || requiredAgents.isEmpty()) requiredAgents.add("search");
            
            return new TaskAnalysis("多Agent协作任务", requiredAgents, "parallel");
        }
    }
    
    /**
     * 执行单一 Agent 任务
     */
    private String executeSingleAgentTask(String agentType, String userInput, TaskExecution task) {
        SpecializedAgent agent = agents.get(agentType);
        if (agent == null) {
            return "❌ 未找到合适的Agent: " + agentType;
        }
        
        task.addLog("调用 " + agent.getName() + " Agent");
        try {
            String result = agent.execute(userInput);
            task.addLog("Agent执行完成");
            return result;
        } catch (Exception e) {
            task.addLog("Agent执行失败: " + e.getMessage());
            return "❌ Agent执行失败: " + e.getMessage();
        }
    }
    
    /**
     * 执行多 Agent 协作任务
     */
    private String executeMultiAgentTask(TaskAnalysis analysis, String userInput, TaskExecution task) {
        task.addLog("启动多Agent协作模式");
        
        if ("parallel".equals(analysis.getCollaborationType())) {
            return executeParallelTask(analysis, userInput, task);
        } else {
            return executeSequentialTask(analysis, userInput, task);
        }
    }
    
    /**
     * 并行执行任务
     */
    private String executeParallelTask(TaskAnalysis analysis, String userInput, TaskExecution task) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        for (String agentType : analysis.getRequiredAgents()) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                task.addLog("并行执行 " + agentType + " Agent");
                return executeSingleAgentTask(agentType, userInput, task);
            }, executorService);
            futures.add(future);
        }
        
        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        try {
            allFutures.get(30, TimeUnit.SECONDS); // 30秒超时
            
            StringBuilder result = new StringBuilder();
            result.append("🤝 多Agent并行协作结果:\n\n");
            
            for (int i = 0; i < analysis.getRequiredAgents().size(); i++) {
                String agentType = analysis.getRequiredAgents().get(i);
                String agentResult = futures.get(i).get();
                result.append(String.format("【%s】\n%s\n\n", agentType, agentResult));
            }
            
            return result.toString();
        } catch (Exception e) {
            task.addLog("并行执行失败: " + e.getMessage());
            return "❌ 并行执行失败: " + e.getMessage();
        }
    }
    
    /**
     * 顺序执行任务
     */
    private String executeSequentialTask(TaskAnalysis analysis, String userInput, TaskExecution task) {
        StringBuilder result = new StringBuilder();
        result.append("🔄 多Agent顺序协作结果:\n\n");
        
        String currentInput = userInput;
        
        for (String agentType : analysis.getRequiredAgents()) {
            task.addLog("顺序执行 " + agentType + " Agent");
            String agentResult = executeSingleAgentTask(agentType, currentInput, task);
            result.append(String.format("【%s】\n%s\n\n", agentType, agentResult));
            
            // 将当前结果作为下一个Agent的输入
            currentInput = agentResult;
        }
        
        return result.toString();
    }
    
    /**
     * 获取任务状态
     */
    public TaskExecution getTaskStatus(String taskId) {
        return activeTasks.get(taskId);
    }
    
    /**
     * 获取所有活跃任务
     */
    public List<TaskExecution> getAllActiveTasks() {
        return new ArrayList<>(activeTasks.values());
    }
    
    /**
     * 获取可用Agent列表
     */
    public Map<String, String> getAvailableAgents() {
        Map<String, String> agentInfo = new HashMap<>();
        agents.forEach((key, agent) -> 
            agentInfo.put(key, agent.getDescription())
        );
        return agentInfo;
    }
    
    /**
     * 关闭编排器
     */
    public void shutdown() {
        agentConfig.shutdown();
    }
    
    // Agent 接口定义
    public interface CalculatorAgent {
        String chat(String userInput);
    }
    
    public interface WeatherAgent {
        String chat(String userInput);
    }
    
    public interface TimeAgent {
        String chat(String userInput);
    }
    
    public interface SearchAgent {
        String chat(String userInput);
    }
    
    public interface TranslationAgent {
        String chat(String userInput);
    }
    
    public interface FileAgent {
        String chat(String userInput);
    }
    
    // 内部类
    private static class TaskAnalysis {
        private final String description;
        private final List<String> requiredAgents;
        private final String collaborationType;
        
        public TaskAnalysis(String description, List<String> requiredAgents, String collaborationType) {
            this.description = description;
            this.requiredAgents = requiredAgents;
            this.collaborationType = collaborationType;
        }
        
        public String getDescription() { return description; }
        public List<String> getRequiredAgents() { return requiredAgents; }
        public String getCollaborationType() { return collaborationType; }
    }
    
    private static class SpecializedAgent {
        private final String name;
        private final String description;
        private final Object agentInstance;
        
        public SpecializedAgent(String name, String description, Object agentInstance) {
            this.name = name;
            this.description = description;
            this.agentInstance = agentInstance;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        
        public String execute(String input) throws Exception {
            // 通过反射调用chat方法
            return (String) agentInstance.getClass()
                .getMethod("chat", String.class)
                .invoke(agentInstance, input);
        }
    }
} 