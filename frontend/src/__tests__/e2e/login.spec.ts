import { test, expect } from '@playwright/test';

// 登录流程端到端测试
test.describe('登录流程', () => {
  test('成功登录', async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');
    
    // 等待页面加载完成
    await page.waitForSelector('form');
    
    // 填写登录表单
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'password');
    
    // 点击登录按钮
    await page.click('button:has-text("登录")');
    
    // 等待登录成功并跳转
    await page.waitForURL('/dashboard');
    
    // 验证登录成功
    await expect(page.locator('.user-info')).toContainText('管理员');
    
    // 验证侧边栏加载
    await expect(page.locator('.sidebar-menu')).toBeVisible();
  });
  
  test('登录失败显示错误消息', async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');
    
    // 等待页面加载完成
    await page.waitForSelector('form');
    
    // 填写错误的登录信息
    await page.fill('input[placeholder="请输入用户名"]', 'wrong');
    await page.fill('input[placeholder="请输入密码"]', 'wrong');
    
    // 点击登录按钮
    await page.click('button:has-text("登录")');
    
    // 等待错误消息显示
    await page.waitForSelector('.el-message--error');
    
    // 验证错误消息
    await expect(page.locator('.el-message--error')).toContainText('用户名或密码错误');
    
    // 验证仍在登录页面
    await expect(page).toHaveURL('/login');
  });
  
  test('表单验证', async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');
    
    // 等待页面加载完成
    await page.waitForSelector('form');
    
    // 不填写任何信息，直接点击登录按钮
    await page.click('button:has-text("登录")');
    
    // 验证表单验证错误
    await expect(page.locator('.el-form-item__error')).toBeVisible();
    await expect(page.locator('.el-form-item__error').first()).toContainText('请输入用户名');
    
    // 只填写用户名
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.click('button:has-text("登录")');
    
    // 验证密码验证错误
    await expect(page.locator('.el-form-item__error')).toBeVisible();
    await expect(page.locator('.el-form-item__error').nth(1)).toContainText('请输入密码');
  });
  
  test('记住用户名功能', async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');
    
    // 等待页面加载完成
    await page.waitForSelector('form');
    
    // 填写登录表单并选中记住用户名
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'password');
    await page.check('input[type="checkbox"]');
    
    // 点击登录按钮
    await page.click('button:has-text("登录")');
    
    // 等待登录成功并跳转
    await page.waitForURL('/dashboard');
    
    // 登出
    await page.click('.logout-button');
    
    // 等待跳转回登录页
    await page.waitForURL('/login');
    
    // 验证用户名已自动填充
    await expect(page.locator('input[placeholder="请输入用户名"]')).toHaveValue('admin');
  });
  
  test('会话过期处理', async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');
    
    // 等待页面加载完成
    await page.waitForSelector('form');
    
    // 填写登录表单
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'password');
    
    // 点击登录按钮
    await page.click('button:has-text("登录")');
    
    // 等待登录成功并跳转
    await page.waitForURL('/dashboard');
    
    // 模拟会话过期
    await page.evaluate(() => {
      localStorage.removeItem('token');
    });
    
    // 尝试访问需要认证的页面
    await page.goto('/profile');
    
    // 验证被重定向到登录页
    await expect(page).toHaveURL(/\/login/);
  });
});