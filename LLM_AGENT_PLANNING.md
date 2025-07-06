# LLM驱动的多Agent规划系统

## 概述

本系统已升级为**LLM驱动的智能多Agent规划系统**，支持：
- **复杂参数传递**：每个Agent调用支持任意参数Map
- **Action多分支**：每个Agent支持多个action方法
- **依赖图调度**：支持复杂的DAG依赖关系
- **智能意图识别**：LLM自动分析用户意图并规划执行步骤

## 核心特性

### 1. 复杂参数支持
每个step支持任意参数Map，LLM可输出：
```json
{
  "id": 1,
  "agent": "calculator",
  "action": "calculate",
  "params": {
    "expression": "25*8",
    "precision": 2,
    "format": "decimal"
  }
}
```

### 2. Action多分支
每个Agent支持多个action：
- calculator: calculate, evaluate, solve
- weather: query, forecast, history
- translator: translate, detect, convert
- file: read, write, list, delete
- search: search, filter, sort
- time: current, convert, calculate

### 3. 依赖图调度
支持复杂的依赖关系：
```json
{
  "steps": [
    {"id": 1, "agent": "calculator", "action": "calculate", "params": {"expression": "25*8"}},
    {"id": 2, "agent": "weather", "action": "query", "params": {"location": "北京"}},
    {"id": 3, "agent": "translator", "action": "translate", "params": {"text": "step:1", "target_language": "英文"}, "depends_on": [1]},
    {"id": 4, "agent": "file", "action": "write", "params": {"content": "step:3", "filename": "result.txt"}, "depends_on": [3]}
  ]
}
```

## 使用示例

### 示例1：简单顺序执行
```
输入：计算100的平方根，然后翻译成英文
```
LLM输出：
```json
{
  "description": "数学计算后翻译",
  "steps": [
    {"id": 1, "agent": "calculator", "action": "calculate", "params": {"expression": "sqrt(100)"}},
    {"id": 2, "agent": "translator", "action": "translate", "params": {"text": "step:1", "target_language": "英文"}, "depends_on": [1]}
  ],
  "collaboration": "sequential"
}
```

### 示例2：并行执行
```
输入：同时计算25乘以8和查询北京天气
```
LLM输出：
```json
{
  "description": "并行计算和天气查询",
  "steps": [
    {"id": 1, "agent": "calculator", "action": "calculate", "params": {"expression": "25*8"}},
    {"id": 2, "agent": "weather", "action": "query", "params": {"location": "北京"}}
  ],
  "collaboration": "parallel"
}
```

### 示例3：复杂依赖图
```
输入：计算25乘以8，查询北京天气，把计算结果翻译成英文，把天气信息写入文件
```
LLM输出：
```json
{
  "description": "复杂多步骤任务",
  "steps": [
    {"id": 1, "agent": "calculator", "action": "calculate", "params": {"expression": "25*8"}},
    {"id": 2, "agent": "weather", "action": "query", "params": {"location": "北京"}},
    {"id": 3, "agent": "translator", "action": "translate", "params": {"text": "step:1", "target_language": "英文"}, "depends_on": [1]},
    {"id": 4, "agent": "file", "action": "write", "params": {"content": "step:2", "filename": "weather.txt"}, "depends_on": [2]}
  ],
  "collaboration": "dag"
}
```

## 执行流程

### 1. 任务分析
- 用户输入自然语言任务
- LLM分析意图，输出结构化JSON
- 系统解析JSON，提取steps、params、dependencies

### 2. 调度执行
- **顺序执行**：按steps顺序执行，支持"上一步结果"依赖
- **并行执行**：所有steps并发执行
- **DAG执行**：按依赖关系拓扑排序执行

### 3. 参数处理
- 自动替换"step:N"为第N步的结果
- 支持"上一步结果"关键字替换
- 参数Map传递给Agent的action方法

### 4. Agent调用
- 优先调用`chat(action, params)`方法
- 如果不存在，fallback到`chat(input)`方法
- 自动将params转换为字符串输入

## 日志输出示例

### DAG执行日志
```
任务创建: task-1
[时间] PENDING: 任务已提交，等待执行
[时间] ANALYZING: 正在分析任务...
[时间] 任务分析完成: 复杂多步骤任务
[时间] EXECUTING: 正在执行任务...
[时间] 开始DAG调度执行，共 4 个步骤
[时间] 执行步骤 1: calculator (calculate)
[时间] 执行步骤 2: weather (query)
[时间] 执行步骤 3: translator (translate)
[时间] 执行步骤 4: file (write)
[时间] 🎉 DAG调度执行完成
[时间] COMPLETED: 任务执行完成
```

## 扩展指南

### 1. 添加新Agent
1. 在`initializeAgents()`中添加新Agent
2. 在LLM prompt中描述新Agent的能力
3. 实现对应的Agent接口

### 2. 添加新Action
1. 在Agent接口中添加新方法
2. 在LLM prompt中描述新action
3. 系统会自动通过反射调用

### 3. 优化Prompt
- 调整prompt模板，让LLM输出更稳定的JSON
- 添加更多示例，提升意图识别准确率
- 支持更复杂的依赖关系描述

## 技术架构

### 核心组件
- **EnhancedAgentOrchestrator**: 主调度器
- **LlmTaskPlan**: 任务规划对象
- **LlmTaskStep**: 执行步骤对象
- **SpecializedAgent**: Agent包装器

### 执行模式
- **Sequential**: 顺序执行，支持上一步结果依赖
- **Parallel**: 并行执行，无依赖关系
- **DAG**: 依赖图执行，支持复杂依赖

### 参数传递
- **Map<String, Object>**: 通用参数容器
- **step:N**: 引用第N步结果
- **上一步结果**: 顺序执行中的结果传递

## 优势

1. **智能规划**: LLM自动分析意图，无需手动维护关键词
2. **灵活扩展**: 只需优化prompt，无需修改代码
3. **复杂依赖**: 支持任意复杂的依赖关系
4. **参数丰富**: 支持任意参数类型和数量
5. **向后兼容**: 保持原有接口兼容性

## 使用建议

1. **输入描述**: 用自然语言清晰描述任务需求
2. **依赖关系**: 明确表达步骤间的依赖关系
3. **参数指定**: 在输入中明确指定参数（如地点、语言等）
4. **监控日志**: 关注执行日志，了解任务执行过程
5. **错误处理**: 系统会自动fallback到search agent

这个系统现在具备了企业级多Agent协作的核心能力，可以处理复杂的业务场景和用户需求！ 