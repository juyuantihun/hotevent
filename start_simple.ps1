# 简单启动脚本
Write-Host "正在启动Hot Events应用程序..."

# 终止现有的Java进程
taskkill /f /im java.exe 2>$null

# 等待一下
Start-Sleep 2

# 启动应用程序
Write-Host "开始编译和启动..."
mvn spring-boot:run -DskipTests -Dspring-boot.run.fork=false