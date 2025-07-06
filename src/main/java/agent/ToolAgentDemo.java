package agent;

import config.AgentConfig;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import tools.CalculatorTool;
import tools.WeatherTool;
import tools.TimeTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * LangChain4j Agent 演示类
 * 展示如何构建一个具备工具调用能力的 AI Agent
 * 使用统一的 AgentConfig 进行配置管理
 */
@Component
public class ToolAgentDemo {
    
    public interface ToolAgent {
        String chat(String userInput);
    }
    
    private final ToolAgent agent;
    private final Scanner scanner;
    private final AgentConfig agentConfig;
    
    @Autowired
    public ToolAgentDemo(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
        
        // 使用统一的配置创建模型和 Agent
        this.agent = AiServices.builder(ToolAgent.class)
                .chatModel(agentConfig.getModel())
                .tools(new CalculatorTool(), new WeatherTool(), new TimeTool())
                .build();
        
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * 启动交互式对话
     */
    public void startInteractiveChat() {
        System.out.println("🤖 AI Agent 已启动！");
        System.out.println("我可以帮你进行以下操作：");
        System.out.println("📊 数学计算（加减乘除、平方、平方根等）");
        System.out.println("🌤️ 天气查询（支持多个城市）");
        System.out.println("⏰ 时间查询（当前时间、时区转换等）");
        System.out.println("💬 一般对话和问答");
        System.out.println("输入 'quit' 或 'exit' 退出程序\n");
        
        while (true) {
            System.out.print("👤 你: ");
            String userInput = scanner.nextLine().trim();
            
            if (userInput.equalsIgnoreCase("quit") || userInput.equalsIgnoreCase("exit")) {
                System.out.println("👋 再见！");
                break;
            }
            
            if (userInput.isEmpty()) {
                continue;
            }
            
            try {
                System.out.print("🤖 Agent: ");
                String response = agent.chat(userInput);
                System.out.println(response);
            } catch (Exception e) {
                System.err.println("❌ 发生错误: " + e.getMessage());
                System.err.println("请检查你的 OpenAI API Key 是否正确设置");
            }
            System.out.println();
        }
    }
    
    /**
     * 演示预设的对话示例
     */
    public void demonstrateExamples() {
        System.out.println("🎯 演示预设对话示例：\n");
        
        String[] examples = {
            "请帮我计算 25 乘以 8 是多少？",
            "北京今天天气怎么样？",
            "现在几点了？",
            "请计算 100 的平方根",
            "上海和北京的温差大概是多少？",
            "帮我计算从 14:30 到 18:45 有多少小时"
        };
        
        for (String example : examples) {
            System.out.println("👤 用户: " + example);
            try {
                System.out.print("🤖 Agent: ");
                String response = agent.chat(example);
                System.out.println(response);
            } catch (Exception e) {
                System.err.println("❌ 错误: " + e.getMessage());
            }
            System.out.println();
        }
    }
    
    /**
     * 主方法 - 演示入口
     * 注意：由于现在使用 Spring 依赖注入，请使用 Spring Boot 启动应用
     */
    public static void main(String[] args) {
        System.out.println("⚠️  注意：ToolAgentDemo 现在使用 Spring 依赖注入");
        System.out.println("请使用以下方式启动应用：");
        System.out.println("1. 运行 main.App 类");
        System.out.println("2. 或使用命令: mvn spring-boot:run");
        System.out.println("3. 然后通过 Web 接口使用 Agent 功能");
    }
} 