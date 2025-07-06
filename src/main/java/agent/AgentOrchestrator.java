package agent;

import config.AgentConfig;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.message.UserMessage;
import tools.CalculatorTool;
import tools.WeatherTool;
import tools.TimeTool;
import tools.SearchTool;
import tools.TranslationTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Agent 编排器
 * 负责协调多个专业 Agent 之间的任务分配和协作
 * 使用统一的 AgentConfig 进行配置管理
 */
@Component
public class AgentOrchestrator {
    
    private final ExecutorService executorService;
    private final Map<String, SpecializedAgent> agents;
    private final OpenAiChatModel model;
    private final AgentConfig agentConfig;
    
    @Autowired
    public AgentOrchestrator(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
        this.executorService = agentConfig.getExecutorService();
        this.model = agentConfig.getModel();
        
        // 初始化各种专业 Agent
        this.agents = new HashMap<>();
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
    }
    
    /**
     * 处理复杂任务，自动分配给合适的 Agent
     */
    public String processComplexTask(String userInput) {
        System.out.println("🎯 任务分析中...");
        
        // 分析任务类型
        TaskAnalysis analysis = analyzeTask(userInput);
        System.out.println("📋 任务分析结果: " + analysis.getDescription());
        
        if (analysis.getRequiredAgents().isEmpty()) {
            return "❌ 无法识别任务类型，请提供更明确的指令";
        }
        
        // 执行任务
        if (analysis.getRequiredAgents().size() == 1) {
            // 单一 Agent 任务
            String agentType = analysis.getRequiredAgents().get(0);
            return executeSingleAgentTask(agentType, userInput);
        } else {
            // 多 Agent 协作任务
            return executeMultiAgentTask(analysis, userInput);
        }
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
            // 简化处理，实际项目中应该解析JSON
            return parseTaskAnalysis(response, userInput);
        } catch (Exception e) {
            return new TaskAnalysis("任务分析失败", Arrays.asList("calculator"), "sequential");
        }
    }
    
    private TaskAnalysis parseTaskAnalysis(String response, String userInput) {
        // 简化的任务分析逻辑
        String lowerInput = userInput.toLowerCase();
        
        if (lowerInput.contains("计算") || lowerInput.contains("数学") || lowerInput.contains("+") || lowerInput.contains("-") || lowerInput.contains("*") || lowerInput.contains("/")) {
            return new TaskAnalysis("数学计算任务", Arrays.asList("calculator"), "sequential");
        } else if (lowerInput.contains("天气") || lowerInput.contains("温度") || lowerInput.contains("下雨")) {
            return new TaskAnalysis("天气查询任务", Arrays.asList("weather"), "sequential");
        } else if (lowerInput.contains("时间") || lowerInput.contains("几点") || lowerInput.contains("时区")) {
            return new TaskAnalysis("时间管理任务", Arrays.asList("time"), "sequential");
        } else if (lowerInput.contains("翻译") || lowerInput.contains("英文") || lowerInput.contains("中文")) {
            return new TaskAnalysis("翻译任务", Arrays.asList("translator"), "sequential");
        } else if (lowerInput.contains("搜索") || lowerInput.contains("查询") || lowerInput.contains("信息")) {
            return new TaskAnalysis("信息搜索任务", Arrays.asList("search"), "sequential");
        } else {
            // 复杂任务，可能需要多个Agent协作
            List<String> requiredAgents = new ArrayList<>();
            if (lowerInput.contains("计算") || lowerInput.contains("数学")) requiredAgents.add("calculator");
            if (lowerInput.contains("天气")) requiredAgents.add("weather");
            if (lowerInput.contains("时间")) requiredAgents.add("time");
            if (lowerInput.contains("翻译")) requiredAgents.add("translator");
            if (lowerInput.contains("搜索") || requiredAgents.isEmpty()) requiredAgents.add("search");
            
            return new TaskAnalysis("多Agent协作任务", requiredAgents, "parallel");
        }
    }
    
    /**
     * 执行单一 Agent 任务
     */
    private String executeSingleAgentTask(String agentType, String userInput) {
        SpecializedAgent agent = agents.get(agentType);
        if (agent == null) {
            return "❌ 未找到合适的Agent: " + agentType;
        }
        
        System.out.println("🤖 调用 " + agent.getName() + " Agent");
        try {
            return agent.execute(userInput);
        } catch (Exception e) {
            return "❌ Agent执行失败: " + e.getMessage();
        }
    }
    
    /**
     * 执行多 Agent 协作任务
     */
    private String executeMultiAgentTask(TaskAnalysis analysis, String userInput) {
        System.out.println("🤝 启动多Agent协作模式");
        
        if ("parallel".equals(analysis.getCollaborationType())) {
            return executeParallelTask(analysis, userInput);
        } else {
            return executeSequentialTask(analysis, userInput);
        }
    }
    
    /**
     * 并行执行任务
     */
    private String executeParallelTask(TaskAnalysis analysis, String userInput) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        for (String agentType : analysis.getRequiredAgents()) {
            SpecializedAgent agent = agents.get(agentType);
            if (agent != null) {
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        System.out.println("🔄 " + agent.getName() + " 开始处理...");
                        return agent.execute(userInput);
                    } catch (Exception e) {
                        return "❌ " + agent.getName() + " 执行失败: " + e.getMessage();
                    }
                }, executorService);
                futures.add(future);
            }
        }
        
        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        try {
            allFutures.get(); // 等待所有任务完成
            
            // 整合结果
            StringBuilder result = new StringBuilder("🤝 多Agent协作结果:\n\n");
            for (int i = 0; i < analysis.getRequiredAgents().size(); i++) {
                String agentType = analysis.getRequiredAgents().get(i);
                String agentResult = futures.get(i).get();
                result.append("📋 ").append(agents.get(agentType).getName()).append(":\n");
                result.append(agentResult).append("\n\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "❌ 多Agent协作失败: " + e.getMessage();
        }
    }
    
    /**
     * 顺序执行任务
     */
    private String executeSequentialTask(TaskAnalysis analysis, String userInput) {
        StringBuilder result = new StringBuilder("🔄 顺序执行多Agent任务:\n\n");
        String currentInput = userInput;
        
        for (String agentType : analysis.getRequiredAgents()) {
            SpecializedAgent agent = agents.get(agentType);
            if (agent != null) {
                try {
                    System.out.println("🔄 " + agent.getName() + " 开始处理...");
                    String agentResult = agent.execute(currentInput);
                    result.append("📋 ").append(agent.getName()).append(" 结果:\n");
                    result.append(agentResult).append("\n\n");
                    
                    // 将当前结果作为下一个Agent的输入
                    currentInput = agentResult;
                } catch (Exception e) {
                    result.append("❌ ").append(agent.getName()).append(" 执行失败: ").append(e.getMessage()).append("\n\n");
                }
            }
        }
        
        return result.toString();
    }
    
    /**
     * 获取所有可用的 Agent 信息
     */
    public String getAvailableAgents() {
        StringBuilder info = new StringBuilder("🤖 可用的专业Agent:\n\n");
        for (SpecializedAgent agent : agents.values()) {
            info.append("• ").append(agent.getName()).append(": ").append(agent.getDescription()).append("\n");
        }
        return info.toString();
    }
    
    /**
     * 关闭资源
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
    
    /**
     * 任务分析结果
     */
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
    
    /**
     * 专业 Agent 包装类
     */
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
            // 通过反射调用 chat 方法
            return (String) agentInstance.getClass().getMethod("chat", String.class).invoke(agentInstance, input);
        }
    }
} 