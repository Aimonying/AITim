# 🚀 GitHub Actions 自动构建 APK 傻瓜式指南

---

## 📋 准备工作

您需要：
1. 一个 GitHub 账号（免费即可）
2. 电脑上有 Git（如果没有，请安装：https://git-scm.com/downloads）

---

## 🔧 步骤 1：注册 GitHub 账号

1. 打开浏览器，访问：https://github.com/
2. 点击右上角的 **Sign up**（注册）
3. 填写：用户名、邮箱、密码
4. 完成注册验证

---

## 🔧 步骤 2：创建新仓库

1. 登录 GitHub 后，点击右上角的 **+** 号
2. 选择 **New repository**（新建仓库）
3. 填写信息：
   - **Repository name**（仓库名）：随便起一个名字，比如 `TingJiZhuShou`
   - **Description**（描述）：可选，填写应用描述
   - 保持其他选项默认，点击 **Create repository**（创建仓库）

---

## 🔧 步骤 3：安装 Git（如果没安装）

1. 下载 Git：https://git-scm.com/downloads
2. 安装时保持所有选项默认，一路点击 **Next**
3. 安装完成后，打开 **命令提示符**（Windows）或 **终端**（Mac/Linux）
4. 输入以下命令配置 Git：
   ```bash
   git config --global user.name "你的GitHub用户名"
   git config --global user.email "你的GitHub邮箱"
   ```

---

## 🔧 步骤 4：上传项目代码到 GitHub

1. **打开命令提示符/终端**
2. 进入项目目录：
   ```bash
   cd f:\APP\zhitingji\TingJiZhuShou
   ```
3. 初始化 Git：
   ```bash
   git init
   ```
4. 添加所有文件：
   ```bash
   git add .
   ```
5. 提交代码：
   ```bash
   git commit -m "Initial commit"
   ```
6. 关联 GitHub 仓库（把下面的 URL 换成您的仓库地址）：
   ```bash
   git remote add origin https://github.com/你的用户名/TingJiZhuShou.git
   ```
7. 上传代码：
   ```bash
   git push -u origin main
   ```
   - 如果提示输入用户名密码，输入您的 GitHub 账号信息

---

## 🔧 步骤 5：触发 GitHub Actions 构建

1. 打开浏览器，进入您的 GitHub 仓库
2. 点击上方的 **Actions**（动作）标签
3. 您会看到 GitHub Actions 正在自动运行
4. 等待几分钟，构建会自动完成

---

## 🔧 步骤 6：下载生成的 APK

1. 构建完成后，点击 **Actions** 标签
2. 在左侧找到 **Build Android APK** 工作流
3. 点击最新的一次运行
4. 向下滚动到 **Artifacts**（产物）部分
5. 点击 **app-debug.apk** 下载

---

## 📱 步骤 7：安装到手机

1. 把下载的 `app-debug.apk` 文件传到手机上（用微信、QQ、邮箱等）
2. 在手机上打开文件管理器，找到这个 APK 文件
3. 点击安装（如果提示“未知来源”，请在设置中允许安装）
4. 安装完成后，打开应用即可使用！

---

## 🎉 恭喜！

您已经成功构建了 APK 文件！

---

## ❓ 常见问题

### Q1：上传代码时提示密码错误？
A：GitHub 已经不再支持密码登录，请使用 **Personal Access Token**：
1. 登录 GitHub → 点击头像 → **Settings**（设置）
2. 点击 **Developer settings** → **Personal access tokens**
3. 点击 **Generate new token**（生成新令牌）
4. 勾选 **repo** 权限，点击生成
5. 复制生成的令牌，在命令行输入密码时粘贴这个令牌

### Q2：构建失败怎么办？
A：点击 **Actions** → 查看具体的构建日志 → 通常是依赖下载问题，重新运行即可

### Q3：手机安装不了 APK？
A：确保手机允许安装“未知来源”的应用，并且手机是 Android 系统

---

## 📞 联系我

如果遇到任何问题，可以给我留言，我会帮您解决！