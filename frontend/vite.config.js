import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // 开发期代理到后端；生产构建时前后端同源（静态资源嵌入 jar），无需代理
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true }
    }
  },
  build: {
    outDir: 'dist',
    chunkSizeWarningLimit: 1500
  }
})
