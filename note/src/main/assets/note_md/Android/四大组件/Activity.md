#  基于使用场景解读 Activity

Activity 作为四大组件之首，是使用最为频繁的一种组件，中文直接翻译为 “**活动**” ，如果翻译成 “**界面**” 就会更好理解。正常情况下，除了 **Window**、**Dialog **和 **Toast**，我们能见到的界面的确只有Activity。Activity这个组件提供了两大接口：**生命周期**和**启动模式**。

## Activity 生命周期

下面这张图来自官网：

![activity_lifecycle.png](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/20200608103641-826962.png)

这张图大家应该很熟悉了，这里我按使用场景来把它们分成三组：

- **完整生存期**：onCreate和onDestroy
- **可见生存期**：onStart和onStop
- **前台生存期**：onResume和onPause

第一组使用频率最高，在一个App里面我们会经常需要打开（startActivity）和关闭（finish）一个页面。

### **onCreate**

表示 Activity 正在被创建，这是生命周期的第一个方法。在这个方法中，我们可以做一些初始化工作，比如调用**setContentView** 去加载界面布局资源、初始化 Activity 所需数据等。

> Tips：当我们进入到这一步时就表示一个 Activity 实例对象（从Java的角度看）已经产生了，当我们New了一个Java对象之后，首先要做的肯定是对其进行初始化了，那么 **onCreate** 就是Android 提供给开发者用来对 Activity 实例对象中的成员做初始化的。Android为了方便对Activity组件的管理以及开发者使用，对Activity做了封装，开发者不能直接new一个Activity对象（你也可以直接new，但是new出来的对象不会被Android管理，也就失去了界面的展示和交互的功能，跟普通Java对象无异）

**onCreate** 里面有个(Bundle **savedInstanceState**)参数，Activity在非正常情况下(**系统内存不够用**、**系统语言改变**，**屏幕方向改变**等)被销毁前自动保存的一些数据，这些数据会在这个 Activity 被重新创建时用到，因此 Android 将这个参数放在了 onCreate 里面。用户主动意愿想要销毁 Activity 就是正常情况，这种场景很少，就两种：调用 finish 和带特殊启动模式的 startActivity 方法。savedInstanceState 会保存两种数据：**系统自动保存**的和你自己保存的。系统只会保存它认为有必要保存的（比方说 EditText 里面的内容，CheckBox、RadioButton 的Check 状态，Fragment 实例等，控件中有 **onSaveInstanceState** 方法的）。

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        if (savedInstanceState != null) {
           // 此时说明 activity 是在异常情况下恢复
        } else {
           // 首次创建或者重新创建
        }
}
```

### **onDestroy**

这个方法被调用后一般代表activity即将要被销毁掉，不管是正常情况还是非正常情况关闭 Activity。一般会在这里面做一些资源的释放操作、注销广播等，以防止出现资源泄露或者依赖 Activity 所引发的一些异常情况的发生。这里我举两个例子来说下上面说的两种情况：

- 异步任务引发的资源泄露，比如 **handler** 或者 **thread**。这种情况发生的原因主要是异步任务的生命周期与activity生命周期不同步造成的，以handler中的message为例：

```java
Handler handler =  new Handler();
handler.postDelayed(new Runnable() {
    @Override
    public void run() {
        tvContent.setText("newContent");
    }
}, 2000);
handler.obtainMessage(1).sendToTarget();
```

不管是使用哪种形式来发送message，message都会直接或者间接引用到当前所在的activity实例对象，如果在activity finish后，还有其相关的message在主线程的消息队列中，就会导致该activity实例对象无法被GC回收，引起内存泄露。所以一般我们需要在onDestroy阶段将handler所持有的message对象从主线程的消息队列中清除。示例如下：

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (handler != null) {
        handler.removeCallbacksAndMessages(null);
    }
}
```

- 异步任务引发的App运行异常，这里以一个显示Dialog的场景为例：

```java
Handler handler =  new Handler();
handler.postDelayed(new Runnable() {
    @Override
    public void run() {
        new AlertDialog.Builder(MainActivity.this).setMessage("Show Dialog").show();
    }
}, 5000);
```

由于我们设置的是5秒后显示一个dialog，当activity在5秒内被finish后可能会导致显示dialog时App发生崩溃。

```dart
FATAL EXCEPTION: main
Process: xiaofei.com.fragmenttest, PID: 4645
android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@aebf1e6 is not valid; is your activity running?
    at android.view.ViewRootImpl.setView(ViewRootImpl.java:567)
    at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:310)
    at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:85)
    at android.app.Dialog.show(Dialog.java:319)
    at android.support.v7.app.AlertDialog$Builder.show(AlertDialog.java:955)
    at xiaofei.com.fragmenttest.Main2Activity$1.run(MainActivity.java:49)
    at android.os.Handler.handleCallback(Handler.java:739)
    at android.os.Handler.dispatchMessage(Handler.java:95)
    at android.os.Looper.loop(Looper.java:148)
    at android.app.ActivityThread.main(ActivityThread.java:5417)
    at java.lang.reflect.Method.invoke(Native Method)
    at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:726)
    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:616)
```

由于我们在activity的onDestroy中会销毁activity对应的窗体资源，所以在显示Dialog的时候由于dialog找不到父窗体就发生异常了。关于onDestroy中系统到底做了哪些资源清理的工作看下面的代码就清楚了：

```java
final void performDestroy() {
    mDestroyed = true;
    mWindow.destroy();
    mFragments.dispatchDestroy();
    onDestroy();
    mFragments.doLoaderDestroy();
    if (mVoiceInteractor != null) {
        mVoiceInteractor.detachActivity();
    }
}
```

所以在这种场景下正确的做法应该是这样的：

```java
Handler handler =  new Handler();
handler.postDelayed(new Runnable() {
    @Override
    public void run() {
        if (!MainActivity.this.isDestroyed()) {
            new AlertDialog.Builder(MainActivity.this).setMessage("Show Dialog").show();
        }
    }
}, 5000);
```

isDestroyed方法要求最低api level是17，一般我们目前app支持的最低api level是14，所以你可以在activity中添加一个flag来标记当前activity的状态是否被destroyed，其实activity源码里面也是这么干的，依葫芦画瓢就行。

```java
/**
 * Returns true if the final {@link #onDestroy()} call has been made
 * on the Activity, so this instance is now dead.
 */
public boolean isDestroyed() {
    return mDestroyed;
}
```

### onStart和onStop

onStart 是从不可见进入到可见状态，onStop 是从可见进入到不可见状态。时间都极为短暂，不常用。

### onResume和onPause

onResume： 这个方法在活动准备好和用户进行交互的时候调用（取得焦点）。 **此时的活动一定位于返回栈的栈顶，并且处于运行状态。**

onPause：失去焦点，处于暂停状态。**我们通常会在这个方法中将一些及其消耗 CPU 的资源释放掉（比如显示地图或者大规模图形），以及保存一些关键数据（比如用户输入的数据等等），但这个方法的执行速度一定要快，不然会影响到新的栈顶活动的使用。**

```java
@Override  
protected void onPause(){  
        mMapView.onPause();
        super.onPause();  
}  
@Override  
protected void onResume(){  
        mMapView.onResume();
       super.onResume();  
}  
```

- 结束占用CPU的动画或者其他正在运行任务，这种在使用地图SDK的时候比较常见：
- 视频播放，当视图组件获得焦点时，即onResume中播放视频，当视图组件失去焦点时，即onPause中暂停播放视频。
- 保存重要数据，为了防止App被意外强杀，**一般会在 onPause 中将一些重要数据保存到本地**。

### onRestart

表示 Activity **正在重新启动**。一般情况下，当当前Activity从不可见重新变为可见状态时，onRestart就会被调用。这种情形一般是用户行为所导致的，比如用户按Home键切换到桌面或者用户打开了一个新的Activity，这时当前的Activity就会暂停，也就是onPause和onStop被执行了，接着用户又回到了这个Activity，就会出现这种情况。

### 常见流程

1. 针对一个特定的 Activity，第一次启动，回调：**onCreate  ⇒  onStart  ⇒  onResume**

2. 当用户打开新的 Activity 或者切换到桌面的时候，回调如下：**onPause  ⇒ onStop**，这里有一种特殊情况，如果新 Activity 采用了透明主题，那么当前 Activity 不会回调 onStop。

3. 当用户再次回到原 Activity 时，回调：**onRestart  ⇒ onStart  ⇒  onResume**。

4. 当用户按 back 键回退时，回调如下：**onPause  ⇒ onStop  ⇒  onDestroy**。

5. 当 Activity 被系统回收后再次打开，生命周期方法回调过程和 1 一样，注意只是生命周期方法一样，不代表所有过程都一样，这个问题在下一节会详细说明。

6. 从整个生命周期来说，onCreate 和 onDestroy 是配对的，分别标识着 Activity 的创建和销毁，并且只可能有一次调用。从 Activity 是否可见来说，onStart 和 onStop 是配对的，随着用户的操作或者设备屏幕的点亮和熄灭，这两个方法可能被调用多次；从 Activity 是否在前台来说，onResume 和 onPause 是配对的，随着用户操作或者设备屏幕的点亮和熄灭，这两个方法可能被调用多次。

### 常见问题

1. Activity 启动后按 home 键返回桌：**onPause ⇒ onStop**，重新返回：**onRestart ⇒ onStart ⇒ onResume**

2. 设当前 Activity 为 A，如果这时用户打开一个新 Activity B，那么 B 的 onResume 和 A 的 onPause 哪个先执行呢？Activity启动之前，桟顶的Activity需要先onPause后，新Activity才能启动。流程为：**A onPause  ⇒  B onCreate  ⇒  onStart  ⇒  onResume ⇒ A onStop** 

3. 资源相关的系统配置发生改变导致Activity被杀死并重新创建：**onPause  ⇒  onSaveInstanceState  ⇒  onStop  ⇒  onDestroy  ⇒  onCreate  ⇒  onStart  ⇒  onRestoreInstanceState  ⇒  onResume**，onSaveInstanceState 肯定出现在 onDestroy  之前，Android 9 之前为上述流程，Android 9 及之后的版本为 onStop 之后。

4. 源内存不足导致低优先级的Activity被杀死：数据存储和恢复过程和情况 3 一样。Activity按照优先级从高到低，可以分为如下三种：

   1.  **前台Activity**——正在和用户交互的Activity，优先级最高。
   2. **可见但非前台Activity**——比如Activity中弹出了一个对话框，导致Activity可见但是位于后台无法和用户直接交互。
   3. **后台Activity**——已经被暂停的Activity，比如执行了onStop，优先级最低。

   当系统内存不足时，系统就会按照上述优先级去杀死目标 Activity 所在的进程，并在后续通过 **onSaveInstanceState** 和 **onRestoreInstanceState** 来存储和恢复数数据。如果一个进程中没有四大组件在执行，那么这个进程将很快被系统杀死，因此，一些后台工作不适合脱离四大组件而独自运行在后台中，这样进程很容易被杀死。比较好的方法是将后台工作放入 **Service** 中从而保证进程有一定的优先级，这样就不会轻易地被系统杀死。

   系统配置中有很多内容，如果当某项内容发生改变后，我们不想系统重新创建 Activity，可以给 Activity 指定configChanges 属性，android:configChanges="locale | orientation | keyboardHidden"。

## Activity四种启动模式

个人认为Activity四种启动模式的设定出于两种目的：

- 复用机制，节省系统资源，这种情况主要发生在除Standard模式外的三种模式上，通过他们的名字前缀Single也可以看出，跟Java中的单例模式有类似的思想，避免太多的实例对象创建开销。
- 根据用户的交互行为定义，因为Activity最终的目的还是完成用户在各种交互场景下的需求。

下面结合具体使用场景来细说每种启动模式：

### Standard

标准模式：这种启动模式最常见，也是 Activity 的默认启动模式。每当我们需要开启一个新的Activity页面时系统都会新建一个 Activity 实例对象，然后开启上面说的 Activity 的生命周期流程之旅，onCreate ⇒ onStart ⇒ onResume。在这种模式下，谁启动了这个 Activity，那么这个 Activity 就运行在启动它的那个 Activity 所在的栈中。

### SingleTop

栈顶复用模式：设置该模式后，当启动的 Activity 与当前 Task  栈顶的 Activity 一样时，系统不会重新创建一个  Activity 实例，Activity 的 onCreate、onStart 不会被系统调用，而是进入一个特殊的方法 onNewIntent ，具体流程为：onNewIntent ⇒ onResume。如果新 Activity 的实例已存在但不是位于栈顶，那么新 Activity 仍然会重新重建。

### SingleTask

栈内复用模式：设置该模式可以保证当前 Task 栈中 Activity 只会有一个实例存在，当通过 startActivity 启动Activity A 时，如果当前 Task 栈中已经存在一个Activity A的实例，那么不再重新创建一个新的 Activity  实例，而是直接复用该实例，进入该 Activity 的 onNewIntent 方法，同时将位于 Activity A 实例之上的所有 Activity 弹出 Task 栈并销毁。

### SingleInstance

设置该模式可以保证该Activity所在的Task中有且仅有一个activity实例，当通过startActivity启动Activity A时，如果该Activity的实例已经存在，那么不再重新创建一个新的Activity实例，而是直接复用该实例，进入该Activity的onNewIntent方法。这种场景出现的比较少，该Activity在整个系统只有一个实例，一般用于系统应用，并且可以被其他应用共享使用（有点类似于操作系统概念中的临界资源），比方说来电呼叫页面，在整个系统中就只能有一个，因为同一时刻只能存在一个电话呼叫。

可以发现后面三种模式的原理其实跟一些App中页面的交互流程比较类似，可能Android也是考虑到为了更方便实现这种交互方式而定义了这三种特殊的启动模式。其实不管是哪种启动方式最终都会影响到Activity的生命周期流程，因此我们在启动一个Activity页面的时候需要留意其对该activity生命周期回调的影响，并做相应的处理逻辑。此外这四种启动方式一般都会在AndroidManifest中写死，同时也可以根据需要在代码中动态配置，代码中可使用的启动标志一般有以下三个：



```css
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
```

一般将Intent.FLAG_ACTIVITY_NEW_TASK和Intent.FLAG_ACTIVITY_CLEAR_TOP搭配使用实现类似SingleTask的效果，将Intent.FLAG_ACTIVITY_NEW_TASK和Intent.FLAG_ACTIVITY_SINGLE_TOP搭配使用实现类似SingleTop的效果。

------

到此，关于Activity的两大主题：生命周期和启动模式就基本讲完了。当然Activity作为与用户交互的入口，所包含的内容还远不止这些，比如与Activity关联的Window，View，还有Dialog等等。如何正确理解他们与Activity的关系，正确的使用他们并与Activity配合来完成用户的交互，后面再单独分析。