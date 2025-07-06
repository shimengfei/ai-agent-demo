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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
     * 提交复杂任务（LLM驱动的多Agent规划与执行）
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
     * LLM驱动的任务执行，按steps顺序/并行调度Agent
     */
    private void executeTask(TaskExecution task) {
        task.updateStatus("ANALYZING", "正在分析任务...");
        webSocketService.pushTaskUpdate(task);
        
        LlmTaskPlan plan = analyzeTaskWithLLM(task.getUserInput());
        task.addLog("任务分析完成: " + (plan.description != null ? plan.description : "LLM任务规划"));
        
        if (plan.steps == null || plan.steps.isEmpty()) {
            task.updateStatus("FAILED", "无法识别任务类型");
            webSocketService.pushTaskFailed(task);
            return;
        }
        
        task.updateStatus("EXECUTING", "正在执行任务...");
        webSocketService.pushTaskUpdate(task);
        
        String result;
        if (hasComplexDependencies(plan.steps)) {
            result = executeDagSteps(plan.steps, task);
        } else if ("parallel".equalsIgnoreCase(plan.collaboration)) {
            result = executeParallelSteps(plan.steps, task);
        } else {
            result = executeSequentialSteps(plan.steps, task);
        }
        
        task.setResult(result);
        task.updateStatus("COMPLETED", "任务执行完成");
        webSocketService.pushTaskCompleted(task);
    }
    
    /**
     * 检查是否有复杂依赖关系
     */
    private boolean hasComplexDependencies(List<LlmTaskStep> steps) {
        return steps.stream().anyMatch(step -> step.depends_on != null && !step.depends_on.isEmpty());
    }
    
    /**
     * DAG调度执行，支持复杂依赖关系
     */
    private String executeDagSteps(List<LlmTaskStep> steps, TaskExecution task) {
        Map<Integer, String> stepResults = new HashMap<>();
        Map<Integer, LlmTaskStep> stepMap = steps.stream().collect(Collectors.toMap(s -> s.id, s -> s));
        Set<Integer> executed = new HashSet<>();
        StringBuilder result = new StringBuilder();
        
        task.addLog("开始DAG调度执行，共 " + steps.size() + " 个步骤");
        
        while (executed.size() < steps.size()) {
            boolean progress = false;
            for (LlmTaskStep step : steps) {
                if (executed.contains(step.id)) continue;
                
                // 检查依赖是否满足
                if (step.depends_on == null || step.depends_on.stream().allMatch(executed::contains)) {
                    task.addLog(String.format("执行步骤 %d: %s (%s)", step.id, step.agent, step.action));
                    
                    // 填充依赖结果到参数中
                    Map<String, Object> params = new HashMap<>(step.params != null ? step.params : new HashMap<>());
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        if (entry.getValue() instanceof String) {
                            String value = (String) entry.getValue();
                            if (value.startsWith("step:")) {
                                try {
                                    int depId = Integer.parseInt(value.substring(5));
                                    String depResult = stepResults.get(depId);
                                    if (depResult != null) {
                                        params.put(entry.getKey(), depResult);
                                    }
                                } catch (NumberFormatException e) {
                                    // 忽略无效的step引用
                                }
                            }
                        }
                    }
                    
                    String stepResult = executeAgentStep(step, params, task);
                    stepResults.put(step.id, stepResult);
                    result.append(String.format("【步骤%d - %s】\n%s\n\n", step.id, step.agent, stepResult));
                    executed.add(step.id);
                    progress = true;
                }
            }
            
            if (!progress) {
                task.addLog("❌ 检测到循环依赖，无法继续执行");
                return "❌ 检测到循环依赖，无法继续执行";
            }
        }
        
        task.addLog("🎉 DAG调度执行完成");
        return result.toString();
    }
    
    /**
     * 顺序执行steps，支持上一步结果依赖
     */
    private String executeSequentialSteps(List<LlmTaskStep> steps, TaskExecution task) {
        StringBuilder result = new StringBuilder();
        String prevResult = null;
        for (int i = 0; i < steps.size(); i++) {
            LlmTaskStep step = steps.get(i);
            Map<String, Object> params = new HashMap<>(step.params != null ? step.params : new HashMap<>());
            
            // 处理"上一步结果"依赖
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof String) {
                    String value = (String) entry.getValue();
                    if (value.contains("上一步结果") && prevResult != null) {
                        params.put(entry.getKey(), value.replace("上一步结果", prevResult));
                    }
                }
            }
            
            task.addLog(String.format("顺序执行第%d步: %s (%s)", i + 1, step.agent, step.action));
            String stepResult = executeAgentStep(step, params, task);
            result.append(String.format("【%s】\n%s\n\n", step.agent, stepResult));
            prevResult = stepResult;
        }
        return result.toString();
    }
    
    /**
     * 并行执行steps
     */
    private String executeParallelSteps(List<LlmTaskStep> steps, TaskExecution task) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            LlmTaskStep step = steps.get(i);
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                Map<String, Object> params = new HashMap<>(step.params != null ? step.params : new HashMap<>());
                task.addLog(String.format("并行执行: %s (%s)", step.agent, step.action));
                return executeAgentStep(step, params, task);
            }, executorService);
            futures.add(future);
        }
        StringBuilder result = new StringBuilder();
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
            for (int i = 0; i < steps.size(); i++) {
                result.append(String.format("【%s】\n%s\n\n", steps.get(i).agent, futures.get(i).get()));
            }
        } catch (Exception e) {
            task.addLog("并行执行失败: " + e.getMessage());
            return "❌ 并行执行失败: " + e.getMessage();
        }
        return result.toString();
    }
    
    /**
     * 按step action/参数调用Agent（支持多方法）
     */
    private String executeAgentStep(LlmTaskStep step, Map<String, Object> params, TaskExecution task) {
        SpecializedAgent agent = agents.get(step.agent);
        if (agent == null) {
            task.addLog("❌ 未找到合适的Agent: " + step.agent);
            return "❌ 未找到合适的Agent: " + step.agent;
        }
        try {
            // 支持多方法调用
            return agent.execute(step.action, params);
        } catch (Exception e) {
            task.addLog("Agent执行失败: " + e.getMessage());
            return "❌ Agent执行失败: " + e.getMessage();
        }
    }
    
    /**
     * LLM结构化意图解析，返回完整plan（支持复杂参数和依赖）
     */
    private LlmTaskPlan analyzeTaskWithLLM(String userInput) {
        String prompt = String.format("""
            分析用户输入，输出JSON格式的任务规划。用户输入: %s
            
            Agent类型: calculator(数学), weather(天气), time(时间), search(搜索), translator(翻译), file(文件)
            
            输出格式示例:
            {
              "description": "任务描述",
              "steps": [
                {"id": 1, "agent": "calculator", "action": "calculate", "params": {"expression": "25*8"}},
                {"id": 2, "agent": "translator", "action": "translate", "params": {"text": "step:1", "target_language": "英文"}, "depends_on": [1]}
              ],
              "collaboration": "sequential"
            }
            
            只输出JSON，不要其他内容。
            """, userInput);
        try {
            String response = model.chat(UserMessage.from(prompt)).aiMessage().text();
            // 尝试提取JSON部分
            String jsonStr = extractJsonFromResponse(response);
            return objectMapper.readValue(jsonStr, LlmTaskPlan.class);
        } catch (Exception e) {
            // fallback: 兜底为search
            LlmTaskPlan fallback = new LlmTaskPlan();
            fallback.description = "任务分析失败";
            LlmTaskStep step = new LlmTaskStep();
            step.id = 1;
            step.agent = "search";
            step.action = "search";
            step.params = new HashMap<>();
            step.params.put("query", userInput);
            fallback.steps = List.of(step);
            fallback.collaboration = "sequential";
            return fallback;
        }
    }
    
    /**
     * 从LLM响应中提取JSON字符串
     */
    private String extractJsonFromResponse(String response) {
        // 查找第一个 { 和最后一个 }
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
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
    
    // LLM结构化输出的任务规划对象（升级版）
    private static class LlmTaskPlan {
        public String description;
        public List<LlmTaskStep> steps;
        public String collaboration;
    }
    
    private static class LlmTaskStep {
        public int id;
        public String agent;
        public String action;
        public Map<String, Object> params; // 支持任意参数
        public List<Integer> depends_on; // 支持依赖关系
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
        
        public String execute(String action, Map<String, Object> params) throws Exception {
            try {
                // 尝试调用带action和params的方法
                return (String) agentInstance.getClass()
                    .getMethod("chat", String.class, Map.class)
                    .invoke(agentInstance, action, params);
            } catch (NoSuchMethodException e) {
                // 如果方法不存在，fallback到原来的chat(String)方法
                // 将params转换为字符串输入
                String input = buildInputFromParams(action, params);
                return (String) agentInstance.getClass()
                    .getMethod("chat", String.class)
                    .invoke(agentInstance, input);
            }
        }
        
        private String buildInputFromParams(String action, Map<String, Object> params) {
            if (params == null || params.isEmpty()) {
                return action;
            }
            
            StringBuilder input = new StringBuilder();
            input.append("Action: ").append(action).append("\n");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                input.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            return input.toString();
        }
    }
} 