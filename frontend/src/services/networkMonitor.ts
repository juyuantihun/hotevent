/**
 * 网络状态监控服务
 * 用于监控网络状态并优化网络请求
 */

// 网络状态类型
export enum NetworkStatus {
  ONLINE = 'online',
  OFFLINE = 'offline',
  SLOW = 'slow',
  FAST = 'fast'
}

// 网络类型
export enum NetworkType {
  UNKNOWN = 'unknown',
  WIFI = 'wifi',
  CELLULAR = 'cellular',
  CELLULAR_2G = '2g',
  CELLULAR_3G = '3g',
  CELLULAR_4G = '4g',
  CELLULAR_5G = '5g',
  ETHERNET = 'ethernet'
}

// 网络状态监听器
type NetworkStatusListener = (status: NetworkStatus, type: NetworkType) => void;

// 网络状态监控类
class NetworkMonitor {
  private status: NetworkStatus = NetworkStatus.ONLINE;
  private type: NetworkType = NetworkType.UNKNOWN;
  private listeners: NetworkStatusListener[] = [];
  private pingInterval: number | null = null;
  private pingUrl: string = 'https://www.google.com/generate_204';
  private pingTimeout: number = 3000;
  private pingFrequency: number = 30000; // 30秒
  // 上次 ping 的时间戳
  private _lastPingTime: number = 0;
  private lastPingDuration: number = 0;
  private pingHistory: number[] = [];
  private maxPingHistory: number = 5;
  
  constructor() {
    // 初始化网络状态
    this.status = navigator.onLine ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE;
    
    // 获取网络类型
    this.detectNetworkType();
    
    // 监听网络状态变化
    window.addEventListener('online', this.handleOnline);
    window.addEventListener('offline', this.handleOffline);
    
    // 如果支持Network Information API，监听连接类型变化
    if ('connection' in navigator && (navigator as any).connection) {
      (navigator as any).connection.addEventListener('change', this.handleConnectionChange);
    }
  }
  
  /**
   * 获取当前网络状态
   * @returns 网络状态
   */
  public getStatus(): { status: NetworkStatus, type: NetworkType } {
    return {
      status: this.status,
      type: this.type
    };
  }
  
  /**
   * 添加网络状态监听器
   * @param listener 监听器函数
   * @returns 移除监听器的函数
   */
  public addListener(listener: NetworkStatusListener): () => void {
    this.listeners.push(listener);
    
    // 立即通知当前状态
    listener(this.status, this.type);
    
    // 返回移除监听器的函数
    return () => {
      const index = this.listeners.indexOf(listener);
      if (index !== -1) {
        this.listeners.splice(index, 1);
      }
    };
  }
  
  /**
   * 开始网络质量监控
   * @param pingUrl Ping URL
   * @param frequency Ping频率（毫秒）
   */
  public startMonitoring(pingUrl?: string, frequency?: number): void {
    if (pingUrl) {
      this.pingUrl = pingUrl;
    }
    
    if (frequency) {
      this.pingFrequency = frequency;
    }
    
    // 如果已经在监控中，先停止
    this.stopMonitoring();
    
    // 立即执行一次Ping
    this.pingServer();
    
    // 设置定时器定期Ping
    this.pingInterval = window.setInterval(() => {
      this.pingServer();
    }, this.pingFrequency);
  }
  
  /**
   * 停止网络质量监控
   */
  public stopMonitoring(): void {
    if (this.pingInterval !== null) {
      window.clearInterval(this.pingInterval);
      this.pingInterval = null;
    }
  }
  
  /**
   * 获取最后一次Ping的延迟
   * @returns Ping延迟（毫秒）
   */
  public getLastPingDuration(): number {
    return this.lastPingDuration;
  }
  
  /**
   * 获取平均Ping延迟
   * @returns 平均Ping延迟（毫秒）
   */
  public getAveragePingDuration(): number {
    if (this.pingHistory.length === 0) {
      return 0;
    }
    
    const sum = this.pingHistory.reduce((a, b) => a + b, 0);
    return sum / this.pingHistory.length;
  }
  
  /**
   * 处理在线状态
   */
  private handleOnline = (): void => {
    const oldStatus = this.status;
    this.status = NetworkStatus.ONLINE;
    
    // 检测网络类型
    this.detectNetworkType();
    
    // 如果状态发生变化，通知监听器
    if (oldStatus !== this.status) {
      this.notifyListeners();
    }
    
    // 开始Ping服务器检测网络质量
    this.pingServer();
  };
  
  /**
   * 处理离线状态
   */
  private handleOffline = (): void => {
    const oldStatus = this.status;
    this.status = NetworkStatus.OFFLINE;
    
    // 如果状态发生变化，通知监听器
    if (oldStatus !== this.status) {
      this.notifyListeners();
    }
  };
  
  /**
   * 处理连接类型变化
   */
  private handleConnectionChange = (): void => {
    // 检测网络类型
    this.detectNetworkType();
    
    // 通知监听器
    this.notifyListeners();
    
    // Ping服务器检测网络质量
    this.pingServer();
  };
  
  /**
   * 检测网络类型
   */
  private detectNetworkType(): void {
    // 如果支持Network Information API
    if ('connection' in navigator && (navigator as any).connection) {
      const connection = (navigator as any).connection;
      
      // 获取连接类型
      if (connection.type) {
        switch (connection.type) {
          case 'wifi':
            this.type = NetworkType.WIFI;
            break;
          case 'cellular':
            // 如果有effectiveType属性，可以获取更详细的移动网络类型
            if (connection.effectiveType) {
              switch (connection.effectiveType) {
                case 'slow-2g':
                case '2g':
                  this.type = NetworkType.CELLULAR_2G;
                  break;
                case '3g':
                  this.type = NetworkType.CELLULAR_3G;
                  break;
                case '4g':
                  this.type = NetworkType.CELLULAR_4G;
                  break;
                default:
                  this.type = NetworkType.CELLULAR;
              }
            } else {
              this.type = NetworkType.CELLULAR;
            }
            break;
          case 'ethernet':
            this.type = NetworkType.ETHERNET;
            break;
          default:
            this.type = NetworkType.UNKNOWN;
        }
      } else if (connection.effectiveType) {
        // 如果只有effectiveType属性
        switch (connection.effectiveType) {
          case 'slow-2g':
          case '2g':
            this.type = NetworkType.CELLULAR_2G;
            break;
          case '3g':
            this.type = NetworkType.CELLULAR_3G;
            break;
          case '4g':
            this.type = NetworkType.CELLULAR_4G;
            break;
          default:
            this.type = NetworkType.UNKNOWN;
        }
      }
    }
  }
  
  /**
   * Ping服务器检测网络质量
   */
  private pingServer(): void {
    // 如果离线，不执行Ping
    if (this.status === NetworkStatus.OFFLINE) {
      return;
    }
    
    const startTime = Date.now();
    
    // 使用fetch API发送请求
    const controller = new AbortController();
    const timeoutId = window.setTimeout(() => {
      controller.abort();
    }, this.pingTimeout);
    
    fetch(this.pingUrl, {
      method: 'HEAD',
      mode: 'no-cors',
      cache: 'no-store',
      signal: controller.signal
    })
      .then(() => {
        // 计算延迟
        const endTime = Date.now();
        this._lastPingTime = endTime;
        this.lastPingDuration = endTime - startTime;
        
        // 添加到历史记录
        this.pingHistory.push(this.lastPingDuration);
        if (this.pingHistory.length > this.maxPingHistory) {
          this.pingHistory.shift();
        }
        
        // 根据延迟判断网络质量
        const oldStatus = this.status;
        if (this.lastPingDuration < 300) {
          this.status = NetworkStatus.FAST;
        } else if (this.lastPingDuration < 1000) {
          this.status = NetworkStatus.ONLINE;
        } else {
          this.status = NetworkStatus.SLOW;
        }
        
        // 如果状态发生变化，通知监听器
        if (oldStatus !== this.status) {
          this.notifyListeners();
        }
      })
      .catch(error => {
        // 如果是超时或网络错误
        if (error.name === 'AbortError' || error instanceof TypeError) {
          const oldStatus = this.status;
          this.status = NetworkStatus.SLOW;
          
          // 如果状态发生变化，通知监听器
          if (oldStatus !== this.status) {
            this.notifyListeners();
          }
        }
      })
      .finally(() => {
        // 清除超时定时器
        window.clearTimeout(timeoutId);
      });
  }
  
  /**
   * 通知所有监听器
   */
  private notifyListeners(): void {
    for (const listener of this.listeners) {
      listener(this.status, this.type);
    }
  }
  
  /**
   * 销毁监控器
   */
  public destroy(): void {
    // 停止监控
    this.stopMonitoring();
    
    // 移除事件监听器
    window.removeEventListener('online', this.handleOnline);
    window.removeEventListener('offline', this.handleOffline);
    
    if ('connection' in navigator && (navigator as any).connection) {
      (navigator as any).connection.removeEventListener('change', this.handleConnectionChange);
    }
    
    // 清空监听器列表
    this.listeners = [];
  }
  
  /**
   * 检查是否在线
   * @returns 是否在线
   */
  public isOnline(): boolean {
    return this.status !== NetworkStatus.OFFLINE;
  }
  
  /**
   * 根据网络状态调整请求配置
   * @param config 请求配置
   * @returns 调整后的请求配置
   */
  public async adjustRequestConfig(config: any): Promise<any> {
    return adjustRequestConfigByNetwork(config);
  }
}

// 这些方法已经直接添加到NetworkMonitor类中

// 创建单例实例
export const networkMonitor = new NetworkMonitor();

/**
 * 根据网络状态调整请求配置
 * @param config 原始请求配置
 * @returns 调整后的请求配置
 */
export function adjustRequestConfigByNetwork(config: any): any {
  const { status, type } = networkMonitor.getStatus();
  const adjustedConfig = { ...config };
  
  // 根据网络状态调整超时时间
  if (status === NetworkStatus.SLOW) {
    // 慢网络增加超时时间
    adjustedConfig.timeout = (config.timeout || 10000) * 2;
  } else if (status === NetworkStatus.OFFLINE) {
    // 离线状态增加重试次数
    adjustedConfig.retries = (config.retries || 0) + 2;
    adjustedConfig.retryDelay = (config.retryDelay || 1000) * 2;
  }
  
  // 根据网络类型调整请求策略
  switch (type) {
    case NetworkType.CELLULAR_2G:
    case NetworkType.CELLULAR_3G:
      // 移动网络优化
      adjustedConfig.timeout = (config.timeout || 10000) * 1.5;
      adjustedConfig.useCache = true;
      adjustedConfig.cacheTTL = (config.cacheTTL || 300000) * 2; // 增加缓存时间
      break;
    case NetworkType.WIFI:
    case NetworkType.ETHERNET:
      // 高速网络优化
      adjustedConfig.timeout = Math.max(5000, config.timeout || 10000);
      break;
  }
  
  return adjustedConfig;
}