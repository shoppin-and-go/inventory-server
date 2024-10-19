package com.shoppin_and_go.inventory_server.utils.restdoc

import com.epages.restdocs.apispec.ParameterDescriptorWithType
import com.epages.restdocs.apispec.SimpleType

class RestDocsParam(
    val descriptor: ParameterDescriptorWithType,
) {
    infix fun isOptional(value: Boolean): RestDocsParam {
        if (value) descriptor.optional()
        return this
    }

    infix fun type(type: SimpleType): RestDocsParam {
        descriptor.type(type)
        return this
    }
}

infix fun String.pathMeans(
    description: String,
): RestDocsParam {
    return createField(this, description)
}

private fun createField(
    value: String,
    description: String,
): RestDocsParam {
    val descriptor = ParameterDescriptorWithType(value)
        .description(description)

    return RestDocsParam(descriptor)
}
