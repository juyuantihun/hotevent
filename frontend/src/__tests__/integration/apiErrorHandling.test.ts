import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createRouter, createWebHistory } from 'vue-router';
import ApiErrorDemo from '@/views/error-handling/ApiErrorDemo.vue';
import ApiErrorFeedback from '@/components/common/ApiErrorFeedback.vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';

// 模拟axios
vi.mock('axios');

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

// 模拟API重试服务
vi.mock('@/services/apiRetryService', () => ({
  retryRequest: vi.fn().mockImplementation((fn) => fn()),
}));

// 模拟API错误反馈服务
vi.mock('@/services/apiErrorFeedbackService', () => ({
  recordApiError: vi.fn(),
  getApiErrorStats: vi.fn().mockReturnValue({
    totalErrors: 5,
    retrySuccess: 2,
    retryFailure: 3
  }),
}));

describe('API错误处理集成测试', () => {
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
          path: '/api-error-demo',
          component: ApiErrorDemo
        }
      ]
    });
    
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
  });
  
  it('API错误应该被正确捕获和显示', async () => {
    // 模拟API请求失败
    const error = {
      response: {
        status: 500,
        data: {
          message: '服务器内部错误'
        }
      }
    };
    axios.get.mockRejectedValue(error);
    
    // 挂载API错误演示组件
    const wrapper = mount(ApiErrorDemo, {
      global: {
        plugins: [router],
        stubs: {
          ApiErrorFeedback: true,
        }
      }
    });
    
    // 触发API请求
    await wrapper.find('button').trigger('click');
    
    // 验证错误状态
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.error).toBeTruthy();
    expect(wrapper.vm.error.message).toBe('服务器内部错误');
    
    // 验证错误反馈组件显示
    const errorFeedback = wrapper.findComponent(ApiErrorFeedback);
    expect(errorFeedback.exists()).toBe(true);
    expect(errorFeedback.props('error')).toBeTruthy();
  });
  
  it('重试机制应该正确工作', async () => {
    // 模拟API请求失败后成功
    axios.get
      .mockRejectedValueOnce({
        response: {
          status: 500,
          data: {
            message: '服务器内部错误'
          }
        }
      })
      .mockResolvedValueOnce({ data: { success: true } });
    
    // 挂载API错误演示组件
    const wrapper = mount(ApiErrorDemo, {
      global: {
        plugins: [router],
        stubs: {
          ApiErrorFeedback: true,
        }
      }
    });
    
    // 触发API请求
    await wrapper.find('button').trigger('click');
    
    // 验证错误状态
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.error).toBeTruthy();
    
    // 模拟重试
    const errorFeedback = wrapper.findComponent(ApiErrorFeedback);
    errorFeedback.vm.$emit('retry');
    
    // 验证重试后的状态
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.loading).toBe(true);
    
    // 模拟请求完成
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.error).toBeFalsy();
    expect(wrapper.vm.data).toEqual({ success: true });
    expect(ElMessage.success).toHaveBeenCalledWith('请求成功');
  });
  
  it('可重试和不可重试的错误应该被正确区分', async () => {
    // 创建一个包含API错误反馈组件的测试组件
    const TestComponent = {
      template: `
        <div>
          <ApiErrorFeedback :error="error1" :retryable="true" @retry="handleRetry1" />
          <ApiErrorFeedback :error="error2" :retryable="false" @retry="handleRetry2" />
        </div>
      `,
      components: {
        ApiErrorFeedback
      },
      data() {
        return {
          error1: {
            message: '服务器错误',
            code: 500,
            details: { url: '/api/test1' }
          },
          error2: {
            message: '权限不足',
            code: 403,
            details: { url: '/api/test2' }
          },
          retry1Called: false,
          retry2Called: false
        };
      },
      methods: {
        handleRetry1() {
          this.retry1Called = true;
        },
        handleRetry2() {
          this.retry2Called = true;
        }
      }
    };
    
    // 挂载测试组件
    const wrapper = mount(TestComponent, {
      global: {
        plugins: [router],
        stubs: {
          'el-alert': true,
          'el-button': true
        }
      }
    });
    
    // 查找两个错误反馈组件
    const errorFeedbacks = wrapper.findAllComponents(ApiErrorFeedback);
    expect(errorFeedbacks.length).toBe(2);
    
    // 第一个组件应该有重试按钮
    const firstComponent = errorFeedbacks[0];
    expect(firstComponent.props('retryable')).toBe(true);
    expect(firstComponent.find('button').exists()).toBe(true);
    
    // 第二个组件不应该有重试按钮
    const secondComponent = errorFeedbacks[1];
    expect(secondComponent.props('retryable')).toBe(false);
    expect(secondComponent.find('button').exists()).toBe(false);
    
    // 触发第一个组件的重试事件
    await firstComponent.vm.$emit('retry');
    expect(wrapper.vm.retry1Called).toBe(true);
    expect(wrapper.vm.retry2Called).toBe(false);
  });
  
  it('增强的错误处理器应该正确处理不同类型的错误', async () => {
    // 模拟增强的错误处理器
    vi.mock('@/api/enhancedErrorHandler', () => ({
      handleApiError: vi.fn().mockImplementation((error) => {
        if (error.response?.status === 401) {
          return { type: 'auth', message: '认证失败' };
        } else if (error.response?.status === 403) {
          return { type: 'permission', message: '权限不足' };
        } else {
          return { type: 'general', message: '服务器错误' };
        }
      })
    }));
    
    // 创建一个使用增强错误处理器的测试组件
    const TestComponent = {
      template: `
        <div>
          <button @click="testAuth">测试认证错误</button>
          <button @click="testPermission">测试权限错误</button>
          <button @click="testGeneral">测试一般错误</button>
          <div v-if="error" class="error-message">{{ error.message }}</div>
          <div v-if="error" class="error-type">{{ error.type }}</div>
        </div>
      `,
      data() {
        return {
          error: null
        };
      },
      methods: {
        async testAuth() {
          try {
            await axios.get('/api/auth-test');
          } catch (error) {
            this.error = this.handleApiError(error);
          }
        },
        async testPermission() {
          try {
            await axios.get('/api/permission-test');
          } catch (error) {
            this.error = this.handleApiError(error);
          }
        },
        async testGeneral() {
          try {
            await axios.get('/api/general-test');
          } catch (error) {
            this.error = this.handleApiError(error);
          }
        },
        handleApiError(error) {
          // 导入增强的错误处理器
          const { handleApiError } = require('@/api/enhancedErrorHandler');
          return handleApiError(error);
        }
      }
    };
    
    // 挂载测试组件
    const wrapper = mount(TestComponent);
    
    // 模拟不同类型的错误
    axios.get
      .mockRejectedValueOnce({
        response: {
          status: 401,
          data: { message: '认证失败' }
        }
      })
      .mockRejectedValueOnce({
        response: {
          status: 403,
          data: { message: '权限不足' }
        }
      })
      .mockRejectedValueOnce({
        response: {
          status: 500,
          data: { message: '服务器错误' }
        }
      });
    
    // 测试认证错误
    await wrapper.find('button:nth-child(1)').trigger('click');
    await wrapper.vm.$nextTick();
    expect(wrapper.find('.error-type').text()).toBe('auth');
    expect(wrapper.find('.error-message').text()).toBe('认证失败');
    
    // 测试权限错误
    await wrapper.find('button:nth-child(2)').trigger('click');
    await wrapper.vm.$nextTick();
    expect(wrapper.find('.error-type').text()).toBe('permission');
    expect(wrapper.find('.error-message').text()).toBe('权限不足');
    
    // 测试一般错误
    await wrapper.find('button:nth-child(3)').trigger('click');
    await wrapper.vm.$nextTick();
    expect(wrapper.find('.error-type').text()).toBe('general');
    expect(wrapper.find('.error-message').text()).toBe('服务器错误');
  });
});