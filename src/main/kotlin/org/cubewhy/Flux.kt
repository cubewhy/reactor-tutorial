package org.cubewhy

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun main() {
    Flux.just("1", "2", "3") // 创建一个flux 里边有三个数据
        .flatMap { data ->
            Mono.just("Hello, ${data}!") // 处理data 然后返回一个Mono
        }
        .flatMap { data ->
            // 继续操作
            Mono.just("$data I'm a teapot!")
        }
        .doFirst {
            // 流开始执行
            println("Start!")
        }
        .doFinally {
            // 流被关闭了 (执行完或者发生错误)
            println("Finally!")
        }
        .subscribe { data ->
            println(data) // 别忘了调用流! (实际中你不需要手动subscribe或者block, 在异步线程内调用会导致阻塞. 你只需要return这个流就可以了)
        }
    // 运行起来是这样的
    // Start!
    //Hello, 1! I'm a teapot!
    //Hello, 2! I'm a teapot!
    //Hello, 3! I'm a teapot!
    // Finally!
}