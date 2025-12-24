import type { ConfigEnv, UserConfig } from 'vite'
import { loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import { wrapperEnv } from './build/utils'
// 需要安装 @typings/node 插件
import { resolve } from 'path'

/** @type {import('vite').UserConfig} */
export default ({ command, mode }: ConfigEnv): UserConfig => {
  const root = process.cwd()

  const env = loadEnv(mode, root)

  // this function can be converted to different typings
  const viteEnv: any = wrapperEnv(env)
  const { VITE_PORT, VITE_DROP_CONSOLE, VITE_API_URL } = viteEnv

  return {
    base: './',
    server: {
      // Listening on all local ips
      host: true,
      open: true,
      port: VITE_PORT,
      proxy: {
        '/api': {
          target: VITE_API_URL,
          changeOrigin: true
        }
      }
    },
    plugins: [
      react(),
      createSvgIconsPlugin({
        iconDirs: [resolve(process.cwd(), 'src/assets/icons')],
        symbolId: 'icon-[dir]-[name]'
      })
    ],

    build: {
      target: 'es2015',
      cssTarget: 'chrome86',
      minify: 'terser',
      terserOptions: {
        compress: {
          keep_infinity: true,
          // used to delete console and debugger in production environment
          drop_console: VITE_DROP_CONSOLE
        }
      },
      chunkSizeWarningLimit: 2000
    },

    resolve: {
      alias: {
        '@': resolve(__dirname, './src')
      }
    }
  }
}
