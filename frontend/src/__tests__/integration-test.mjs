// 集成测试文件
import { readFileSync, existsSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

console.log('开始运行前端集成测试...');

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

// 检查组件是否引用了另一个组件
function testComponentImports(componentPath, importedComponentName) {
  const fullPath = join(__dirname, componentPath);
  if (!existsSync(fullPath)) {
    assert(false, `组件${componentPath}不存在`);
    return false;
  }
  
  const content = readFileSync(fullPath, 'utf-8');
  const hasImport = content.includes(`import ${importedComponentName}`) || 
                    content.includes(`from './${importedComponentName}.vue'`) ||
                    content.includes(`from './${importedComponentName}'`) ||
                    content.includes(`from '@/components/common/${importedComponentName}.vue'`) ||
                    content.includes(`from '@/components/common/${importedComponentName}'`);
  
  assert(hasImport, `组件${componentPath}引用了${importedComponentName}: ${hasImport}`);
  return hasImport;
}

// 检查组件是否使用了特定的服务
function testComponentUsesService(componentPath, serviceName) {
  const fullPath = join(__dirname, componentPath);
  if (!existsSync(fullPath)) {
    assert(false, `组件${componentPath}不存在`);
    return false;
  }
  
  const content = readFileSync(fullPath, 'utf-8');
  const usesService = content.includes(`import { ${serviceName} }`) || 
                      content.includes(`import ${serviceName}`) ||
                      content.includes(`from '@/services/${serviceName}'`) ||
                      content.includes(`from '@/services/${serviceName}.ts'`);
  
  assert(usesService, `组件${componentPath}使用了${serviceName}服务: ${usesService}`);
  return usesService;
}

// 检查组件是否使用了特定的API
function testComponentUsesApi(componentPath, apiName) {
  const fullPath = join(__dirname, componentPath);
  if (!existsSync(fullPath)) {
    assert(false, `组件${componentPath}不存在`);
    return false;
  }
  
  const content = readFileSync(fullPath, 'utf-8');
  const usesApi = content.includes(`import { ${apiName} }`) || 
                  content.includes(`import ${apiName}`) ||
                  content.includes(`from '@/api/${apiName}'`) ||
                  content.includes(`from '@/api/${apiName}.ts'`);
  
  assert(usesApi, `组件${componentPath}使用了${apiName} API: ${usesApi}`);
  return usesApi;
}

// 检查组件是否使用了特定的Store
function testComponentUsesStore(componentPath, storeName) {
  const fullPath = join(__dirname, componentPath);
  if (!existsSync(fullPath)) {
    assert(false, `组件${componentPath}不存在`);
    return false;
  }
  
  const content = readFileSync(fullPath, 'utf-8');
  const usesStore = content.includes(`import { ${storeName} }`) || 
                    content.includes(`import ${storeName}`) ||
                    content.includes(`use${storeName}Store`) ||
                    content.includes(`from '@/store/modules/${storeName.toLowerCase()}'`);
  
  assert(usesStore, `组件${componentPath}使用了${storeName} Store: ${usesStore}`);
  return usesStore;
}

try {
  console.log('\n1. 测试错误处理组件集成');
  testComponentImports('../components/common/ErrorBoundary.vue', 'ErrorState');
  testComponentUsesStore('../components/common/ErrorBoundary.vue', 'App');
  testComponentUsesStore('../components/common/GlobalErrorDisplay.vue', 'App');
  
  console.log('\n2. 测试API错误处理集成');
  testComponentUsesService('../api/errorHandler.ts', 'errorHandler');
  testComponentUsesStore('../api/errorHandler.ts', 'App');
  
  console.log('\n3. 测试错误报告集成');
  testComponentImports('../components/common/ErrorReportManager.vue', 'ErrorReport');
  
  console.log('\n4. 测试认证流程集成');
  testComponentUsesStore('../views/login/index.vue', 'Auth');
  testComponentUsesApi('../store/modules/auth.ts', 'auth');
  
  console.log('\n测试完成!');
  console.log(`通过: ${passedTests} 失败: ${failedTests}`);
  
  if (failedTests > 0) {
    console.error('有测试失败，请检查上面的错误信息');
    process.exit(1);
  } else {
    console.log('所有集成测试通过！');
  }
} catch (error) {
  console.error('测试过程中发生错误:', error);
  process.exit(1);
}