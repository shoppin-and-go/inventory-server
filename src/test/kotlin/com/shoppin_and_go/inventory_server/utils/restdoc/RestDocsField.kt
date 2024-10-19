package com.shoppin_and_go.inventory_server.utils.restdoc

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation

class RestDocsField(
    val descriptor: FieldDescriptor,
) {

    private var default: String
        get() = descriptor.attributes["default"] as String
        set(value) {
            descriptor.attributes["default"] = value
        }

    private var example: String
        get() = descriptor.attributes["example"] as String
        set(value) {
            descriptor.attributes["example"] = value
        }

    infix fun means(value: String): RestDocsField {
        descriptor.description(value)

        return this
    }

    infix fun attributes(block: RestDocsField.() -> Unit): RestDocsField {
        block()
        return this
    }

    infix fun withDefaultValue(value: String): RestDocsField {
        this.default = value
        return this
    }

    infix fun example(value: String): RestDocsField {
        this.example = value
        return this
    }

    infix fun isOptional(value: Boolean): RestDocsField {
        if (value) descriptor.optional()
        return this
    }

    infix fun isIgnored(value: Boolean): RestDocsField {
        if (value) descriptor.ignored()
        return this
    }
}

infix fun String.type(
    docsFieldType: DocsFieldType,
): RestDocsField {
    return createField(this, docsFieldType.type)
}

private fun createField(
    value: String,
    type: JsonFieldType,
    optional: Boolean = true,
): RestDocsField {
    val descriptor = PayloadDocumentation.fieldWithPath(value)
        .type(type)
        .description("")

    if (optional) descriptor.optional()

    return RestDocsField(descriptor)
}
