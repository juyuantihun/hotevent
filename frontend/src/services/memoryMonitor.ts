/**
 * 内存监控服务
 * 用于检测和防止内存泄漏
 */
import { monitorMemoryUsage, checkMemoryUsage } from '@/utils/memoryManager'

// 内存使用阈值（百分比）
const MEMORY_THRESHOLD = 80

// 检查间隔（毫秒）
const CHECK_INTERVAL = 30000

// 内存监控实例
let monitorInstance: (() => void) | null = null

// 内存使用记录
const memoryUsageHistory: Array<{
  timestamp: number,
  usedJSHeapSize?: number,
  totalJSHeapSize?: number,
  jsHeapSizeLimit?: number
}> = []

// 最大历史记录数
const MAX_HISTORY_LENGTH = 10

/**
 * 启动内存监控
 */
export function startMemoryMonitoring() {
  if (monitorInstance) {
    return
  }
  
  // 开始监控内存使用情况
  monitorInstance = monitorMemoryUsage(CHECK_INTERVAL, MEMORY_THRESHOLD)
  
  // 记录初始内存使用情况
  recordMemoryUsage()
  
  console.log('[内存监控] 已启动')
}

/**
 * 停止内存监控
 */
export function stopMemoryMonitoring() {
  if (monitorInstance) {
    monitorInstance()
    monitorInstance = null
    console.log('[内存监控] 已停止')
  }
}

/**
 * 记录当前内存使用情况
 */
export function recordMemoryUsage() {
  const memory = checkMemoryUsage()
  
  // 添加时间戳
  const record = {
    timestamp: Date.now(),
    ...memory
  }
  
  // 添加到历史记录
  memoryUsageHistory.push(record)
  
  // 限制历史记录长度
  if (memoryUsageHistory.length > MAX_HISTORY_LENGTH) {
    memoryUsageHistory.shift()
  }
  
  return record
}

/**
 * 获取内存使用历史记录
 */
export function getMemoryUsageHistory() {
  return [...memoryUsageHistory]
}

/**
 * 检测内存泄漏
 * 通过比较一段时间内的内存使用趋势来检测可能的内存泄漏
 */
export function detectMemoryLeak() {
  if (memoryUsageHistory.length < 3) {
    return {
      detected: false,
      message: '历史数据不足，无法检测内存泄漏'
    }
  }
  
  // 计算内存使用增长率
  const growthRates: number[] = []
  
  for (let i = 1; i < memoryUsageHistory.length; i++) {
    const prev = memoryUsageHistory[i - 1]
    const curr = memoryUsageHistory[i]
    
    if (prev.usedJSHeapSize && curr.usedJSHeapSize) {
      const growthRate = (curr.usedJSHeapSize - prev.usedJSHeapSize) / prev.usedJSHeapSize * 100
      growthRates.push(growthRate)
    }
  }
  
  // 计算平均增长率
  const avgGrowthRate = growthRates.reduce((sum, rate) => sum + rate, 0) / growthRates.length
  
  // 如果平均增长率持续为正且超过阈值，可能存在内存泄漏
  const LEAK_THRESHOLD = 5 // 5%的持续增长率被视为可能的内存泄漏
  
  if (avgGrowthRate > LEAK_THRESHOLD) {
    return {
      detected: true,
      message: `检测到可能的内存泄漏，内存使用平均增长率: ${avgGrowthRate.toFixed(2)}%`,
      growthRate: avgGrowthRate,
      history: getMemoryUsageHistory()
    }
  }
  
  return {
    detected: false,
    message: '未检测到内存泄漏',
    growthRate: avgGrowthRate,
    history: getMemoryUsageHistory()
  }
}

/**
 * 尝试释放内存
 * 通过触发垃圾回收和清理缓存来释放内存
 */
export function tryFreeMemory() {
  // 记录释放前的内存使用情况
  const before = recordMemoryUsage()
  
  // 清理可能的内存泄漏源
  // 1. 清除未使用的事件监听器
  // 2. 清除未使用的定时器
  // 3. 清除组件缓存
  // 4. 清除路由缓存
  
  // 提示浏览器进行垃圾回收（注意：这只是一个建议，不保证立即执行）
  if (window.gc) {
    try {
      window.gc()
    } catch (e) {
      console.warn('[内存监控] 无法手动触发垃圾回收')
    }
  }
  
  // 记录释放后的内存使用情况
  setTimeout(() => {
    const after = recordMemoryUsage()
    
    if (before.usedJSHeapSize && after.usedJSHeapSize) {
      const freed = before.usedJSHeapSize - after.usedJSHeapSize
      const freedMB = freed / (1024 * 1024)
      
      if (freed > 0) {
        console.log(`[内存监控] 成功释放 ${freedMB.toFixed(2)} MB 内存`)
      } else {
        console.log('[内存监控] 未能释放内存')
      }
    }
  }, 1000)
}