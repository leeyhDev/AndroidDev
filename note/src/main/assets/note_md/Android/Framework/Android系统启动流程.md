## Android系统启动流程

先看流程图

![Android系统启动](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/20200715145336-595913.png)

### 1.启动电源以及系统启动

当电源按下时引导芯片代码从预定义的地方（固化在ROM）开始执行。加载引导程序BootLoader到RAM，然后执行。

### 2.引导程序Bootloader

引导程序BootLoader是在Android操作系统开始运行前的一个小程序，它的主要作用是把系统OS拉起来并运行。硬件初始化，接收按键输入。

此时根据不同的按键组合可以进入不同的模式。

- recovery模式：此模式也会启动一个Linux内核，然后进入recovery程序和加载文件系统，可以进行擦除用户数据、系统更新等操作。
- BootLoader模式：fastboot分区刷写（线刷模式）
- 正常模式：启动Android系统

### 3.Linux内核启动（Linux kernel）

内核启动时，设置缓存、被保护存储器、计划列表、加载驱动。当内核完成系统设置时，它首先在系统文件中寻找 “**init.rc**” 文件，并启动 **init** 程。

### 4.init进程启动

init进程的启动过程主要分为以下三部：

1. 创建和挂载启动所需的文件目录。
2. 初始化和启动属性服务。
3. 解析init.rc配置文件并启动Zygote进程。

### 5.Zygote进程启动

Zygote进程启动中承担的主要职责如下：

1. 创建AppRuntime，执行其start方法，启动Zygote进程。。
2. 创建JVM并为JVM注册JNI方法。
3. 使用JNI调用ZygoteInit的main函数进入Zygote的Java FrameWork层。
4. 使用registerZygoteSocket方法创建服务器端Socket，并通过runSelectLoop方法等等AMS的请求去创建新的应用进程。
5. 启动SystemServer进程。

### 6.SystemServer进程启动

SystemService进程被创建后，主要的处理如下：

1. 启动Binder线程池，这样就可以与其他进程进行Binder跨进程通信。
2. 创建SystemServiceManager，它用来对系统服务进行创建、启动和生命周期管理。
3. 启动各种系统服务：引导服务、核心服务、其他服务，共100多种。应用开发主要关注引导服务ActivityManagerService、PackageManagerService和其他服务WindowManagerService、InputManagerService即可。

### 7.Launcher启动

被 SystemServer 进程启动的 ActivityManagerService 会启动 Launcher，Launcher 启动后会将己安装应用的快捷图标显示到界面上。

