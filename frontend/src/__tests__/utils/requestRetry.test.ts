import { describe, it, expect, vi, beforeEach } from 'vitest';
import { withRetry as retryRequest, isErrorRetryable as isRetryableError, calculateRetryDelay as calculateBackoff } from '@/utils/requestRetry';

describe('请求重试工具', () => {
  beforeEach(() => {
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
    
    // 模拟定时器
    vi.useFakeTimers();
  });
  
  afterEach(() => {
    // 恢复真实定时器
    vi.useRealTimers();
  });
  
  describe('isRetryableError', () => {
    it('应该将网络错误识别为可重试', () => {
      const networkError = new Error('Network Error');
      expect(isRetryableError(networkError)).toBe(true);
    });
    
    it('应该将超时错误识别为可重试', () => {
      const timeoutError = new Error('timeout of 10000ms exceeded');
      expect(isRetryableError(timeoutError)).toBe(true);
    });
    
    it('应该将特定HTTP状态码识别为可重试', () => {
      const retryableStatusCodes = [408, 429, 500, 502, 503, 504];
      
      for (const code of retryableStatusCodes) {
        const error = {
          response: { status: code }
        };
        expect(isRetryableError(error)).toBe(true);
      }
    });
    
    it('应该将非重试HTTP状态码识别为不可重试', () => {
      const nonRetryableStatusCodes = [400, 401, 403, 404];
      
      for (const code of nonRetryableStatusCodes) {
        const error = {
          response: { status: code }
        };
        expect(isRetryableError(error)).toBe(false);
      }
    });
    
    it('应该将自定义可重试错误识别为可重试', () => {
      const customError = new Error('Custom Error');
      (customError as any).isRetryable = true;
      
      expect(isRetryableError(customError)).toBe(true);
    });
  });
  
  describe('calculateBackoff', () => {
    it('应该计算指数退避延迟', () => {
      // 基础延迟为1000ms
      expect(calculateBackoff(1, 1000)).toBe(1000); // 第1次重试
      expect(calculateBackoff(2, 1000)).toBe(2000); // 第2次重试
      expect(calculateBackoff(3, 1000)).toBe(4000); // 第3次重试
      expect(calculateBackoff(4, 1000)).toBe(8000); // 第4次重试
    });
    
    it('应该添加随机抖动', () => {
      // 模拟Math.random返回固定值
      const randomSpy = vi.spyOn(Math, 'random').mockReturnValue(0.5);
      
      // 基础延迟为1000ms，抖动因子为0.5
      expect(calculateBackoff(1, 1000, 0.5)).toBe(1250); // 1000 + 1000*0.5*0.5
      expect(calculateBackoff(2, 1000, 0.5)).toBe(2500); // 2000 + 2000*0.5*0.5
      
      // 恢复Math.random
      randomSpy.mockRestore();
    });
    
    it('应该尊重最大延迟限制', () => {
      // 基础延迟为1000ms，最大延迟为5000ms
      expect(calculateBackoff(1, 1000, 0, 5000)).toBe(1000);
      expect(calculateBackoff(2, 1000, 0, 5000)).toBe(2000);
      expect(calculateBackoff(3, 1000, 0, 5000)).toBe(4000);
      expect(calculateBackoff(4, 1000, 0, 5000)).toBe(5000); // 应该被限制在5000ms
      expect(calculateBackoff(5, 1000, 0, 5000)).toBe(5000); // 应该被限制在5000ms
    });
  });
  
  describe('retryRequest', () => {
    it('应该在成功时立即返回结果', async () => {
      const requestFn = vi.fn().mockResolvedValue('success');
      const onRetry = vi.fn();
      
      const result = await retryRequest(requestFn, {
        maxRetries: 3,
        onRetry
      });
      
      expect(result).toBe('success');
      expect(requestFn).toHaveBeenCalledTimes(1);
      expect(onRetry).not.toHaveBeenCalled();
    });
    
    it('应该在可重试错误时重试请求', async () => {
      // 前两次失败，第三次成功
      const requestFn = vi.fn()
        .mockRejectedValueOnce(new Error('Network Error'))
        .mockRejectedValueOnce(new Error('Network Error'))
        .mockResolvedValue('success');
      
      const onRetry = vi.fn();
      
      const resultPromise = retryRequest(requestFn, {
        maxRetries: 3,
        baseDelay: 1000,
        onRetry
      });
      
      // 第一次失败后
      expect(requestFn).toHaveBeenCalledTimes(1);
      expect(onRetry).toHaveBeenCalledTimes(1);
      expect(onRetry).toHaveBeenCalledWith(expect.any(Error), 1);
      
      // 前进1秒
      vi.advanceTimersByTime(1000);
      
      // 第二次失败后
      expect(requestFn).toHaveBeenCalledTimes(2);
      expect(onRetry).toHaveBeenCalledTimes(2);
      expect(onRetry).toHaveBeenCalledWith(expect.any(Error), 2);
      
      // 前进2秒
      vi.advanceTimersByTime(2000);
      
      // 第三次成功
      expect(requestFn).toHaveBeenCalledTimes(3);
      
      const result = await resultPromise;
      expect(result).toBe('success');
    });
    
    it('应该在达到最大重试次数后抛出错误', async () => {
      // 所有请求都失败
      const error = new Error('Network Error');
      const requestFn = vi.fn().mockRejectedValue(error);
      const onRetry = vi.fn();
      const onMaxRetries = vi.fn();
      
      const promise = retryRequest(requestFn, {
        maxRetries: 3,
        baseDelay: 1000,
        onRetry,
        onMaxRetries
      });
      
      // 第一次失败后
      expect(requestFn).toHaveBeenCalledTimes(1);
      expect(onRetry).toHaveBeenCalledTimes(1);
      
      // 前进1秒
      vi.advanceTimersByTime(1000);
      
      // 第二次失败后
      expect(requestFn).toHaveBeenCalledTimes(2);
      expect(onRetry).toHaveBeenCalledTimes(2);
      
      // 前进2秒
      vi.advanceTimersByTime(2000);
      
      // 第三次失败后
      expect(requestFn).toHaveBeenCalledTimes(3);
      expect(onRetry).toHaveBeenCalledTimes(3);
      
      // 前进4秒
      vi.advanceTimersByTime(4000);
      
      // 第四次失败后
      expect(requestFn).toHaveBeenCalledTimes(4);
      expect(onMaxRetries).toHaveBeenCalledTimes(1);
      expect(onMaxRetries).toHaveBeenCalledWith(error);
      
      // 验证最终抛出错误
      await expect(promise).rejects.toThrow('Network Error');
    });
    
    it('应该使用自定义重试条件', async () => {
      // 创建一个自定义错误
      const customError = new Error('Custom Error');
      
      // 自定义重试条件
      const retryCondition = (error) => error.message === 'Custom Error';
      
      // 前两次失败，第三次成功
      const requestFn = vi.fn()
        .mockRejectedValueOnce(customError)
        .mockRejectedValueOnce(customError)
        .mockResolvedValue('success');
      
      const onRetry = vi.fn();
      
      const resultPromise = retryRequest(requestFn, {
        maxRetries: 3,
        baseDelay: 1000,
        onRetry,
        retryCondition
      });
      
      // 第一次失败后
      expect(requestFn).toHaveBeenCalledTimes(1);
      expect(onRetry).toHaveBeenCalledTimes(1);
      
      // 前进1秒
      vi.advanceTimersByTime(1000);
      
      // 第二次失败后
      expect(requestFn).toHaveBeenCalledTimes(2);
      expect(onRetry).toHaveBeenCalledTimes(2);
      
      // 前进2秒
      vi.advanceTimersByTime(2000);
      
      // 第三次成功
      expect(requestFn).toHaveBeenCalledTimes(3);
      
      const result = await resultPromise;
      expect(result).toBe('success');
    });
    
    it('应该使用自定义延迟计算', async () => {
      // 自定义延迟计算
      const delayFn = vi.fn().mockReturnValue(500);
      
      // 前两次失败，第三次成功
      const requestFn = vi.fn()
        .mockRejectedValueOnce(new Error('Network Error'))
        .mockRejectedValueOnce(new Error('Network Error'))
        .mockResolvedValue('success');
      
      const resultPromise = retryRequest(requestFn, {
        maxRetries: 3,
        delayFn
      });
      
      // 第一次失败后
      expect(requestFn).toHaveBeenCalledTimes(1);
      expect(delayFn).toHaveBeenCalledTimes(1);
      expect(delayFn).toHaveBeenCalledWith(1);
      
      // 前进500毫秒
      vi.advanceTimersByTime(500);
      
      // 第二次失败后
      expect(requestFn).toHaveBeenCalledTimes(2);
      expect(delayFn).toHaveBeenCalledTimes(2);
      expect(delayFn).toHaveBeenCalledWith(2);
      
      // 前进500毫秒
      vi.advanceTimersByTime(500);
      
      // 第三次成功
      expect(requestFn).toHaveBeenCalledTimes(3);
      
      const result = await resultPromise;
      expect(result).toBe('success');
    });
  });
});