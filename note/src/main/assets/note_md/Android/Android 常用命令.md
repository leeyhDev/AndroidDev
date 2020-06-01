## Android 常用命令

#### 编译出错时在日志中显示详细信息

```kotlin
gradlew assembleDebug --stacktrace 
```

#### GitHub常用配置


```kotlin
git config --global http.proxy socks5://127.0.0.1:1080
git config --global https.proxy socks5://127.0.0.1:1080

git config --global --unset http.proxy
git config --global --unset https.proxy

npm config delete proxy

# 只对github.com
git config --global http.https://github.com.proxy socks5://127.0.0.1:1080
git config --global --unset http.https://github.com.proxy

ipconfig /flushdns
```

#### Git常用命令

```kotlin
git branch -d dev  删除分支
git merge dev   合并分支
git checkout master 切换分支
git checkout -b dev 创建并切换到分支

git switch -c dev 创建并切换到新的dev分支
git switch master 直接切换到master分支
```

#### adb常用命令


```kotlin
### 边界布局
adb shell setprop debug.layout true 
adb shell setprop debug.layout false

### 查看活动的 Activity
adb shell dumpsys activity activities
adb shell dumpsys window | findstr mCurrentFocus

### 启动指定Activity
adb shell am start -S -R 3 -W com.starpany.app/.MainActivity

adb shell pm uninstall  --user 0 package

###过渡绘制
adb shell setprop debug.hwui.overdraw show
adb shell setprop debug.hwui.overdraw false
```

