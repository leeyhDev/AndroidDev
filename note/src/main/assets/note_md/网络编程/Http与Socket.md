## Http与Socket

### 什么是Http协议？

HTTP协议是**超文本传输协议**的缩写，英文是Hyper Text Transfer Protocol。它是从WEB服务器传输超文本标记语言(HTML)到本地浏览器的传送协议。HTTP协议是一种应用层协议，默认端口号是 80，可以通过传输层的TCP协议在客户端和服务器之间传输数据（B/S架构），一般不使用UDP协议。

### Http连接的特点

最显著的特点是，客户端每次发送的请求，都需要服务器响应，请求结束后，会主动释放连接。从建立连接到关闭连接的过程，成为”一次连接”。

- http协议支持客户端/服务端模式，也是一种请求/响应模式的协议。
- 简单快速：客户向服务器请求服务时，只需传送请求方法和路径。请求方法常用的有GET、HEAD、POST。
- 灵活：HTTP允许传输任意类型的数据对象。传输的类型由Content-Type加以标记。
- **无连接**：限制每次连接只处理一个请求。服务器处理完请求，并收到客户的应答后，即断开连接，但是却不利于客户端与服务器保持会话连接，为了弥补这种不足，产生了两项记录http状态的技术，一个叫做**Cookie**,一个叫做**Session**。
- **无状态**：无状态是指协议对于事务处理没有记忆，后续处理需要前面的信息，则必须重传。

### HTTP原理

HTTP是一个基于TCP/IP通信协议来传递数据的协议，传输的数据类型为HTML 文件,、图片文件, 查询结果等。

HTTP协议一般用于B/S架构（）。浏览器作为HTTP客户端通过URL向HTTP服务端即WEB服务器发送所有请求。

我们以访问百度为例：



![img](https://gitee.com/leeyhDev/TyporaImages/raw/master/images/20200605104025-256628.jpeg)

### 5.URI和URL的区别

HTTP使用统一资源标识符（Uniform Resource Identifiers, URI）来传输数据和建立连接。

- URI：Uniform Resource Identifier 统一资源**标识**符
- URL：Uniform Resource Location 统一资源**定位**符

URI 是用来标示 一个具体的资源的，我们可以通过 URI 知道一个资源是什么。

URL 则是用来定位具体的资源的，标示了一个具体的资源位置。互联网上的每个文件都有一个唯一的URL。