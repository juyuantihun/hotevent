// 简单测试 security.ts 文件的语法
try {
  // 读取文件内容
  const fs = require('fs');
  const content = fs.readFileSync('src/utils/security.ts', 'utf8');
  
  // 检查是否有明显的语法错误
  console.log('文件读取成功');
  console.log('文件长度:', content.length);
  
  // 检查正则表达式
  const regexLines = content.split('\n').filter(line => line.includes('/.*/'));
  console.log('找到的正则表达式行数:', regexLines.length);
  
  regexLines.forEach((line, index) => {
    console.log(`正则表达式 ${index + 1}:`, line.trim());
  });
  
} catch (error) {
  console.error('错误:', error.message);
}