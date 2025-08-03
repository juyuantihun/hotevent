import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath, URL } from 'node:url';

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'happy-dom',
    include: ['src/__tests__/**/*.{test,spec}.{js,ts,jsx,tsx}'],
    exclude: ['src/__tests__/e2e/**/*'],
    coverage: {
      provider: 'c8',
      reporter: ['text', 'json', 'html', 'lcov'],
      reportsDirectory: './coverage',
      exclude: [
        'src/main.ts',
        'src/router/index.ts',
        'src/vite-env.d.ts',
        'src/types/**',
        'src/__tests__/**',
        'src/assets/**',
        '**/*.d.ts',
      ],
      all: true,
      thresholds: {
        statements: 70,
        branches: 60,
        functions: 70,
        lines: 70,
      },
    },
    reporters: ['default', 'html', 'json'],
    outputFile: {
      html: './test-results/unit/html/index.html',
      json: './test-results/unit/json/results.json',
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
});