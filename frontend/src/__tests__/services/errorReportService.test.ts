import { describe, it, expect, vi, beforeEach } from 'vitest';
import { errorReportService } from '@/services/errorReportService';
import { ErrorType, ErrorSeverity } from '@/services/errorHandler';

describe('错误报告服务', () => {
  beforeEach(() => {
    // 清除localStorage
    localStorage.clear();
    
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
    
    // 模拟fetch
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ success: true })
    });
    
    // 模拟navigator.sendBeacon
    global.navigator.sendBeacon = vi.fn().mockReturnValue(true);
  });
  
  it('reportError应该保存错误报告到本地存储', () => {
    const error = new Error('测试错误');
    
    errorReportService.reportError(error, ErrorType.RUNTIME, ErrorSeverity.ERROR, {
      message: '测试错误',
      details: { description: '测试描述' }
    });
    
    // 验证错误报告被保存到本地存储
    const reports = JSON.parse(localStorage.getItem('errorReports') || '[]');
    expect(reports.length).toBe(1);
    expect(reports[0].message).toBe('测试错误');
    expect(reports[0].type).toBe('functional');
  });
  
  it('reportError应该尝试将错误报告发送到服务器', () => {
    const error = new Error('测试错误');
    
    errorReportService.reportError(error, ErrorType.RUNTIME, ErrorSeverity.ERROR, { 
      message: '测试错误',
      details: { description: '测试描述' }
    });
    
    // 验证尝试发送到服务器
    expect(global.fetch).toHaveBeenCalledWith(
      '/api/error-report',
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({
          'Content-Type': 'application/json'
        }),
        body: expect.any(String)
      })
    );
  });
  
  it('reportError应该在发送失败时仍保存到本地存储', async () => {
    // 模拟fetch失败
    global.fetch = vi.fn().mockRejectedValue(new Error('发送失败'));
    
    const error = new Error('测试错误');
    
    await errorReportService.reportError(error, ErrorType.RUNTIME, ErrorSeverity.ERROR, {
      message: '测试错误',
      details: { description: '测试描述' }
    });
    
    // 验证错误报告被保存到本地存储
    const reports = errorReportService.getErrorCache();
    expect(reports.length).toBe(1);
    expect(reports[0].message).toBe('测试错误');
  });
  
  it('getErrorReports应该返回所有保存的错误报告', () => {
    // 保存一些错误报告
    const errors = [
      {
        message: '错误1',
        type: 'functional',
        severity: 3,
        timestamp: Date.now() - 1000
      },
      {
        message: '错误2',
        type: 'ui',
        severity: 2,
        timestamp: Date.now()
      }
    ];
    
    localStorage.setItem('errorReports', JSON.stringify(errors));
    
    // 获取错误报告
    const reports = errorReportService.getErrorCache();
    
    // 验证返回了所有报告
    expect(reports.length).toBe(2);
    expect(reports[0].message).toBe('错误1');
    expect(reports[1].message).toBe('错误2');
  });
  
  it('getErrorReports应该在没有报告时返回空数组', () => {
    // 确保没有报告
    localStorage.removeItem('errorReports');
    
    // 获取错误报告
    const reports = errorReportService.getErrorCache();
    
    // 验证返回了空数组
    expect(reports).toEqual([]);
  });
  
  it('clearErrorReports应该清除所有保存的错误报告', () => {
    // 保存一些错误报告
    const errors = [
      {
        message: '错误1',
        type: 'functional',
        severity: 3,
        timestamp: Date.now()
      }
    ];
    
    localStorage.setItem('errorReports', JSON.stringify(errors));
    
    // 清除错误报告
    errorReportService.clearErrorCache();
    
    // 验证报告被清除
    expect(errorReportService.getErrorCache().length).toBe(0);
  });
  
  it('reportError应该限制本地存储的报告数量', () => {
    // 创建超过限制的错误报告
    const maxReports = 100; // 根据defaultConfig.maxCachedErrors
    
    // 保存所有报告
    for (let i = 0; i < maxReports + 10; i++) {
      const error = new Error(`错误${i}`);
      errorReportService.reportError(error, ErrorType.RUNTIME, ErrorSeverity.ERROR, {
        message: `错误${i}`
      });
    }
    
    // 获取保存的报告
    const reports = errorReportService.getErrorCache();
    
    // 验证报告数量被限制
    expect(reports.length).toBe(maxReports);
  });
  
  it('reportError应该添加技术信息', () => {
    const error = new Error('测试错误');
    
    errorReportService.reportError(error, ErrorType.RUNTIME, ErrorSeverity.ERROR, {
      message: '测试错误',
      details: { description: '测试描述' }
    });
    
    // 获取保存的报告
    const reports = errorReportService.getErrorCache();
    const report = reports[0];
    
    // 验证添加了技术信息
    expect(report.techInfo).toBeDefined();
    expect(report.techInfo.browser).toBe(navigator.userAgent);
    expect(report.techInfo.url).toBe(window.location.href);
  });
});