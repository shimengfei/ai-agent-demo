package tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 翻译工具
 * 支持多语言翻译功能
 */
@Component
public class TranslationTool {
    
    private final Map<String, String> languageMap = new HashMap<>();
    
    public TranslationTool() {
        // 初始化语言映射
        languageMap.put("中文", "zh");
        languageMap.put("英文", "en");
        languageMap.put("日文", "ja");
        languageMap.put("韩文", "ko");
        languageMap.put("法文", "fr");
        languageMap.put("德文", "de");
        languageMap.put("西班牙文", "es");
        languageMap.put("俄文", "ru");
    }
    
    @Tool("翻译文本到指定语言")
    public String translate(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return "❌ 翻译文本不能为空";
        }
        
        if (targetLanguage == null || targetLanguage.trim().isEmpty()) {
            return "❌ 目标语言不能为空";
        }
        
        try {
            // 这里应该调用真实的翻译API，这里用模拟实现
            String languageCode = languageMap.get(targetLanguage);
            if (languageCode == null) {
                languageCode = targetLanguage.toLowerCase();
            }
            
            // 模拟翻译结果
            String translatedText = simulateTranslation(text, languageCode);
            
            return String.format("✅ 翻译结果 (%s): %s", targetLanguage, translatedText);
        } catch (Exception e) {
            return "❌ 翻译失败: " + e.getMessage();
        }
    }
    
    @Tool("检测文本语言")
    public String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "❌ 检测文本不能为空";
        }
        
        try {
            // 简单的语言检测逻辑
            String detectedLanguage = simpleLanguageDetection(text);
            return String.format("🔍 检测到的语言: %s", detectedLanguage);
        } catch (Exception e) {
            return "❌ 语言检测失败: " + e.getMessage();
        }
    }
    
    @Tool("获取支持的语言列表")
    public String getSupportedLanguages() {
        StringBuilder sb = new StringBuilder("🌍 支持的语言:\n");
        languageMap.forEach((name, code) -> 
            sb.append(String.format("- %s (%s)\n", name, code))
        );
        return sb.toString();
    }
    
    private String simulateTranslation(String text, String targetLanguage) {
        // 模拟翻译逻辑
        switch (targetLanguage) {
            case "en":
                return "[EN] " + text + " (translated to English)";
            case "zh":
                return "[中文] " + text + " (翻译成中文)";
            case "ja":
                return "[日本語] " + text + " (日本語に翻訳)";
            case "ko":
                return "[한국어] " + text + " (한국어로 번역)";
            case "fr":
                return "[Français] " + text + " (traduit en français)";
            case "de":
                return "[Deutsch] " + text + " (ins Deutsche übersetzt)";
            case "es":
                return "[Español] " + text + " (traducido al español)";
            case "ru":
                return "[Русский] " + text + " (переведено на русский)";
            default:
                return "[" + targetLanguage.toUpperCase() + "] " + text + " (translated)";
        }
    }
    
    private String simpleLanguageDetection(String text) {
        // 简单的语言检测逻辑
        if (text.matches(".*[\\u4e00-\\u9fa5].*")) {
            return "中文";
        } else if (text.matches(".*[\\u3040-\\u309f\\u30a0-\\u30ff].*")) {
            return "日文";
        } else if (text.matches(".*[\\uac00-\\ud7af].*")) {
            return "韩文";
        } else if (text.matches(".*[а-яё].*")) {
            return "俄文";
        } else {
            return "英文";
        }
    }
} 