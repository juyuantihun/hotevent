package com.hotech.events.controller;

import com.hotech.events.service.WebSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * 联网搜索页面控制器
 * 提供临时的HTML页面，直到前端应用正确构建
 */
@Controller
@RequestMapping("/websearch")
public class WebSearchPageController {

    @Autowired(required = false)
    private WebSearchService webSearchService;

    @GetMapping
    @ResponseBody
    public String websearchPage() {
        try {
            if (webSearchService != null) {
                // 获取联网搜索状态
                Map<String, Object> config = webSearchService.getWebSearchConfig();
                Map<String, Object> stats = webSearchService.getWebSearchStats();
                boolean available = webSearchService.isWebSearchAvailable();
                
                return generateWebSearchHtml(config, stats, available, null);
            } else {
                return generateWebSearchHtml(null, null, false, "WebSearchService未配置");
            }
            
        } catch (Exception e) {
            return generateWebSearchHtml(null, null, false, e.getMessage());
        }
    }

    @GetMapping("/status-json")
    @ResponseBody
    public Map<String, Object> getStatusJson() {
        try {
            Map<String, Object> result = new java.util.HashMap<>();
            if (webSearchService != null) {
                result.put("available", webSearchService.isWebSearchAvailable());
                result.put("config", webSearchService.getWebSearchConfig());
                result.put("stats", webSearchService.getWebSearchStats());
            } else {
                result.put("available", false);
                result.put("config", null);
                result.put("stats", null);
                result.put("error", "WebSearchService未配置");
            }
            return result;
        } catch (Exception e) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }

    /**
     * 生成联网搜索管理页面HTML
     */
    private String generateWebSearchHtml(Map<String, Object> config, Map<String, Object> stats, 
                                       boolean available, String error) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n")
            .append("<html lang=\"zh-CN\">\n")
            .append("<head>\n")
            .append("    <meta charset=\"UTF-8\">\n")
            .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("    <title>联网搜索管理 - TimeFlow</title>\n")
            .append("    <style>\n")
            .append("        * { margin: 0; padding: 0; box-sizing: border-box; }\n")
            .append("        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f5f7fa; color: #333; line-height: 1.6; }\n")
            .append("        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }\n")
            .append("        .header { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); margin-bottom: 20px; }\n")
            .append("        .header h1 { color: #2c3e50; margin-bottom: 10px; }\n")
            .append("        .status-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 30px; }\n")
            .append("        .status-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); border-left: 4px solid #3498db; }\n")
            .append("        .status-card.success { border-left-color: #27ae60; }\n")
            .append("        .status-card.error { border-left-color: #e74c3c; }\n")
            .append("        .status-label { font-size: 14px; color: #7f8c8d; margin-bottom: 5px; }\n")
            .append("        .status-value { font-size: 24px; font-weight: bold; color: #2c3e50; }\n")
            .append("        .status-value.success { color: #27ae60; }\n")
            .append("        .status-value.error { color: #e74c3c; }\n")
            .append("        .control-panel, .test-section, .config-section { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); margin-bottom: 20px; }\n")
            .append("        .control-panel h2, .test-section h2, .config-section h2 { margin-bottom: 20px; color: #2c3e50; }\n")
            .append("        .button-group { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 20px; }\n")
            .append("        .btn { padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; font-size: 14px; text-decoration: none; display: inline-block; transition: all 0.3s; }\n")
            .append("        .btn-primary { background: #3498db; color: white; }\n")
            .append("        .btn-primary:hover { background: #2980b9; }\n")
            .append("        .btn-success { background: #27ae60; color: white; }\n")
            .append("        .btn-success:hover { background: #229954; }\n")
            .append("        .btn-warning { background: #f39c12; color: white; }\n")
            .append("        .btn-warning:hover { background: #e67e22; }\n")
            .append("        .btn-danger { background: #e74c3c; color: white; }\n")
            .append("        .btn-danger:hover { background: #c0392b; }\n")
            .append("        .test-form { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }\n")
            .append("        .test-input { flex: 1; min-width: 200px; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; }\n")
            .append("        .config-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }\n")
            .append("        .config-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid #eee; }\n")
            .append("        .config-label { font-weight: 500; color: #2c3e50; }\n")
            .append("        .config-value { color: #7f8c8d; }\n")
            .append("        .alert { padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n")
            .append("        .alert-error { background: #f8d7da; border: 1px solid #f5c6cb; color: #721c24; }\n")
            .append("        .alert-success { background: #d4edda; border: 1px solid #c3e6cb; color: #155724; }\n")
            .append("        .loading { display: none; text-align: center; padding: 20px; }\n")
            .append("        .spinner { border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 0 auto 10px; }\n")
            .append("        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }\n")
            .append("        .result-area { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 5px; padding: 15px; margin-top: 15px; font-family: monospace; font-size: 12px; max-height: 300px; overflow-y: auto; }\n")
            .append("    </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("    <div class=\"container\">\n")
            .append("        <div class=\"header\">\n")
            .append("            <h1>🌐 DeepSeek联网搜索管理</h1>\n")
            .append("            <p>管理和测试DeepSeek联网搜索功能</p>\n")
            .append("        </div>\n");

        // 错误提示
        if (error != null) {
            html.append(String.format(
                "<div class=\"alert alert-error\">\n" +
                "    <strong>错误：</strong> %s\n" +
                "</div>\n", error));
        }

        // 状态概览
        html.append("<div class=\"status-grid\">\n");
        
        // 联网搜索状态
        String statusClass = available ? "success" : "error";
        String statusText = available ? "可用" : "不可用";
        html.append(String.format(
            "<div class=\"status-card %s\">\n" +
            "    <div class=\"status-label\">联网搜索状态</div>\n" +
            "    <div class=\"status-value %s\">%s</div>\n" +
            "</div>\n", statusClass, statusClass, statusText));

        // 统计信息
        if (stats != null) {
            Object totalRequests = stats.get("totalRequests");
            Object successRate = stats.get("successRate");
            Object avgResponseTime = stats.get("averageResponseTime");
            
            html.append(String.format(
                "<div class=\"status-card\">\n" +
                "    <div class=\"status-label\">总请求数</div>\n" +
                "    <div class=\"status-value\">%s</div>\n" +
                "</div>\n" +
                "<div class=\"status-card\">\n" +
                "    <div class=\"status-label\">成功率</div>\n" +
                "    <div class=\"status-value\">%.1f%%</div>\n" +
                "</div>\n" +
                "<div class=\"status-card\">\n" +
                "    <div class=\"status-label\">平均响应时间</div>\n" +
                "    <div class=\"status-value\">%.0fms</div>\n" +
                "</div>\n", 
                totalRequests != null ? totalRequests : 0,
                successRate != null ? ((Number) successRate).doubleValue() : 0.0,
                avgResponseTime != null ? ((Number) avgResponseTime).doubleValue() : 0.0));
        } else {
            html.append(
                "<div class=\"status-card\">\n" +
                "    <div class=\"status-label\">总请求数</div>\n" +
                "    <div class=\"status-value\">0</div>\n" +
                "</div>\n" +
                "<div class=\"status-card\">\n" +
                "    <div class=\"status-label\">成功率</div>\n" +
                "    <div class=\"status-value\">0.0%</div>\n" +
                "</div>\n" +
                "<div class=\"status-card\">\n" +
                "    <div class=\"status-label\">平均响应时间</div>\n" +
                "    <div class=\"status-value\">0ms</div>\n" +
                "</div>\n");
        }

        html.append("</div>\n")
            .append("\n")
            .append("<!-- 控制面板 -->\n")
            .append("<div class=\"control-panel\">\n")
            .append("    <h2>🎛️ 控制面板</h2>\n")
            .append("    <div class=\"button-group\">\n")
            .append("        <button class=\"btn btn-success\" onclick=\"enableWebSearch()\">启用联网搜索</button>\n")
            .append("        <button class=\"btn btn-warning\" onclick=\"disableWebSearch()\">禁用联网搜索</button>\n")
            .append("        <button class=\"btn btn-primary\" onclick=\"clearCache()\">清除缓存</button>\n")
            .append("        <button class=\"btn btn-primary\" onclick=\"refreshStatus()\">刷新状态</button>\n")
            .append("    </div>\n")
            .append("</div>\n")
            .append("\n")
            .append("<!-- 测试功能 -->\n")
            .append("<div class=\"test-section\">\n")
            .append("    <h2>🧪 测试功能</h2>\n")
            .append("    <div class=\"test-form\">\n")
            .append("        <input type=\"text\" id=\"testQuery\" class=\"test-input\" \n")
            .append("               placeholder=\"输入测试查询内容\" value=\"伊以战争时间线\">\n")
            .append("        <button class=\"btn btn-primary\" onclick=\"testWebSearch()\">测试联网搜索</button>\n")
            .append("        <button class=\"btn btn-warning\" onclick=\"testRawApi()\">测试原始API</button>\n")
            .append("        <button class=\"btn btn-danger\" onclick=\"testWebSearchFormats()\">测试参数格式</button>\n")
            .append("    </div>\n")
            .append("    <div id=\"testResult\" class=\"result-area\" style=\"display: none;\"></div>\n")
            .append("    <div id=\"rawResult\" class=\"result-area\" style=\"display: none;\"></div>\n")
            .append("    <div id=\"formatResult\" class=\"result-area\" style=\"display: none;\"></div>\n")
            .append("</div>\n")
            .append("\n")
            .append("<!-- 配置信息 -->\n")
            .append("<div class=\"config-section\">\n")
            .append("    <h2>⚙️ 配置信息</h2>\n")
            .append("    <div class=\"config-grid\">\n")
            .append("        <div>\n");

        // 配置信息
        if (config != null) {
            html.append(String.format(
                "        <div class=\"config-item\">\n" +
                "            <span class=\"config-label\">启用状态</span>\n" +
                "            <span class=\"config-value\">%s</span>\n" +
                "        </div>\n" +
                "        <div class=\"config-item\">\n" +
                "            <span class=\"config-label\">最大结果数</span>\n" +
                "            <span class=\"config-value\">%s</span>\n" +
                "        </div>\n" +
                "        <div class=\"config-item\">\n" +
                "            <span class=\"config-label\">搜索超时(ms)</span>\n" +
                "            <span class=\"config-value\">%s</span>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <div>\n" +
                "        <div class=\"config-item\">\n" +
                "            <span class=\"config-label\">API密钥状态</span>\n" +
                "            <span class=\"config-value\">%s</span>\n" +
                "        </div>\n" +
                "        <div class=\"config-item\">\n" +
                "            <span class=\"config-label\">缓存大小</span>\n" +
                "            <span class=\"config-value\">%s</span>\n" +
                "        </div>\n" +
                "        <div class=\"config-item\">\n" +
                "            <span class=\"config-label\">功能可用</span>\n" +
                "            <span class=\"config-value\">%s</span>\n" +
                "        </div>\n",
                config.get("enabled"),
                config.get("maxResults"),
                config.get("searchTimeout"),
                (Boolean) config.get("hasApiKey") ? "已配置" : "未配置",
                stats != null ? stats.get("cacheSize") : 0,
                available));
        } else {
            html.append(
                "        <div class=\"config-item\">\n" +
                "            <span class=\"config-label\">配置加载失败</span>\n" +
                "            <span class=\"config-value\">请检查服务状态</span>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <div>\n");
        }

        html.append(
                "    </div>\n" +
                "</div>\n" +
                "</div>\n" +
                "\n" +
                "<!-- 加载指示器 -->\n" +
                "<div id=\"loading\" class=\"loading\">\n" +
                "    <div class=\"spinner\"></div>\n" +
                "    <div>处理中...</div>\n" +
                "</div>\n" +
                "</div>\n" +
                "\n" +
                "<script>\n" +
                "    function showLoading() { document.getElementById('loading').style.display = 'block'; }\n" +
                "    function hideLoading() { document.getElementById('loading').style.display = 'none'; }\n" +
                "    function showResult(elementId, content, isError = false) {\n" +
                "        const element = document.getElementById(elementId);\n" +
                "        element.style.display = 'block';\n" +
                "        element.innerHTML = content;\n" +
                "        element.style.color = isError ? '#e74c3c' : '#2c3e50';\n" +
                "    }\n" +
                "\n" +
                "    async function apiCall(url, method = 'GET', body = null) {\n" +
                "        showLoading();\n" +
                "        try {\n" +
                "            const options = { method: method, headers: { 'Content-Type': 'application/json' } };\n" +
                "            if (body) options.body = JSON.stringify(body);\n" +
                "            const response = await fetch(url, options);\n" +
                "            const result = await response.json();\n" +
                "            return result;\n" +
                "        } catch (error) {\n" +
                "            throw new Error('网络请求失败: ' + error.message);\n" +
                "        } finally {\n" +
                "            hideLoading();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    async function enableWebSearch() {\n" +
                "        try {\n" +
                "            const result = await apiCall('/api/web-search/enable', 'POST');\n" +
                "            if (result.success) { alert('联网搜索已启用'); location.reload(); } \n" +
                "            else { alert('启用失败: ' + result.message); }\n" +
                "        } catch (error) { alert('操作失败: ' + error.message); }\n" +
                "    }\n" +
                "\n" +
                "    async function disableWebSearch() {\n" +
                "        try {\n" +
                "            const result = await apiCall('/api/web-search/disable', 'POST');\n" +
                "            if (result.success) { alert('联网搜索已禁用'); location.reload(); } \n" +
                "            else { alert('禁用失败: ' + result.message); }\n" +
                "        } catch (error) { alert('操作失败: ' + error.message); }\n" +
                "    }\n" +
                "\n" +
                "    async function clearCache() {\n" +
                "        try {\n" +
                "            const result = await apiCall('/api/web-search/clear-cache', 'POST');\n" +
                "            if (result.success) { alert('缓存已清空'); location.reload(); } \n" +
                "            else { alert('清空失败: ' + result.message); }\n" +
                "        } catch (error) { alert('操作失败: ' + error.message); }\n" +
                "    }\n" +
                "\n" +
                "    function refreshStatus() { location.reload(); }\n" +
                "\n" +
                "    async function testWebSearch() {\n" +
                "        const query = document.getElementById('testQuery').value;\n" +
                "        if (!query.trim()) { alert('请输入测试查询内容'); return; }\n" +
                "        try {\n" +
                "            const result = await apiCall(`/api/web-search/test?query=${encodeURIComponent(query)}`, 'POST');\n" +
                "            let resultText = `测试时间: ${result.testTime}\\n查询内容: ${result.query}\\n测试结果: ${result.success ? '成功' : '失败'}\\n`;\n" +
                "            if (result.responseTime) resultText += `响应时间: ${result.responseTime}ms\\n`;\n" +
                "            if (result.responseLength) resultText += `响应长度: ${result.responseLength} 字符\\n`;\n" +
                "            if (result.error) resultText += `错误信息: ${result.error}\\n`;\n" +
                "            if (result.response) resultText += `\\n搜索响应:\\n${result.response}`;\n" +
                "            showResult('testResult', resultText, !result.success);\n" +
                "        } catch (error) { showResult('testResult', '测试失败: ' + error.message, true); }\n" +
                "    }\n" +
                "\n" +
                "    async function testRawApi() {\n" +
                "        const query = document.getElementById('testQuery').value;\n" +
                "        if (!query.trim()) { alert('请输入测试查询内容'); return; }\n" +
                "        try {\n" +
                "            const result = await apiCall(`/api/debug/deepseek/test-raw-api?query=${encodeURIComponent(query)}`, 'POST');\n" +
                "            let resultText = `=== 原始API测试结果 ===\\n查询内容: ${result.query}\\n测试结果: ${result.success ? '成功' : '失败'}\\n响应时间: ${result.responseTime}ms\\n提示词长度: ${result.promptLength} 字符\\n响应长度: ${result.responseLength} 字符\\n有响应内容: ${result.hasResponse ? '是' : '否'}\\n`;\n" +
                "            if (result.analysis) {\n" +
                "                resultText += `\\n=== 响应分析 ===\\n类型: ${result.analysis.type}\\n描述: ${result.analysis.description}\\n长度: ${result.analysis.length}\\n行数: ${result.analysis.lines}\\n包含事件: ${result.analysis.contains_events ? '是' : '否'}\\n包含JSON: ${result.analysis.contains_json ? '是' : '否'}\\n`;\n" +
                "                if (result.analysis.has_events !== undefined) {\n" +
                "                    resultText += `有events字段: ${result.analysis.has_events ? '是' : '否'}\\n`;\n" +
                "                    if (result.analysis.events_count !== undefined) resultText += `事件数量: ${result.analysis.events_count}\\n`;\n" +
                "                }\n" +
                "            }\n" +
                "            if (result.error) resultText += `\\n错误信息: ${result.error}\\n`;\n" +
                "            if (result.rawResponse) resultText += `\\n=== 原始响应 ===\\n${result.rawResponse}`;\n" +
                "            showResult('rawResult', resultText, !result.success);\n" +
                "        } catch (error) { showResult('rawResult', '原始API测试失败: ' + error.message, true); }\n" +
                "    }\n" +
                "\n" +
                "    async function testWebSearchFormats() {\n" +
                "        try {\n" +
                "            showResult('formatResult', '正在测试不同的联网搜索参数格式，请稍候...', false);\n" +
                "            const result = await apiCall('/api/debug/deepseek/test-websearch-formats', 'POST');\n" +
                "            \n" +
                "            let resultText = `=== 联网搜索格式测试结果 ===\\n`;\n" +
                "            resultText += `测试查询: ${result.testQuery}\\n`;\n" +
                "            resultText += `测试时间: ${result.testTime}\\n`;\n" +
                "            resultText += `总测试数: ${result.totalTests}\\n\\n`;\n" +
                "            \n" +
                "            if (result.analysis) {\n" +
                "                resultText += `=== 分析结果 ===\\n`;\n" +
                "                resultText += `成功格式: ${result.analysis.successfulFormats.join(', ')}\\n`;\n" +
                "                resultText += `联网搜索格式: ${result.analysis.webSearchFormats.join(', ')}\\n`;\n" +
                "                resultText += `最佳格式: ${result.analysis.bestFormat}\\n`;\n" +
                "                resultText += `最佳评分: ${result.analysis.bestScore}%\\n`;\n" +
                "                resultText += `建议: ${result.analysis.recommendation}\\n\\n`;\n" +
                "            }\n" +
                "            \n" +
                "            if (result.results) {\n" +
                "                resultText += `=== 详细测试结果 ===\\n`;\n" +
                "                result.results.forEach((test, index) => {\n" +
                "                    resultText += `\\n${index + 1}. ${test.format}:\\n`;\n" +
                "                    resultText += `   成功: ${test.success ? '是' : '否'}\\n`;\n" +
                "                    if (test.success) {\n" +
                "                        resultText += `   响应时间: ${test.responseTime}ms\\n`;\n" +
                "                        if (test.analysis) {\n" +
                "                            resultText += `   提到2025年: ${test.analysis.mentions2025 ? '是' : '否'}\\n`;\n" +
                "                            resultText += `   提到最新信息: ${test.analysis.mentionsLatest ? '是' : '否'}\\n`;\n" +
                "                            resultText += `   提到信息来源: ${test.analysis.mentionsSources ? '是' : '否'}\\n`;\n" +
                "                            resultText += `   可能使用联网: ${test.analysis.likelyUsedWebSearch ? '是' : '否'}\\n`;\n" +
                "                            resultText += `   置信度: ${test.analysis.webSearchConfidence}%\\n`;\n" +
                "                        }\n" +
                "                    } else if (test.error) {\n" +
                "                        resultText += `   错误: ${test.error}\\n`;\n" +
                "                    }\n" +
                "                });\n" +
                "            }\n" +
                "            \n" +
                "            showResult('formatResult', resultText, false);\n" +
                "            \n" +
                "        } catch (error) { showResult('formatResult', '格式测试失败: ' + error.message, true); }\n" +
                "    }\n" +
                "\n" +
                "    document.addEventListener('DOMContentLoaded', function() { console.log('联网搜索管理页面已加载'); });\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>\n");

        return html.toString();
    }
}