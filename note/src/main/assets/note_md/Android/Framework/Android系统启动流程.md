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

初始化和启动属性服务，井且启动 Zygote 进程。

#### init入口函数


init的入口函数为main，目录为 **system/core/init/init.cpp**

- 创建文件并挂载（文件系统）
- 初始化属性相关资源（主题资源）
- 解析 init.rc 配置文件

init.rc是一个配置文件，内部由Android初始化语言编写（Android Init Language）编写的脚本，它主要包含五种类型语句：Action、Commands、Services、Options和Import。

init进程解析和执行脚本过程：①解析启动脚本  ②讲解析脚本中对应的操作加到action_queue队列中  ③创建命令，加到action_list和action_queue中  ④执行命令，创建并守护服务。

需要注意的是在Android 7.0中对init.rc文件进行了拆分，每个服务一个rc文件。我们要分析的zygote服务的启动脚本则在init.zygoteXX.rc中定义，这里拿64位处理器为例，init.zygote64.rc的代码如下所示。
**system/core/rootdir/init.zygote64.rc**

```ini
service zygote /system/bin/app_process64 -Xzygote /system/bin --zygote --start-system-server
```

#### 解析service

解析service，会用到两个函数，一个是ParseSection，它会解析service的rc文件，比如上文讲到的init.zygote64.rc，ParseSection函数主要用来搭建service的架子。另一个是ParseLineSection，用于解析子项。

#### 启动zygote

在 **system/core/init/service.cpp** 中

```
bool Service::StartIfNotDisabled() {
    if (!(flags_ & SVC_DISABLED)) {
        return Start();
    } else {
        flags_ |= SVC_DISABLED_START;
    }
    return true;
}
```

Start方法：

```
 pid_t pid = fork();//1.fork函数创建子进程
    if (pid == 0) {//运行在子进程中
        umask(077);
        for (const auto& ei : envvars_) {
            add_environment(ei.name.c_str(), ei.value.c_str());
        }
        for (const auto& si : sockets_) {
            int socket_type = ((si.type == "stream" ? SOCK_STREAM :
                                (si.type == "dgram" ? SOCK_DGRAM :
                                 SOCK_SEQPACKET)));
            const char* socketcon =
                !si.socketcon.empty() ? si.socketcon.c_str() : scon.c_str();

            int s = create_socket(si.name.c_str(), socket_type, si.perm,
                                  si.uid, si.gid, socketcon);
            if (s >= 0) {
                PublishSocket(si.name, s);
            }
        }
...
        //2.通过execve执行程序
        if (execve(args_[0].c_str(), (char**) &strs[0], (char**) ENV) < 0) {
            ERROR("cannot execve('%s'): %s\n", args_[0].c_str(), strerror(errno));
        }

        _exit(127);
    }
...
    return true;
```



### 5.Zygote进程启动

创建虚拟机并为虚拟机注册 JNI 方法，创建服务器端 Socket，启动 SystemServer 程。

### 6.SystemServer进程启动

启动 Binder 线程池和 SystemServiceManager，并且启动各种系统服务。

### 7.Launcher启动

被 SystemServer 进程启动的 ActivityManagerService 会启动 Launcher，Launcher 启动后会将己安装应用的快捷图标显示到界面上。

