# AI Agent Demo - 智能代理编排系统

一个基于 Spring Boot 和 LangChain4j 的智能代理编排系统，支持多Agent协作、任务编排和实时状态监控。

## 🚀 功能特性

### 核心功能
- **多Agent协作**: 支持6种专业Agent（计算、天气、时间、搜索、翻译、文件操作）
- **智能任务编排**: 自动分析任务类型并分配给合适的Agent
- **并行/顺序执行**: 支持多Agent并行协作和顺序协作
- **实时状态监控**: WebSocket实时推送任务执行状态
- **现代化Web界面**: 响应式设计，支持实时交互

### 支持的Agent类型
- 🤖 **计算Agent**: 数学运算、公式计算
- 🌤️ **天气Agent**: 全球天气查询、温度湿度信息
- ⏰ **时间Agent**: 时间查询、时区转换、时间计算
- 🔍 **搜索Agent**: 知识查询、信息搜索
- 🌍 **翻译Agent**: 多语言翻译、语言检测
- 📁 **文件Agent**: 文件读写、目录管理、文件搜索

### 技术栈
- **后端**: Spring Boot 2.7.18, LangChain4j 1.0.1
- **前端**: Bootstrap 5, Font Awesome, WebSocket
- **通信**: REST API, WebSocket (STOMP)
- **构建**: Maven

## 📦 快速开始

### 环境要求
- Java 8+
- Maven 3.6+
- OpenAI API Key

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd ai-agent-demo
   ```

2. **设置环境变量**
   ```bash
   export OPENAI_API_KEY=你的OpenAI_API密钥
   ```

3. **编译运行**
   ```bash
   mvn clean package
   java -jar target/ai-agent-demo-langchain4j-1.0-SNAPSHOT.jar
   ```

4. **访问应用**
   - 前端界面: http://localhost:8080
   - API文档: http://localhost:8080/swagger-ui.html
   - 健康检查: http://localhost:8080/actuator/health

## 🎯 使用示例

### 基础任务
- 数学计算: "请帮我计算 25 乘以 8 是多少？"
- 天气查询: "北京今天天气怎么样？"
- 时间查询: "现在几点了？"
- 翻译任务: "请将'Hello World'翻译成中文"
- 文件操作: "创建一个名为test.txt的文件，内容为'Hello Agent'"
- 信息搜索: "搜索关于人工智能的最新信息"

### 复杂任务（多Agent协作）
- "查询北京天气，然后计算今天的温度比昨天高多少度"
- "搜索AI相关信息，翻译成英文，并保存到文件"
- "计算当前时间到明天早上8点还有多少小时"

## 🔧 API接口

### 任务管理
- `POST /api/agent/task` - 提交新任务
- `GET /api/agent/task/{taskId}` - 获取任务状态
- `GET /api/agent/tasks` - 获取所有任务
- `GET /api/agent/agents` - 获取可用Agent列表
- `GET /api/agent/health` - 健康检查

### WebSocket事件
- `/topic/task-update` - 任务状态更新
- `/topic/task-completed` - 任务完成通知
- `/topic/task-failed` - 任务失败通知
- `/topic/system` - 系统消息

## 🏗️ 项目结构

```
src/main/java/
├── agent/
│   ├── EnhancedAgentOrchestrator.java  # 增强的Agent编排器
│   ├── TaskExecution.java              # 任务执行状态
│   └── ToolAgentDemo.java              # 原始演示类
├── tools/
│   ├── CalculatorTool.java             # 计算工具
│   ├── WeatherTool.java                # 天气工具
│   ├── TimeTool.java                   # 时间工具
│   ├── SearchTool.java                 # 搜索工具
│   ├── TranslationTool.java            # 翻译工具
│   └── FileTool.java                   # 文件工具
├── controller/
│   └── AgentController.java            # REST API控制器
├── service/
│   └── WebSocketService.java           # WebSocket服务
├── config/
│   └── WebSocketConfig.java            # WebSocket配置
└── main/
    └── App.java                        # 应用主类
```

## 🔄 任务执行流程

1. **任务提交**: 用户通过Web界面或API提交任务
2. **任务分析**: 系统自动分析任务类型和所需Agent
3. **Agent分配**: 根据分析结果分配合适的Agent
4. **任务执行**: 单Agent执行或多Agent协作执行
5. **状态跟踪**: 实时更新任务执行状态和日志
6. **结果返回**: 返回执行结果和详细日志

## 🎨 界面特性

- **响应式设计**: 支持桌面和移动设备
- **实时更新**: WebSocket实时推送任务状态
- **任务卡片**: 清晰展示任务信息和执行状态
- **执行日志**: 详细的任务执行过程记录
- **示例任务**: 快速体验各种功能
- **状态指示**: 直观的任务状态标识

## 🔧 配置选项

在 `application.yml` 中可以配置：

```yaml
app:
  agent:
    max-concurrent-tasks: 10        # 最大并发任务数
    task-timeout-seconds: 300       # 任务超时时间
    enable-websocket: true          # 启用WebSocket
```

## 🚀 部署

### Docker部署
```bash
# 构建镜像
docker build -t ai-agent-demo .

# 运行容器
docker run -p 8080:8080 -e OPENAI_API_KEY=你的密钥 ai-agent-demo
```

### 生产环境
- 建议使用反向代理（如Nginx）
- 配置HTTPS
- 设置适当的内存和CPU限制
- 配置日志收集和监控

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

MIT License

## 🔗 相关链接

- [LangChain4j](https://github.com/langchain4j/langchain4j)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [OpenAI API](https://platform.openai.com/docs)
