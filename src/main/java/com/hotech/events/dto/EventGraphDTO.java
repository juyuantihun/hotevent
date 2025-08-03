package com.hotech.events.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件图数据传输对象
 * 用于前端图形化展示
 * 
 * @author AI助手
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventGraphDTO {
    
    /**
     * 图节点
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {
        private String id;           // 节点ID（事件编码）
        private String label;        // 节点标签（事件标题）
        private String type;         // 节点类型（事件类型）
        private String description;  // 节点描述
        private LocalDateTime time;  // 事件时间
        private String location;     // 事件地点
        private String subject;      // 事件主体
        private String object;       // 事件客体
        private List<String> keywords; // 关键词
        private Integer size;        // 节点大小（基于关联度）
        private String color;        // 节点颜色
        private String shape;        // 节点形状
        private Double x;            // X坐标
        private Double y;            // Y坐标
    }
    
    /**
     * 图边
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {
        private String id;           // 边ID
        private String source;       // 源节点ID
        private String target;       // 目标节点ID
        private String label;        // 边标签（关系类型）
        private String type;         // 关系类型
        private String description;  // 关系描述
        private Double confidence;   // 置信度
        private Integer strength;    // 关系强度
        private String direction;    // 关系方向
        private String color;        // 边颜色
        private Integer width;       // 边宽度
        private String style;        // 边样式
    }
    
    private List<Node> nodes;        // 节点列表
    private List<Edge> edges;        // 边列表
    private String centerNode;       // 中心节点ID
    private Integer totalNodes;      // 总节点数
    private Integer totalEdges;      // 总边数
    private String graphType;        // 图类型（timeline/cluster/causal/general）
} 