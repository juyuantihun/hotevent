import { test, expect, Page } from '@playwright/test'

// 端到端测试配置
const BASE_URL = process.env.BASE_URL || 'http://localhost:3000'
const TIMELINE_LIST_URL = `${BASE_URL}/timeline/list`

// 测试数据
const mockTimelines = [
  {
    id: '1',
    title: '2024年中东地区冲突事件链',
    status: 'COMPLETED',
    eventCount: 15,
    relationCount: 23
  },
  {
    id: '2',
    title: '乌克兰局势发展时间线',
    status: 'PROCESSING',
    eventCount: 28,
    relationCount: 45
  }
]

// 辅助函数
async function waitForPageLoad(page: Page) {
  await page.waitForLoadState('networkidle')
  await page.waitForSelector('[data-testid="timeline-container"]', { timeout: 10000 })
}

async function mockApiResponses(page: Page) {
  // 模拟时间线列表API - 修复API路径，确保与optimizedApi.ts中的配置一致
  await page.route('**/timeline/list*', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        content: mockTimelines,
        totalElements: mockTimelines.length,
        totalPages: 1,
        number: 0,
        size: 10
      })
    })
  })

  // 模拟时间线搜索API - 修复API路径，确保与optimizedApi.ts中的配置一致
  await page.route('**/timeline/search*', async route => {
    const url = new URL(route.request().url())
    const keyword = url.searchParams.get('keyword')
    
    let filteredTimelines = mockTimelines
    if (keyword) {
      filteredTimelines = mockTimelines.filter(t => 
        t.title.toLowerCase().includes(keyword.toLowerCase())
      )
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        content: filteredTimelines,
        totalElements: filteredTimelines.length,
        totalPages: 1,
        number: 0,
        size: 10
      })
    })
  })

  // 模拟时间线详情API - 修复API路径，确保与optimizedApi.ts中的配置一致
  await page.route('**/timeline/detail*', async route => {
    const url = new URL(route.request().url())
    const id = url.searchParams.get('id') || url.pathname.split('/').pop()
    
    const timeline = mockTimelines.find(t => t.id === id)
    if (timeline) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          ...timeline,
          nodes: [
            {
              id: 'event-1',
              event: {
                title: '测试事件1',
                eventTime: '2024-01-10T08:00:00',
                location: '测试地点1'
              },
              nodeType: 'source',
              importanceScore: 0.95
            }
          ],
          relationships: [
            {
              id: 'rel-1',
              sourceId: 'event-1',
              targetId: 'event-2',
              type: 'cause',
              description: '测试关系'
            }
          ]
        })
      })
    } else {
      await route.fulfill({
        status: 404,
        contentType: 'application/json',
        body: JSON.stringify({ message: '时间线不存在' })
      })
    }
  })

  // 模拟删除API - 修复API路径，确保与optimizedApi.ts中的配置一致
  await page.route('**/timeline/delete*', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true })
    })
  })
}

test.describe('Timeline List E2E Tests - 端到端测试', () => {
  test.beforeEach(async ({ page }) => {
    // 设置API模拟
    await mockApiResponses(page)
  })

  test.describe('页面加载和基本功能', () => {
    test('应该正确加载时间线列表页面', async ({ page }) => {
      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 验证页面标题
      await expect(page).toHaveTitle(/TimeFlow事件管理系统/)

      // 验证页面主要元素存在
      await expect(page.locator('h2')).toContainText('事件时间线')
      await expect(page.locator('[data-testid="timeline-container"]')).toBeVisible()

      // 验证时间线卡片显示
      const timelineCards = page.locator('.timeline-card')
      await expect(timelineCards).toHaveCount(mockTimelines.length)

      // 验证第一个时间线卡片的内容
      const firstCard = timelineCards.first()
      await expect(firstCard.locator('h3')).toContainText(mockTimelines[0].title)
    })

    test('应该正确显示加载状态', async ({ page }) => {
      // 延迟API响应以测试加载状态 - 修复API路径
      await page.route('**/timeline/list*', async route => {
        await new Promise(resolve => setTimeout(resolve, 1000))
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            content: mockTimelines,
            totalElements: mockTimelines.length
          })
        })
      })

      await page.goto(TIMELINE_LIST_URL)

      // 验证加载状态显示
      await expect(page.locator('.el-loading-mask')).toBeVisible()

      // 等待加载完成
      await waitForPageLoad(page)

      // 验证加载状态消失
      await expect(page.locator('.el-loading-mask')).not.toBeVisible()
    })
  })

  test.describe('搜索功能', () => {
    test('应该正确执行关键词搜索', async ({ page }) => {
      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 输入搜索关键词
      const searchInput = page.locator('input[placeholder*="搜索时间线标题"]')
      await searchInput.fill('中东')

      // 点击搜索按钮
      await page.locator('button:has-text("搜索")').click()

      // 等待搜索结果 - 修复API路径
      await page.waitForResponse('**/timeline/search*')

      // 验证搜索结果
      const timelineCards = page.locator('.timeline-card')
      await expect(timelineCards).toHaveCount(1)
      await expect(timelineCards.first().locator('h3')).toContainText('中东')
    })

    test('应该正确处理状态筛选', async ({ page }) => {
      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 选择状态筛选
      const statusSelect = page.locator('input[placeholder*="选择状态"]').first()
      await statusSelect.click()

      // 选择"已完成"状态
      await page.locator('.el-select-dropdown__item:has-text("已完成")').click()

      // 点击搜索按钮
      await page.locator('button:has-text("搜索")').click()

      // 等待搜索结果 - 修复API路径
      await page.waitForResponse('**/timeline/search*')

      // 验证筛选结果（这里需要根据实际的状态显示逻辑调整）
      const statusTags = page.locator('.el-tag')
      await expect(statusTags.first()).toContainText('已完成')
    })

    test('应该正确重置搜索条件', async ({ page }) => {
      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 设置搜索条件
      const searchInput = page.locator('input[placeholder*="搜索时间线标题"]')
      await searchInput.fill('测试搜索')

      // 点击重置按钮
      await page.locator('button:has-text("重置")').click()

      // 验证搜索条件被重置
      await expect(searchInput).toHaveValue('')

      // 验证显示所有时间线
      const timelineCards = page.locator('.timeline-card')
      await expect(timelineCards).toHaveCount(mockTimelines.length)
    })
  })

  test.describe('时间线操作', () => {
    test('应该正确查看时间线详情', async ({ page }) => {
      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 点击第一个时间线卡片
      const firstCard = page.locator('.timeline-card').first()
      await firstCard.click()

      // 等待详情API响应 - 修复API路径
      await page.waitForResponse('**/timeline/detail*')

      // 验证详情对话框显示
      const detailDialog = page.locator('.el-dialog:has-text("时间线详情")')
      await expect(detailDialog).toBeVisible()

      // 验证详情内容
      await expect(detailDialog.locator('h2')).toContainText(mockTimelines[0].title)

      // 关闭详情对话框
      await detailDialog.locator('button:has-text("关闭")').click()
      await expect(detailDialog).not.toBeVisible()
    })

    test('应该正确处理删除操作', async ({ page }) => {
      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 找到删除按钮（假设在卡片的操作区域）
      const deleteButton = page.locator('.timeline-card').first().locator('button:has-text("删除")')
      await deleteButton.click()

      // 处理确认对话框
      const confirmDialog = page.locator('.el-message-box')
      await expect(confirmDialog).toBeVisible()
      await expect(confirmDialog).toContainText('确定要删除')

      // 点击确认删除
      await confirmDialog.locator('button:has-text("确定")').click()

      // 等待删除API响应 - 修复API路径
      await page.waitForResponse('**/timeline/delete*')

      // 验证成功消息显示
      const successMessage = page.locator('.el-message--success')
      await expect(successMessage).toContainText('删除成功')
    })

    test('应该正确打开创建时间线对话框', async ({ page }) => {
      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 点击生成时间线按钮
      const createButton = page.locator('button:has-text("生成时间线")')
      await createButton.click()

      // 验证创建对话框显示
      const createDialog = page.locator('.el-dialog:has-text("生成事件时间线")')
      await expect(createDialog).toBeVisible()

      // 关闭对话框
      await createDialog.locator('button:has-text("取消")').click()
      await expect(createDialog).not.toBeVisible()
    })
  })

  test.describe('分页功能', () => {
    test('应该正确处理分页操作', async ({ page }) => {
      // 模拟大量数据以测试分页 - 修复API路径
      await page.route('**/timeline/list*', async route => {
        const url = new URL(route.request().url())
        const page_num = parseInt(url.searchParams.get('page') || '0')
        const size = parseInt(url.searchParams.get('size') || '10')

        // 生成测试数据
        const totalItems = 25
        const startIndex = page_num * size
        const endIndex = Math.min(startIndex + size, totalItems)
        const pageItems = []

        for (let i = startIndex; i < endIndex; i++) {
          pageItems.push({
            id: `${i + 1}`,
            title: `测试时间线 ${i + 1}`,
            status: 'COMPLETED',
            eventCount: 5,
            relationCount: 3
          })
        }

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            content: pageItems,
            totalElements: totalItems,
            totalPages: Math.ceil(totalItems / size),
            number: page_num,
            size: size
          })
        })
      })

      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 验证分页组件显示
      const pagination = page.locator('.el-pagination')
      await expect(pagination).toBeVisible()

      // 点击下一页
      const nextButton = pagination.locator('button.btn-next')
      await nextButton.click()

      // 等待API响应 - 修复API路径
      await page.waitForResponse('**/timeline/list*')

      // 验证页码变化
      const currentPage = pagination.locator('.el-pager .is-active')
      await expect(currentPage).toContainText('2')
    })
  })

  test.describe('错误处理', () => {
    test('应该正确处理API错误', async ({ page }) => {
      // 模拟API错误 - 修复API路径
      await page.route('**/timeline/list*', async route => {
        await route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ message: '服务器内部错误' })
        })
      })

      await page.goto(TIMELINE_LIST_URL)

      // 验证错误消息显示
      const errorAlert = page.locator('.el-alert--error')
      await expect(errorAlert).toBeVisible()

      // 验证显示备用数据
      const timelineCards = page.locator('.timeline-card')
      await expect(timelineCards.count()).toBeGreaterThan(0)
    })

    test('应该正确处理网络连接错误', async ({ page }) => {
      // 模拟网络错误 - 修复API路径
      await page.route('**/timeline/list*', async route => {
        await route.abort('failed')
      })

      await page.goto(TIMELINE_LIST_URL)

      // 验证错误处理
      const errorMessage = page.locator('.el-message--error')
      await expect(errorMessage).toBeVisible()
    })

    test('应该正确处理空数据状态', async ({ page }) => {
      // 模拟空数据响应 - 修复API路径
      await page.route('**/timeline/list*', async route => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            content: [],
            totalElements: 0,
            totalPages: 0,
            number: 0,
            size: 10
          })
        })
      })

      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 验证空状态显示
      const emptyState = page.locator('.el-empty')
      await expect(emptyState).toBeVisible()
      await expect(emptyState).toContainText('没有找到符合条件的时间线')
    })
  })

  test.describe('响应式设计', () => {
    test('应该在移动设备上正确显示', async ({ page }) => {
      // 设置移动设备视口
      await page.setViewportSize({ width: 375, height: 667 })

      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 验证移动端布局
      const container = page.locator('[data-testid="timeline-container"]')
      await expect(container).toBeVisible()

      // 验证时间线卡片在移动端的显示
      const timelineCards = page.locator('.timeline-card')
      await expect(timelineCards.first()).toBeVisible()
    })

    test('应该在平板设备上正确显示', async ({ page }) => {
      // 设置平板设备视口
      await page.setViewportSize({ width: 768, height: 1024 })

      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 验证平板端布局
      const container = page.locator('[data-testid="timeline-container"]')
      await expect(container).toBeVisible()
    })
  })

  test.describe('性能测试', () => {
    test('页面加载性能应该在合理范围内', async ({ page }) => {
      const startTime = Date.now()

      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      const loadTime = Date.now() - startTime

      // 验证页面加载时间不超过5秒
      expect(loadTime).toBeLessThan(5000)
    })

    test('搜索响应时间应该在合理范围内', async ({ page }) => {
      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      const searchInput = page.locator('input[placeholder*="搜索时间线标题"]')
      await searchInput.fill('测试')

      const startTime = Date.now()

      await page.locator('button:has-text("搜索")').click()
      await page.waitForResponse('**/timeline/search*')

      const searchTime = Date.now() - startTime

      // 验证搜索响应时间不超过2秒
      expect(searchTime).toBeLessThan(2000)
    })
  })

  test.describe('用户体验', () => {
    test('应该提供良好的键盘导航支持', async ({ page }) => {
      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 使用Tab键导航
      await page.keyboard.press('Tab')
      await page.keyboard.press('Tab')

      // 验证焦点状态
      const focusedElement = page.locator(':focus')
      await expect(focusedElement).toBeVisible()
    })

    test('应该正确处理浏览器前进后退', async ({ page }) => {
      await page.goto(TIMELINE_LIST_URL)
      await waitForPageLoad(page)

      // 执行搜索
      const searchInput = page.locator('input[placeholder*="搜索时间线标题"]')
      await searchInput.fill('中东')
      await page.locator('button:has-text("搜索")').click()
      await page.waitForResponse('**/timeline/search*')

      // 导航到其他页面
      await page.goto(`${BASE_URL}/dashboard`)

      // 使用浏览器后退
      await page.goBack()

      // 验证搜索状态保持
      await expect(searchInput).toHaveValue('中东')
    })
  })
})