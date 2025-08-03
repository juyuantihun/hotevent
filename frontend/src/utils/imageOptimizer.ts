/**
 * 图片优化工具
 * 用于优化图片资源的加载和使用
 */

// 图片格式
export enum ImageFormat {
  JPEG = 'jpeg',
  PNG = 'png',
  WEBP = 'webp',
  AVIF = 'avif'
}

// 图片尺寸
export interface ImageSize {
  width: number;
  height: number;
}

// 图片质量
export type ImageQuality = 'low' | 'medium' | 'high' | 'auto';

// 图片优化选项
export interface ImageOptimizationOptions {
  format?: ImageFormat;
  quality?: ImageQuality | number;
  size?: ImageSize;
  responsive?: boolean;
  lazyLoad?: boolean;
}

/**
 * 检测浏览器支持的图片格式
 * @returns 支持的图片格式数组
 */
export function detectSupportedFormats(): ImageFormat[] {
  const supported: ImageFormat[] = [ImageFormat.JPEG, ImageFormat.PNG];
  
  // 检测WebP支持
  const webpCanvas = document.createElement('canvas');
  if (webpCanvas.toDataURL('image/webp').indexOf('data:image/webp') === 0) {
    supported.push(ImageFormat.WEBP);
  }
  
  // 检测AVIF支持
  const img = new Image();
  img.onload = () => {
    if (img.width > 0 && img.height > 0) {
      supported.push(ImageFormat.AVIF);
    }
  };
  img.onerror = () => {
    // 不支持AVIF
  };
  img.src = 'data:image/avif;base64,AAAAIGZ0eXBhdmlmAAAAAGF2aWZtaWYxbWlhZk1BMUIAAADybWV0YQAAAAAAAAAoaGRscgAAAAAAAAAAcGljdAAAAAAAAAAAAAAAAGxpYmF2aWYAAAAADnBpdG0AAAAAAAEAAAAeaWxvYwAAAABEAAABAAEAAAABAAABGgAAAB0AAAAoaWluZgAAAAAAAQAAABppbmZlAgAAAAABAABhdjAxQ29sb3IAAAAAamlwcnAAAABLaXBjbwAAABRpc3BlAAAAAAAAAAIAAAACAAAAEHBpeGkAAAAAAwgICAAAAAxhdjFDgQ0MAAAAABNjb2xybmNseAACAAIAAYAAAAAXaXBtYQAAAAAAAAABAAEEAQKDBAAAACVtZGF0EgAKCBgANogQEAwgMg8f8D///8WfhwB8+ErK';
  
  return supported;
}

/**
 * 获取最佳图片格式
 * @returns 最佳图片格式
 */
export function getBestImageFormat(): ImageFormat {
  const supported = detectSupportedFormats();
  
  // 优先使用AVIF，其次是WebP，最后是JPEG
  if (supported.includes(ImageFormat.AVIF)) {
    return ImageFormat.AVIF;
  } else if (supported.includes(ImageFormat.WEBP)) {
    return ImageFormat.WEBP;
  } else {
    return ImageFormat.JPEG;
  }
}

/**
 * 生成优化的图片URL
 * @param url 原始图片URL
 * @param options 优化选项
 * @returns 优化后的图片URL
 */
export function getOptimizedImageUrl(url: string, options: ImageOptimizationOptions = {}): string {
  // 如果URL是数据URI或绝对URL，则不处理
  if (url.startsWith('data:') || url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  
  // 解析URL
  const urlObj = new URL(url, window.location.origin);
  const params = new URLSearchParams(urlObj.search);
  
  // 设置格式
  if (options.format) {
    params.set('format', options.format);
  } else {
    // 使用最佳格式
    params.set('format', getBestImageFormat());
  }
  
  // 设置质量
  if (options.quality) {
    if (typeof options.quality === 'number') {
      params.set('quality', options.quality.toString());
    } else {
      const qualityMap = {
        low: '60',
        medium: '75',
        high: '85',
        auto: 'auto'
      };
      params.set('quality', qualityMap[options.quality]);
    }
  }
  
  // 设置尺寸
  if (options.size) {
    if (options.size.width) {
      params.set('width', options.size.width.toString());
    }
    if (options.size.height) {
      params.set('height', options.size.height.toString());
    }
  }
  
  // 更新URL参数
  urlObj.search = params.toString();
  
  return urlObj.toString();
}

/**
 * 生成响应式图片源集
 * @param url 原始图片URL
 * @param widths 宽度数组
 * @param options 优化选项
 * @returns 图片源集字符串
 */
export function generateSrcSet(url: string, widths: number[], options: ImageOptimizationOptions = {}): string {
  return widths
    .map(width => {
      const optimizedUrl = getOptimizedImageUrl(url, {
        ...options,
        size: { width, height: 0 }
      });
      return `${optimizedUrl} ${width}w`;
    })
    .join(', ');
}

/**
 * 生成响应式尺寸字符串
 * @param breakpoints 断点配置
 * @returns 尺寸字符串
 */
export function generateSizes(breakpoints: { [key: string]: string }): string {
  return Object.entries(breakpoints)
    .map(([breakpoint, size]) => `(max-width: ${breakpoint}) ${size}`)
    .join(', ');
}

/**
 * 创建图片占位符
 * @param width 宽度
 * @param height 高度
 * @param color 颜色
 * @returns 占位符数据URI
 */
export function createPlaceholder(width: number = 1, height: number = 1, color: string = '#eeeeee'): string {
  // 创建SVG占位符
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}"><rect width="${width}" height="${height}" fill="${color}"/></svg>`;
  
  // 转换为Base64
  return `data:image/svg+xml;base64,${btoa(svg)}`;
}

/**
 * 预加载关键图片
 * @param urls 图片URL数组
 */
export function preloadCriticalImages(urls: string[]): void {
  urls.forEach(url => {
    const link = document.createElement('link');
    link.rel = 'preload';
    link.as = 'image';
    link.href = url;
    document.head.appendChild(link);
  });
}

/**
 * 图片加载状态监控
 * @param url 图片URL
 * @returns Promise
 */
export function monitorImageLoad(url: string): Promise<{ success: boolean, loadTime: number }> {
  return new Promise(resolve => {
    const img = new Image();
    const startTime = performance.now();
    
    img.onload = () => {
      const loadTime = performance.now() - startTime;
      resolve({ success: true, loadTime });
    };
    
    img.onerror = () => {
      const loadTime = performance.now() - startTime;
      resolve({ success: false, loadTime });
    };
    
    img.src = url;
  });
}