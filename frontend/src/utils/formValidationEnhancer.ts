/**
 * 表单验证增强工具
 * 提供更强大的表单验证和错误处理功能
 */

import { ElMessage } from 'element-plus'

// 验证规则接口
interface ValidationRule {
  required?: boolean
  message?: string
  trigger?: string | string[]
  validator?: (rule: any, value: any, callback: Function) => void
  min?: number
  max?: number
  pattern?: RegExp
  type?: string
}

// 验证结果接口
interface ValidationResult {
  valid: boolean
  errors: string[]
  warnings: string[]
  field?: string
}

// 表单数据接口
interface FormData {
  [key: string]: any
}

class FormValidationEnhancer {
  private validationHistory: Map<string, ValidationResult[]> = new Map()
  private fieldValidators: Map<string, ValidationRule[]> = new Map()
  
  /**
   * 注册字段验证器
   * @param field 字段名
   * @param rules 验证规则
   */
  registerFieldValidator(field: string, rules: ValidationRule[]): void {
    this.fieldValidators.set(field, rules)
  }
  
  /**
   * 验证单个字段
   * @param field 字段名
   * @param value 字段值
   * @param rules 验证规则（可选，如果不提供则使用注册的规则）
   * @returns 验证结果
   */
  validateField(field: string, value: any, rules?: ValidationRule[]): ValidationResult {
    const validationRules = rules || this.fieldValidators.get(field) || []
    const result: ValidationResult = {
      valid: true,
      errors: [],
      warnings: [],
      field
    }
    
    for (const rule of validationRules) {
      const fieldResult = this.applyRule(field, value, rule)
      if (!fieldResult.valid) {
        result.valid = false
        result.errors.push(...fieldResult.errors)
        result.warnings.push(...fieldResult.warnings)
      }
    }
    
    // 记录验证历史
    this.recordValidationHistory(field, result)
    
    return result
  }
  
  /**
   * 验证整个表单
   * @param formData 表单数据
   * @param fieldRules 字段规则映射
   * @returns 验证结果
   */
  validateForm(formData: FormData, fieldRules?: Map<string, ValidationRule[]>): ValidationResult {
    const result: ValidationResult = {
      valid: true,
      errors: [],
      warnings: []
    }
    
    // 合并字段规则
    const allRules = new Map([...this.fieldValidators])
    if (fieldRules) {
      for (const [field, rules] of fieldRules) {
        allRules.set(field, rules)
      }
    }
    
    // 验证每个字段
    for (const [field, rules] of allRules) {
      const value = formData[field]
      const fieldResult = this.validateField(field, value, rules)
      
      if (!fieldResult.valid) {
        result.valid = false
        result.errors.push(...fieldResult.errors.map(err => `${field}: ${err}`))
        result.warnings.push(...fieldResult.warnings.map(warn => `${field}: ${warn}`))
      }
    }
    
    return result
  }
  
  /**
   * 应用单个验证规则
   * @param field 字段名
   * @param value 字段值
   * @param rule 验证规则
   * @returns 验证结果
   */
  private applyRule(field: string, value: any, rule: ValidationRule): ValidationResult {
    const result: ValidationResult = {
      valid: true,
      errors: [],
      warnings: [],
      field
    }
    
    // 必填验证
    if (rule.required && this.isEmpty(value)) {
      result.valid = false
      result.errors.push(rule.message || `${field}是必填项`)
      return result
    }
    
    // 如果值为空且不是必填，跳过其他验证
    if (this.isEmpty(value) && !rule.required) {
      return result
    }
    
    // 类型验证
    if (rule.type && !this.validateType(value, rule.type)) {
      result.valid = false
      result.errors.push(rule.message || `${field}类型不正确`)
      return result
    }
    
    // 长度验证
    if (rule.min !== undefined || rule.max !== undefined) {
      const length = this.getValueLength(value)
      if (rule.min !== undefined && length < rule.min) {
        result.valid = false
        result.errors.push(rule.message || `${field}长度不能少于${rule.min}`)
      }
      if (rule.max !== undefined && length > rule.max) {
        result.valid = false
        result.errors.push(rule.message || `${field}长度不能超过${rule.max}`)
      }
    }
    
    // 正则验证
    if (rule.pattern && !rule.pattern.test(String(value))) {
      result.valid = false
      result.errors.push(rule.message || `${field}格式不正确`)
    }
    
    // 自定义验证器
    if (rule.validator) {
      try {
        rule.validator(rule, value, (error?: string) => {
          if (error) {
            result.valid = false
            result.errors.push(error)
          }
        })
      } catch (error) {
        result.valid = false
        result.errors.push(`${field}验证失败: ${error}`)
      }
    }
    
    return result
  }
  
  /**
   * 检查值是否为空
   * @param value 值
   * @returns 是否为空
   */
  private isEmpty(value: any): boolean {
    if (value === null || value === undefined) return true
    if (typeof value === 'string') return value.trim() === ''
    if (Array.isArray(value)) return value.length === 0
    if (typeof value === 'object') return Object.keys(value).length === 0
    return false
  }
  
  /**
   * 验证类型
   * @param value 值
   * @param type 类型
   * @returns 是否符合类型
   */
  private validateType(value: any, type: string): boolean {
    switch (type) {
      case 'string':
        return typeof value === 'string'
      case 'number':
        return typeof value === 'number' && !isNaN(value)
      case 'boolean':
        return typeof value === 'boolean'
      case 'array':
        return Array.isArray(value)
      case 'object':
        return typeof value === 'object' && value !== null && !Array.isArray(value)
      case 'email':
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value))
      case 'url':
        try {
          new URL(String(value))
          return true
        } catch {
          return false
        }
      case 'date':
        return value instanceof Date || !isNaN(Date.parse(String(value)))
      default:
        return true
    }
  }
  
  /**
   * 获取值的长度
   * @param value 值
   * @returns 长度
   */
  private getValueLength(value: any): number {
    if (typeof value === 'string') return value.length
    if (Array.isArray(value)) return value.length
    if (typeof value === 'number') return String(value).length
    return 0
  }
  
  /**
   * 记录验证历史
   * @param field 字段名
   * @param result 验证结果
   */
  private recordValidationHistory(field: string, result: ValidationResult): void {
    const history = this.validationHistory.get(field) || []
    history.push({
      ...result,
      field
    })
    
    // 只保留最近10次验证记录
    if (history.length > 10) {
      history.shift()
    }
    
    this.validationHistory.set(field, history)
  }
  
  /**
   * 获取字段验证历史
   * @param field 字段名
   * @returns 验证历史
   */
  getValidationHistory(field: string): ValidationResult[] {
    return this.validationHistory.get(field) || []
  }
  
  /**
   * 清除验证历史
   * @param field 字段名（可选，如果不提供则清除所有）
   */
  clearValidationHistory(field?: string): void {
    if (field) {
      this.validationHistory.delete(field)
    } else {
      this.validationHistory.clear()
    }
  }
  
  /**
   * 获取字段错误统计
   * @param field 字段名
   * @returns 错误统计
   */
  getFieldErrorStats(field: string): {
    totalValidations: number
    errorCount: number
    errorRate: number
    commonErrors: string[]
  } {
    const history = this.getValidationHistory(field)
    const totalValidations = history.length
    const errorCount = history.filter(h => !h.valid).length
    const errorRate = totalValidations > 0 ? errorCount / totalValidations : 0
    
    // 统计常见错误
    const errorMap = new Map<string, number>()
    history.forEach(h => {
      h.errors.forEach(error => {
        errorMap.set(error, (errorMap.get(error) || 0) + 1)
      })
    })
    
    const commonErrors = Array.from(errorMap.entries())
      .sort((a, b) => b[1] - a[1])
      .slice(0, 3)
      .map(([error]) => error)
    
    return {
      totalValidations,
      errorCount,
      errorRate,
      commonErrors
    }
  }
}

// 时间线表单专用验证器
export class TimelineFormValidator extends FormValidationEnhancer {
  constructor() {
    super()
    this.initializeTimelineRules()
  }
  
  /**
   * 初始化时间线表单验证规则
   */
  private initializeTimelineRules(): void {
    // 时间线名称验证
    this.registerFieldValidator('name', [
      { required: true, message: '请输入时间线名称' },
      { min: 2, max: 100, message: '名称长度应在2-100个字符之间' },
      { pattern: /^[^<>'"&]*$/, message: '名称不能包含特殊字符' },
      {
        validator: (rule, value, callback) => {
          // 检查是否包含敏感词
          const sensitiveWords = ['测试', 'test', 'demo']
          const lowerValue = String(value).toLowerCase()
          const hasSensitiveWord = sensitiveWords.some(word => lowerValue.includes(word))
          
          if (hasSensitiveWord) {
            callback('建议使用更具描述性的名称')
          } else {
            callback()
          }
        }
      }
    ])
    
    // 时间线描述验证
    this.registerFieldValidator('description', [
      { required: true, message: '请输入时间线描述' },
      { min: 10, max: 500, message: '描述长度应在10-500个字符之间' },
      {
        validator: (rule, value, callback) => {
          // 检查描述质量
          const wordCount = String(value).split(/\s+/).length
          if (wordCount < 5) {
            callback('描述过于简单，建议提供更详细的信息')
          } else {
            callback()
          }
        }
      }
    ])
    
    // 地区选择验证
    this.registerFieldValidator('regionIds', [
      { required: true, message: '请选择至少一个地区' },
      {
        validator: (rule, value, callback) => {
          if (!Array.isArray(value) || value.length === 0) {
            callback('请选择至少一个地区')
          } else if (value.length > 10) {
            callback('选择的地区过多，建议不超过10个')
          } else {
            callback()
          }
        }
      }
    ])
    
    // 时间范围验证
    this.registerFieldValidator('timeRange', [
      { required: true, message: '请选择时间范围' },
      {
        validator: (rule, value, callback) => {
          if (!Array.isArray(value) || value.length !== 2) {
            callback('请选择完整的时间范围')
            return
          }
          
          const [startTime, endTime] = value
          const start = new Date(startTime)
          const end = new Date(endTime)
          
          if (isNaN(start.getTime()) || isNaN(end.getTime())) {
            callback('时间格式不正确')
            return
          }
          
          if (start >= end) {
            callback('开始时间必须早于结束时间')
            return
          }
          
          const timeDiff = end.getTime() - start.getTime()
          const daysDiff = timeDiff / (1000 * 60 * 60 * 24)
          
          if (daysDiff < 1) {
            callback('时间范围至少应为1天')
          } else if (daysDiff > 365) {
            callback('时间范围过大，建议不超过1年')
          } else {
            callback()
          }
        }
      }
    ])
  }
  
  /**
   * 验证时间线表单
   * @param formData 表单数据
   * @returns 验证结果
   */
  validateTimelineForm(formData: FormData): ValidationResult & {
    suggestions: string[]
  } {
    const result = this.validateForm(formData)
    const suggestions: string[] = []
    
    // 生成改进建议
    if (formData.name && formData.name.length < 10) {
      suggestions.push('建议使用更具描述性的时间线名称')
    }
    
    if (formData.description && formData.description.length < 50) {
      suggestions.push('建议提供更详细的描述信息，有助于提高事件检索的准确性')
    }
    
    if (formData.regionIds && formData.regionIds.length === 1) {
      suggestions.push('建议选择多个相关地区，可以获得更全面的事件信息')
    }
    
    if (formData.timeRange && formData.timeRange.length === 2) {
      const [startTime, endTime] = formData.timeRange
      const timeDiff = new Date(endTime).getTime() - new Date(startTime).getTime()
      const daysDiff = timeDiff / (1000 * 60 * 60 * 24)
      
      if (daysDiff > 180) {
        suggestions.push('时间范围较大，生成可能需要更长时间')
      }
    }
    
    return {
      ...result,
      suggestions
    }
  }
}

// 创建全局实例
export const formValidationEnhancer = new FormValidationEnhancer()
export const timelineFormValidator = new TimelineFormValidator()

// 显示验证错误的工具函数
export const showValidationErrors = (result: ValidationResult): void => {
  if (result.errors.length > 0) {
    result.errors.forEach(error => {
      ElMessage.error(error)
    })
  }
  
  if (result.warnings.length > 0) {
    result.warnings.forEach(warning => {
      ElMessage.warning(warning)
    })
  }
}

export default formValidationEnhancer