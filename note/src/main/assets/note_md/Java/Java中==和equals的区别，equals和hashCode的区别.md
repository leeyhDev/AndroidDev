## Java中==和equals的区别，equals和hashCode的区别

**equals**是Objec类的方法，用于比较两个对象是否相等，默认Object类的equals方法是比较两个对象的地址，跟==的结果一样。Object的equals方法如下：

    public boolean equals(Object obj) {
        return (this == obj);
    }

**hashCode**在Object类中的实现是将对象在内存中的地址转化为一个int类型。在集合类操作中使用，为了提高查询速度。（HashMap，HashSet等）

### java中的数据类型

- **基本数据类型**，分为boolean、byte（8）、char（16）、short（16）、int（32）、float（64）double（64）、long（64）；应用双等号（==）,比较的是他们的值。 

- **复合数据类型（类）**。
    当他们用（==）进行比较的时候，比较的是他们在内存中的存放地址，只有同一个new出来的对象比较后的结果为true。 JAVA当中所有的类都是继承于Object这个基类的，在Object中的基类中定义了一个equals的方法，这个方法的初始行为是比较对象的内存地 址，但在一些类库当中这个方法被覆盖掉了，如String,Integer,Date在这些类当中equals有其自身的实现，而不再是比较类在堆内存中的存放地址了。  对于复合数据类型之间进行equals比较，在没有覆写equals方法的情况下，他们之间的比较还是基于他们在内存中的存放位置的地址值的，因为Object的equals方法也是用双等号（==）进行比较的，所以比较后的结果跟双等号（==）的结果相同。

在没有覆写equals()和hashCode()的情况下：

- equals方法返回“true”，其hashCode方法返回的值是相同的。

- equals方法返回“false”，其hashCode方法返回的值不一定不同，因为其内部实现的算法不能够保证输入值不同，输出值也不同。例如1 + 1 = 2 ，    0 + 2 = 2。

- hashCode返回的值不同，其equals方法返回的一定是“false”

### 总结：

- equals方法本意是用来判断引用的对象是否一致，内存地址是否一致。

- “==”用在基本数据类型比较时是比较他们的“值”是否相等，用在复合数据类型（类）比较时则与equals()效果一致。
- hashcode是系统用来快速检索对象而使用。
- 重写equals方法和hashcode方法时，equals方法中用到的成员变量也必定会在hashcode方法中用到,只不过前者作为比较项，后者作为生成摘要的信息项，本质上所用到的数据是一样的，从而保证二者的一致性。