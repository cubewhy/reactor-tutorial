# 开始

> 这个教程没有太多的专业术语, 说的话基本上都是会写传统服务端就可以听懂的 可能有错误

## 怎么用这个教程

这个教程有一些代码, 你可以修改它们, 看看会发生什么

建议把文档放到窗口右边

## 其他的资源

这个东西在网络上搜索没有几个人会, 甚至问ChatGPT都可能是错的

https://projectreactor.io/docs/core/release/reference/

维基百科 https://en.wikipedia.org/wiki/Reactive_programming

## 什么是响应式

响应式就是不会阻塞, 所有操作都在异步线程内完成

如果你比较熟悉nodejs, Promise就是异步的

### 和传统服务端有什么区别

传统服务端, 例如Tomcat 每有一个连接就会开一个线程

这样做虽然简单, 但是如果连接数爆炸, 会导致内存溢出还有性能问题

如果使用响应式, 那就不会开新的线程, 所有的操作都在异步内完成

## 等等...你可能还得了解新的思想

> 觉得简单? 你可以深入了解 [外部链接](https://projectreactor.io/docs/core/release/reference/reactiveProgramming.html)

⚠️ 在响应式, 你写的代码只有在Publisher被block的时候才会执行! 并不是写了就会执行

这里写得比较简单, 建议看看官网说的

你必须转变自己的思想来适应响应式编程

在同步中你的代码看起来是这样的

```java
public User findUser(String username) {
    return userRepository.findByUsername(username); // 直接返回user对象 会阻塞
}

// 在传统服务端中, 这个函数会在新的线程内执行
public SomeData useUser() {
    String username = "1234";
    User user = this.findUser(username); // 阻塞线程
    // 调用其他工作 也会阻塞线程
    var data1 = processData(user);
    var finalData = processFinal(data1);
    return finalData;
}
```

而异步是这样的

```java
public Mono<User> findUser(String username) {
    return userRepository.findByUsername(username); // 返回Mono, 有结果了调用流处理user
}

// 在响应式服务端中, 这个函数会被异步执行
public Mono<SomeData> useUser() {
    String username = "1234";
    // 在获取到user之后 调用flatmap
    // 在外面你不能获取User, 如果使用.block()获取会导致线程阻塞
    // 在reactive中阻塞的东西要使用 Schedulers 来防止阻塞
    return this.findUser(username).flatMap(user -> {
        // 进行其他操作
        return processData(user);
    }).flatMap(data -> { // 这里的data是上面传递下来的 你可以在这里继续操作
        return processFinal(data);
    }); // 务必返回这个流, 否则一辈子里边的代码也无法被执行
    // 实际中你还得进行错误处理, 在后面我们会讲解
}
```

很复杂? 你可以使用Kotlin协程 来简化你的代码 本教程也会使用Kotlin进行编写

使用Kotlin编写的代码看起来是这样的

```kotlin
// 看到这里的suspend了吗? 这个函数只能运行在协程内
suspend fun findUser(username: String): User? {
    return userRepository.findByUsername(username)
        .awaitFirst() // awaitFirst是Mono的扩展函数 注: 这个函数需要在协程内执行, 而不是会阻塞
}

suspend fun useUser(): SomeData {
    val user = findUser("1234") // suspend!
    // 接下来你可以像同步一样继续编写你的代码
    val data = processData(user)
    return processFinal(data)
}

// wait...那我怎么调用userUser? 带有suspend 修饰符的函数只能运行在协程内 这样不就无限套娃了...
// 这个在Kotlin reactor中有 mono {} 和flux {} 方法,允许你将协程的运行结果转换为Publisher 这个后面也会说
// 不知道什么是Publisher? 后面会说
```

## 什么是Publisher

Publisher是一个抽象的类 其中提供了一个 subscribe方法用来订阅流

流被订阅之后里边的代码才会被实际执行

```java
public interface Publisher<T> {
    void subscribe(Subscriber<? super T> var1);
}
```

Flux, Mono都是Publisher的实现

## 什么是Flux

代码 **Flux.kt**

> 觉得简单? [外部链接](https://projectreactor.io/docs/core/release/reference/coreFeatures/flux.html)

用最通俗的话说 Flux是Stream, 但是它是可以添加数据的

每当一个数据被添加进来, 你自己定义的流就会被执行一次

Flux中可以有多个数据(或者0个)

![flux](images/flux.svg)

## 什么是Mono

代码 **Mono.kt**

> 觉得简单? [外部链接](https://projectreactor.io/docs/core/release/reference/coreFeatures/flux.html)

Mono 中只能带有一个数据(或者是空的), 在被block之后会调用里边的flow

![flux](images/mono.svg)

## 如何将Kotlin协程的结果转换为Publisher

```kotlin
// 很简单 使用 mono {} 或者 flux {} 包裹起来就可以了, 里边是异步的
// 有坑! 读取inputStream的操作需要在mono {}或者flux {} 之外执行,否则stream会被关闭
anotherPublisher
    .flatMap { data ->
        mono {
            processData(data) // process data是suspend方法
        } // 返回一个Mono
        // flux同理, 这里不写了
    }
```

## 剩下的呢?

* 我懒得写
* 就着么些,很简单不是吗