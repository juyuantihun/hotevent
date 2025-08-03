import { InternalAxiosRequestConfig } from 'axios';

declare module 'axios' {
  interface InternalAxiosRequestConfig {
    _retryCount?: number;
    _isRetry?: boolean;
    _retryDelay?: number;
    _isRefreshingToken?: boolean;
    metadata?: {
      startTime?: number;
      duration?: number;
      [key: string]: any;
    };
  }
}