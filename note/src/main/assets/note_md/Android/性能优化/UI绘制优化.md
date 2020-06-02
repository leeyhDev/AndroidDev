## UI绘制优化

### 什么是过度绘制？

过度绘制是指系统在渲染单个帧的过程中<span style="font-size: 18px;color:#000">**多次在屏幕上绘制某一个像素**</span>。过度绘制通常是不必要的，最好避免。它会浪费 GPU 时间来渲染与用户在屏幕上所见内容无关的像素，进而导致性能问题。

例如，如果我们有若干界面卡片堆叠在一起，每张卡片都会遮盖其下面一张卡片的部分内容。但是，系统仍然需要绘制堆叠中的卡片被遮盖的部分。这是因为堆叠的卡片是根据 [Painter 算法](https://en.wikipedia.org/wiki/Painter's_algorithm)（也就是<span style="font-size: 18px;color:#ef570e">**按从后到前的顺序**</span>）来渲染的。按照这种渲染顺序，系统可以将适当的透明度混合应用于阴影之类的半透明对象。

> **注意**：过度绘制的问题在 Google I/O 大会性能会议和[性能模式视频](https://www.youtube.com/watch?v=vkTn3Ule4Ps&hl=zh-cn)中讨论过，这个问题现在已没有当时那么严重了。这是因为低端设备的 GPU 性能不断提升，而其显示屏的分辨率保持在了一个相对较低的水平。除非是要针对已知的低性能 GPU 设备进行优化，否则建议将重点放在优化界面线程工作上，以确保应用性能稳定。除此之外，在很多情况下，操作系统优化可以避免应用内的过度绘制（例如，Fragment 背景过度绘制窗口背景）。

Android的渲染依赖于两个核心组件：

- CPU：负责包括Measure，Layout，Record，Execute的<span style="font-size: 18px;color:#ef570e">**计算操作** </span>（容易产生不必要的重绘）
- GPU：负责Rasterization(栅格化)操作 (<span style="font-size: 18px;color:#ef570e">**容易产生过度绘制的问题**</span>)

### 直观呈现 GPU 过度绘制

您应先[启用开发者选项](https://developer.android.com/studio/debug/dev-options?hl=zh-cn#enable)（如果您尚未执行此操作）。然后，如需在您的设备上直观呈现过度绘制问题，请按以下步骤操作：

1. 在您的设备上，转到 **Settings** 并点按 **Developer Options**。
2. 向下滚动到**硬件加速渲染**部分，并选择**调试 GPU 过度绘制**。
3. 在**调试 GPU 过度绘制**对话框中，选择**显示过度绘制区域**。

或者可以使用以下adb命令：

```kotlin
adb shell setprop debug.hwui.overdraw show      //开启『调试 GPU 过度绘制』
adb shell setprop debug.hwui.overdraw false		//关闭『调试 GPU 过度绘制』	
```

Android 将按如下方式为界面元素着色，以确定过度绘制的次数：

- **真彩色**：没有过度绘制
- ![overdraw-blue](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/20200602092106-608124.png)**蓝色**：过度绘制 1 次
- ![overdraw-green](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/20200602092219-108132.png) **绿色**：过度绘制 2 次
- ![overdraw-pink](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/20200602092221-145674.png) **粉色**：过度绘制 3 次
- ![overdraw-red](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/20200602092223-255485.png) **红色**：过度绘制 4 次或更多次

请注意，有些过度绘制是不可避免的。在优化您的应用的界面时，应尝试达到大部分显示真彩色或仅有 1 次过度绘制（蓝色）的视觉效果。

### 如何减少过度绘制

#### 移除布局中不需要的背景

 默认情况下，布局没有背景，这表示布局本身不会直接渲染任何内容。但是，当布局具有背景时，其有可能会导致过度绘制。移除不必要的背景可以快速提高渲染性能。不必要的背景可能永远不可见，因为它会被应用在该视图上绘制的任何其他内容完全覆盖。例如，当系统在父视图上绘制子视图时，可能会完全覆盖父视图的背景。

方法一：去掉window的默认背景

```java
getWindow().setBackgroundDrawable(null)；//activity中
android:windowbackground="@null"；//theme中
```

方法二：去掉其他不必要的背景

```
排查Layout中不必要的背景
```

#### 使视图层次结构扁平化

使用合理的布局方式减少布局嵌套，采用 **merge**(不增加布局层次)/**include** (会增加嵌套)标签优化布局

#### 降低透明度。

在屏幕上渲染透明像素，即所谓的透明度渲染，是导致过度绘制的重要因素。在普通的过度绘制中，系统会在已绘制的现有像素上绘制不透明的像素，从而将其完全遮盖，与此不同的是，<span style="font-size: 18px;color:#ef570e">**透明对象需要先绘制现有的像素**</span>，以便达到正确的**<span style="font-size: 18px;color:#ef570e">混合效果</span>**。诸如透明动画、淡出和阴影之类的视觉效果都会涉及某种透明度，因此有可能导致严重的过度绘制。您可以通过减少要渲染的透明对象的数量，来改善这些情况下的过度绘制。**例如，如需获得灰色文本，您可以在 TextView 中绘制黑色文本，再为其设置半透明的透明度值。**但是，您可以简单地通过用灰色绘制文本来获得同样的效果，而且能够大幅提升性能。

#### ClipRect & QuickReject

 为了解决Overdraw的问题，Android系统会通过避免绘制那些完全不可见的组件来尽量减少消耗。但是不幸的是，对于那些过于复杂的自定义的View(通常重写了onDraw方法)，Android系统无法检测在onDraw里面具体会执行什么操作，系统无法监控并自动优化，也就无法避免Overdraw了。但是我们可以通过canvas.clipRect()来帮助系统识别那些可见的区域。**这个方法可以指定一块矩形区域，只有在这个区域内才会被绘制，其他的区域会被忽视。**这个API可以很好的帮助那些有多组重叠组件的自定义View来控制显示的区域。同时clipRect方法还可以帮助节约CPU与GPU资源，在clipRect区域之外的绘制指令都不会被执行，那些部分内容在矩形区域内的组件，仍然会得到绘制。除了clipRect方法之外，**我们还可以使用canvas.quickreject()来判断是否没和某个矩形相交**，从而跳过那些非矩形区域内的绘制操作。DrawerLayout是一个很好的例子。

#### 使用ViewStub占位

 ViewStub是个什么东西？一句话总结：**高效占位符。**我们经常会遇到这样的情况，运行时动态根据条件来决定显示哪个View或布局。常用的做法是把View都写在上面，先把它们的可见性都设为View.GONE，然后在代码中动态的更改它的可见性。这样的做法的优点是逻辑简单而且控制起来比较灵活。但是它的缺点就是，耗费资源。虽然把View的初始可见View.GONE但是在Inflate布局的时候View仍然会被Inflate，也就是说仍然会创建对象，会被实例化，会被设置属性。也就是说，会耗费内存等资源。推荐的做法是使用android.view.ViewStub，ViewStub是一个轻量级的View，它一个看不见的，不占布局位置，占用资源非常小的控件。可以为ViewStub指定一个布局，在Inflate布局的时候，只有ViewStub会被初始化，然后当ViewStub被设置为可见的时候，或是调用了ViewStub.inflate()的时候，ViewStub所向的布局就会被Inflate和实例化，然后ViewStub的布局属性都会传给它所指向的布局。这样，就可以使用ViewStub来方便的在运行时，要还是不要显示某个布局。

#### 善用draw9patch

 给ImageView加一个边框，你肯定遇到过这种需求，通常在ImageView后面设置一张背景图，露出边框便完美解决问题，此时这个ImageView，设置了两层drawable，底下一层仅仅是为了作为图片的边框而已。但是两层drawable的重叠区域去绘制了两次，导致overdraw。优化方案： 将背景drawable制作成draw9patch，并且将和前景重叠的部分设置为透明。由于Android的2D渲染器会优化draw9patch中的透明区域，从而优化了这次overdraw。 但是背景图片必须制作成draw9patch才行，因为Android 2D渲染器只对draw9patch有这个优化，否则，一张普通的Png，就算你把中间的部分设置成透明，也不会减少这次overdraw。

#### ImageView的background和imageDrawable重叠

 Android中，所有的view均可以设置background。ImageView除了能够设置background之外，还能设置ImageDrawable。在开发中，很多时候需要显示图片，在图片加载出来之前通常是需要显示一张默认图片的，很多时候会使用ImageView的background属性来设置默认背景图，而imageDrawable来设置需要加载的图片。这样会导致一个问题，当图片加载到页面后，默认背景图被挡住了，但是却仍然需要绘制，导致过度绘制情况的发生。解决方案是把背景图和真正加载的图片都通过imageDrawable方法进行设置。

