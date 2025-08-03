import type { Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/store/modules/auth'

/**
 * 权限指令
 * 用法：
 * v-permission="'user:create'"  // 单个权限
 * v-permission="['user:create', 'user:edit']"  // 多个权限（满足任一权限即可）
 * v-permission.all="['user:create', 'user:edit']"  // 多个权限（必须同时满足所有权限）
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value, modifiers } = binding
    const authStore = useAuthStore()
    
    // 没有传值，直接返回
    if (!value) return
    
    // 检查权限
    const hasPermission = checkPermission(value, modifiers.all)
    
    // 如果没有权限，则移除元素
    if (!hasPermission) {
      el.parentNode?.removeChild(el)
    }
    
    /**
     * 检查是否有权限
     * @param value 权限值
     * @param all 是否需要满足所有权限
     * @returns 是否有权限
     */
    function checkPermission(value: string | string[], all = false): boolean {
      const authStore = useAuthStore()
      
      // 如果没有登录，则没有权限
      if (!authStore.isLoggedIn) return false
      
      // 如果是超级管理员，则有所有权限
      if (authStore.roles.includes('admin')) return true
      
      // 如果是字符串，则检查单个权限
      if (typeof value === 'string') {
        return authStore.hasPermission(value)
      }
      
      // 如果是数组，则检查多个权限
      if (Array.isArray(value)) {
        if (all) {
          // 必须满足所有权限
          return value.every(permission => authStore.hasPermission(permission))
        } else {
          // 满足任一权限即可
          return value.some(permission => authStore.hasPermission(permission))
        }
      }
      
      return false
    }
  }
}

/**
 * 角色指令
 * 用法：
 * v-role="'admin'"  // 单个角色
 * v-role="['admin', 'editor']"  // 多个角色（满足任一角色即可）
 * v-role.all="['admin', 'editor']"  // 多个角色（必须同时满足所有角色）
 */
export const role: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value, modifiers } = binding
    const authStore = useAuthStore()
    
    // 没有传值，直接返回
    if (!value) return
    
    // 检查角色
    const hasRole = checkRole(value, modifiers.all)
    
    // 如果没有角色，则移除元素
    if (!hasRole) {
      el.parentNode?.removeChild(el)
    }
    
    /**
     * 检查是否有角色
     * @param value 角色值
     * @param all 是否需要满足所有角色
     * @returns 是否有角色
     */
    function checkRole(value: string | string[], all = false): boolean {
      const authStore = useAuthStore()
      
      // 如果没有登录，则没有角色
      if (!authStore.isLoggedIn) return false
      
      // 如果是字符串，则检查单个角色
      if (typeof value === 'string') {
        return authStore.hasRole(value)
      }
      
      // 如果是数组，则检查多个角色
      if (Array.isArray(value)) {
        if (all) {
          // 必须满足所有角色
          return value.every(role => authStore.hasRole(role))
        } else {
          // 满足任一角色即可
          return value.some(role => authStore.hasRole(role))
        }
      }
      
      return false
    }
  }
}