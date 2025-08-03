/**
 * 资源优化配置
 * 用于管理静态资源的加载和优化
 */

// 资源类型
export enum ResourceType {
  SCRIPT = 'script',
  STYLE = 'style',
  IMAGE = 'image',
  FONT = 'font',
  FETCH = 'fetch'
}

// 资源优先级
export enum ResourcePriority {
  CRITICAL = 'critical', // 关键资源，立即加载
  HIGH = 'high',         // 高优先级，尽快加载
  MEDIUM = 'medium',     // 中等优先级，空闲时加载
  LOW = 'low',           // 低优先级，延迟加载
  OPTIONAL = 'optional'  // 可选资源，仅在需要时加载
}

// 资源配置
export interface ResourceConfig {
  url: string;
  type: ResourceType;
  priority: ResourcePriority;
  preload?: boolean;     // 是否预加载
  prefetch?: boolean;    // 是否预获取
  preconnect?: boolean;  // 是否预连接
  crossOrigin?: boolean; // 是否跨域
  async?: boolean;       // 脚本是否异步加载
  defer?: boolean;       // 脚本是否延迟加载
  media?: string;        // 媒体查询
}

// 关键资源（应用启动必需的资源）
export const criticalResources: ResourceConfig[] = [
  // 主样式文件
  {
    url: '/assets/index.css',
    type: ResourceType.STYLE,
    priority: ResourcePriority.CRITICAL,
    preload: true
  },
  // 主脚本文件
  {
    url: '/assets/index.js',
    type: ResourceType.SCRIPT,
    priority: ResourcePriority.CRITICAL,
    preload: true,
    async: true
  },
  // Element Plus 图标字体
  {
    url: '/assets/fonts/element-icons.woff',
    type: ResourceType.FONT,
    priority: ResourcePriority.CRITICAL,
    preload: true,
    crossOrigin: true
  }
];

// 高优先级资源（首屏渲染需要的资源）
export const highPriorityResources: ResourceConfig[] = [
  // 应用图标
  {
    url: '/favicon.ico',
    type: ResourceType.IMAGE,
    priority: ResourcePriority.HIGH,
    preload: true
  },
  // 登录页面背景
  {
    url: '/assets/images/login-bg.jpg',
    type: ResourceType.IMAGE,
    priority: ResourcePriority.HIGH,
    preload: true
  }
];

// 中等优先级资源（非首屏但常用的资源）
export const mediumPriorityResources: ResourceConfig[] = [
  // 仪表板图表库
  {
    url: '/assets/charts.js',
    type: ResourceType.SCRIPT,
    priority: ResourcePriority.MEDIUM,
    prefetch: true,
    async: true
  },
  // 常用图标
  {
    url: '/assets/images/icons-sprite.svg',
    type: ResourceType.IMAGE,
    priority: ResourcePriority.MEDIUM,
    prefetch: true
  }
];

// 低优先级资源（不常用的资源）
export const lowPriorityResources: ResourceConfig[] = [
  // PDF导出库
  {
    url: '/assets/pdf-export.js',
    type: ResourceType.SCRIPT,
    priority: ResourcePriority.LOW,
    prefetch: true,
    async: true
  },
  // 高级编辑器
  {
    url: '/assets/editor.js',
    type: ResourceType.SCRIPT,
    priority: ResourcePriority.LOW,
    prefetch: true,
    async: true
  }
];

// 需要预连接的域名
export const preconnectDomains: string[] = [
  // API服务器
  window.location.origin,
  // CDN服务器
  'https://cdn.example.com',
  // 字体服务器
  'https://fonts.googleapis.com'
];

// 第三方库优化配置
export interface LibraryOptimizationConfig {
  name: string;
  importPath: string;
  chunkName?: string;
  preload?: boolean;
  lazyLoad?: boolean;
}

// 第三方库优化配置
export const libraryOptimizations: LibraryOptimizationConfig[] = [
  // Element Plus
  {
    name: 'element-plus',
    importPath: 'element-plus',
    chunkName: 'element-plus',
    lazyLoad: false // 核心UI库，不延迟加载
  },
  // Element Plus 图标
  {
    name: 'element-plus-icons',
    importPath: '@element-plus/icons-vue',
    chunkName: 'element-icons',
    lazyLoad: true // 图标可以延迟加载
  },
  // 图表库
  {
    name: 'echarts',
    importPath: 'echarts',
    chunkName: 'echarts',
    lazyLoad: true
  },
  // 日期处理库
  {
    name: 'dayjs',
    importPath: 'dayjs',
    chunkName: 'dayjs',
    lazyLoad: false // 日期处理常用，不延迟加载
  }
];

/**
 * 获取所有需要预加载的资源
 * @returns 预加载资源配置数组
 */
export function getPreloadResources(): ResourceConfig[] {
  return [
    ...criticalResources.filter(r => r.preload),
    ...highPriorityResources.filter(r => r.preload)
  ];
}

/**
 * 获取所有需要预获取的资源
 * @returns 预获取资源配置数组
 */
export function getPrefetchResources(): ResourceConfig[] {
  return [
    ...mediumPriorityResources.filter(r => r.prefetch),
    ...lowPriorityResources.filter(r => r.prefetch)
  ];
}