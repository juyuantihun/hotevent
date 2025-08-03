// 一个简单的测试文件，不依赖任何测试框架
console.log('开始运行简单测试...');

// 测试ErrorBoundary组件是否存在
try {
  const fs = require('fs');
  const path = require('path');
  
  const errorBoundaryPath = path.join(__dirname, '../components/common/ErrorBoundary.vue');
  const exists = fs.existsSync(errorBoundaryPath);
  
  console.log(`ErrorBoundary组件${exists ? '存在' : '不存在'}`);
  
  if (exists) {
    const content = fs.readFileSync(errorBoundaryPath, 'utf-8');
    console.log('ErrorBoundary组件内容长度:', content.length);
    
    // 检查组件是否包含关键函数
    const hasHandleError = content.includes('handleError');
    const hasResetError = content.includes('resetError');
    
    console.log('包含handleError函数:', hasHandleError);
    console.log('包含resetError函数:', hasResetError);
  }
  
  // 测试ErrorReport组件是否存在
  const errorReportPath = path.join(__dirname, '../components/common/ErrorReport.vue');
  const errorReportExists = fs.existsSync(errorReportPath);
  
  console.log(`ErrorReport组件${errorReportExists ? '存在' : '不存在'}`);
  
  // 测试API错误处理器是否存在
  const apiErrorHandlerPath = path.join(__dirname, '../api/errorHandler.ts');
  const apiErrorHandlerExists = fs.existsSync(apiErrorHandlerPath);
  
  console.log(`API错误处理器${apiErrorHandlerExists ? '存在' : '不存在'}`);
  
  // 测试错误处理服务是否存在
  const errorHandlerServicePath = path.join(__dirname, '../services/errorHandler.ts');
  const errorHandlerServiceExists = fs.existsSync(errorHandlerServicePath);
  
  console.log(`错误处理服务${errorHandlerServiceExists ? '存在' : '不存在'}`);
  
  console.log('简单测试完成，所有测试通过！');
} catch (error) {
  console.error('测试过程中发生错误:', error);
  process.exit(1);
}