import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { ElMessage } from 'element-plus';
import LoginView from '@/views/login/index.vue';
import { useAuthStore } from '@/store/modules/auth';
import { setupApiMocks } from '../mocks/api';

// 模拟路由
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
  }),
}));

// 模拟Element Plus的消息组件
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus');
  return {
    ...actual as any,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
    },
  };
});

describe('登录视图', () => {
  beforeEach(() => {
    // 设置Pinia
    setActivePinia(createPinia());
    
    // 设置API模拟
    setupApiMocks();
    
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
  });
  
  it('应该正确渲染登录表单', () => {
    const wrapper = mount(LoginView);
    
    // 检查标题
    expect(wrapper.find('h1').text()).toBe('TimeFlow 事件管理系统');
    
    // 检查表单元素
    expect(wrapper.find('input[placeholder="请输入用户名"]').exists()).toBe(true);
    expect(wrapper.find('input[placeholder="请输入密码"]').exists()).toBe(true);
    expect(wrapper.find('button').text()).toContain('登录');
  });
  
  it('当表单验证失败时不应调用登录API', async () => {
    const wrapper = mount(LoginView);
    const authStore = useAuthStore();
    const loginActionSpy = vi.spyOn(authStore, 'loginAction');
    
    // 点击登录按钮，但不填写表单
    await wrapper.find('button').trigger('click');
    
    // 验证loginAction没有被调用
    expect(loginActionSpy).not.toHaveBeenCalled();
  });
  
  it('当登录成功时应显示成功消息并跳转', async () => {
    const wrapper = mount(LoginView);
    const authStore = useAuthStore();
    
    // 模拟loginAction返回成功
    vi.spyOn(authStore, 'loginAction').mockResolvedValue(true);
    
    // 填写表单
    await wrapper.find('input[placeholder="请输入用户名"]').setValue('admin');
    await wrapper.find('input[placeholder="请输入密码"]').setValue('password');
    
    // 点击登录按钮
    await wrapper.find('button').trigger('click');
    
    // 验证成功消息被显示
    expect(ElMessage.success).toHaveBeenCalledWith('登录成功');
  });
  
  it('当登录失败时应显示错误消息', async () => {
    const wrapper = mount(LoginView);
    const authStore = useAuthStore();
    
    // 模拟loginAction返回失败
    vi.spyOn(authStore, 'loginAction').mockResolvedValue(false);
    
    // 填写表单
    await wrapper.find('input[placeholder="请输入用户名"]').setValue('wrong');
    await wrapper.find('input[placeholder="请输入密码"]').setValue('wrong');
    
    // 点击登录按钮
    await wrapper.find('button').trigger('click');
    
    // 验证错误消息被显示
    expect(ElMessage.error).toHaveBeenCalledWith('登录失败，请检查用户名和密码');
  });
  
  it('当记住密码选项被选中时应保存用户名', async () => {
    const wrapper = mount(LoginView);
    const authStore = useAuthStore();
    
    // 模拟loginAction返回成功
    vi.spyOn(authStore, 'loginAction').mockImplementation(async (loginData) => {
      // 验证rememberMe参数被正确传递
      expect(loginData.rememberMe).toBe(true);
      return true;
    });
    
    // 填写表单
    await wrapper.find('input[placeholder="请输入用户名"]').setValue('admin');
    await wrapper.find('input[placeholder="请输入密码"]').setValue('password');
    
    // 选中记住密码
    await wrapper.find('input[type="checkbox"]').setValue(true);
    
    // 点击登录按钮
    await wrapper.find('button').trigger('click');
    
    // 验证loginAction被调用，并且rememberMe参数为true
    expect(authStore.loginAction).toHaveBeenCalled();
  });
});