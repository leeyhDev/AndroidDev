## int与Integer区别

- Integer是int的包装类；int是基本数据类型；
- Integer变量必须实例化后才能使用；int变量不需要；
- Integer实际是对象的引用，指向此new的Integer对象；int是直接存储数据值 ；
- Integer的默认值是null；int的默认值是0。
- 泛型不支持int，但是支持Integer
- int 存储在栈中，Integer 对象的引用存储在栈空间中，对象的数据存储在堆空间中。

#### 分析

```
int 是java的基本数据类型。
```

```
Integer 继承了Object类，是对象类型，是 int 的包装类。
```

##### Java两种数据类型

- 基本数据类型，分为boolean、byte、int、char、long、short、double、float；
- 引用数据类型 ，分为数组、类、接口、数组、枚举、标注。

##### Java原始类型的封装类

为了编程的方便还是引入了基本数据类型，但是为了能够将这些基本数据类型当成对象操作，Java为每 一个基本数据类型都引入了对应的包装类型（wrapper class），int的包装类就是Integer，从Java 5开始引入了自动装箱/拆箱机制，使得二者可以相互转换。

| 基本类型 | 包装类型  |
| :------: | :-------: |
|   byte   |   Byte    |
|  short   |   Short   |
|   int    |  Integer  |
|   long   |   Long    |
|  float   |   Float   |
|  double  |  Double   |
|   char   | Character |
| boolean  |  Boolean  |



##### 自动装箱和自动拆箱

自动装箱：将基本数据类型重新转化为对象

```java
//声明一个Integer对象
Integer num = 1;
//以上的声明就是用到了自动的装箱：解析为:Integer num = new Integer(1);
```

1是属于基本数据类型的，原则上它是不能直接赋值给一个对象Integer的。但jdk1.5后你就可以进行这样的声明，自动将基本数据类型转化为对应的封装类型，成为一个对象以后就可以调用对象所声明的所有的方法。

自动拆箱：将对象重新转化为基本数据类型

```java
//自动装箱 将基本数据类型变成对象
Integer integer = 1;
//自动拆箱
System.out.println(integer+10);
```

因为对象是不能直接进行运算的，而是要转化为基本数据类型后才能进行加减乘除。

对比：

```java
Integer a = 10;// 装箱
int b = a;// 拆箱
```

##### 相同值下的 int 和 Integer 的比较结果

1. 两个通过new生成的变量，结果为false。

   ```java
   Integer i1 = new Integer(10);
   Integer i2 = new Integer(10);
   System.out.print(i1 == i2); //false
   ```

2. int 和 Integer 的值比较，若两者的值相等，则为true。（注意：在比较时，Integer会自动拆箱为int类型，然后再做比较，实际上就变为两个int变量的比较）

   ```java
   Integer i1 = new Integer(10);
   int i2 = 10；
   System.out.print(i1 == i2); //true
   ```

3. new 生成的Integer变量 和 非new 生成的Integer变量比较，结果为false。（注意：new 生成的Integer变量的值在堆空间中，非new 生成的Integer变量的值在在常量池中。非new生成的Integer变量，会先判断常量池中是否有该对象，若有则共享，若无则在常量池中放入该对象；这也叫享元模式）

   ```java
   Integer i1 = new Integer(10);
   Integer i2 = 10;
   System.out.print(i1 == i2); //false
   ```

4. 对于两个非new生成的Integer对象，进行比较时，如果两个变量的值在区间-128到127之间，则比较结果为true，如果两个变量的值不在此区间，则比较结果为false。

   ```java
   Integer i1 = 10;
   Integer i2 = 10;
   System.out.print(i1 == i2); //true
   Integer j1 = 128;
   Integer j2 = 128;
   System.out.print(j1 == j2); //false
   ```

   当值在 -128 ~ 127之间时，java会进行自动装箱，然后会对值进行缓存，如果下次再有相同的值，会直接在缓存中取出使用。缓存是通过Integer的内部类IntegerCache来完成的。当值超出此范围，会在堆中new出一个对象来存储。

   JDK1.5之后，java提供了自动装箱和自动拆箱的功能。自动装箱也就是调用了Integer类的一个静态方法`valueOf`方法,源码如下：

   ```java
   public static Integer valueOf(int i) {
       if (i >= IntegerCache.low && i <= IntegerCache.high)
           return IntegerCache.cache[i + (-IntegerCache.low)];
       return new Integer(i);
   }
   ```

   源码中有一个`IntegerCache`，这是一个私有的内部类。这个类缓存了`low - high`之间数字的包装类。我们可以看到源码中的解析为：

   - 缓存支持自动装箱的对象标识语义 -128和127（含），
   - 缓存在第一次使用时初始化。 缓存的大小可以由-XX：AutoBoxCacheMax = 选项控制。
   - 在VM初始化期间，java.lang.Integer.IntegerCache.high属性可以设置并保存在私有系统属性中

   如果判断成立就把缓存中的那个包装类返回，反之则`new`一个新的。这样我们也就明白了上面第四点Integer比较结果为false的原因了。

