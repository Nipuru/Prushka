# Prushka 资源包服务器

一个高性能的 Minecraft 资源包分发服务器，支持自动文件监控和实时更新。

## ✨ 特性

- 🚀 **高性能**: 基于 aiohttp 的异步 Web 服务器
- 📦 **多格式支持**: 支持 ZIP 文件和目录形式的资源包
- 🔍 **自动监控**: 实时监控资源包目录变化，自动更新
- 🌐 **RESTful API**: 提供完整的资源包管理 API
- 📊 **实时状态**: 内置调试界面，实时查看服务器状态
- 💾 **智能缓存**: 自动创建临时 ZIP 文件，优化下载体验
- 🛡️ **强制退出**: 支持 Ctrl+C 强制退出，确保所有后台线程正确关闭

## 🚀 快速开始

### 1. 安装依赖

```bash
# 自动安装（推荐）
python install.py

# 或手动安装
pip install -r requirements.txt
```

### 2. 配置设置

复制配置文件模板并修改：

```bash
cp config/settings.template.toml config/settings.toml
```

编辑 `config/settings.toml`，特别是资源包目录路径。

### 3. 启动服务器

```bash
# Windows
start.bat

# Linux/macOS
./start.sh

# 手动启动
python run.py
```

### 4. 测试强制退出

```bash
# 测试 Ctrl+C 强制退出功能
python test_force_exit.py
```

## 🔍 文件监控功能

### 自动检测变化

服务器会自动监控资源包目录，检测以下变化：

- ✨ **新增资源包**: 添加新的 ZIP 文件或目录
- 🗑️ **删除资源包**: 移除资源包文件或目录  
- 📝 **修改资源包**: 更新 ZIP 文件或 pack.mcmeta
- 🔄 **移动资源包**: 重命名或移动资源包

### 配置选项

```toml
[packs]
# 是否启用文件监控
file_monitor = true
# 文件监控间隔（秒）
file_monitor_interval = 1.0
# 扫描冷却时间（秒，防止频繁扫描）
scan_cooldown = 2.0
```

### 手动重新扫描

如果需要手动触发重新扫描，可以调用 API：

```bash
curl -X POST http://localhost:8080/api/rescan
```

## 🌐 API 接口

### 资源包列表
```
GET /api/packs
```

### 获取特定资源包
```
GET /api/packs/{name}
```

### 下载资源包
```
GET /download/{name}
```

### 获取资源包 Hash
```
GET /hash/{name}
```

### 手动重新扫描
```
POST /api/rescan
```

### 调试信息
```
GET /debug
```

## 📁 资源包格式

### ZIP 文件
- 直接上传 `.zip` 文件到资源包目录
- 自动检测并解析 `pack.mcmeta`

### 目录形式
- 创建包含 `pack.mcmeta` 的目录
- 服务器会动态压缩并提供下载

## 🛠️ 依赖要求

- Python 3.8+
- aiohttp >= 3.8.0
- watchdog >= 3.0.0 (文件监控)
- toml >= 0.10.2
- 其他依赖见 `requirements.txt`

## 📝 注意事项

1. **文件监控**: 需要安装 `watchdog` 库才能使用自动监控功能
2. **权限**: 确保服务器有读取资源包目录的权限
3. **性能**: 大量文件变化时会有短暂延迟，这是正常现象
4. **冷却时间**: 扫描冷却时间防止频繁扫描，可根据需要调整

## 🔧 故障排除

### 文件监控不工作
```bash
pip install watchdog
```

### 权限问题
确保服务器进程有访问资源包目录的权限

### 端口被占用
修改 `config/settings.toml` 中的端口设置

## 📄 许可证

本项目采用 MIT 许可证。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

**享受自动化的资源包管理体验！** 🎮✨

## 🛡️ 强制退出功能

服务器支持 Ctrl+C 强制退出，确保在关闭时：

- ✅ 立即停止文件监控线程
- ✅ 关闭所有 HTTP 连接
- ✅ 清理临时文件和资源
- ✅ 强制退出所有后台线程
- ✅ 程序完全退出，不留僵尸进程

**使用方法**: 在服务器运行时按 `Ctrl+C`，服务器将立即强制退出。

**测试方法**: 运行 `python test_force_exit.py` 来测试强制退出功能。
