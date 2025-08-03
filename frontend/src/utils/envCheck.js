// 环境变量检查工具
export function checkEnvironment() {
  console.log('=== 环境变量检查 ===');
  console.log('DEV:', import.meta.env.DEV);
  console.log('PROD:', import.meta.env.PROD);
  console.log('MODE:', import.meta.env.MODE);
  console.log('BASE_URL:', import.meta.env.BASE_URL);
  console.log('所有环境变量:', import.meta.env);
  console.log('=== 检查结束 ===');
  
  return {
    isDev: import.meta.env.DEV,
    isProd: import.meta.env.PROD,
    mode: import.meta.env.MODE,
    baseUrl: import.meta.env.BASE_URL
  };
}