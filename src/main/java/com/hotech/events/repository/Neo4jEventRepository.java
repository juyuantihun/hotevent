package com.hotech.events.repository;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.exceptions.Neo4jException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Neo4j事件存储库
 */
@Slf4j
@Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "spring.neo4j.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
public class Neo4jEventRepository {

    @Autowired
    private Driver neo4jDriver;
    
    /**
     * 创建事件节点
     * @param event 事件信息
     * @return 创建的节点ID
     */
    public Long createEventNode(Map<String, Object> event) {
        try (Session session = neo4jDriver.session()) {
            return session.writeTransaction(tx -> {
                // 构建Cypher查询
                StringBuilder cypher = new StringBuilder();
                cypher.append("CREATE (e:Event {");
                
                // 添加属性
                List<String> properties = new ArrayList<>();
                Map<String, Object> params = new HashMap<>();
                
                for (Map.Entry<String, Object> entry : event.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    
                    if (value != null) {
                        properties.add(key + ": $" + key);
                        params.put(key, value);
                    }
                }
                
                cypher.append(String.join(", ", properties));
                cypher.append("}) RETURN id(e) as id");
                
                // 执行查询
                Result result = tx.run(cypher.toString(), params);
                return result.single().get("id").asLong();
            });
        } catch (Neo4jException e) {
            log.error("创建事件节点失败", e);
            throw new RuntimeException("创建事件节点失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量创建事件节点
     * @param events 事件列表
     * @return 创建的节点ID列表
     */
    public List<Long> batchCreateEventNodes(List<Map<String, Object>> events) {
        try (Session session = neo4jDriver.session()) {
            return session.writeTransaction(tx -> {
                List<Long> nodeIds = new ArrayList<>();
                
                for (Map<String, Object> event : events) {
                    // 构建Cypher查询
                    StringBuilder cypher = new StringBuilder();
                    cypher.append("CREATE (e:Event {");
                    
                    // 添加属性
                    List<String> properties = new ArrayList<>();
                    Map<String, Object> params = new HashMap<>();
                    
                    for (Map.Entry<String, Object> entry : event.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        
                        if (value != null) {
                            properties.add(key + ": $" + key);
                            params.put(key, value);
                        }
                    }
                    
                    cypher.append(String.join(", ", properties));
                    cypher.append("}) RETURN id(e) as id");
                    
                    // 执行查询
                    Result result = tx.run(cypher.toString(), params);
                    nodeIds.add(result.single().get("id").asLong());
                }
                
                return nodeIds;
            });
        } catch (Neo4jException e) {
            log.error("批量创建事件节点失败", e);
            throw new RuntimeException("批量创建事件节点失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建事件关系
     * @param sourceEventId 源事件ID
     * @param targetEventId 目标事件ID
     * @param relationType 关系类型
     * @param properties 关系属性
     * @return 创建的关系ID
     */
    public Long createEventRelation(Long sourceEventId, Long targetEventId, String relationType, Map<String, Object> properties) {
        try (Session session = neo4jDriver.session()) {
            return session.writeTransaction(tx -> {
                // 构建Cypher查询
                StringBuilder cypher = new StringBuilder();
                cypher.append("MATCH (source:Event), (target:Event) ");
                cypher.append("WHERE id(source) = $sourceId AND id(target) = $targetId ");
                cypher.append("CREATE (source)-[r:" + relationType + " {");
                
                // 添加属性
                List<String> propList = new ArrayList<>();
                Map<String, Object> params = new HashMap<>();
                params.put("sourceId", sourceEventId);
                params.put("targetId", targetEventId);
                
                if (properties != null) {
                    for (Map.Entry<String, Object> entry : properties.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        
                        if (value != null) {
                            propList.add(key + ": $" + key);
                            params.put(key, value);
                        }
                    }
                }
                
                cypher.append(String.join(", ", propList));
                cypher.append("}]->(target) RETURN id(r) as id");
                
                // 执行查询
                Result result = tx.run(cypher.toString(), params);
                return result.single().get("id").asLong();
            });
        } catch (Neo4jException e) {
            log.error("创建事件关系失败", e);
            throw new RuntimeException("创建事件关系失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量创建事件关系
     * @param relations 关系列表，每个关系包含sourceId, targetId, type, properties
     * @return 创建的关系ID列表
     */
    public List<Long> batchCreateEventRelations(List<Map<String, Object>> relations) {
        try (Session session = neo4jDriver.session()) {
            return session.writeTransaction(tx -> {
                List<Long> relationIds = new ArrayList<>();
                
                for (Map<String, Object> relation : relations) {
                    Long sourceId = Long.valueOf(relation.get("sourceEventId").toString());
                    Long targetId = Long.valueOf(relation.get("targetEventId").toString());
                    String type = relation.get("type").toString();
                    
                    // 构建属性
                    Map<String, Object> properties = new HashMap<>(relation);
                    properties.remove("sourceEventId");
                    properties.remove("targetEventId");
                    properties.remove("type");
                    
                    // 构建Cypher查询
                    StringBuilder cypher = new StringBuilder();
                    cypher.append("MATCH (source:Event), (target:Event) ");
                    cypher.append("WHERE id(source) = $sourceId AND id(target) = $targetId ");
                    cypher.append("CREATE (source)-[r:" + type + " {");
                    
                    // 添加属性
                    List<String> propList = new ArrayList<>();
                    Map<String, Object> params = new HashMap<>();
                    params.put("sourceId", sourceId);
                    params.put("targetId", targetId);
                    
                    for (Map.Entry<String, Object> entry : properties.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        
                        if (value != null) {
                            propList.add(key + ": $" + key);
                            params.put(key, value);
                        }
                    }
                    
                    cypher.append(String.join(", ", propList));
                    cypher.append("}]->(target) RETURN id(r) as id");
                    
                    // 执行查询
                    Result result = tx.run(cypher.toString(), params);
                    relationIds.add(result.single().get("id").asLong());
                }
                
                return relationIds;
            });
        } catch (Neo4jException e) {
            log.error("批量创建事件关系失败", e);
            throw new RuntimeException("批量创建事件关系失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取时间线图形数据
     * @param timelineId 时间线ID
     * @return 图形数据，包含nodes和links
     */
    public Map<String, Object> getTimelineGraph(Long timelineId) {
        try (Session session = neo4jDriver.session()) {
            return session.readTransaction(tx -> {
                // 查询节点
                String nodeQuery = "MATCH (e:Event) WHERE e.timelineId = $timelineId RETURN e";
                Result nodeResult = tx.run(nodeQuery, Map.of("timelineId", timelineId));
                
                List<Map<String, Object>> nodes = new ArrayList<>();
                while (nodeResult.hasNext()) {
                    Record record = nodeResult.next();
                    Map<String, Object> node = record.get("e").asMap();
                    node.put("id", record.get("e").asNode().id());
                    nodes.add(node);
                }
                
                // 查询关系
                String linkQuery = "MATCH (e1:Event)-[r]->(e2:Event) " +
                        "WHERE e1.timelineId = $timelineId AND e2.timelineId = $timelineId " +
                        "RETURN id(e1) as source, id(e2) as target, type(r) as type, r as properties";
                Result linkResult = tx.run(linkQuery, Map.of("timelineId", timelineId));
                
                List<Map<String, Object>> links = new ArrayList<>();
                while (linkResult.hasNext()) {
                    Record record = linkResult.next();
                    Map<String, Object> link = new HashMap<>();
                    link.put("source", record.get("source").asLong());
                    link.put("target", record.get("target").asLong());
                    link.put("type", record.get("type").asString());
                    
                    // 添加关系属性
                    Map<String, Object> props = record.get("properties").asMap();
                    link.putAll(props);
                    
                    links.add(link);
                }
                
                // 返回图形数据
                Map<String, Object> graph = new HashMap<>();
                graph.put("nodes", nodes);
                graph.put("links", links);
                
                return graph;
            });
        } catch (Neo4jException e) {
            log.error("获取时间线图形数据失败", e);
            throw new RuntimeException("获取时间线图形数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除时间线相关的所有节点和关系
     * @param timelineId 时间线ID
     */
    public void deleteTimelineGraph(Long timelineId) {
        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                // 删除关系
                String deleteRelationsQuery = "MATCH (e1:Event)-[r]->(e2:Event) " +
                        "WHERE e1.timelineId = $timelineId AND e2.timelineId = $timelineId " +
                        "DELETE r";
                tx.run(deleteRelationsQuery, Map.of("timelineId", timelineId));
                
                // 删除节点
                String deleteNodesQuery = "MATCH (e:Event) WHERE e.timelineId = $timelineId DELETE e";
                tx.run(deleteNodesQuery, Map.of("timelineId", timelineId));
                
                return null;
            });
        } catch (Neo4jException e) {
            log.error("删除时间线图形数据失败", e);
            throw new RuntimeException("删除时间线图形数据失败: " + e.getMessage());
        }
    }
}