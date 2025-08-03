@echo off
REM 数据库连接信息
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=hot_events_db
set DB_USER=root
set DB_PASS=your_password
REM 请替换为您的数据库密码

REM 执行SQL文件
echo 正在禁用外键约束...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% < ..\src\main\resources\db\migration\V3__disable_foreign_keys.sql

REM 检查执行结果
if %ERRORLEVEL% EQU 0 (
    echo 外键约束已成功禁用！
) else (
    echo 禁用外键约束失败，请检查错误信息。
)