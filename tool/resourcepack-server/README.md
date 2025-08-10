# Prushka Resource Pack Server

这是一个专门为 Prushka 项目提供资源包托管服务的高性能 Web 服务器。

## ✨ 功能特性

- 🎮 **Minecraft 资源包托管** - 专门为 Minecraft 资源包提供托管和分发服务
- 🚀 **高性能异步服务器** - 基于 aiohttp 的高性能异步 Web 服务器
- 📦 **智能资源包管理** - 支持资源包的自动发现、缓存和分发
- 🔒 **安全访问控制** - 内置安全验证、跨域控制和速率限制
- 📋 **一键复制 Hash** - 支持一键复制资源包 MD5 Hash 到剪贴板
- 🐳 **Docker 支持** - 完整的容器化部署支持
- 📊 **实时监控** - 提供资源包下载统计和服务器状态监控

## 🚀 快速开始

### 环境要求
- **Python**: 3.8 或更高版本
- **操作系统**: Windows、Linux、macOS
- **内存**: 建议 512MB 以上

### 安装依赖
```bash
pip install -r requirements.txt
```

### 配置服务器
1. 复制配置模板：
   ```bash
   cp config/settings.template.toml config/settings.toml
   ```

2. 编辑 `config/settings.toml`，设置资源包目录：
   ```toml
   [packs]
   directory = "../../resourcepack"  # 修改为你的资源包目录
   ```

### 运行服务器
```bash
# 直接运行
python run.py

# 使用启动脚本
# Windows
start.bat

# Linux/macOS
./start.sh
```

服务器启动后访问 `http://localhost:8080` 查看资源包列表。

## 📁 项目结构

```
resourcepack-server/
├── prushka_server/          # 核心服务器代码
│   ├── __init__.py
│   ├── core.py               # 主程序入口和服务器管理
│   ├── server.py             # HTTP 服务器和路由处理
│   ├── packs.py              # 资源包扫描和管理
│   ├── config.py             # 配置管理和加载
│   └── utils.py              # 工具函数和目录管理
├── config/                   # 配置文件目录
│   ├── settings.toml         # 主配置文件（运行时生成）
│   └── settings.template.toml # 配置模板文件
├── data/                     # 数据存储目录（自动创建）
├── logs/                     # 日志文件目录（自动创建）
├── requirements.txt          # Python 依赖包列表
├── run.py                    # 主启动脚本
├── start.bat                 # Windows 启动脚本
├── start.sh                  # Linux/macOS 启动脚本
├── Dockerfile               # Docker 镜像构建文件
├── docker-compose.yml       # Docker Compose 编排文件
└── README.md                # 项目说明文档
```

## ⚙️ 配置说明

### 服务器配置
```toml
[server]
host = "0.0.0.0"    # 监听地址，0.0.0.0 表示所有网络接口
port = 8080         # 监听端口
debug = false       # 调试模式
```

### 资源包配置
```toml
[packs]
directory = "../../resourcepack"  # 资源包存储目录
```

### 日志配置
```toml
[logging]
level = "INFO"                  # 日志级别
file = "logs/server.log"        # 日志文件路径
```

## 🌐 API 接口

### 主要端点
- `GET /` - 资源包列表页面（带复制 Hash 功能）
- `GET /api/packs` - 获取所有资源包列表（JSON）
- `GET /api/packs/{name}` - 获取指定资源包信息
- `GET /download/{name}` - 下载指定资源包
- `GET /hash/{name}` - 获取资源包 Hash 值
- `GET /debug` - 服务器调试信息

### 使用示例
```bash
# 获取资源包列表
curl http://localhost:8080/api/packs

# 下载资源包
curl -O http://localhost:8080/download/resourcepack_name

# 获取 Hash 值
curl http://localhost:8080/hash/resourcepack_name
```

## 🐳 Docker 部署

### 使用 Docker Compose（推荐）
```bash
# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 手动构建镜像
```bash
# 构建镜像
docker build -t prushka-resourcepack-server .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -v $(pwd)/config:/app/config \
  -v $(pwd)/data:/app/data \
  -v $(pwd)/logs:/app/logs \
  --name prushka-server \
  prushka-resourcepack-server
```

## 🔧 故障排除

### 常见问题
1. **端口被占用**: 修改 `config/settings.toml` 中的 `port` 值
2. **资源包不显示**: 检查 `packs.directory` 路径是否正确
3. **权限错误**: 确保服务器有读取资源包目录的权限

### 日志查看
```bash
# 查看实时日志
tail -f logs/server.log

# 查看 Docker 日志
docker-compose logs -f
```

## 📝 更新日志

### v1.0.0
- ✨ 新增一键复制 Hash 功能
- 🎨 优化用户界面和按钮样式
- 🗂️ 简化配置文件，删除多余配置
- 🐳 优化 Docker 部署配置
- 📚 完善项目文档

### v1.1.0
- 🧹 清理未使用的配置项
- ⚡ 简化配置结构，只保留核心功能
- 🔧 优化配置验证逻辑

## 📄 许可证

本项目为 Prushka 项目的一部分，遵循相应的许可证条款。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

---

**注意**: 首次运行时，服务器会自动创建必要的目录结构。确保配置的资源包目录路径正确，服务器才能正常扫描和提供资源包。
