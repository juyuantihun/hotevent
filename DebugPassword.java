import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class DebugPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试密码
        String rawPassword = "123456";
        
        // 加密密码
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        
        // 验证密码
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("密码验证结果: " + matches);
        
        // 测试另一个密码
        String wrongPassword = "wrong";
        boolean wrongMatches = encoder.matches(wrongPassword, encodedPassword);
        System.out.println("错误密码验证结果: " + wrongMatches);
    }
}