import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import axios from 'axios';
import { createApiErrorHandler } from '@/api/errorHandler';
import { ElMessage } from 'element-plus';
import { useAppStore } from '@/store';
import { handleError } from '@/services/errorHandler';

// 模拟依赖
vi.mock('axios');
vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
    success: vi.fn()
  }
}));
vi.mock('@/services/errorHandler', () => ({
  handleError: vi.fn(),
  ErrorType: {
    API: 'api'
  },
  ErrorSeverity: {
    ERROR: 'error',
    WARNING: 'warning',
    INFO: 'info'
  }
}));
vi.mock('@/store', () => ({
  useAppStore: vi.fn(() => ({
    setGlobalError: vi.fn(),
  }))
}));

describe('API错误处理器', () => {
  let axiosInstance;
  let requestInterceptor;
  let responseInterceptor;
  let responseErrorInterceptor;
  
  beforeEach(() => {
    // 重置所有模拟
    vi.clearAllMocks();
    
    // 模拟axios实例
    axiosInstance = {
      interceptors: {
        request: {
          use: vi.fn((successFn, errorFn) => {
            requestInterceptor = { successFn, errorFn };
            return 1;
          })
        },
        response: {
          use: vi.fn((successFn, errorFn) => {
            responseInterceptor = successFn;
            responseErrorInterceptor = errorFn;
            return 2;
          })
        }
      }
    };
    
    // 应用错误处理器
    createApiErrorHandler()(axiosInstance);
    
    // 模拟navigator.onLine
    Object.defineProperty(navigator, 'onLine', {
      configurable: true,
      value: true
    });
  });
  
  afterEach(() => {
    vi.restoreAllMocks();
  });
  
  it('应该正确设置请求和响应拦截器', () => {
    expect(axiosInstance.interceptors.request.use).toHaveBeenCalled();
    expect(axiosInstance.interceptors.response.use).toHaveBeenCalled();
    expect(requestInterceptor).toBeDefined();
    expect(responseInterceptor).toBeDefined();
    expect(responseErrorInterceptor).toBeDefined();
  });
  
  it('请求拦截器应该在离线状态下将请求添加到队列', () => {
    // 模拟离线状态
    Object.defineProperty(navigator, 'onLine', {
      configurable: true,
      value: false
    });
    
    const config = { url: '/api/test', method: 'GET' };
    const promise = requestInterceptor.successFn(config);
    
    // 验证返回的是Promise
    expect(promise).toBeInstanceOf(Promise);
    
    // 验证显示了离线提示
    expect(ElMessage.warning).toHaveBeenCalledWith(
      expect.objectContaining({
        message: expect.stringContaining('离线状态')
      })
    );
  });
  
  it('响应拦截器应该处理业务逻辑错误', () => {
    const response = {
      data: {
        code: 400,
        message: '业务逻辑错误'
      },
      config: {}
    };
    
    // 调用响应拦截器
    expect(() => responseInterceptor(response)).rejects.toThrow('业务逻辑错误');
    
    // 验证错误被处理
    expect(handleError).toHaveBeenCalledWith(
      expect.objectContaining({
        message: '业务逻辑错误'
      }),
      expect.anything(),
      expect.anything(),
      expect.anything()
    );
  });
  
  it('响应拦截器应该正确处理正常响应', () => {
    const response = {
      data: {
        code: 0,
        data: { id: 1, name: '测试' }
      }
    };
    
    // 调用响应拦截器
    const result = responseInterceptor(response);
    
    // 验证正常响应被直接返回
    expect(result).toBe(response);
  });
  
  it('响应错误拦截器应该处理HTTP错误', async () => {
    const error = {
      config: {},
      response: {
        status: 404,
        data: {
          message: '资源不存在'
        }
      },
      isAxiosError: true
    };
    
    // 调用响应错误拦截器
    try {
      await responseErrorInterceptor(error);
      fail('应该抛出错误');
    } catch (e) {
      // 预期会抛出错误
    }
    
    // 验证错误被处理
    expect(handleError).toHaveBeenCalled();
    expect(ElMessage.error).toHaveBeenCalledWith(
      expect.objectContaining({
        message: expect.stringContaining('资源不存在')
      })
    );
  });
  
  it('响应错误拦截器应该处理网络错误', async () => {
    const error = {
      config: {},
      request: {},
      isAxiosError: true,
      message: '网络错误'
    };
    
    // 调用响应错误拦截器
    try {
      await responseErrorInterceptor(error);
      fail('应该抛出错误');
    } catch (e) {
      // 预期会抛出错误
    }
    
    // 验证错误被处理
    expect(handleError).toHaveBeenCalled();
    expect(ElMessage.error).toHaveBeenCalledWith(
      expect.objectContaining({
        message: expect.stringContaining('无法连接到服务器')
      })
    );
  });
  
  it('响应错误拦截器应该处理请求超时', async () => {
    const error = {
      config: {},
      request: {},
      isAxiosError: true,
      message: '网络错误',
      code: 'ECONNABORTED'
    };
    
    // 调用响应错误拦截器
    try {
      await responseErrorInterceptor(error);
      fail('应该抛出错误');
    } catch (e) {
      // 预期会抛出错误
    }
    
    // 验证错误被处理
    expect(handleError).toHaveBeenCalled();
    expect(ElMessage.error).toHaveBeenCalledWith(
      expect.objectContaining({
        message: expect.stringContaining('请求超时')
      })
    );
  });
  
  it('响应错误拦截器应该尝试重试可重试的错误', async () => {
    const config = { url: '/api/test', method: 'GET' };
    const error = {
      config,
      response: {
        status: 500,
        data: {
          message: '服务器错误'
        }
      },
      isAxiosError: true
    };
    
    // 模拟axios实例方法
    axiosInstance.request = vi.fn().mockResolvedValue({ data: { success: true } });
    
    // 调用响应错误拦截器
    try {
      await responseErrorInterceptor(error);
      fail('应该抛出错误');
    } catch (e) {
      // 预期会抛出错误
    }
    
    // 验证错误被处理
    expect(handleError).toHaveBeenCalled();
  });
  
  it('应该在网络恢复时处理离线请求队列', () => {
    // 模拟离线状态
    Object.defineProperty(navigator, 'onLine', {
      configurable: true,
      value: false
    });
    
    // 添加一些请求到队列
    const config1 = { url: '/api/test1', method: 'GET' };
    const config2 = { url: '/api/test2', method: 'POST' };
    
    requestInterceptor.successFn(config1);
    requestInterceptor.successFn(config2);
    
    // 模拟网络恢复
    const onlineEvent = new Event('online');
    Object.defineProperty(navigator, 'onLine', {
      configurable: true,
      value: true
    });
    window.dispatchEvent(onlineEvent);
    
    // 验证显示了网络恢复提示
    expect(ElMessage.success).toHaveBeenCalledWith(
      expect.objectContaining({
        message: expect.stringContaining('网络已恢复')
      })
    );
  });
  
  it('应该根据错误码正确映射错误消息和严重程度', async () => {
    // 测试不同的错误码
    const errorCodes = [400, 401, 403, 404, 500, 502, 503];
    
    for (const code of errorCodes) {
      vi.clearAllMocks();
      
      const error = {
        config: {},
        response: {
          status: code,
          data: {}
        },
        isAxiosError: true
      };
      
      // 调用响应错误拦截器
      try {
        await responseErrorInterceptor(error);
        fail('应该抛出错误');
      } catch (e) {
        // 预期会抛出错误
      }
      
      // 验证错误被处理
      expect(handleError).toHaveBeenCalled();
      expect(ElMessage.error).toHaveBeenCalled();
    }
  });
  
  it('应该在配置了updateGlobalError时更新全局错误状态', async () => {
    // 创建带有updateGlobalError配置的错误处理器
    createApiErrorHandler({ updateGlobalError: true })(axiosInstance);
    
    const error = {
      config: {},
      response: {
        status: 500,
        data: {
          message: '服务器错误'
        }
      },
      isAxiosError: true
    };
    
    // 调用响应错误拦截器
    try {
      await responseErrorInterceptor(error);
      fail('应该抛出错误');
    } catch (e) {
      // 预期会抛出错误
    }
    
    // 验证全局错误状态被更新
    const appStore = useAppStore();
    expect(appStore.setGlobalError).toHaveBeenCalledWith('服务器错误');
  });
});