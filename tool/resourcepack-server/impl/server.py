"""HTTP 服务器模块"""

import json
import time
from typing import Dict, Any
from aiohttp import web, ClientSession
from aiohttp.web import Request, Response


class Server:
    """HTTP 服务器类"""
    
    def __init__(self, config, packs_manager):
        self.config = config
        self.packs_manager = packs_manager
        self.app = web.Application()
        self.setup_routes()
    
    def setup_routes(self):
        """设置路由"""
        self.app.router.add_get('/', self.index_handler)
        
        self.app.router.add_get('/api/packs', self.list_packs_handler)
        self.app.router.add_get('/api/packs/{name}', self.get_pack_handler)
        
        self.app.router.add_get('/download/{name}', self.download_pack_handler)
        
        self.app.router.add_get('/hash/{name}', self.hash_handler)
        
        self.app.router.add_post('/api/rescan', self.rescan_packs_handler)
        
        self.app.router.add_get('/debug', self.debug_handler)
        
        self.app.middlewares.append(self.error_middleware)
    
    async def index_handler(self, request: Request) -> Response:
        """主页处理器"""
        try:
            packs = self.packs_manager.get_all_packs()
            
            html_content = f"""
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Minecraft 资源包服务器</title>
                <style>
                    body {{ font-family: 'Microsoft YaHei', sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }}
                    .container {{ max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }}
                    h1 {{ color: #2c3e50; text-align: center; margin-bottom: 30px; }}
                    .pack-card {{ border: 1px solid #ddd; padding: 20px; margin-bottom: 20px; border-radius: 8px; background: #fafafa; }}
                    .pack-name {{ font-size: 1.3em; font-weight: bold; color: #2c3e50; margin-bottom: 10px; }}
                    .pack-desc {{ color: #7f8c8d; margin-bottom: 15px; }}
                    .pack-meta {{ font-size: 0.9em; color: #95a5a6; margin-bottom: 15px; }}
                    .download-btn, .copy-btn {{ 
                        background: #27ae60; 
                        color: white; 
                        padding: 10px 20px; 
                        text-decoration: none; 
                        border-radius: 5px; 
                        display: inline-block; 
                        margin-right: 10px; 
                        cursor: pointer; 
                        border: none; 
                        font-size: 14px;
                        font-family: inherit;
                        line-height: 1.4;
                        min-width: 120px;
                        text-align: center;
                    }}
                    .download-btn:hover {{ background: #229954; }}
                    .copy-btn {{ background: #3498db; }}
                    .copy-btn:hover {{ background: #2980b9; }}
                    .copy-btn:active {{ background: #1f5f8b; }}
                    .hash-info {{ background: #ecf0f1; padding: 10px; border-radius: 5px; font-family: monospace; font-size: 0.9em; }}
                    .no-packs {{ text-align: center; color: #7f8c8d; font-style: italic; }}
                    .copy-feedback {{ 
                        position: fixed; 
                        top: 20px; 
                        right: 20px; 
                        background: #27ae60; 
                        color: white; 
                        padding: 10px 20px; 
                        border-radius: 5px; 
                        display: none; 
                        z-index: 1000;
                        animation: slideIn 0.3s ease-out;
                    }}
                    @keyframes slideIn {{
                        from {{ transform: translateX(100%); opacity: 0; }}
                        to {{ transform: translateX(0); opacity: 1; }}
                    }}
                </style>
                <script>
                    function copyHash(hash) {{
                        navigator.clipboard.writeText(hash).then(function() {{
                            showCopyFeedback('Hash 已复制到剪贴板！');
                        }}).catch(function(err) {{
                            const textArea = document.createElement('textarea');
                            textArea.value = hash;
                            document.body.appendChild(textArea);
                            textArea.select();
                            try {{
                                document.execCommand('copy');
                                showCopyFeedback('Hash 已复制到剪贴板！');
                            }} catch (err) {{
                                showCopyFeedback('复制失败，请手动复制');
                            }}
                            document.body.removeChild(textArea);
                        }});
                    }}
                    
                    function showCopyFeedback(message) {{
                        const feedback = document.getElementById('copy-feedback');
                        feedback.textContent = message;
                        feedback.style.display = 'block';
                        
                        setTimeout(function() {{
                            feedback.style.display = 'none';
                        }}, 2000);
                    }}
                </script>
            </head>
            <body>
                <div class="container">
                    <h1>Minecraft 资源包服务器</h1>
                    <p style="text-align: center; color: #7f8c8d; margin-bottom: 30px;">
                    </p>
                    
                    <h2>可用资源包 ({len(packs)} 个)</h2>
            """
            
            if not packs:
                html_content += '<div class="no-packs">暂无可用资源包</div>'
            else:
                for pack in packs:
                    size_mb = pack.size / 1024 / 1024
                    html_content += f"""
                    <div class="pack-card">
                        <div class="pack-name">{pack.name}</div>
                        <div class="pack-desc">{pack.description}</div>
                        <div class="pack-meta">
                            格式: {pack.pack_format} | 大小: {size_mb:.2f} MB<br>
                            类型: {'目录' if pack.is_directory else 'ZIP文件'} | 
                            更新时间: {time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(pack.last_modified))}
                        </div>
                        <div class="hash-info">
                            <strong>Hash (MD5):</strong> {pack.hash}
                        </div>
                        <a href="/download/{pack.name}" class="download-btn">下载资源包</a>
                        <button onclick="copyHash('{pack.hash}')" class="copy-btn">复制 Hash</button>
                    </div>
                    """
            
            html_content += """
                </div>
                <div id="copy-feedback" class="copy-feedback"></div>
                <div style="text-align: center; margin-top: 30px; padding: 20px; color: #7f8c8d; border-top: 1px solid #eee;">
                    <p>&copy; 2025 Nipuru. All rights reserved.</p>
                </div>
            </body>
            </html>
            """
            
            return web.Response(text=html_content, content_type='text/html')
            
        except Exception as e:
            return web.Response(text=f"页面加载失败: {str(e)}", status=500)
    
    async def list_packs_handler(self, request: Request) -> Response:
        """获取所有资源包列表"""
        try:
            packs = self.packs_manager.get_all_packs()
            packs_data = [pack.to_dict() for pack in packs]
            
            return web.json_response({
                'success': True,
                'data': packs_data,
                'count': len(packs_data)
            })
            
        except Exception as e:
            return web.json_response({
                'success': False,
                'error': str(e)
            }, status=500)
    
    async def get_pack_handler(self, request: Request) -> Response:
        """获取指定资源包信息"""
        try:
            name = request.match_info['name']
            pack = self.packs_manager.get_pack(name)
            
            if not pack:
                return web.json_response({
                    'success': False,
                    'error': '资源包不存在'
                }, status=404)
            
            return web.json_response({
                'success': True,
                'data': pack.to_dict()
            })
            
        except Exception as e:
            return web.json_response({
                'success': False,
                'error': str(e)
            }, status=500)
    
    async def download_pack_handler(self, request: Request) -> Response:
        """下载资源包"""
        try:
            name = request.match_info['name']
            pack = self.packs_manager.get_pack(name)
            
            if not pack:
                return web.json_response({
                    'success': False,
                    'error': '资源包不存在'
                }, status=404)
            
            response = await self.packs_manager.serve_pack(name)
            if response is not None:
                return response
            else:
                return web.json_response({
                    'success': False,
                    'error': '资源包文件生成失败'
                }, status=500)
            
        except Exception as e:
            return web.json_response({
                'success': False,
                'error': str(e)
            }, status=500)
    
    async def hash_handler(self, request: Request) -> Response:
        """获取资源包 Hash"""
        try:
            name = request.match_info['name']
            hash_value = self.packs_manager.get_pack_hash(name)
            
            if not hash_value:
                return web.json_response({
                    'success': False,
                    'error': '资源包不存在'
                }, status=404)
            
            return web.json_response({
                'success': True,
                'data': {
                    'name': name,
                    'hash': hash_value,
                    'hash_type': 'MD5'
                }
            })
            
        except Exception as e:
            return web.json_response({
                'success': False,
                'error': str(e)
            }, status=500)
    
    async def rescan_packs_handler(self, request: Request) -> Response:
        """手动重新扫描资源包"""
        try:
            import threading
            def scan_task():
                self.packs_manager.scan_packs()
            
            scan_thread = threading.Thread(target=scan_task, daemon=True)
            scan_thread.start()
            
            return web.json_response({
                'success': True,
                'message': '资源包重新扫描已启动',
                'timestamp': time.time()
            })
            
        except Exception as e:
            return web.json_response({
                'success': False,
                'error': str(e)
            }, status=500)
    
    async def debug_handler(self, request: Request) -> Response:
        """调试信息处理器"""
        debug_info = {
            'server': 'Resource Pack Server',
            'version': '1.0.0',
            'config': {
                'host': self.config.get('server.host'),
                'port': self.config.get('server.port'),
                'debug': self.config.get('server.debug')
            },
            'packs': {
                'directory': str(self.packs_manager.packs_directory),
                'count': len(self.packs_manager.packs),
                'temp_directory': str(self.packs_manager.temp_dir),
                'file_monitor': {
                    'enabled': self.packs_manager.file_monitor_enabled,
                    'watchdog_available': hasattr(self.packs_manager, 'observer') and self.packs_manager.observer is not None,
                    'last_scan_time': self.packs_manager.last_scan_time,
                    'scan_cooldown': self.packs_manager.scan_cooldown
                }
            },
            'endpoints': {
                'list_packs': '/api/packs',
                'get_pack': '/api/packs/{name}',
                'download': '/download/{name}',
                'hash': '/hash/{name}',
                'rescan': '/api/rescan',
                'debug': '/debug'
            },
            'timestamp': time.time()
        }
        
        return web.json_response(debug_info)
    
    async def error_middleware(self, app, handler):
        """错误处理中间件"""
        async def middleware_handler(request):
            try:
                return await handler(request)
            except web.HTTPException as ex:
                return web.json_response({
                    'success': False,
                    'error': ex.reason,
                    'status': ex.status
                }, status=ex.status)
            except Exception as e:
                return web.json_response({
                    'success': False,
                    'error': str(e),
                    'status': 500
                }, status=500)
        
        return middleware_handler
