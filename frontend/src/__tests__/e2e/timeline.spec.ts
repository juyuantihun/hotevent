import { test, expect } from '@playwright/test';

// 时间线管理流程端到端测试
test.describe('时间线管理流程', () => {
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
  
  test('查看时间线列表', async ({ page }) => {
    // 访问时间线列表页面
    await page.goto('/timeline');
    
    // 等待页面加载完成
    await page.waitForSelector('.timeline-list');
    
    // 验证时间线列表加载
    await expect(page.locator('.timeline-card')).toHaveCount.above(0);
    
    // 验证分页组件存在
    await expect(page.locator('.el-pagination')).toBeVisible();
  });
  
  test('创建新时间线', async ({ page }) => {
    // 访问时间线列表页面
    await page.goto('/timeline');
    
    // 点击创建按钮
    await page.click('button:has-text("创建时间线")');
    
    // 等待创建对话框显示
    await page.waitForSelector('.create-timeline-dialog');
    
    // 填写表单
    await page.fill('input[placeholder="请输入时间线标题"]', '测试时间线');
    await page.fill('textarea[placeholder="请输入时间线描述"]', '这是一个测试时间线');
    
    // 选择地区
    await page.click('.region-selector');
    await page.click('.el-tree-node:has-text("中国")');
    
    // 选择日期范围
    await page.click('.el-date-editor--daterange');
    await page.click('.el-date-table td.available:nth-child(1)'); // 选择开始日期
    await page.click('.el-date-table td.available:nth-child(10)'); // 选择结束日期
    
    // 点击确认按钮
    await page.click('.create-timeline-dialog button:has-text("确认")');
    
    // 等待创建成功消息
    await page.waitForSelector('.el-message--success');
    
    // 验证成功消息
    await expect(page.locator('.el-message--success')).toContainText('创建成功');
    
    // 验证新时间线出现在列表中
    await expect(page.locator('.timeline-card:has-text("测试时间线")')).toBeVisible();
  });
  
  test('查看时间线详情', async ({ page }) => {
    // 访问时间线列表页面
    await page.goto('/timeline');
    
    // 等待页面加载完成
    await page.waitForSelector('.timeline-list');
    
    // 点击第一个时间线卡片
    await page.click('.timeline-card:nth-child(1)');
    
    // 等待详情对话框显示
    await page.waitForSelector('.timeline-detail-dialog');
    
    // 验证详情内容
    await expect(page.locator('.timeline-detail-dialog .timeline-title')).toBeVisible();
    await expect(page.locator('.timeline-detail-dialog .timeline-description')).toBeVisible();
    await expect(page.locator('.timeline-detail-dialog .timeline-events')).toBeVisible();
  });
  
  test('搜索时间线', async ({ page }) => {
    // 访问时间线列表页面
    await page.goto('/timeline');
    
    // 等待页面加载完成
    await page.waitForSelector('.timeline-list');
    
    // 记录初始时间线数量
    const initialCount = await page.locator('.timeline-card').count();
    
    // 在搜索框中输入关键词
    await page.fill('input[placeholder="搜索时间线"]', '测试');
    
    // 点击搜索按钮
    await page.click('button:has-text("搜索")');
    
    // 等待搜索结果加载
    await page.waitForResponse(response => response.url().includes('/api/timelines/search'));
    
    // 验证搜索结果
    const searchResultCount = await page.locator('.timeline-card').count();
    expect(searchResultCount).toBeLessThanOrEqual(initialCount);
    
    // 验证搜索结果包含关键词
    await expect(page.locator('.timeline-card')).toContainText('测试');
  });
  
  test('删除时间线', async ({ page }) => {
    // 访问时间线列表页面
    await page.goto('/timeline');
    
    // 等待页面加载完成
    await page.waitForSelector('.timeline-list');
    
    // 记录初始时间线数量
    const initialCount = await page.locator('.timeline-card').count();
    
    // 点击第一个时间线的删除按钮
    await page.click('.timeline-card:nth-child(1) .delete-button');
    
    // 等待确认对话框显示
    await page.waitForSelector('.el-message-box');
    
    // 点击确认按钮
    await page.click('.el-message-box__btns button:has-text("确定")');
    
    // 等待删除成功消息
    await page.waitForSelector('.el-message--success');
    
    // 验证成功消息
    await expect(page.locator('.el-message--success')).toContainText('删除成功');
    
    // 验证时间线数量减少
    await expect(page.locator('.timeline-card')).toHaveCount(initialCount - 1);
  });
  
  test('筛选时间线', async ({ page }) => {
    // 访问时间线列表页面
    await page.goto('/timeline');
    
    // 等待页面加载完成
    await page.waitForSelector('.timeline-list');
    
    // 记录初始时间线数量
    const initialCount = await page.locator('.timeline-card').count();
    
    // 点击高级筛选按钮
    await page.click('button:has-text("高级筛选")');
    
    // 等待筛选面板显示
    await page.waitForSelector('.filter-panel');
    
    // 选择状态筛选条件
    await page.click('.filter-panel .el-select');
    await page.click('.el-select-dropdown__item:has-text("已完成")');
    
    // 点击应用筛选按钮
    await page.click('.filter-panel button:has-text("应用")');
    
    // 等待筛选结果加载
    await page.waitForResponse(response => response.url().includes('/api/timelines'));
    
    // 验证筛选结果
    const filteredCount = await page.locator('.timeline-card').count();
    expect(filteredCount).toBeLessThanOrEqual(initialCount);
    
    // 验证筛选结果状态
    await expect(page.locator('.timeline-card .status-tag')).toContainText('已完成');
  });
});