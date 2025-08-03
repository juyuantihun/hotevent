/**
 * 内存管理工具
 * 用于检测和防止内存泄漏
 */

// 存储需要清理的资源
interface CleanupResource {
  id: string;
  cleanup: () => void;
}

// 全局资源注册表
const resources: Map<string, CleanupResource> = new Map();

/**
 * 注册需要清理的资源
 * @param id 资源ID
 * @param cleanup 清理函数
 * @returns 资源ID
 */
export function registerCleanup(id: string, cleanup: () => void): string {
  resources.set(id, { id, cleanup });
  return id;
}

/**
 * 清理特定资源
 * @param id 资源ID
 * @returns 是否成功清理
 */
export function cleanupResource(id: string): boolean {
  const resource = resources.get(id);
  if (resource) {
    try {
      resource.cleanup();
      resources.delete(id);
      return true;
    } catch (error) {
      console.error(`清理资源 ${id} 时出错:`, error);
      return false;
    }
  }
  return false;
}

/**
 * 清理组件相关的所有资源
 * @param componentId 组件ID前缀
 */
export function cleanupComponentResources(componentId: string): void {
  // 查找所有以componentId开头的资源
  const idsToCleanup: string[] = [];
  
  resources.forEach((resource, id) => {
    if (id.startsWith(componentId)) {
      idsToCleanup.push(id);
    }
  });
  
  // 清理找到的资源
  idsToCleanup.forEach(id => {
    cleanupResource(id);
  });
}

/**
 * 创建组件资源ID
 * @param componentId 组件ID
 * @param resourceName 资源名称
 * @returns 完整的资源ID
 */
export function createResourceId(componentId: string, resourceName: string): string {
  return `${componentId}:${resourceName}`;
}

/**
 * 检测内存使用情况
 * 注意：此功能仅在支持performance.memory的浏览器中可用
 * @returns 内存使用信息
 */
export function checkMemoryUsage(): { usedJSHeapSize?: number, totalJSHeapSize?: number, jsHeapSizeLimit?: number } {
  if (window.performance && (performance as any).memory) {
    const memory = (performance as any).memory;
    return {
      usedJSHeapSize: memory.usedJSHeapSize,
      totalJSHeapSize: memory.totalJSHeapSize,
      jsHeapSizeLimit: memory.jsHeapSizeLimit
    };
  }
  return {};
}

/**
 * 监控内存使用情况
 * @param interval 检查间隔（毫秒）
 * @param threshold 警告阈值（已用内存占总内存的百分比）
 * @returns 停止监控的函数
 */
export function monitorMemoryUsage(interval = 10000, threshold = 80): () => void {
  if (!(window.performance && (performance as any).memory)) {
    console.warn('当前浏览器不支持内存监控');
    return () => {};
  }
  
  const timerId = setInterval(() => {
    const memory = checkMemoryUsage();
    if (memory.usedJSHeapSize && memory.totalJSHeapSize) {
      const usagePercentage = (memory.usedJSHeapSize / memory.totalJSHeapSize) * 100;
      
      // 记录内存使用情况
      console.log(`[内存] 使用率: ${usagePercentage.toFixed(2)}% (${(memory.usedJSHeapSize / 1048576).toFixed(2)} MB / ${(memory.totalJSHeapSize / 1048576).toFixed(2)} MB)`);
      
      // 如果超过阈值，发出警告
      if (usagePercentage > threshold) {
        console.warn(`[内存警告] 内存使用率超过 ${threshold}%，可能存在内存泄漏`);
      }
    }
  }, interval);
  
  // 返回停止监控的函数
  return () => {
    clearInterval(timerId);
  };
}