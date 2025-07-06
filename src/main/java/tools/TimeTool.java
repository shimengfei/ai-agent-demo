package tools;

import dev.langchain4j.agent.tool.Tool;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 时间工具类
 * 提供时间相关的查询功能
 */
public class TimeTool {
    
    @Tool("获取当前时间")
    public String getCurrentTime() {
        System.out.println("🔧 工具调用: 获取当前时间");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "当前时间是: " + now.format(formatter);
    }
    
    @Tool("获取当前日期")
    public String getCurrentDate() {
        System.out.println("🔧 工具调用: 获取当前日期");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        return "今天是: " + now.format(formatter);
    }
    
    @Tool("获取指定时区的当前时间")
    public String getTimeInZone(String timeZone) {
        System.out.println("🔧 工具调用: 获取 " + timeZone + " 时区的当前时间");
        try {
            ZoneId zoneId = ZoneId.of(timeZone);
            ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return timeZone + " 时区的当前时间是: " + zonedDateTime.format(formatter);
        } catch (Exception e) {
            return "无效的时区: " + timeZone + "。请使用标准时区格式，如 'Asia/Shanghai', 'America/New_York'";
        }
    }
    
    @Tool("计算两个时间之间的差值（小时）")
    public double calculateTimeDifference(String time1, String time2) {
        System.out.println("🔧 工具调用: 计算时间差 " + time1 + " 和 " + time2);
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalDateTime dt1 = LocalDateTime.parse("2000-01-01 " + time1, 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime dt2 = LocalDateTime.parse("2000-01-01 " + time2, 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            long diffInMinutes = java.time.Duration.between(dt1, dt2).toMinutes();
            return Math.abs(diffInMinutes) / 60.0;
        } catch (Exception e) {
            return -1; // 表示计算失败
        }
    }
} 