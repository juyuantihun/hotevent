#!/usr/bin/env pwsh

Write-Host "=== 隐藏关联关系菜单验证脚本 ===" -ForegroundColor Green

# 检查是否在正确的目录
if (-not (Test-Path "frontend/src/layout/index.vue")) {
    Write-Host "错误：请在项目根目录运行此脚本" -ForegroundColor Red
    exit 1
}

Write-Host "1. 验证菜单项隐藏..." -ForegroundColor Yellow

# 检查布局文件内容
$layoutContent = Get-Content "frontend/src/layout/index.vue" -Raw

# 检查关联关系菜单是否被注释
if ($layoutContent -match "<!-- 关联关系菜单已隐藏 -->") {
    Write-Host "   ✓ 关联关系菜单已被注释隐藏" -ForegroundColor Green
} else {
    Write-Host "   ✗ 关联关系菜单隐藏标记未找到" -ForegroundColor Red
}

# 检查是否还有未注释的关联关系菜单项
if ($layoutContent -match '<el-menu-item index="/relation">' -and $layoutContent -notmatch '<!-- <el-menu-item index="/relation">') {
    Write-Host "   ✗ 发现未注释的关联关系菜单项" -ForegroundColor Red
} else {
    Write-Host "   ✓ 关联关系菜单项已正确注释" -ForegroundColor Green
}

Write-Host "2. 验证图标导入清理..." -ForegroundColor Yellow

# 检查Share图标是否从导入中移除
if ($layoutContent -match "import.*Share.*from '@element-plus/icons-vue'") {
    Write-Host "   ✗ Share图标仍在导入中" -ForegroundColor Red
} else {
    Write-Host "   ✓ Share图标已从导入中移除" -ForegroundColor Green
}

Write-Host "3. 验证其他菜单项完整性..." -ForegroundColor Yellow

# 检查其他重要菜单项是否仍然存在
$menuItemsToCheck = @("仪表板", "事件管理", "时间线管理", "字典管理")
$allMenuItemsPresent = $true

foreach ($menuItem in $menuItemsToCheck) {
    if ($layoutContent -match $menuItem) {
        Write-Host "   ✓ '$menuItem'菜单项存在" -ForegroundColor Green
    } else {
        Write-Host "   ✗ '$menuItem'菜单项缺失" -ForegroundColor Red
        $allMenuItemsPresent = $false
    }
}

if ($allMenuItemsPresent) {
    Write-Host "   ✓ 所有重要菜单项完整" -ForegroundColor Green
} else {
    Write-Host "   ✗ 部分重要菜单项缺失" -ForegroundColor Red
}

Write-Host "4. 检查代码结构完整性..." -ForegroundColor Yellow

# 检查Vue模板语法
$templateErrors = @()

# 检查是否有未闭合的标签
$openMenuItems = ($layoutContent | Select-String -Pattern "<el-menu-item" -AllMatches).Matches.Count
$closeMenuItems = ($layoutContent | Select-String -Pattern "</el-menu-item>" -AllMatches).Matches.Count

# 注意：注释中的标签不会被Vue解析，所以需要排除注释中的标签
$commentedMenuItems = ($layoutContent | Select-String -Pattern "<!-- <el-menu-item" -AllMatches).Matches.Count

$activeOpenMenuItems = $openMenuItems - $commentedMenuItems
$activeCloseMenuItems = $closeMenuItems - $commentedMenuItems

if ($activeOpenMenuItems -eq $activeCloseMenuItems) {
    Write-Host "   ✓ 菜单项标签匹配" -ForegroundColor Green
} else {
    Write-Host "   ✗ 菜单项标签不匹配 (活动开始:$activeOpenMenuItems, 活动结束:$activeCloseMenuItems)" -ForegroundColor Red
    $templateErrors += "菜单项标签不匹配"
}

# 检查导入语句的完整性
if ($layoutContent -match "} from '@element-plus/icons-vue'") {
    Write-Host "   ✓ 图标导入语句完整" -ForegroundColor Green
} else {
    Write-Host "   ✗ 图标导入语句可能有问题" -ForegroundColor Red
    $templateErrors += "图标导入语句问题"
}

if ($templateErrors.Count -eq 0) {
    Write-Host "   ✓ 未发现明显语法问题" -ForegroundColor Green
} else {
    Write-Host "   ✗ 发现潜在语法问题: $($templateErrors -join ', ')" -ForegroundColor Red
}

Write-Host "5. 检查路由配置..." -ForegroundColor Yellow

# 检查路由配置文件
if (Test-Path "frontend/src/router/index.ts") {
    $routerContent = Get-Content "frontend/src/router/index.ts" -Raw
    if ($routerContent -match "/relation") {
        Write-Host "   ⚠️  路由配置中仍包含关联关系路由" -ForegroundColor Yellow
        Write-Host "      注意：用户仍可直接访问 /relation 路径" -ForegroundColor Yellow
    } else {
        Write-Host "   ✓ 路由配置中未发现关联关系路由" -ForegroundColor Green
    }
} else {
    Write-Host "   ✗ 路由配置文件未找到" -ForegroundColor Red
}

Write-Host "6. 测试建议..." -ForegroundColor Yellow
Write-Host "   请按照以下步骤测试修改效果：" -ForegroundColor Cyan
Write-Host "   1. 启动开发服务器: cd frontend && npm run dev" -ForegroundColor Cyan
Write-Host "   2. 登录系统" -ForegroundColor Cyan
Write-Host "   3. 检查左侧导航栏是否不再显示'关联关系'菜单" -ForegroundColor Cyan
Write-Host "   4. 验证其他菜单项功能正常" -ForegroundColor Cyan
Write-Host "   5. 测试菜单折叠/展开功能" -ForegroundColor Cyan
Write-Host "   6. 检查浏览器控制台是否有错误" -ForegroundColor Cyan

Write-Host ""
Write-Host "=== 验证完成 ===" -ForegroundColor Green

# 显示修改摘要
Write-Host "修改摘要：" -ForegroundColor Yellow
Write-Host "- 隐藏了'关联关系'菜单项（通过注释）" -ForegroundColor White
Write-Host "- 移除了未使用的Share图标导入" -ForegroundColor White
Write-Host "- 保留了代码结构以便将来恢复" -ForegroundColor White
Write-Host "- 其他菜单项保持不变" -ForegroundColor White

# 显示注意事项
Write-Host ""
Write-Host "注意事项：" -ForegroundColor Yellow
Write-Host "- 菜单项已隐藏，但路由配置可能仍然存在" -ForegroundColor White
Write-Host "- 用户可能仍可直接访问 /relation 路径" -ForegroundColor White
Write-Host "- 如需完全禁用，请考虑修改路由配置" -ForegroundColor White

# 打开测试页面
if (Test-Path "test-hide-relation-menu.html") {
    Write-Host "正在打开测试指南页面..." -ForegroundColor Yellow
    Start-Process "test-hide-relation-menu.html"
}