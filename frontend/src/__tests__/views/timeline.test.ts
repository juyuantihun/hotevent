import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { ElMessage, ElMessageBox } from 'element-plus';
import { mockTimelines } from '../fixtures/timelines';
import { setupApiMocks } from '../mocks/api';

// 模拟时间线API
vi.mock('@/api/timeline', () => ({
  timelineApi: {
    getTimelineList: vi.fn().mockResolvedValue({ content: mockTimelines, totalElements: mockTimelines.length }),
    combinedSearchTimelines: vi.fn().mockResolvedValue({ content: mockTimelines, totalElements: mockTimelines.length }),
    getTimelineWithDetails: vi.fn().mockResolvedValue(mockTimelines[0]),
    deleteTimeline: vi.fn().mockResolvedValue({ success: true }),
  }
}));

// 模拟Element Plus组件
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus');
  return {
    ...actual as any,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      info: vi.fn(),
    },
    ElMessageBox: {
      confirm: vi.fn().mockResolvedValue(true),
    },
  };
});

// 模拟组件
vi.mock('@/views/timeline/components/CreateTimelineForm.vue', () => ({
  default: {
    template: '<div>创建时间线表单</div>',
  },
}));

vi.mock('@/views/timeline/components/DeduplicationPanel.vue', () => ({
  default: {
    template: '<div>事件去重面板</div>',
  },
}));

vi.mock('@/views/timeline/components/DictionaryPanel.vue', () => ({
  default: {
    template: '<div>字典管理面板</div>',
  },
}));

vi.mock('@/views/timeline/components/StatisticsPanel.vue', () => ({
  default: {
    template: '<div>统计信息面板</div>',
  },
}));

vi.mock('@/views/timeline/components/TimelineDetailView.vue', () => ({
  default: {
    template: '<div>时间线详情视图</div>',
    props: ['timeline'],
  },
}));

// 导入被测试组件
const TimelineIndex = await import('@/views/timeline/index.vue');

describe('时间线列表页面', () => {
  beforeEach(() => {
    // 设置Pinia
    setActivePinia(createPinia());
    
    // 设置API模拟
    setupApiMocks();
    
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
  });
  
  it('应该正确加载时间线列表', async () => {
    const wrapper = mount(TimelineIndex.default);
    
    // 等待异步操作完成
    await vi.waitFor(() => {
      expect(wrapper.vm.loading).toBe(false);
    });
    
    // 验证时间线列表是否正确加载
    expect(wrapper.vm.timelineList.length).toBeGreaterThan(0);
    
    // 验证时间线卡片是否正确渲染
    const cards = wrapper.findAll('.timeline-card');
    expect(cards.length).toBe(wrapper.vm.timelineList.length);
  });
  
  it('应该正确处理搜索操作', async () => {
    const wrapper = mount(TimelineIndex.default);
    
    // 设置搜索条件
    await wrapper.vm.searchForm.keyword = '测试';
    
    // 触发搜索
    await wrapper.vm.handleSearch();
    
    // 验证搜索API是否被调用
    expect(wrapper.vm.timelineApi.combinedSearchTimelines).toHaveBeenCalled();
    
    // 验证分页是否重置
    expect(wrapper.vm.pagination.page).toBe(1);
  });
  
  it('应该正确处理重置搜索操作', async () => {
    const wrapper = mount(TimelineIndex.default);
    
    // 设置搜索条件
    await wrapper.vm.searchForm.keyword = '测试';
    await wrapper.vm.searchForm.statuses = ['COMPLETED'];
    
    // 触发重置
    await wrapper.vm.resetSearch();
    
    // 验证搜索条件是否重置
    expect(wrapper.vm.searchForm.keyword).toBe('');
    expect(wrapper.vm.searchForm.statuses).toEqual([]);
    expect(wrapper.vm.searchForm.sort).toBe('createdAt');
    expect(wrapper.vm.searchForm.direction).toBe('desc');
    
    // 验证分页是否重置
    expect(wrapper.vm.pagination.page).toBe(1);
    
    // 验证列表是否重新加载
    expect(wrapper.vm.timelineApi.getTimelineList).toHaveBeenCalled();
  });
  
  it('应该正确处理分页操作', async () => {
    const wrapper = mount(TimelineIndex.default);
    
    // 触发页码变化
    await wrapper.vm.handleCurrentChange(2);
    
    // 验证分页是否更新
    expect(wrapper.vm.pagination.page).toBe(2);
    
    // 验证列表是否重新加载
    expect(wrapper.vm.timelineApi.getTimelineList).toHaveBeenCalled();
    
    // 触发每页数量变化
    await wrapper.vm.handleSizeChange(20);
    
    // 验证分页是否更新
    expect(wrapper.vm.pagination.size).toBe(20);
    
    // 验证列表是否重新加载
    expect(wrapper.vm.timelineApi.getTimelineList).toHaveBeenCalled();
  });
  
  it('应该正确处理查看时间线详情', async () => {
    const wrapper = mount(TimelineIndex.default);
    
    // 等待异步操作完成
    await vi.waitFor(() => {
      expect(wrapper.vm.loading).toBe(false);
    });
    
    // 触发查看详情
    await wrapper.vm.viewTimeline(mockTimelines[0]);
    
    // 验证详情API是否被调用
    expect(wrapper.vm.timelineApi.getTimelineWithDetails).toHaveBeenCalledWith(mockTimelines[0].id);
    
    // 验证详情对话框是否显示
    expect(wrapper.vm.showDetailDialog).toBe(true);
    expect(wrapper.vm.selectedTimeline).toBeTruthy();
  });
  
  it('应该正确处理删除时间线', async () => {
    const wrapper = mount(TimelineIndex.default);
    
    // 等待异步操作完成
    await vi.waitFor(() => {
      expect(wrapper.vm.loading).toBe(false);
    });
    
    // 触发删除
    await wrapper.vm.deleteTimeline(mockTimelines[0]);
    
    // 验证确认对话框是否显示
    expect(ElMessageBox.confirm).toHaveBeenCalled();
    
    // 验证删除API是否被调用
    expect(wrapper.vm.timelineApi.deleteTimeline).toHaveBeenCalledWith(mockTimelines[0].id);
    
    // 验证成功消息是否显示
    expect(ElMessage.success).toHaveBeenCalledWith('删除成功');
    
    // 验证列表是否重新加载
    expect(wrapper.vm.timelineApi.getTimelineList).toHaveBeenCalled();
  });
  
  it('应该正确处理创建时间线成功', async () => {
    const wrapper = mount(TimelineIndex.default);
    
    // 触发创建成功
    await wrapper.vm.handleCreateSuccess({ id: '4', title: '新时间线' });
    
    // 验证创建对话框是否关闭
    expect(wrapper.vm.showCreateDialog).toBe(false);
    
    // 验证成功消息是否显示
    expect(ElMessage.success).toHaveBeenCalledWith('时间线生成成功');
    
    // 验证列表是否重新加载
    expect(wrapper.vm.timelineApi.getTimelineList).toHaveBeenCalled();
  });
  
  it('应该正确格式化时间跨度', () => {
    const wrapper = mount(TimelineIndex.default);
    
    // 测试格式化时间跨度
    expect(wrapper.vm.formatTimeSpan('PT24H')).toBe('24小时');
    expect(wrapper.vm.formatTimeSpan('PT30M')).toBe('30分钟');
    expect(wrapper.vm.formatTimeSpan('')).toBe('-');
    expect(wrapper.vm.formatTimeSpan(undefined)).toBe('-');
  });
  
  it('应该正确格式化日期', () => {
    const wrapper = mount(TimelineIndex.default);
    
    // 测试格式化日期
    const date = new Date('2023-01-01T12:00:00').toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
    
    expect(wrapper.vm.formatDate('2023-01-01T12:00:00')).toBe(date);
    expect(wrapper.vm.formatDate('')).toBe('-');
    expect(wrapper.vm.formatDate(undefined)).toBe('-');
  });
});