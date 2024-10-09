package com.shoppin_and_go.inventory_server.extensions

fun Boolean.throwIfTrue(exception: () -> Exception) {
    if (this) throw exception()
}