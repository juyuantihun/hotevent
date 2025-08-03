// 直接测试region API
const axios = require('axios');

async function testRegionApi() {
  try {
    console.log('测试region树形结构API...');
    
    const response = await axios.get('http://localhost:8080/api/regions/tree', {
      timeout: 10000,
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    });
    
    console.log('API响应状态:', response.status);
    console.log('API响应数据:', JSON.stringify(response.data, null, 2));
    
    if (response.data && response.data.data) {
      console.log('地区数量:', response.data.data.length);
      console.log('前3个地区:');
      response.data.data.slice(0, 3).forEach(region => {
        console.log(`  - ID: ${region.id}, 名称: ${region.name}, 类型: ${region.type}, 父ID: ${region.parentId}`);
        if (region.children && region.children.length > 0) {
          console.log(`    子地区数量: ${region.children.length}`);
        }
      });
    }
    
  } catch (error) {
    console.error('API请求失败:');
    console.error('错误类型:', error.constructor.name);
    console.error('错误消息:', error.message);
    
    if (error.response) {
      console.error('HTTP状态码:', error.response.status);
      console.error('响应头:', error.response.headers);
      console.error('响应数据:', error.response.data);
    } else if (error.request) {
      console.error('请求配置:', error.config);
      console.error('无响应');
    }
  }
}

// 运行测试
testRegionApi();