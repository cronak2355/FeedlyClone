package com.feedly.feedlyclonebackend

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FeedlyCloneBackendApplication
fun main(args: Array<String>) {
    val dotenv = Dotenv.configure()
        .ignoreIfMissing().load()
    dotenv.entries().forEach{
        System.setProperty(it.key, it.value)
    }
    runApplication<FeedlyCloneBackendApplication>(*args)
}
