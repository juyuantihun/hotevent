import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import ErrorBoundary from '@/components/common/ErrorBoundary.vue';
import ErrorState from '@/components/common/ErrorState.vue';
import { createPinia, setActivePinia } from 'pinia';

// 创建一个会抛出错误的测试组件
const ErrorComponent = {
  template: '<div>{{ nonExistentProperty.value }}</div>',
};

// 创建一个正常的测试组件
const NormalComponent = {
  template: '<div>正常组件</div>',
};

describe('ErrorBoundary组件', () => {
  beforeEach(() => {
    // 设置Pinia
    setActivePinia(createPinia());
    
    // 清除控制台错误的模拟，避免测试输出中出现大量错误信息
    vi.spyOn(console, 'error').mockImplementation(() => {});
  });
  
  it('当子组件正常时应该正确渲染子组件', () => {
    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: NormalComponent,
      },
    });
    
    // 验证子组件被正确渲染
    expect(wrapper.text()).toContain('正常组件');
    
    // 验证错误状态组件没有被渲染
    expect(wrapper.findComponent(ErrorState).exists()).toBe(false);
  });
  
  it('当子组件抛出错误时应该显示错误状态', async () => {
    // 模拟onErrorCaptured钩子
    const errorHandler = vi.fn();
    
    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: ErrorComponent,
      },
      global: {
        config: {
          errorHandler,
        },
      },
      props: {
        errorTitle: '测试错误标题',
        errorMessage: '测试错误消息',
      },
    });
    
    // 手动触发错误
    await wrapper.vm.handleError(new Error('测试错误'), '测试信息');
    
    // 验证错误状态组件被渲染
    expect(wrapper.findComponent(ErrorState).exists()).toBe(true);
    
    // 验证错误标题和消息被正确传递
    expect(wrapper.findComponent(ErrorState).props('title')).toBe('测试错误标题');
    expect(wrapper.findComponent(ErrorState).props('message')).toBe('测试错误消息');
  });
  
  it('点击重试按钮应该重置错误状态', async () => {
    const resetSpy = vi.fn();
    
    const wrapper = mount(ErrorBoundary, {
      slots: {
        default: NormalComponent,
      },
      props: {
        showResetButton: true,
      },
    });
    
    // 设置错误状态
    await wrapper.vm.handleError(new Error('测试错误'), '测试信息');
    
    // 模拟resetError方法
    wrapper.vm.resetError = resetSpy;
    
    // 找到并点击重置按钮
    const resetButton = wrapper.find('.reset-button');
    await resetButton.trigger('click');
    
    // 验证resetError方法被调用
    expect(resetSpy).toHaveBeenCalled();
  });
  
  it('应该正确发出错误事件', async () => {
    const wrapper = mount(ErrorBoundary);
    
    // 设置错误状态
    const testError = new Error('测试错误');
    await wrapper.vm.handleError(testError, '测试信息');
    
    // 验证错误事件被发出
    const emitted = wrapper.emitted('error');
    expect(emitted).toBeTruthy();
    expect(emitted?.[0][0]).toEqual({ error: testError, info: '测试信息' });
  });
  
  it('当reportToGlobal为true时应该设置全局错误', async () => {
    const wrapper = mount(ErrorBoundary, {
      props: {
        reportToGlobal: true,
        errorTitle: '全局错误测试',
      },
    });
    
    // 设置错误状态
    await wrapper.vm.handleError(new Error('测试错误'), '测试信息');
    
    // 验证全局错误被设置
    const appStore = wrapper.vm.appStore;
    expect(appStore.setGlobalError).toHaveBeenCalledWith('全局错误测试: 测试错误');
  });
  
  it('resetError应该清除错误状态', async () => {
    const wrapper = mount(ErrorBoundary);
    
    // 设置错误状态
    await wrapper.vm.handleError(new Error('测试错误'), '测试信息');
    
    // 重置错误
    await wrapper.vm.resetError();
    
    // 验证错误状态被清除
    expect(wrapper.vm.error).toBeNull();
    expect(wrapper.vm.errorInfo).toBeNull();
  });
  
  it('当resetError被调用且reportToGlobal为true时应该清除全局错误', async () => {
    const wrapper = mount(ErrorBoundary, {
      props: {
        reportToGlobal: true,
      },
    });
    
    // 设置错误状态
    await wrapper.vm.handleError(new Error('测试错误'), '测试信息');
    
    // 重置错误
    await wrapper.vm.resetError();
    
    // 验证全局错误被清除
    const appStore = wrapper.vm.appStore;
    expect(appStore.setGlobalError).toHaveBeenCalledWith(null);
  });
});