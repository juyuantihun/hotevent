import type { App } from 'vue'
import { permission, role } from './permission'

// 注册所有自定义指令
export function registerDirectives(app: App) {
  // 注册权限指令
  app.directive('permission', permission)
  
  // 注册角色指令
  app.directive('role', role)
}