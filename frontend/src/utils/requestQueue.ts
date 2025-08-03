/**
 * API请求队列管理器
 * 用于优化并发请求
 */

// 请求优先级
export enum RequestPriority {
  HIGH = 0,
  NORMAL = 1,
  LOW = 2
}

// 请求项
interface QueueItem<T> {
  id: string;
  priority: RequestPriority;
  execute: () => Promise<T>;
  resolve: (value: T | PromiseLike<T>) => void;
  reject: (reason?: any) => void;
  timestamp: number;
  timeout?: number;
  timeoutId?: number;
}

// 队列配置
interface QueueConfig {
  // 最大并发请求数
  maxConcurrent: number;
  // 默认请求超时时间（毫秒）
  defaultTimeout: number;
  // 是否启用优先级排序
  enablePriority: boolean;
  // 是否启用请求合并
  enableMerging: boolean;
  // 请求合并时间窗口（毫秒）
  mergingWindow: number;
}

// 默认队列配置
const defaultConfig: QueueConfig = {
  maxConcurrent: 6,
  defaultTimeout: 30000,
  enablePriority: true,
  enableMerging: true,
  mergingWindow: 50
};

/**
 * 请求队列类
 */
export class RequestQueue {
  private queue: QueueItem<any>[] = [];
  private activeCount = 0;
  private config: QueueConfig;
  private mergeMap = new Map<string, QueueItem<any>>();
  private mergeTimers = new Map<string, number>();
  
  /**
   * 构造函数
   * @param config 队列配置
   */
  constructor(config: Partial<QueueConfig> = {}) {
    this.config = { ...defaultConfig, ...config };
  }
  
  /**
   * 添加请求到队列
   * @param id 请求ID（用于去重和合并）
   * @param execute 执行函数
   * @param priority 请求优先级
   * @param timeout 请求超时时间（毫秒）
   * @returns Promise
   */
  public enqueue<T>(
    id: string,
    execute: () => Promise<T>,
    priority: RequestPriority = RequestPriority.NORMAL,
    timeout?: number
  ): Promise<T> {
    return new Promise<T>((resolve, reject) => {
      // 创建请求项
      const item: QueueItem<T> = {
        id,
        priority,
        execute,
        resolve,
        reject,
        timestamp: Date.now(),
        timeout: timeout || this.config.defaultTimeout
      };
      
      // 如果启用了请求合并，尝试合并相同ID的请求
      if (this.config.enableMerging && id) {
        const existingMergeTimer = this.mergeTimers.get(id);
        
        if (existingMergeTimer) {
          // 如果已经有一个合并定时器，取消它
          window.clearTimeout(existingMergeTimer);
        }
        
        // 存储当前请求项
        this.mergeMap.set(id, item);
        
        // 设置新的合并定时器
        const mergeTimer = window.setTimeout(() => {
          // 合并时间窗口结束，处理请求
          const mergedItem = this.mergeMap.get(id);
          if (mergedItem) {
            this.mergeMap.delete(id);
            this.mergeTimers.delete(id);
            this.addToQueue(mergedItem);
          }
        }, this.config.mergingWindow);
        
        this.mergeTimers.set(id, mergeTimer);
        return;
      }
      
      // 直接添加到队列
      this.addToQueue(item);
    });
  }
  
  /**
   * 添加请求项到队列
   * @param item 请求项
   */
  private addToQueue<T>(item: QueueItem<T>): void {
    // 添加到队列
    this.queue.push(item);
    
    // 如果启用了优先级排序，按优先级排序
    if (this.config.enablePriority) {
      this.queue.sort((a, b) => {
        // 首先按优先级排序
        if (a.priority !== b.priority) {
          return a.priority - b.priority;
        }
        // 然后按时间戳排序（先进先出）
        return a.timestamp - b.timestamp;
      });
    }
    
    // 设置超时处理
    if (item.timeout) {
      item.timeoutId = window.setTimeout(() => {
        // 从队列中移除
        const index = this.queue.indexOf(item);
        if (index !== -1) {
          this.queue.splice(index, 1);
        }
        
        // 拒绝Promise
        item.reject(new Error('Request timeout'));
      }, item.timeout);
    }
    
    // 尝试处理队列
    this.processQueue();
  }
  
  /**
   * 处理队列
   */
  private processQueue(): void {
    // 如果没有请求或已达到最大并发数，不处理
    if (this.queue.length === 0 || this.activeCount >= this.config.maxConcurrent) {
      return;
    }
    
    // 取出队列头部的请求
    const item = this.queue.shift();
    if (!item) return;
    
    // 清除超时定时器
    if (item.timeoutId) {
      window.clearTimeout(item.timeoutId);
    }
    
    // 增加活动请求计数
    this.activeCount++;
    
    // 执行请求
    item.execute()
      .then(result => {
        // 解决Promise
        item.resolve(result);
      })
      .catch(error => {
        // 拒绝Promise
        item.reject(error);
      })
      .finally(() => {
        // 减少活动请求计数
        this.activeCount--;
        
        // 继续处理队列
        this.processQueue();
      });
  }
  
  /**
   * 取消所有请求
   * @param reason 取消原因
   */
  public cancelAll(reason: string = 'Request cancelled'): void {
    // 取消所有合并中的请求
    for (const [id, timer] of this.mergeTimers.entries()) {
      window.clearTimeout(timer);
      const item = this.mergeMap.get(id);
      if (item) {
        item.reject(new Error(reason));
      }
    }
    
    // 清空合并映射
    this.mergeMap.clear();
    this.mergeTimers.clear();
    
    // 取消所有队列中的请求
    for (const item of this.queue) {
      if (item.timeoutId) {
        window.clearTimeout(item.timeoutId);
      }
      item.reject(new Error(reason));
    }
    
    // 清空队列
    this.queue = [];
  }
  
  /**
   * 获取队列状态
   * @returns 队列状态
   */
  public getStatus(): {
    queueLength: number;
    activeCount: number;
    mergeCount: number;
  } {
    return {
      queueLength: this.queue.length,
      activeCount: this.activeCount,
      mergeCount: this.mergeMap.size
    };
  }
}

// 创建默认请求队列实例
export const defaultQueue = new RequestQueue();

/**
 * 使用请求队列包装请求函数
 * @param requestFn 请求函数
 * @param id 请求ID
 * @param priority 请求优先级
 * @param timeout 请求超时时间（毫秒）
 * @param queue 请求队列
 * @returns 包装后的请求函数
 */
export function withQueue<T, P extends any[]>(
  requestFn: (...args: P) => Promise<T>,
  id: string | ((...args: P) => string),
  priority: RequestPriority = RequestPriority.NORMAL,
  timeout?: number,
  queue: RequestQueue = defaultQueue
): (...args: P) => Promise<T> {
  return (...args: P): Promise<T> => {
    // 生成请求ID
    const requestId = typeof id === 'function' ? id(...args) : id;
    
    // 添加到队列
    return queue.enqueue(
      requestId,
      () => requestFn(...args),
      priority,
      timeout
    );
  };
}