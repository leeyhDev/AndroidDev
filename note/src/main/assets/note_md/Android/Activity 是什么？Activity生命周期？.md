# Activity 是什么？Activity生命周期？

###  什么是 Activity?

四大组件之一，实际上是一个与用户交互的接口，一般的一个用户交互界面对应一个 **Activity**。**Activity** 是 **Context** 的子类,同时实现了 **window.callback** 和**keyevent.callback**, 可以处理与窗体用户交互的事件。

### 请描述一下 Activity 生命周期

**Activity** 从创建到销毁有多种状态，从一种状态到另一种状态时会激发相应的回调方法，这些回调方法包括：**onCreate() 、onStart()、 onResume()、onPause()、 onStop()、onDestroy()**，其实这些方法都是两两对应的，onCreate 创建与 onDestroy 销毁；onStart 可见与 onStop 不可见；onResume 可编辑（即焦点）与 onPause；如果界面有共同的特点或者功能的时候,还会自己定义一个 BaseActivity.进度对话框的显示与销毁。

### 如何保存 Activity 的状态或者(Activiy 重启怎么保存数据？）

Activity 的状态通常情况下系统会自动保存的，只有当我们需要保存额外的数据时才需要使用到这样的功能。
一般来说, 调用 onPause()和 onStop()方法后的 activity 实例仍然存在于内存中, activity 的所有信息和状态数据不会消失, 当 activity 重新回到前台之后,所有的改变都会得到保留。但是当系统内存不足时, 调用 onPause()和 onStop()方法后的 activity 可能会被系统摧毁, 此时内存中就不会存有该 activity 的实例对象了。如果之后这个
activity 重新回到前台, 之前所作的改变就会消失。为了避免此种情况的发生, 我们可以覆写 onSaveInstanceState()方法。onSaveInstanceState()方法接受一个 Bundle 类型的参数, 开发者可以将状态数据存储到这个 Bundle 对象中, 这样即使 activity 被系统摧毁, 当用户重新启动这个 activity 而调用它的onCreate()方法时, 上述的 Bundle 对象会作为实参传递给 onCreate()方法, 开发者可以从 Bundle 对象中取出保存的数据, 然后利用这些数据将 activity 恢复到被摧毁之前的状态。需要注意的是, onSaveInstanceState()方法并不是一定会被调用的, 因为有些场景是不需要保存状态数据的. 比如用户按下 BACK 键退出 activity 时, 用户显然想要关闭这个 activity, 此时是没有必要保存数据以供下次恢复的, 也就是onSaveInstanceState()方法不会被调用. 如果调用 onSaveInstanceState()方法, 调用将发生在 onPause()或 onStop()方法之前。

```java
@Override
protected void onSaveInstanceState(Bundle outState) {
	// TODO Auto-generated method stub
	super.onSaveInstanceState(outState);
}
```

### 如何将一个 Activity 设置成窗口的样式

只需要给 Activity 配置： android:theme="@android:style/Theme.Dialog"

###  如何退出 Activity？如何安全退出已调用多个Activity 的 Application？

- 通常情况用户退出一个 Activity 只需按返回键，我们写代码想退出 activity直接调用 finish()方法就行。
- 记录打开的 Activity：
  每打开一个 Activity，就记录下来。在需要退出时，关闭每一个 Activity 即可。
  //伪代码
  List<Activity> lists ;// 在 application 全局的变量里面
  lists = new ArrayList<Activity>();
  lists.add(this);
  for(Activity activity: lists){
          activity.finish();
  }
  lists.remove(this);
- 发送特定广播：
  在需要结束应用时，发送一个特定的广播，每个 Activity 收到广播后，关闭即可。
  //给某个 activity 注册接受接受广播的意图
  registerReceiver(receiver, filter)
  //如果过接受到的是 关闭 activity 的广播 就调用 finish()方法 把当前的activity finish()掉
- 递归退出
  在打开新的 Activity 时使用 startActivityForResult，然后自己加标志，在onActivityResult 中处理，递归关闭。
- 其实 也可以通过 intent 的 flag 来实现
  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)激活一个新的 activity。
  此时如果该任务栈中已经有该 Activity，那么系统会把这个 Activity 上面的所有Activity 干掉。其实相当于给 Activity 配置的启动模式为 SingleTop。