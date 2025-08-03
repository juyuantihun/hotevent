/**
 * 组件渲染优化工具
 * 用于减少不必要的重渲染
 */
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'

/**
 * 使用延迟渲染
 * 在组件挂载后延迟渲染内容，用于优化首次渲染性能
 * 
 * @param delay 延迟时间（毫秒）
 * @returns 是否应该渲染的响应式引用
 */
export function useDeferredRender(delay: number = 100) {
  const shouldRender = ref(false);
  let timeoutId: number | null = null;
  
  onMounted(() => {
    timeoutId = window.setTimeout(() => {
      shouldRender.value = true;
      timeoutId = null;
    }, delay);
  });
  
  onBeforeUnmount(() => {
    if (timeoutId !== null) {
      window.clearTimeout(timeoutId);
      timeoutId = null;
    }
  });
  
  return shouldRender;
}

/**
 * 使用批量更新
 * 将多次状态更新合并为一次渲染
 * 
 * @param initialValue 初始值
 * @param delay 批处理延迟（毫秒）
 * @returns 批处理状态对象
 */
export function useBatchUpdate<T>(initialValue: T, delay: number = 100) {
  const state = ref<T>(initialValue) as { value: T };
  const pendingState = ref<T>(initialValue) as { value: T };
  const isPending = ref(false);
  let timeoutId: number | null = null;
  
  // 更新状态
  const updateState = (newValue: T) => {
    pendingState.value = newValue;
    
    if (!isPending.value) {
      isPending.value = true;
      
      if (timeoutId !== null) {
        window.clearTimeout(timeoutId);
      }
      
      timeoutId = window.setTimeout(() => {
        state.value = pendingState.value;
        isPending.value = false;
        timeoutId = null;
      }, delay);
    }
  };
  
  // 立即应用更新
  const flushUpdates = () => {
    if (isPending.value) {
      if (timeoutId !== null) {
        window.clearTimeout(timeoutId);
        timeoutId = null;
      }
      
      state.value = pendingState.value;
      isPending.value = false;
    }
  };
  
  // 取消待处理的更新
  const cancelUpdates = () => {
    if (timeoutId !== null) {
      window.clearTimeout(timeoutId);
      timeoutId = null;
    }
    
    pendingState.value = state.value;
    isPending.value = false;
  };
  
  onBeforeUnmount(() => {
    if (timeoutId !== null) {
      window.clearTimeout(timeoutId);
      timeoutId = null;
    }
  });
  
  return {
    state,
    updateState,
    flushUpdates,
    cancelUpdates,
    isPending
  };
}

/**
 * 使用渲染节流
 * 限制组件的重渲染频率
 * 
 * @param source 源数据
 * @param delay 节流延迟（毫秒）
 * @returns 节流后的数据
 */
export function useThrottledRef<T>(source: { value: T }, delay: number = 200) {
  const throttled = ref(source.value) as { value: T };
  let timeoutId: number | null = null;
  let lastUpdateTime = 0;
  
  watch(
    () => source.value,
    (newValue) => {
      const now = Date.now();
      
      if (now - lastUpdateTime >= delay) {
        // 如果已经超过节流时间，立即更新
        throttled.value = newValue;
        lastUpdateTime = now;
        
        if (timeoutId !== null) {
          window.clearTimeout(timeoutId);
          timeoutId = null;
        }
      } else if (timeoutId === null) {
        // 否则设置定时器延迟更新
        timeoutId = window.setTimeout(() => {
          throttled.value = source.value;
          lastUpdateTime = Date.now();
          timeoutId = null;
        }, delay - (now - lastUpdateTime));
      }
    },
    { deep: true }
  );
  
  onBeforeUnmount(() => {
    if (timeoutId !== null) {
      window.clearTimeout(timeoutId);
      timeoutId = null;
    }
  });
  
  return throttled;
}

/**
 * 使用可见性检测
 * 只有当元素在视口中可见时才渲染内容
 * 
 * @returns 可见性检测工具
 */
export function useVisibilityDetection() {
  const elementRef = ref<HTMLElement | null>(null);
  const isVisible = ref(false);
  let observer: IntersectionObserver | null = null;
  
  onMounted(() => {
    if ('IntersectionObserver' in window && elementRef.value) {
      observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            isVisible.value = entry.isIntersecting;
          });
        },
        { threshold: 0.1 }
      );
      
      observer.observe(elementRef.value);
    } else {
      // 降级处理：如果不支持IntersectionObserver，则始终显示
      isVisible.value = true;
    }
  });
  
  onBeforeUnmount(() => {
    if (observer) {
      observer.disconnect();
      observer = null;
    }
  });
  
  return {
    elementRef,
    isVisible
  };
}