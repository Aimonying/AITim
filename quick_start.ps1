# 快速构建 APK - PowerShell 脚本
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TingJiZhuShou APK 构建工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 设置 Java 环境
$env:JAVA_HOME = "f:\APP\zhitingji\tools\jdk-17.0.11+9"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "✅ Java 环境已配置：" -ForegroundColor Green
Write-Host "   JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Gray
java -version 2>&1 | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
Write-Host ""

Write-Host "📋 下一步操作选项：" -ForegroundColor Yellow
Write-Host "1. 使用 Android Studio 构建（推荐，最简单）" -ForegroundColor White
Write-Host "2. 尝试自动下载 Android SDK 组件" -ForegroundColor White
Write-Host "3. 查看详细文档" -ForegroundColor White
Write-Host ""

$choice = Read-Host "请输入选择 (1-3)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "📝 Android Studio 构建步骤：" -ForegroundColor Cyan
        Write-Host "1. 下载并安装 Android Studio" -ForegroundColor White
        Write-Host "   https://developer.android.google.cn/studio" -ForegroundColor Blue
        Write-Host ""
        Write-Host "2. 打开项目文件夹：" -ForegroundColor White
        Write-Host "   f:\APP\zhitingji\TingJiZhuShou" -ForegroundColor Gray
        Write-Host ""
        Write-Host "3. 等待同步完成（会自动下载 SDK）" -ForegroundColor White
        Write-Host ""
        Write-Host "4. 菜单：Build -> Build Bundle(s) / APK(s) -> Build APK(s)" -ForegroundColor White
        Write-Host ""
        Write-Host "📄 详细说明请查看：README_BUILD.md" -ForegroundColor Yellow
    }
    "2" {
        Write-Host ""
        Write-Host "⚠️  注意：此步骤需要能够访问 Google 服务器" -ForegroundColor Yellow
        Write-Host "   如果网络连接失败，请使用方案 1 (Android Studio)" -ForegroundColor Yellow
        Write-Host ""
        
        $confirm = Read-Host "继续尝试？(Y/N)"
        if ($confirm -eq "Y" -or $confirm -eq "y") {
            Write-Host ""
            Write-Host "🚀 开始构建 Debug APK..." -ForegroundColor Cyan
            try {
                Set-Location "f:\APP\zhitingji\TingJiZhuShou"
                & ".\gradlew.bat" assembleDebug
            }
            catch {
                Write-Host "❌ 构建失败：$_" -ForegroundColor Red
                Write-Host ""
                Write-Host "💡 建议使用 Android Studio 构建" -ForegroundColor Yellow
            }
        }
    }
    "3" {
        Write-Host ""
        Write-Host "📄 正在打开文档..." -ForegroundColor Cyan
        if (Test-Path "README_BUILD.md") {
            notepad "README_BUILD.md"
        }
        else {
            Write-Host "文档不存在，请查看：README_BUILD.md" -ForegroundColor Red
        }
    }
    default {
        Write-Host "❌ 无效选择" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
