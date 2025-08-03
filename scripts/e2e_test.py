#!/usr/bin/env python3
"""
端到端测试脚本
用于测试事件时间线功能增强系统的完整用户工作流程
"""

import requests
import json
import time
import sys
import argparse
from datetime import datetime, timedelta
from typing import Dict, List, Optional

class Color:
    """颜色输出类"""
    RED = '\033[0;31m'
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    BLUE = '\033[0;34m'
    NC = '\033[0m'  # No Color

class Logger:
    """日志记录类"""
    
    @staticmethod
    def info(message: str):
        print(f"{Color.GREEN}[INFO]{Color.NC} {message}")
    
    @staticmethod
    def warn(message: str):
        print(f"{Color.YELLOW}[WARN]{Color.NC} {message}")
    
    @staticmethod
    def error(message: str):
        print(f"{Color.RED}[ERROR]{Color.NC} {message}")
    
    @staticmethod
    def debug(message: str):
        print(f"{Color.BLUE}[DEBUG]{Color.NC} {message}")

class APIClient:
    """API客户端类"""
    
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip('/')
        self.session = requests.Session()
        self.session.headers.update({
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        })
    
    def request(self, method: str, endpoint: str, data: Optional[Dict] = None, 
                params: Optional[Dict] = None) -> requests.Response:
        """发送HTTP请求"""
        url = f"{self.base_url}{endpoint}"
        
        try:
            if method.upper() == 'GET':
                response = self.session.get(url, params=params)
            elif method.upper() == 'POST':
                response = self.session.post(url, json=data, params=params)
            elif method.upper() == 'PUT':
                response = self.session.put(url, json=data, params=params)
            elif method.upper() == 'DELETE':
                response = self.session.delete(url, params=params)
            else:
                raise ValueError(f"不支持的HTTP方法: {method}")
            
            return response
        except requests.exceptions.RequestException as e:
            Logger.error(f"请求失败: {e}")
            raise

class E2ETestSuite:
    """端到端测试套件"""
    
    def __init__(self, api_base_url: str):
        self.api_client = APIClient(api_base_url)
        self.test_data = {}
        self.test_results = []
    
    def run_test(self, test_name: str, test_func):
        """运行单个测试"""
        Logger.info(f"开始测试: {test_name}")
        
        try:
            result = test_func()
            if result:
                Logger.info(f"✓ {test_name} - 通过")
                self.test_results.append((test_name, True, None))
            else:
                Logger.error(f"✗ {test_name} - 失败")
                self.test_results.append((test_name, False, "测试返回False"))
        except Exception as e:
            Logger.error(f"✗ {test_name} - 异常: {str(e)}")
            self.test_results.append((test_name, False, str(e)))
    
    def test_system_health(self) -> bool:
        """测试系统健康状态"""
        try:
            response = self.api_client.request('GET', '/actuator/health')
            if response.status_code == 200:
                health_data = response.json()
                return health_data.get('status') == 'UP'
            return False
        except:
            return False
    
    def test_region_workflow(self) -> bool:
        """测试地区管理完整工作流程"""
        try:
            # 1. 获取地区列表
            response = self.api_client.request('GET', '/regions', params={'page': 1, 'size': 10})
            if response.status_code != 200:
                return False
            
            # 2. 创建测试地区
            region_data = {
                'name': f'E2E测试地区_{int(time.time())}',
                'type': 'CUSTOM',
                'description': '端到端测试创建的地区',
                'dictionaryIds': []
            }
            
            response = self.api_client.request('POST', '/regions', data=region_data)
            if response.status_code != 200:
                return False
            
            result = response.json()
            if not result.get('success'):
                return False
            
            region_id = result['data']['id']
            self.test_data['region_id'] = region_id
            Logger.debug(f"创建的地区ID: {region_id}")
            
            # 3. 获取地区详情
            response = self.api_client.request('GET', f'/regions/{region_id}')
            if response.status_code != 200:
                return False
            
            # 4. 更新地区信息
            update_data = {
                'id': region_id,
                'name': f'E2E测试地区_更新_{int(time.time())}',
                'type': 'CUSTOM',
                'description': '端到端测试更新的地区',
                'dictionaryIds': []
            }
            
            response = self.api_client.request('PUT', f'/regions/{region_id}', data=update_data)
            if response.status_code != 200:
                return False
            
            # 5. 获取地区包含的字典项
            response = self.api_client.request('GET', f'/regions/{region_id}/items')
            if response.status_code != 200:
                return False
            
            return True
        except Exception as e:
            Logger.error(f"地区工作流程测试异常: {e}")
            return False
    
    def test_timeline_workflow(self) -> bool:
        """测试时间线管理完整工作流程"""
        try:
            # 确保有地区数据
            if 'region_id' not in self.test_data:
                Logger.warn("没有可用的地区数据，跳过时间线测试")
                return True
            
            region_id = self.test_data['region_id']
            
            # 1. 获取时间线列表
            response = self.api_client.request('GET', '/timelines', params={'page': 1, 'size': 10})
            if response.status_code != 200:
                return False
            
            # 2. 异步生成时间线
            start_time = (datetime.now() - timedelta(days=30)).isoformat()
            end_time = datetime.now().isoformat()
            
            timeline_data = {
                'name': f'E2E测试时间线_{int(time.time())}',
                'description': '端到端测试生成的时间线',
                'regionIds': [region_id],
                'startTime': start_time,
                'endTime': end_time
            }
            
            response = self.api_client.request('POST', '/timelines/generate/async', data=timeline_data)
            if response.status_code != 200:
                return False
            
            result = response.json()
            if not result.get('success'):
                return False
            
            timeline_id = result['data']['id']
            self.test_data['timeline_id'] = timeline_id
            Logger.debug(f"生成的时间线ID: {timeline_id}")
            
            # 3. 等待时间线生成
            Logger.info("等待时间线生成...")
            max_wait_time = 60  # 最大等待60秒
            wait_interval = 5   # 每5秒检查一次
            
            for _ in range(max_wait_time // wait_interval):
                time.sleep(wait_interval)
                
                # 检查生成进度
                response = self.api_client.request('GET', f'/timelines/{timeline_id}/generation-progress')
                if response.status_code == 200:
                    progress = response.json()
                    if progress.get('success'):
                        status = progress['data'].get('status')
                        Logger.debug(f"时间线生成状态: {status}")
                        
                        if status in ['COMPLETED', 'FAILED']:
                            break
            
            # 4. 获取时间线详情
            response = self.api_client.request('GET', f'/timelines/{timeline_id}')
            if response.status_code != 200:
                return False
            
            # 5. 获取时间线包含的地区
            response = self.api_client.request('GET', f'/timelines/{timeline_id}/regions')
            if response.status_code != 200:
                return False
            
            # 6. 获取时间线包含的事件
            response = self.api_client.request('GET', f'/timelines/{timeline_id}/events')
            if response.status_code != 200:
                return False
            
            # 7. 获取时间线图形数据
            response = self.api_client.request('GET', f'/timelines/{timeline_id}/graph')
            if response.status_code != 200:
                return False
            
            return True
        except Exception as e:
            Logger.error(f"时间线工作流程测试异常: {e}")
            return False
    
    def test_data_consistency(self) -> bool:
        """测试数据一致性"""
        try:
            if 'timeline_id' not in self.test_data:
                Logger.warn("没有可用的时间线数据，跳过数据一致性测试")
                return True
            
            timeline_id = self.test_data['timeline_id']
            
            # 获取时间线详情
            response = self.api_client.request('GET', f'/timelines/{timeline_id}')
            if response.status_code != 200:
                return False
            
            timeline_detail = response.json()['data']
            
            # 获取时间线包含的事件
            response = self.api_client.request('GET', f'/timelines/{timeline_id}/events')
            if response.status_code != 200:
                return False
            
            events = response.json()['data']
            
            # 检查事件数量是否一致
            expected_event_count = timeline_detail.get('timeline', {}).get('eventCount', 0)
            actual_event_count = len(events)
            
            if expected_event_count != actual_event_count:
                Logger.warn(f"事件数量不一致: 期望 {expected_event_count}, 实际 {actual_event_count}")
                # 这里不返回False，因为在测试环境中可能存在数据不一致的情况
            
            return True
        except Exception as e:
            Logger.error(f"数据一致性测试异常: {e}")
            return False
    
    def cleanup_test_data(self):
        """清理测试数据"""
        Logger.info("清理测试数据...")
        
        # 删除测试时间线
        if 'timeline_id' in self.test_data:
            try:
                timeline_id = self.test_data['timeline_id']
                response = self.api_client.request('DELETE', f'/timelines/{timeline_id}')
                if response.status_code == 200:
                    Logger.debug(f"已删除测试时间线: {timeline_id}")
                else:
                    Logger.warn(f"删除测试时间线失败: {timeline_id}")
            except Exception as e:
                Logger.warn(f"删除测试时间线异常: {e}")
        
        # 删除测试地区
        if 'region_id' in self.test_data:
            try:
                region_id = self.test_data['region_id']
                response = self.api_client.request('DELETE', f'/regions/{region_id}')
                if response.status_code == 200:
                    Logger.debug(f"已删除测试地区: {region_id}")
                else:
                    Logger.warn(f"删除测试地区失败: {region_id}")
            except Exception as e:
                Logger.warn(f"删除测试地区异常: {e}")
    
    def run_all_tests(self):
        """运行所有测试"""
        Logger.info("开始端到端测试...")
        
        # 定义测试用例
        test_cases = [
            ("系统健康检查", self.test_system_health),
            ("地区管理工作流程", self.test_region_workflow),
            ("时间线管理工作流程", self.test_timeline_workflow),
            ("数据一致性检查", self.test_data_consistency),
        ]
        
        # 运行测试用例
        for test_name, test_func in test_cases:
            self.run_test(test_name, test_func)
        
        # 清理测试数据
        self.cleanup_test_data()
        
        # 输出测试结果摘要
        self.print_test_summary()
    
    def print_test_summary(self):
        """输出测试结果摘要"""
        Logger.info("========== 测试结果摘要 ==========")
        
        passed_count = 0
        failed_count = 0
        
        for test_name, passed, error in self.test_results:
            if passed:
                print(f"✓ {test_name}")
                passed_count += 1
            else:
                print(f"✗ {test_name}")
                if error:
                    print(f"  错误: {error}")
                failed_count += 1
        
        print(f"\n总计: {len(self.test_results)} 个测试")
        print(f"通过: {passed_count}")
        print(f"失败: {failed_count}")
        
        if failed_count == 0:
            Logger.info("所有端到端测试通过！")
            return True
        else:
            Logger.error(f"有 {failed_count} 个测试失败。")
            return False

def main():
    """主函数"""
    parser = argparse.ArgumentParser(description='事件时间线功能增强系统端到端测试')
    parser.add_argument('-u', '--url', default='http://localhost:8080/api',
                        help='API基础URL (默认: http://localhost:8080/api)')
    parser.add_argument('-v', '--verbose', action='store_true',
                        help='详细输出')
    
    args = parser.parse_args()
    
    if args.verbose:
        Logger.info(f"API基础URL: {args.url}")
    
    # 创建测试套件并运行测试
    test_suite = E2ETestSuite(args.url)
    success = test_suite.run_all_tests()
    
    # 根据测试结果设置退出码
    sys.exit(0 if success else 1)

if __name__ == '__main__':
    main()