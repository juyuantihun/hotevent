const { spawn } = require('child_process');
const path = require('path');

console.log('正在启动 Vite 开发服务器...');
console.log('当前目录:', process.cwd());

const vitePath = path.join(__dirname, 'node_modules', 'vite', 'bin', 'vite.js');
console.log('Vite 路径:', vitePath);

const viteProcess = spawn('node', [vitePath, '--port', '3000', '--host', '127.0.0.1'], {
  stdio: 'inherit',
  cwd: __dirname
});

viteProcess.on('error', (error) => {
  console.error('启动失败:', error);
});

viteProcess.on('exit', (code) => {
  console.log(`Vite 进程退出，代码: ${code}`);
});

console.log('Vite 进程 PID:', viteProcess.pid);