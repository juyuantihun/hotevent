#!/bin/bash

# 数据库连接信息
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="hot_events_db"
DB_USER="root"
DB_PASS="your_password"  # 请替换为您的数据库密码

# 执行SQL文件
echo "正在禁用外键约束..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME < ../src/main/resources/db/migration/V3__disable_foreign_keys.sql

# 检查执行结果
if [ $? -eq 0 ]; then
    echo "外键约束已成功禁用！"
else
    echo "禁用外键约束失败，请检查错误信息。"
fi