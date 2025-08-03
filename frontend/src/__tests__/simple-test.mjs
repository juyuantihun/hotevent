// 一个简单的测试文件，使用ES模块
import { readFileSync, existsSync, readdirSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

console.log('开始运行前端组件测试...');

// 获取当前文件的目录
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// 测试结果统计
let passedTests = 0;
let failedTests = 0;

// 简单的断言函数
function assert(condition, message) {
  if (condition) {
    console.log('✅ ' + message);
    passedTests++;
  } else {
    console.error('❌ ' + message);
    failedTests++;
  }
}

// 测试组件是否存在
function testComponentExists(componentPath, componentName) {
  const fullPath = join(__dirname, componentPath);
  const exists = existsSync(fullPath);
  assert(exists, `${componentName}组件${exists ? '存在' : '不存在'}`);
  return exists;
}

// 测试组件是否包含特定函数
function testComponentHasFunction(componentPath, functionName) {
  const fullPath = join(__dirname, componentPath);
  if (!existsSync(fullPath)) return false;
  
  const content = readFileSync(fullPath, 'utf-8');
  const hasFunction = content.includes(functionName);
  assert(hasFunction, `组件包含${functionName}函数: ${hasFunction}`);
  return hasFunction;
}

// 测试目录是否包含特定数量的文件
function testDirectoryHasFiles(dirPath, minFileCount) {
  const fullPath = join(__dirname, dirPath);
  if (!existsSync(fullPath)) {
    assert(false, `目录${dirPath}不存在`);
    return false;
  }
  
  try {
    const files = readdirSync(fullPath);
    const fileCount = files.length;
    assert(fileCount >= minFileCount, `目录${dirPath}包含${fileCount}个文件，期望至少${minFileCount}个`);
    return fileCount >= minFileCount;
  } catch (error) {
    assert(false, `读取目录${dirPath}失败: ${error.message}`);
    return false;
  }
}

try {
  console.log('\n1. 测试错误处理组件');
  testComponentExists('../components/common/ErrorBoundary.vue', 'ErrorBoundary');
  testComponentExists('../components/common/ErrorReport.vue', 'ErrorReport');
  testComponentExists('../components/common/ErrorState.vue', 'ErrorState');
  testComponentExists('../components/common/GlobalErrorDisplay.vue', 'GlobalErrorDisplay');
  testComponentExists('../components/common/ApiErrorFeedback.vue', 'ApiErrorFeedback');
  
  console.log('\n2. 测试错误处理组件功能');
  testComponentHasFunction('../components/common/ErrorBoundary.vue', 'handleError');
  testComponentHasFunction('../components/common/ErrorBoundary.vue', 'resetError');
  testComponentHasFunction('../components/common/ErrorReport.vue', 'submitReport');
  testComponentHasFunction('../components/common/GlobalErrorDisplay.vue', 'dismissError');
  
  console.log('\n3. 测试错误处理服务');
  testComponentExists('../api/errorHandler.ts', 'API错误处理器');
  testComponentExists('../services/errorHandler.ts', '错误处理服务');
  testComponentHasFunction('../api/errorHandler.ts', 'createApiErrorHandler');
  testComponentHasFunction('../services/errorHandler.ts', 'handleError');
  
  console.log('\n4. 测试测试文件');
  testDirectoryHasFiles('.', 5);
  testDirectoryHasFiles('./components', 3);
  testDirectoryHasFiles('./api', 1);
  testDirectoryHasFiles('./services', 1);
  
  console.log('\n5. 测试状态管理');
  testComponentExists('../store/index.ts', '状态管理入口');
  testComponentExists('../store/modules/app.ts', 'App状态模块');
  testComponentExists('../store/modules/auth.ts', '认证状态模块');
  
  console.log('\n测试完成!');
  console.log(`通过: ${passedTests} 失败: ${failedTests}`);
  
  if (failedTests > 0) {
    console.error('有测试失败，请检查上面的错误信息');
    process.exit(1);
  } else {
    console.log('所有测试通过！');
  }
} catch (error) {
  console.error('测试过程中发生错误:', error);
  process.exit(1);
}