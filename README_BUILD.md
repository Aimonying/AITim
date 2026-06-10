# APK 构建指南

## 已完成的准备工作

✅ **JDK 17 已配置完成**
- 位置：`f:\APP\zhitingji\tools\jdk-17.0.11+9
- 版本：OpenJDK 17.0.11

✅ **项目配置已更新**
- 创建了 `gradlew.bat` 脚本
- 创建了 `local.properties` 配置文件
- 配置了阿里云镜像源
- 临时禁用了 lint 检查

## 需要您需要完成的步骤

### 方案一：使用 Android Studio 构建（推荐）

1. **下载并安装 Android Studio**
   - 访问：https://developer.android.google.cn/studio
   - 下载 Windows 版本并安装

2. **打开项目**
   - 启动 Android Studio
   - 选择 "Open an Existing Project"
   - 选择 `f:\APP\zhitingji\TingJiZhuShou` 目录

3. **等待同步完成**
   - Android Studio 会自动下载所需的 SDK 组件
   - 首次同步可能需要一些时间

4. **构建 APK**
   - 菜单：Build → Build Bundle(s) / APK(s) → Build APK(s)
   - 或使用快捷键：Ctrl + F9

### 方案二：手动配置 Android SDK 并构建

如果您想继续使用命令行构建：

1. **下载 Android Studio 命令行工具**
   - 访问：https://developer.android.google.cn/studio#command-tools
   - 下载 "Command line tools only"

2. **解压并配置**
   ```powershell
   # 在 C:\Users\16707\AppData\Local\Android\Sdk 下创建目录结构
   # 解压命令行工具到 cmdline-tools/latest 目录
   ```

3. **安装必要组件**
   ```powershell
   cd C:\Users\16707\AppData\Local\Android\Sdk\cmdline-tools\latest\bin
   sdkmanager --install "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```

4. **运行构建脚本**
   ```powershell
   cd f:\APP\zhitingji\TingJiZhuShou
   .\build_apk.bat
   ```

## APK 输出位置

构建成功后，APK 文件将位于：
- Debug 版本：`app/build/outputs/apk/debug/app-debug.apk`
- Release 版本：`app/build/outputs/apk/release/app-release.apk`

## 注意事项

- 首次构建会下载很多依赖，需要稳定的网络连接
- Release 版本 APK 可以直接安装到 Android 手机上
- 如果遇到网络问题，建议使用 Android Studio，它会自动处理依赖下载
