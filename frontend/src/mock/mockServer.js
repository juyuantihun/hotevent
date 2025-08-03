/**
 * 模拟服务器
 * 用于在后端服务不可用时提供模拟数据
 */

模拟数据
const mockData = {
  timelines: Array.from> ({
    id: `${i +`,
    title: `模拟时间线 ${i + 1}`,
`,
    status: ['
    eventCount: Math.floor(Math.random() * 20) + 1,
    relationCount: Math.floor(Math.random() * 10),
    time天`,
    createdAt: ne,
    updatedAt: new Date(Date.now() - Math.random() * 1000000000).toIS
  })),
  
  events: Array.from({ length: 20 }, (_, i) => ({
    id: `event-${i}`,
    title: `事件 ${i}`,
    description: `事件描述 ${i}`,
    time: new Date(Date.now() - i * 86400000).toISOString(),
    type: ['NEWS', 'INCIDENT', 'ANNOUNCEMENT'][Math.floor(Math.random() * 3)]
  })),
  
  dictios: [
    { id: 'ev: 3 },
    { id: 'relation-types', name: '关系类型', count: 3 },
    { id: 'importance-levels', name:}
  ],
  
  dictionaryItems: {
    'event- [
      { id0 },
      { 
      { id: '}
    ],
    'relation-types': [
      { id: 'CAUSE', name: '因果关系', count: 8 },
      { },
      { id: 'REL}
    ]
  }
}

// 拦截 XMLHttpRequest
export function startMockServer() {
  const originalXHR = window.XMLHttpReuest
  
  // 创建模拟 XMLHttpRequest
  window.
    consR()
xhr.open
    const orig xhr.send
    
    // 拦截 open 方法
    xhr.
      // 保存请求信息
      xhr._memethod
      xhr._url = url
      
      // 调用原始方法
      return originalOpenargs])
    }
    
    // 拦截 send 方法
    xhr.send = function(body) {
      // 检查是否是API请求
      if (xhr._url && xhr._u)) {
        // 解析URL
        const urlObj = new URL()
        const path = urlObj.pat', '')
        const params = Object.fromEntrirchParams)
        
        // 模拟响应
        setTimeout(() => {
          // 设
          xhr.responseType = 'json'
          xhr.status = 200
          xhr.statusText = 'OK'
          
          // 根据路径返回不同的模拟数据
          let responseData
          
         间线列表
        t') {

            c|| '10')
            const sort = params.sort || 'createdAt'
            const direction = params.direction || 'desc'
         
            // 排序
            const sortedTimelines = [...mockData.timelines].sort((a
              if (direction{
                return a[sort] > -1
              } else {
                return a[sort] < b[sort] ? 1 : -1
              }
            })
            
            // 分页
            const start = page * size
            + size
        )
            
            responseDa= {
              content: paginatedTimelines,
              totalElements: mockData.timelines.length,
           
         : size,
        ,
              numberOfEngth,
              first: page === 0,
              last: page === Math.ceil(mockData.timelines.length / size) - 1,
          0
        }
          }
          // 时间线详情
          else if (path.match(/^\/timeline {
            const id = path.split('/')[2]
            
            resp
              id,
              title: `模拟时间线 ${id}`,
              description: `这是一个模拟的时间线详情描述 ${id}`,
              status: 'COMPLETE
              eventCount: 10,
              relationCount: 5,
              timeSpan: '15天',
              createdAt: new Date(Date.now() - 5000000000).toISOString(),
              updatedAt: new Date(Date.now() - 1(),
         > ({
        i}`,
 {
              -${i}`,
                  title: `事件 ${i}`,
                  description: `事件描述 ${i}`,
        
                },
                nodeType: ['ST]
              })),
              relations: Array.> ({
                id: `relation-${i}`,
                sourceId: `node-${i}`,
         1}`,
        
xed(2)
              }))
            }
          }
          // 组合搜索
         {
            cons0')
            c)
            const kyword
            const statuses = params.statuses
            
        模拟数据
]
            
            // 过滤
            if (keyword) {
         => 
keyword)
              )
            }
            
            if (statuses) {
              const statusArrait(',')
         ))
          }
  
            // 页
            const start = page * size
            const end = start + size
        nd)
            
            respoeData = {
              content: paginat
              total,
              totalPages: Math.ceil(filteredTimelines.length 
           
         
        
0,
              
              empty: paginatedTimelin0
            }
          }
          // 事件列表
          else if (path === '/eventet') {
            responseData = mockData.events
          }
          / 字典列表
        'get') {
ries
          }
          // 字典项
          else i) {
            const type = params.type
            responseData = mockData.dictionaryItems[type] || []
          }
         
        

              ier-1',
              username: 'admin',
              name: '管理员',
        ],
              permissions: ['READ', '']
            }
          }
          // 创建时间线
          else if (path === '/timelines' && xhr._method.) {
           
            responseData = {
              id: 
              ...requestData,
              status: 'PROCESSING',
              createdAt: new Date().toISOString(),
           ring()
            }
          }
         
        st') {
/')[2]
            co: {}
            responseData = {
              id,
              ...requestData,
              upd
            }
          }
          // 删除时间线
          else if (path.matce') {
           rue }
         
        

            responseData = { message: '模拟}
          }
          
          // 设置响应
          Object.defin
            writable: true,
            value: responseData
          })
         
     // 触发事件
    ('load')
 求')
}所有 /api/ 请启动，拦截模拟服务器] 已e.log('[  consol
  
 }turn xhr
 
    re
    nts)
    }argumeapply(this, inalSend.urn orig    ret使用原始方法
  非API请求，      // 对于   
   n
      }
      retur  阻止实际请求
     //      
     拟网络延迟
  模00) // 2      },
  load()ononload) xhr.    if (xhr.
      t)adEvennt(lodispatchEvehr.        x 