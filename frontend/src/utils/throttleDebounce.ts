/**
 * 防抖和节流工具
 * 用于优化频繁触发的事件处理
 */

/**
 * 防抖函数
 * 将多次连续调用合并为一次，在最后一次调用后等待指定时间执行
 * 常用于处理搜索输入、窗口调整等频繁触发的事件
 * 
 * @template T 函数类型
 * @param {T} fn 需要防抖的函数
 * @param {number} delay 延迟时间（毫秒），默认300ms
 * @param {boolean} immediate 是否在延迟开始前调用函数，默认false
 * @returns {(...args: Parameters<T>) => void} 防抖处理后的函数
 * 
 * @example
 * // 基本用法
 * const debouncedSearch = debounce(searchFunction, 500);
 * 
 * // 立即执行模式
 * const debouncedHandler = debounce(handler, 300, true);
 */
export function debounce<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300,
  immediate: boolean = false
): (...args: Parameters<T>) => void {
  let timer: number | null = null;
  let isInvoked = false;
  
  return function(this: any, ...args: Parameters<T>): void {
    const context = this;
    
    // 如果已经设置了定时器，则清除
    if (timer !== null) {
      window.clearTimeout(timer);
      timer = null;
    }
    
    // 是否立即执行
    if (immediate && !isInvoked) {
      fn.apply(context, args);
      isInvoked = true;
    }
    
    // 设置新的定时器
    timer = window.setTimeout(() => {
      if (!immediate) {
        fn.apply(context, args);
      }
      isInvoked = false;
      timer = null;
    }, delay);
  };
}

/**
 * 节流函数
 * 限制函数在一定时间内只能执行一次
 * 常用于处理滚动、拖拽、调整大小等持续触发的事件
 * 
 * @template T 函数类型
 * @param {T} fn 需要节流的函数
 * @param {number} limit 时间限制（毫秒），默认300ms
 * @param {boolean} trailing 是否在结束后执行一次，默认false
 * @returns {(...args: Parameters<T>) => void} 节流处理后的函数
 * 
 * @example
 * // 基本用法
 * const throttledScroll = throttle(scrollHandler, 200);
 * 
 * // 带尾随执行的用法
 * const throttledResize = throttle(resizeHandler, 200, true);
 */
export function throttle<T extends (...args: any[]) => any>(
  fn: T,
  limit: number = 300,
  trailing: boolean = false
): (...args: Parameters<T>) => void {
  let lastFunc: number | null = null;
  let lastRan: number = 0;
  let lastArgs: Parameters<T> | null = null;
  let lastThis: any = null;
  
  return function(this: any, ...args: Parameters<T>): void {
    const context = this;
    const now = Date.now();
    
    // 如果是第一次调用或者已经超过了限制时间
    if (!lastRan || now - lastRan >= limit) {
      if (lastFunc !== null) {
        window.clearTimeout(lastFunc);
        lastFunc = null;
      }
      
      fn.apply(context, args);
      lastRan = now;
    } else if (trailing) {
      // 保存最后一次调用的参数和上下文
      lastArgs = args;
      lastThis = context;
      
      // 如果已经设置了定时器，则不再设置
      if (lastFunc === null) {
        lastFunc = window.setTimeout(() => {
          if (lastArgs !== null) {
            fn.apply(lastThis, lastArgs);
          }
          lastRan = Date.now();
          lastFunc = null;
          lastArgs = null;
          lastThis = null;
        }, limit - (now - lastRan));
      }
    }
  };
}

/**
 * 创建一个可取消的防抖函数
 * 返回一个对象，包含执行函数和取消方法，便于在组件生命周期中管理
 * 
 * @template T 函数类型
 * @param {T} fn 需要防抖的函数
 * @param {number} delay 延迟时间（毫秒），默认300ms
 * @returns {{ execute: (...args: Parameters<T>) => void; cancel: () => void }} 包含执行函数和取消方法的对象
 * 
 * @example
 * // 在组件中使用
 * const searchDebounce = createCancelableDebounce(searchApi, 500);
 * 
 * // 执行搜索
 * searchDebounce.execute('关键词');
 * 
 * // 取消未执行的搜索
 * searchDebounce.cancel();
 * 
 * // 在组件卸载时清理
 * onBeforeUnmount(() => {
 *   searchDebounce.cancel();
 * });
 */
export function createCancelableDebounce<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
): { 
  execute: (...args: Parameters<T>) => void;
  cancel: () => void;
} {
  let timer: number | null = null;
  
  /**
   * 执行防抖函数
   * 每次调用会重置定时器，延迟指定时间后执行原函数
   */
  const execute = function(this: any, ...args: Parameters<T>): void {
    const context = this;
    
    if (timer !== null) {
      window.clearTimeout(timer);
      timer = null;
    }
    
    timer = window.setTimeout(() => {
      fn.apply(context, args);
      timer = null;
    }, delay);
  };
  
  /**
   * 取消防抖函数的执行
   * 清除定时器，阻止原函数的执行
   */
  const cancel = (): void => {
    if (timer !== null) {
      window.clearTimeout(timer);
      timer = null;
    }
  };
  
  return { execute, cancel };
}