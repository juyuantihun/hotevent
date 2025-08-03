/**
 * 请求缓存工具
 * 用于缓存API请求结果，减少重复请求
 */

// 缓存项类型
interface CacheItem<T> {
  data: T;
  timestamp: number;
  expiry: number;
}

// 缓存配置
interface CacheConfig {
  // 缓存有效期（毫秒）
  ttl: number;
  // 是否在后台自动刷新缓存
  backgroundRefresh: boolean;
  // 缓存键生成函数
  keyGenerator?: (url: string, params: any) => string;
}

// 默认缓存配置
const defaultConfig: CacheConfig = {
  ttl: 5 * 60 * 1000, // 5分钟
  backgroundRefresh: true,
  keyGenerator: (url: string, params: any) => {
    // 默认使用URL和参数的组合作为缓存键
    return `${url}:${JSON.stringify(params || {})}`
  }
};

// 缓存存储
const cacheStore = new Map<string, CacheItem<any>>();

// 正在进行的请求
const pendingRequests = new Map<string, Promise<any>>();

/**
 * 获取缓存项
 * @param key 缓存键
 * @returns 缓存项或undefined
 */
function getCacheItem<T>(key: string): CacheItem<T> | undefined {
  return cacheStore.get(key);
}

/**
 * 设置缓存项
 * @param key 缓存键
 * @param data 数据
 * @param ttl 有效期（毫秒）
 */
function setCacheItem<T>(key: string, data: T, ttl: number): void {
  const now = Date.now();
  cacheStore.set(key, {
    data,
    timestamp: now,
    expiry: now + ttl
  });
}

/**
 * 检查缓存项是否有效
 * @param item 缓存项
 * @returns 是否有效
 */
function isCacheValid<T>(item: CacheItem<T> | undefined): boolean {
  if (!item) return false;
  return Date.now() < item.expiry;
}

/**
 * 检查缓存项是否需要在后台刷新
 * @param item 缓存项
 * @param ttl 有效期（毫秒）
 * @returns 是否需要刷新
 */
function shouldRefreshInBackground<T>(item: CacheItem<T>, ttl: number): boolean {
  // 如果缓存已经过期，则不需要在后台刷新
  if (Date.now() >= item.expiry) return false;
  
  // 如果缓存已经使用了超过一半的有效期，则需要在后台刷新
  const halfTtl = ttl / 2;
  return Date.now() - item.timestamp >= halfTtl;
}

/**
 * 清除过期缓存
 */
function clearExpiredCache(): void {
  const now = Date.now();
  for (const [key, item] of cacheStore.entries()) {
    if (now >= item.expiry) {
      cacheStore.delete(key);
    }
  }
}

/**
 * 清除所有缓存
 */
export function clearAllCache(): void {
  cacheStore.clear();
}

/**
 * 清除特定URL的缓存
 * @param url 请求URL
 */
export function clearUrlCache(url: string): void {
  for (const key of cacheStore.keys()) {
    if (key.startsWith(`${url}:`)) {
      cacheStore.delete(key);
    }
  }
}

/**
 * 获取缓存统计信息
 * @returns 缓存统计信息
 */
export function getCacheStats(): { size: number, validItems: number, expiredItems: number } {
  const now = Date.now();
  let validItems = 0;
  let expiredItems = 0;
  
  for (const item of cacheStore.values()) {
    if (now < item.expiry) {
      validItems++;
    } else {
      expiredItems++;
    }
  }
  
  return {
    size: cacheStore.size,
    validItems,
    expiredItems
  };
}

// 定期清理过期缓存
setInterval(clearExpiredCache, 60 * 1000); // 每分钟清理一次

/**
 * 创建带缓存的请求函数
 * @param requestFn 原始请求函数
 * @param config 缓存配置
 * @returns 带缓存的请求函数
 */
export function createCachedRequest<T>(
  requestFn: (params?: any) => Promise<T>,
  config: Partial<CacheConfig> = {}
): (params?: any) => Promise<T> {
  // 合并配置
  const mergedConfig: CacheConfig = { ...defaultConfig, ...config };
  
  return async (params?: any): Promise<T> => {
    // 生成缓存键
    const url = requestFn.name || 'anonymous';
    const cacheKey = mergedConfig.keyGenerator!(url, params);
    
    // 检查是否有正在进行的相同请求
    if (pendingRequests.has(cacheKey)) {
      return pendingRequests.get(cacheKey)!;
    }
    
    // 检查缓存
    const cachedItem = getCacheItem<T>(cacheKey);
    
    if (isCacheValid(cachedItem)) {
      // 如果缓存有效，检查是否需要在后台刷新
      if (mergedConfig.backgroundRefresh && shouldRefreshInBackground(cachedItem!, mergedConfig.ttl)) {
        // 在后台刷新缓存
        setTimeout(async () => {
          try {
            const freshData = await requestFn(params);
            setCacheItem(cacheKey, freshData, mergedConfig.ttl);
          } catch (error) {
            console.error('Background cache refresh failed:', error);
          }
        }, 0);
      }
      
      // 返回缓存的数据
      return cachedItem!.data;
    }
    
    // 如果缓存无效，发起新请求
    try {
      // 记录正在进行的请求
      const requestPromise = requestFn(params);
      pendingRequests.set(cacheKey, requestPromise);
      
      // 等待请求完成
      const data = await requestPromise;
      
      // 缓存结果
      setCacheItem(cacheKey, data, mergedConfig.ttl);
      
      // 返回数据
      return data;
    } finally {
      // 无论成功还是失败，都从正在进行的请求中移除
      pendingRequests.delete(cacheKey);
    }
  };
}

/**
 * 创建带防抖的请求函数
 * @param requestFn 原始请求函数
 * @param delay 防抖延迟（毫秒）
 * @returns 带防抖的请求函数
 */
export function createDebouncedRequest<T>(
  requestFn: (params?: any) => Promise<T>,
  delay: number = 300
): (params?: any) => Promise<T> {
  let timer: number | null = null;
  let lastPromise: Promise<T> | null = null;
  
  return (params?: any): Promise<T> => {
    // 如果已经有一个定时器，取消它
    if (timer !== null) {
      window.clearTimeout(timer);
      timer = null;
    }
    
    // 创建一个新的Promise
    return new Promise<T>((resolve, reject) => {
      // 设置新的定时器
      timer = window.setTimeout(async () => {
        try {
          // 发起请求
          const data = await requestFn(params);
          resolve(data);
        } catch (error) {
          reject(error);
        } finally {
          timer = null;
          lastPromise = null;
        }
      }, delay);
      
      // 保存最后一个Promise
      if (lastPromise === null) {
        lastPromise = new Promise<T>((innerResolve, innerReject) => {
          // 这个Promise会在上面的定时器回调中被解决
        });
      }
    });
  };
}

/**
 * 创建带批处理的请求函数
 * 将短时间内的多个请求合并为一个
 * @param requestFn 原始请求函数（接受一个参数数组）
 * @param delay 批处理延迟（毫秒）
 * @returns 带批处理的请求函数
 */
export function createBatchedRequest<T, P>(
  requestFn: (paramsArray: P[]) => Promise<T[]>,
  delay: number = 50
): (params: P) => Promise<T> {
  let timer: number | null = null;
  let batch: P[] = [];
  let resolvers: Array<(value: T) => void> = [];
  let rejectors: Array<(reason: any) => void> = [];
  
  return (params: P): Promise<T> => {
    return new Promise<T>((resolve, reject) => {
      // 添加到批处理队列
      batch.push(params);
      resolvers.push(resolve);
      rejectors.push(reject);
      
      // 如果已经有一个定时器，不需要再设置
      if (timer !== null) {
        return;
      }
      
      // 设置定时器
      timer = window.setTimeout(async () => {
        // 保存当前批次
        const currentBatch = [...batch];
        const currentResolvers = [...resolvers];
        const currentRejectors = [...rejectors];
        
        // 清空队列
        batch = [];
        resolvers = [];
        rejectors = [];
        timer = null;
        
        try {
          // 发起批处理请求
          const results = await requestFn(currentBatch);
          
          // 解决每个Promise
          results.forEach((result, index) => {
            currentResolvers[index](result);
          });
        } catch (error) {
          // 拒绝所有Promise
          currentRejectors.forEach(reject => reject(error));
        }
      }, delay);
    });
  };
}