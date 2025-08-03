import { vi } from 'vitest';
import { mockTimelines, mockTimelineDetail, mockRegions } from '../fixtures/timelines';

// 模拟axios
export function mockAxios() {
  return {
    get: vi.fn((url) => {
      if (url.includes('/api/timelines')) {
        if (url.includes('/api/timelines/1')) {
          return Promise.resolve({ data: mockTimelineDetail });
        }
        return Promise.resolve({ data: { records: mockTimelines, total: mockTimelines.length } });
      }
      
      if (url.includes('/api/regions')) {
        return Promise.resolve({ data: mockRegions });
      }
      
      return Promise.reject(new Error(`未模拟的API请求: ${url}`));
    }),
    
    post: vi.fn((url, data) => {
      if (url.includes('/api/timelines')) {
        return Promise.resolve({ 
          data: { 
            id: '5', 
            ...data,
            createdAt: new Date().toISOString(),
            status: 'draft'
          } 
        });
      }
      
      if (url.includes('/api/auth/login')) {
        if (data.username === 'admin' && data.password === 'password') {
          return Promise.resolve({ 
            data: { 
              token: 'mock-jwt-token',
              user: {
                id: '1',
                username: 'admin',
                name: '管理员',
                roles: ['admin']
              }
            } 
          });
        } else {
          return Promise.reject({ 
            response: { 
              status: 401, 
              data: { message: '用户名或密码错误' } 
            } 
          });
        }
      }
      
      return Promise.reject(new Error(`未模拟的API请求: ${url}`));
    }),
    
    put: vi.fn((url, data) => {
      if (url.includes('/api/timelines')) {
        const id = url.split('/').pop();
        return Promise.resolve({ 
          data: { 
            id,
            ...data,
            updatedAt: new Date().toISOString()
          } 
        });
      }
      
      return Promise.reject(new Error(`未模拟的API请求: ${url}`));
    }),
    
    delete: vi.fn((url) => {
      if (url.includes('/api/timelines')) {
        return Promise.resolve({ data: { success: true } });
      }
      
      return Promise.reject(new Error(`未模拟的API请求: ${url}`));
    }),
  };
}

// 设置全局API模拟
export function setupApiMocks() {
  vi.mock('axios', () => {
    return {
      default: mockAxios(),
      create: () => mockAxios(),
    };
  });
}