package com.shoppin_and_go.inventory_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InventoryServerApplication

fun main(args: Array<String>) {
	runApplication<InventoryServerApplication>(*args)
}
