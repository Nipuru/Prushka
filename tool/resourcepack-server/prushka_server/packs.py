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
from pathlib import Path
from typing import Dict, List, Optional
from dataclasses import dataclass
import aiohttp
from aiohttp import web


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
        self.config = config
        self.packs_directory = Path(config.get("packs.directory", "data/packs"))
        self.packs: Dict[str, ResourcePack] = {}
        self.temp_dir = Path(tempfile.gettempdir()) / "prushka_packs"
        
        # 确保目录存在（只在不存在时创建）
        if not self.packs_directory.exists():
            self.packs_directory.mkdir(parents=True, exist_ok=True)
            print(f"📁 创建资源包目录: {self.packs_directory}")
        
        if not self.temp_dir.exists():
            self.temp_dir.mkdir(parents=True, exist_ok=True)
            print(f"📁 创建临时目录: {self.temp_dir}")
        
        # 扫描资源包
        self.scan_packs()
    
    def scan_packs(self) -> None:
        """扫描资源包目录"""
        try:
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
