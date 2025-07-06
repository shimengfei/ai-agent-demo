package tools;

import dev.langchain4j.agent.tool.Tool;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 天气工具类
 * 模拟天气查询功能
 */
public class WeatherTool {
    
    private final Map<String, String> weatherData;
    private final Random random;
    
    public WeatherTool() {
        this.weatherData = new HashMap<>();
        this.random = new Random();
        
        // 初始化一些模拟天气数据
        weatherData.put("北京", "晴天");
        weatherData.put("上海", "多云");
        weatherData.put("广州", "小雨");
        weatherData.put("深圳", "晴天");
        weatherData.put("杭州", "阴天");
        weatherData.put("成都", "多云");
        weatherData.put("西安", "晴天");
        weatherData.put("武汉", "小雨");
    }
    
    @Tool("查询指定城市的天气情况")
    public String getWeather(String city) {
        System.out.println("🔧 工具调用: 查询 " + city + " 的天气");
        
        String weather = weatherData.get(city);
        if (weather == null) {
            // 如果城市不存在，随机生成一个天气
            String[] weatherTypes = {"晴天", "多云", "阴天", "小雨", "中雨", "大雨"};
            weather = weatherTypes[random.nextInt(weatherTypes.length)];
            weatherData.put(city, weather);
        }
        
        int temperature = 15 + random.nextInt(20); // 15-35度
        return city + "的天气是" + weather + "，温度" + temperature + "°C";
    }
    
    @Tool("查询指定城市的温度")
    public int getTemperature(String city) {
        System.out.println("🔧 工具调用: 查询 " + city + " 的温度");
        return 15 + random.nextInt(20); // 15-35度
    }
    
    @Tool("查询指定城市的湿度")
    public int getHumidity(String city) {
        System.out.println("🔧 工具调用: 查询 " + city + " 的湿度");
        return 30 + random.nextInt(50); // 30-80%
    }
} 