/**
 * 优化的API请求客户端
 * 使用缓存和批处理策略优化网络请求
 */
import service from './index'
import { createCachedRequest, createDebouncedRequest, createBatchedRequest } from '@/utils/requestCache'

// 请求配置类型
interface RequestConfig {
  // 是否启用缓存
  useCache?: boolean;
  // 缓存有效期（毫秒）
  cacheTTL?: number;
  // 是否在后台自动刷新缓存
  backgroundRefresh?: boolean;
  // 是否使用防抖
  useDebounce?: boolean;
  // 防抖延迟（毫秒）
  debounceDelay?: number;
  // 是否使用批处理
  useBatch?: boolean;
  // 批处理延迟（毫秒）
  batchDelay?: number;
  // 请求超时时间（毫秒）
  timeout?: number;
  // 重试次数
  retries?: number;
  // 重试延迟（毫秒）
  retryDelay?: number;
}

// 默认请求配置
const defaultConfig: RequestConfig = {
  useCache: false,
  cacheTTL: 5 * 60 * 1000, // 5分钟
  backgroundRefresh: true,
  useDebounce: false,
  debounceDelay: 300,
  useBatch: false,
  batchDelay: 50,
  timeout: 10000,
  retries: 0,
  retryDelay: 1000
};

/**
 * 创建优化的GET请求
 * @param url 请求URL
 * @param config 请求配置
 * @returns GET请求函数
 */
export function createOptimizedGet<T>(url: string, config: RequestConfig = {}) {
  // 合并配置
  const mergedConfig = { ...defaultConfig, ...config };
  
  // 基础GET请求函数
  const baseGet = async (params?: any): Promise<T> => {
    try {
      // 直接调用真实API，不再使用模拟数据
      return await service.get(url, { params, timeout: mergedConfig.timeout });
    } catch (error) {
      // 如果配置了重试，则进行重试
      if (mergedConfig.retries && mergedConfig.retries > 0) {
        return retryRequest(() => service.get(url, { params, timeout: mergedConfig.timeout }), mergedConfig.retries, mergedConfig.retryDelay!);
      }
      
      // 如果请求失败，记录错误并抛出
      console.error(`API请求失败: ${url}`, error);
      throw error;
    }
  };
  
  // 获取模拟数据
  const getMockData = async (url: string, params?: any): Promise<any> => {
    // 模拟延迟
    await new Promise(resolve => setTimeout(resolve, 300));
    
    // 地区树
    if (url === '/regions/tree') {
      return [
        {
          id: 1,
          name: '亚洲',
          type: 'continent',
          level: 1,
          parentId: null,
          children: [
            {
              id: 101,
              name: '中国',
              type: 'country',
              level: 2,
              parentId: 1,
              children: [
                {
                  id: 10101,
                  name: '北京市',
                  type: 'province',
                  level: 3,
                  parentId: 101,
                  children: [
                    {
                      id: 1010101,
                      name: '海淀区',
                      type: 'city',
                      level: 4,
                      parentId: 10101,
                      children: []
                    },
                    {
                      id: 1010102,
                      name: '朝阳区',
                      type: 'city',
                      level: 4,
                      parentId: 10101,
                      children: []
                    }
                  ]
                },
                {
                  id: 10102,
                  name: '上海市',
                  type: 'province',
                  level: 3,
                  parentId: 101,
                  children: [
                    {
                      id: 1010201,
                      name: '浦东新区',
                      type: 'city',
                      level: 4,
                      parentId: 10102,
                      children: []
                    },
                    {
                      id: 1010202,
                      name: '黄浦区',
                      type: 'city',
                      level: 4,
                      parentId: 10102,
                      children: []
                    }
                  ]
                }
              ]
            },
            {
              id: 102,
              name: '日本',
              type: 'country',
              level: 2,
              parentId: 1,
              children: [
                {
                  id: 10201,
                  name: '东京都',
                  type: 'province',
                  level: 3,
                  parentId: 102,
                  children: []
                },
                {
                  id: 10202,
                  name: '大阪府',
                  type: 'province',
                  level: 3,
                  parentId: 102,
                  children: []
                }
              ]
            }
          ]
        },
        {
          id: 2,
          name: '欧洲',
          type: 'continent',
          level: 1,
          parentId: null,
          children: [
            {
              id: 201,
              name: '法国',
              type: 'country',
              level: 2,
              parentId: 2,
              children: [
                {
                  id: 20101,
                  name: '巴黎',
                  type: 'province',
                  level: 3,
                  parentId: 201,
                  children: []
                }
              ]
            },
            {
              id: 202,
              name: '德国',
              type: 'country',
              level: 2,
              parentId: 2,
              children: [
                {
                  id: 20201,
                  name: '柏林',
                  type: 'province',
                  level: 3,
                  parentId: 202,
                  children: []
                }
              ]
            }
          ]
        }
      ];
    }
    
    // 地区详情
    if (url.match(/^\/regions\/\d+$/)) {
      const id = parseInt(url.split('/')[2]);
      return {
        id,
        name: `地区${id}`,
        type: id < 100 ? 'continent' : id < 10000 ? 'country' : id < 1000000 ? 'province' : 'city',
        level: id < 100 ? 1 : id < 10000 ? 2 : id < 1000000 ? 3 : 4,
        parentId: id < 100 ? null : Math.floor(id / 100)
      };
    }
    
    // 地区祖先
    if (url.match(/^\/regions\/\d+\/ancestors$/)) {
      const id = parseInt(url.split('/')[2]);
      const ancestors = [];
      
      let currentId = id;
      while (currentId > 100) {
        const parentId = Math.floor(currentId / 100);
        ancestors.push({
          id: parentId,
          name: `地区${parentId}`,
          type: parentId < 100 ? 'continent' : parentId < 10000 ? 'country' : 'province',
          level: parentId < 100 ? 1 : parentId < 10000 ? 2 : 3,
          parentId: parentId < 100 ? null : Math.floor(parentId / 100)
        });
        currentId = parentId;
      }
      
      return ancestors;
    }
    
    // 地区后代
    if (url.match(/^\/regions\/\d+\/descendants$/)) {
      const id = parseInt(url.split('/')[2]);
      const descendants = [];
      
      // 生成子地区
      for (let i = 1; i <= 5; i++) {
        const childId = id * 100 + i;
        descendants.push({
          id: childId,
          name: `地区${childId}`,
          type: id < 100 ? 'country' : id < 10000 ? 'province' : 'city',
          level: id < 100 ? 2 : id < 10000 ? 3 : 4,
          parentId: id
        });
      }
      
      return descendants;
    }
    
    // 地区搜索
    if (url === '/regions/search') {
      const keyword = params?.keyword || '';
      const results = [];
      
      // 生成搜索结果
      for (let i = 1; i <= 10; i++) {
        const id = 1000 + i;
        results.push({
          id,
          name: `${keyword}相关地区${i}`,
          type: i % 4 === 0 ? 'continent' : i % 4 === 1 ? 'country' : i % 4 === 2 ? 'province' : 'city',
          level: (i % 4) + 1,
          parentId: i % 4 === 0 ? null : Math.floor(id / 10)
        });
      }
      
      return results;
    }
    
    // 时间线列表
    if (url === '/timelines') {
      const page = parseInt(params?.page || '0');
      const size = parseInt(params?.size || '10');
      const sort = params?.sort || 'createdAt';
      const direction = params?.direction || 'desc';
      
      // 生成模拟数据
      const mockTimelines = Array.from({ length: 20 }, (_, i) => ({
        id: `${i + 1}`,
        title: `模拟时间线 ${i + 1}`,
        description: `这是一个模拟的时间线描述 ${i + 1}`,
        status: ['COMPLETED', 'PROCESSING', 'FAILED', 'DRAFT'][Math.floor(Math.random() * 4)],
        eventCount: Math.floor(Math.random() * 20) + 1,
        relationCount: Math.floor(Math.random() * 10),
        timeSpan: `${Math.floor(Math.random() * 30) + 1}天`,
        createdAt: new Date(Date.now() - Math.random() * 10000000000).toISOString(),
        updatedAt: new Date(Date.now() - Math.random() * 1000000000).toISOString()
      }));
      
      // 排序
      const sortedTimelines = [...mockTimelines].sort((a, b) => {
        if (direction === 'asc') {
          return a[sort] > b[sort] ? 1 : -1;
        } else {
          return a[sort] < b[sort] ? 1 : -1;
        }
      });
      
      // 分页
      const start = page * size;
      const end = start + size;
      const paginatedTimelines = sortedTimelines.slice(start, end);
      
      return {
        content: paginatedTimelines,
        totalElements: mockTimelines.length,
        totalPages: Math.ceil(mockTimelines.length / size),
        size: size,
        number: page,
        numberOfElements: paginatedTimelines.length,
        first: page === 0,
        last: page === Math.ceil(mockTimelines.length / size) - 1,
        empty: paginatedTimelines.length === 0
      };
    }
    // 时间线详情
    else if (url.match(/^\/timelines\/\d+\/details$/)) {
      const id = url.split('/')[2];
      
      return {
        id,
        title: `模拟时间线 ${id}`,
        description: `这是一个模拟的时间线详情描述 ${id}`,
        status: 'COMPLETED',
        eventCount: 10,
        relationCount: 5,
        timeSpan: '15天',
        createdAt: new Date(Date.now() - 5000000000).toISOString(),
        updatedAt: new Date(Date.now() - 1000000000).toISOString(),
        nodes: Array.from({ length: 10 }, (_, i) => ({
          id: `node-${i}`,
          event: {
            id: `event-${i}`,
            title: `事件 ${i}`,
            description: `事件描述 ${i}`,
            time: new Date(Date.now() - (10 - i) * 86400000).toISOString()
          },
          nodeType: ['START', 'MIDDLE', 'END', 'IMPORTANT'][Math.floor(Math.random() * 4)]
        })),
        relations: Array.from({ length: 5 }, (_, i) => ({
          id: `relation-${i}`,
          sourceId: `node-${i}`,
          targetId: `node-${i + 1}`,
          type: ['CAUSE', 'EFFECT', 'RELATED'][Math.floor(Math.random() * 3)],
          strength: Math.random().toFixed(2)
        }))
      };
    }
    // 组合搜索
    else if (url === '/timelines/search/combined') {
      const page = parseInt(params?.page || '0');
      const size = parseInt(params?.size || '10');
      const keyword = params?.keyword;
      const statuses = params?.statuses;
      
      // 生成模拟数据
      let mockTimelines = Array.from({ length: 15 }, (_, i) => ({
        id: `${i + 100}`,
        title: `搜索结果 ${i + 1}`,
        description: `这是一个与"${keyword || '默认'}"相关的时间线`,
        status: ['COMPLETED', 'PROCESSING', 'FAILED', 'DRAFT'][Math.floor(Math.random() * 4)],
        eventCount: Math.floor(Math.random() * 20) + 1,
        relationCount: Math.floor(Math.random() * 10),
        timeSpan: `${Math.floor(Math.random() * 30) + 1}天`,
        createdAt: new Date(Date.now() - Math.random() * 10000000000).toISOString(),
        updatedAt: new Date(Date.now() - Math.random() * 1000000000).toISOString()
      }));
      
      // 过滤
      if (keyword) {
        mockTimelines = mockTimelines.filter(t => 
          t.title.includes(keyword) || t.description.includes(keyword)
        );
      }
      
      if (statuses) {
        const statusArray = statuses.split(',');
        mockTimelines = mockTimelines.filter(t => statusArray.includes(t.status));
      }
      
      // 分页
      const start = page * size;
      const end = start + size;
      const paginatedTimelines = mockTimelines.slice(start, end);
      
      return {
        content: paginatedTimelines,
        totalElements: mockTimelines.length,
        totalPages: Math.ceil(mockTimelines.length / size),
        size: size,
        number: page,
        numberOfElements: paginatedTimelines.length,
        first: page === 0,
        last: page === Math.ceil(mockTimelines.length / size) - 1,
        empty: paginatedTimelines.length === 0
      };
    }
    
    // 默认返回空数据
    return { message: '模拟数据不可用' };
  };
  
  // 根据配置应用优化策略
  let optimizedGet = baseGet;
  
  // 应用缓存
  if (mergedConfig.useCache) {
    optimizedGet = createCachedRequest(optimizedGet, {
      ttl: mergedConfig.cacheTTL!,
      backgroundRefresh: mergedConfig.backgroundRefresh
    });
  }
  
  // 应用防抖
  if (mergedConfig.useDebounce) {
    optimizedGet = createDebouncedRequest(optimizedGet, mergedConfig.debounceDelay);
  }
  
  return optimizedGet;
}

/**
 * 创建优化的POST请求
 * @param url 请求URL
 * @param config 请求配置
 * @returns POST请求函数
 */
export function createOptimizedPost<T>(url: string, config: RequestConfig = {}) {
  // 合并配置
  const mergedConfig = { ...defaultConfig, ...config };
  
  // 基础POST请求函数
  const basePost = async (data?: any): Promise<T> => {
    try {
      // 直接调用真实API，不再使用模拟数据
      return await service.post(url, data, { timeout: mergedConfig.timeout });
    } catch (error) {
      // 如果配置了重试，则进行重试
      if (mergedConfig.retries && mergedConfig.retries > 0) {
        return retryRequest(() => service.post(url, data, { timeout: mergedConfig.timeout }), mergedConfig.retries, mergedConfig.retryDelay!);
      }
      
      // 如果请求失败，记录错误并抛出
      console.error(`API请求失败: ${url}`, error);
      throw error;
    }
  };
  
  // 获取模拟POST数据
  const getMockPostData = async (url: string, data?: any): Promise<any> => {
    // 模拟延迟
    await new Promise(resolve => setTimeout(resolve, 300));
    
    // 创建时间线
    if (url === '/timelines') {
      return {
        id: `${Date.now()}`,
        ...data,
        status: 'PROCESSING',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
    }
    // 更新时间线
    else if (url.match(/^\/timelines\/\d+$/)) {
      const id = url.split('/')[2];
      return {
        id,
        ...data,
        updatedAt: new Date().toISOString()
      };
    }
    // 删除时间线
    else if (url.match(/^\/timelines\/\d+$/)) {
      return { success: true };
    }
    
    // 默认返回空数据
    return { message: '模拟数据不可用' };
  };
  
  // 根据配置应用优化策略
  let optimizedPost = basePost;
  
  // POST请求通常不使用缓存，但如果特别指定了，也可以使用
  if (mergedConfig.useCache) {
    optimizedPost = createCachedRequest(optimizedPost, {
      ttl: mergedConfig.cacheTTL!,
      backgroundRefresh: mergedConfig.backgroundRefresh
    });
  }
  
  // 应用防抖
  if (mergedConfig.useDebounce) {
    optimizedPost = createDebouncedRequest(optimizedPost, mergedConfig.debounceDelay);
  }
  
  return optimizedPost;
}

/**
 * 创建批处理请求
 * @param url 请求URL
 * @param config 请求配置
 * @returns 批处理请求函数
 */
export function createBatchRequest<T, P>(url: string, config: RequestConfig = {}) {
  // 合并配置
  const mergedConfig = { ...defaultConfig, ...config, useBatch: true };
  
  // 批处理请求函数
  const batchRequest = async (paramsArray: P[]): Promise<T[]> => {
    try {
      return await service.post(url, { batch: paramsArray }, { timeout: mergedConfig.timeout });
    } catch (error) {
      // 如果配置了重试，则进行重试
      if (mergedConfig.retries && mergedConfig.retries > 0) {
        return retryRequest(() => service.post(url, { batch: paramsArray }, { timeout: mergedConfig.timeout }), mergedConfig.retries, mergedConfig.retryDelay!);
      }
      throw error;
    }
  };
  
  // 创建批处理请求
  return createBatchedRequest(batchRequest, mergedConfig.batchDelay);
}

/**
 * 重试请求
 * @param requestFn 请求函数
 * @param retries 重试次数
 * @param delay 重试延迟（毫秒）
 * @returns 请求结果
 */
async function retryRequest<T>(requestFn: () => Promise<T>, retries: number, delay: number): Promise<T> {
  let lastError: any;
  
  for (let i = 0; i < retries; i++) {
    try {
      return await requestFn();
    } catch (error) {
      lastError = error;
      
      // 等待一段时间后重试
      await new Promise(resolve => setTimeout(resolve, delay));
      
      // 指数退避策略
      delay *= 2;
    }
  }
  
  throw lastError;
}

/**
 * 预加载资源
 * @param urls 要预加载的URL数组
 */
export function preloadResources(urls: string[]): void {
  urls.forEach(url => {
    // 使用HEAD请求预加载资源
    service.head(url).catch(() => {
      // 忽略错误
    });
  });
}