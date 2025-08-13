#!/usr/bin/env python3
"""Resource Pack Server 启动脚本"""

import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from impl import core

if __name__ == "__main__":
    core.main()
