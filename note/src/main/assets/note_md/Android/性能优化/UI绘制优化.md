## UI绘制优化

### 什么是过度绘制？

过度绘制是指系统在渲染单个帧的过程中<span style="font-size: 18px;color:#000">**多次在屏幕上绘制某一个像素**</span>。过度绘制通常是不必要的，最好避免。它会浪费 GPU 时间来渲染与用户在屏幕上所见内容无关的像素，进而导致性能问题。

例如，如果我们有若干界面卡片堆叠在一起，每张卡片都会遮盖其下面一张卡片的部分内容。但是，系统仍然需要绘制堆叠中的卡片被遮盖的部分。这是因为堆叠的卡片是根据 [Painter 算法](https://en.wikipedia.org/wiki/Painter's_algorithm)（也就是<span style="font-size: 18px;color:#000">**按从后到前的顺序**</span>）来渲染的。按照这种渲染顺序，系统可以将适当的透明度混合应用于阴影之类的半透明对象。

> **注意**：过度绘制的问题在 Google I/O 大会性能会议和[性能模式视频](https://www.youtube.com/watch?v=vkTn3Ule4Ps&hl=zh-cn)中讨论过，这个问题现在已没有当时那么严重了。这是因为低端设备的 GPU 性能不断提升，而其显示屏的分辨率保持在了一个相对较低的水平。除非是要针对已知的低性能 GPU 设备进行优化，否则建议将重点放在优化界面线程工作上，以确保应用性能稳定。除此之外，在很多情况下，操作系统优化可以避免应用内的过度绘制（例如，Fragment 背景过度绘制窗口背景）。

Android的渲染依赖于两个核心组件：

- CPU：负责包括Measure，Layout，Record，Execute的<span style="font-size: 20px;color:#f00">**计算操作** </span>（容易产生不必要的重绘）
- GPU：负责Rasterization(栅格化)操作 (容易产生过度绘制的问题)

### 直观呈现 GPU 过度绘制

您应先[启用开发者选项](https://developer.android.com/studio/debug/dev-options?hl=zh-cn#enable)（如果您尚未执行此操作）。然后，如需在您的设备上直观呈现过度绘制问题，请按以下步骤操作：

1. 在您的设备上，转到 **Settings** 并点按 **Developer Options**。
2. 向下滚动到**硬件加速渲染**部分，并选择**调试 GPU 过度绘制**。
3. 在**调试 GPU 过度绘制**对话框中，选择**显示过度绘制区域**。

Android 将按如下方式为界面元素着色，以确定过度绘制的次数：

- **真彩色**：没有过度绘制
- ![overdraw-blue](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/202006/02/013413-603798.png)**蓝色**：过度绘制 1 次
- ![overdraw-green](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/202006/02/013537-103333.png) **绿色**：过度绘制 2 次
- ![overdraw-pink](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/202006/02/013426-744051.png) **粉色**：过度绘制 3 次
- ![overdraw-red](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/202006/02/013427-177467.png) **红色**：过度绘制 4 次或更多次

请注意，有些过度绘制是不可避免的。在优化您的应用的界面时，应尝试达到大部分显示真彩色或仅有 1 次过度绘制（蓝色）的视觉效果。

### 如何减少过度绘制

##### 移除布局中不需要的背景

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

##### 使视图层次结构扁平化

使用合理的布局方式减少布局嵌套，采用 **merge**（不增加布局层次），**include** 则会增加嵌套

##### 降低透明度。

在屏幕上渲染透明像素，即所谓的透明度渲染，是导致过度绘制的重要因素。在普通的过度绘制中，系统会在已绘制的现有像素上绘制不透明的像素，从而将其完全遮盖，与此不同的是，**透明对象需要先绘制现有的像素**，以便达到正确的混合效果。诸如透明动画、淡出和阴影之类的视觉效果都会涉及某种透明度，因此有可能导致严重的过度绘制。您可以通过减少要渲染的透明对象的数量，来改善这些情况下的过度绘制。**例如，如需获得灰色文本，您可以在 TextView 中绘制黑色文本，再为其设置半透明的透明度值。**但是，您可以简单地通过用灰色绘制文本来获得同样的效果，而且能够大幅提升性能。



https://github.com/Thobian/typora-plugins-win-img

60c5dbc12bf9bfcfc62be12fc2947361

TyporaImages

