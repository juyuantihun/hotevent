import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { useAuthStore } from '@/store/modules/auth';
import { useAppStore } from '@/store/modules/app';
import { useEventStore } from '@/store/modules/event';
import { useDictionaryStore } from '@/store/modules/dictionary';

// 模拟API
vi.mock('@/api/auth', () => ({
  login: vi.fn().mockResolvedValue({
    token: 'mock-token',
    user: {
      id: '1',
      username: 'admin',
      name: '管理员',
      roles: ['admin'],
      permissions: ['user:create', 'user:update']
    }
  }),
  logout: vi.fn().mockResolvedValue({}),
  getUserInfo: vi.fn().mockResolvedValue({
    id: '1',
    username: 'admin',
    name: '管理员',
    roles: ['admin'],
    permissions: ['user:create', 'user:update']
  })
}));

vi.mock('@/api/event', () => ({
  getEventList: vi.fn().mockResolvedValue({
    content: [
      { id: '1', title: '事件1' },
      { id: '2', title: '事件2' }
    ],
    totalElements: 2
  }),
  getEventDetail: vi.fn().mockImplementation((id) => {
    return Promise.resolve({
      id,
      title: `事件${id}`,
      description: `事件${id}的描述`
    });
  })
}));

vi.mock('@/api/dictionary', () => ({
  getDictionaryList: vi.fn().mockResolvedValue([
    { code: 'event_type', name: '事件类型', items: [
      { code: 'political', name: '政治' },
      { code: 'economic', name: '经济' }
    ]},
    { code: 'event_status', name: '事件状态', items: [
      { code: 'active', name: '活跃' },
      { code: 'closed', name: '已关闭' }
    ]}
  ])
}));

describe('状态管理集成测试', () => {
  beforeEach(() => {
    // 设置Pinia
    setActivePinia(createPinia());
    
    // 清除localStorage
    localStorage.clear();
    
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
  });
  
  it('认证状态应该正确更新并持久化', async () => {
    const authStore = useAuthStore();
    
    // 初始状态
    expect(authStore.isLoggedIn).toBe(false);
    expect(authStore.token).toBe('');
    expect(authStore.userInfo).toBeNull();
    
    // 登录
    await authStore.loginAction({
      username: 'admin',
      password: 'password'
    });
    
    // 验证状态更新
    expect(authStore.isLoggedIn).toBe(true);
    expect(authStore.token).toBe('mock-token');
    expect(authStore.userInfo).toBeTruthy();
    expect(authStore.userInfo.username).toBe('admin');
    expect(authStore.roles).toContain('admin');
    expect(authStore.permissions).toContain('user:create');
    
    // 验证令牌持久化
    expect(localStorage.getItem('token')).toBe('mock-token');
    
    // 创建新的store实例，模拟页面刷新
    setActivePinia(createPinia());
    const newAuthStore = useAuthStore();
    
    // 验证状态从localStorage恢复
    expect(newAuthStore.token).toBe('mock-token');
    
    // 登出
    await newAuthStore.logoutAction();
    
    // 验证状态清除
    expect(newAuthStore.isLoggedIn).toBe(false);
    expect(newAuthStore.token).toBe('');
    expect(newAuthStore.userInfo).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
  });
  
  it('应用状态应该正确管理全局设置', async () => {
    const appStore = useAppStore();
    
    // 初始状态
    expect(appStore.sidebar.opened).toBe(true);
    expect(appStore.size).toBe('default');
    expect(appStore.theme).toBe('light');
    
    // 更新状态
    appStore.toggleSidebar();
    appStore.setSize('small');
    appStore.setTheme('dark');
    
    // 验证状态更新
    expect(appStore.sidebar.opened).toBe(false);
    expect(appStore.size).toBe('small');
    expect(appStore.theme).toBe('dark');
    
    // 创建新的store实例，模拟页面刷新
    setActivePinia(createPinia());
    const newAppStore = useAppStore();
    
    // 验证状态从localStorage恢复
    expect(newAppStore.sidebar.opened).toBe(false);
    expect(newAppStore.size).toBe('small');
    expect(newAppStore.theme).toBe('dark');
    
    // 重置状态
    newAppStore.resetState();
    
    // 验证状态重置
    expect(newAppStore.sidebar.opened).toBe(true);
    expect(newAppStore.size).toBe('default');
    expect(newAppStore.theme).toBe('light');
  });
  
  it('事件状态应该正确加载和缓存数据', async () => {
    const eventStore = useEventStore();
    
    // 初始状态
    expect(eventStore.eventList).toEqual([]);
    expect(eventStore.currentEvent).toBeNull();
    
    // 加载事件列表
    await eventStore.getEventList();
    
    // 验证状态更新
    expect(eventStore.eventList.length).toBe(2);
    expect(eventStore.eventList[0].title).toBe('事件1');
    
    // 加载事件详情
    await eventStore.getEventDetail('1');
    
    // 验证状态更新
    expect(eventStore.currentEvent).toBeTruthy();
    expect(eventStore.currentEvent.id).toBe('1');
    expect(eventStore.currentEvent.title).toBe('事件1');
    
    // 验证缓存机制
    const { getEventDetail } = require('@/api/event');
    vi.clearAllMocks();
    
    // 再次加载相同的事件详情
    await eventStore.getEventDetail('1');
    
    // 验证API没有被再次调用
    expect(getEventDetail).not.toHaveBeenCalled();
    
    // 加载不同的事件详情
    await eventStore.getEventDetail('2');
    
    // 验证API被调用
    expect(getEventDetail).toHaveBeenCalledWith('2');
    expect(eventStore.currentEvent.id).toBe('2');
  });
  
  it('字典状态应该正确加载和使用', async () => {
    const dictionaryStore = useDictionaryStore();
    
    // 初始状态
    expect(dictionaryStore.dictionaries).toEqual({});
    
    // 加载字典
    await dictionaryStore.loadDictionaries();
    
    // 验证状态更新
    expect(Object.keys(dictionaryStore.dictionaries).length).toBe(2);
    expect(dictionaryStore.dictionaries.event_type).toBeTruthy();
    expect(dictionaryStore.dictionaries.event_status).toBeTruthy();
    
    // 使用字典项
    const eventType = dictionaryStore.getDictionary('event_type');
    expect(eventType.length).toBe(2);
    expect(eventType[0].code).toBe('political');
    
    // 获取字典项名称
    const typeName = dictionaryStore.getDictLabel('event_type', 'economic');
    expect(typeName).toBe('经济');
    
    // 获取不存在的字典项
    const unknownName = dictionaryStore.getDictLabel('event_type', 'unknown');
    expect(unknownName).toBe('unknown');
  });
  
  it('多个状态模块应该能够协同工作', async () => {
    // 创建一个使用多个状态模块的测试组件
    const TestComponent = {
      template: `
        <div>
          <div v-if="isLoggedIn">
            <div class="user-info">{{ userInfo.name }}</div>
            <div class="theme">当前主题: {{ theme }}</div>
            <button @click="loadEvents">加载事件</button>
            <div v-if="eventList.length > 0" class="event-list">
              <div v-for="event in eventList" :key="event.id" class="event-item">
                {{ event.title }} - {{ getEventTypeName(event.type) }}
              </div>
            </div>
          </div>
          <div v-else>
            <button @click="login">登录</button>
          </div>
        </div>
      `,
      computed: {
        isLoggedIn() {
          return useAuthStore().isLoggedIn;
        },
        userInfo() {
          return useAuthStore().userInfo || {};
        },
        theme() {
          return useAppStore().theme;
        },
        eventList() {
          return useEventStore().eventList;
        }
      },
      methods: {
        async login() {
          await useAuthStore().loginAction({
            username: 'admin',
            password: 'password'
          });
        },
        async loadEvents() {
          await useEventStore().getEventList();
        },
        getEventTypeName(type) {
          return useDictionaryStore().getDictLabel('event_type', type) || '未知';
        }
      },
      async mounted() {
        // 加载字典
        await useDictionaryStore().loadDictionaries();
      }
    };
    
    // 挂载测试组件
    const wrapper = mount(TestComponent);
    
    // 初始状态应该显示登录按钮
    expect(wrapper.find('button').text()).toBe('登录');
    
    // 点击登录按钮
    await wrapper.find('button').trigger('click');
    
    // 验证登录后显示用户信息
    await wrapper.vm.$nextTick();
    expect(wrapper.find('.user-info').text()).toBe('管理员');
    
    // 验证显示主题
    expect(wrapper.find('.theme').text()).toContain('当前主题');
    
    // 点击加载事件按钮
    await wrapper.find('button').trigger('click');
    
    // 验证显示事件列表
    await wrapper.vm.$nextTick();
    expect(wrapper.find('.event-list').exists()).toBe(true);
    expect(wrapper.findAll('.event-item').length).toBe(2);
  });
});