import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import ApiErrorFeedback from '@/components/common/ApiErrorFeedback.vue';
import { ElButton } from 'element-plus';

describe('ApiErrorFeedback组件', () => {
  beforeEach(() => {
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
  });
  
  it('当没有错误时不应显示', () => {
    const wrapper = mount(ApiErrorFeedback, {
      props: {
        error: null
      },
      global: {
        stubs: {
          ElAlert: true,
          ElButton: true
        }
      }
    });
    
    // 验证组件不可见
    expect(wrapper.isVisible()).toBe(false);
  });
  
  it('当有错误时应显示错误消息', () => {
    const error = {
      message: '测试API错误',
      code: 500,
      details: { url: '/api/test' }
    };
    
    const wrapper = mount(ApiErrorFeedback, {
      props: {
        error
      },
      global: {
        stubs: {
          ElAlert: true,
          ElButton: true
        }
      }
    });
    
    // 验证组件可见
    expect(wrapper.isVisible()).toBe(true);
    
    // 验证显示了错误消息
    expect(wrapper.text()).toContain('测试API错误');
  });
  
  it('点击重试按钮应该触发retry事件', async () => {
    const error = {
      message: '测试API错误',
      code: 500,
      details: { url: '/api/test' }
    };
    
    const wrapper = mount(ApiErrorFeedback, {
      props: {
        error,
        retryable: true
      },
      global: {
        stubs: {
          ElAlert: true
        }
      }
    });
    
    // 点击重试按钮
    await wrapper.findComponent(ElButton).trigger('click');
    
    // 验证retry事件被触发
    expect(wrapper.emitted('retry')).toBeTruthy();
  });
  
  it('当retryable为false时不应显示重试按钮', () => {
    const error = {
      message: '测试API错误',
      code: 403,
      details: { url: '/api/test' }
    };
    
    const wrapper = mount(ApiErrorFeedback, {
      props: {
        error,
        retryable: false
      },
      global: {
        stubs: {
          ElAlert: true,
          ElButton: true
        }
      }
    });
    
    // 验证重试按钮不存在
    expect(wrapper.findComponent(ElButton).exists()).toBe(false);
  });
  
  it('应该根据错误代码显示不同的错误类型', () => {
    // 测试不同的错误代码
    const errorCodes = [
      { code: 400, type: 'warning' },
      { code: 401, type: 'error' },
      { code: 403, type: 'error' },
      { code: 404, type: 'warning' },
      { code: 500, type: 'error' }
    ];
    
    for (const { code, type } of errorCodes) {
      const error = {
        message: `测试错误 ${code}`,
        code,
        details: { url: '/api/test' }
      };
      
      const wrapper = mount(ApiErrorFeedback, {
        props: {
          error
        },
        global: {
          stubs: {
            ElAlert: {
              template: '<div :data-type="type"><slot /></div>',
              props: ['type']
            },
            ElButton: true
          }
        }
      });
      
      // 验证使用了正确的错误类型
      expect(wrapper.findComponent({ name: 'ElAlert' }).attributes('data-type')).toBe(type);
    }
  });
  
  it('应该显示错误详情', () => {
    const error = {
      message: '测试API错误',
      code: 500,
      details: {
        url: '/api/test',
        method: 'GET',
        params: { id: 1 }
      }
    };
    
    const wrapper = mount(ApiErrorFeedback, {
      props: {
        error,
        showDetails: true
      },
      global: {
        stubs: {
          ElAlert: true,
          ElButton: true
        }
      }
    });
    
    // 验证显示了错误详情
    const detailsText = wrapper.text();
    expect(detailsText).toContain('/api/test');
    expect(detailsText).toContain('GET');
  });
  
  it('当showDetails为false时不应显示错误详情', () => {
    const error = {
      message: '测试API错误',
      code: 500,
      details: {
        url: '/api/test',
        method: 'GET',
        params: { id: 1 }
      }
    };
    
    const wrapper = mount(ApiErrorFeedback, {
      props: {
        error,
        showDetails: false
      },
      global: {
        stubs: {
          ElAlert: true,
          ElButton: true
        }
      }
    });
    
    // 验证没有显示错误详情
    const detailsText = wrapper.text();
    expect(detailsText).not.toContain('/api/test');
    expect(detailsText).not.toContain('GET');
  });
  
  it('应该根据错误代码显示不同的图标', () => {
    // 测试不同的错误代码
    const errorCodes = [
      { code: 400, icon: 'warning' },
      { code: 401, icon: 'error' },
      { code: 403, icon: 'error' },
      { code: 404, icon: 'warning' },
      { code: 500, icon: 'error' }
    ];
    
    for (const { code, icon } of errorCodes) {
      const error = {
        message: `测试错误 ${code}`,
        code,
        details: { url: '/api/test' }
      };
      
      const wrapper = mount(ApiErrorFeedback, {
        props: {
          error
        },
        global: {
          stubs: {
            ElAlert: {
              template: '<div :data-icon="icon"><slot /></div>',
              props: ['icon']
            },
            ElButton: true
          }
        }
      });
      
      // 验证使用了正确的图标
      expect(wrapper.findComponent({ name: 'ElAlert' }).attributes('data-icon')).toBe(icon);
    }
  });
});