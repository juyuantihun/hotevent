<template>
  <div class="websearch-container">
    <el-card class="header-card">
      <div slot="header" class="clearfix">
        <span class="card-title">DeepSeek联网搜索管理</span>
        <el-button 
          style="float: right; padding: 3px 0" 
          type="text" 
          @click="refreshStatus"
          :loading="loading"
        >
          刷新状态
        </el-button>
      </div>
      
      <!-- 状态概览 -->
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="status-card">
            <div class="status-item">
              <div class="status-label">联网搜索状态</div>
              <div class="status-value" :class="status.available ? 'status-success' : 'status-error'">
                {{ status.available ? '可用' : '不可用' }}
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="status-card">
            <div class="status-item">
              <div class="status-label">总请求数</div>
              <div class="status-value">{{ stats.totalRequests || 0 }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="status-card">
            <div class="status-item">
              <div class="status-label">成功率</div>
              <div class="status-value">{{ (stats.successRate || 0).toFixed(1) }}%</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="status-card">
            <div class="status-item">
              <div class="status-label">平均响应时间</div>
              <div class="status-value">{{ (stats.averageResponseTime || 0).toFixed(0) }}ms</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 控制面板 -->
    <el-card class="control-card">
      <div slot="header" class="clearfix">
        <span class="card-title">控制面板</span>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="control-section">
            <h4>功能控制</h4>
            <el-button-group>
              <el-button 
                type="success" 
                @click="enableWebSearch"
                :loading="operationLoading"
                :disabled="status.available"
              >
                启用联网搜索
              </el-button>
              <el-button 
                type="warning" 
                @click="disableWebSearch"
                :loading="operationLoading"
                :disabled="!status.available"
              >
                禁用联网搜索
              </el-button>
              <el-button 
                type="info" 
                @click="clearCache"
                :loading="operationLoading"
              >
                清除缓存
              </el-button>
            </el-button-group>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="control-section">
            <h4>测试功能</h4>
            <el-input
              v-model="testQuery"
              placeholder="输入测试查询内容"
              style="width: 200px; margin-right: 10px;"
            />
            <el-button 
              type="primary" 
              @click="testWebSearch"
              :loading="testLoading"
            >
              测试联网搜索
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 配置管理 -->
    <el-card class="config-card">
      <div slot="header" class="clearfix">
        <span class="card-title">配置管理</span>
        <el-button 
          style="float: right; padding: 3px 0" 
          type="text" 
          @click="saveConfig"
          :loading="configLoading"
        >
          保存配置
        </el-button>
      </div>
      
      <el-form :model="config" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="启用状态">
              <el-switch v-model="config.enabled" />
            </el-form-item>
            <el-form-item label="最大结果数">
              <el-input-number 
                v-model="config.maxResults" 
                :min="1" 
                :max="50" 
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="搜索超时(ms)">
              <el-input-number 
                v-model="config.searchTimeout" 
                :min="5000" 
                :max="60000" 
                :step="1000"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="API密钥状态">
              <el-tag :type="config.hasApiKey ? 'success' : 'danger'">
                {{ config.hasApiKey ? '已配置' : '未配置' }}
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 测试结果 -->
    <el-card v-if="testResult" class="result-card">
      <div slot="header" class="clearfix">
        <span class="card-title">测试结果</span>
        <el-tag 
          :type="testResult.success ? 'success' : 'danger'"
          style="float: right;"
        >
          {{ testResult.success ? '成功' : '失败' }}
        </el-tag>
      </div>
      
      <div class="test-result">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="测试时间">
            {{ testResult.testTime }}
          </el-descriptions-item>
          <el-descriptions-item label="查询内容">
            {{ testResult.query }}
          </el-descriptions-item>
          <el-descriptions-item label="响应时间" v-if="testResult.responseTime">
            {{ testResult.responseTime }}ms
          </el-descriptions-item>
          <el-descriptions-item label="响应长度" v-if="testResult.responseLength">
            {{ testResult.responseLength }} 字符
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="testResult.error" class="error-message">
          <h4>错误信息：</h4>
          <el-alert :title="testResult.error" type="error" :closable="false" />
        </div>
        
        <div v-if="testResult.response" class="response-content">
          <h4>搜索响应：</h4>
          <el-input
            type="textarea"
            :rows="10"
            :value="testResult.response"
            readonly
          />
        </div>
      </div>
    </el-card>

    <!-- 统计信息 -->
    <el-card class="stats-card">
      <div slot="header" class="clearfix">
        <span class="card-title">详细统计</span>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-descriptions title="请求统计" :column="1" border>
            <el-descriptions-item label="总请求数">
              {{ stats.totalRequests || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="成功请求数">
              {{ stats.successfulRequests || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="失败请求数">
              {{ stats.failedRequests || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="成功率">
              {{ (stats.successRate || 0).toFixed(2) }}%
            </el-descriptions-item>
          </el-descriptions>
        </el-col>
        <el-col :span="12">
          <el-descriptions title="性能统计" :column="1" border>
            <el-descriptions-item label="总搜索时间">
              {{ stats.totalSearchTime || 0 }}ms
            </el-descriptions-item>
            <el-descriptions-item label="平均响应时间">
              {{ (stats.averageResponseTime || 0).toFixed(2) }}ms
            </el-descriptions-item>
            <el-descriptions-item label="缓存大小">
              {{ stats.cacheSize || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="功能状态">
              <el-tag :type="stats.isEnabled ? 'success' : 'info'">
                {{ stats.isEnabled ? '已启用' : '已禁用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'WebSearchManagement',
  data() {
    return {
      loading: false,
      operationLoading: false,
      testLoading: false,
      configLoading: false,
      status: {
        available: false,
        config: {},
        stats: {}
      },
      config: {
        enabled: false,
        maxResults: 10,
        searchTimeout: 30000,
        hasApiKey: false
      },
      stats: {},
      testQuery: '最新科技新闻',
      testResult: null
    }
  },
  mounted() {
    this.refreshStatus()
  },
  methods: {
    async refreshStatus() {
      this.loading = true
      try {
        const response = await axios.get('/api/web-search/status')
        this.status = response.data
        this.config = { ...response.data.config }
        this.stats = response.data.stats
        
        this.$message.success('状态刷新成功')
      } catch (error) {
        console.error('获取状态失败:', error)
        this.$message.error('获取状态失败: ' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    
    async enableWebSearch() {
      this.operationLoading = true
      try {
        const response = await axios.post('/api/web-search/enable')
        if (response.data.success) {
          this.$message.success('联网搜索已启用')
          await this.refreshStatus()
        } else {
          this.$message.error(response.data.message)
        }
      } catch (error) {
        console.error('启用联网搜索失败:', error)
        this.$message.error('启用失败: ' + (error.response?.data?.message || error.message))
      } finally {
        this.operationLoading = false
      }
    },
    
    async disableWebSearch() {
      this.operationLoading = true
      try {
        const response = await axios.post('/api/web-search/disable')
        if (response.data.success) {
          this.$message.success('联网搜索已禁用')
          await this.refreshStatus()
        } else {
          this.$message.error(response.data.message)
        }
      } catch (error) {
        console.error('禁用联网搜索失败:', error)
        this.$message.error('禁用失败: ' + (error.response?.data?.message || error.message))
      } finally {
        this.operationLoading = false
      }
    },
    
    async clearCache() {
      this.operationLoading = true
      try {
        const response = await axios.post('/api/web-search/clear-cache')
        if (response.data.success) {
          this.$message.success('缓存已清空')
          await this.refreshStatus()
        } else {
          this.$message.error(response.data.message)
        }
      } catch (error) {
        console.error('清除缓存失败:', error)
        this.$message.error('清除缓存失败: ' + (error.response?.data?.message || error.message))
      } finally {
        this.operationLoading = false
      }
    },
    
    async testWebSearch() {
      if (!this.testQuery.trim()) {
        this.$message.warning('请输入测试查询内容')
        return
      }
      
      this.testLoading = true
      this.testResult = null
      try {
        const response = await axios.post('/api/web-search/test', null, {
          params: { query: this.testQuery }
        })
        this.testResult = response.data
        
        if (response.data.success) {
          this.$message.success('测试完成')
        } else {
          this.$message.error('测试失败: ' + response.data.error)
        }
      } catch (error) {
        console.error('测试联网搜索失败:', error)
        this.testResult = {
          success: false,
          error: error.response?.data?.message || error.message,
          query: this.testQuery,
          testTime: new Date().toLocaleString()
        }
        this.$message.error('测试失败: ' + (error.response?.data?.message || error.message))
      } finally {
        this.testLoading = false
      }
    },
    
    async saveConfig() {
      this.configLoading = true
      try {
        const response = await axios.post('/api/web-search/config', this.config)
        if (response.data.success) {
          this.$message.success('配置保存成功')
          await this.refreshStatus()
        } else {
          this.$message.error(response.data.message)
        }
      } catch (error) {
        console.error('保存配置失败:', error)
        this.$message.error('保存配置失败: ' + (error.response?.data?.message || error.message))
      } finally {
        this.configLoading = false
      }
    }
  }
}
</script>

<style scoped>
.websearch-container {
  padding: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.header-card, .control-card, .config-card, .result-card, .stats-card {
  margin-bottom: 20px;
}

.status-card {
  text-align: center;
}

.status-item {
  padding: 10px;
}

.status-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.status-value {
  font-size: 18px;
  font-weight: bold;
}

.status-success {
  color: #67C23A;
}

.status-error {
  color: #F56C6C;
}

.control-section {
  margin-bottom: 20px;
}

.control-section h4 {
  margin-bottom: 10px;
  color: #606266;
}

.test-result {
  margin-top: 15px;
}

.error-message, .response-content {
  margin-top: 15px;
}

.error-message h4, .response-content h4 {
  margin-bottom: 10px;
  color: #606266;
}
</style>