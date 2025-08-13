"""
资源包管理模块
专门为 Bukkit 插件提供资源包下载服务
"""

import os
import json
import hashlib
import time
import zipfile
import tempfile
import threading
import asyncio
from pathlib import Path
from typing import Dict, List, Optional
from dataclasses import dataclass
import aiohttp
from aiohttp import web

# 添加文件监控相关导入
try:
    from watchdog.observers import Observer
    from watchdog.events import FileSystemEventHandler, FileSystemEvent
    WATCHDOG_AVAILABLE = True
except ImportError:
    WATCHDOG_AVAILABLE = False
    print("⚠️ watchdog 库未安装，文件监控功能将不可用。请运行: pip install watchdog")


@dataclass
class ResourcePack:
    """资源包信息类"""
    name: str
    path: Path
    description: str
    pack_format: int
    size: int
    hash: str
    last_modified: float
    is_directory: bool = False
    
    def to_dict(self) -> Dict:
        """转换为字典格式，包含 Bukkit 需要的字段"""
        return {
            "name": self.name,
            "description": self.description,
            "pack_format": self.pack_format,
            "size": self.size,
            "hash": self.hash,
            "last_modified": self.last_modified,
            "is_directory": self.is_directory,
            "download_url": f"/download/{self.name}",
            "hash_url": f"/hash/{self.name}"
        }


class PacksManager:
    """资源包管理器 - 专门为 Bukkit 设计"""
    
    def __init__(self, config):
        """初始化资源包管理器"""
        self.config = config
        self.packs_directory = Path(config.get("packs.directory", "resourcepack"))
        self.packs = {}
        self.temp_dir = Path(tempfile.gettempdir()) / "prushka_resourcepacks"
        
        # 确保临时目录存在
        self.temp_dir.mkdir(parents=True, exist_ok=True)
        
        # 文件监控相关配置
        self.file_monitor_enabled = config.get("packs.file_monitor", True)
        self.file_monitor_interval = config.get("packs.file_monitor_interval", 1.0)  # 秒
        self.observer = None
        self.file_monitor_thread = None
        self.last_scan_time = 0
        self.scan_cooldown = config.get("packs.scan_cooldown", 2.0)  # 扫描冷却时间（秒）
        
        # 添加停止标志
        self._stop_monitoring = False
        
        # 扫描资源包
        self.scan_packs()
        
        # 启动文件监控
        if self.file_monitor_enabled and WATCHDOG_AVAILABLE:
            self.start_file_monitoring()
        elif self.file_monitor_enabled and not WATCHDOG_AVAILABLE:
            print("⚠️ 文件监控功能已启用但 watchdog 库未安装")
            print("💡 请运行以下命令安装: pip install watchdog")
    
    def start_file_monitoring(self):
        """启动文件监控"""
        if not WATCHDOG_AVAILABLE:
            return
            
        try:
            # 重置停止标志
            self._stop_monitoring = False
            
            # 创建文件系统事件处理器
            event_handler = ResourcePackFileHandler(self)
            
            # 创建观察者
            self.observer = Observer()
            self.observer.schedule(event_handler, str(self.packs_directory), recursive=True)
            
            # 启动监控
            self.observer.start()
            print(f"🔍 文件监控已启动，监控目录: {self.packs_directory}")
            
        except Exception as e:
            print(f"❌ 启动文件监控失败: {e}")
    
    def stop_file_monitoring(self):
        """停止文件监控"""
        if self.observer:
            try:
                # 设置停止标志
                self._stop_monitoring = True
                
                # 立即停止观察者
                self.observer.stop()
                
                # 等待观察者线程结束，但设置超时
                self.observer.join(timeout=1.0)
                
                # 如果超时，强制清理
                if self.observer.is_alive():
                    print("⚠️ 文件监控线程超时，强制清理")
                    # 在Python中无法强制杀死线程，但我们可以标记为停止
                
                print("🔍 文件监控已停止")
                
            except Exception as e:
                print(f"❌ 停止文件监控失败: {e}")
    
    def schedule_rescan(self):
        """计划重新扫描资源包（带冷却时间）"""
        # 检查是否已停止监控
        if self._stop_monitoring:
            return
            
        current_time = time.time()
        if current_time - self.last_scan_time >= self.scan_cooldown:
            self.last_scan_time = current_time
            
            # 在新线程中执行扫描，避免阻塞主线程
            def delayed_scan():
                # 再次检查停止标志
                if self._stop_monitoring:
                    return
                    
                time.sleep(0.5)  # 短暂延迟，确保文件操作完成
                
                # 最终检查停止标志
                if not self._stop_monitoring:
                    self.scan_packs()
            
            scan_thread = threading.Thread(target=delayed_scan, daemon=True)
            scan_thread.start()
            print("🔄 检测到文件变化，计划重新扫描资源包...")
    
    def scan_packs(self) -> None:
        """扫描资源包目录"""
        try:
            old_packs = set(self.packs.keys())
            self.packs.clear()
            print(f"🔍 开始扫描资源包目录: {self.packs_directory.absolute()}")
            
            # 首先检查资源包目录本身是否是一个资源包
            if self._is_resource_pack_directory(self.packs_directory):
                print(f"📁 发现根目录资源包: {self.packs_directory.name}")
                pack = self._load_directory_pack(self.packs_directory)
                if pack:
                    self.packs[pack.name] = pack
                    print(f"✅ 加载根目录资源包成功: {pack.name}")
            
            # 然后扫描子目录和文件
            for item in self.packs_directory.iterdir():
                if item.is_file() and item.suffix == '.zip':
                    # 处理 .zip 文件
                    pack = self._load_zip_pack(item)
                    if pack:
                        self.packs[pack.name] = pack
                        print(f"📦 发现 ZIP 资源包: {pack.name}")
                elif item.is_dir() and self._is_resource_pack_directory(item):
                    # 处理子目录资源包
                    pack = self._load_directory_pack(item)
                    if pack:
                        self.packs[pack.name] = pack
                        print(f"📁 发现子目录资源包: {pack.name}")
            
            new_packs = set(self.packs.keys())
            
            # 检查变化
            added = new_packs - old_packs
            removed = old_packs - new_packs
            
            if added:
                print(f"✨ 新增资源包: {', '.join(added)}")
            if removed:
                print(f"🗑️ 移除资源包: {', '.join(removed)}")
            
            print(f"✅ 扫描完成，共发现 {len(self.packs)} 个资源包")
            
        except Exception as e:
            print(f"❌ 扫描资源包失败: {e}")
            import traceback
            traceback.print_exc()
    
    def _is_resource_pack_directory(self, dir_path: Path) -> bool:
        """检查是否是有效的资源包目录"""
        pack_mcmeta = dir_path / "pack.mcmeta"
        return pack_mcmeta.exists() and pack_mcmeta.is_file()
    
    def _load_zip_pack(self, pack_path: Path) -> Optional[ResourcePack]:
        """加载 .zip 资源包信息"""
        try:
            # 获取基本信息
            stat = pack_path.stat()
            name = pack_path.stem
            size = stat.st_size
            last_modified = stat.st_mtime
            
            # 计算文件哈希
            file_hash = self._calculate_file_hash(pack_path)
            
            # 尝试读取 pack.mcmeta 信息
            description = f"Resource Pack: {name}"
            pack_format = 22
            
            try:
                with zipfile.ZipFile(pack_path, 'r') as zip_file:
                    if 'pack.mcmeta' in zip_file.namelist():
                        mcmeta_content = zip_file.read('pack.mcmeta').decode('utf-8')
                        pack_info = self._parse_pack_mcmeta(mcmeta_content)
                        if pack_info:
                            description = pack_info.get('description', description)
                            pack_format = pack_info.get('pack_format', pack_format)
            except Exception as e:
                print(f"⚠️ 读取 {pack_path} 的 pack.mcmeta 失败: {e}")
            
            return ResourcePack(
                name=name,
                path=pack_path,
                description=description,
                pack_format=pack_format,
                size=size,
                hash=file_hash,
                last_modified=last_modified,
                is_directory=False
            )
            
        except Exception as e:
            print(f"❌ 加载 .zip 资源包失败 {pack_path}: {e}")
            return None
    
    def _load_directory_pack(self, dir_path: Path) -> Optional[ResourcePack]:
        """加载目录资源包信息"""
        try:
            # 获取基本信息
            name = dir_path.name
            last_modified = dir_path.stat().st_mtime
            
            # 读取 pack.mcmeta 信息
            pack_mcmeta_path = dir_path / "pack.mcmeta"
            description = f"Resource Pack: {name}"
            pack_format = 22
            
            try:
                with open(pack_mcmeta_path, 'r', encoding='utf-8') as f:
                    mcmeta_content = f.read()
                    pack_info = self._parse_pack_mcmeta(mcmeta_content)
                    if pack_info:
                        description = pack_info.get('description', description)
                        pack_format = pack_info.get('pack_format', pack_format)
            except Exception as e:
                print(f"⚠️ 读取 {pack_mcmeta_path} 失败: {e}")
            
            # 计算目录大小和哈希
            size = self._calculate_directory_size(dir_path)
            dir_hash = self._calculate_directory_hash(dir_path)
            
            return ResourcePack(
                name=name,
                path=dir_path,
                description=description,
                pack_format=pack_format,
                size=size,
                hash=dir_hash,
                last_modified=last_modified,
                is_directory=True
            )
            
        except Exception as e:
            print(f"❌ 加载目录资源包失败 {dir_path}: {e}")
            return None
    
    def _parse_pack_mcmeta(self, content: str) -> Optional[Dict]:
        """解析 pack.mcmeta 文件内容"""
        try:
            data = json.loads(content)
            pack_info = data.get('pack', {})
            return {
                'description': pack_info.get('description', ''),
                'pack_format': pack_info.get('pack_format', 22)
            }
        except Exception as e:
            print(f"⚠️ 解析 pack.mcmeta 失败: {e}")
            return None
    
    def _calculate_directory_size(self, dir_path: Path) -> int:
        """计算目录大小"""
        total_size = 0
        try:
            for item in dir_path.rglob('*'):
                if item.is_file():
                    total_size += item.stat().st_size
        except Exception as e:
            print(f"⚠️ 计算目录大小失败 {dir_path}: {e}")
        return total_size
    
    def _calculate_directory_hash(self, dir_path: Path) -> str:
        """计算目录哈希值（基于文件修改时间和大小）"""
        hash_md5 = hashlib.md5()
        try:
            # 收集所有文件的信息
            file_infos = []
            for item in sorted(dir_path.rglob('*')):
                if item.is_file():
                    stat = item.stat()
                    file_infos.append(f"{item.relative_to(dir_path)}:{stat.st_mtime}:{stat.st_size}")
            
            # 计算哈希
            content = "\n".join(file_infos).encode('utf-8')
            hash_md5.update(content)
            return hash_md5.hexdigest()
        except Exception as e:
            print(f"⚠️ 计算目录哈希失败 {dir_path}: {e}")
            return ""
    
    def _calculate_file_hash(self, file_path: Path) -> str:
        """计算文件哈希值"""
        hash_md5 = hashlib.md5()
        try:
            with open(file_path, "rb") as f:
                for chunk in iter(lambda: f.read(4096), b""):
                    hash_md5.update(chunk)
            return hash_md5.hexdigest()
        except Exception as e:
            print(f"⚠️ 计算文件哈希失败 {file_path}: {e}")
            return ""
    
    def get_pack(self, name: str) -> Optional[ResourcePack]:
        """获取指定名称的资源包"""
        return self.packs.get(name)
    
    def get_all_packs(self) -> List[ResourcePack]:
        """获取所有资源包"""
        return list(self.packs.values())
    
    def get_pack_hash(self, name: str) -> Optional[str]:
        """获取资源包的 hash 值（Bukkit 需要）"""
        pack = self.get_pack(name)
        return pack.hash if pack else None
    
    async def serve_pack(self, name: str) -> Optional[web.FileResponse]:
        """提供资源包下载"""
        pack = self.get_pack(name)
        if not pack:
            return None
        
        if pack.is_directory:
            # 动态压缩目录
            zip_path = await self._create_zip_from_directory(pack.path, pack.name)
            if zip_path and zip_path.exists():
                return web.FileResponse(
                    path=zip_path,
                    headers={
                        'Content-Disposition': f'attachment; filename="{pack.name}.zip"',
                        'Content-Type': 'application/zip'
                    }
                )
            else:
                return None
        else:
            # 直接返回 .zip 文件
            return web.FileResponse(
                path=pack.path,
                headers={
                    'Content-Disposition': f'attachment; filename="{pack.name}.zip"',
                    'Content-Type': 'application/zip'
                }
            )
    
    async def _create_zip_from_directory(self, dir_path: Path, pack_name: str) -> Optional[Path]:
        """从目录创建 zip 文件"""
        try:
            # 创建临时 zip 文件
            zip_path = self.temp_dir / f"{pack_name}_{int(time.time())}.zip"
            
            with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zip_file:
                for item in dir_path.rglob('*'):
                    if item.is_file():
                        # 计算相对路径
                        arcname = item.relative_to(dir_path)
                        zip_file.write(item, arcname)
            
            print(f"📦 已创建临时 zip 文件: {zip_path}")
            return zip_path
            
        except Exception as e:
            print(f"❌ 创建 zip 文件失败 {dir_path}: {e}")
            return None

    def __del__(self):
        """析构函数，确保停止文件监控"""
        self.stop_file_monitoring()


class ResourcePackFileHandler(FileSystemEventHandler):
    """资源包文件系统事件处理器"""
    
    def __init__(self, packs_manager: PacksManager):
        self.packs_manager = packs_manager
        super().__init__()
    
    def on_created(self, event: FileSystemEvent):
        """文件/目录创建事件"""
        if not event.is_directory and event.src_path.endswith('.zip'):
            print(f"📦 检测到新的 ZIP 资源包: {event.src_path}")
            self.packs_manager.schedule_rescan()
        elif event.is_directory:
            # 检查新创建的目录是否是资源包
            dir_path = Path(event.src_path)
            if self.packs_manager._is_resource_pack_directory(dir_path):
                print(f"📁 检测到新的目录资源包: {event.src_path}")
                self.packs_manager.schedule_rescan()
    
    def on_deleted(self, event: FileSystemEvent):
        """文件/目录删除事件"""
        if not event.is_directory and event.src_path.endswith('.zip'):
            print(f"🗑️ 检测到 ZIP 资源包被删除: {event.src_path}")
            self.packs_manager.schedule_rescan()
        elif event.is_directory:
            print(f"🗑️ 检测到目录被删除: {event.src_path}")
            self.packs_manager.schedule_rescan()
    
    def on_modified(self, event: FileSystemEvent):
        """文件修改事件"""
        if not event.is_directory:
            if event.src_path.endswith('.zip'):
                print(f"📝 检测到 ZIP 资源包被修改: {event.src_path}")
                self.packs_manager.schedule_rescan()
            elif event.src_path.endswith('pack.mcmeta'):
                print(f"📝 检测到 pack.mcmeta 被修改: {event.src_path}")
                self.packs_manager.schedule_rescan()
    
    def on_moved(self, event: FileSystemEvent):
        """文件/目录移动事件"""
        if not event.is_directory and (event.src_path.endswith('.zip') or event.dest_path.endswith('.zip')):
            print(f"🔄 检测到 ZIP 资源包被移动: {event.src_path} -> {event.dest_path}")
            self.packs_manager.schedule_rescan()
        elif event.is_directory:
            print(f"🔄 检测到目录被移动: {event.src_path} -> {event.dest_path}")
            self.packs_manager.schedule_rescan()
