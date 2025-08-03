package com.hotech.events.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类
 * 用于生成和验证JWT令牌
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT密钥
     */
    @Value("${jwt.secret:hotech-events-secret-key-2024}")
    private String secret;

    /**
     * JWT过期时间（毫秒）
     * 默认24小时
     */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * JWT刷新令牌过期时间（毫秒）
     * 默认7天
     */
    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    /**
     * JWT签发者
     */
    @Value("${jwt.issuer:hotech-events}")
    private String issuer;

    /**
     * 生成JWT令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(Long userId, String username) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);

            return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(username)
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withIssuedAt(now)
                    .withExpiresAt(expiryDate)
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            log.error("生成JWT令牌失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成JWT令牌失败", e);
        }
    }

    /**
     * 生成刷新令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return 刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + refreshExpiration);

            return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(username)
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withClaim("type", "refresh")
                    .withIssuedAt(now)
                    .withExpiresAt(expiryDate)
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            log.error("生成刷新令牌失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成刷新令牌失败", e);
        }
    }

    /**
     * 验证JWT令牌
     * 
     * @param token JWT令牌
     * @return 解码后的JWT
     * @throws JWTVerificationException 验证失败时抛出异常
     */
    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();
            
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.warn("JWT令牌验证失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 从令牌中获取用户ID
     * 
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getClaim("userId").asLong();
        } catch (Exception e) {
            log.error("从令牌中获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Authorization头中提取令牌
     * 
     * @param authHeader Authorization头的值
     * @return JWT令牌，如果无效则返回null
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            log.debug("Authorization头为空");
            return null;
        }
        
        // 检查是否以 "Bearer " 开头
        if (authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (token.isEmpty()) {
                log.debug("Bearer令牌为空");
                return null;
            }
            return token;
        } else {
            log.debug("Authorization头格式不正确，应以'Bearer '开头");
            return null;
        }
    }

    /**
     * 从令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getClaim("username").asString();
        } catch (Exception e) {
            log.error("从令牌中获取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查令牌是否过期
     * 
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            Date expiresAt = decodedJWT.getExpiresAt();
            return expiresAt.before(new Date());
        } catch (Exception e) {
            log.error("检查令牌过期状态失败: {}", e.getMessage());
            return true; // 出错时认为已过期
        }
    }

    /**
     * 检查令牌是否即将过期
     * 
     * @param token JWT令牌
     * @param thresholdMinutes 阈值（分钟）
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token, int thresholdMinutes) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            Date expiresAt = decodedJWT.getExpiresAt();
            Date threshold = new Date(System.currentTimeMillis() + (thresholdMinutes * 60 * 1000L));
            return expiresAt.before(threshold);
        } catch (Exception e) {
            log.error("检查令牌即将过期状态失败: {}", e.getMessage());
            return true; // 出错时认为即将过期
        }
    }

    /**
     * 刷新令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    public String refreshAccessToken(String refreshToken) {
        try {
            DecodedJWT decodedJWT = verifyToken(refreshToken);
            
            // 检查是否是刷新令牌
            String tokenType = decodedJWT.getClaim("type").asString();
            if (!"refresh".equals(tokenType)) {
                throw new JWTVerificationException("不是有效的刷新令牌");
            }
            
            Long userId = decodedJWT.getClaim("userId").asLong();
            String username = decodedJWT.getClaim("username").asString();
            
            return generateToken(userId, username);
        } catch (Exception e) {
            log.error("刷新访问令牌失败: {}", e.getMessage());
            throw new RuntimeException("刷新访问令牌失败", e);
        }
    }



    /**
     * 获取令牌过期时间（毫秒）
     * 
     * @return 过期时间
     */
    public Long getExpiration() {
        return expiration;
    }

    /**
     * 获取刷新令牌过期时间（毫秒）
     * 
     * @return 刷新令牌过期时间
     */
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}