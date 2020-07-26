## Zygote

### 概述

在 Android 系统中，DVM (Dalvik拟机）和 ART、应用程序进程以及运行系统的关键服务的SystemServer进程都是由Zygote进来创建的，我们也将它称为孵化器。它通过 **fock**（**复制进程**）的形式来创建应用程序进程和 SystemServer 程，由于 Zygote 进程在启动时会创建 DVM或者 ART，因此通过 fock 而创建的应用程序进程和 SystemServer 进程可以在内部获取一个 DVM 或者 ART 的实例副本。Zygote 程是在 init 进程启动时创建的，起初 Zygote 进程的名称并不是叫 ”zygote“，而是叫 “app_process”，这个名称是在 Android.mk 中定义的，Zygote 进程启动后，Linux 系统下的 petri 系统会调用 app_process，将其名称换成了 “zygote”。

### Zygote的作用是什么（What）？

- 启动 SystemServer
- 孵化应用进程 

### Zygote的工作原理是什么（Why）？

- 启动进程 fork + handle
- 本地 socket 通信

```java
// fork + handle （Zygote进程启动应用进程）
pid_t pid = fork();
if (pid == 0) {
    // child process
} else {
    // parent process
}
// fork + execve (init进程启动Zygote进程)
pid_t pid = fork();
if (pid == 0) {
    // child process
    execve(path, argv, env);
} else {
    // parent process
}
```

socket 处理消息

```java
boolean runOnce() {
    String[] args = readArgumentList();
    int pid = Zygote.forkAndSpecialize();

    if (pid == 0) {
        // in child
        handleChildProc(args,...);//ActivityThread.main()
        return true;
    }
}
```
**信号处理**：子进程挂掉父进程就会收到SIGCHLD信号，父就会重启子进程。

### Zygote的启动流程是什么（How）？

#### 启动三段式

Android 进程启动三段式：启动进程  $\Longrightarrow$  准备工作   $\Longrightarrow$  LOOP （接收消息，处理消息）

**native 世界**：启动 Android 虚拟机 $\Longrightarrow$ 注册 Android 的 JNI 函数  $\Longrightarrow$   反射调用 ZygoteInit.main 进入 java 世界

**java 世界**：注册本地 socket  $\Longrightarrow$ 准备工作  $\Longrightarrow$ LOOP

**准备工作：加载常用类、主题资源、共享库**

![Zgyote](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/202007182133-394640.png)

1. 解析 init.zygote.rc 中的参数，创建 AppRuntime 并调用 AppRuntime.start() 方法；
2. 调用 AndroidRuntime 的 startVM() 方法创建虚拟机，再调用 startReg() 注册JNI函数；
3. 通过 JNI 方式调用 ZygoteInit.main()，第一次进入 Java 世界；
4. registerZygoteSocket() 建立本地 socket 通道，zygote 作为通信的服务端，用于响应客户端请求；
5. preload() 预加载通用类、drawable 和 color 资源、openGL 以及共享库以及 WebView，用于提高 app 启动效率；
6. zygote 完毕大部分工作，接下来再通过 startSystemServer()，fork 得力帮手 system_server 进程，也是上层 framework 的运行载体。
7. zygote 功成身退，调用 runSelectLoop()，随时待命，当接收到请求创建新进程请求时立即唤醒并执行相应工作。

#### init 进程解析 **init.zygote.rc**

Zygote 是由 **init 进程**通过解析 **init.zygote.rc** 配置文件而创建的，zygote 所对应的可执行程序 app_process，所对应的源文件是App_main.cpp，进程名为 zygote。

```
service zygote /system/bin/app_process -Xzygote /system/bin --zygote --start-system-server
    class main
    socket zygote stream 660 root system
    onrestart write /sys/android_power/request_state wake
    onrestart write /sys/power/state on
    onrestart restart media
    onrestart restart netd
```

Zygote进程能够重启的地方:

- servicemanager进程被杀; (onresart)
- surfaceflinger进程被杀; (onresart)
- Zygote进程自己被杀; (oneshot=false)
- system_server进程被杀; (waitpid)

#### App_main.cpp main()

```c++
int main(int argc, char* const argv[])
{
    //传到的参数argv为“-Xzygote /system/bin --zygote --start-system-server”
    AppRuntime runtime(argv[0], computeArgBlockSize(argc, argv));
    argc--; argv++; //忽略第一个参数
    ......
    //参数解析
    bool zygote = false;
    bool startSystemServer = false;
    bool application = false;
    String8 niceName;
    String8 className;
    ++i;
    while (i < argc) {
        const char* arg = argv[i++];
        if (strcmp(arg, "--zygote") == 0) {
            zygote = true;
            //对于64位系统nice_name为zygote64; 32位系统为zygote
            niceName = ZYGOTE_NICE_NAME;
        } else if (strcmp(arg, "--start-system-server") == 0) {
            startSystemServer = true;
        } else if (strcmp(arg, "--application") == 0) {
            application = true;
        } else if (strncmp(arg, "--nice-name=", 12) == 0) {
            niceName.setTo(arg + 12);
        } else if (strncmp(arg, "--", 2) != 0) {
            className.setTo(arg);
            break;
        } else {
            --i;
            break;
        }
    }
    Vector<String8> args;
    if (!className.isEmpty()) {
        // 运行application或tool程序
        args.add(application ? String8("application") : String8("tool"));
        runtime.setClassNameAndArgs(className, argc - i, argv + i);
    } else {
        //进入zygote模式，创建 /data/dalvik-cache路径
        maybeCreateDalvikCache();
        if (startSystemServer) {
            args.add(String8("start-system-server"));
        }
        char prop[PROP_VALUE_MAX];
        if (property_get(ABI_LIST_PROPERTY, prop, NULL) == 0) {
            return 11;
        }
        String8 abiFlag("--abi-list=");
        abiFlag.append(prop);
        args.add(abiFlag);

        for (; i < argc; ++i) {
            args.add(String8(argv[i]));
        }
    }

    //设置进程名
    if (!niceName.isEmpty()) {
        runtime.setArgv0(niceName.string());
        set_process_name(niceName.string());
    }
    if (zygote) {
        // 启动AppRuntime
        runtime.start("com.android.internal.os.ZygoteInit", args, zygote);
    } else if (className) {
        runtime.start("com.android.internal.os.RuntimeInit", args, zygote);
    } else {
        //没有指定类名或zygote，参数错误
        return 10;
    }
}
```

#### AndroidRuntime.cpp  start()

```c++
void AndroidRuntime::start(const char* className, const Vector<String8>& options, bool zygote)
{
    static const String8 startSystemServer("start-system-server");
    ...
    JNIEnv* env;
    // 虚拟机创建
    if (startVm(&mJavaVM, &env, zygote) != 0) {
        return;
    }
    onVmCreated(env);
    // JNI方法注册
    if (startReg(env) < 0) {
        return;
    }

    jclass stringClass;
    jobjectArray strArray;
    jstring classNameStr;

    //等价 strArray= new String[options.size() + 1];
    stringClass = env->FindClass("java/lang/String");
    strArray = env->NewObjectArray(options.size() + 1, stringClass, NULL);

    //等价 strArray[0] = "com.android.internal.os.ZygoteInit"
    classNameStr = env->NewStringUTF(className);
    env->SetObjectArrayElement(strArray, 0, classNameStr);

    //等价 strArray[1] = "start-system-server"；
    // strArray[2] = "--abi-list=xxx"；
    //其中xxx为系统响应的cpu架构类型，比如arm64-v8a.
    for (size_t i = 0; i < options.size(); ++i) {
        jstring optionsStr = env->NewStringUTF(options.itemAt(i).string());
        env->SetObjectArrayElement(strArray, i + 1, optionsStr);
    }

    //将"com.android.internal.os.ZygoteInit"转换为"com/android/internal/os/ZygoteInit"
    char* slashClassName = toSlashClassName(className);
    jclass startClass = env->FindClass(slashClassName);
    if (startClass == NULL) {
        ...
    } else {
        jmethodID startMeth = env->GetStaticMethodID(startClass, "main",
            "([Ljava/lang/String;)V");
        // 调用ZygoteInit.main()方法【见小节3.1】
        env->CallStaticVoidMethod(startClass, startMeth, strArray);
    }
    //释放相应对象的内存空间
    free(slashClassName);
    mJavaVM->DetachCurrentThread();
    mJavaVM->DestroyJavaVM();
}
```

#### AndroidRuntime.cpp startVm()

AndroidRuntime.cpp  startVm()创建Java虚拟机方法的主要篇幅是关于虚拟机参数的设置，下面只列举部分在调试优化过程中常用参数。

```c++
int AndroidRuntime::startVm(JavaVM** pJavaVM, JNIEnv** pEnv, bool zygote)
{
    // JNI检测功能，用于native层调用jni函数时进行常规检测，比较弱字符串格式是否符合要求，资源是否正确释放。该功能一般用于早期系统调试或手机Eng版，对于User版往往不会开启，引用该功能比较消耗系统CPU资源，降低系统性能。
    bool checkJni = false;
    property_get("dalvik.vm.checkjni", propBuf, "");
    if (strcmp(propBuf, "true") == 0) {
        checkJni = true;
    } else if (strcmp(propBuf, "false") != 0) {
        property_get("ro.kernel.android.checkjni", propBuf, "");
        if (propBuf[0] == '1') {
            checkJni = true;
        }
    }
    if (checkJni) {
        addOption("-Xcheck:jni");
    }

    //虚拟机产生的trace文件，主要用于分析系统问题，路径默认为/data/anr/traces.txt
    parseRuntimeOption("dalvik.vm.stack-trace-file", stackTraceFileBuf, "-Xstacktracefile:");

    //对于不同的软硬件环境，这些参数往往需要调整、优化，从而使系统达到最佳性能
    parseRuntimeOption("dalvik.vm.heapstartsize", heapstartsizeOptsBuf, "-Xms", "4m");
    parseRuntimeOption("dalvik.vm.heapsize", heapsizeOptsBuf, "-Xmx", "16m");
    parseRuntimeOption("dalvik.vm.heapgrowthlimit", heapgrowthlimitOptsBuf, "-XX:HeapGrowthLimit=");
    parseRuntimeOption("dalvik.vm.heapminfree", heapminfreeOptsBuf, "-XX:HeapMinFree=");
    parseRuntimeOption("dalvik.vm.heapmaxfree", heapmaxfreeOptsBuf, "-XX:HeapMaxFree=");
    parseRuntimeOption("dalvik.vm.heaptargetutilization",
                       heaptargetutilizationOptsBuf, "-XX:HeapTargetUtilization=");
    ...

    //preloaded-classes文件内容是由WritePreloadedClassFile.java生成的，
    //在ZygoteInit类中会预加载工作将其中的classes提前加载到内存，以提高系统性能
    if (!hasFile("/system/etc/preloaded-classes")) {
        return -1;
    }

    //初始化虚拟机
    if (JNI_CreateJavaVM(pJavaVM, pEnv, &initArgs) < 0) {
        ALOGE("JNI_CreateJavaVM failed\n");
        return -1;
    }
}
```

#### AndroidRuntime.cpp startReg()

```c++
int AndroidRuntime::startReg(JNIEnv* env)
{
    //设置线程创建方法为javaCreateThreadEtc 
    androidSetCreateThreadFunc((android_create_thread_fn) javaCreateThreadEtc);

    env->PushLocalFrame(200);
    //进程JNI方法的注册
    if (register_jni_procs(gRegJNI, NELEM(gRegJNI), env) < 0) {
        env->PopLocalFrame(NULL);
        return -1;
    }
    env->PopLocalFrame(NULL);
    return 0;
}
```

AndroidRuntime.start()执行到最后通过反射调用到ZygoteInit.main()

#### ZygoteInit.java main()

```java
public static void main(String argv[]) {
    try {
        RuntimeInit.enableDdms(); //开启DDMS功能
        SamplingProfilerIntegration.start();
        boolean startSystemServer = false;
        String socketName = "zygote";
        String abiList = null;
        for (int i = 1; i < argv.length; i++) {
            if ("start-system-server".equals(argv[i])) {
                startSystemServer = true;
            } else if (argv[i].startsWith(ABI_LIST_ARG)) {
                abiList = argv[i].substring(ABI_LIST_ARG.length());
            } else if (argv[i].startsWith(SOCKET_NAME_ARG)) {
                socketName = argv[i].substring(SOCKET_NAME_ARG.length());
            } else {
                throw new RuntimeException("Unknown command line argument: " + argv[i]);
            }
        }
        ...

        registerZygoteSocket(socketName); //为Zygote注册socket
        preload(); // 预加载类和资源
        SamplingProfilerIntegration.writeZygoteSnapshot();
        gcAndFinalize(); //GC操作
        if (startSystemServer) {
            startSystemServer(abiList, socketName);//启动system_server
        }
        runSelectLoop(abiList); //进入循环模式
        closeServerSocket();
    } catch (MethodAndArgsCaller caller) {
        caller.run(); //启动system_server中会讲到。
    } catch (RuntimeException ex) {
        closeServerSocket();
        throw ex;
    }
}
```

#### ZygoteInit.java registerZygoteSocket()

```java
private static void registerZygoteSocket(String socketName) {
    if (sServerSocket == null) {
        int fileDesc;
        final String fullSocketName = ANDROID_SOCKET_PREFIX + socketName;
        try {
            String env = System.getenv(fullSocketName);
            fileDesc = Integer.parseInt(env);
        } catch (RuntimeException ex) {
            ...
        }

        try {
            FileDescriptor fd = new FileDescriptor();
            fd.setInt$(fileDesc); //设置文件描述符
            sServerSocket = new LocalServerSocket(fd); //创建Socket的本地服务端
        } catch (IOException ex) {
            ...
        }
    }
}
```

#### ZygoteInit.java preload()

```java
static void preload() {
    //预加载位于/system/etc/preloaded-classes文件中的类
    preloadClasses();

    //预加载资源，包含drawable和color资源
    preloadResources();

    //预加载OpenGL
    preloadOpenGL();

    //通过System.loadLibrary()方法，
    //预加载"android","compiler_rt","jnigraphics"这3个共享库
    preloadSharedLibraries();

    //预加载 文本连接符资源
    preloadTextResources();

    //仅用于zygote进程，用于内存共享的进程
    WebViewFactory.prepareWebViewInZygote();
}
```

执行 Zygote 进程的初始化,对于类加载，采用反射机制 Class.forName() 方法来加载。对于资源加载，主要是 com.android.internal.R.array.preloaded_drawables 和 com.android.internal.R.array.preloaded_color_state_lists，在应用程序中以com.android.internal.R.xxx 开头的资源，便是此时由 Zygote 加载到内存的。

zygote进程内加载了 preload() 方法中的所有资源，当需要fork新进程时，采用 **copy on write** 技术，如下：

![zygote_fork](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/202007182207-425473.jpeg)

#### ZygoteInit.java  startSystemServer()

```java
private static boolean startSystemServer(String abiList, String socketName) throws MethodAndArgsCaller, RuntimeException {
    long capabilities = posixCapabilitiesAsBits(
        OsConstants.CAP_BLOCK_SUSPEND,
        OsConstants.CAP_KILL,
        OsConstants.CAP_NET_ADMIN,
        OsConstants.CAP_NET_BIND_SERVICE,
        OsConstants.CAP_NET_BROADCAST,
        OsConstants.CAP_NET_RAW,
        OsConstants.CAP_SYS_MODULE,
        OsConstants.CAP_SYS_NICE,
        OsConstants.CAP_SYS_RESOURCE,
        OsConstants.CAP_SYS_TIME,
        OsConstants.CAP_SYS_TTY_CONFIG
    );
    //参数准备
    String args[] = {
        "--setuid=1000",
        "--setgid=1000",
        "--setgroups=1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1018,1021,1032,3001,3002,3003,3006,3007",
        "--capabilities=" + capabilities + "," + capabilities,
        "--nice-name=system_server",
        "--runtime-args",
        "com.android.server.SystemServer",
    };

    ZygoteConnection.Arguments parsedArgs = null;
    int pid;
    try {
        //用于解析参数，生成目标格式
        parsedArgs = new ZygoteConnection.Arguments(args);
        ZygoteConnection.applyDebuggerSystemProperty(parsedArgs);
        ZygoteConnection.applyInvokeWithSystemProperty(parsedArgs);

        // fork子进程，用于运行system_server
        pid = Zygote.forkSystemServer(
                parsedArgs.uid, parsedArgs.gid,
                parsedArgs.gids,
                parsedArgs.debugFlags,
                null,
                parsedArgs.permittedCapabilities,
                parsedArgs.effectiveCapabilities);
    } catch (IllegalArgumentException ex) {
        throw new RuntimeException(ex);
    }

    //进入子进程system_server
    if (pid == 0) {
        if (hasSecondZygote(abiList)) {
            waitForSecondaryZygote(socketName);
        }
        // 完成system_server进程剩余的工作
        handleSystemServerProcess(parsedArgs);
    }
    return true;
}
```

准备参数并 fork 新进程，从上面可以看出 system server 进程参数信息为 uid=1000,gid=1000, 进程名为 sytem_server，从 zygote进程 fork 新进程后，需要关闭 zygote 原有的 socket。另外，对于有两个 zygote 进程情况，需等待第2个 zygote 创建完成。单线程 fork

#### ZygoteInit.java  runSelectLoop()

```java
private static void runSelectLoop(String abiList) throws MethodAndArgsCaller {
    ArrayList<FileDescriptor> fds = new ArrayList<FileDescriptor>();
    ArrayList<ZygoteConnection> peers = new ArrayList<ZygoteConnection>();
    //sServerSocket是socket通信中的服务端，即zygote进程。保存到fds[0]
    fds.add(sServerSocket.getFileDescriptor());
    peers.add(null);

    while (true) {
        StructPollfd[] pollFds = new StructPollfd[fds.size()];
        for (int i = 0; i < pollFds.length; ++i) {
            pollFds[i] = new StructPollfd();
            pollFds[i].fd = fds.get(i);
            pollFds[i].events = (short) POLLIN;
        }
        try {
             //处理轮询状态，当pollFds有事件到来则往下执行，否则阻塞在这里
            Os.poll(pollFds, -1);
        } catch (ErrnoException ex) {
            ...
        }
        
        for (int i = pollFds.length - 1; i >= 0; --i) {
            //采用I/O多路复用机制，当接收到客户端发出连接请求 或者数据处理请求到来，则往下执行；
            // 否则进入continue，跳出本次循环。
            if ((pollFds[i].revents & POLLIN) == 0) {
                continue;
            }
            if (i == 0) {
                //即fds[0]，代表的是sServerSocket，则意味着有客户端连接请求；
                // 则创建ZygoteConnection对象,并添加到fds。
                ZygoteConnection newPeer = acceptCommandPeer(abiList);
                peers.add(newPeer);
                fds.add(newPeer.getFileDesciptor()); //添加到fds.
            } else {
                //i>0，则代表通过socket接收来自对端的数据，并执行相应操作【见小节3.6】
                boolean done = peers.get(i).runOnce();
                if (done) {
                    peers.remove(i);
                    fds.remove(i); //处理完则从fds中移除该文件描述符
                }
            }
        }
    }
}
```

Zygote采用高效的I/O多路复用机制，保证在没有客户端连接请求或数据处理时休眠，否则响应客户端的请求。

#### ZygoteConnection.java  runOnce() 处理请求

```java
boolean runOnce() throws ZygoteInit.MethodAndArgsCaller {

    String args[];
    Arguments parsedArgs = null;
    FileDescriptor[] descriptors;

    try {
        //读取socket客户端发送过来的参数列表
        args = readArgumentList();
        descriptors = mSocket.getAncillaryFileDescriptors();
    } catch (IOException ex) {
        ...
        return true;
    }
    ...

    try {
        //将binder客户端传递过来的参数，解析成Arguments对象格式
        parsedArgs = new Arguments(args);
        ...
        //启动子进程
        pid = Zygote.forkAndSpecialize(parsedArgs.uid, parsedArgs.gid, parsedArgs.gids,
                parsedArgs.debugFlags, rlimits, parsedArgs.mountExternal, parsedArgs.seInfo,
                parsedArgs.niceName, fdsToClose, parsedArgs.instructionSet,
                parsedArgs.appDataDir);
    } catch (Exception e) {
        ...
    }

    try {
        if (pid == 0) {
            //子进程执行
            IoUtils.closeQuietly(serverPipeFd);
            serverPipeFd = null;
            //进入子进程流程（ActivityThread.main()）
            handleChildProc(parsedArgs, descriptors, childPipeFd, newStderr);
            return true;
        } else {
            //父进程执行
            IoUtils.closeQuietly(childPipeFd);
            childPipeFd = null;
            return handleParentProc(pid, descriptors, serverPipeFd, parsedArgs);
        }
    } finally {
        IoUtils.closeQuietly(childPipeFd);
        IoUtils.closeQuietly(serverPipeFd);
    }
}
```