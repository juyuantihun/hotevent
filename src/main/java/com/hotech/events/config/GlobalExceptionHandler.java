package com.hotech.events.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.hotech.events.common.Result;
import com.hotech.events.dto.ApiResponse;
import com.hotech.events.exception.ApiException;
import com.hotech.events.exception.NetworkException;
import com.hotech.events.exception.TimelineException;
import com.hotech.events.exception.ValidationException;
import com.hotech.events.service.SystemMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private SystemMonitoringService monitoringService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.error("数据验证失败: {}", errors);
        
        // 记录验证错误
        monitoringService.recordSystemError("VALIDATION", "METHOD_ARGUMENT_NOT_VALID", 
                                          "数据验证失败: " + errors, getStackTrace(ex));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.validateFailed("数据验证失败"));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(ValidationException ex) {
        log.error("业务验证异常: operation={}, fieldName={}, message={}", 
                 ex.getOperation(), ex.getFieldName(), ex.getMessage());
        
        // 记录业务验证错误
        monitoringService.recordSystemError(ex.getOperation(), "VALIDATION_ERROR", 
                                          ex.getMessage(), getStackTrace(ex));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.validateFailed(ex.getMessage()));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<String>> handleApiException(ApiException ex) {
        log.error("API调用异常: operation={}, statusCode={}, apiUrl={}, message={}", 
                 ex.getOperation(), ex.getStatusCode(), ex.getApiUrl(), ex.getMessage());
        
        // 记录API调用错误
        monitoringService.recordSystemError(ex.getOperation(), "API_ERROR", 
                                          ex.getMessage(), getStackTrace(ex));
        
        // 触发API异常告警
        if (ex.getStatusCode() >= 500) {
            monitoringService.triggerAlert("API_SERVER_ERROR", 
                    "API服务器错误: " + ex.getMessage(), 
                    SystemMonitoringService.AlertSeverity.HIGH);
        }
        
        HttpStatus status = ex.getStatusCode() >= 500 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiResponse.error("API调用失败: " + ex.getMessage()));
    }

    @ExceptionHandler(NetworkException.class)
    public ResponseEntity<ApiResponse<String>> handleNetworkException(NetworkException ex) {
        log.error("网络异常: operation={}, host={}, port={}, timeout={}, message={}", 
                 ex.getOperation(), ex.getHost(), ex.getPort(), ex.getTimeout(), ex.getMessage());
        
        // 记录网络错误
        monitoringService.recordSystemError(ex.getOperation(), "NETWORK_ERROR", 
                                          ex.getMessage(), getStackTrace(ex));
        
        // 触发网络异常告警
        monitoringService.triggerAlert("NETWORK_ERROR", 
                "网络连接异常: " + ex.getMessage(), 
                SystemMonitoringService.AlertSeverity.MEDIUM);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("网络连接异常，请稍后重试"));
    }

    @ExceptionHandler(TimelineException.class)
    public ResponseEntity<ApiResponse<String>> handleTimelineException(TimelineException ex) {
        log.error("时间线业务异常: operation={}, errorCode={}, message={}", 
                 ex.getOperation(), ex.getErrorCode(), ex.getMessage());
        
        // 记录业务错误
        monitoringService.recordSystemError(ex.getOperation(), ex.getErrorCode(), 
                                          ex.getMessage(), getStackTrace(ex));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({SocketTimeoutException.class, TimeoutException.class})
    public ResponseEntity<ApiResponse<String>> handleTimeoutException(Exception ex) {
        log.error("请求超时异常: {}", ex.getMessage());
        
        // 记录超时错误
        monitoringService.recordSystemError("TIMEOUT", "REQUEST_TIMEOUT", 
                                          ex.getMessage(), getStackTrace(ex));
        
        // 触发超时告警
        monitoringService.triggerAlert("REQUEST_TIMEOUT", 
                "请求超时: " + ex.getMessage(), 
                SystemMonitoringService.AlertSeverity.MEDIUM);
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .body(ApiResponse.error("请求超时，请稍后重试"));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiResponse<String>> handleResourceAccessException(ResourceAccessException ex) {
        log.error("资源访问异常: {}", ex.getMessage());
        
        // 记录资源访问错误
        monitoringService.recordSystemError("RESOURCE_ACCESS", "RESOURCE_ACCESS_ERROR", 
                                          ex.getMessage(), getStackTrace(ex));
        
        // 检查是否是网络相关异常
        if (ex.getCause() instanceof SocketTimeoutException) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body(ApiResponse.error("连接超时，请检查网络连接"));
        }
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("服务暂时不可用，请稍后重试"));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiResponse<String>> handleRestClientException(RestClientException ex) {
        log.error("REST客户端异常: {}", ex.getMessage());
        
        // 记录REST客户端错误
        monitoringService.recordSystemError("REST_CLIENT", "REST_CLIENT_ERROR", 
                                          ex.getMessage(), getStackTrace(ex));
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error("外部服务调用失败"));
    }

    /**
     * JWT验证异常处理
     */
    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<Object> handleJWTVerificationException(JWTVerificationException ex) {
        log.error("JWT令牌验证失败: {}", ex.getMessage());
        
        // 记录JWT验证错误
        monitoringService.recordSystemError("AUTH", "JWT_VERIFICATION_FAILED", 
                                          ex.getMessage(), getStackTrace(ex));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error("认证令牌无效，请重新登录"));
    }

    /**
     * 认证异常处理
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        log.error("认证异常: {}", ex.getMessage());
        
        // 记录认证错误
        monitoringService.recordSystemError("AUTH", "AUTHENTICATION_FAILED", 
                                          ex.getMessage(), getStackTrace(ex));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error("认证失败，请检查用户名和密码"));
    }

    /**
     * 凭据错误异常处理
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("凭据错误: {}", ex.getMessage());
        
        // 记录凭据错误
        monitoringService.recordSystemError("AUTH", "BAD_CREDENTIALS", 
                                          ex.getMessage(), getStackTrace(ex));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error("用户名或密码错误"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("参数异常: {}", ex.getMessage());
        
        // 记录参数错误
        monitoringService.recordSystemError("PARAMETER", "ILLEGAL_ARGUMENT", 
                                          ex.getMessage(), getStackTrace(ex));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.validateFailed("参数错误: " + ex.getMessage()));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<String>> handleNullPointerException(NullPointerException ex) {
        log.error("空指针异常: ", ex);
        
        // 记录空指针错误
        monitoringService.recordSystemError("NULL_POINTER", "NULL_POINTER_EXCEPTION", 
                                          ex.getMessage(), getStackTrace(ex));
        
        // 触发严重错误告警
        monitoringService.triggerAlert("NULL_POINTER_ERROR", 
                "系统出现空指针异常", 
                SystemMonitoringService.AlertSeverity.HIGH);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系统内部错误"));
    }

    @ExceptionHandler(OutOfMemoryError.class)
    public ResponseEntity<ApiResponse<String>> handleOutOfMemoryError(OutOfMemoryError ex) {
        log.error("内存溢出错误: ", ex);
        
        // 记录内存溢出错误
        monitoringService.recordSystemError("MEMORY", "OUT_OF_MEMORY", 
                                          ex.getMessage(), getStackTrace(ex));
        
        // 触发严重错误告警
        monitoringService.triggerAlert("OUT_OF_MEMORY", 
                "系统内存不足", 
                SystemMonitoringService.AlertSeverity.CRITICAL);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系统资源不足，请联系管理员"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception ex) {
        log.error("未处理的系统异常: ", ex);
        
        // 记录未知错误
        monitoringService.recordSystemError("UNKNOWN", "GENERAL_EXCEPTION", 
                                          ex.getMessage(), getStackTrace(ex));
        
        // 触发未知错误告警
        monitoringService.triggerAlert("UNKNOWN_ERROR", 
                "系统出现未知异常: " + ex.getClass().getSimpleName(), 
                SystemMonitoringService.AlertSeverity.HIGH);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系统内部错误"));
    }

    /**
     * 获取异常堆栈跟踪
     */
    private String getStackTrace(Throwable throwable) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "无法获取堆栈跟踪: " + e.getMessage();
        }
    }
} 