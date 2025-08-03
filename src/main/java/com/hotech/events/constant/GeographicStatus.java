package com.hotech.events.constant;

/**
 * 地理信息处理状态常量
 */
public class GeographicStatus {
    
    /**
     * 未处理
     */
    public static final int UNPROCESSED = 0;
    
    /**
     * 已处理
     */
    public static final int PROCESSED = 1;
    
    /**
     * 处理失败
     */
    public static final int FAILED = 2;
    
    /**
     * 获取状态描述
     * @param status 状态值
     * @return 状态描述
     */
    public static String getDescription(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case UNPROCESSED:
                return "未处理";
            case PROCESSED:
                return "已处理";
            case FAILED:
                return "处理失败";
            default:
                return "未知状态";
        }
    }
}