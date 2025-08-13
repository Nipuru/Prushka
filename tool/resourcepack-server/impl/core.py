"""核心主程序模块"""

import asyncio
import os
import sys
import signal
import logging
import threading
from pathlib import Path
from colorama import Fore, Style, init
from aiohttp import web

from .config import Config
from .packs import PacksManager
from .server import Server
from .utils import create_directory_structure, log_message

init(autoreset=True)

class ResourcePackServer:
    """资源包服务器主类"""
    
    def __init__(self):
        self.config = None
        self.packs_manager = None
        self.server = None
        self.runner = None
        self.site = None
        self.is_running = False
        self.shutdown_event = asyncio.Event()
        
        signal.signal(signal.SIGINT, self._signal_handler)
        signal.signal(signal.SIGTERM, self._signal_handler)
    
    def _signal_handler(self, signum, frame):
        """信号处理器"""
        print(f"\n{Fore.RED}收到信号 {signum}，正在关闭服务器...{Style.RESET_ALL}")
        
        if hasattr(self, 'shutdown_event'):
            self.shutdown_event.set()
        
        self._force_exit()
    
    def _force_exit(self):
        """强制退出程序"""
        print(f"{Fore.YELLOW}正在关闭所有后台线程...{Style.RESET_ALL}")
        
        try:
            if self.packs_manager and hasattr(self.packs_manager, 'stop_file_monitoring'):
                self.packs_manager.stop_file_monitoring()
                print(f"{Fore.GREEN}文件监控已停止{Style.RESET_ALL}")
            
            for thread in threading.enumerate():
                if thread != threading.main_thread() and thread.is_alive():
                    print(f"{Fore.YELLOW}强制停止线程: {thread.name}{Style.RESET_ALL}")
            
        except Exception as e:
            print(f"{Fore.RED}强制关闭时出错: {e}{Style.RESET_ALL}")
        
        finally:
            print(f"{Fore.RED}强制退出程序{Style.RESET_ALL}")
            os._exit(0)
    
    async def initialize(self):
        """初始化服务器"""
        try:
            print(f"{Fore.CYAN}正在启动资源包服务器...{Style.RESET_ALL}")
            
            base_path = Path.cwd()
            create_directory_structure(base_path)
            print(f"{Fore.GREEN}目录结构创建完成{Style.RESET_ALL}")
            
            self.config = Config()
            if not self.config.configured:
                print(f"{Fore.RED}配置加载失败，服务器无法启动{Style.RESET_ALL}")
                return False
            
            print(f"{Fore.GREEN}配置加载成功{Style.RESET_ALL}")
            
            self.packs_manager = PacksManager(self.config)
            print(f"{Fore.GREEN}资源包管理器初始化完成{Style.RESET_ALL}")
            
            self.server = Server(self.config, self.packs_manager)
            print(f"{Fore.GREEN}HTTP 服务器初始化完成{Style.RESET_ALL}")
            
            self._setup_logging()
            
            return True
            
        except Exception as e:
            print(f"{Fore.RED}服务器初始化失败: {e}{Style.RESET_ALL}")
            return False
    
    def _setup_logging(self):
        """设置日志系统"""
        try:
            log_config = self.config.get('logging', {})
            log_level = getattr(logging, log_config.get('level', 'INFO'))
            log_file = log_config.get('file', 'logs/server.log')
            
            log_path = Path(log_file)
            log_path.parent.mkdir(parents=True, exist_ok=True)
            
            logging.basicConfig(
                level=log_level,
                format='[%(asctime)s] %(levelname)s: %(message)s',
                handlers=[
                    logging.FileHandler(log_file, encoding='utf-8'),
                    logging.StreamHandler(sys.stdout)
                ]
            )
            
            print(f"{Fore.GREEN}日志系统配置完成{Style.RESET_ALL}")
            
        except Exception as e:
            print(f"{Fore.YELLOW}日志系统配置失败: {e}{Style.RESET_ALL}")
    
    async def start(self):
        """启动服务器"""
        try:
            if not await self.initialize():
                return False
            
            host = self.config.get('server.host', '0.0.0.0')
            port = self.config.get('server.port', 8080)
            
            self.runner = web.AppRunner(
                self.server.app,
                access_log=None if not self.config.get('server.debug', False) else None
            )
            
            await self.runner.setup()
            
            self.site = web.TCPSite(self.runner, host, port)
            await self.site.start()
            
            self.is_running = True
            
            self._print_startup_info(host, port)
            
            return True
            
        except Exception as e:
            print(f"{Fore.RED}服务器启动失败: {e}{Style.RESET_ALL}")
            return False
    
    def _print_startup_info(self, host: str, port: int):
        """打印启动信息"""
        os.system('cls' if os.name == 'nt' else 'clear')
        
        print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}")
        print(f"{Fore.GREEN}服务器启动成功！{Style.RESET_ALL}")
        print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}")
        print(f"{Fore.YELLOW}服务器地址: {Fore.WHITE}http://{host}:{port}{Style.RESET_ALL}")
        print(f"{Fore.YELLOW}本地访问: {Fore.WHITE}http://127.0.0.1:{port}{Style.RESET_ALL}")
        print(f"{Fore.YELLOW}调试信息: {Fore.WHITE}http://127.0.0.1:{port}/debug{Style.RESET_ALL}")
        print(f"{Fore.YELLOW}资源包目录: {Fore.WHITE}{self.packs_manager.packs_directory}{Style.RESET_ALL}")
        print(f"{Fore.YELLOW}发现资源包: {Fore.WHITE}{len(self.packs_manager.packs)} 个{Style.RESET_ALL}")
        print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}")
        print(f"{Fore.GREEN}服务器正在运行中... 按 Ctrl+C 停止服务器{Style.RESET_ALL}")
        print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}")
    
    async def shutdown(self):
        """关闭服务器"""
        if not self.is_running:
            return
        
        print(f"{Fore.YELLOW}正在关闭服务器...{Style.RESET_ALL}")
        
        try:
            if self.packs_manager and hasattr(self.packs_manager, 'stop_file_monitoring'):
                self.packs_manager.stop_file_monitoring()
                print(f"{Fore.GREEN}文件监控已停止{Style.RESET_ALL}")
            
            if self.site:
                await self.site.stop()
            
            if self.runner:
                await self.runner.cleanup()
            
            self.is_running = False
            print(f"{Fore.GREEN}服务器已关闭{Style.RESET_ALL}")
            
        except Exception as e:
            print(f"{Fore.RED}服务器关闭时出错: {e}{Style.RESET_ALL}")
        
        finally:
            print(f"{Fore.RED}强制退出程序{Style.RESET_ALL}")
            os._exit(0)
    
    async def run(self):
        """运行服务器"""
        try:
            if await self.start():
                await self.shutdown_event.wait()
            else:
                print(f"{Fore.RED}服务器启动失败，程序退出{Style.RESET_ALL}")
                os._exit(1)
                
        except KeyboardInterrupt:
            print(f"\n{Fore.RED}收到键盘中断信号，强制退出{Style.RESET_ALL}")
            self._force_exit()
        except Exception as e:
            print(f"{Fore.RED}服务器运行出错: {e}{Style.RESET_ALL}")
            self._force_exit()


def main():
    """主函数"""
    try:
        server = ResourcePackServer()
        asyncio.run(server.run())
        
    except Exception as e:
        print(f"{Fore.RED}程序启动失败: {e}{Style.RESET_ALL}")
        sys.exit(1)