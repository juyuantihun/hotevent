import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { permission, role } from '@/directives/permission';
import { useAuthStore } from '@/store/modules/auth';

// 创建一个测试组件
const TestComponent = {
  template: `
    <div>
      <button v-permission="'user:create'" id="single-permission">创建用户</button>
      <button v-permission="['user:create', 'user:edit']" id="any-permission">创建或编辑用户</button>
      <button v-permission.all="['user:create', 'user:edit']" id="all-permission">创建和编辑用户</button>
      <button v-role="'admin'" id="single-role">管理员操作</button>
      <button v-role="['admin', 'editor']" id="any-role">管理员或编辑者操作</button>
      <button v-role.all="['admin', 'editor']" id="all-role">管理员和编辑者操作</button>
    </div>
  `,
  directives: {
    permission,
    role,
  },
};

describe('权限指令', () => {
  beforeEach(() => {
    // 设置Pinia
    setActivePinia(createPinia());
    
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
  });
  
  it('当用户有权限时应显示元素', () => {
    const authStore = useAuthStore();
    
    // 模拟用户有权限
    vi.spyOn(authStore, 'hasPermission').mockImplementation((perm) => {
      return perm === 'user:create';
    });
    
    vi.spyOn(authStore, 'isLoggedIn', 'get').mockReturnValue(true);
    
    const wrapper = mount(TestComponent);
    
    // 检查单个权限的元素是否显示
    expect(wrapper.find('#single-permission').exists()).toBe(true);
    
    // 检查多个权限（满足任一权限）的元素是否显示
    expect(wrapper.find('#any-permission').exists()).toBe(true);
    
    // 检查多个权限（必须同时满足所有权限）的元素是否隐藏
    expect(wrapper.find('#all-permission').exists()).toBe(false);
  });
  
  it('当用户没有权限时应隐藏元素', () => {
    const authStore = useAuthStore();
    
    // 模拟用户没有权限
    vi.spyOn(authStore, 'hasPermission').mockReturnValue(false);
    vi.spyOn(authStore, 'isLoggedIn', 'get').mockReturnValue(true);
    
    const wrapper = mount(TestComponent);
    
    // 检查所有权限元素是否隐藏
    expect(wrapper.find('#single-permission').exists()).toBe(false);
    expect(wrapper.find('#any-permission').exists()).toBe(false);
    expect(wrapper.find('#all-permission').exists()).toBe(false);
  });
  
  it('当用户是超级管理员时应显示所有元素', () => {
    const authStore = useAuthStore();
    
    // 模拟用户是超级管理员
    vi.spyOn(authStore, 'roles', 'get').mockReturnValue(['admin']);
    vi.spyOn(authStore, 'isLoggedIn', 'get').mockReturnValue(true);
    
    const wrapper = mount(TestComponent);
    
    // 检查所有权限元素是否显示
    expect(wrapper.find('#single-permission').exists()).toBe(true);
    expect(wrapper.find('#any-permission').exists()).toBe(true);
    expect(wrapper.find('#all-permission').exists()).toBe(true);
  });
});

describe('角色指令', () => {
  beforeEach(() => {
    // 设置Pinia
    setActivePinia(createPinia());
    
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
  });
  
  it('当用户有角色时应显示元素', () => {
    const authStore = useAuthStore();
    
    // 模拟用户有角色
    vi.spyOn(authStore, 'hasRole').mockImplementation((r) => {
      return r === 'admin';
    });
    
    vi.spyOn(authStore, 'isLoggedIn', 'get').mockReturnValue(true);
    
    const wrapper = mount(TestComponent);
    
    // 检查单个角色的元素是否显示
    expect(wrapper.find('#single-role').exists()).toBe(true);
    
    // 检查多个角色（满足任一角色）的元素是否显示
    expect(wrapper.find('#any-role').exists()).toBe(true);
    
    // 检查多个角色（必须同时满足所有角色）的元素是否隐藏
    expect(wrapper.find('#all-role').exists()).toBe(false);
  });
  
  it('当用户没有角色时应隐藏元素', () => {
    const authStore = useAuthStore();
    
    // 模拟用户没有角色
    vi.spyOn(authStore, 'hasRole').mockReturnValue(false);
    vi.spyOn(authStore, 'isLoggedIn', 'get').mockReturnValue(true);
    
    const wrapper = mount(TestComponent);
    
    // 检查所有角色元素是否隐藏
    expect(wrapper.find('#single-role').exists()).toBe(false);
    expect(wrapper.find('#any-role').exists()).toBe(false);
    expect(wrapper.find('#all-role').exists()).toBe(false);
  });
});