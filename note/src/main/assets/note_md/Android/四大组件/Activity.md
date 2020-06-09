# 基于使用场景解读Activity

Activity这个组件提供了两大接口：**生命周期**和**启动模式**，下面这张图来自官网：

![activity_lifecycle.png](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/20200608103641-826962.png)

这张图大家应该很熟悉了，这里我按使用场景来把它们分成三组：

- **完整生存期**：onCreate和onDestroy
- **可见生存期**：onStart和onStop
- **前台生存期**：onResume和onPause

第一组使用频率最高，在一个App里面我们会经常需要打开（startActivity）和关闭（finish）一个页面。**onCreate** 是Activity生命周期里面的第一步，每个 Activity 中基本都会重写了这个方法， **它会在活动第一次被创建的时候调用**（只调用一次）。 你应该在这个方法中完成活动的**初始化操作**， 比如说**加载布局、添加View、给View填充数据、绑定事件**等。

> Tips：当我们进入到这一步时就表示一个 Activity 实例对象（从Java的角度看）已经产生了，当我们New了一个Java对象之后，首先要做的肯定是对其进行初始化了，那么 **onCreate** 就是Android 提供给开发者用来对 Activity 实例对象中的成员做初始化的。Android为了方便对Activity组件的管理以及开发者使用，对Activity做了封装，开发者不能直接new一个Activity对象（你也可以直接new，但是new出来的对象不会被Android管理，也就失去了界面的展示和交互的功能，跟普通Java对象无异）

还有就是在onCreate里面有个savedInstanceState参数，这个主要用于你的Activity在非正常情况下被销毁前帮你自动保存的一些数据，这些数据会在这个Activity被重新创建时用到，因此Android将这个参数放在了onCreate里面。注意，我这里说的是非正常情况销毁Activity，这种场景比较多，比如系统内存不够用，系统语言改变，屏幕方向改变等，如果你不清楚哪些是非正常情况，没关系，只要清楚正常情况就行了，其他的自然就都可以认为是非正常场景下的Activity销毁行为。那么正常情况是什么？用户主动意愿想要销毁Activity就是正常情况，这种场景很少，就两种：调用finish和带特殊启动模式的startActivity方法。那么对于非正常情况下的onCreate我们在里面又该如何使用这个savedInstanceState？那么就要搞清楚savedInstanceState会保存到哪些数据，有两种：系统帮你自动保存的和你自己保存的。系统只会保存它认为有必要保存的（比方说EditText里面的内容，CheckBox的Check状态，Fragment实例等），但是很多童鞋不知道Activity会自动保存其中的Fragment实例，onCreate写成这样子：



```css
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, FragmentBase.newInstance(this, "One", FragmentBase.class.getName()))
                .add(R.id.container, FragmentFour.newInstance(this, "Two", FragmentFour.class.getName()))
                .add(R.id.container, FragmentFive.newInstance(this, "Three", FragmentFive.class.getName()))
                .commit();
    }
```

这样会有个问题，当Activity在非正常情况下重启时，由于系统已经保存了FragmentBase，FragmentFour和FragmentFive三个Fragment实例，而你又重新添加了三个Fragment实例，结果导致Activity中存在了6个Fragment实例。截图如下：

![img](https:////upload-images.jianshu.io/upload_images/694018-9455c84889c32af4.png?imageMogr2/auto-orient/strip|imageView2/2/w/831/format/webp)

activitymanager查看到的结果

这样不仅浪费内存资源还有可能会引发App行为异常。所以在了解了savedInstanceState的作用后正确的写法应该是这样的：



```kotlin
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if (savedInstanceState != null) {
            // 这里根据自己需要去从savedInstanceState中去数据
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(FragmentBase.class.getName());
            if (fragment instanceof FragmentBase) {
                FragmentBase base = (FragmentBase) fragment;
                
            }
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, FragmentBase.newInstance(this, "One", FragmentBase.class.getName()))
                    .add(R.id.container, FragmentFour.newInstance(this, "Two", FragmentFour.class.getName()))
                    .add(R.id.container, FragmentFive.newInstance(this, "Three", FragmentFive.class.getName()))
                    .commit();
        } 
    }
```

再说说onDestroy，执行到这一步，一般代表activity即将要被销毁掉，不管是正常情况还是非正常情况关闭activity。一般在这里面我们会做一些资源的释放操作，以防止出现资源泄露或者依赖activity所引发的一些异常情况的发生。这里我举两个例子来说下上面说的两种情况：

- 异步任务引发的资源泄露，比如handler或者thread。这种情况发生的原因主要是异步任务的生命周期与activity生命周期不同步造成的，以handler中的message为例：



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

### onStart、onRestart和onStop

说实话这三个回调接口在实际使用场景中并不多，对onStart、onRestart和onStop的使用可以从是否可见这点来找到它的正确使用姿势。这里举几个常用的场景：

- 对数据的时效性要求较高。以新闻类App为例：Activity A代表新闻列表，点击列表中的一个Item进入到Activity B新闻详情，在从B返回到A的时候为了保证用户能看到最新的新闻，就需要从服务器拉取最新的新闻列表数据并填充到Activity A，那么这个工作就可以放在onStart里面，当然你也可以放在onRestart里面，但是activity首次加载启动的时候不会调用onRestart，所以也就不会去拉取新闻列表数据。
- 需要显示动画效果。有些activity需要显示一些动画来帮助提升用户体验，但是当我们从该页面进入到一个新页面时，由于该页面已经不可见了，所以就可以把当前页面中的动画给关掉以节省系统资源，而这个工作就可以放在onStop中进行。

### onResume和onPause

这两个接口使用的频率比上一组要高，对onResume和onPause的使用可以从可以从是否获得焦点（焦点即代表是是否可交互）这点来找到它的正确使用姿势，这里也举几个常见场景：

- 结束占用CPU的动画或者其他正在运行任务，这种在使用地图SDK的时候比较常见：



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

- 视频播放，当视图组件获得焦点时，即onResume中播放视频，当视图组件失去焦点时，即onPause中暂停播放视频。
- 保存重要数据，为了防止App被意外强杀，一般会在onPause中将一些重要数据保存到本地。

其实一般情况下他们和onStart、onRestart和onStop这一组里面做的事情可以是一样的，也就是说放在onResume中执行的任务也可以放在onStop中去做，放在onPause中执行的任务也可以放到onStop中去做。不过他们之间还是有区别的：可见性和可交互性。我们需要根据具体的需求来分析哪些任务可以在视图可见或者不可见的时候做，哪些任务可以在焦点获得或者失去的时候去做。

## Activity四种启动模式

个人认为Activity四种启动模式的设定出于两种目的：

- 复用机制，节省系统资源，这种情况主要发生在除Standard模式外的三种模式上，通过他们的名字前缀Single也可以看出，跟Java中的单例模式有类似的思想，避免太多的实例对象创建开销。
- 根据用户的交互行为定义，因为Activity最终的目的还是完成用户在各种交互场景下的需求。

下面结合具体使用场景来细说每种启动模式：

### Standard

这种启动模式最常见，也是Activity的默认启动模式，每当我们需要开启一个新的Activity页面时系统都会新建一个Activity实例对象，然后开启上面说的Activity的生命周期流程之旅，onCreate->onStart->onResume。

### SingleTop

设置该模式后，当通过startActivity启动的Activity与当前Task 栈中最顶部的Activity一样时，系统不会重新创建一个Activity实例，而是进入一个特殊的方法onNewIntent，具体流程为：onNewIntent->onResume。这种场景还是比较多的，比方说商品详情页面一般都会有相关的商品推荐，点击推荐的某个商品后进入的还是一个商品详情页面，这个时候就不需要重新再创建一个新的商品详情Activity页面，直接复用已有的页面，刷新下View中的数据就好了。

### SingleTask

设置该模式可以保证当前Task栈中每种Activity只会有一个实例存在，当通过startActivity启动Activity A时，如果当前Task栈中已经存在一个Activity A的实例，那么不再重新创建一个新的Activity实例，而是直接复用该实例，进入该Activity的onNewIntent方法，同时将位于Activity A实例之上的所有Activity弹出Task栈并销毁。这种场景在IM应用中使用的比较多，比如QQ或者微信的聊天页面，当从聊天页面进入其他页面，然后在重新进入聊天页面时就会直接进入原来的聊天页面，同时销毁中间新创建的Activity页面，并刷新聊天页面的数据。

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