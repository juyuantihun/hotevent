import { test as base } from '@playwright/test';
import type { Page } from '@playwright/test';

/**
 * 扩展测试固定装置，添加自定义方法
 */
export const test = base.extend({
  // 登录状态固定装置
  loggedInPage: async ({ page }, use) => {
    // 访问登录页面
    await page.goto('/login');
    
    // 填写登录表单
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'password');
    
    // 点击登录按钮
    await page.click('button:has-text("登录")');
    
    // 等待登录成功并跳转
    await page.waitForURL('/dashboard');
    
    // 使用已登录的页面
    await use(page);
  },
  
  // 性能测试辅助方法
  performancePage: async ({ page }, use) => {
    // 扩展页面对象，添加性能测量方法
    const performancePage = page as Page & {
      measurePerformance: (name: string, action: () => Promise<void>) => Promise<number>;
    };
    
    // 添加性能测量方法
    performancePage.measurePerformance = async (name, action) => {
      // 开始测量
      await performancePage.evaluate((measureName) => {
        window.performance.mark(`${measureName}-start`);
      }, name);
      
      // 执行操作
      await action();
      
      // 结束测量
      return await performancePage.evaluate((measureName) => {
        window.performance.mark(`${measureName}-end`);
        window.performance.measure(
          measureName,
          `${measureName}-start`,
          `${measureName}-end`
        );
        const measure = window.performance.getEntriesByName(measureName)[0];
        return measure.duration;
      }, name);
    };
    
    // 使用扩展的页面对象
    await use(performancePage);
  },
  
  // 错误处理测试辅助方法
  errorHandlingPage: async ({ page }, use) => {
    // 扩展页面对象，添加错误处理方法
    const errorHandlingPage = page as Page & {
      triggerError: (selector: string) => Promise<void>;
      waitForErrorMessage: () => Promise<string>;
    };
    
    // 添加触发错误方法
    errorHandlingPage.triggerError = async (selector) => {
      await errorHandlingPage.click(selector);
    };
    
    // 添加等待错误消息方法
    errorHandlingPage.waitForErrorMessage = async () => {
      await errorHandlingPage.waitForSelector('.el-message--error');
      return errorHandlingPage.locator('.el-message--error').textContent();
    };
    
    // 使用扩展的页面对象
    await use(errorHandlingPage);
  },
  
  // 响应式测试辅助方法
  responsivePage: async ({ page }, use) => {
    // 扩展页面对象，添加响应式测试方法
    const responsivePage = page as Page & {
      testResponsive: (sizes: Array<{ width: number; height: number }>, url: string) => Promise<void>;
    };
    
    // 添加响应式测试方法
    responsivePage.testResponsive = async (sizes, url) => {
      for (const size of sizes) {
        // 设置视口大小
        await responsivePage.setViewportSize(size);
        
        // 访问页面
        await responsivePage.goto(url);
        
        // 等待页面加载完成
        await responsivePage.waitForLoadState('networkidle');
        
        // 截图
        await responsivePage.screenshot({
          path: `./test-results/responsive-${url.replace(/\//g, '-')}-${size.width}x${size.height}.png`
        });
      }
    };
    
    // 使用扩展的页面对象
    await use(responsivePage);
  }
});

export { expect } from '@playwright/test';