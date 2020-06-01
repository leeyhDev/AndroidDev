## Android序列化与反序列化

#### why

为什么要序列化与反序列化？Android中组件之间（Activity、Fragment、Service）传递对象，网络上用套接字传送对象，对内存中的对象状态进行持久化存储（文件或者数据库）等。

#### what

序列化：把对象转换为字节序列的过程称为对象的序列化。

反序列化：把字节序列恢复为对象的过程称为对象的反序列化。

#### how

对象实现Serializable接口[ˈsɪˌriəˌlaɪzəbl]（Java自带）

对象实现Parcelable接口（Android专用）

### Serializable与Parcelable的区别

- 在内存中传递数据（Android程序间（AIDL），IBinder通信）Parcelable效率更高。 

- Serializable 使用反射，序列化和反序列化过程需要大量 I/O 操作，会产生大量的临时变量，从而引起频繁的GC。Parcelable 自已实现封送和解封（marshalled &unmarshalled）操作不需要用反射，数据也存放在 Native 内存中，效率要快很多。
- Parcelable也可以应用在数据存储在磁盘上的情况，但是Parcelable需要大量的代码模块，在对象结构复杂的情况下不利于读写。而Serializable就要简单的许多。

在Android组件之间传递数据，无论使用Serializable还是Parcelable都需要注意对象数据的大小，避免TransactionTooLargeException异常（The Binder transaction failed because it was too large）。