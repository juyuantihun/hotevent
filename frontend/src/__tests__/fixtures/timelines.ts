// 时间线测试数据
export const mockTimelines = [
  {
    id: '1',
    title: '美国大选时间线',
    description: '2024年美国总统大选重要事件时间线',
    region: {
      id: '101',
      name: '美国',
      code: 'USA',
    },
    eventCount: 45,
    timeSpan: '2023-01-01 至 2024-11-05',
    createdAt: '2023-12-15T08:30:00Z',
    status: 'completed', // 已完成
  },
  {
    id: '2',
    title: '欧洲经济峰会',
    description: '2024年欧洲经济峰会及相关事件',
    region: {
      id: '201',
      name: '欧盟',
      code: 'EU',
    },
    eventCount: 28,
    timeSpan: '2024-03-15 至 2024-06-30',
    createdAt: '2024-02-20T14:15:00Z',
    status: 'in_progress', // 生成中
  },
  {
    id: '3',
    title: '亚太贸易协议谈判',
    description: '亚太地区贸易协议谈判过程',
    region: {
      id: '301',
      name: '亚太地区',
      code: 'APAC',
    },
    eventCount: 0,
    timeSpan: '',
    createdAt: '2024-05-10T09:45:00Z',
    status: 'draft', // 草稿
  },
  {
    id: '4',
    title: '非洲联盟峰会',
    description: '2024年非洲联盟峰会及相关事件',
    region: {
      id: '401',
      name: '非洲联盟',
      code: 'AU',
    },
    eventCount: 0,
    timeSpan: '',
    createdAt: '2024-04-05T11:20:00Z',
    status: 'failed', // 失败
  },
];

// 单个时间线详情测试数据
export const mockTimelineDetail = {
  id: '1',
  title: '美国大选时间线',
  description: '2024年美国总统大选重要事件时间线',
  region: {
    id: '101',
    name: '美国',
    code: 'USA',
  },
  eventCount: 45,
  relationCount: 67,
  timeSpan: '2023-01-01 至 2024-11-05',
  createdAt: '2023-12-15T08:30:00Z',
  updatedAt: '2024-06-20T16:45:00Z',
  status: 'completed',
  events: [
    {
      id: '101',
      title: '候选人宣布参选',
      description: '主要政党候选人宣布参加2024年总统大选',
      date: '2023-04-25T00:00:00Z',
      location: '华盛顿特区',
      type: 'political',
      importance: 'high',
    },
    {
      id: '102',
      title: '第一场电视辩论',
      description: '总统候选人首次电视辩论',
      date: '2024-06-10T00:00:00Z',
      location: '纽约',
      type: 'debate',
      importance: 'high',
    },
    {
      id: '103',
      title: '民调结果发布',
      description: '全国性民意调查结果发布',
      date: '2024-07-15T00:00:00Z',
      location: '全国',
      type: 'poll',
      importance: 'medium',
    },
  ],
  relations: [
    {
      id: '201',
      sourceEventId: '101',
      targetEventId: '102',
      type: 'leads_to',
      description: '候选人宣布参选后参加首次电视辩论',
    },
    {
      id: '202',
      sourceEventId: '102',
      targetEventId: '103',
      type: 'influences',
      description: '电视辩论影响了民调结果',
    },
  ],
};

// 地区测试数据
export const mockRegions = [
  {
    id: '101',
    name: '美国',
    code: 'USA',
    parentId: null,
    level: 1,
    children: [
      {
        id: '10101',
        name: '加利福尼亚州',
        code: 'CA',
        parentId: '101',
        level: 2,
        children: [],
      },
      {
        id: '10102',
        name: '纽约州',
        code: 'NY',
        parentId: '101',
        level: 2,
        children: [],
      },
    ],
  },
  {
    id: '201',
    name: '欧盟',
    code: 'EU',
    parentId: null,
    level: 1,
    children: [
      {
        id: '20101',
        name: '德国',
        code: 'DE',
        parentId: '201',
        level: 2,
        children: [],
      },
      {
        id: '20102',
        name: '法国',
        code: 'FR',
        parentId: '201',
        level: 2,
        children: [],
      },
    ],
  },
];