import { defineConfig, devices } from '@playwright/test';

/**
 * 端到端测试配置
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  testDir: './src/__tests__/e2e',
  /* 每个测试的最大超时时间 */
  timeout: 30 * 1000,
  /* 测试运行器的超时时间 */
  expect: {
    /**
     * 断言的最大超时时间
     * @see https://playwright.dev/docs/api/class-locatorassertions
     */
    timeout: 5000
  },
  /* 测试失败时自动截图 */
  use: {
    /* 每个测试的跟踪视图 */
    trace: 'on-first-retry',
    /* 基础URL，所有相对URL都会基于此URL */
    baseURL: 'http://localhost:5173',
    /* 自动截图 */
    screenshot: 'only-on-failure',
    /* 收集跟踪信息 */
    video: 'on-first-retry',
  },

  /* 配置项目 */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
    /* 测试移动浏览器 */
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 12'] },
    },
  ],

  /* 本地开发服务器配置 */
  webServer: {
    command: 'npm run dev',
    port: 5173,
    reuseExistingServer: !process.env.CI,
  },
});