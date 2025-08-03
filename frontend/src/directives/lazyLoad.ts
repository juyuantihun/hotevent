/**
 * 图片懒加载指令
 * 用于优化图片加载性能
 */

interface LazyLoadOptions {
  // 图片加载前显示的占位图
  placeholder?: string;
  // 图片加载失败时显示的图片
  error?: string;
  // 观察器的根元素
  root?: Element | null;
  // 根元素的边距
  rootMargin?: string;
  // 阈值
  threshold?: number | number[];
}

export default {
  install(app: any) {
    // 默认配置
    const defaultOptions: LazyLoadOptions = {
      placeholder: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2VlZWVlZSIvPjwvc3ZnPg==',
      error: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2ZmZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjI0IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBhbGlnbm1lbnQtYmFzZWxpbmU9Im1pZGRsZSIgZmlsbD0iI2Y1NmM2YyI+5Zu+54mH5Yqg6L295aSx6LSlPC90ZXh0Pjwvc3ZnPg==',
      rootMargin: '0px',
      threshold: 0.1
    };

    // 存储所有需要懒加载的元素和它们的观察器
    const observerMap = new WeakMap<Element, IntersectionObserver>();
    const stateMap = new WeakMap<Element, { src: string, loading: boolean, error: boolean }>();

    // 设置图片源
    const setImageSrc = (el: HTMLElement, binding: any, options: LazyLoadOptions) => {
      const imgSrc = binding.value;
      
      // 如果没有图片源，则不处理
      if (!imgSrc) return;
      
      // 存储原始图片源
      if (!stateMap.has(el)) {
        stateMap.set(el, {
          src: imgSrc,
          loading: true,
          error: false
        });
      }
      
      // 设置占位图
      if (el.tagName.toLowerCase() === 'img') {
        (el as HTMLImageElement).src = options.placeholder || '';
      } else {
        el.style.backgroundImage = `url('${options.placeholder || ''}')`;
      }
      
      // 创建观察器
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const state = stateMap.get(el);
            if (state && state.loading && !state.error) {
              loadImage(el, state.src, options);
              observer.unobserve(el);
              observerMap.delete(el);
            }
          }
        });
      }, {
        root: options.root,
        rootMargin: options.rootMargin,
        threshold: options.threshold
      });
      
      // 存储观察器
      observerMap.set(el, observer);
      
      // 开始观察
      observer.observe(el);
    };
    
    // 加载图片
    const loadImage = (el: HTMLElement, src: string, options: LazyLoadOptions) => {
      const state = stateMap.get(el);
      if (!state) return;
      
      // 创建一个新的图片对象来加载图片
      const img = new Image();
      
      // 图片加载成功
      img.onload = () => {
        if (el.tagName.toLowerCase() === 'img') {
          (el as HTMLImageElement).src = src;
        } else {
          el.style.backgroundImage = `url('${src}')`;
        }
        state.loading = false;
      };
      
      // 图片加载失败
      img.onerror = () => {
        if (el.tagName.toLowerCase() === 'img') {
          (el as HTMLImageElement).src = options.error || '';
        } else {
          el.style.backgroundImage = `url('${options.error || ''}')`;
        }
        state.loading = false;
        state.error = true;
      };
      
      // 开始加载图片
      img.src = src;
    };
    
    // 注册指令
    app.directive('lazy', {
      mounted(el: HTMLElement, binding: any) {
        setImageSrc(el, binding, defaultOptions);
      },
      updated(el: HTMLElement, binding: any) {
        if (binding.value !== binding.oldValue) {
          const observer = observerMap.get(el);
          if (observer) {
            observer.unobserve(el);
            observerMap.delete(el);
          }
          setImageSrc(el, binding, defaultOptions);
        }
      },
      unmounted(el: HTMLElement) {
        const observer = observerMap.get(el);
        if (observer) {
          observer.unobserve(el);
          observerMap.delete(el);
        }
        stateMap.delete(el);
      }
    });
  }
};