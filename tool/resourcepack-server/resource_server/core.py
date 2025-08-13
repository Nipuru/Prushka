"""
核心主程序模块
负责启动和管理整个资源包服务器
"""

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

# 初始化 colorama
init(autoreset=True)

class PrushkaServer:
    """Prushka 资源包服务器主类"""
    
    def __init__(self):
        self.config = None
        self.packs_manager = None
        self.server = None
        self.runner = None
        self.site = None
        self.is_running = False
        self.shutdown_event = asyncio.Event()
        
        # 设置信号处理
        signal.signal(signal.SIGINT, self._signal_handler)
        signal.signal(signal.SIGTERM, self._signal_handler)
    
    def _signal_handler(self, signum, frame):
        """信号处理器 - 强制退出"""
        print(f"\n{Fore.RED}🚨 收到信号 {signum}，正在强制关闭服务器...{Style.RESET_ALL}")
        
        # 设置关闭事件
        if hasattr(self, 'shutdown_event'):
            self.shutdown_event.set()
        
        # 强制退出所有后台线程
        self._force_exit()
    
    def _force_exit(self):
        """强制退出所有后台线程和程序"""
        print(f"{Fore.YELLOW}🔄 正在强制关闭所有后台线程...{Style.RESET_ALL}")
        
        try:
            # 停止文件监控
            if self.packs_manager and hasattr(self.packs_manager, 'stop_file_monitoring'):
                self.packs_manager.stop_file_monitoring()
                print(f"{Fore.GREEN}✅ 文件监控已停止{Style.RESET_ALL}")
            
            # 强制退出所有Python线程
            for thread in threading.enumerate():
                if thread != threading.main_thread() and thread.is_alive():
                    print(f"{Fore.YELLOW}⚠️ 强制停止线程: {thread.name}{Style.RESET_ALL}")
                    # 注意：在Python中无法强制杀死线程，只能设置标志位
                    # 这里我们直接退出程序
            
        except Exception as e:
            print(f"{Fore.RED}❌ 强制关闭时出错: {e}{Style.RESET_ALL}")
        
        finally:
            print(f"{Fore.RED}💀 强制退出程序{Style.RESET_ALL}")
            # 强制退出，不等待任何清理
            os._exit(0)
    
    async def initialize(self):
        """初始化服务器"""
        try:
            print(f"{Fore.CYAN}🚀 正在启动 Prushka 资源包服务器...{Style.RESET_ALL}")
            
            # 创建目录结构
            base_path = Path.cwd()
            create_directory_structure(base_path)
            print(f"{Fore.GREEN}✅ 目录结构创建完成{Style.RESET_ALL}")
            
            # 加载配置
            self.config = Config()
            if not self.config.configured:
                print(f"{Fore.RED}❌ 配置加载失败，服务器无法启动{Style.RESET_ALL}")
                return False
            
            print(f"{Fore.GREEN}✅ 配置加载成功{Style.RESET_ALL}")
            
            # 初始化资源包管理器
            self.packs_manager = PacksManager(self.config)
            print(f"{Fore.GREEN}✅ 资源包管理器初始化完成{Style.RESET_ALL}")
            
            # 初始化 HTTP 服务器
            self.server = Server(self.config, self.packs_manager)
            print(f"{Fore.GREEN}✅ HTTP 服务器初始化完成{Style.RESET_ALL}")
            
            # 设置日志
            self._setup_logging()
            
            return True
            
        except Exception as e:
            print(f"{Fore.RED}❌ 服务器初始化失败: {e}{Style.RESET_ALL}")
            return False
    
    def _setup_logging(self):
        """设置日志系统"""
        try:
            log_config = self.config.get('logging', {})
            log_level = getattr(logging, log_config.get('level', 'INFO'))
            log_file = log_config.get('file', 'logs/server.log')
            
            # 确保日志目录存在
            log_path = Path(log_file)
            log_path.parent.mkdir(parents=True, exist_ok=True)
            
            # 配置日志格式
            logging.basicConfig(
                level=log_level,
                format='[%(asctime)s] %(levelname)s: %(message)s',
                handlers=[
                    logging.FileHandler(log_file, encoding='utf-8'),
                    logging.StreamHandler(sys.stdout)
                ]
            )
            
            print(f"{Fore.GREEN}✅ 日志系统配置完成{Style.RESET_ALL}")
            
        except Exception as e:
            print(f"{Fore.YELLOW}⚠️ 日志系统配置失败: {e}{Style.RESET_ALL}")
    
    async def start(self):
        """启动服务器"""
        try:
            # 初始化
            if not await self.initialize():
                return False
            
            # 获取服务器配置
            host = self.config.get('server.host', '0.0.0.0')
            port = self.config.get('server.port', 8080)
            
            # 创建应用运行器
            self.runner = web.AppRunner(
                self.server.app,
                access_log=None if not self.config.get('server.debug', False) else None
            )
            
            # 启动运行器
            await self.runner.setup()
            
            # 启动站点
            self.site = web.TCPSite(self.runner, host, port)
            await self.site.start()
            
            self.is_running = True
            
            # 显示启动信息
            self._print_startup_info(host, port)
            
            return True
            
        except Exception as e:
            print(f"{Fore.RED}❌ 服务器启动失败: {e}{Style.RESET_ALL}")
            return False
    
    def _print_startup_info(self, host: str, port: int):
        """打印启动信息"""
        # 清屏
        os.system('cls' if os.name == 'nt' else 'clear')
        
        print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}")
        print(f"{Fore.GREEN}🎮 Prushka 资源包服务器启动成功！{Style.RESET_ALL}")
        print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}")
        print(f"{Fore.YELLOW}📍 服务器地址: {Fore.WHITE}http://{host}:{port}{Style.RESET_ALL}")
        print(f"{Fore.YELLOW}🔗 本地访问: {Fore.WHITE}http://127.0.0.1:{port}{Style.RESET_ALL}")
        print(f"{Fore.YELLOW}📊 调试信息: {Fore.WHITE}http://127.0.0.1:{port}/debug{Style.RESET_ALL}")
        print(f"{Fore.YELLOW}📦 资源包目录: {Fore.WHITE}{self.packs_manager.packs_directory}{Style.RESET_ALL}")
        print(f"{Fore.YELLOW}🔍 发现资源包: {Fore.WHITE}{len(self.packs_manager.packs)} 个{Style.RESET_ALL}")
        print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}")
        print(f"{Fore.GREEN}✅ 服务器正在运行中... 按 Ctrl+C 停止服务器{Style.RESET_ALL}")
        print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}")
    
    async def shutdown(self):
        """关闭服务器"""
        if not self.is_running:
            return
        
        print(f"{Fore.YELLOW}🔄 正在关闭服务器...{Style.RESET_ALL}")
        
        try:
            # 停止文件监控
            if self.packs_manager and hasattr(self.packs_manager, 'stop_file_monitoring'):
                self.packs_manager.stop_file_monitoring()
                print(f"{Fore.GREEN}✅ 文件监控已停止{Style.RESET_ALL}")
            
            # 关闭站点
            if self.site:
                await self.site.stop()
            
            # 关闭运行器
            if self.runner:
                await self.runner.cleanup()
            
            self.is_running = False
            print(f"{Fore.GREEN}✅ 服务器已关闭{Style.RESET_ALL}")
            
        except Exception as e:
            print(f"{Fore.RED}❌ 服务器关闭时出错: {e}{Style.RESET_ALL}")
        
        finally:
            # 强制退出程序
            print(f"{Fore.RED}💀 强制退出程序{Style.RESET_ALL}")
            os._exit(0)
    
    async def run(self):
        """运行服务器"""
        try:
            if await self.start():
                # 保持服务器运行，等待关闭事件
                await self.shutdown_event.wait()
            else:
                print(f"{Fore.RED}❌ 服务器启动失败，程序退出{Style.RESET_ALL}")
                os._exit(1)
                
        except KeyboardInterrupt:
            print(f"\n{Fore.RED}🚨 收到键盘中断信号，强制退出{Style.RESET_ALL}")
            self._force_exit()
        except Exception as e:
            print(f"{Fore.RED}❌ 服务器运行出错: {e}{Style.RESET_ALL}")
            self._force_exit()


def main():
    """主函数"""
    try:
        # 创建服务器实例
        server = PrushkaServer()
        
        # 运行服务器
        asyncio.run(server.run())
        
    except Exception as e:
        print(f"{Fore.RED}❌ 程序启动失败: {e}{Style.RESET_ALL}")
        sys.exit(1)


if __name__ == "__main__":
    main()
