@echo off
chcp 65001 >nul
title Prushka 资源包服务器

echo.
echo ========================================
echo    Prushka 资源包服务器启动脚本
echo ========================================
echo.

echo 正在检查 Python 环境...
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未找到 Python 环境
    echo 请先安装 Python 3.8 或更高版本
    pause
    exit /b 1
)

echo ✅ Python 环境检查通过
echo.

echo 正在检查依赖包...
pip show aiohttp >nul 2>&1
if errorlevel 1 (
    echo 正在安装依赖包...
    pip install -r requirements.txt
    if errorlevel 1 (
        echo ❌ 依赖安装失败
        pause
        exit /b 1
    )
    echo ✅ 依赖安装完成
) else (
    echo ✅ 依赖包已安装
)
echo.

echo 正在启动服务器...
echo 按 Ctrl+C 停止服务器
echo.

python run.py

pause
