const fs = require('fs');
const path = require('path');

// 读取文件
const filePath = path.join(__dirname, 'src/views/timeline/components/TimelineDetailView.vue');
let content = fs.readFileSync(filePath, 'utf8');

// 查找并移除重复的函数声明
const functionsToFix = [
  'formatDate',
  'formatTime', 
  'formatDateTime',
  'formatDateTimeMinute'
];

functionsToFix.forEach(funcName => {
  // 查找所有该函数的声明
  const regex = new RegExp(`const ${funcName} = \\([^)]*\\) => \\{[^}]*\\}`, 'g');
  const matches = content.match(regex);
  
  if (matches && matches.length > 1) {
    console.log(`发现 ${matches.length} 个重复的 ${funcName} 函数声明`);
    
    // 保留第一个，移除其他的
    let firstMatch = true;
    content = content.replace(regex, (match) => {
      if (firstMatch) {
        firstMatch = false;
        return match;
      } else {
        console.log(`移除重复的 ${funcName} 函数`);
        return '';
      }
    });
  }
});

// 写回文件
fs.writeFileSync(filePath, content, 'utf8');
console.log('修复完成！');