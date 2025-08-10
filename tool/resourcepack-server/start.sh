#!/bin/bash

# Prushka 资源包服务器启动脚本

echo "========================================"
echo "   Prushka 资源包服务器启动脚本"
echo "========================================"
echo

# 检查 Python 环境
echo "正在检查 Python 环境..."
if ! command -v python3 &> /dev/null; then
    echo "❌ 错误：未找到 Python 3 环境"
    echo "请先安装 Python 3.8 或更高版本"
    exit 1
fi

echo "✅ Python 环境检查通过"
echo

# 检查虚拟环境
if [ -d "venv" ]; then
    echo "发现虚拟环境，正在激活..."
    source venv/bin/activate
    echo "✅ 虚拟环境已激活"
    echo
fi

# 安装依赖
echo "正在安装依赖包..."
pip install -r requirements.txt
if [ $? -ne 0 ]; then
    echo "❌ 依赖安装失败"
    exit 1
fi

echo "✅ 依赖安装完成"
echo

# 启动服务器
echo "正在启动服务器..."
echo "按 Ctrl+C 停止服务器"
echo

python3 run.py
