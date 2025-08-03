/**
 * 资源优化服务
 * 用于优化静态资源的加载和使用
 */
import { 
  ResourceType, 
  ResourcePriority, 
  ResourceConfig,
  getPreloadResources,
  getPrefetchResources,
  preconnectDomains
} from '@/config/resourceOptimization'

/**
 * 添加资源提示
 * @param resource 资源配置
 * @param hint 提示类型 ('preload' | 'prefetch' | 'preconnect')
 */
function addResourceHint(resource: ResourceConfig | string, hint: 'preload' | 'prefetch' | 'preconnect'): HTMLLinkElement {
  const link = document.createElement('link');
  link.rel = hint;
  
  if (typeof resource === 'string') {
    // 如果是字符串，则为预连接域名
    link.href = resource;
    if (hint === 'preconnect') {
      link.crossOrigin = 'anonymous';
    }
  } else {
    // 如果是资源配置对象
    link.href = resource.url;
    
    if (hint === 'preload') {
      // 设置资源类型
      switch (resource.type) {
        case ResourceType.SCRIPT:
          link.as = 'script';
          break;
        case ResourceType.STYLE:
          link.as = 'style';
          break;
        case ResourceType.IMAGE:
          link.as = 'image';
          break;
        case ResourceType.FONT:
          link.as = 'font';
          break;
        case ResourceType.FETCH:
          link.as = 'fetch';
          break;
      }
      
      // 设置跨域属性
      if (resource.crossOrigin) {
        link.crossOrigin = 'anonymous';
      }
      
      // 设置媒体查询
      if (resource.media) {
        link.media = resource.media;
      }
    }
  }
  
  // 添加到文档头部
  document.head.appendChild(link);
  return link;
}

/**
 * 预加载关键资源
 */
export function preloadCriticalResources(): void {
  const resources = getPreloadResources();
  
  // 按优先级排序
  resources.sort((a, b) => {
    const priorityOrder = {
      [ResourcePriority.CRITICAL]: 0,
      [ResourcePriority.HIGH]: 1,
      [ResourcePriority.MEDIUM]: 2,
      [ResourcePriority.LOW]: 3,
      [ResourcePriority.OPTIONAL]: 4
    };
    
    return priorityOrder[a.priority] - priorityOrder[b.priority];
  });
  
  // 添加预加载提示
  resources.forEach(resource => {
    addResourceHint(resource, 'preload');
  });
}

/**
 * 预获取非关键资源
 */
export function prefetchNonCriticalResources(): void {
  // 使用requestIdleCallback在浏览器空闲时预获取资源
  if ('requestIdleCallback' in window) {
    window.requestIdleCallback(() => {
      const resources = getPrefetchResources();
      resources.forEach(resource => {
        addResourceHint(resource, 'prefetch');
      });
    }, { timeout: 2000 });
  } else {
    // 降级处理
    setTimeout(() => {
      const resources = getPrefetchResources();
      resources.forEach(resource => {
        addResourceHint(resource, 'prefetch');
      });
    }, 1000);
  }
}

/**
 * 预连接到关键域名
 */
export function preconnectToDomains(): void {
  preconnectDomains.forEach(domain => {
    // 添加DNS预解析
    const dns = document.createElement('link');
    dns.rel = 'dns-prefetch';
    dns.href = domain;
    document.head.appendChild(dns);
    
    // 添加预连接
    addResourceHint(domain, 'preconnect');
  });
}

/**
 * 动态加载脚本
 * @param url 脚本URL
 * @param async 是否异步加载
 * @param defer 是否延迟加载
 * @returns Promise
 */
export function loadScript(url: string, async: boolean = true, defer: boolean = false): Promise<void> {
  return new Promise((resolve, reject) => {
    // 检查脚本是否已加载
    const existingScript = document.querySelector(`script[src="${url}"]`);
    if (existingScript) {
      resolve();
      return;
    }
    
    // 创建脚本元素
    const script = document.createElement('script');
    script.src = url;
    script.async = async;
    script.defer = defer;
    
    // 设置加载事件
    script.onload = () => resolve();
    script.onerror = () => reject(new Error(`Failed to load script: ${url}`));
    
    // 添加到文档
    document.head.appendChild(script);
  });
}

/**
 * 动态加载样式
 * @param url 样式URL
 * @returns Promise
 */
export function loadStyle(url: string): Promise<void> {
  return new Promise((resolve, reject) => {
    // 检查样式是否已加载
    const existingLink = document.querySelector(`link[href="${url}"]`);
    if (existingLink) {
      resolve();
      return;
    }
    
    // 创建链接元素
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = url;
    
    // 设置加载事件
    link.onload = () => resolve();
    link.onerror = () => reject(new Error(`Failed to load style: ${url}`));
    
    // 添加到文档
    document.head.appendChild(link);
  });
}

/**
 * 预加载图片
 * @param url 图片URL
 * @returns Promise
 */
export function preloadImage(url: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.src = url;
    img.onload = () => resolve(img);
    img.onerror = () => reject(new Error(`Failed to load image: ${url}`));
  });
}

/**
 * 批量预加载图片
 * @param urls 图片URL数组
 * @returns Promise
 */
export function preloadImages(urls: string[]): Promise<HTMLImageElement[]> {
  return Promise.all(urls.map(url => preloadImage(url)));
}

/**
 * 清理资源提示
 * 在不需要时移除资源提示，减少内存占用
 */
export function cleanupResourceHints(): void {
  // 移除所有预加载和预获取提示
  document.querySelectorAll('link[rel="preload"], link[rel="prefetch"]').forEach(el => {
    el.remove();
  });
}

/**
 * 初始化资源优化
 */
export function initResourceOptimization(): void {
  // 预连接到关键域名
  preconnectToDomains();
  
  // 预加载关键资源
  preloadCriticalResources();
  
  // 在浏览器空闲时预获取非关键资源
  setTimeout(() => {
    prefetchNonCriticalResources();
  }, 1000);
  
  // 在页面完全加载后清理资源提示
  window.addEventListener('load', () => {
    setTimeout(() => {
      cleanupResourceHints();
    }, 5000);
  });
}