import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver, VantResolver } from 'unplugin-vue-components/resolvers'
import path from 'node:path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    server: {
      host: '0.0.0.0',
      port: 3001,
      proxy: {
        '/app-api': {
          target: env.VITE_API_BASE || 'http://localhost:48080',
          changeOrigin: true
        }
      }
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    css: {
      preprocessorOptions: {
        scss: {
          // 全局注入 brand token,各组件 <style lang="scss"> 直接用 brand.$xxx
          additionalData: `@use "@/assets/styles/brand.scss" as brand;`
        }
      }
    },
    plugins: [
      vue(),
      AutoImport({
        imports: ['vue', 'vue-router', 'pinia', 'vue-i18n'],
        resolvers: [ElementPlusResolver(), VantResolver()],
        dts: 'src/auto-imports.d.ts'
      }),
      Components({
        resolvers: [ElementPlusResolver(), VantResolver()],
        dts: 'src/components.d.ts'
      })
    ],
    build: {
      rollupOptions: {
        output: {
          manualChunks: {
            vue: ['vue', 'vue-router', 'pinia', 'vue-i18n'],
            ui: ['element-plus', 'vant'],
            utilities: ['axios', 'dayjs']
          }
        }
      }
    }
  }
})
