/**
 * 性能监控工具
 * 用于收集和分析应用性能指标
 */

// 性能指标类型
interface PerformanceMetrics {
  // 首次内容绘制时间
  fcp?: number;
  // 最大内容绘制时间
  lcp?: number;
  // 首次输入延迟
  fid?: number;
  // 累积布局偏移
  cls?: number;
  // 首次可交互时间
  tti?: number;
  // 总阻塞时间
  tbt?: number;
  // 自定义指标
  custom: Record<string, number>;
}

// 全局性能指标对象
const metrics: PerformanceMetrics = {
  custom: {}
};

/**
 * 初始化性能监控
 */
export function initPerformanceMonitoring(): void {
  // 确保只在浏览器环境中运行
  if (typeof window === 'undefined' || typeof performance === 'undefined') {
    return;
  }

  // 记录导航开始时间
  const navigationStart = performance.timing?.navigationStart || performance.now();
  
  // 监听首次内容绘制
  const observer = new PerformanceObserver((entryList) => {
    for (const entry of entryList.getEntries()) {
      if (entry.name === 'first-contentful-paint') {
        metrics.fcp = entry.startTime;
        console.log(`[性能] 首次内容绘制 (FCP): ${Math.round(metrics.fcp)}ms`);
      }
      if (entry.name === 'largest-contentful-paint') {
        metrics.lcp = entry.startTime;
        console.log(`[性能] 最大内容绘制 (LCP): ${Math.round(metrics.lcp)}ms`);
      }
    }
  });
  
  try {
    observer.observe({ type: 'paint', buffered: true });
  } catch (e) {
    console.error('性能观察API不可用', e);
  }
  
  // 监听首次输入延迟
  const fidObserver = new PerformanceObserver((entryList) => {
    for (const entry of entryList.getEntries()) {
      if (entry.name === 'first-input') {
        metrics.fid = entry.processingStart - entry.startTime;
        console.log(`[性能] 首次输入延迟 (FID): ${Math.round(metrics.fid)}ms`);
      }
    }
  });
  
  try {
    fidObserver.observe({ type: 'first-input', buffered: true });
  } catch (e) {
    console.error('首次输入延迟监控不可用', e);
  }
  
  // 页面加载完成后记录指标
  window.addEventListener('load', () => {
    setTimeout(() => {
      // 记录页面加载时间
      const loadTime = performance.now() - navigationStart;
      metrics.custom.pageLoad = loadTime;
      console.log(`[性能] 页面加载时间: ${Math.round(loadTime)}ms`);
      
      // 记录资源加载情况
      const resources = performance.getEntriesByType('resource');
      const jsResources = resources.filter(r => r.name.endsWith('.js'));
      const cssResources = resources.filter(r => r.name.endsWith('.css'));
      
      const jsLoadTime = jsResources.reduce((total, r) => total + r.duration, 0);
      const cssLoadTime = cssResources.reduce((total, r) => total + r.duration, 0);
      
      metrics.custom.jsLoadTime = jsLoadTime;
      metrics.custom.cssLoadTime = cssLoadTime;
      
      console.log(`[性能] JS资源加载时间: ${Math.round(jsLoadTime)}ms`);
      console.log(`[性能] CSS资源加载时间: ${Math.round(cssLoadTime)}ms`);
      console.log(`[性能] 资源总数: ${resources.length} (JS: ${jsResources.length}, CSS: ${cssResources.length})`);
    }, 0);
  });
}

/**
 * 记录自定义性能指标
 * @param name 指标名称
 * @param startTime 开始时间
 */
export function recordMetric(name: string, startTime: number): void {
  const duration = performance.now() - startTime;
  metrics.custom[name] = duration;
  console.log(`[性能] ${name}: ${Math.round(duration)}ms`);
}

/**
 * 获取所有收集的性能指标
 */
export function getMetrics(): PerformanceMetrics {
  return { ...metrics };
}

/**
 * 清除性能条目
 */
export function clearPerformanceEntries(): void {
  if (typeof performance !== 'undefined' && typeof performance.clearResourceTimings === 'function') {
    performance.clearResourceTimings();
  }
}