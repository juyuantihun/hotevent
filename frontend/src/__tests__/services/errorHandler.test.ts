import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  handleError,
  ErrorType,
  ErrorSeverity,
  configureErrorHandler,
  getErrorHistory,
  clearErrorHistory,
  createError,
  withErrorHandling,
  withAsyncErrorHandling
} from '@/services/errorHandler';

describe('错误处理服务', () => {
  beforeEach(() => {
    // 清除错误历史
    clearErrorHistory();
    
    // 模拟控制台方法
    vi.spyOn(console, 'error').mockImplementation(() => {});
    vi.spyOn(console, 'group').mockImplementation(() => {});
    vi.spyOn(console, 'groupEnd').mockImplementation(() => {});
    
    // 模拟fetch和navigator.sendBeacon
    global.fetch = vi.fn().mockResolvedValue({ ok: true });
    global.navigator.sendBeacon = vi.fn().mockReturnValue(true);
  });
  
  afterEach(() => {
    vi.restoreAllMocks();
  });
  
  it('handleError应该正确处理错误对象', () => {
    const error = new Error('测试错误');
    handleError(error, ErrorType.RUNTIME, ErrorSeverity.ERROR);
    
    // 验证错误被记录到历史
    const history = getErrorHistory();
    expect(history.length).toBe(1);
    expect(history[0].message).toBe('测试错误');
    expect(history[0].type).toBe(ErrorType.RUNTIME);
    expect(history[0].severity).toBe(ErrorSeverity.ERROR);
    
    // 验证控制台错误被记录
    expect(console.error).toHaveBeenCalled();
  });
  
  it('handleError应该正确处理字符串错误', () => {
    handleError('字符串错误', ErrorType.UNKNOWN, ErrorSeverity.WARNING);
    
    // 验证错误被记录到历史
    const history = getErrorHistory();
    expect(history.length).toBe(1);
    expect(history[0].message).toBe('字符串错误');
    expect(history[0].type).toBe(ErrorType.UNKNOWN);
    expect(history[0].severity).toBe(ErrorSeverity.WARNING);
  });
  
  it('handleError应该正确处理对象错误', () => {
    const errorObj = { message: '对象错误', code: 500 };
    handleError(errorObj, ErrorType.API, ErrorSeverity.ERROR);
    
    // 验证错误被记录到历史
    const history = getErrorHistory();
    expect(history.length).toBe(1);
    expect(history[0].message).toBe('对象错误');
    expect(history[0].type).toBe(ErrorType.API);
    expect(history[0].severity).toBe(ErrorSeverity.ERROR);
    expect(history[0].details).toEqual(errorObj);
  });
  
  it('handleError应该合并额外信息', () => {
    const error = new Error('测试错误');
    const additionalInfo = {
      message: '自定义错误消息',
      location: '测试位置',
      userAction: '测试操作'
    };
    
    handleError(error, ErrorType.VUE, ErrorSeverity.ERROR, additionalInfo);
    
    // 验证错误被记录到历史
    const history = getErrorHistory();
    expect(history.length).toBe(1);
    expect(history[0].message).toBe('自定义错误消息');
    expect(history[0].location).toBe('测试位置');
    expect(history[0].userAction).toBe('测试操作');
  });
  
  it('configureErrorHandler应该正确更新配置', () => {
    // 配置错误处理器
    const customHandler = vi.fn();
    configureErrorHandler({
      showNotification: false,
      logToConsole: false,
      reportToServer: false,
      affectGlobalState: false,
      customHandler
    });
    
    // 触发错误
    const error = new Error('配置测试错误');
    handleError(error);
    
    // 验证自定义处理函数被调用
    expect(customHandler).toHaveBeenCalled();
    expect(customHandler.mock.calls[0][0].message).toBe('配置测试错误');
    
    // 验证控制台错误没有被记录
    expect(console.error).not.toHaveBeenCalled();
  });
  
  it('clearErrorHistory应该清除错误历史', () => {
    // 添加一些错误
    handleError('错误1');
    handleError('错误2');
    handleError('错误3');
    
    // 验证错误被记录
    expect(getErrorHistory().length).toBe(3);
    
    // 清除错误历史
    clearErrorHistory();
    
    // 验证错误历史被清除
    expect(getErrorHistory().length).toBe(0);
  });
  
  it('createError应该创建错误对象', () => {
    const error = createError('测试错误');
    
    expect(error).toBeInstanceOf(Error);
    expect(error.message).toBe('测试错误');
  });
  
  it('withErrorHandling应该捕获并处理同步函数中的错误', () => {
    // 创建一个会抛出错误的函数
    const throwingFunction = () => {
      throw new Error('同步函数错误');
    };
    
    // 包装函数
    const wrappedFunction = withErrorHandling(throwingFunction, ErrorType.RUNTIME, ErrorSeverity.ERROR);
    
    // 调用包装函数
    const result = wrappedFunction();
    
    // 验证错误被处理
    expect(result).toBeUndefined();
    
    // 验证错误被记录到历史
    const history = getErrorHistory();
    expect(history.length).toBe(1);
    expect(history[0].message).toBe('同步函数错误');
    expect(history[0].type).toBe(ErrorType.RUNTIME);
  });
  
  it('withAsyncErrorHandling应该捕获并处理异步函数中的错误', async () => {
    // 创建一个会抛出错误的异步函数
    const throwingAsyncFunction = async () => {
      throw new Error('异步函数错误');
    };
    
    // 包装函数
    const wrappedFunction = withAsyncErrorHandling(throwingAsyncFunction, ErrorType.PROMISE, ErrorSeverity.ERROR);
    
    // 调用包装函数
    const result = await wrappedFunction();
    
    // 验证错误被处理
    expect(result).toBeUndefined();
    
    // 验证错误被记录到历史
    const history = getErrorHistory();
    expect(history.length).toBe(1);
    expect(history[0].message).toBe('异步函数错误');
    expect(history[0].type).toBe(ErrorType.PROMISE);
  });
  
  it('错误历史应该限制大小', () => {
    // 添加超过最大限制的错误
    for (let i = 0; i < 60; i++) {
      handleError(`错误${i}`);
    }
    
    // 验证错误历史被限制
    const history = getErrorHistory();
    expect(history.length).toBe(50); // 假设最大历史记录为50
    expect(history[0].message).toBe('错误59'); // 最新的错误应该在前面
  });
});