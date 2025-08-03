import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createRouter, createWebHistory } from 'vue-router';
import { ElMessage } from 'element-plus';
import CreateTimelineForm from '@/views/timeline/components/CreateTimelineForm.vue';
import RegionSelector from '@/views/timeline/components/RegionSelector.vue';

// 模拟Element Plus组件
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus');
  return {
    ...actual as any,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn(),
    },
  };
});

// 模拟API
vi.mock('@/api/timeline', () => ({
  createTimeline: vi.fn().mockImplementation((data) => {
    return Promise.resolve({
      id: '123',
      ...data,
      createdAt: new Date().toISOString()
    });
  })
}));

vi.mock('@/api/region', () => ({
  getRegionTree: vi.fn().mockResolvedValue([
    {
      id: '1',
      name: '亚洲',
      code: 'ASIA',
      children: [
        {
          id: '101',
          name: '中国',
          code: 'CN',
          children: []
        },
        {
          id: '102',
          name: '日本',
          code: 'JP',
          children: []
        }
      ]
    },
    {
      id: '2',
      name: '欧洲',
      code: 'EU',
      children: [
        {
          id: '201',
          name: '德国',
          code: 'DE',
          children: []
        },
        {
          id: '202',
          name: '法国',
          code: 'FR',
          children: []
        }
      ]
    }
  ])
}));

describe('表单处理集成测试', () => {
  let router;
  
  beforeEach(() => {
    // 设置Pinia
    setActivePinia(createPinia());
    
    // 创建路由
    router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: '/',
          component: { template: '<div>Home</div>' }
        },
        {
          path: '/timeline',
          component: { template: '<div>Timeline</div>' }
        }
      ]
    });
    
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
  });
  
  it('创建时间线表单应该正确验证和提交', async () => {
    // 挂载创建时间线表单组件
    const wrapper = mount(CreateTimelineForm, {
      global: {
        plugins: [router],
        stubs: {
          RegionSelector: true,
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-button': true,
          'el-date-picker': true
        }
      }
    });
    
    // 初始状态应该是无效的
    expect(wrapper.vm.isFormValid()).toBe(false);
    
    // 填写表单
    wrapper.vm.form.title = '测试时间线';
    wrapper.vm.form.description = '这是一个测试时间线';
    wrapper.vm.form.region = {
      id: '101',
      name: '中国',
      code: 'CN'
    };
    wrapper.vm.form.startDate = new Date('2023-01-01');
    wrapper.vm.form.endDate = new Date('2023-12-31');
    wrapper.vm.form.timeSpan = 'P365D';
    
    // 验证表单有效性
    expect(wrapper.vm.isFormValid()).toBe(true);
    
    // 提交表单
    await wrapper.vm.submitForm();
    
    // 验证API被调用
    const { createTimeline } = require('@/api/timeline');
    expect(createTimeline).toHaveBeenCalledWith(expect.objectContaining({
      title: '测试时间线',
      description: '这是一个测试时间线',
      regionId: '101'
    }));
    
    // 验证成功消息
    expect(ElMessage.success).toHaveBeenCalledWith(expect.stringContaining('创建成功'));
    
    // 验证成功事件被触发
    expect(wrapper.emitted('success')).toBeTruthy();
    expect(wrapper.emitted('success')[0][0].id).toBe('123');
  });
  
  it('表单验证应该正确工作', async () => {
    // 挂载创建时间线表单组件
    const wrapper = mount(CreateTimelineForm, {
      global: {
        plugins: [router],
        stubs: {
          RegionSelector: true,
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-button': true,
          'el-date-picker': true
        }
      }
    });
    
    // 模拟表单验证方法
    wrapper.vm.$refs.form = {
      validate: vi.fn((callback) => {
        // 模拟验证失败
        callback(false);
      })
    };
    
    // 提交表单
    await wrapper.vm.submitForm();
    
    // 验证API没有被调用
    const { createTimeline } = require('@/api/timeline');
    expect(createTimeline).not.toHaveBeenCalled();
    
    // 验证错误消息
    expect(ElMessage.error).toHaveBeenCalledWith(expect.stringContaining('请检查表单'));
    
    // 验证成功事件没有被触发
    expect(wrapper.emitted('success')).toBeFalsy();
  });
  
  it('地区选择器应该正确加载和选择地区', async () => {
    // 挂载地区选择器组件
    const wrapper = mount(RegionSelector, {
      global: {
        plugins: [router],
        stubs: {
          'el-tree': true,
          'el-input': true
        }
      }
    });
    
    // 等待地区数据加载
    await wrapper.vm.$nextTick();
    
    // 验证地区数据被加载
    expect(wrapper.vm.regions.length).toBe(2);
    expect(wrapper.vm.regions[0].name).toBe('亚洲');
    expect(wrapper.vm.regions[0].children.length).toBe(2);
    
    // 模拟选择地区
    const selectedRegion = {
      id: '101',
      name: '中国',
      code: 'CN'
    };
    wrapper.vm.handleNodeClick(selectedRegion);
    
    // 验证选择事件被触发
    expect(wrapper.emitted('select')).toBeTruthy();
    expect(wrapper.emitted('select')[0][0]).toEqual(selectedRegion);
    
    // 验证选中状态更新
    expect(wrapper.vm.selectedRegion).toEqual(selectedRegion);
  });
  
  it('表单和选择器应该能够协同工作', async () => {
    // 创建一个包含表单和选择器的测试组件
    const TestComponent = {
      template: `
        <div>
          <CreateTimelineForm ref="form" @success="handleSuccess" />
          <div v-if="successData" class="success-message">创建成功: {{ successData.title }}</div>
        </div>
      `,
      components: {
        CreateTimelineForm
      },
      data() {
        return {
          successData: null
        };
      },
      methods: {
        handleSuccess(data) {
          this.successData = data;
        },
        async fillAndSubmitForm() {
          const form = this.$refs.form;
          form.form.title = '测试时间线';
          form.form.description = '这是一个测试时间线';
          form.form.region = {
            id: '101',
            name: '中国',
            code: 'CN'
          };
          form.form.startDate = new Date('2023-01-01');
          form.form.endDate = new Date('2023-12-31');
          form.form.timeSpan = 'P365D';
          
          await form.submitForm();
        }
      }
    };
    
    // 挂载测试组件
    const wrapper = mount(TestComponent, {
      global: {
        plugins: [router],
        stubs: {
          RegionSelector: true,
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-button': true,
          'el-date-picker': true
        }
      }
    });
    
    // 模拟表单验证方法
    wrapper.findComponent(CreateTimelineForm).vm.$refs.form = {
      validate: vi.fn((callback) => {
        // 模拟验证成功
        callback(true);
      })
    };
    
    // 填写并提交表单
    await wrapper.vm.fillAndSubmitForm();
    
    // 验证API被调用
    const { createTimeline } = require('@/api/timeline');
    expect(createTimeline).toHaveBeenCalled();
    
    // 验证成功消息显示
    await wrapper.vm.$nextTick();
    expect(wrapper.find('.success-message').exists()).toBe(true);
    expect(wrapper.find('.success-message').text()).toContain('测试时间线');
  });
  
  it('表单提交失败应该显示错误消息', async () => {
    // 模拟API失败
    const { createTimeline } = require('@/api/timeline');
    createTimeline.mockRejectedValueOnce(new Error('创建失败'));
    
    // 挂载创建时间线表单组件
    const wrapper = mount(CreateTimelineForm, {
      global: {
        plugins: [router],
        stubs: {
          RegionSelector: true,
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-button': true,
          'el-date-picker': true
        }
      }
    });
    
    // 模拟表单验证方法
    wrapper.vm.$refs.form = {
      validate: vi.fn((callback) => {
        // 模拟验证成功
        callback(true);
      })
    };
    
    // 填写表单
    wrapper.vm.form.title = '测试时间线';
    wrapper.vm.form.description = '这是一个测试时间线';
    wrapper.vm.form.region = {
      id: '101',
      name: '中国',
      code: 'CN'
    };
    wrapper.vm.form.startDate = new Date('2023-01-01');
    wrapper.vm.form.endDate = new Date('2023-12-31');
    wrapper.vm.form.timeSpan = 'P365D';
    
    // 提交表单
    await wrapper.vm.submitForm();
    
    // 验证API被调用
    expect(createTimeline).toHaveBeenCalled();
    
    // 验证错误消息
    expect(ElMessage.error).toHaveBeenCalledWith(expect.stringContaining('创建失败'));
    
    // 验证成功事件没有被触发
    expect(wrapper.emitted('success')).toBeFalsy();
    
    // 验证错误事件被触发
    expect(wrapper.emitted('error')).toBeTruthy();
  });
});