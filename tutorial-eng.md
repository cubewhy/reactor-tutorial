# Introduction

> This tutorial doesn't have too many professional terms, it should be relatively easy to understand.

> This has been **roughly** translated from Chinese -> English, please excuse any grammatical errors.

## How to use

This tutorial has some code snippets that you can modify and use as you wish.

It is recommended to read this while you program.

## Resources

Not many people know what to search for when researching this topic & using ChatGPT may not always be correct.

> https://projectreactor.io/docs/core/release/reference/

> https://en.wikipedia.org/wiki/Reactive_programming

## What is "responsive"

Responsive means all operations will not be blocked and completed in asynchronus threads.

> If you are familiar with NodeJS, `promise` is asynchronous.

### What makes this different from "traditional" servers

Traditional servers, such as `TomCat`, will open a new thread every time there is a connection made.

Although this is simple, if the number of connections is too much, it will cause a memory overflow and performance issues

If you use responsiveness, no new threads will be opened and all operations will be completed asynchronously.

## What you need to know

> You can learn about reactive programming [here](https://projectreactor.io/docs/core/release/reference/reactiveProgramming.html).

⚠️ In reactive mode, the code you write will only be executed when the publisher is blocked, it does not mean that code will be executed once you write it.

The writing in this document is relatively simple and it's recommended to read what the official website says.

You'll have to change your thinking to adapt to reactive programming.

In sync, your code would look like this..

```java
public User findUser(String username) {
    return userRepository.findByUsername(username); // 直接返回user对象 会阻塞
}

// In a traditional server, this function will be executed in a new thread
public SomeData useUser() {
    String username = "1234";
    User user = this.findUser(username); // blocking thread
    // Calling other work will also block the thread
    var data1 = processData(user);
    var finalData = processFinal(data1);
    return finalData;
}
```

Asynchronous code would look like..

```java
public Mono<User> findUser(String username) {
    return userRepository.findByUsername(username); // 返回Mono, 有结果了调用流处理user
}

// In a reactive server, this function will be executed asynchronously
public Mono<SomeData> useUser() {
    String username = "1234";
    // After getting the user, call flatmap.
    // You cannot obtain User from outside. If you use .block() to obtain it, it will cause the thread to block.
    // Things that block in reactive should use Schedulers to prevent blocking.
    return this.findUser(username).flatMap(user -> {
        // Perform other operations
        return processData(user);
    }).flatMap(data -> { // The data here is passed from above. You can continue the operation here.
        return processFinal(data);
    }); // This stream must be returned, otherwise the code in it will not be executed for a lifetime.
    // In practice, you still have to perform error handling, which we will explain later.
}
```

Complex? You can use Kotlin coroutines to simplify your code. This tutorial will also be written in Kotlin.

Code written in Kotlin looks like this

```kotlin
// Do you see the suspend here? This function can only run within the coroutine
suspend fun findUser(username: String): User? {
    return userRepository.findByUsername(username)
        .awaitFirst() // awaitFirst is an extension function of Mono. Note: This function needs to be executed within the coroutine instead of blocking
}

suspend fun useUser(): SomeData {
    val user = findUser("1234") // suspend!
    // Then you can continue writing your code just like synchronously
    val data = processData(user)
    return processFinal(data)
}

// wait... So how do I call userUser? Functions with the suspend modifier can only run within the coroutine.
// This has mono {} and flux {} methods in Kotlin reactor, allowing you to convert the running results of the coroutine into Publisher. This will be discussed later.
// Don’t know what Publisher is? I’ll explain it later.

```

## What is Publisher

Publisher is an interface which provides a subscribe method to subscribe to the stream

The code inside will not be actually executed until the stream is subscribed.



```java
public interface Publisher<T> {
    void subscribe(Subscriber<? super T> var1);
}
```

Flux, Mono is an implementation of Publisher

## What is Flux

> Want the best description possible? [You can view the Flux documentation here](https://projectreactor.io/docs/core/release/reference/coreFeatures/flux.html)

In simple terms, Flux is a Stream, but it can add data.

Whenever a piece of data is added, the stream you defined will be executed once

There can be multiple data (or 0) in Flux

![flux](images/flux.svg)

## What is Mono

> Want the best description possible? [You can view the Mono documentation here](https://projectreactor.io/docs/core/release/reference/coreFeatures/mono.html)

Mono can only contain one data (or empty), and the flow inside will be called after being blocked.

![mono](images/mono.svg)

## How do you convert the results of Kotlin to Publisher

```kotlin
// It's very simple. Just use mono {} or flux {} to wrap it up. It's asynchronous inside.
// There’s a pitfall! The operation of reading inputStream needs to be performed outside mono {} or flux {}, otherwise the stream will be closed.
anotherPublisher
    .flatMap { data ->
        mono {
            processData(data) // process data is the suspend method
        } // Return a Mono
        // The same is true for flux, so I won’t write it here.
    }
```
