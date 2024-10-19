package com.shoppin_and_go.inventory_server.utils.restdoc

import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation

class RestDocsParam(
    val descriptor: ParameterDescriptor,
)

infix fun String.pathMeans(
    description: String,
): RestDocsParam {
    return createField(this, description)
}

private fun createField(
    value: String,
    description: String,
    optional: Boolean = false,
): RestDocsParam {
    RequestDocumentation.parameterWithName("").attributes()
    val descriptor = RequestDocumentation
        .parameterWithName(value)
        .description(description)

    if (optional) descriptor.optional()

    return RestDocsParam(descriptor)
}
