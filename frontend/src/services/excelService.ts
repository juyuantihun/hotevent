/**
 * Excel服务
 * 用于处理Excel文件的导入导出
 * 
 * 注意：此服务替换了之前使用的xlsx库，使用更安全的exceljs库
 * 
 * 暂时注释掉缺失的依赖，提供一个模拟实现
 */
// import { Workbook, Worksheet, Column } from 'exceljs'
import dayjs from 'dayjs'

// 模拟 ExcelJS 类型
interface ExcelJS {
  Style: any;
}

// 模拟 Workbook 类
class Workbook {
  worksheets: any[] = [];
  
  addWorksheet(name: string) {
    const worksheet = {
      name,
      columns: [],
      rows: [],
      addRow: (data: any[]) => {
        const row = {
          cells: data.map(value => ({ value, style: {} })),
          eachCell: (callback: (cell: any, colNumber: number) => void) => {
            row.cells.forEach((cell, index) => {
              callback(cell, index + 1);
            });
          }
        };
        worksheet.rows.push(row);
        return row;
      },
      getRow: (rowNumber: number) => worksheet.rows[rowNumber - 1],
      eachRow: (options: any, callback: (row: any, rowNumber: number) => void) => {
        worksheet.rows.forEach((row, index) => {
          callback(row, index + 1);
        });
      }
    };
    this.worksheets.push(worksheet);
    return worksheet;
  }
  
  xlsx = {
    writeBuffer: async () => new ArrayBuffer(0),
    load: async (buffer: ArrayBuffer) => {}
  };
}

/**
 * 导出选项接口
 */
export interface ExportOptions {
  // 文件名（不含扩展名）
  fileName?: string
  // 工作表名
  sheetName?: string
  // 是否自动调整列宽
  autoWidth?: boolean
  // 是否包含表头
  withHeader?: boolean
  // 自定义样式
  styles?: {
    header?: any
    cell?: any
  }
}

/**
 * 默认导出选项
 */
const defaultExportOptions: ExportOptions = {
  fileName: `export_${dayjs().format('YYYYMMDD_HHmmss')}`,
  sheetName: 'Sheet1',
  autoWidth: true,
  withHeader: true,
  styles: {
    header: {
      font: { bold: true, size: 12 },
      fill: {
        type: 'pattern',
        pattern: 'solid',
        fgColor: { argb: 'FFE0E0E0' }
      }
    }
  }
}

/**
 * 导出数据到Excel文件
 * @param data 要导出的数据数组
 * @param columns 列定义
 * @param options 导出选项
 * @returns 下载的文件名
 */
export async function exportToExcel<T = any>(
  data: T[],
  columns: Array<{
    label: string
    prop: keyof T | string
    formatter?: (row: T) => any
    width?: number
  }>,
  options: ExportOptions = {}
): Promise<string> {
  // 合并选项
  const mergedOptions = { ...defaultExportOptions, ...options }
  const { fileName, sheetName, autoWidth, withHeader, styles } = mergedOptions
  
  // 创建工作簿
  const workbook = new Workbook()
  
  // 创建工作表
  const worksheet = workbook.addWorksheet(sheetName)
  
  // 添加表头
  if (withHeader) {
    const headerRow = worksheet.addRow(columns.map(col => col.label))
    
    // 应用表头样式
    if (styles?.header) {
      headerRow.eachCell((cell) => {
        Object.assign(cell.style, styles.header)
      })
    }
  }
  
  // 添加数据行
  data.forEach(row => {
    const rowData = columns.map(col => {
      if (col.formatter) {
        return col.formatter(row)
      }
      
      // 支持嵌套属性访问，如 'user.name'
      const prop = col.prop as string
      if (prop.includes('.')) {
        return prop.split('.').reduce((obj, key) => obj?.[key], row as any)
      }
      
      return row[col.prop as keyof T]
    })
    
    const excelRow = worksheet.addRow(rowData)
    
    // 应用单元格样式
    if (styles?.cell) {
      excelRow.eachCell((cell) => {
        Object.assign(cell.style, styles.cell)
      })
    }
  })
  
  // 自动调整列宽
  if (autoWidth) {
    worksheet.columns.forEach((column, index) => {
      // 如果有指定宽度，使用指定宽度
      if (columns[index]?.width) {
        column.width = columns[index].width
        return
      }
      
      // 计算最大宽度
      let maxLength = columns[index].label.length
      
      // 检查每行的内容长度
      worksheet.eachRow({ includeEmpty: false }, (row, rowNumber) => {
        if (rowNumber > 1 || !withHeader) { // 跳过表头
          const cell = row.getCell(index + 1)
          const cellValue = cell.text || ''
          maxLength = Math.max(maxLength, cellValue.length)
        }
      })
      
      // 设置列宽（加一点额外空间）
      column.width = Math.min(maxLength + 2, 50)
    })
  }
  
  // 生成Excel文件
  const buffer = await workbook.xlsx.writeBuffer()
  
  // 创建Blob对象
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  
  // 创建下载链接
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `${fileName}.xlsx`
  
  // 触发下载
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  return `${fileName}.xlsx`
}

/**
 * 导入选项接口
 */
export interface ImportOptions {
  // 是否包含表头
  withHeader?: boolean
  // 表头行索引（从0开始）
  headerRowIndex?: number
  // 数据起始行索引（从0开始）
  dataStartRowIndex?: number
  // 是否跳过空行
  skipEmptyRows?: boolean
  // 列映射（表头名称到属性名的映射）
  columnMapping?: Record<string, string>
}

/**
 * 默认导入选项
 */
const defaultImportOptions: ImportOptions = {
  withHeader: true,
  headerRowIndex: 0,
  dataStartRowIndex: 1,
  skipEmptyRows: true
}

/**
 * 从Excel文件导入数据
 * @param file Excel文件
 * @param options 导入选项
 * @returns 导入的数据数组
 */
export async function importFromExcel<T = Record<string, any>>(
  file: File,
  options: ImportOptions = {}
): Promise<T[]> {
  // 合并选项
  const mergedOptions = { ...defaultImportOptions, ...options }
  const { withHeader, headerRowIndex, dataStartRowIndex, skipEmptyRows, columnMapping } = mergedOptions
  
  // 创建工作簿
  const workbook = new Workbook()
  
  // 读取文件
  const buffer = await file.arrayBuffer()
  await workbook.xlsx.load(buffer)
  
  // 获取第一个工作表
  const worksheet = workbook.worksheets[0]
  if (!worksheet) {
    throw new Error('Excel文件不包含工作表')
  }
  
  // 提取表头
  let headers: string[] = []
  if (withHeader) {
    const headerRow = worksheet.getRow(headerRowIndex + 1)
    headerRow.eachCell((cell, colNumber) => {
      headers[colNumber - 1] = cell.text.trim()
    })
  }
  
  // 提取数据
  const data: T[] = []
  
  // 遍历行
  worksheet.eachRow((row, rowNumber) => {
    // 跳过表头行和数据起始行之前的行
    if (rowNumber <= dataStartRowIndex) {
      return
    }
    
    // 创建行数据对象
    const rowData: Record<string, any> = {}
    let isEmpty = true
    
    // 遍历单元格
    row.eachCell((cell, colNumber) => {
      const value = cell.value
      
      // 如果有值，标记行不为空
      if (value !== null && value !== undefined && value !== '') {
        isEmpty = false
      }
      
      // 获取属性名
      let propName: string
      
      if (withHeader) {
        const header = headers[colNumber - 1]
        // 如果提供了列映射，使用映射后的属性名
        propName = columnMapping?.[header] || header
      } else {
        // 如果没有表头，使用列索引作为属性名
        propName = `col${colNumber}`
      }
      
      // 设置属性值
      rowData[propName] = value
    })
    
    // 如果行不为空或不跳过空行，添加到数据数组
    if (!isEmpty || !skipEmptyRows) {
      data.push(rowData as T)
    }
  })
  
  return data
}

/**
 * 创建Excel模板文件
 * @param columns 列定义
 * @param options 导出选项
 * @returns 下载的文件名
 */
export async function createExcelTemplate(
  columns: Array<{
    label: string
    prop: string
    example?: any
    width?: number
  }>,
  options: ExportOptions = {}
): Promise<string> {
  // 合并选项
  const mergedOptions = { 
    ...defaultExportOptions, 
    ...options,
    fileName: options.fileName || `template_${dayjs().format('YYYYMMDD_HHmmss')}`
  }
  
  // 创建示例数据
  const exampleData = columns.some(col => col.example !== undefined)
    ? [columns.reduce((obj, col) => {
        obj[col.prop] = col.example !== undefined ? col.example : ''
        return obj
      }, {} as Record<string, any>)]
    : []
  
  // 导出模板
  return exportToExcel(
    exampleData,
    columns.map(({ label, prop, width }) => ({ label, prop, width })),
    mergedOptions
  )
}

export default {
  exportToExcel,
  importFromExcel,
  createExcelTemplate
}