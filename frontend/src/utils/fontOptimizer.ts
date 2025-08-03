/**
 * 字体优化工具
 * 用于优化字体资源的加载和使用，提高页面性能和用户体验
 * 
 * 包含字体格式定义、加载策略、预加载配置等功能
 */

/**
 * 字体格式枚举
 * 定义支持的字体文件格式类型
 */
export enum FontFormat {
  /** WOFF2 格式，最佳压缩比和性能 */
  WOFF2 = 'woff2',
  /** WOFF 格式，良好的兼容性和压缩比 */
  WOFF = 'woff',
  /** TTF 格式，传统字体格式 */
  TTF = 'ttf',
  /** EOT 格式，用于旧版IE浏览器 */
  EOT = 'eot'
}

/**
 * 字体变体接口
 * 定义字体的粗细和样式变体
 */
export interface FontVariant {
  /** 字体粗细，可以是数值(100-900)或关键字(normal, bold等) */
  weight: string | number;
  /** 字体样式，normal或italic */
  style: 'normal' | 'italic';
}

/**
 * 字体加载策略枚举
 * 定义字体加载时的行为策略
 */
export enum FontLoadStrategy {
  /** 立即使用后备字体，字体加载完成后切换 */
  SWAP = 'swap',
  /** 短暂阻塞渲染，然后使用后备字体 */
  BLOCK = 'block',
  /** 短暂使用后备字体，然后切换 */
  FALLBACK = 'fallback',
  /** 仅在已缓存时使用自定义字体 */
  OPTIONAL = 'optional'
}

/**
 * 字体预加载配置接口
 * 定义字体预加载的完整配置选项
 */
export interface FontPreloadConfig {
  /** 字体族名称 */
  family: string;
  /** 字体变体列表 */
  variants: FontVariant[];
  /** 支持的字体格式列表 */
  formats: FontFormat[];
  /** 字体加载策略 */
  strategy: FontLoadStrategy;
  /** 是否预加载字体 */
  preload: boolean;
  /** 可选的Unicode范围，用于子集化 */
  unicodeRange?: string;
}

/**
 * 系统字体堆栈
 * 为不同类型的文本提供优化的系统字体组合
 */
export const systemFontStack = {
  /** 无衬线字体堆栈，适用于大多数UI文本 */
  sans: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif',
  /** 衬线字体堆栈，适用于正文内容 */
  serif: 'Georgia, "Times New Roman", serif',
  /** 等宽字体堆栈，适用于代码显示 */
  mono: 'SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace',
  /** 中文字体堆栈，优化中文显示 */
  chinese: '"PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "WenQuanYi Micro Hei", sans-serif'
};

/**
 * 预定义的字体配置
 * 系统使用的默认字体配置列表
 */
export const fontConfigs: FontPreloadConfig[] = [
  {
    family: 'element-icons',
    variants: [{ weight: 'normal', style: 'normal' }],
    formats: [FontFormat.WOFF2, FontFormat.WOFF],
    strategy: FontLoadStrategy.BLOCK,
    preload: true
  }
];