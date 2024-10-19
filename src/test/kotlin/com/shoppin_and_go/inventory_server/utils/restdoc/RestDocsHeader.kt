package com.shoppin_and_go.inventory_server.utils.restdoc

import com.epages.restdocs.apispec.HeaderDescriptorWithType
import com.epages.restdocs.apispec.SimpleType

class RestDocsHeader(
    val descriptor: HeaderDescriptorWithType,
) {
    infix fun isOptional(value: Boolean): RestDocsHeader {
        if (value) descriptor.optional()
        return this
    }

    infix fun type(type: SimpleType): RestDocsHeader {
        descriptor.type(type)
        return this
    }
}

infix fun String.headerMeans(
    description: String,
): RestDocsHeader {
    return createField(this, description)
}

private fun createField(
    value: String,
    description: String,
): RestDocsHeader {
    val descriptor = HeaderDescriptorWithType(value)
        .description(description)

    return RestDocsHeader(descriptor)
}
