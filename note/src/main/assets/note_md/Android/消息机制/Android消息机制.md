# Android消息机制

### 源码分析

##### App 启动后，创建全局唯一 **Looper** 对象和全局唯一 **MessageQueue** 消息对象，在 **ActivityThread.java **中

```java
public static void main(String[] args) {
    ...
    Looper.prepareMainLooper();
    ...
    Looper.loop();
}
```

##### 初始化全局唯一的 Looper：

```java
public static void prepareMainLooper() {
    prepare(false);
    synchronized (Looper.class) {
        //不能被多次初始化，全局唯一
        if (sMainLooper != null) {
            throw new IllegalStateException("The main Looper has already been prepared.");
        }
        sMainLooper = myLooper();
    }
}
```

```java
public static @Nullable Looper myLooper() {
    return sThreadLocal.get();
}
```

```java
private static void prepare(boolean quitAllowed) {
    if (sThreadLocal.get() != null) {
        throw new RuntimeException("Only one Looper may be created per thread");
    }
    //创建全局唯一主线程 Looper 对象
    sThreadLocal.set(new Looper(quitAllowed));
}
```

##### ThreadLocal 线程内部的数据存储类

```java
public T get() {
    Thread t = Thread.currentThread();
    //结构类似HashMap<K,V>,键为线程，实现了线程隔离
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue();
}
```

```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}
```

##### 初始化全局唯一的 MessageQueue

```java
private Looper(boolean quitAllowed) {
    //创建全局唯一主线程消息队列
    mQueue = new MessageQueue(quitAllowed);
    mThread = Thread.currentThread();
}
```

##### **Handler **在 **Activity** 中初始化

```java
public Handler() {
    this(null, false);
}
```

```java
public Handler(@Nullable Callback callback, boolean async) {
        mLooper = Looper.myLooper();
        if (mLooper == null) {
            throw new RuntimeException(
                "Can't create handler inside thread " + Thread.currentThread()
                        + " that has not called Looper.prepare()");
        }
        mQueue = mLooper.mQueue;
        mCallback = callback;
        mAsynchronous = async;
}
```

##### 从全局线程中取出 **Looper**

```  java
public static @Nullable Looper myLooper() {
    return sThreadLocal.get();
}
```

##### 重写 **handleMessage**

```java
public void handleMessage(@NonNull Message msg) {
    //消息在此消费
}
```

##### 消息发送

```java
public final boolean sendMessage(@NonNull Message msg) {
    return sendMessageDelayed(msg, 0);
}
```

```java
public final boolean sendMessageDelayed(@NonNull Message msg, long delayMillis) {
    if (delayMillis < 0) {
        delayMillis = 0;
    }
    return sendMessageAtTime(msg, SystemClock.uptimeMillis() + delayMillis);
}
```

```java
public boolean sendMessageAtTime(@NonNull Message msg, long uptimeMillis) {
    MessageQueue queue = mQueue;
    if (queue == null) {
        RuntimeException e = new RuntimeException(
                this + " sendMessageAtTime() called with no mQueue");
        Log.w("Looper", e.getMessage(), e);
        return false;
    }
    return enqueueMessage(queue, msg, uptimeMllis);
}
```

##### 在 **MessageQueue** 中消息入队

```java
boolean enqueueMessage(Message msg, long when) {
    if (msg.target == null) {
        throw new IllegalArgumentException("Message must have a target.");
    }
    if (msg.isInUse()) {
        throw new IllegalStateException(msg + " This message is already in use.");
    }

    synchronized (this) {
        if (mQuitting) {
            IllegalStateException e = new IllegalStateException(
                    msg.target + " sending message to a Handler on a dead thread");
            Log.w(TAG, e.getMessage(), e);
            msg.recycle();
            return false;
        }

        msg.markInUse();
        msg.when = when;
        Message p = mMessages;
        boolean needWake;
        if (p == null || when == 0 || when < p.when) {
            // New head, wake up the event queue if blocked.
            msg.next = p;
            mMessages = msg;
            needWake = mBlocked;
        } else {
            // Inserted within the middle of the queue.  Usually we don't have to wake
            // up the event queue unless there is a barrier at the head of the queue
            // and the message is the earliest asynchronous message in the queue.
            needWake = mBlocked && p.target == null && msg.isAsynchronous();
            Message prev;
            for (;;) {
                prev = p;
                p = p.next;
                if (p == null || when < p.when) {
                    break;
                }
                if (needWake && p.isAsynchronous()) {
                    needWake = false;
                }
            }
            msg.next = p; // invariant: p == prev.next
            prev.next = msg;
        }

        // We can assume mPtr != 0 because mQuitting is false.
        if (needWake) {
            //此处进行消息唤醒
            nativeWake(mPtr);
        }
    }
    return true;
}
```

##### **Looper.loop()** 开启消息循环，取出消息

```java
public static void loop() {
    final Looper me = myLooper();//获取主线程 Looper
    if (me == null) {
        throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
    }
    final MessageQueue queue = me.mQueue;
    ...
    for (;;) {
        Message msg = queue.next(); // might block
        ...
        try {
            msg.target.dispatchMessage(msg);
            ...
        } 
        ...
    }
}
```

##### MessageQueue中取出消息

```java
Message next() {
    ...
    int nextPollTimeoutMillis = 0;
    for (;;) {
        if (nextPollTimeoutMillis != 0) {
            Binder.flushPendingCommands();
        }

        //native层依靠 epoll 机制进行休眠，由nextPollTimeoutMillis决定是否需要阻塞,					//为0的时候表示不阻塞，为-1的时候表示一直阻塞直到被唤醒，其他时间表示延时。
        nativePollOnce(ptr, nextPollTimeoutMillis);
			
        synchronized (this) {
            // Try to retrieve the next message.  Return if found.
            final long now = SystemClock.uptimeMillis();
            Message prevMsg = null;
            Message msg = mMessages;
            if (msg != null && msg.target == null) {
                // Stalled by a barrier.  Find the next asynchronous message in the queue
                do {
                    prevMsg = msg;
                    msg = msg.next;
                } while (msg != null && !msg.isAsynchronous());
            }
            if (msg != null) {
                if (now < msg.when) {
               // Next message is not ready.  Set a timeout to wake up when it is ready.
               nextPollTimeoutMillis = (int) Math.min(msg.when - now, Integer.MAX_VALUE);
                } else {
                    // Got a message.
                    Blocked = false;
                    if (prevMsg != null) {
                        prevMsg.next = msg.next;
                    } else {
                        mMessages = msg.next;
                    }
                    msg.next = null;
                    if (DEBUG) Log.v(TAG, "Returning message: " + msg);
                        msg.markInUse();
                        return msg;
                    }
                } else {
                    // No more messages.
                    //没有消息则一直阻塞直到被唤醒
                    nextPollTimeoutMillis = -1;
                }
               ...
            }
			...
        }
    }
```

##### Handler 中消费消息（调用接口或重写方法）

```java
public void dispatchMessage(@NonNull Message msg) {
    if (msg.callback != null) {
        handleCallback(msg);
    } else {
        if (mCallback != null) {
            if (mCallback.handleMessage(msg)) {
                return;
            }
        }
        handleMessage(msg);
    }
}
```

![Handler机制](https://img-blog.csdnimg.cn/20200331223115403.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2xlZXloQ29kaW5n,size_16,color_FFFFFF,t_70)