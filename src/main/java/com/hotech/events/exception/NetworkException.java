package com.hotech.events.exception;

/**
 * 网络异常
 */
public class NetworkException extends TimelineException {
    
    private final String host;
    private final int port;
    private final long timeout;
    
    public NetworkException(String message) {
        super(message, "NETWORK_ERROR");
        this.host = null;
        this.port = 0;
        this.timeout = 0;
    }
    
    public NetworkException(String message, String host, int port) {
        super(message, "NETWORK_ERROR");
        this.host = host;
        this.port = port;
        this.timeout = 0;
    }
    
    public NetworkException(String message, Throwable cause, String host, int port, long timeout) {
        super(message, cause, "NETWORK_ERROR", "NETWORK_CALL");
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    public long getTimeout() {
        return timeout;
    }
}