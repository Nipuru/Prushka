"""
配置管理模块
负责加载和管理服务器配置
"""

import os
import toml
from pathlib import Path
from typing import Dict, Any, Optional


class Config:
    """配置管理类"""
    
    def __init__(self, config_path: str = "config/settings.toml", 
                 template_path: str = "config/settings.template.toml"):
        self.config_path = Path(config_path)
        self.template_path = Path(template_path)
        self.config: Dict[str, Any] = {}
        self.configured = False
        
        self._load_config()
    
    def _load_config(self):
        """加载配置文件"""
        try:
            # 如果配置文件不存在，从模板创建
            if not self.config_path.exists():
                if self.template_path.exists():
                    self._create_config_from_template()
                else:
                    self._create_default_config()
            
            # 加载配置文件
            with open(self.config_path, 'r', encoding='utf-8') as f:
                self.config = toml.load(f)
            
            self.configured = True
            print(f"✅ 配置文件加载成功: {self.config_path}")
            
        except Exception as e:
            print(f"❌ 配置文件加载失败: {e}")
            self._create_default_config()
    
    def _create_config_from_template(self):
        """从模板创建配置文件"""
        try:
            with open(self.template_path, 'r', encoding='utf-8') as f:
                template_config = toml.load(f)
            
            # 确保配置目录存在
            self.config_path.parent.mkdir(parents=True, exist_ok=True)
            
            # 写入配置文件
            with open(self.config_path, 'w', encoding='utf-8') as f:
                toml.dump(template_config, f)
            
            print(f"📝 从模板创建配置文件: {self.config_path}")
            
        except Exception as e:
            print(f"❌ 从模板创建配置失败: {e}")
            self._create_default_config()
    
    def _create_default_config(self):
        """创建默认配置"""
        default_config = {
            "server": {
                "host": "0.0.0.0",
                "port": 8080,
                "debug": False
            },
            "packs": {
                "directory": "../../resourcepack"
            },
            "logging": {
                "level": "INFO",
                "file": "logs/server.log"
            }
        }
        
        # 确保配置目录存在
        self.config_path.parent.mkdir(parents=True, exist_ok=True)
        
        # 写入默认配置
        with open(self.config_path, 'w', encoding='utf-8') as f:
            toml.dump(default_config, f)
        
        self.config = default_config
        self.configured = True
        print(f"📝 创建默认配置文件: {self.config_path}")
    
    def get(self, key: str, default: Any = None) -> Any:
        """获取配置值"""
        keys = key.split('.')
        value = self.config
        
        for k in keys:
            if isinstance(value, dict) and k in value:
                value = value[k]
            else:
                return default
        
        return value
    
    def __getitem__(self, key: str) -> Any:
        """支持字典式访问"""
        return self.get(key)
    
    def __contains__(self, key: str) -> bool:
        """检查配置键是否存在"""
        return self.get(key) is not None
