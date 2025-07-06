package tools;

import dev.langchain4j.agent.tool.Tool;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 搜索工具类
 * 提供知识查询和搜索服务
 */
public class SearchTool {
    
    private final Map<String, String> knowledgeBase;
    private final Random random;
    
    public SearchTool() {
        this.knowledgeBase = new HashMap<>();
        this.random = new Random();
        
        // 初始化知识库
        initializeKnowledgeBase();
    }
    
    private void initializeKnowledgeBase() {
        // 技术知识
        knowledgeBase.put("java", "Java是一种面向对象的编程语言，由Sun Microsystems开发，现在属于Oracle公司。它具有跨平台、安全性高、性能稳定等特点。");
        knowledgeBase.put("python", "Python是一种解释型、面向对象、动态数据类型的高级程序设计语言。它语法简洁，适合初学者学习。");
        knowledgeBase.put("javascript", "JavaScript是一种具有函数优先的轻量级，解释型或即时编译型的编程语言。主要用于网页开发。");
        
        // 科学知识
        knowledgeBase.put("人工智能", "人工智能（AI）是计算机科学的一个分支，致力于创建能够执行通常需要人类智能的任务的系统。");
        knowledgeBase.put("机器学习", "机器学习是人工智能的一个子集，它使计算机能够在没有明确编程的情况下学习和改进。");
        knowledgeBase.put("深度学习", "深度学习是机器学习的一个分支，使用多层神经网络来模拟人脑的学习过程。");
        
        // 历史知识
        knowledgeBase.put("中国历史", "中国有着悠久的历史文化，从夏商周到现代，经历了数千年的发展。");
        knowledgeBase.put("世界历史", "世界历史涵盖了人类文明的发展历程，从古代文明到现代社会。");
        
        // 地理知识
        knowledgeBase.put("北京", "北京是中国的首都，政治、文化、国际交往中心，有着丰富的历史文化遗产。");
        knowledgeBase.put("上海", "上海是中国最大的经济中心，国际化大都市，金融、贸易、航运中心。");
        knowledgeBase.put("深圳", "深圳是中国改革开放的窗口，科技创新中心，现代化国际化城市。");
    }
    
    @Tool("搜索指定主题的相关信息")
    public String searchTopic(String topic) {
        System.out.println("🔍 工具调用: 搜索主题 " + topic);
        
        String lowerTopic = topic.toLowerCase();
        
        // 在知识库中查找
        for (Map.Entry<String, String> entry : knowledgeBase.entrySet()) {
            if (lowerTopic.contains(entry.getKey()) || entry.getKey().contains(lowerTopic)) {
                return "📚 搜索结果 - " + topic + ":\n" + entry.getValue();
            }
        }
        
        // 如果没有找到，返回通用信息
        return "📚 关于 " + topic + " 的信息：\n" + 
               "这是一个有趣的话题。根据我的知识库，目前没有找到关于 " + topic + " 的详细信息。\n" +
               "建议您可以尝试搜索更具体的关键词，或者我可以为您提供相关的知识链接。";
    }
    
    @Tool("搜索技术文档和教程")
    public String searchTechnicalDocs(String technology) {
        System.out.println("🔍 工具调用: 搜索技术文档 " + technology);
        
        String lowerTech = technology.toLowerCase();
        
        if (lowerTech.contains("java")) {
            return "📖 Java技术文档:\n" +
                   "• 官方文档: https://docs.oracle.com/javase/\n" +
                   "• Spring框架: https://spring.io/docs\n" +
                   "• Maven: https://maven.apache.org/guides/\n" +
                   "• 推荐书籍: 《Effective Java》、《Java核心技术》";
        } else if (lowerTech.contains("python")) {
            return "📖 Python技术文档:\n" +
                   "• 官方文档: https://docs.python.org/\n" +
                   "• Django框架: https://docs.djangoproject.com/\n" +
                   "• Flask框架: https://flask.palletsprojects.com/\n" +
                   "• 推荐书籍: 《Python编程：从入门到实践》";
        } else if (lowerTech.contains("javascript")) {
            return "📖 JavaScript技术文档:\n" +
                   "• MDN文档: https://developer.mozilla.org/zh-CN/docs/Web/JavaScript\n" +
                   "• Node.js: https://nodejs.org/docs/\n" +
                   "• React: https://reactjs.org/docs/\n" +
                   "• 推荐书籍: 《JavaScript高级程序设计》";
        } else {
            return "📖 技术文档搜索:\n" +
                   "关于 " + technology + " 的技术文档，建议访问以下资源：\n" +
                   "• 官方文档网站\n" +
                   "• GitHub项目页面\n" +
                   "• Stack Overflow社区\n" +
                   "• 技术博客和教程网站";
        }
    }
    
    @Tool("搜索编程问题和解决方案")
    public String searchProgrammingSolution(String problem) {
        System.out.println("🔍 工具调用: 搜索编程解决方案 " + problem);
        
        String lowerProblem = problem.toLowerCase();
        
        if (lowerProblem.contains("错误") || lowerProblem.contains("exception")) {
            return "🐛 编程错误解决方案:\n" +
                   "1. 检查错误日志和堆栈跟踪\n" +
                   "2. 在Stack Overflow搜索类似问题\n" +
                   "3. 查看官方文档和API参考\n" +
                   "4. 使用调试工具逐步排查\n" +
                   "5. 考虑代码审查和重构";
        } else if (lowerProblem.contains("性能") || lowerProblem.contains("优化")) {
            return "⚡ 性能优化建议:\n" +
                   "1. 使用性能分析工具\n" +
                   "2. 优化算法和数据结构\n" +
                   "3. 减少不必要的计算和内存分配\n" +
                   "4. 使用缓存和异步处理\n" +
                   "5. 考虑数据库查询优化";
        } else if (lowerProblem.contains("架构") || lowerProblem.contains("设计")) {
            return "🏗️ 软件架构设计:\n" +
                   "1. 遵循SOLID原则\n" +
                   "2. 使用设计模式\n" +
                   "3. 考虑微服务架构\n" +
                   "4. 实现松耦合高内聚\n" +
                   "5. 注重可扩展性和可维护性";
        } else {
            return "💡 编程问题解决建议:\n" +
                   "对于 " + problem + " 这个问题，建议：\n" +
                   "1. 明确问题描述和期望结果\n" +
                   "2. 搜索相关技术文档和社区讨论\n" +
                   "3. 尝试最小化复现问题\n" +
                   "4. 考虑多种解决方案并比较\n" +
                   "5. 记录解决方案供将来参考";
        }
    }
    
    @Tool("搜索最新的技术趋势和新闻")
    public String searchTechTrends(String category) {
        System.out.println("🔍 工具调用: 搜索技术趋势 " + category);
        
        String lowerCategory = category.toLowerCase();
        
        if (lowerCategory.contains("ai") || lowerCategory.contains("人工智能")) {
            return "🤖 AI技术趋势:\n" +
                   "• 大语言模型（GPT、Claude等）的快速发展\n" +
                   "• 多模态AI（文本、图像、音频）的融合\n" +
                   "• AI在医疗、教育、金融等领域的应用\n" +
                   "• 生成式AI和创意工具\n" +
                   "• AI伦理和监管的讨论";
        } else if (lowerCategory.contains("云计算") || lowerCategory.contains("cloud")) {
            return "☁️ 云计算趋势:\n" +
                   "• 多云和混合云策略\n" +
                   "• 边缘计算和5G网络\n" +
                   "• 容器化和Kubernetes\n" +
                   "• 无服务器架构（Serverless）\n" +
                   "• 云原生应用开发";
        } else if (lowerCategory.contains("区块链") || lowerCategory.contains("web3")) {
            return "🔗 区块链和Web3趋势:\n" +
                   "• DeFi（去中心化金融）的发展\n" +
                   "• NFT和数字艺术\n" +
                   "• 元宇宙概念\n" +
                   "• 区块链在供应链中的应用\n" +
                   "• 加密货币和数字资产";
        } else {
            return "📈 技术趋势概览:\n" +
                   "当前主要技术趋势包括：\n" +
                   "• 人工智能和机器学习\n" +
                   "• 云计算和边缘计算\n" +
                   "• 区块链和Web3\n" +
                   "• 物联网（IoT）\n" +
                   "• 5G和通信技术\n" +
                   "• 可持续技术和绿色计算";
        }
    }
} 