import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createResettableState } from '@/utils/storeHelpers';
import { reactive } from 'vue';

describe('状态管理辅助函数', () => {
  describe('createResettableState', () => {
    // 测试初始状态
    it('应该使用初始状态函数创建状态', () => {
      const initialState = () => ({ count: 0, name: 'test' });
      const state = createResettableState(initialState);
      
      expect(state.count).toBe(0);
      expect(state.name).toBe('test');
    });
    
    // 测试状态修改
    it('应该允许修改状态', () => {
      const initialState = () => ({ count: 0, name: 'test' });
      const state = createResettableState(initialState);
      
      state.count = 10;
      state.name = 'updated';
      
      expect(state.count).toBe(10);
      expect(state.name).toBe('updated');
    });
    
    // 测试状态重置
    it('应该能够重置状态到初始值', () => {
      const initialState = () => ({ count: 0, name: 'test' });
      const state = createResettableState(initialState);
      
      // 修改状态
      state.count = 10;
      state.name = 'updated';
      
      // 重置状态
      state.resetState();
      
      // 验证状态被重置
      expect(state.count).toBe(0);
      expect(state.name).toBe('test');
    });
    
    // 测试嵌套对象
    it('应该正确处理嵌套对象', () => {
      const initialState = () => ({
        user: {
          id: 1,
          profile: {
            name: 'test',
            age: 25
          }
        },
        settings: {
          theme: 'light',
          notifications: true
        }
      });
      
      const state = createResettableState(initialState);
      
      // 修改嵌套属性
      state.user.profile.name = 'updated';
      state.settings.theme = 'dark';
      
      // 验证修改成功
      expect(state.user.profile.name).toBe('updated');
      expect(state.settings.theme).toBe('dark');
      
      // 重置状态
      state.resetState();
      
      // 验证嵌套属性被重置
      expect(state.user.profile.name).toBe('test');
      expect(state.settings.theme).toBe('light');
    });
    
    // 测试数组
    it('应该正确处理数组', () => {
      const initialState = () => ({
        items: [1, 2, 3],
        users: [
          { id: 1, name: 'User 1' },
          { id: 2, name: 'User 2' }
        ]
      });
      
      const state = createResettableState(initialState);
      
      // 修改数组
      state.items.push(4);
      state.users.push({ id: 3, name: 'User 3' });
      state.users[0].name = 'Updated User 1';
      
      // 验证修改成功
      expect(state.items).toEqual([1, 2, 3, 4]);
      expect(state.users.length).toBe(3);
      expect(state.users[0].name).toBe('Updated User 1');
      
      // 重置状态
      state.resetState();
      
      // 验证数组被重置
      expect(state.items).toEqual([1, 2, 3]);
      expect(state.users.length).toBe(2);
      expect(state.users[0].name).toBe('User 1');
    });
    
    // 测试与Vue的reactive集成
    it('应该与Vue的reactive正确集成', () => {
      const initialState = () => ({ count: 0, name: 'test' });
      const state = createResettableState(initialState);
      
      // 创建响应式状态
      const reactiveState = reactive(state);
      
      // 修改状态
      reactiveState.count = 10;
      reactiveState.name = 'updated';
      
      // 验证原始状态和响应式状态都被更新
      expect(state.count).toBe(10);
      expect(state.name).toBe('updated');
      expect(reactiveState.count).toBe(10);
      expect(reactiveState.name).toBe('updated');
      
      // 重置状态
      reactiveState.resetState();
      
      // 验证原始状态和响应式状态都被重置
      expect(state.count).toBe(0);
      expect(state.name).toBe('test');
      expect(reactiveState.count).toBe(0);
      expect(reactiveState.name).toBe('test');
    });
    
    // 测试初始状态函数被多次调用
    it('每次重置时应该调用初始状态函数', () => {
      const initialState = vi.fn(() => ({ count: 0, name: 'test' }));
      const state = createResettableState(initialState);
      
      // 初始化时应该调用一次
      expect(initialState).toHaveBeenCalledTimes(1);
      
      // 重置状态
      state.resetState();
      
      // 应该再次调用初始状态函数
      expect(initialState).toHaveBeenCalledTimes(2);
      
      // 再次重置
      state.resetState();
      
      // 应该第三次调用初始状态函数
      expect(initialState).toHaveBeenCalledTimes(3);
    });
    
    // 测试初始状态函数返回不同的对象
    it('应该使用初始状态函数的最新返回值', () => {
      let counter = 0;
      const initialState = () => ({ count: counter++, name: `test-${counter}` });
      const state = createResettableState(initialState);
      
      // 初始值
      expect(state.count).toBe(0);
      expect(state.name).toBe('test-1');
      
      // 重置状态
      state.resetState();
      
      // 应该使用新的初始值
      expect(state.count).toBe(1);
      expect(state.name).toBe('test-2');
      
      // 再次重置
      state.resetState();
      
      // 应该使用更新的初始值
      expect(state.count).toBe(2);
      expect(state.name).toBe('test-3');
    });
  });
});