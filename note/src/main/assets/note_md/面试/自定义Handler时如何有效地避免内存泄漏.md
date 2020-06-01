## 自定义 Handler 时如何有效地避免内存泄漏？

在Android系统中，Handler是一个消息发送和处理机制的核心组件之一，与之配套的其他主要组件还有Looper和Message，MessageQueue。Message和Runnable类是消息的载体。MessageQueue是消息等待的队列。Looper则负责从队列中取消息。

#### Handler有两个主要作用：

- 安排调度(scheule)消息和可执行的runnable，可以立即执行，也可以安排在某个将来的时间点执行。

- 让某一个行为（action）在其他线程中执行。

Handler是由系统所提供的一种异步消息处理的常用方式,一般情况下不会发生内存泄露。Handler为什么可能造成内存泄漏？这里的内存泄漏，常常指的是泄漏了Activity等组件。

```java
public class HandlerActivity extends Activity {
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
```

在上述代码中Handler的实例采用了内部类的写法，它是HandlerActivity这个实例的内部类，在Java中，关于内部类有一个特点：在java中，非静态的内部类和匿名内部类都会隐式的持有一个外部类的引用。所以，该handler实例持有了HandlerActivity的一个引用。**生命周期较短的组件引用了生命周期较长的组件。**HandlerActivity可能会被泄漏，也就是该组件没有用了，比如调用了finish()后，垃圾回收器却迟迟没有回收该Activity。原因出在该实例的handler内部类引用了它，而handler实例可能被MessageQueue引用着。

从上面的说法中，可以思考得到相应的解决方法：

1. 保证Activity被**finish()**时该线程的消息队列没有这个Activity的handler内部类的引用，该类需要回收的时候，手动地**把消息队列中的消息清空**：`mHandler.removeCallbacksAndMessages(null);`
2. handler不持有Activity等外部组件实例，让该Handler成为**静态内部类**。（静态内部类是不持有外部类的实例的，因而也就调用不了外部的实例方法了）
3. 在2方法的基础上，为了能调用外部的实例方法，传递一个外部的**弱引用**进来）
4. 将Handler放到抽取出来放入一个单独的顶层类文件中。

这里需要了解一下关于Java里面引用的知识：

|          引用类型          | 引用说明                                                     |
| :------------------------: | :----------------------------------------------------------- |
|  强引用(StrongReference)   | 默认引用。如果一个对象具有强引用，垃圾回收器绝不会回收它。在内存空间不足时，Java虚拟机宁愿抛出OutOfMemory的错误，使程序异常终止，也不会强引用的对象来解决内存不足问题。 |
|  软引用（SoftReference）   | 如果内存空间足够，垃圾回收器就不会回收它，如果内存空间不足了，就会回收这些对象的内存。 |
|  弱引用（WeakReference）   | 在垃圾回收器一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存。 |
| 虚引用（PhantomReference） | 如果一个对象仅持有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收。 |

通常使用弱引用：

```java
public class HandlerActivity extends Activity {
    private static class MyHandler extends Handler {
        private final WeakReference<HandlerActivity> mActivity;

        public MyHandler(HandlerActivity activity) {
            mActivity = new WeakReference<HandlerActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            HandlerActivity activity = mActivity.get();
            if (activity != null) {
                //do Something
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
```

抽取做单独封装：

```java
/**
 * 实现回调弱引用的Handler
 * 防止由于内部持有导致的内存泄露
 * 传入的Callback不能使用匿名实现的变量，必须与使用这个Handler的对象的生命周期一 
 * 致否则会被立即释放掉了
 */
public class WeakRefHandler extends Handler {
    private WeakReference<Callback> mWeakReference;
    
    public WeakRefHandler(Callback callback) {
        mWeakReference = new WeakReference<Handler.Callback>(callback);
    }
    
    public WeakRefHandler(Callback callback, Looper looper) {
        super(looper);
        mWeakReference = new WeakReference<Handler.Callback>(callback);
    }
    
    @Override
    public void handleMessage(Message msg) {
        if (mWeakReference != null && mWeakReference.get() != null) {
            Callback callback = mWeakReference.get();
            callback.handleMessage(msg);
        }
    }
}
```

WeakRefHandler的使用时如下：

```java
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what){
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
```

