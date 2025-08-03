import { test, expect } from '@playwright/test';

// 错误处理和恢复流程端到端测试
test.describe('错误处理和恢复流程', () => {
  // 在每个测试前先登录
  test.beforeEach(async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');
    
    // 填写登录表单
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'password');
    
    // 点击登录按钮
    await page.click('button:has-text("登录")');
    
    // 等待登录成功并跳转
    await page.waitForURL('/dashboard');
  });
  
  test('API错误处理和重试', async ({ page }) => {
    // 访问API错误演示页面
    await page.goto('/error-handling/api-error-demo');
    
    // 等待页面加载完成
    await page.waitForSelector('.api-error-demo');
    
    // 模拟API错误
    await page.evaluate(() => {
      // 覆盖fetch方法，使其返回错误
      window.originalFetch = window.fetch;
      window.fetch = (url, options) => {
        if (url.includes('/api/test')) {
          return Promise.reject(new Error('模拟API错误'));
        }
        return window.originalFetch(url, options);
      };
    });
    
    // 点击触发API请求按钮
    await page.click('button:has-text("发送请求")');
    
    // 等待错误反馈组件显示
    await page.waitForSelector('.api-error-feedback');
    
    // 验证错误消息
    await expect(page.locator('.api-error-feedback')).toContainText('模拟API错误');
    
    // 恢复fetch方法
    await page.evaluate(() => {
      window.fetch = window.originalFetch;
    });
    
    // 点击重试按钮
    await page.click('.api-error-feedback button:has-text("重试")');
    
    // 等待请求成功
    await page.waitForSelector('.success-message');
    
    // 验证成功消息
    await expect(page.locator('.success-message')).toBeVisible();
  });
  
  test('全局错误处理', async ({ page }) => {
    // 访问错误演示页面
    await page.goto('/error-handling');
    
    // 等待页面加载完成
    await page.waitForSelector('.error-handling-demo');
    
    // 点击触发全局错误按钮
    await page.click('button:has-text("触发全局错误")');
    
    // 等待全局错误显示
    await page.waitForSelector('.global-error-display');
    
    // 验证错误消息
    await expect(page.locator('.global-error-display')).toContainText('全局错误');
    
    // 点击关闭按钮
    await page.click('.global-error-display .close-button');
    
    // 验证错误消息已关闭
    await expect(page.locator('.global-error-display')).not.toBeVisible();
  });
  
  test('组件错误边界', async ({ page }) => {
    // 访问错误边界演示页面
    await page.goto('/error-handling/error-boundary-demo');
    
    // 等待页面加载完成
    await page.waitForSelector('.error-boundary-demo');
    
    // 点击触发组件错误按钮
    await page.click('button:has-text("触发组件错误")');
    
    // 等待错误状态组件显示
    await page.waitForSelector('.error-state');
    
    // 验证错误消息
    await expect(page.locator('.error-state')).toContainText('组件渲染错误');
    
    // 点击重试按钮
    await page.click('.error-state button:has-text("重试")');
    
    // 验证组件已恢复
    await expect(page.locator('.error-component')).toBeVisible();
  });
  
  test('表单验证错误处理', async ({ page }) => {
    // 访问表单演示页面
    await page.goto('/form-demo');
    
    // 等待页面加载完成
    await page.waitForSelector('.form-demo');
    
    // 不填写必填字段，直接提交表单
    await page.click('button:has-text("提交")');
    
    // 验证表单验证错误
    await expect(page.locator('.el-form-item__error')).toBeVisible();
    
    // 填写必填字段
    await page.fill('input[required]', '测试内容');
    
    // 再次提交表单
    await page.click('button:has-text("提交")');
    
    // 验证表单提交成功
    await page.waitForSelector('.el-message--success');
    await expect(page.locator('.el-message--success')).toContainText('提交成功');
  });
  
  test('网络恢复处理', async ({ page }) => {
    // 访问API错误演示页面
    await page.goto('/error-handling/api-error-demo');
    
    // 等待页面加载完成
    await page.waitForSelector('.api-error-demo');
    
    // 模拟网络断开
    await page.evaluate(() => {
      // 覆盖navigator.onLine属性
      Object.defineProperty(navigator, 'onLine', {
        configurable: true,
        get: () => false
      });
      
      // 触发offline事件
      window.dispatchEvent(new Event('offline'));
    });
    
    // 点击触发API请求按钮
    await page.click('button:has-text("发送请求")');
    
    // 等待离线提示显示
    await page.waitForSelector('.offline-notice');
    
    // 验证离线提示
    await expect(page.locator('.offline-notice')).toContainText('离线');
    
    // 模拟网络恢复
    await page.evaluate(() => {
      // 恢复navigator.onLine属性
      Object.defineProperty(navigator, 'onLine', {
        configurable: true,
        get: () => true
      });
      
      // 触发online事件
      window.dispatchEvent(new Event('online'));
    });
    
    // 等待网络恢复提示
    await page.waitForSelector('.el-message--success');
    
    // 验证网络恢复提示
    await expect(page.locator('.el-message--success')).toContainText('网络已恢复');
    
    // 验证离线提示已关闭
    await expect(page.locator('.offline-notice')).not.toBeVisible();
  });
  
  test('错误报告功能', async ({ page }) => {
    // 访问错误报告演示页面
    await page.goto('/error-handling/error-report-demo');
    
    // 等待页面加载完成
    await page.waitForSelector('.error-report-demo');
    
    // 点击触发错误按钮
    await page.click('button:has-text("触发错误")');
    
    // 等待错误报告对话框显示
    await page.waitForSelector('.error-report-dialog');
    
    // 填写错误报告表单
    await page.selectOption('select[placeholder="请选择错误类型"]', 'functional');
    await page.fill('textarea[placeholder*="问题描述"]', '这是一个测试错误报告');
    
    // 点击提交按钮
    await page.click('.error-report-dialog button:has-text("提交报告")');
    
    // 等待提交成功消息
    await page.waitForSelector('.el-message--success');
    
    // 验证成功消息
    await expect(page.locator('.el-message--success')).toContainText('错误报告已提交');
  });
});