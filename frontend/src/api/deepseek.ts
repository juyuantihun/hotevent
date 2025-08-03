import request from './index'

export interface DeepSeekStatus {
  connected: boolean
  message: string
  timestamp: number
}

export interface FetchResult {
  totalFetched: number
  successSaved: number
  events: any[]
  keywords?: string[]
  startDate?: string
  endDate?: string
}

// DeepSeek API接口
export const deepseekApi = {
  // 检查连接状态
  checkStatus(): Promise<DeepSeekStatus> {
    return request({
      url: '/deepseek/status',
      method: 'get'
    })
  },

  // 抓取最新事件
  fetchLatestEvents(limit: number = 5): Promise<FetchResult> {
    return request({
      url: '/deepseek/fetch/latest',
      method: 'post',
      params: { limit }
    })
  },

  // 根据关键词抓取事件
  fetchByKeywords(keywords: string[], limit: number = 5): Promise<FetchResult> {
    return request({
      url: '/deepseek/fetch/keywords',
      method: 'post',
      params: { limit },
      data: keywords
    })
  },

  // 根据日期范围抓取事件
  fetchByDateRange(startDate: string, endDate: string, limit: number = 5): Promise<FetchResult> {
    return request({
      url: '/deepseek/fetch/daterange',
      method: 'post',
      params: {
        startDate,
        endDate,
        limit
      }
    })
  },

  // 触发定时任务
  triggerTask(): Promise<string> {
    return request({
      url: '/deepseek/task/trigger',
      method: 'post'
    })
  },

  // 解析GDELT数据
  parseGdeltData(gdeltData: string): Promise<{ totalParsed: number; events: any[] }> {
    return request({
      url: '/deepseek/parse/gdelt',
      method: 'post',
      data: gdeltData
    })
  }
} 