import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElCard, ElTag, ElButton, ElIcon } from 'element-plus'
import TimelineCard from '../../views/timeline/components/TimelineCard.vue'

// Mock Element Plus icons
vi.mock('@element-plus/icons-vue', () => ({
  Calendar: { name: 'Calendar' },
  Location: { name: 'Location' },
  User: { name: 'User' },
  Clock: { name: 'Clock' },
  View: { name: 'View' },
  Edit: { name: 'Edit' },
  Delete: { name: 'Delete' },
  Position: { name: 'Position' },
  Flag: { name: 'Flag' }
}))

describe('TimelineCard', () => {
  let wrapper: any
  
  const mockTimeline = {
    id: '1',
    title: '测试时间线',
    description: '这是一个测试时间线',
    createdAt: '2024-01-01T10:00:00Z',
    updatedAt: '2024-01-01T12:00:00Z',
    eventCount: 5,
    region: '北京',
    coordinates: { latitude: 39.9042, longitude: 116.4074 },
    subjectCoordinate: { lat: 31.2304, lng: 121.4737 },
    objectCoordinate: { lat: 22.3193, lng: 114.1694 },
    tags: ['热点', '政治']
  }

  beforeEach(() => {
    wrapper = mount(TimelineCard, {
      props: {
        timeline: mockTimeline
      },
      global: {
        components: {
          ElCard,
          ElTag,
          ElButton,
          ElIcon
        },
        stubs: {
          'el-card': true,
          'el-tag': true,
          'el-button': true,
          'el-icon': true
        }
      }
    })
  })

  it('应该正确渲染时间线卡片', () => {
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.timeline-card').exists()).toBe(true)
  })

  it('应该显示时间线标题和描述', () => {
    expect(wrapper.text()).toContain('测试时间线')
    expect(wrapper.text()).toContain('这是一个测试时间线')
  })

  it('应该显示地理坐标信息', () => {
    const coordinateSection = wrapper.find('.coordinate-section')
    expect(coordinateSection.exists()).toBe(true)
    
    const coordinateInfos = wrapper.findAll('.coordinate-info')
    expect(coordinateInfos.length).toBe(3) // 主坐标、主体坐标、客体坐标
  })

  it('应该显示事件数量', () => {
    const eventCount = wrapper.find('.event-count')
    expect(eventCount.exists()).toBe(true)
    expect(eventCount.text()).toContain('5')
  })

  it('应该显示标签', () => {
    const tags = wrapper.findAll('.timeline-tag')
    expect(tags.length).toBe(2)
  })

  it('应该支持点击查看详情', async () => {
    const viewButton = wrapper.find('[data-test="view-detail"]')
    if (viewButton.exists()) {
      await viewButton.trigger('click')
      expect(wrapper.emitted('view')).toBeTruthy()
      expect(wrapper.emitted('view')[0]).toEqual([mockTimeline])
    }
  })

  it('应该支持编辑功能', async () => {
    const editButton = wrapper.find('[data-test="edit-timeline"]')
    if (editButton.exists()) {
      await editButton.trigger('click')
      expect(wrapper.emitted('edit')).toBeTruthy()
      expect(wrapper.emitted('edit')[0]).toEqual([mockTimeline])
    }
  })

  it('应该支持删除功能', async () => {
    const deleteButton = wrapper.find('[data-test="delete-timeline"]')
    if (deleteButton.exists()) {
      await deleteButton.trigger('click')
      expect(wrapper.emitted('delete')).toBeTruthy()
      expect(wrapper.emitted('delete')[0]).toEqual([mockTimeline.id])
    }
  })

  it('应该正确格式化坐标信息', () => {
    const coordinateValues = wrapper.findAll('.coordinate-value')
    expect(coordinateValues.length).toBeGreaterThan(0)
    
    // 检查主坐标格式
    const mainCoordinate = coordinateValues.find(el => 
      el.text().includes('39.9042') && el.text().includes('116.4074')
    )
    expect(mainCoordinate).toBeTruthy()
  })

  it('应该正确格式化时间显示', () => {
    const timeElement = wrapper.find('.timeline-time')
    expect(timeElement.exists()).toBe(true)
    expect(timeElement.text()).toMatch(/2024/)
  })

  it('应该在没有坐标时隐藏坐标部分', async () => {
    const timelineWithoutCoordinates = {
      ...mockTimeline,
      coordinates: undefined,
      subjectCoordinate: undefined,
      objectCoordinate: undefined
    }
    
    await wrapper.setProps({ timeline: timelineWithoutCoordinates })
    
    const coordinateSection = wrapper.find('.coordinate-section')
    expect(coordinateSection.exists()).toBe(false)
  })
})

// 测试坐标格式化函数的边界情况
describe('TimelineCard 坐标格式化', () => {
  let wrapper: any

  const timelineWithDifferentCoordinates = {
    id: '1',
    title: '测试时间线',
    coordinates: { latitude: 39.9042, longitude: 116.4074 },
    subjectCoordinate: { lat: 31.2304, lng: 121.4737 },
    objectCoordinate: { locationName: '天安门广场' }
  }

  beforeEach(() => {
    wrapper = mount(TimelineCard, {
      props: {
        timeline: timelineWithDifferentCoordinates
      },
      global: {
        stubs: {
          'el-card': true,
          'el-tag': true,
          'el-button': true,
          'el-icon': true
        }
      }
    })
  })

  it('应该处理不同格式的坐标', () => {
    const coordinateValues = wrapper.findAll('.coordinate-value')
    
    // 检查latitude/longitude格式
    expect(coordinateValues.some(el => el.text().includes('39.9042, 116.4074'))).toBe(true)
    
    // 检查lat/lng格式
    expect(coordinateValues.some(el => el.text().includes('31.2304, 121.4737'))).toBe(true)
    
    // 检查位置名称格式
    expect(coordinateValues.some(el => el.text().includes('天安门广场'))).toBe(true)
  })
})

// 测试响应式布局
describe('TimelineCard 响应式布局', () => {
  let wrapper: any

  beforeEach(() => {
    wrapper = mount(TimelineCard, {
      props: {
        timeline: {
          id: '1',
          title: '测试时间线',
          description: '测试描述',
          createdAt: '2024-01-01T10:00:00Z'
        }
      },
      global: {
        stubs: {
          'el-card': true,
          'el-tag': true,
          'el-button': true,
          'el-icon': true
        }
      }
    })
  })

  it('应该在移动端正确显示', () => {
    // 检查响应式样式类是否存在
    const cardHeader = wrapper.find('.card-header')
    expect(cardHeader.exists()).toBe(true)
    
    const cardActions = wrapper.find('.card-actions')
    expect(cardActions.exists()).toBe(true)
  })
})