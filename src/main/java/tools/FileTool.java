package tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件操作工具
 * 支持文件读写、格式转换等功能
 */
@Component
public class FileTool {
    
    private static final String WORKSPACE_DIR = "workspace";
    
    public FileTool() {
        // 确保工作目录存在
        createWorkspaceIfNotExists();
    }
    
    @Tool("读取文件内容")
    public String readFile(String filePath) {
        try {
            Path path = getWorkspacePath(filePath);
            if (!Files.exists(path)) {
                return "❌ 文件不存在: " + filePath;
            }
            
            if (!Files.isReadable(path)) {
                return "❌ 文件无法读取: " + filePath;
            }
            
            List<String> lines = Files.readAllLines(path);
            return String.format("📄 文件内容 (%s):\n%s", filePath, 
                lines.stream().collect(Collectors.joining("\n")));
        } catch (Exception e) {
            return "❌ 读取文件失败: " + e.getMessage();
        }
    }
    
    @Tool("写入文件内容")
    public String writeFile(String filePath, String content) {
        try {
            Path path = getWorkspacePath(filePath);
            
            // 确保父目录存在
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            
            Files.write(path, content.getBytes());
            return String.format("✅ 文件写入成功: %s (大小: %d 字节)", filePath, content.length());
        } catch (Exception e) {
            return "❌ 写入文件失败: " + e.getMessage();
        }
    }
    
    @Tool("列出目录内容")
    public String listDirectory(String directoryPath) {
        try {
            Path path = getWorkspacePath(directoryPath);
            if (!Files.exists(path)) {
                return "❌ 目录不存在: " + directoryPath;
            }
            
            if (!Files.isDirectory(path)) {
                return "❌ 不是目录: " + directoryPath;
            }
            
            List<Path> items = Files.list(path).collect(Collectors.toList());
            if (items.isEmpty()) {
                return String.format("📁 目录为空: %s", directoryPath);
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📁 目录内容 (%s):\n", directoryPath));
            
            for (Path item : items) {
                String type = Files.isDirectory(item) ? "📁" : "📄";
                String name = item.getFileName().toString();
                sb.append(String.format("%s %s\n", type, name));
            }
            
            return sb.toString();
        } catch (Exception e) {
            return "❌ 列出目录失败: " + e.getMessage();
        }
    }
    
    @Tool("创建目录")
    public String createDirectory(String directoryPath) {
        try {
            Path path = getWorkspacePath(directoryPath);
            if (Files.exists(path)) {
                return "❌ 目录已存在: " + directoryPath;
            }
            
            Files.createDirectories(path);
            return String.format("✅ 目录创建成功: %s", directoryPath);
        } catch (Exception e) {
            return "❌ 创建目录失败: " + e.getMessage();
        }
    }
    
    @Tool("删除文件或目录")
    public String deleteFile(String filePath) {
        try {
            Path path = getWorkspacePath(filePath);
            if (!Files.exists(path)) {
                return "❌ 文件或目录不存在: " + filePath;
            }
            
            if (Files.isDirectory(path)) {
                deleteDirectoryRecursively(path);
                return String.format("✅ 目录删除成功: %s", filePath);
            } else {
                Files.delete(path);
                return String.format("✅ 文件删除成功: %s", filePath);
            }
        } catch (Exception e) {
            return "❌ 删除失败: " + e.getMessage();
        }
    }
    
    @Tool("获取文件信息")
    public String getFileInfo(String filePath) {
        try {
            Path path = getWorkspacePath(filePath);
            if (!Files.exists(path)) {
                return "❌ 文件不存在: " + filePath;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📊 文件信息 (%s):\n", filePath));
            sb.append(String.format("- 类型: %s\n", Files.isDirectory(path) ? "目录" : "文件"));
            sb.append(String.format("- 大小: %d 字节\n", Files.size(path)));
            sb.append(String.format("- 创建时间: %s\n", Files.getAttribute(path, "creationTime")));
            sb.append(String.format("- 修改时间: %s\n", Files.getLastModifiedTime(path)));
            sb.append(String.format("- 可读: %s\n", Files.isReadable(path)));
            sb.append(String.format("- 可写: %s\n", Files.isWritable(path)));
            
            return sb.toString();
        } catch (Exception e) {
            return "❌ 获取文件信息失败: " + e.getMessage();
        }
    }
    
    @Tool("搜索文件")
    public String searchFiles(String pattern) {
        try {
            Path workspacePath = Paths.get(WORKSPACE_DIR);
            if (!Files.exists(workspacePath)) {
                return "❌ 工作目录不存在";
            }
            
            List<Path> foundFiles = Files.walk(workspacePath)
                .filter(path -> path.getFileName().toString().contains(pattern))
                .collect(Collectors.toList());
            
            if (foundFiles.isEmpty()) {
                return String.format("🔍 未找到匹配 '%s' 的文件", pattern);
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🔍 找到 %d 个匹配文件:\n", foundFiles.size()));
            
            for (Path file : foundFiles) {
                String relativePath = workspacePath.relativize(file).toString();
                String type = Files.isDirectory(file) ? "📁" : "📄";
                sb.append(String.format("%s %s\n", type, relativePath));
            }
            
            return sb.toString();
        } catch (Exception e) {
            return "❌ 搜索文件失败: " + e.getMessage();
        }
    }
    
    private Path getWorkspacePath(String filePath) {
        return Paths.get(WORKSPACE_DIR, filePath);
    }
    
    private void createWorkspaceIfNotExists() {
        try {
            Path workspacePath = Paths.get(WORKSPACE_DIR);
            if (!Files.exists(workspacePath)) {
                Files.createDirectories(workspacePath);
            }
        } catch (Exception e) {
            System.err.println("创建工作目录失败: " + e.getMessage());
        }
    }
    
    private void deleteDirectoryRecursively(Path directory) throws IOException {
        Files.walk(directory)
            .sorted((a, b) -> b.compareTo(a)) // 先删除子文件，再删除父目录
            .forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }
} 