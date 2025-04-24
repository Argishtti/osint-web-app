package com.ptbox.osint

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OsintApplication

fun main(args: Array<String>) {
	runApplication<OsintApplication>(*args)
}
