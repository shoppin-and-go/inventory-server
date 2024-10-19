package com.shoppin_and_go.inventory_server.utils.restdoc

import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder
import com.epages.restdocs.apispec.Schema
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.web.servlet.ResultActions

inline fun ResultActions.andApiSpec(
    identifier: String,
    block: ResourceSnippetParametersBuilder.() -> ResourceSnippetParametersBuilder,
): ResultActions {
    val snippets = block.invoke(ResourceSnippetParameters.builder())
        .requestSchema(Schema("${identifier}Request"))
        .build()

    return andDo(
        document(
            identifier,
            resource(snippets),
        )
    )
}

inline fun <reified E : Exception> ResultActions.andErrorApiSpec(identifier: String): ResultActions {
    val snippets = ResourceSnippetParameters.builder()
        .tags("ErrorHandling")
        .requestSchema(Schema("${identifier}Request"))
        .responseSchema(Schema("ErrorResponse"))
        .responseFields(
            "code" type STRING means "응답 코드",
            "message" type STRING means "오류 메시지",
            "result" type OBJECT means "빈 객체",
        )
        .build()

    return andDo(
        document(
            "${identifier}_${E::class.simpleName}",
            resource(snippets),
        )
    )
}


fun ResourceSnippetParametersBuilder.requestHeaders(vararg params: RestDocsHeader) =
    requestHeaders(params.map { it.descriptor })

fun ResourceSnippetParametersBuilder.pathParameters(vararg params: RestDocsParam) =
    pathParameters(params.map { it.descriptor })

fun ResourceSnippetParametersBuilder.queryParameters(vararg params: RestDocsParam) =
    queryParameters(params.map { it.descriptor })

fun ResourceSnippetParametersBuilder.requestFields(vararg fields: RestDocsField) =
    requestFields(fields.map(RestDocsField::descriptor))

fun ResourceSnippetParametersBuilder.responseFields(vararg fields: RestDocsField) =
    responseFields(fields.map(RestDocsField::descriptor))

