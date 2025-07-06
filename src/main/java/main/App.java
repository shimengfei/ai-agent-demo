package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot 应用主类
 * AI Agent Demo 启动入口
 */
@SpringBootApplication
@ComponentScan(basePackages = {"agent", "tools", "config", "controller", "service"})
public class App {
    
    public static void main(String[] args) {
        System.out.println("🚀 AI Agent Demo 启动中...");
        System.out.println("================================");
        
        // 检查环境变量
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("❌ 错误: 未设置 OPENAI_API_KEY 环境变量");
            System.err.println("请按以下步骤设置：");
            System.err.println("1. 获取你的 OpenAI API Key");
            System.err.println("2. 设置环境变量: export OPENAI_API_KEY=你的API密钥");
            System.err.println("3. 重新运行程序");
            System.exit(1);
        }
        
        System.out.println("✅ OpenAI API Key 已配置");
        System.out.println("🌐 启动 Web 服务...");
        
        // 启动 Spring Boot 应用
        SpringApplication.run(App.class, args);
        
        System.out.println("🎉 AI Agent Demo 启动完成！");
        System.out.println("📱 访问地址: http://localhost:8080");
        System.out.println("🔧 API 文档: http://localhost:8080/swagger-ui.html");
    }
} 