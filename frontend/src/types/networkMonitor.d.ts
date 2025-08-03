import { NetworkStatus, NetworkType } from '@/services/networkMonitor';

declare module '@/services/networkMonitor' {
  interface NetworkMonitor {
    isOnline(): boolean;
    adjustRequestConfig(config: any): Promise<any>;
  }
}