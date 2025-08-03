// 直接请求工具，绕过模拟数据机制
import axios from 'axios';

// 创建一个新的axios实例，不使用拦截器
const directAxios = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
});

// 添加请求拦截器
directAxios.interceptors.request.use(
  (config) => {
    // 添加token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // 添加请求ID
    const requestId = `direct_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    config.headers['X-Request-ID'] = requestId;
    
    console.log(`[直接请求] 发送请求: ${config.method} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('[直接请求] 请求错误:', error);
    return Promise.reject(error);
  }
);

// 添加响应拦截器
directAxios.interceptors.response.use(
  (response) => {
    console.log(`[直接请求] 收到响应: ${response.status}`, response.data);
    
    // 处理响应数据
    const { code, msg, data } = response.data;
    
    if (code === 200 || code === 0) {
      return data;
    } else {
      const error = new Error(msg || '请求失败');
      console.error('[直接请求] 业务错误:', msg);
      return Promise.reject(error);
    }
  },
  (error) => {
    console.error('[直接请求] 响应错误:', error);
    return Promise.reject(error);
  }
);

/**
 * 直接发送POST请求，绕过模拟数据机制
 * @param {string} url 请求URL
 * @param {object} data 请求体数据
 * @param {object} params URL参数
 * @returns {Promise} 请求Promise
 */
export function directPost(url, data = {}, params = {}) {
  console.log(`[直接请求] POST ${url}`, { data, params });
  return directAxios.post(url, data, { params });
}

/**
 * 直接发送GET请求，绕过模拟数据机制
 * @param {string} url 请求URL
 * @param {object} params URL参数
 * @returns {Promise} 请求Promise
 */
export function directGet(url, params = {}) {
  console.log(`[直接请求] GET ${url}`, { params });
  return directAxios.get(url, { params });
}

export default directAxios;