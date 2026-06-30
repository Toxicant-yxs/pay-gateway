#!/bin/bash

echo "========================================="
echo "  PayGateway 后端服务启动脚本"
echo "========================================="
echo ""

# 检查Java版本
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java，请安装JDK 17+"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
echo "检测到Java版本: $JAVA_VERSION"
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "警告: 推荐使用JDK 17+，当前版本可能存在兼容性问题"
fi

# 解析参数
PROFILE="h2"
if [ "$1" = "mysql" ]; then
    PROFILE="mysql"
    echo "使用MySQL数据库配置"
else
    echo "使用H2内存数据库配置（默认）"
fi

echo ""
echo "正在构建项目..."
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "构建失败！"
    exit 1
fi

echo "构建成功，正在启动服务..."
echo ""
echo "默认账号: admin / admin123"
echo "API文档: http://localhost:8080/api/v1/doc.html"
if [ "$PROFILE" = "h2" ]; then
    echo "H2控制台: http://localhost:8080/api/v1/h2-console"
fi
echo "========================================="
echo ""

java -jar paygateway-admin/target/paygateway-admin-1.0.0-SNAPSHOT.jar --spring.profiles.active=$PROFILE
