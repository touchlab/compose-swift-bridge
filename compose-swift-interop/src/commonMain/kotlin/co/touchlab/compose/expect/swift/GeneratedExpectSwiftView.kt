package co.touchlab.compose.expect.swift

/**
 * Annotation used internally for the SKIE SubPlugin.
 * It should only be used by the code generator.
 */
@Target(AnnotationTarget.CLASS)
annotation class GeneratedExpectSwiftView(
    val factoryName: String,
    val type: String,
    val viewName: String,
)

