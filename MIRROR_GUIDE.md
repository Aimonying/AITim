# Android Studio 国内镜像源下载指南

## 📦 Android Studio 安装包下载

### 推荐镜像源（按稳定性排序）

1. **腾讯云镜像**（推荐）
   - 地址：https://mirrors.cloud.tencent.com/android/studio/
   - 特点：速度快、更新及时

2. **清华大学镜像**
   - 地址：https://mirrors.tuna.tsinghua.edu.cn/android/studio/
   - 特点：教育网速度快

3. **华为云镜像**
   - 地址：https://mirrors.huaweicloud.com/androidstudio/
   - 特点：稳定可靠

4. **谷歌中国官网**
   - 地址：https://developer.android.google.cn/studio
   - 特点：官方中国版，中文界面

## 🛠️ Android SDK 镜像配置

### 方案一：Android Studio 中配置 SDK 镜像

1. 打开 Android Studio
2. 进入：`File > Settings > Appearance & Behavior > System Settings > Android SDK`
3. 点击 `SDK Update Sites` 标签
4. 添加以下镜像：

```
腾讯云：https://mirrors.cloud.tencent.com/AndroidSDK/
华为云：https://developer.huawei.com/repo/
```

### 方案二：使用阿里云 Maven 镜像（已为您的项目已配置好！

您的项目 [settings.gradle.kts](file:///f:/APP/zhitingji/TingJiZhuShou/settings.gradle.kts) 已经配置好了阿里云镜像源，无需额外配置。

## 📦 Gradle 分发加速

### 修改 gradle-wrapper.properties

您的项目可以修改 [gradle-wrapper.properties](file:///f:/APP/zhitingji/TingJiZhuShou/gradle/wrapper/gradle-wrapper.properties) 中的 `distributionUrl`：

```properties
# 腾讯云（推荐）
distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-8.2-bin.zip

# 或阿里云
# distributionUrl=https\://mirrors.aliyun.com/gradle/gradle-8.2-bin.zip
```

## 📋 快速下载步骤

1. **下载 Android Studio
   - 访问推荐的镜像源
   - 下载最新版本（Windows: android-studio-xxx-windows.exe

2. **安装 Android Studio
   - 双击运行安装程序
   - 选择安装路径
   - 完成安装向导

3. **打开项目**
   - 选择：`Open an Existing Project`
   - 选择：`f:\APP\zhitingji\TingJiZhuShou`
   - 等待同步完成（会自动下载所需依赖）

4. **构建 APK**
   - 菜单：`Build > Build Bundle(s) / APK(s) > Build APK(s)`

## 🎯 完成！

APK 将输出到：`app/build/outputs/apk/`
