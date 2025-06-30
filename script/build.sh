#!/bin/bash

# 更新最新分支
git pull

# 如果没有提供参数，使用默认的BukkitServer目录
if [ $# -eq 0 ]; then
    PROJECT_DIR="BukkitServer"
else
    # 检查是否提供了项目目录作为参数
    if [ "$#" -ge 2 ]; then
        echo "Usage: $0 [directory]"
        exit 1
    fi
    PROJECT_DIR="$1"
fi

update_server() {
    local module=$1
    cd ../server
    if [[ "$module" ]]; then
	echo "build ${module}"
        ./gradlew "$module":build
    else
	echo "build server"
        ./gradlew build
    fi
}

update_sheet() {
    cd ../tool/gen-tool/
    python3 tool_cmd.py
}

if [[ "$PROJECT_DIR" == "all" ]]; then
    echo "build all"
    update_server
    update_sheet
elif [[ "$PROJECT_DIR" == "BukkitServer" ]]; then
    update_server "server-bukkit"
elif [[ "$PROJECT_DIR" == "BrokerServer" ]]; then
    update_server "server-broker"
elif [[ "$PROJECT_DIR" == "AuthServer" ]]; then
    update_server "server-auth"
elif [[ "$PROJECT_DIR" == "SharedServer" ]]; then
    update_server "server-shared"
elif [[ "$PROJECT_DIR" == "LogServer" ]]; then
    update_server "server-log"
elif [[ "$PROJECT_DIR" == "DatabaseServer" ]]; then
    update_server "server-database"
elif [[ "$PROJECT_DIR" == "sheet" ]]; then
	update_sheet
else
	echo "Usage:  ./build.sh [BukkitServer|BrokerServer|AuthServer|SharedServer|LogServer|DatabaseServer|sheet|all]"
	exit 1
fi
