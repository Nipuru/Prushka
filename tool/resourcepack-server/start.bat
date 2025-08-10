@echo off
chcp 65001 >nul
echo 🚀 启动 Prushka 资源包服务器...

REM 检查 Python 是否安装
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python 未安装或不在 PATH 中
    echo 💡 请先安装 Python 3.8 或更高版本
    pause
    exit /b 1
)

echo ✅ Python 已安装

REM 检查并安装依赖
echo 📦 检查依赖包...
pip install -r requirements.txt

if errorlevel 1 (
    echo ❌ 依赖安装失败
    pause
    exit /b 1
)

echo ✅ 依赖安装完成

REM 检查配置文件
if not exist "config\settings.toml" (
    echo ⚠️ 配置文件不存在，正在创建...
    copy "config\settings.template.toml" "config\settings.toml"
    echo ✅ 配置文件已创建，请根据需要修改 config\settings.toml
    echo 💡 特别是资源包目录路径
    pause
)

echo 🎮 启动服务器...
echo 💡 文件监控功能已启用，资源包变化将自动检测
echo 🌐 服务器将在 http://localhost:8080 启动
echo 📁 监控目录: 请查看 config\settings.toml 中的 packs.directory 设置

python run.py

pause
