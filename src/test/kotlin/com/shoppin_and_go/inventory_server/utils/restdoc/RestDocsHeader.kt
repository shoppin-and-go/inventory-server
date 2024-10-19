package com.shoppin_and_go.inventory_server.utils.restdoc

import org.springframework.restdocs.headers.HeaderDescriptor
import org.springframework.restdocs.headers.HeaderDocumentation

class RestDocsHeader(
    val descriptor: HeaderDescriptor,
)

infix fun String.headerMeans(
    description: String,
): RestDocsHeader {
    return createField(this, description)
}

private fun createField(
    value: String,
    description: String,
    optional: Boolean = false,
): RestDocsHeader {
    val descriptor = HeaderDocumentation
        .headerWithName(value)
        .description(description)

    if (optional) descriptor.optional()

    return RestDocsHeader(descriptor)
}
