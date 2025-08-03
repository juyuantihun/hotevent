import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import ErrorReport from '@/components/common/ErrorReport.vue';
import { ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElRate, ElCheckbox } from 'element-plus';

// 模拟Element Plus组件
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus');
  return {
    ...actual as any,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
    },
  };
});

describe('ErrorReport组件', () => {
  beforeEach(() => {
    // 清除模拟函数的调用记录
    vi.clearAllMocks();
  });
  
  it('应该正确渲染错误报告对话框', () => {
    const wrapper = mount(ErrorReport, {
      props: {
        title: '测试错误报告',
        errorInfo: {
          message: '测试错误消息',
          timestamp: Date.now(),
          location: '测试位置',
        },
      },
      global: {
        stubs: {
          ElDialog: true,
          ElForm: true,
          ElFormItem: true,
          ElInput: true,
          ElSelect: true,
          ElRate: true,
          ElCheckbox: true,
          ElAlert: true,
          ElButton: true,
          ElIcon: true,
          ElDivider: true,
        },
      },
    });
    
    // 验证对话框标题
    expect(wrapper.props('title')).toBe('测试错误报告');
    
    // 验证错误信息被正确传递
    expect(wrapper.props('errorInfo').message).toBe('测试错误消息');
  });
  
  it('open方法应该打开对话框并设置初始描述', async () => {
    const wrapper = mount(ErrorReport, {
      props: {
        errorInfo: {
          message: '测试错误消息',
          timestamp: Date.now(),
          location: '测试位置',
        },
      },
      global: {
        stubs: {
          ElDialog: true,
          ElForm: true,
          ElFormItem: true,
          ElInput: true,
          ElSelect: true,
          ElRate: true,
          ElCheckbox: true,
          ElAlert: true,
          ElButton: true,
          ElIcon: true,
          ElDivider: true,
        },
      },
    });
    
    // 初始状态对话框应该是关闭的
    expect(wrapper.vm.dialogVisible).toBe(false);
    
    // 调用open方法
    wrapper.vm.open();
    
    // 验证对话框被打开
    expect(wrapper.vm.dialogVisible).toBe(true);
    
    // 验证描述被自动填充
    expect(wrapper.vm.form.description).toBe('我遇到了以下错误: "测试错误消息"');
  });
  
  it('close方法应该关闭对话框', async () => {
    const wrapper = mount(ErrorReport, {
      props: {
        errorInfo: {
          message: '测试错误消息',
          timestamp: Date.now(),
          location: '测试位置',
        },
      },
      global: {
        stubs: {
          ElDialog: true,
          ElForm: true,
          ElFormItem: true,
          ElInput: true,
          ElSelect: true,
          ElRate: true,
          ElCheckbox: true,
          ElAlert: true,
          ElButton: true,
          ElIcon: true,
          ElDivider: true,
        },
      },
    });
    
    // 先打开对话框
    wrapper.vm.open();
    expect(wrapper.vm.dialogVisible).toBe(true);
    
    // 调用close方法
    wrapper.vm.close();
    
    // 验证对话框被关闭
    expect(wrapper.vm.dialogVisible).toBe(false);
  });
  
  it('handleDialogClosed应该重置表单并触发close事件', async () => {
    const wrapper = mount(ErrorReport, {
      props: {
        errorInfo: {
          message: '测试错误消息',
          timestamp: Date.now(),
          location: '测试位置',
        },
      },
      global: {
        stubs: {
          ElDialog: true,
          ElForm: true,
          ElFormItem: true,
          ElInput: true,
          ElSelect: true,
          ElRate: true,
          ElCheckbox: true,
          ElAlert: true,
          ElButton: true,
          ElIcon: true,
          ElDivider: true,
        },
      },
    });
    
    // 模拟表单引用
    wrapper.vm.formRef = {
      resetFields: vi.fn(),
    } as any;
    
    // 调用handleDialogClosed方法
    await wrapper.vm.handleDialogClosed();
    
    // 验证表单被重置
    expect(wrapper.vm.formRef.resetFields).toHaveBeenCalled();
    
    // 验证close事件被触发
    expect(wrapper.emitted('close')).toBeTruthy();
  });
  
  it('submitReport应该在表单验证通过后提交报告', async () => {
    const wrapper = mount(ErrorReport, {
      props: {
        errorInfo: {
          message: '测试错误消息',
          timestamp: Date.now(),
          location: '测试位置',
        },
      },
      global: {
        stubs: {
          ElDialog: true,
          ElForm: true,
          ElFormItem: true,
          ElInput: true,
          ElSelect: true,
          ElRate: true,
          ElCheckbox: true,
          ElAlert: true,
          ElButton: true,
          ElIcon: true,
          ElDivider: true,
        },
      },
    });
    
    // 模拟表单验证通过
    wrapper.vm.formRef = {
      validate: vi.fn((callback) => callback(true)),
    } as any;
    
    // 设置表单数据
    wrapper.vm.form.type = 'functional';
    wrapper.vm.form.severity = 4;
    wrapper.vm.form.description = '测试描述';
    wrapper.vm.form.steps = '测试步骤';
    wrapper.vm.form.contact = 'test@example.com';
    wrapper.vm.form.includeTechInfo = true;
    wrapper.vm.form.includeErrorDetails = true;
    
    // 调用submitReport方法
    await wrapper.vm.submitReport();
    
    // 验证提交状态
    expect(wrapper.vm.submitting).toBe(true);
    
    // 等待异步操作完成
    await new Promise(resolve => setTimeout(resolve, 1100));
    
    // 验证submit事件被触发
    expect(wrapper.emitted('submit')).toBeTruthy();
    const submitData = wrapper.emitted('submit')?.[0][0];
    expect(submitData.type).toBe('functional');
    expect(submitData.severity).toBe(4);
    expect(submitData.description).toBe('测试描述');
    expect(submitData.errorInfo).toEqual(wrapper.props('errorInfo'));
    
    // 验证对话框被关闭
    expect(wrapper.vm.dialogVisible).toBe(false);
    
    // 验证提交状态被重置
    expect(wrapper.vm.submitting).toBe(false);
  });
  
  it('submitReport应该在表单验证失败时不提交报告', async () => {
    const wrapper = mount(ErrorReport, {
      props: {
        errorInfo: {
          message: '测试错误消息',
          timestamp: Date.now(),
          location: '测试位置',
        },
      },
      global: {
        stubs: {
          ElDialog: true,
          ElForm: true,
          ElFormItem: true,
          ElInput: true,
          ElSelect: true,
          ElRate: true,
          ElCheckbox: true,
          ElAlert: true,
          ElButton: true,
          ElIcon: true,
          ElDivider: true,
        },
      },
    });
    
    // 模拟表单验证失败
    wrapper.vm.formRef = {
      validate: vi.fn((callback) => callback(false)),
    } as any;
    
    // 调用submitReport方法
    await wrapper.vm.submitReport();
    
    // 验证submit事件没有被触发
    expect(wrapper.emitted('submit')).toBeFalsy();
    
    // 验证对话框没有被关闭
    expect(wrapper.vm.dialogVisible).toBe(false);
  });
  
  it('formatTime应该正确格式化时间戳', () => {
    const wrapper = mount(ErrorReport, {
      global: {
        stubs: {
          ElDialog: true,
          ElForm: true,
          ElFormItem: true,
          ElInput: true,
          ElSelect: true,
          ElRate: true,
          ElCheckbox: true,
          ElAlert: true,
          ElButton: true,
          ElIcon: true,
          ElDivider: true,
        },
      },
    });
    
    // 测试时间戳格式化
    const timestamp = new Date('2023-01-01T12:00:00').getTime();
    const formatted = wrapper.vm.formatTime(timestamp);
    
    // 验证格式化结果是否为字符串
    expect(typeof formatted).toBe('string');
    expect(formatted).not.toBe('');
    
    // 验证空值处理
    expect(wrapper.vm.formatTime('')).toBe('');
    expect(wrapper.vm.formatTime(null)).toBe('');
    expect(wrapper.vm.formatTime(undefined)).toBe('');
  });
});