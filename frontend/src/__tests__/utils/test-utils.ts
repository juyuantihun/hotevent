import { render } from '@testing-library/vue';
import { createRouter, createWebHistory } from 'vue-router';
import { createPinia } from 'pinia';
import { Component } from 'vue';

// 创建一个测试用的路由实例
export function createTestRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: '/',
        name: 'Home',
        component: { template: '<div>Home Page</div>' },
      },
      {
        path: '/login',
        name: 'Login',
        component: { template: '<div>Login Page</div>' },
      },
    ],
  });
}

// 创建一个测试用的Pinia实例
export function createTestPinia() {
  return createPinia();
}

// 自定义渲染函数，包含全局插件
export function renderWithPlugins(component: Component, options = {}) {
  const router = createTestRouter();
  const pinia = createTestPinia();
  
  return render(component, {
    global: {
      plugins: [router, pinia],
    },
    ...options,
  });
}