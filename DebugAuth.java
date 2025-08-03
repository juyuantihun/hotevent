// 临时调试文件 - 用于测试密码加密和验证
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class DebugAuth {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试你注册时使用的密码
        String testPassword = "123456"; // 请替换为你实际注册时使用的密码
        
        System.out.println("=== 密码加密测试 ===");
        System.out.println("原始密码: " + testPassword);
        
        // 生成加密密码（模拟注册过程）
        String encodedPassword = encoder.encode(testPassword);
        System.out.println("加密后密码: " + encodedPassword);
        
        // 验证密码（模拟登录过程）
        boolean isValid = encoder.matches(testPassword, encodedPassword);
        System.out.println("密码验证结果: " + isValid);
        
        // 测试错误密码
        String wrongPassword = "wrong123";
        boolean isWrongValid = encoder.matches(wrongPassword, encodedPassword);
        System.out.println("错误密码验证结果: " + isWrongValid);
        
        System.out.println("\n=== 建议检查项 ===");
        System.out.println("1. 检查数据库中用户的密码字段是否以 $2a$ 开头（BCrypt 特征）");
        System.out.println("2. 确认注册和登录使用的是相同的密码");
        System.out.println("3. 检查用户状态字段是否为 1（启用状态）");
        System.out.println("4. 查看后端日志中的详细错误信息");
    }
}