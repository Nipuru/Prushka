"""工具函数模块"""

import os
import time
import hashlib
import zipfile
from pathlib import Path
from typing import Dict, Any, Optional, List
import json


def format_file_size(size_bytes: int) -> str:
    """格式化文件大小"""
    if size_bytes == 0:
        return "0 B"
    
    size_names = ["B", "KB", "MB", "GB", "TB"]
    i = 0
    while size_bytes >= 1024 and i < len(size_names) - 1:
        size_bytes /= 1024.0
        i += 1
    
    return f"{size_bytes:.2f} {size_names[i]}"


def format_timestamp(timestamp: float) -> str:
    """格式化时间戳"""
    return time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(timestamp))


def calculate_file_hash(file_path: Path, algorithm: str = "md5") -> str:
    """计算文件哈希值"""
    hash_obj = hashlib.new(algorithm)
    
    try:
        with open(file_path, "rb") as f:
            for chunk in iter(lambda: f.read(4096), b""):
                hash_obj.update(chunk)
        return hash_obj.hexdigest()
    except Exception:
        return ""


def validate_zip_file(file_path: Path) -> bool:
    """验证 ZIP 文件的有效性"""
    try:
        with zipfile.ZipFile(file_path, 'r') as zip_file:
            return 'pack.mcmeta' in zip_file.namelist()
    except Exception:
        return False


def extract_pack_info(zip_path: Path) -> Optional[Dict[str, Any]]:
    """从 ZIP 文件中提取资源包信息"""
    try:
        with zipfile.ZipFile(zip_path, 'r') as zip_file:
            if 'pack.mcmeta' not in zip_file.namelist():
                return None
            
            with zip_file.open('pack.mcmeta', 'r') as meta_file:
                meta_content = meta_file.read().decode('utf-8')
                meta_data = json.loads(meta_content)
                
                pack_info = meta_data.get('pack', {})
                return {
                    'description': pack_info.get('description', ''),
                    'pack_format': pack_info.get('pack_format', 22)
                }
                
    except Exception as e:
        print(f"提取资源包信息失败: {e}")
        return None


def create_directory_structure(base_path: Path) -> None:
    """创建必要的目录结构"""
    directories = [
        "logs",
        "config"
    ]
    
    for directory in directories:
        dir_path = base_path / directory
        if not dir_path.exists():
            dir_path.mkdir(parents=True, exist_ok=True)
            print(f"创建目录: {directory}")
        else:
            print(f"目录已存在: {directory}")


def get_safe_filename(filename: str) -> str:
    """获取安全的文件名"""
    unsafe_chars = '<>:"/\\|?*'
    for char in unsafe_chars:
        filename = filename.replace(char, '_')
    
    if len(filename) > 100:
        name, ext = os.path.splitext(filename)
        filename = name[:100-len(ext)] + ext
    
    return filename


def log_message(message: str, level: str = "INFO", log_file: Optional[str] = None) -> None:
    """记录日志消息"""
    timestamp = format_timestamp(time.time())
    log_entry = f"[{timestamp}] [{level}] {message}"
    
    print(log_entry)
    
    if log_file:
        try:
            with open(log_file, 'a', encoding='utf-8') as f:
                f.write(log_entry + '\n')
        except Exception:
            pass


def validate_config(config: Dict[str, Any]) -> List[str]:
    """验证配置的有效性"""
    errors = []
    
    required_fields = [
        'server.host',
        'server.port',
        'packs.directory'
    ]
    
    for field in required_fields:
        if not config.get(field):
            errors.append(f"缺少必需的配置项: {field}")
    
    port = config.get('server.port')
    if port and (not isinstance(port, int) or port < 1 or port > 65535):
        errors.append("端口号必须是 1-65535 之间的整数")
    
    return errors


def create_sample_resource_pack(output_dir: Path) -> None:
    """创建示例资源包"""
    try:
        pack_dir = output_dir / "sample_pack"
        pack_dir.mkdir(parents=True, exist_ok=True)
        
        pack_meta = {
            "pack": {
                "description": "示例资源包 - 用于测试",
                "pack_format": 22
            }
        }
        
        with open(pack_dir / "pack.mcmeta", 'w', encoding='utf-8') as f:
            json.dump(pack_meta, f, indent=4, ensure_ascii=False)
        
        assets_dir = pack_dir / "assets" / "minecraft"
        assets_dir.mkdir(parents=True, exist_ok=True)
        
        readme_content = """这是一个示例资源包，用于测试资源包服务器功能。

目录结构:
- pack.mcmeta: 资源包元数据
- assets/minecraft/: Minecraft 资源目录

您可以将自己的资源包文件放在这里，然后压缩成 ZIP 文件。
"""
        
        with open(pack_dir / "README.txt", 'w', encoding='utf-8') as f:
            f.write(readme_content)
        
        print(f"示例资源包已创建: {pack_dir}")
        
    except Exception as e:
        print(f"创建示例资源包失败: {e}")


def cleanup_old_files(directory: Path, max_age_days: int = 30) -> int:
    """清理旧文件"""
    cleaned_count = 0
    current_time = time.time()
    max_age_seconds = max_age_days * 24 * 3600
    
    try:
        for file_path in directory.rglob("*"):
            if file_path.is_file():
                file_age = current_time - file_path.stat().st_mtime
                if file_age > max_age_seconds:
                    file_path.unlink()
                    cleaned_count += 1
        
        if cleaned_count > 0:
            print(f"清理了 {cleaned_count} 个旧文件")
            
    except Exception as e:
        print(f"清理旧文件失败: {e}")
    
    return cleaned_count
