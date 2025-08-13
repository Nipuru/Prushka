#!/usr/bin/env python3
"""
Prushka Resource Pack Server
启动脚本
"""

import sys
import os

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from resource_server import core

if __name__ == "__main__":
    core.main()
