## 为什么主线程用 Looper 死循环不会引发 ANR 异常?

> 线程默认没有 Looper 的，如果需要使用Handler就必须为线程创建 Looper。我们经常提到的主线程，也叫UI线程，它就是 ActivityThread，ActivityThread 被创建时就会初始化 Looper，这也是在主线程中默认可以使用 Handler 的原因。

在 **ActivityThread.java **的 main() 中

```java
public static void main(String[] args) {
    ...
    Looper.prepareMainLooper();//创建Looper和MessageQueue对象，用于处理主线程的消息

    ActivityThread thread = new ActivityThread();
    thread.attach(false);//建立Binder通道 (创建新线程)

    if (sMainThreadHandler == null) {
        sMainThreadHandler = thread.getHandler();
    }

    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
    Looper.loop();

    //如果能执行下面方法，说明应用崩溃或者是退出了...
    throw new RuntimeException("Main thread loop unexpectedly exited");
}
```
> 对于线程即是一段可执行的代码，当可执行代码执行完成后，线程生命周期便该终止了，线程退出。而对于主线程，我们是绝不希望会被运行一段时间，自己就退出，那么如何保证能一直存活呢？简单做法就是可执行代码是能一直执行下去的，死循环便能保证不会被退出，例如，**Binder** 线程也是采用死循环的方法，通过循环方式不同与 **Binder** 驱动进行读写操作，当然并非简单地死循环，无消息时会休眠。但这里可能又引发了另一个问题，既然是死循环又如何去处理其他事务呢？通过创建新线程的方式。真正会卡死主线程的操作是在回调方法 **onCreate** / **onStart** / **onResume** 等操作时间过长，会导致掉帧，甚至发生 **ANR**，**Looper.loop()** 本身不会导致应用卡死。

简单说就是在主线程的 **MessageQueue** 没有消息时，便阻塞在 **loop** 的 **queue.next()** 中的 **nativePollOnce()** 方法里，此时主线程会释放 **CPU** 资源进入休眠状态，直到下个消息到达或者有事务发生，通过往 **pipe** 管道写端写入数据来唤醒主线程工作。这里采用的 **epoll** 机制，是一种 IO 多路复用机制。

## 为什么不能在子线程中更新 UI,根本原因是什么?

Android 的 UI 控件不是线程安全的，如果在多线程中并发访问可能会导致UI控件处于不可预期的状态。

在 **ViewRootImpl.java** 中有

```java
void checkThread() {
    if (mThread != Thread.currentThread()) {
        throw new CalledFromWrongThreadException(
                "Only the original thread that created a view hierarchy can touch its views.");
    }
}
```

**mThread **是 **UI** 线程，这里会检查当前线程是不是 **UI** 线程。那么为什么 **onCreate** 里面没有进行这个检查呢？这个问题原因出现在Activity的生命周期中，在 **onCreate** 方法中，**UI** 处于创建过程，对用户来说界面还不可视，直到 **onStart** 方法后界面可视了，再到 **onResume** 方法后界面可以交互。从某种程度来讲，在 **onCreate** 方法中不能算是更新 **UI**，只能说是配置 **UI**，或者是设置 **UI** 的属性。这个时候不会调用到**ViewRootImpl.checkThread()**，因为 **ViewRootImpl** 没被创建。而在 **onResume** 方法后，**ViewRootImpl **才被创建。这个时候去交互界面才算是更新UI。

**setContentView** 只是建立了 **View** 树，并没有进行渲染工作（其实真正的渲染工作是在 **onResume** 之后）。也正是建立了 **View** 树，因此我们可以通过 **findViewById()** 来获取到 **View** 对象，但是由于并没有进行渲染视图的工作，也就是没有执行 **ViewRootImpl.performTransversal**。同样 **View** 中也不会执行**onMeasure()**，如果在 **onResume()** 方法里直接获取 **View.getHeight() **、**View.getWidth()** 得到的结果总是0。

## 为什么 Handler 构造方法里面的 Looper 不是直接 new？

如果在Handler构造方法里面new Looper，怕是无法保证保证Looper唯一，只有用Looper.prepare()才能保证唯一性，具体去看prepare方法。

## MessageQueue 为什么要放在 Looper 私有构造方法初始化？

因为一个线程只绑定一个 Looper，所以在 Looper 构造方法里面初始化就可以保证 mQueue 也是唯一的 Thread 对应一个 Looper 对应一个 mQueue。

### Handler.post 的逻辑在哪个线程执行的，是由 Looper 所在线程还是 Handler 所在线程决定的？

由 **Looper** 所在线程决定的。逻辑是在**Looper.loop()** 方法中，从 **MesssageQueue** 中拿出 **msg**，并且执行其逻辑，这是在 **Looper** 中执行的，因此有 **Looper** 所在线程决定。

## MessageQueue.next() 会因为发现了延迟消息，而进行阻塞。那么为什么后面加入的非延迟消息没有被阻塞呢？

在