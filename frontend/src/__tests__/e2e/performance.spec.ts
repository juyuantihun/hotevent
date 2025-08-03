import { test, expect } from '@playwright/test';

// 性能和兼容性测试
test.describe('性能和兼容性测试', () => {
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
  
  test('首次加载性能', async ({ page }) => {
    // 启用性能测量
    await page.evaluate(() => {
      window.performance.mark('navigation-start');
    });
    
    // 访问首页
    await page.goto('/');
    
    // 等待页面完全加载
    await page.waitForLoadState('networkidle');
    
    // 标记加载完成
    await page.evaluate(() => {
      window.performance.mark('navigation-end');
      window.performance.measure('navigation', 'navigation-start', 'navigation-end');
    });
    
    // 获取性能指标
    const navigationTiming = await page.evaluate(() => {
      const navigationEntry = window.performance.getEntriesByType('navigation')[0];
      const paintEntries = window.performance.getEntriesByType('paint');
      const firstPaint = paintEntries.find(entry => entry.name === 'first-paint');
      const firstContentfulPaint = paintEntries.find(entry => entry.name === 'first-contentful-paint');
      
      return {
        navigationTime: navigationEntry.duration,
        firstPaint: firstPaint ? firstPaint.startTime : null,
        firstContentfulPaint: firstContentfulPaint ? firstContentfulPaint.startTime : null,
        domContentLoaded: navigationEntry.domContentLoadedEventEnd - navigationEntry.startTime,
        loadEvent: navigationEntry.loadEventEnd - navigationEntry.startTime
      };
    });
    
    // 验证性能指标
    expect(navigationTiming.navigationTime).toBeLessThan(5000); // 导航时间小于5秒
    expect(navigationTiming.firstPaint).toBeLessThan(2000); // 首次绘制时间小于2秒
    expect(navigationTiming.firstContentfulPaint).toBeLessThan(2500); // 首次内容绘制时间小于2.5秒
    expect(navigationTiming.domContentLoaded).toBeLessThan(3000); // DOM内容加载时间小于3秒
    expect(navigationTiming.loadEvent).toBeLessThan(5000); // 加载事件时间小于5秒
  });
  
  test('长列表渲染性能', async ({ page }) => {
    // 访问时间线列表页面
    await page.goto('/timeline');
    
    // 等待页面加载完成
    await page.waitForSelector('.timeline-list');
    
    // 启用性能测量
    await page.evaluate(() => {
      window.performance.mark('scroll-start');
    });
    
    // 滚动页面
    await page.evaluate(() => {
      window.scrollTo(0, document.body.scrollHeight);
    });
    
    // 等待滚动完成
    await page.waitForTimeout(500);
    
    // 标记滚动完成
    await page.evaluate(() => {
      window.performance.mark('scroll-end');
      window.performance.measure('scroll', 'scroll-start', 'scroll-end');
    });
    
    // 获取滚动性能指标
    const scrollTiming = await page.evaluate(() => {
      const scrollMeasure = window.performance.getEntriesByName('scroll')[0];
      return scrollMeasure.duration;
    });
    
    // 验证滚动性能
    expect(scrollTiming).toBeLessThan(500); // 滚动时间小于500毫秒
    
    // 验证滚动后页面响应
    await page.click('button:has-text("创建时间线")');
    await expect(page.locator('.create-timeline-dialog')).toBeVisible();
  });
  
  test('表单交互性能', async ({ page }) => {
    // 访问创建时间线页面
    await page.goto('/timeline');
    
    // 点击创建按钮
    await page.click('button:has-text("创建时间线")');
    
    // 等待创建对话框显示
    await page.waitForSelector('.create-timeline-dialog');
    
    // 启用性能测量
    await page.evaluate(() => {
      window.performance.mark('form-interaction-start');
    });
    
    // 快速填写表单
    await page.fill('input[placeholder="请输入时间线标题"]', '性能测试时间线');
    await page.fill('textarea[placeholder="请输入时间线描述"]', '这是一个性能测试时间线');
    await page.click('.region-selector');
    await page.click('.el-tree-node:has-text("中国")');
    await page.click('.el-date-editor--daterange');
    await page.click('.el-date-table td.available:nth-child(1)');
    await page.click('.el-date-table td.available:nth-child(10)');
    
    // 标记表单交互完成
    await page.evaluate(() => {
      window.performance.mark('form-interaction-end');
      window.performance.measure('form-interaction', 'form-interaction-start', 'form-interaction-end');
    });
    
    // 获取表单交互性能指标
    const formTiming = await page.evaluate(() => {
      const formMeasure = window.performance.getEntriesByName('form-interaction')[0];
      return formMeasure.duration;
    });
    
    // 验证表单交互性能
    expect(formTiming).toBeLessThan(3000); // 表单交互时间小于3秒
    
    // 点击确认按钮
    await page.click('.create-timeline-dialog button:has-text("确认")');
    
    // 启用性能测量
    await page.evaluate(() => {
      window.performance.mark('form-submit-start');
    });
    
    // 等待提交完成
    await page.waitForSelector('.el-message--success');
    
    // 标记表单提交完成
    await page.evaluate(() => {
      window.performance.mark('form-submit-end');
      window.performance.measure('form-submit', 'form-submit-start', 'form-submit-end');
    });
    
    // 获取表单提交性能指标
    const submitTiming = await page.evaluate(() => {
      const submitMeasure = window.performance.getEntriesByName('form-submit')[0];
      return submitMeasure.duration;
    });
    
    // 验证表单提交性能
    expect(submitTiming).toBeLessThan(2000); // 表单提交时间小于2秒
  });
  
  test('页面切换性能', async ({ page }) => {
    // 访问首页
    await page.goto('/dashboard');
    
    // 等待页面加载完成
    await page.waitForSelector('.dashboard');
    
    // 启用性能测量
    await page.evaluate(() => {
      window.performance.mark('navigation-start');
    });
    
    // 点击导航到时间线页面
    await page.click('a:has-text("时间线")');
    
    // 等待页面加载完成
    await page.waitForSelector('.timeline-list');
    
    // 标记导航完成
    await page.evaluate(() => {
      window.performance.mark('navigation-end');
      window.performance.measure('navigation', 'navigation-start', 'navigation-end');
    });
    
    // 获取导航性能指标
    const navigationTiming = await page.evaluate(() => {
      const navigationMeasure = window.performance.getEntriesByName('navigation')[0];
      return navigationMeasure.duration;
    });
    
    // 验证导航性能
    expect(navigationTiming).toBeLessThan(1000); // 页面切换时间小于1秒
    
    // 验证页面过渡效果
    await expect(page.locator('.page-transition-enter-active')).toBeVisible();
  });
  
  test('响应式布局兼容性', async ({ page }) => {
    // 测试不同屏幕尺寸
    const screenSizes = [
      { width: 1920, height: 1080 }, // 桌面
      { width: 1366, height: 768 },  // 笔记本
      { width: 768, height: 1024 },  // 平板
      { width: 375, height: 667 }    // 手机
    ];
    
    for (const size of screenSizes) {
      // 设置视口大小
      await page.setViewportSize(size);
      
      // 访问首页
      await page.goto('/dashboard');
      
      // 等待页面加载完成
      await page.waitForSelector('.dashboard');
      
      // 验证关键元素可见性
      await expect(page.locator('.app-header')).toBeVisible();
      await expect(page.locator('.app-content')).toBeVisible();
      
      // 验证布局适应性
      if (size.width < 768) {
        // 移动视图
        await expect(page.locator('.mobile-menu-button')).toBeVisible();
        await expect(page.locator('.sidebar')).not.toBeVisible();
        
        // 点击菜单按钮
        await page.click('.mobile-menu-button');
        
        // 验证侧边栏显示
        await expect(page.locator('.sidebar')).toBeVisible();
      } else {
        // 桌面视图
        await expect(page.locator('.sidebar')).toBeVisible();
        await expect(page.locator('.mobile-menu-button')).not.toBeVisible();
      }
    }
  });
  
  test('内存使用监控', async ({ page }) => {
    // 访问首页
    await page.goto('/dashboard');
    
    // 等待页面加载完成
    await page.waitForSelector('.dashboard');
    
    // 获取初始内存使用情况
    const initialMemory = await page.evaluate(() => {
      return performance.memory ? performance.memory.usedJSHeapSize : null;
    });
    
    // 如果浏览器不支持内存API，跳过测试
    if (initialMemory === null) {
      test.skip();
      return;
    }
    
    // 执行一系列操作
    for (let i = 0; i < 5; i++) {
      // 导航到不同页面
      await page.goto('/timeline');
      await page.waitForSelector('.timeline-list');
      
      await page.goto('/dashboard');
      await page.waitForSelector('.dashboard');
    }
    
    // 获取最终内存使用情况
    const finalMemory = await page.evaluate(() => {
      return performance.memory ? performance.memory.usedJSHeapSize : null;
    });
    
    // 计算内存增长
    const memoryGrowth = finalMemory - initialMemory;
    const memoryGrowthMB = memoryGrowth / (1024 * 1024);
    
    // 验证内存增长在合理范围内
    expect(memoryGrowthMB).toBeLessThan(50); // 内存增长小于50MB
  });
});