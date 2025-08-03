import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createRouter, createWebHistory } from 'vue-router';
import ErrorReportDemo from '@/views/error-handling/ErrorReportDemo.vue';
import ErrorReportManager from '@/components/common/ErrorReportManager.vue';
import ErrorReport from '@/components/common/ErrorReport.vue';
import UserFeedback from '@/components/common/UserFeedback.vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 模拟Element Plus组件
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus');
  return {
    ...actual as any,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn(),
    },
    ElMessageBox: {
      confirm: vi.fn().mockResolvedValue(true),
    },
  };
});

// 模拟错误报告服务
vi.mock('@/services/errorReportService', () => ({
  reportError: vi.fn().mockResolvedValue(true),
  getErrorReports: vi.fn().mockReturnValue([]),
  clearErrorReports: vi.fn(),
}));

describe('错误处理集成测试', () => {
  let router;
  
  beforeEach(() => {
    // 设置Pinia
    setActivePinia(createPinia());
    
    // 创建路由
    router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: '/',
          component: { template: '<div>Home</div>' }
        },
        {
          path: '/error-demo',
          component: ErrorReportDemo
        }
      ]
    });
    
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
  });
  
  it('错误报告流程应该正确工作', async () => {
    // 挂载错误报告演示组件
    const wrapper = mount(ErrorReportDemo, {
      global: {
        plugins: [router],
        stubs: {
          ErrorReportManager: true,
          UserFeedback: true,
        }
      }
    });
    
    // 模拟错误报告管理器
    const errorReportManager = wrapper.findComponent(ErrorReportManager);
    errorReportManager.vm.$emit('report', {
      message: '测试错误',
      type: 'functional',
      severity: 3,
      description: '测试描述'
    });
    
    // 验证错误报告被处理
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.errorReported).toBe(true);
    expect(ElMessage.success).toHaveBeenCalledWith(expect.stringContaining('感谢您的反馈'));
  });
  
  it('用户反馈流程应该正确工作', async () => {
    // 挂载错误报告演示组件
    const wrapper = mount(ErrorReportDemo, {
      global: {
        plugins: [router],
        stubs: {
          ErrorReportManager: true,
          UserFeedback: true,
        }
      }
    });
    
    // 模拟用户反馈组件
    const userFeedback = wrapper.findComponent(UserFeedback);
    userFeedback.vm.$emit('submit', {
      type: 'suggestion',
      content: '测试反馈',
      rating: 4
    });
    
    // 验证用户反馈被处理
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.feedbackSubmitted).toBe(true);
    expect(ElMessage.success).toHaveBeenCalledWith(expect.stringContaining('感谢您的反馈'));
  });
  
  it('错误报告和用户反馈应该集成在一起', async () => {
    // 创建一个包含两个组件的测试组件
    const TestComponent = {
      template: `
        <div>
          <ErrorReportManager ref="errorManager" />
          <UserFeedback ref="feedbackManager" />
          <button @click="reportError">报告错误</button>
          <button @click="submitFeedback">提交反馈</button>
        </div>
      `,
      components: {
        ErrorReportManager,
        UserFeedback
      },
      methods: {
        reportError() {
          this.$refs.errorManager.openReportDialog({
            message: '测试错误',
            timestamp: Date.now()
          });
        },
        submitFeedback() {
          this.$refs.feedbackManager.openFeedbackDialog();
        }
      }
    };
    
    // 挂载测试组件
    const wrapper = mount(TestComponent, {
      global: {
        plugins: [router],
        stubs: {
          ErrorReport: true,
        }
      }
    });
    
    // 点击报告错误按钮
    await wrapper.find('button:first-child').trigger('click');
    
    // 验证错误报告对话框被打开
    expect(wrapper.vm.$refs.errorManager.showReportDialog).toBe(true);
    
    // 点击提交反馈按钮
    await wrapper.find('button:last-child').trigger('click');
    
    // 验证反馈对话框被打开
    expect(wrapper.vm.$refs.feedbackManager.showDialog).toBe(true);
  });
  
  it('错误报告应该能够从全局错误处理器中触发', async () => {
    // 创建一个包含错误边界和错误报告管理器的测试组件
    const TestComponent = {
      template: `
        <div>
          <ErrorBoundary @error="handleError">
            <ErrorComponent />
          </ErrorBoundary>
          <ErrorReportManager ref="errorManager" />
        </div>
      `,
      components: {
        ErrorBoundary: {
          template: '<div><slot></slot></div>',
          emits: ['error']
        },
        ErrorComponent: {
          template: '<div>{{ nonExistentProperty.value }}</div>',
          mounted() {
            throw new Error('测试错误');
          }
        },
        ErrorReportManager
      },
      methods: {
        handleError(error) {
          this.$refs.errorManager.openReportDialog({
            message: error.error.message,
            timestamp: Date.now()
          });
        }
      }
    };
    
    // 挂载测试组件
    const wrapper = mount(TestComponent, {
      global: {
        plugins: [router],
        stubs: {
          ErrorReport: true,
        }
      }
    });
    
    // 模拟错误触发
    wrapper.findComponent({ name: 'ErrorBoundary' }).vm.$emit('error', {
      error: new Error('测试错误'),
      info: '测试信息'
    });
    
    // 验证错误报告对话框被打开
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.$refs.errorManager.showReportDialog).toBe(true);
  });
});