package org.cubewhy

import reactor.core.publisher.Mono

fun main() {
    Mono.just("1") // 创建一个Mono
        .flatMap { data ->
            // 处理数据
            Mono.just("Hello, ${data}!")
        }
        // 和Flux一样,你还可以进行其他操作
        .subscribe {
            println(it) // Hello, 1!
        }

    Mono.empty<String>()
        .flatMap { data ->
            Mono.just("Hello, ${data}!")
        }
        .doFirst {
            println("Started") // 这个可以被执行
            // 如果你要使用 if (data == null) data = xxx不要在这里进行, 请看下面的switchIfEmpty
        }
        .subscribe {
            println(it) // 这个中的操作永远不会被执行 因为Mono是空的
        }

    // 如果用for循环表示看起来是这样,但是Mono比这个复杂得多
//    val list = emptyList<String>() // 长度为0或1
//    for (item in list) {
//        // 这里你可以想象为流中操作数据的代码
//        println(item) // 如果list没有东西 这里就执行不到
//    }

    // switchIfEmpty
    Mono.empty<String>()
        .switchIfEmpty(Mono.just("1234")) // 如果上面的Mono是空的, 就使用下面的Mono
        .subscribe {
            println(it) // 输出1234
        }
}