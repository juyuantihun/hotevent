import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import GlobalErrorDisplay from '@/components/common/GlobalErrorDisplay.vue';
import { createPinia, setActivePinia } from 'pinia';
import { useAppStore } from '@/store';

describe('GlobalErrorDisplay组件', () => {
  beforeEach(() => {
    // 设置Pinia
    setActivePinia(createPinia());
    
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
  });
  
  it('当没有全局错误时不应显示', () => {
    const appStore = useAppStore();
    appStore.globalError = null;
    
    const wrapper = mount(GlobalErrorDisplay);
    
    // 验证组件不可见
    expect(wrapper.isVisible()).toBe(false);
  });
  
  it('当有全局错误时应显示错误消息', async () => {
    const appStore = useAppStore();
    appStore.globalError = '测试全局错误';
    
    const wrapper = mount(GlobalErrorDisplay);
    
    // 验证组件可见
    expect(wrapper.isVisible()).toBe(true);
    
    // 验证显示了错误消息
    expect(wrapper.text()).toContain('测试全局错误');
  });
  
  it('点击关闭按钮应该清除全局错误', async () => {
    const appStore = useAppStore();
    appStore.globalError = '测试全局错误';
    
    // 模拟setGlobalError方法
    vi.spyOn(appStore, 'setGlobalError');
    
    const wrapper = mount(GlobalErrorDisplay);
    
    // 点击关闭按钮
    await wrapper.find('.close-button').trigger('click');
    
    // 验证setGlobalError被调用
    expect(appStore.setGlobalError).toHaveBeenCalledWith(null);
  });
  
  it('应该在错误变化时更新显示', async () => {
    const appStore = useAppStore();
    appStore.globalError = null;
    
    const wrapper = mount(GlobalErrorDisplay);
    
    // 初始状态不可见
    expect(wrapper.isVisible()).toBe(false);
    
    // 设置全局错误
    appStore.globalError = '新的全局错误';
    await wrapper.vm.$nextTick();
    
    // 验证组件变为可见
    expect(wrapper.isVisible()).toBe(true);
    expect(wrapper.text()).toContain('新的全局错误');
    
    // 清除全局错误
    appStore.globalError = null;
    await wrapper.vm.$nextTick();
    
    // 验证组件再次变为不可见
    expect(wrapper.isVisible()).toBe(false);
  });
  
  it('应该在指定时间后自动关闭', async () => {
    // 模拟定时器
    vi.useFakeTimers();
    
    const appStore = useAppStore();
    appStore.globalError = '测试全局错误';
    
    // 模拟setGlobalError方法
    vi.spyOn(appStore, 'setGlobalError');
    
    const wrapper = mount(GlobalErrorDisplay, {
      props: {
        autoClose: true,
        autoCloseDelay: 3000
      }
    });
    
    // 验证组件可见
    expect(wrapper.isVisible()).toBe(true);
    
    // 前进3秒
    vi.advanceTimersByTime(3000);
    
    // 验证setGlobalError被调用
    expect(appStore.setGlobalError).toHaveBeenCalledWith(null);
    
    // 恢复真实定时器
    vi.useRealTimers();
  });
  
  it('鼠标悬停时应该暂停自动关闭', async () => {
    // 模拟定时器
    vi.useFakeTimers();
    
    const appStore = useAppStore();
    appStore.globalError = '测试全局错误';
    
    // 模拟setGlobalError方法
    vi.spyOn(appStore, 'setGlobalError');
    
    const wrapper = mount(GlobalErrorDisplay, {
      props: {
        autoClose: true,
        autoCloseDelay: 3000
      }
    });
    
    // 模拟鼠标悬停
    await wrapper.trigger('mouseenter');
    
    // 前进5秒
    vi.advanceTimersByTime(5000);
    
    // 验证setGlobalError没有被调用
    expect(appStore.setGlobalError).not.toHaveBeenCalled();
    
    // 模拟鼠标离开
    await wrapper.trigger('mouseleave');
    
    // 前进3秒
    vi.advanceTimersByTime(3000);
    
    // 验证setGlobalError被调用
    expect(appStore.setGlobalError).toHaveBeenCalledWith(null);
    
    // 恢复真实定时器
    vi.useRealTimers();
  });
  
  it('应该根据错误类型应用不同的样式', async () => {
    const appStore = useAppStore();
    
    // 测试不同的错误类型
    const errorTypes = ['error', 'warning', 'info'];
    
    for (const type of errorTypes) {
      appStore.globalError = '测试错误';
      appStore.globalErrorType = type;
      
      const wrapper = mount(GlobalErrorDisplay);
      await wrapper.vm.$nextTick();
      
      // 验证应用了正确的样式类
      expect(wrapper.classes()).toContain(`global-error-${type}`);
    }
  });
});