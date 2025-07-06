package tools;

import dev.langchain4j.agent.tool.Tool;

/**
 * 计算器工具类
 * 提供基本的数学运算功能
 */
public class CalculatorTool {
    
    @Tool("计算两个数字的和")
    public int add(int a, int b) {
        System.out.println("🔧 工具调用: 计算 " + a + " + " + b);
        return a + b;
    }
    
    @Tool("计算两个数字的差")
    public int subtract(int a, int b) {
        System.out.println("🔧 工具调用: 计算 " + a + " - " + b);
        return a - b;
    }
    
    @Tool("计算两个数字的积")
    public int multiply(int a, int b) {
        System.out.println("🔧 工具调用: 计算 " + a + " × " + b);
        return a * b;
    }
    
    @Tool("计算两个数字的商")
    public double divide(int a, int b) {
        System.out.println("🔧 工具调用: 计算 " + a + " ÷ " + b);
        if (b == 0) {
            throw new ArithmeticException("除数不能为零");
        }
        return (double) a / b;
    }
    
    @Tool("计算一个数字的平方")
    public int square(int a) {
        System.out.println("🔧 工具调用: 计算 " + a + " 的平方");
        return a * a;
    }
    
    @Tool("计算一个数字的平方根")
    public double sqrt(int a) {
        System.out.println("🔧 工具调用: 计算 " + a + " 的平方根");
        if (a < 0) {
            throw new IllegalArgumentException("不能计算负数的平方根");
        }
        return Math.sqrt(a);
    }
} 