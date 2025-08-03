import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { enableStoreDebugger, disableStoreDebugger } from '@/utils/storeDebugger';
import { createPinia, defineStore, setActivePinia } from 'pinia';

describe('状态管理调试工具', () => {
  // 模拟控制台方法
  beforeEach(() => {
    vi.spyOn(console, 'log').mockImplementation(() => {});
    vi.spyOn(console, 'group').mockImplementation(() => {});
    vi.spyOn(console, 'groupEnd').mockImplementation(() => {});
    
    // 设置Pinia
    setActivePinia(createPinia());
  });
  
  afterEach(() => {
    vi.restoreAllMocks();
  });
  
  // 创建测试store
  const useTestStore = defineStore('test', {
    state: () => ({
      count: 0,
      name: 'test'
    }),
    actions: {
      increment() {
        this.count++;
      },
      setName(name: string) {
        this.name = name;
      }
    }
  });
  
  it('enableStoreDebugger应该启用状态变更日志', () => {
    // 启用调试器
    enableStoreDebugger();
    
    // 创建store实例
    const store = useTestStore();
    
    // 修改状态
    store.increment();
    store.setName('updated');
    
    // 验证控制台日志被调用
    expect(console.group).toHaveBeenCalled();
    expect(console.log).toHaveBeenCalled();
    expect(console.groupEnd).toHaveBeenCalled();
  });
  
  it('disableStoreDebugger应该禁用状态变更日志', () => {
    // 先启用调试器
    enableStoreDebugger();
    
    // 然后禁用调试器
    disableStoreDebugger();
    
    // 创建store实例
    const store = useTestStore();
    
    // 清除之前的调用记录
    vi.clearAllMocks();
    
    // 修改状态
    store.increment();
    store.setName('updated');
    
    // 验证控制台日志没有被调用
    expect(console.group).not.toHaveBeenCalled();
    expect(console.log).not.toHaveBeenCalled();
    expect(console.groupEnd).not.toHaveBeenCalled();
  });
  
  it('enableStoreDebugger应该可以配置过滤器', () => {
    // 启用调试器，只记录test store的变更
    enableStoreDebugger({
      storeFilter: (storeName) => storeName === 'test'
    });
    
    // 创建test store实例
    const testStore = useTestStore();
    
    // 创建另一个store
    const useOtherStore = defineStore('other', {
      state: () => ({ value: 0 }),
      actions: {
        update() {
          this.value++;
        }
      }
    });
    const otherStore = useOtherStore();
    
    // 清除之前的调用记录
    vi.clearAllMocks();
    
    // 修改test store状态
    testStore.increment();
    
    // 验证控制台日志被调用
    expect(console.group).toHaveBeenCalled();
    expect(console.log).toHaveBeenCalled();
    
    // 清除调用记录
    vi.clearAllMocks();
    
    // 修改other store状态
    otherStore.update();
    
    // 验证控制台日志没有被调用
    expect(console.group).not.toHaveBeenCalled();
    expect(console.log).not.toHaveBeenCalled();
  });
  
  it('enableStoreDebugger应该可以配置操作过滤器', () => {
    // 启用调试器，只记录increment操作
    enableStoreDebugger({
      actionFilter: (actionName) => actionName === 'increment'
    });
    
    // 创建store实例
    const store = useTestStore();
    
    // 清除之前的调用记录
    vi.clearAllMocks();
    
    // 执行increment操作
    store.increment();
    
    // 验证控制台日志被调用
    expect(console.group).toHaveBeenCalled();
    expect(console.log).toHaveBeenCalled();
    
    // 清除调用记录
    vi.clearAllMocks();
    
    // 执行setName操作
    store.setName('updated');
    
    // 验证控制台日志没有被调用
    expect(console.group).not.toHaveBeenCalled();
    expect(console.log).not.toHaveBeenCalled();
  });
  
  it('enableStoreDebugger应该可以配置状态过滤器', () => {
    // 启用调试器，只记录count属性的变更
    enableStoreDebugger({
      stateFilter: (key) => key === 'count'
    });
    
    // 创建store实例
    const store = useTestStore();
    
    // 清除之前的调用记录
    vi.clearAllMocks();
    
    // 修改count状态
    store.increment();
    
    // 验证控制台日志被调用
    expect(console.group).toHaveBeenCalled();
    expect(console.log).toHaveBeenCalled();
    
    // 清除调用记录
    vi.clearAllMocks();
    
    // 修改name状态
    store.setName('updated');
    
    // 验证控制台日志没有被调用
    expect(console.group).not.toHaveBeenCalled();
    expect(console.log).not.toHaveBeenCalled();
  });
  
  it('enableStoreDebugger应该可以配置自定义日志格式', () => {
    // 创建自定义日志函数
    const customLogger = vi.fn();
    
    // 启用调试器，使用自定义日志函数
    enableStoreDebugger({
      logger: customLogger
    });
    
    // 创建store实例
    const store = useTestStore();
    
    // 修改状态
    store.increment();
    
    // 验证自定义日志函数被调用
    expect(customLogger).toHaveBeenCalled();
    expect(customLogger).toHaveBeenCalledWith(
      expect.objectContaining({
        storeName: 'test',
        actionName: 'increment',
        newState: expect.objectContaining({ count: 1 }),
        oldState: expect.objectContaining({ count: 0 })
      })
    );
  });
});