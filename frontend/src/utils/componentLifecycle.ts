/**
 * 组件生命周期管理工具
 * 用于优化组件的创建和销毁过程
 */
import { onMounted, onBeforeUnmount, onActivated, onDeactivated } from 'vue'
import { createResourceId, registerCleanup, cleanupComponentResources } from './memoryManager'

/**
 * 使用组件生命周期管理
 * @param componentId 组件唯一标识
 * @returns 生命周期管理工具
 */
export function useComponentLifecycle(componentId: string) {
  // 在组件挂载时执行
  onMounted(() => {
    console.log(`[组件] ${componentId} 已挂载`);
  });
  
  // 在组件卸载前执行
  onBeforeUnmount(() => {
    console.log(`[组件] ${componentId} 即将卸载，清理资源`);
    // 清理组件相关的所有资源
    cleanupComponentResources(componentId);
  });
  
  // 如果使用keep-alive，处理激活和停用事件
  onActivated(() => {
    console.log(`[组件] ${componentId} 已激活`);
  });
  
  onDeactivated(() => {
    console.log(`[组件] ${componentId} 已停用`);
  });
  
  /**
   * 注册需要在组件卸载时清理的资源
   * @param resourceName 资源名称
   * @param cleanup 清理函数
   */
  const registerComponentCleanup = (resourceName: string, cleanup: () => void) => {
    const resourceId = createResourceId(componentId, resourceName);
    registerCleanup(resourceId, cleanup);
    return resourceId;
  };
  
  /**
   * 注册事件监听器，并确保在组件卸载时移除
   * @param element DOM元素或window
   * @param eventName 事件名称
   * @param handler 事件处理函数
   * @param options 事件选项
   */
  const registerEventListener = (
    element: Window | Document | HTMLElement,
    eventName: string,
    handler: EventListenerOrEventListenerObject,
    options?: boolean | AddEventListenerOptions
  ) => {
    // 添加事件监听器
    element.addEventListener(eventName, handler, options);
    
    // 注册清理函数
    const resourceName = `event:${eventName}`;
    registerComponentCleanup(resourceName, () => {
      element.removeEventListener(eventName, handler, options);
    });
  };
  
  /**
   * 注册定时器，并确保在组件卸载时清除
   * @param callback 回调函数
   * @param delay 延迟时间（毫秒）
   * @returns 定时器ID
   */
  const registerInterval = (callback: () => void, delay: number) => {
    const timerId = setInterval(callback, delay);
    const resourceName = `interval:${timerId}`;
    
    registerComponentCleanup(resourceName, () => {
      clearInterval(timerId);
    });
    
    return timerId;
  };
  
  /**
   * 注册延时任务，并确保在组件卸载时清除
   * @param callback 回调函数
   * @param delay 延迟时间（毫秒）
   * @returns 定时器ID
   */
  const registerTimeout = (callback: () => void, delay: number) => {
    const timerId = setTimeout(callback, delay);
    const resourceName = `timeout:${timerId}`;
    
    registerComponentCleanup(resourceName, () => {
      clearTimeout(timerId);
    });
    
    return timerId;
  };
  
  /**
   * 注册动画帧请求，并确保在组件卸载时取消
   * @param callback 回调函数
   * @returns 请求ID
   */
  const registerAnimationFrame = (callback: FrameRequestCallback) => {
    const requestId = requestAnimationFrame(callback);
    const resourceName = `animationFrame:${requestId}`;
    
    registerComponentCleanup(resourceName, () => {
      cancelAnimationFrame(requestId);
    });
    
    return requestId;
  };
  
  return {
    registerComponentCleanup,
    registerEventListener,
    registerInterval,
    registerTimeout,
    registerAnimationFrame
  };
}